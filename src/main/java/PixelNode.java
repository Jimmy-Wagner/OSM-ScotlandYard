import org.locationtech.jts.geom.Coordinate;
import pl.luwi.series.reducer.Point;

public class PixelNode implements Point, Cloneable {
    private long id;
    private double pixelX;
    private double pixelY;
    private boolean intersectionOrEndpoint;
    private boolean isNodeOfOtherWay = false;
    private boolean connectTwoWays = false;
    private RouteType routeType;

    public PixelNode(PixelNode original) {
        this.id = original.getId();
        this.pixelX = original.getX();
        this.pixelY = original.getY();
        this.intersectionOrEndpoint = original.isIntersectionOrEndpoint();
        this.isNodeOfOtherWay = original.isNodeOfOtherWay();
        this.routeType = original.getRouteType();
        this.isProjectedNode = original.isProjectedNode();
        this.projectedWay = original.getProjectedWay();
    }



    public void setConnectTwoWays(boolean connectTwoWays) {
        this.connectTwoWays = connectTwoWays;
    }

    public PixelNode(long encodedNumber){
        int [] pixelXY = decodeNumber(encodedNumber);
        this.pixelX = pixelXY[0];
        this.pixelY = pixelXY[1];
    }

    public int[] decodeNumber(long num) {
        int w = (int) (Math.sqrt(8 * num + 1) - 1) / 2;
        int t = (int) (Math.pow(w, 2) + w) / 2;
        int y = (int) (num - t);
        int x = (int) (w - y);
        return new int[]{x, y};
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

    // Indicates wheter this node was merged to by another way
    // Is saved to split ways in an easy manner
    private boolean isProjectedNode = false;
    private ReducedWay projectedWay = null;

    public PixelNode(double x, double y){
        this(x, y, false);
    }

    public PixelNode(double x, double y, RouteType type){
        this(x, y, false);
        this.routeType = type;
        this.id = encodeNumbers((int) x, (int) y);
    }


    public PixelNode(double x, double y, boolean intersectionOrEndpoint){
        this.pixelX = x;
        this.pixelY = y;
        this.intersectionOrEndpoint = intersectionOrEndpoint;
        this.id = encodeNumbers((int) x, (int) y);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new PixelNode(this.pixelX, this.pixelY);
    }

    @Override
    public String toString() {
        return this.pixelX + " | " + this.pixelY + " | " + this.routeType.toString();
    }

    public void setNodeOfOtherWay(boolean nodeOfOtherWay) {
        isNodeOfOtherWay = nodeOfOtherWay;
    }

    public boolean isNodeOfOtherWay() {
        return isNodeOfOtherWay;
    }

    public ReducedWay getProjectedWay() {
        return projectedWay;
    }

    public void setProjectedWay(ReducedWay projectedWay) {
        this.projectedWay = projectedWay;
    }

    public boolean isProjectedNode() {
        return isProjectedNode;
    }

    public void setProjectedNode(boolean projectedNode) {
        isProjectedNode = projectedNode;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public void setRouteType(RouteType routeType) {
        this.routeType = routeType;
    }

    @Override
    public boolean equals(Object obj) {
        PixelNode node = (PixelNode) obj;
        if (node.getX() == this.getX() && node.getY() == this.getY()){
            return true;
        }
        else{
            return false;
        }
    }

    public Coordinate getCoordinate(){
        return new Coordinate(this.pixelX, this.pixelY);
    }

    @Override
    public double getX() {
        return pixelX;
    }

    @Override
    public double getY() {
        return pixelY;
    }

    public void setPixelX(double x){
        this.pixelX = x;
        this.id = encodeNumbers((int) pixelX, (int) pixelY);
    }

    public void setPixelY(double pixelY) {
        this.pixelY = pixelY;
        this.id = encodeNumbers((int) pixelX, (int) pixelY);
    }

    public boolean isIntersectionOrEndpoint(){
        return intersectionOrEndpoint;
    }


    public long getId() {
        return id;
    }
}
