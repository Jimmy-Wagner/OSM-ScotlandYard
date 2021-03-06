
import bentleyottmann.BentleyOttmann;
import bentleyottmann.ISegment;

import java.util.ArrayList;
import java.util.List;

public class IntersectionFinder {

    public IntersectionListener listener = new IntersectionListener();


    public IntersectionFinder() {

    }


    public void findIntersectionsNetwork(ArrayList<NetworkWay> way) {
        List<ISegment> segments = getAllSegments(way);
        // Shorten the segments for robust BO algorithm
        for (ISegment segment: segments){
            SegmentBO seg = ((SegmentBO) segment);
            seg.initializeForBO();
        }
        executeIntersectionAlgo(segments);
    }

    private void executeIntersectionAlgo(List<ISegment> segments) {
        BentleyOttmann bo = new BentleyOttmann(PointBO::new);
        bo.addSegments(segments);
        bo.setListener(listener);
        bo.findIntersections();
    }

    public List<ISegment> getAllSegments(ArrayList<NetworkWay> ways) {
        List<ISegment> segments = new ArrayList<>();
        for (NetworkWay way : ways) {
            segments.addAll(way.getSegments());
        }
        return segments;
    }


}
