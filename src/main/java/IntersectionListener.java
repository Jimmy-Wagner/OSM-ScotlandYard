import bentleyottmann.IPoint;
import bentleyottmann.ISegment;
import bentleyottmann.OnIntersectionListener;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Monitors intersection while bentley ottman algorithm is executing and splits the segments at every intersection point into smaller segments in the
 * containing ways.
 * The Ways still needs to be splitted into several split sections.
 */
public class IntersectionListener implements OnIntersectionListener {


    // Saves all intersection points with their involved segments
    // The segments contain a pointer to their corresponding way
    private HashMap<ISegment, List<Coordinate>> intersectionPointsOfSegments = new HashMap<>();

    public HashSet<IPoint> intersections = new HashSet<>();


    @Override
    public void onIntersection(@NotNull ISegment iSegment, @NotNull ISegment iSegment1, @NotNull IPoint iPoint) {


        //System.out.println("Intersec: " + iPoint.x() + " | " + iPoint.y());


        SegmentBO segment1 = (SegmentBO) iSegment;
        SegmentBO segment2 = (SegmentBO) iSegment1;
        PointBO intersec = (PointBO) iPoint;
        intersec.setIntersectionPoint(true);


        // Splits this segment or the split sections of this segment when it was already splitted
        // Synchronizes implicitly the network way which contains the segment
        if(newIntersection(segment1, intersec)){
            splitOnChild(segment1, intersec);
        }
        if (newIntersection(segment2, intersec)){
            splitOnChild(segment2, intersec);
        }

        Container.intersections.add(intersec);
    }


    public boolean newIntersection(SegmentBO segment, IPoint point){

        Coordinate pointC = new Coordinate(point.x(), point.y());

        if (intersectionPointsOfSegments.get(segment) == null){
            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(pointC);
            intersectionPointsOfSegments.put(segment, coordinates);
            return true;
        }

        for (Coordinate c: intersectionPointsOfSegments.get(segment)){
            if (c.equals2D(pointC)){
                return false;
            }
        }

        List<Coordinate> currentPoints = intersectionPointsOfSegments.get(segment);
        currentPoints.add(pointC);

        return true;

    }


    public void splitOnChild(SegmentBO segment, PointBO intersec){
        if (segment.getSplits().isEmpty()){
            // If the point lies on the segment
            // If it isnt lieing on the segment the other split section of the parent segment is the one which needs to be splitted
            if (isOnSegment(segment, intersec)){
                segment.createSplits(intersec);
            }

        }
        else{
            for (SegmentBO child: segment.getSplits()){
                splitOnChild(child, intersec);
            }
        }
    }

    public boolean isOnSegment(SegmentBO segment, PointBO point){
        LineSegment lineSegment = segment.getLineSegment();
        Coordinate c = point.getCoordinate();
        double distance = lineSegment.distance(c);
        // Not 0 because of rounding errors which occur when calculating intersections
        if (distance<=0.0001){
            return true;
        }
        return false;
    }

}
