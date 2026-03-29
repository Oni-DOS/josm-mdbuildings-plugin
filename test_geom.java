import java.util.Arrays;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Geometry;
public class test_geom {
    public static void main(String[] args) {
        Node p = new Node(new LatLon(47.0106, 28.8266));
        Node v1 = new Node(new LatLon(47.0105, 28.8265));
        Node v2 = new Node(new LatLon(47.0105, 28.8267));
        Node v3 = new Node(new LatLon(47.0107, 28.8267));
        Node v4 = new Node(new LatLon(47.0107, 28.8265));
        System.out.println("Result: " + Geometry.nodeInsidePolygon(p, Arrays.asList(v1, v2, v3, v4, v1)));
    }
}
