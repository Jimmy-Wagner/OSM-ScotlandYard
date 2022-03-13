import bentleyottmann.BentleyOttmann;
import bentleyottmann.ISegment;

import java.util.List;

public class IntersectionFinderNew {

    public IntersectionListenerNew listener = new IntersectionListenerNew();

    public IntersectionFinderNew(){

    }

    public void findIntersections(List<ISegment> segments) {
        // Shorten the segments for robust BO algorithm
        for (ISegment segment: segments){
            ((SegmentBoNew) segment).initializeForBO();
        }
        executeIntersectionAlgo(segments);
    }

    private void executeIntersectionAlgo(List<ISegment> segments) {
        BentleyOttmann bo = new BentleyOttmann(PointBoNew::new);
        bo.addSegments(segments);
        bo.setListener(listener);
        bo.findIntersections();
    }
}
