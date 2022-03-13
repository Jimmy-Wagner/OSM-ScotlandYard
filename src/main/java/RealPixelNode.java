import org.locationtech.jts.geom.Coordinate;

import java.util.HashSet;

public class RealPixelNode {
    private int x;
    private int y;
    private int id;
    private HashSet<RealPixelNode> connectedNodes = new HashSet<>();
    private boolean isEndPoint;
    private boolean isConnectionPoint;
    private boolean isIntersectionPoint;

    public RealPixelNode(int x, int y){
        this.x = x;
        this.y = y;
        this.id = encodeNumbers(x,y);
    }

    public RealPixelNode(int id){
        int [] xy = decodeNumber(id);
        this.x = xy[0];
        this.y = xy[1];
        this.id = id;
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        this.id = encodeNumbers(x,y);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        this.id = encodeNumbers(x,y);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        int []xy = decodeNumber(id);
        this.x = xy[0];
        this.y = xy[1];
    }

    public void setEndPoint(boolean endPoint) {
        isEndPoint = endPoint;
    }

    public void addConnectedNode(RealPixelNode node){
        this.connectedNodes.add(node);
    }

    public void addConnectionNodes(HashSet<RealPixelNode> nodes){
        this.connectedNodes.addAll(nodes);
    }

    public int sizeConnectionList(){
        return this.connectedNodes.size();
    }

    public double distance (RealPixelNode node){
        return getCoordinate().distance(node.getCoordinate());
    }

    public Coordinate getCoordinate(){
        return new Coordinate(this.x, this.y);
    }

    public HashSet<RealPixelNode> getConnectedNodes() {
        return connectedNodes;
    }



    public void setConnectionPoint(boolean connectionPoint) {
        isConnectionPoint = connectionPoint;
    }

    public void setIntersectionPoint(boolean intersectionPoint) {
        isIntersectionPoint = intersectionPoint;
    }
}
