package org.openstreetmap.josm.plugins.mdbuildings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.io.GeoJSONReader;
import org.openstreetmap.josm.plugins.mdbuildings.gui.NotificationPopup;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

public class CadastruAddressFetcher {

    private static final String WFS_URL = "https://cadastru.md/geoserver/w_rsuat/ows";
    private static final String WMS_URL = "https://map.cadastru.md/geoserver/w_rsuat/ows";
    private static final Pattern INITIALS_WARNING_PATTERN = Pattern.compile("\\b[A-ZĂÂÎȘȚ]\\.\\s*[A-ZĂÂÎȘȚ]\\w+");

    // Fixed search radius (~1 km). DO NOT use BuildingsSettings.BBOX_OFFSET — that is the
    // buildings cursor click size (tiny, ~0.000001°), not a useful search radius.
    private static final double ADDR_SEARCH_RADIUS = 0.01;

    public static void fetchAddresses(DataSet buildingsDataSet, LatLon centerLatLon) {
        Logging.warn("[MDBUILDINGS] fetchAddresses called. primitives={0}, center={1}",
            buildingsDataSet == null ? -1 : buildingsDataSet.allPrimitives().size(), centerLatLon);

        if (buildingsDataSet == null || buildingsDataSet.allPrimitives().isEmpty()) {
            Logging.warn("[MDBUILDINGS] Dataset null or empty, aborting.");
            return;
        }

        double minLon = centerLatLon.lon() - ADDR_SEARCH_RADIUS;
        double minLat = centerLatLon.lat() - ADDR_SEARCH_RADIUS;
        double maxLon = centerLatLon.lon() + ADDR_SEARCH_RADIUS;
        double maxLat = centerLatLon.lat() + ADDR_SEARCH_RADIUS;

        // Fetch structure points inside BBox
        String wfsUrl = String.format(
            Locale.US,
            "%s?service=WFS&version=1.0.0&request=GetFeature&typeName=w_rsuat%%3Apct_constr"
            + "&outputFormat=application%%2Fjson&srsName=EPSG:4326&bbox=%f,%f,%f,%f,EPSG:4326",
            WFS_URL, minLon, minLat, maxLon, maxLat
        );
        Logging.warn("[MDBUILDINGS] WFS URL: {0}", wfsUrl);

        DataSet pointsDataSet = null;
        String jsonResponse = null;
        try {
            jsonResponse = downloadString(wfsUrl);
            Logging.warn("[MDBUILDINGS] WFS response length: {0}", jsonResponse == null ? 0 : jsonResponse.length());
            if (jsonResponse != null && !jsonResponse.isEmpty()
                    && jsonResponse.contains("\"coordinates\"")) {
                InputStream is = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
                pointsDataSet = GeoJSONReader.parseDataSet(is, null);
            }
        } catch (Exception e) {
            Logging.warn("[MDBUILDINGS] Error fetching pct_constr: {0}", e.getMessage());
        }

        if (pointsDataSet == null || pointsDataSet.getNodes().isEmpty()) {
            Logging.warn("[MDBUILDINGS] 0 pct_constr nodes parsed. JSON snippet: {0}",
                jsonResponse != null && jsonResponse.length() > 100 ? jsonResponse.substring(0, 100) : jsonResponse);
            return;
        }
        Logging.warn("[MDBUILDINGS] Downloaded {0} pct_constr points.", pointsDataSet.getNodes().size());

        long closedWays = buildingsDataSet.allPrimitives().stream()
            .filter(p -> p instanceof Way && ((Way) p).isClosed() && ((Way) p).getNodesCount() >= 4)
            .count();
        Logging.warn("[MDBUILDINGS] Closed Ways in dataset: {0}", closedWays);

        for (OsmPrimitive primitive : buildingsDataSet.allPrimitives()) {
            if (!(primitive instanceof Way)) {
                continue;
            }
            Way building = (Way) primitive;
            if (!building.isClosed() || building.getNodesCount() < 4) {
                continue;
            }

            // Skip if building already has redundant address data (e.g. both number and street)
            if ((building.hasKey("addr:housenumber") && building.hasKey("addr:street")) 
                    || building.hasKey("addr:full")) {
                continue;
            }

            Node matchedPoint = null;
            org.openstreetmap.josm.data.osm.BBox buildingBBox = building.getBBox();
            for (Node pointNode : pointsDataSet.getNodes()) {
                // First fast BBox check, then precise polygon check
                if (buildingBBox.bounds(pointNode.getCoor())
                        && Geometry.nodeInsidePolygon(pointNode, building.getNodes())) {
                    matchedPoint = pointNode;
                    break;
                }
            }

            if (matchedPoint != null) {
                Logging.warn("[MDBUILDINGS] Matched point {0} -> fetching address.", matchedPoint.getCoor());
                fetchAndApplyAddress(building, matchedPoint.getCoor());
            }
        }
    }

    private static void fetchAndApplyAddress(OsmPrimitive building, LatLon point) {
        double delta = 0.0001;
        double minLon = point.lon() - delta;
        double minLat = point.lat() - delta;
        double maxLon = point.lon() + delta;
        double maxLat = point.lat() + delta;

        String wmsUrl = String.format(
            Locale.US,
            "%s?service=WMS&version=1.1.1&request=GetFeatureInfo"
            + "&layers=w_rsuat%%3Amv_punct_constr&query_layers=w_rsuat%%3Amv_punct_constr"
            + "&bbox=%f,%f,%f,%f&width=101&height=101&srs=EPSG:4326&x=50&y=50&info_format=application%%2Fjson",
            WMS_URL, minLon, minLat, maxLon, maxLat
        );

        try {
            String jsonResponse = downloadString(wmsUrl);
            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                String gfullname = extractProperty(jsonResponse, "gfullname");
                String gname = extractProperty(jsonResponse, "gname");
                String locName = extractProperty(jsonResponse, "loc_name");
                String streetName = extractProperty(jsonResponse, "street_name");
                String uat1Name = extractProperty(jsonResponse, "uat1_name");
                String uat3Name = extractProperty(jsonResponse, "uat3_name");
                String ido = extractProperty(jsonResponse, "ido");

                if (gfullname != null) {
                    building.put("addr:full", expandAbbreviationsAndCheck(gfullname));
                }
                if (gname != null) {
                    building.put("addr:housenumber", expandAbbreviationsAndCheck(gname));
                }
                if (locName != null) {
                    building.put("addr:city", stripSettlementType(expandAbbreviationsAndCheck(locName)));
                }
                if (streetName != null) {
                    building.put("addr:street", expandAbbreviationsAndCheck(streetName));
                }
                if (uat1Name != null) {
                    building.put("addr:district", expandAbbreviationsAndCheck(uat1Name));
                }
                if (uat3Name != null) {
                    building.put("addr:subdistrict", expandAbbreviationsAndCheck(uat3Name));
                }
                if (ido != null) {
                    building.put("mdcad_unic_id", ido); // no abbreviation check on ID
                }

                // At least one address info found?
                if (gfullname != null || gname != null || locName != null || streetName != null) {
                    building.put("addr:country", "MD");
                    Logging.info("CadastruAddressFetcher: Successfully applied address data to building.");
                } else {
                    Logging.info("CadastruAddressFetcher: WMS JSON response found no usable string property. Raw: {0}", jsonResponse);
                }
            } else {
                Logging.info("CadastruAddressFetcher: Blank/null WMS getFeatureInfo response.");
            }
        } catch (Exception e) {
            Logging.warn("Error fetching Cadastru address for matched point: {0}", e.getMessage());
        }
    }

    protected static String expandAbbreviations(String text) {
        if (text == null || text.trim().isEmpty() || text.equalsIgnoreCase("null")) {
            return null;
        }

        String result = text;
        result = result.replaceAll("(?Ui)\\bstr\\.", "Strada");
        result = result.replaceAll("(?Ui)\\bbd\\.", "Bulevardul");
        result = result.replaceAll("(?Ui)\\bstr-la\\.", "Stradela");
        result = result.replaceAll("(?Ui)\\bstr-la\\b", "Stradela");
        result = result.replaceAll("(?Ui)\\bp-\\u021Ba\\b", "Piața");
        result = result.replaceAll("(?Ui)\\bșos\\.", "Șoseaua");
        result = result.replaceAll("(?Ui)\\bpas\\.", "Pasajul");
        result = result.replaceAll("(?Ui)\\bfun\\.", "Fundătura");
        result = result.replaceAll("(?Ui)\\bmun\\.", "Municipiul");
        result = result.replace('Ş', 'Ș').replace('ş', 'ș').replace('Ţ', 'Ț').replace('ţ', 'ț');
        result = result.replaceAll("(?Ui)\\bor\\.", "Orașul");
        result = result.replaceAll("(?Ui)\\bsat\\.", "Satul");
        result = result.replaceAll("(?Ui)\\bcom\\.", "Comuna");
        result = result.replaceAll("(?Ui)\\br-nul\\b", "Raionul");
        result = result.replaceAll("(?Ui)\\bloc\\.\\s*st\\.\\s*cf\\.", "Localitatea-stație de cale ferată");
        result = result.replaceAll("(?Ui)\\bdr\\.\\s*naț\\.", "Drumul național");
        
        return result.replaceAll("\\s+", " ").trim();
    }

    /**
     * Strips standard settlement type prefixes from a city name.
     * e.g. "Municipiul Chișinău" -> "Chișinău", "Satul Dumbrava" -> "Dumbrava".
     * "Localitatea-stație de cale ferată Xyz" is left intact (it is part of the name).
     */
    protected static String stripSettlementType(String city) {
        if (city == null) {
            return null;
        }
        String[] prefixes = {"Municipiul ", "Orașul ", "Satul ", "Comuna ", "Raionul ", "UTA "};
        for (String prefix : prefixes) {
            if (city.startsWith(prefix)) {
                return city.substring(prefix.length()).trim();
            }
        }
        return city;
    }

    protected static String expandAbbreviationsAndCheck(String text) {
        String result = expandAbbreviations(text);
        if (result == null) {
            return null;
        }

        if (INITIALS_WARNING_PATTERN.matcher(result).find()) {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                NotificationPopup.showNotification("Atenție: Adresa conține o abreviere de tip inițială (ex. M. Eminescu): " + result);
            }
        }

        return result;
    }

    private static String extractProperty(String json, String propertyName) {
        Pattern pattern = Pattern.compile("\\\"" + propertyName + "\\\"\\s*:\\s*(?:\\\"([^\\\"]*)\\\"|([0-9]*\\.?[0-9]+))");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String value = matcher.group(1);
            if (value == null) {
                value = matcher.group(2);
            }
            if (value != null) {
                value = value.trim();
                if (value.isEmpty() || value.equalsIgnoreCase("null")) {
                    return null;
                }
                return value;
            }
        }
        return null;
    }

    private static String downloadString(String serverUrl) throws Exception {
        URL url = new URL(serverUrl);
        HttpClient httpClient = new Http1Client(url, "GET");
        httpClient.setConnectTimeout(BuildingsSettings.CONNECTION_TIMEOUT.get());
        httpClient.setHeader("User-Agent", DownloaderConstants.USER_AGENT);
        
        httpClient.connect();
        HttpClient.Response response = httpClient.getResponse();
        try (InputStream stream = response.getContent();
             Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
    }
}
