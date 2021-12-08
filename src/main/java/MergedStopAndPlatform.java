import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

public class MergedStopAndPlatform {
    // stops are always nodes
    private long stopID;
    // platforms could be nodes, ways and relations
    private long platformID;

    public MergedStopAndPlatform(long stopID, long platformID){
        this.stopID = stopID;
        this.platformID = platformID;
    }
}
