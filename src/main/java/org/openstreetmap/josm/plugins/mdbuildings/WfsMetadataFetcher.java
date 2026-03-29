package org.openstreetmap.josm.plugins.mdbuildings;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WfsMetadataFetcher {

    public static List<String> fetchLayers(String serverUrl) throws Exception {
        String url = buildCapabilitiesUrl(serverUrl);
        Logging.info("Fetching WFS layers from: " + url);
        
        try (InputStream is = download(url)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            
            List<String> layers = new ArrayList<>();
            NodeList featureTypes = doc.getElementsByTagNameNS("*", "FeatureType");
            for (int i = 0; i < featureTypes.getLength(); i++) {
                Node node = featureTypes.item(i);
                NodeList children = node.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    if (child.getLocalName() != null && child.getLocalName().equals("Name")) {
                        layers.add(child.getTextContent());
                    }
                }
            }
            return layers;
        }
    }

    public static List<String> fetchAttributes(String serverUrl, String typeName) throws Exception {
        String url = buildDescribeFeatureUrl(serverUrl, typeName);
        Logging.info("Fetching WFS attributes from: " + url);
        
        try (InputStream is = download(url)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            
            List<String> attributes = new ArrayList<>();
            NodeList elements = doc.getElementsByTagNameNS("*", "element");
            for (int i = 0; i < elements.getLength(); i++) {
                Node node = elements.item(i);
                Node nameAttr = node.getAttributes().getNamedItem("name");
                if (nameAttr != null) {
                    String name = nameAttr.getNodeValue();
                    // Skip technical geometry/id fields common in WFS
                    if (!name.equalsIgnoreCase("geom")
                            && !name.equalsIgnoreCase("msGeometry")
                            && !name.equalsIgnoreCase("the_geom")) {
                        attributes.add(name);
                    }
                }
            }
            return attributes;
        }
    }

    private static String buildCapabilitiesUrl(String serverUrl) {
        String separator = serverUrl.contains("?") ? "&" : "?";
        if (serverUrl.endsWith("?") || serverUrl.endsWith("&")) {
            separator = "";
        }
        return serverUrl + separator + "service=WFS&version=1.0.0&request=GetCapabilities";
    }

    private static String buildDescribeFeatureUrl(String serverUrl, String typeName) {
        String separator = serverUrl.contains("?") ? "&" : "?";
        if (serverUrl.endsWith("?") || serverUrl.endsWith("&")) {
            separator = "";
        }
        return serverUrl + separator + "service=WFS&version=1.0.0&request=DescribeFeatureType&typeName=" + typeName;
    }

    private static InputStream download(String serverUrl) throws Exception {
        URL url = new URL(serverUrl);
        HttpClient httpClient = new Http1Client(url, "GET");
        httpClient.setConnectTimeout(BuildingsSettings.CONNECTION_TIMEOUT.get());
        httpClient.connect();
        HttpClient.Response response = httpClient.getResponse();
        return response.getContent();
    }
}
