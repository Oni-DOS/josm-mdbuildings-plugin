package org.openstreetmap.josm.plugins.mdbuildings;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

/**
 * Utility to fetch ground elevation from the geodata.gov.md DTM WMS layer.
 */
public class DtmElevationFetcher {

    private static final String DTM_WMS_URL = "https://geodata.gov.md/geoserver/DTM/wms";
    private static final String[] LAYER_NAMES = {
        "DTM:DTM_2020_centru", 
        "DTM:DTM_2021_Sud", 
        "DTM:DTM_5m"
    };

    /**
     * Queries the DTM WMS layer for the elevation at a given LatLon coordinate.
     *
     * @param latLon The coordinate to query.
     * @return The elevation in meters, or Double.NaN if the query fails or returns no data.
     */
    public static double getElevation(LatLon latLon) {
        if (!BuildingsSettings.USE_DTM_ELEVATION.get()) {
            return Double.NaN;
        }

        for (int attempt = 0; attempt < 2; attempt++) {
            for (String layerName : LAYER_NAMES) {
                double elev = getElevationFromLayer(latLon, layerName);
                if (!Double.isNaN(elev)) {
                    return elev;
                }
            }
        }
        return Double.NaN;
    }

    private static double getElevationFromLayer(LatLon latLon, String layerName) {
        // Create a tiny bounding box around the point
        double delta = 0.0001;
        double minLon = latLon.lon() - delta;
        double minLat = latLon.lat() - delta;
        double maxLon = latLon.lon() + delta;
        double maxLat = latLon.lat() + delta;

        String urlString = String.format(
            Locale.US,
            "%s?service=WMS&version=1.1.1&request=GetFeatureInfo"
            + "&layers=%s&query_layers=%s"
            + "&bbox=%f,%f,%f,%f&width=10&height=10&srs=EPSG:4326&x=5&y=5&info_format=application/json",
            DTM_WMS_URL, layerName, layerName, minLon, minLat, maxLon, maxLat
        );

        try {
            URL url = new URL(urlString);
            HttpClient httpClient = new Http1Client(url, "GET");
            httpClient.setConnectTimeout(BuildingsSettings.CONNECTION_TIMEOUT.get());
            httpClient.setHeader("User-Agent", DownloaderConstants.USER_AGENT);
            
            httpClient.connect();
            HttpClient.Response response = httpClient.getResponse();
            try (InputStream stream = response.getContent();
                 Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name())) {
                String jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                return parseElevationFromJson(jsonResponse);
            }
        } catch (Exception e) {
            Logging.warn("Error fetching DTM elevation for {0}: {1}", latLon, e.getMessage());
        }

        return Double.NaN;
    }

    /**
     * Parses the GRAY_INDEX from the GeoServer WMS JSON response.
     */
    static double parseElevationFromJson(String json) {
        // Simple regex to extract GRAY_INDEX value since we don't have a full JSON parser library handy
        // Example response property: "GRAY_INDEX": 202.65939331054688
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "\"GRAY_INDEX\"\\s*:\\s*([+-]?([0-9]*[.])?[0-9]+)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                Logging.warn("Failed to parse GRAY_INDEX value as double: {0}", matcher.group(1));
            }
        }
        return Double.NaN;
    }
}
