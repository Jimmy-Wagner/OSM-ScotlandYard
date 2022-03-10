package Types;

import bentleyottmann.IPoint;
import bentleyottmann.ISegment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;

public class SegmentBoNew implements ISegment {


    // the segment is shortened for a robust calculation of the BO algorithm and these are the endpoints
    private IPoint p1BO;
    private IPoint p2BPO;

    private RouteType routeType;
    final private IPoint p1;
    final private IPoint p2;
    private ArrayList<SegmentBoNew> splits = new ArrayList<>();



    public SegmentBoNew(@NotNull IPoint p1, @NotNull IPoint p2, RouteType type) {
        this.p1 = p1;
        this.p2 = p2;
        this.routeType = type;
    }

    public ArrayList<SegmentBoNew> getSubSegments(){
        ArrayList<SegmentBoNew> allSubSegments = new ArrayList<>();
        if (splits.isEmpty()){
            splits.add(this);
            return splits;
        }
        else{
            for (SegmentBoNew subSegment: splits){
                allSubSegments.addAll(subSegment.getSubSegments());
            }
        }
        return allSubSegments;
    }

    public void simplifyToPixel(){

    }


    public IPoint getP1() {
        return p1;
    }

    public IPoint getP2() {
        return p2;
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
        return this.routeType.toString();
    }

    public ArrayList<SegmentBoNew> getSplits() {
        return splits;
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

    public IPoint getP1BO() {
        return p1BO;
    }

    public void setP1BO(IPoint p1BO) {
        this.p1BO = p1BO;
    }

    public IPoint getP2BPO() {
        return p2BPO;
    }

    public void setP2BPO(IPoint p2BPO) {
        this.p2BPO = p2BPO;
    }

    public LineSegment getLineSegment(){
        return new LineSegment(new Coordinate(p1.x(), p1.y()), new Coordinate(p2.x(), p2.y()));
    }

    public RouteType getRouteType() {
        return routeType;
    }

    /**
     * Synchronizes the segments and nodes of the parent way.
     * @param splitPoint
     */
    public void createSplits(PointBoNew splitPoint){
        SegmentBoNew frontSplit = new SegmentBoNew(p1, splitPoint, this.routeType);
        SegmentBoNew backSplit = new SegmentBoNew(splitPoint, p2, this.routeType);
        splits.add(frontSplit);
        splits.add(backSplit);
    }

}
