import bentleyottmann.IPoint;
import org.locationtech.jts.geom.Coordinate;

public class PointBoNew implements IPoint {
    private double x;
    private double y;
    private int id;
    private boolean isIntersectionPoint;

    public PointBoNew(double x, double y){
        this.x = x;
        this.y = y;
        this.id = encodeNumbers((int) x, (int) y);
    }
    public PointBoNew(){

    }

    public PointBoNew(int id){
        int [] xy = decodeNumber(id);
        this.x = xy[0];
        this.y = xy[1];
        this.id = id;
    }

    public Coordinate getCoordinate(){
        return new Coordinate(x, y);
    }

    public int[] decodeNumber(int num){
        int w = (int)(Math.sqrt(8 * num + 1) - 1) / 2;
        int t = (int)(Math.pow(w, 2) + w) / 2;
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


    public int getId() {
        this.id = encodeNumbers((int) x, (int) y);
        return id;
    }

    @Override
    public double x() {
        return this.x;
    }

    @Override
    public double y() {
        return this.y;
    }

    public boolean isIntersectionPoint() {
        return isIntersectionPoint;
    }

    public void setIntersectionPoint(boolean intersectionPoint) {
        isIntersectionPoint = intersectionPoint;
    }


    @Override
    public String toString() {
        return " { " + x + " |  "+ y + " }" + "id: " + id;
    }
}
