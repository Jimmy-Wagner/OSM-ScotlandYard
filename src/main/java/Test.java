import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

public class Test {
    public static void main(String[] args) {
        OSM_Pbf_Reader reader = new OSM_Pbf_Reader();
        OsmDataHandler handler = reader.readOsmPbfFile();
        System.out.println("Size mit neuer methode:" + handler.getBusStopsNeu().size());



    }
}
