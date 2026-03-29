package org.openstreetmap.josm.plugins.mdbuildings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.io.GeoJSONReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

public class BuildingsDownloader {
    /**
     * Download buildings from MD-Buildings V2 Server API and parse it as DataSets.
     * It also updates the status on any error.
     *
     * @param manager ImportManager which contains DataSourceConfig for current download.
     * @return BuildingsImportData with downloaded data or empty datasets or null if IO/parse error
     */
    public static BuildingsImportData getBuildingsImportData(BuildingsImportManager manager) {
        String url = buildUrl(manager.getDataSourceConfig(), manager.getCursorLatLon(), manager.getCurrentProfile());
        try {
            InputStream responseStream = download(url);
            BuildingsImportData data = parseData(responseStream, manager.getCurrentProfile());
            if (data != null) {
                DataSet geometryDs = data.get(manager.getCurrentProfile().getGeometry());
                CadastruAddressFetcher.fetchAddresses(geometryDs, manager.getCursorLatLon());
            }
            return data;
        } catch (IOException ioException) {
            Logging.warn("Connection error with getting building data: {0}", ioException.getMessage());
            manager.setStatus(ImportStatus.CONNECTION_ERROR, ioException.getMessage());
        } catch (Exception parseAndOtherExceptions) {
            Logging.error("Parsing error – dataset from the server: {0}", parseAndOtherExceptions.getMessage());
            manager.setStatus(ImportStatus.IMPORT_ERROR, parseAndOtherExceptions.getMessage());
        }
        return null;
    }

    /**
     * @param latLon location of searching building (EPSG 4326)
     */
    public static String buildUrl(DataSourceConfig dataSourceConfig, LatLon latLon, DataSourceProfile currentProfile) {
        String serverBaseApiUrl = dataSourceConfig.getServerByName(currentProfile.getDataSourceServerName()).getUrl();

        double offset = BuildingsSettings.BBOX_OFFSET.get();
        double minLon = latLon.lon() - offset;
        double minLat = latLon.lat() - offset;
        double maxLon = latLon.lon() + offset;
        double maxLat = latLon.lat() + offset;
        String typeName = currentProfile.getGeometry();

        String separator = serverBaseApiUrl.contains("?") ? "&" : "?";
        if (serverBaseApiUrl.endsWith("?") || serverBaseApiUrl.endsWith("&")) {
            separator = "";
        }

        return String.format(
            Locale.US,
            "%s%sservice=WFS&version=1.0.0&request=GetFeature&typeName=%s"
            + "&outputFormat=application/json&srsName=EPSG:4326&bbox=%f,%f,%f,%f,EPSG:4326",
            serverBaseApiUrl, separator, typeName, minLon, minLat, maxLon, maxLat
        );
    }

    /**
     * @param serverUrl full url to make request (with params etc.)
     */
    static InputStream download(String serverUrl) throws IOException {
        Logging.info("Getting buildings data from: {0}", serverUrl);

        URL url = new URL(serverUrl);
        HttpClient httpClient = new Http1Client(url, "GET");
        httpClient.setConnectTimeout(BuildingsSettings.CONNECTION_TIMEOUT.get());
        httpClient.setHeader("User-Agent", DownloaderConstants.USER_AGENT);
        httpClient.connect();
        HttpClient.Response response = httpClient.getResponse();

        return response.getContent();
    }

    static BuildingsImportData parseData(InputStream responseStream,
                                         DataSourceProfile profile) throws IllegalDataException, IOException {
        // Read the entire stream into a string
        String jsonString;
        try (Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            jsonString = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }

        BuildingsImportData dataSourceBuildingsData = new BuildingsImportData();

        // The WFS returns 3D coordinates: [lon, lat, z]. 
        // JOSM GeoJSONReader expects 2D coordinates: [lon, lat].
        // Regex to match an array of 3 numbers and capture the first two, dropping the third.
        // e.g. [28.1, 48.0, 115.5] -> [28.1, 48.0]
        jsonString = jsonString.replaceAll(
            "\\[\\s*([+-]?([0-9]*[.])?[0-9]+)" 
            + "\\s*,\\s*([+-]?([0-9]*[.])?[0-9]+)"
            + "\\s*,\\s*[+-]?([0-9]*[.])?[0-9]+\\s*\\]",
            "[$1, $3]"
        );

        InputStream modifiedStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
        DataSet dataSet = GeoJSONReader.parseDataSet(modifiedStream, null);
        for (OsmPrimitive primitive : dataSet.allPrimitives()) {
            // Basic tags
            // 1. Tag Filtering (Include/Exclude) from WFS
            String includeStr = profile.getTagsToInclude();
            String excludeStr = profile.getTagsToExclude();

            Map<String, String> currentKeys = new HashMap<>(primitive.getKeys());

            // Handle Inclusion (Whitelist)
            if (includeStr != null && !includeStr.trim().isEmpty()) {
                Set<String> includeSet = Arrays.stream(includeStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
                // We must preserve 'z' if it's needed for height calculation later, 
                // even if not in the include list, but only if it's actually present.
                for (String key : currentKeys.keySet()) {
                    if (!includeSet.contains(key) && !key.equals("z")) {
                        primitive.remove(key);
                    }
                }
            }

            // Handle Exclusion (Blacklist)
            if (excludeStr != null && !excludeStr.trim().isEmpty()) {
                Set<String> excludeSet = Arrays.stream(excludeStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
                for (String key : excludeSet) {
                    primitive.remove(key);
                }
            }

            // Translate NRCASA to addr:housenumber
            if (primitive.hasKey("NRCASA")) {
                primitive.put("addr:housenumber", primitive.get("NRCASA"));
                primitive.remove("NRCASA");
            }

            // 2. Add Plugin Mandatory Tags
            primitive.put("building", "yes");
            primitive.put("source", "AGCC/Linemap2017");

            // 3. Height calculation from absolute z
            String value = primitive.get("z");
            if (value != null) {
                try {
                    double absoluteZ = Double.parseDouble(value);
                    BBox bbox = primitive.getBBox();
                    if (bbox != null) {
                        LatLon center = bbox.getCenter();
                        double terrainElevation = DtmElevationFetcher.getElevation(center);
                        if (!Double.isNaN(terrainElevation)) {
                            double relativeHeight = absoluteZ - terrainElevation;
                            // Round to nearest 0.1
                            relativeHeight = Math.round(relativeHeight * 10.0) / 10.0;
                            if (relativeHeight < 0) {
                                relativeHeight = 0;
                            }
                            primitive.put("height", String.valueOf(relativeHeight));
                        }
                    }
                } catch (NumberFormatException e) {
                    Logging.warn("Could not parse z value: {0}", value);
                }
                primitive.remove("z");
            }
        }

        dataSourceBuildingsData.add(profile.getGeometry(), dataSet);
        if (!profile.getGeometry().equals(profile.getTags())) {
            dataSourceBuildingsData.add(profile.getTags(), dataSet);
        }

        return dataSourceBuildingsData;
    }
}
