package Types;

import bentleyottmann.IPoint;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;

public class PointBO implements IPoint, Comparable {

    private double x;
    private double y;
    private int id;
    private boolean connectTwoWays = false;
    private boolean isIntersectionPoint = false;
    private HashSet<PointBO> snapBrothers = new HashSet<>();
    private boolean wasMerged = false;
    private RouteType type;

    public PointBO(double x, double y, RouteType type){
        this.x = x;
        this.y = y;
        this.id = encodeNumbers((int) x,(int) y);
        this.type = type;
    }

    public PointBO(double x, double y){
        this(x, y, null);
    }

    public void addType(RouteType type){
        if (this.type == null){
            this.type = type;
        }
        else{
            this.type = RouteTypeCreator.combineTypes(this.type, type);
        }
    }

    public RouteType getType() {
        return type;
    }

    public void setType(RouteType type) {
        this.type = type;
    }

    public boolean isWasMerged() {
        return wasMerged;
    }

    public void setWasMerged(boolean wasMerged) {
        this.wasMerged = wasMerged;
    }

    public HashSet<PointBO> getSnapBrothers() {
        return snapBrothers;
    }

    public void setX(double x) {
        this.x = x;
        this.id = encodeNumbers((int) x, (int) y);
    }

    public void setY(double y) {
        this.y = y;
        this.id = encodeNumbers((int) x, (int) y);
    }

    public double distance(PointBO node){
        Coordinate thisC = this.getCoordinate();
        Coordinate nodeC = node.getCoordinate();
        return thisC.distance(nodeC);
    }

    public void addSnapBrother(PointBO snap){
        snapBrothers.add(snap);
    }

    public int getId() {
        return id;
    }

    public Coordinate getCoordinate(){
        return new Coordinate(x, y);
    }



    @Override
    public String toString() {
        return /*
                "( " + x +
                " | " + y +
                ")";*/
        "intersec: " + isIntersectionPoint + " | " + type.toString();
    }



    public int[] decodeNumber(int num){
        var w = (int)(Math.sqrt(8 * num + 1) - 1) / 2;
        var t = (int)(Math.pow(w, 2) + w) / 2;
        int y = (int)(num - t);
        int x = (int)(w - y);
        return new int[] { x, y };
    }

    /**
     * Encodes two integers to one unique integer with the cantor pairing function.
     * @param x
     * @param y
     * @return
     */
    private int encodeNumbers(int x, int y){
        return (((x + y) * (x + y + 1)) / 2) + y;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }


    public boolean isConnectTwoWays() {
        return connectTwoWays;
    }

    public void project(PointBO destiny){
        this.x = destiny.x();
        this.y = destiny.y();
        this.id = encodeNumbers((int) x, (int) y);
    }

    public void project(SegmentBO segmentBO){
        LineSegment lineSegment = segmentBO.getLineSegment();
        Coordinate projected = lineSegment.project(this.getCoordinate());
        this.x = projected.getX();
        this.y = projected.getY();
        this.id = encodeNumbers((int) x,(int) y);

    }

    public void setConnectTwoWays(boolean connectTwoWays) {
        this.connectTwoWays = connectTwoWays;
    }

    public boolean isIntersectionPoint() {
        return isIntersectionPoint;
    }

    public void setIntersectionPoint(boolean intersectionPoint) {
        isIntersectionPoint = intersectionPoint;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (this.getId() < ((PointBO)o).getId()){
            return -1;
        }
        else{
            return 1;
        }
    }
}
