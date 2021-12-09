import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Test {
    public static void main(String[] args) {
        OSM_Pbf_Reader reader = new OSM_Pbf_Reader();
        OsmDataHandler handler = reader.readOsmPbfFile();



    }
}
