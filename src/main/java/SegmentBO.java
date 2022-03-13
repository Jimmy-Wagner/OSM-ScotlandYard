import bentleyottmann.IPoint;
import bentleyottmann.ISegment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;

public class SegmentBO implements ISegment {

    // the segment is shortened for a robust calculation of the BO algorithm and these are the endpoints
    private IPoint p1BO;
    private IPoint p2BPO;

    public void setP1BO(IPoint p1BO) {
        this.p1BO = p1BO;
    }

    public void setP2BPO(IPoint p2BPO) {
        this.p2BPO = p2BPO;
    }

    private NetworkWay segmentContainer;
    private RouteType routeType;
    final private IPoint p1;
    final private IPoint p2;
    private int correspondendIndex;
    private ArrayList<SegmentBO> splits = new ArrayList<>();

    public void setSegmentContainer(NetworkWay segmentContainer) {
        this.segmentContainer = segmentContainer;
    }

    public ArrayList<SegmentBO> getSplits() {
        return splits;
    }

    public void setCorrespondendIndex(int correspondendIndex) {
        this.correspondendIndex = correspondendIndex;
    }

    /**
     * Synchronizes the segments and nodes of the parent way.
     * @param splitPoint
     */
    public void createSplits(PointBO splitPoint){
        splitPoint.addType(this.routeType);
        SegmentBO frontSplit = new SegmentBO(p1, splitPoint, segmentContainer, correspondendIndex, this.routeType);
        SegmentBO backSplit = new SegmentBO(splitPoint, p2, segmentContainer, correspondendIndex+1, this.routeType);
        splits.add(frontSplit);
        splits.add(backSplit);
        // int index = segmentContainer.indexOfSegment(this);
        // Remove this segment from the way and insert both segment splits
        segmentContainer.removeSegment(correspondendIndex);
        segmentContainer.addNode(correspondendIndex+1, splitPoint);
        segmentContainer.addSegment(correspondendIndex, frontSplit);
        segmentContainer.addSegment(correspondendIndex+1, backSplit);
    }



    public SegmentBO(@NotNull IPoint p1, @NotNull IPoint p2, NetworkWay segmentContainer, int correspondendIndex, RouteType type) {
        this.p1 = p1;
        this.p2 = p2;
        this.segmentContainer = segmentContainer;
        this.correspondendIndex = correspondendIndex;
        this.routeType = type;
    }

    public LineSegment getLineSegment(){
        return new LineSegment(new Coordinate(p1.x(), p1.y()), new Coordinate(p2.x(), p2.y()));
    }

    public void initializeForBO(){
        LineSegment line = getLineSegment();
        double fraction1 = Math.random()/10000;
        double fraction2 = 1-fraction1;
        Coordinate c1 = line.pointAlong(fraction1);
        Coordinate c2 = line.pointAlong(fraction2);
        setP1BO(new PointBO(c1.getX(), c1.getY(), null));
        setP2BPO(new PointBO(c2.getX(), c2.getY(), null));
    }

    @Override
    public @NotNull IPoint p1() {
        return this.p1BO;
    }

    @Override
    public @NotNull IPoint p2() {
        return this.p2BPO;
    }

    @Override
    public @Nullable String name() {
        return null;
    }

    public NetworkWay getContainer(){
        return this.segmentContainer;
    }

    public int getCorrespondendIndex(){
        return this.correspondendIndex;
    }

    @Override
    public String toString() {
        return
                /*"WayID=" + segmentContainer.getId() +
                "      p1= " + p1 +
                "      p2= " + p2 +
                "      Index=" + correspondendIndex*/
                routeType.toString();
    }
}
