import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

public class SegmentStroke {

    private long id;
    private long idStartNode;
    private long idEndNode;
    private Coordinate startNode;
    private Coordinate endNode;
    private RouteType type;

    public SegmentStroke(){

    }

    public SegmentStroke(PixelNode node1, PixelNode node2, RouteType type){
        Coordinate c1 = node1.getCoordinate();
        Coordinate c2 = node2.getCoordinate();
        this.startNode = c1;
        this.endNode = c2;
        this.idStartNode = encodeNumbers((int) c1.getX(), (int) c1.getY());
        this.idEndNode = encodeNumbers((int) c2.getX(), (int) c2.getY());
        this.type = type;
    }

    public SegmentStroke(long id, long startNode, long endNode) {
        this.id = id;
        this.idStartNode = startNode;
        this.idEndNode = endNode;
        this.startNode = getFirstCoord();
        this.endNode = getSecondCoord();
    }

    public SegmentStroke(long id, long idStartNode, long idEndNode, RouteType type){
        this(id, idStartNode, idEndNode);
        this.type = type;
    }

    public SegmentBoNew toSegmentBoNew(){
        PointBoNew p1 = new PointBoNew((int) idStartNode);
        PointBoNew p2 = new PointBoNew((int) idEndNode);
        SegmentBoNew segment = new SegmentBoNew(p1, p2, this.type);
        return segment;

    }

    public LineSegment getLineSegment() {
        int[] pixelXYStart = decodeNumber(this.idStartNode);
        int[] pixelXYEnd = decodeNumber(this.idEndNode);
        Coordinate c1 = new Coordinate(pixelXYStart[0], pixelXYStart[1]);
        Coordinate c2 = new Coordinate(pixelXYEnd[0], pixelXYEnd[1]);

        return new LineSegment(c1, c2);
    }

    public Coordinate getStartNode() {
        return startNode;
    }

    public Coordinate getEndNode() {
        return endNode;
    }

    public void setStartNode(Coordinate startNode) {
        this.startNode = startNode;
    }

    public void setEndNode(Coordinate endNode) {
        this.endNode = endNode;
    }

    public Coordinate getFirstCoord(){
        int [] pixelXY = decodeNumber(idStartNode);
        return new Coordinate(pixelXY[0], pixelXY[1]);
    }

    public Coordinate getSecondCoord(){
        int [] pixelXY = decodeNumber(idEndNode);
        return new Coordinate(pixelXY[0], pixelXY[1]);
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdStartNode() {
        return idStartNode;
    }

    public void setIdStartNode(long idStartNode) {
        this.idStartNode = idStartNode;
    }

    public long getIdEndNode() {
        return idEndNode;
    }

    public void setIdEndNode(long idEndNode) {
        this.idEndNode = idEndNode;
    }

    /*public double angle(SegmentStroke seg) {
        LineSegment thisLineSeg = this.getLineSegment();
        LineSegment parameterLineSeg = seg.getLineSegment();
        double angleThis = Math.abs(thisLineSeg.angle() * 180 / Math.PI);
        double angleParameter = Math.abs(parameterLineSeg.angle() * 180 / Math.PI);
        double angleThisNormalized = AngleHelper.angleToFirstQuadrant(angleThis);
        double angleParamterNormalized = AngleHelper.angleToFirstQuadrant(angleParameter);
        double angleBetween = Math.abs(angleThisNormalized - angleParamterNormalized);

        return angleBetween;
    }*/

    private int encodeNumbers(int x, int y){
        return (((x + y) * (x + y + 1)) / 2) + y;
    }

    public int[] decodeNumber(long num) {
        int w = (int) (Math.sqrt(8 * num + 1) - 1) / 2;
        int t = (int) (Math.pow(w, 2) + w) / 2;
        int y = (int) (num - t);
        int x = (int) (w - y);
        return new int[]{x, y};
    }

    public double angleToSegment(SegmentStroke seg) {
        double angle1 = Math.atan2(this.startNode.getY() - this.endNode.getY(),
                this.startNode.getX() - this.endNode.getX());
        double angle2 = Math.atan2(seg.getStartNode().getY() - seg.getEndNode().getY(),
                seg.getStartNode().getX() - seg.getEndNode().getX());
        double angleRad = Math.abs(angle1) - Math.abs(angle2);
        return Angle.toDegrees(angleRad);
    }

    public double angleNew(SegmentStroke seg){
        return Angle.toDegrees(Angle.angleBetween(this.endNode, this.startNode, seg.endNode));
    }

    public SegmentStroke nodeIsFirst(long nodeId){
        if (nodeId == this.idStartNode){
            return this;
        }
        else{
            return new SegmentStroke(this.id, idEndNode, idStartNode);
        }
    }

    public SegmentStroke nodeIsLast(long nodeId){
        if (nodeId == this.idEndNode){
            return this;
        }
        else{
            return new SegmentStroke(this.id, idEndNode, idStartNode);
        }
    }

    public RouteType getType() {
        return type;
    }

    public SegmentStroke reverse(){
        return new SegmentStroke(this.id, this.idEndNode, this.idStartNode);
    }

    @Override
    public String toString() {
        return "SegmentStroke{" +
                "id=" + id +
                ", idStartNode=" + idStartNode +
                ", idEndNode=" + idEndNode;
    }
}
