package DataManipulation.BO;

import HelperClasses.INtersectionContainer;
import Types.PointBO;
import Types.PointBoNew;
import Types.SegmentBO;
import Types.SegmentBoNew;
import bentleyottmann.IPoint;
import bentleyottmann.ISegment;
import bentleyottmann.OnIntersectionListener;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IntersectionListenerNew implements OnIntersectionListener {

    // Saves all intersection points with their involved segments
    // The segments contain a pointer to their corresponding way
    private HashMap<ISegment, List<Coordinate>> intersectionPointsOfSegments = new HashMap<>();

    @Override
    public void onIntersection(@NotNull ISegment iSegment, @NotNull ISegment iSegment1, @NotNull IPoint iPoint) {

        SegmentBoNew segment1 = (SegmentBoNew) iSegment;
        SegmentBoNew segment2 = (SegmentBoNew) iSegment1;
        PointBoNew intersec = (PointBoNew) iPoint;
        intersec.setIntersectionPoint(true);

        INtersectionContainer.intersections.add(intersec);

        // Splits this segment or the split sections of this segment when it was already splitted
        // Synchronizes implicitly the network way which contains the segment
        if(newIntersection(segment1, intersec)){
            splitOnChild(segment1, intersec);
        }
        if (newIntersection(segment2, intersec)){
            splitOnChild(segment2, intersec);
        }
    }

    public boolean newIntersection(SegmentBoNew segment, IPoint point){

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


    public void splitOnChild(SegmentBoNew segment, PointBoNew intersec){
        if (segment.getSplits().isEmpty()){
            // If the point lies on the segment
            // If it isnt lieing on the segment the other split section of the parent segment is the one which needs to be splitted
            if (isOnSegment(segment, intersec)){
                segment.createSplits(intersec);
            }

        }
        else{
            for (SegmentBoNew child: segment.getSplits()){
                splitOnChild(child, intersec);
            }
        }
    }

    public boolean isOnSegment(SegmentBoNew segment, PointBoNew point){
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
