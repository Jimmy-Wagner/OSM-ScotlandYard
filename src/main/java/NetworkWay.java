import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NetworkWay implements Comparable{

    ArrayList<PointBO> nodes = new ArrayList<>();
    ArrayList<SegmentBO> segments = new ArrayList<>();
    private int id;


    public NetworkWay(ArrayList<PointBO> points, int id){
        this.id = id;
        this.nodes = new ArrayList<>(points);
        this.segments = buildSegmentsByNodes(points);
    }

    public NetworkWay(ReducedWay way, int id){

        this.id = id;

        PixelNode lastpoint = null;
        PointBO lastBOPoint = null;
        int loopcount = -1;
        //Converts points from reudcedway to points of a networkway
        // Initialize points and segment lists
        for (PixelNode point: way.getWayPoints()){
            PointBO thisPoint = new PointBO(point.getX(), point.getY(), point.getRouteType());
            nodes.add(thisPoint);

            if (lastpoint!=null){
                RouteType segmentType = RouteTypeCreator.segmentType(lastpoint.getRouteType(), point.getRouteType());
                segments.add(new SegmentBO(lastBOPoint, thisPoint, this, loopcount, segmentType));
            }

            lastpoint = point;
            lastBOPoint = thisPoint;
            loopcount++;
        }
    }


    public double getLength(){
        double length= 0;
        for (SegmentBO segment: segments){
            length += segment.getLineSegment().getLength();
        }
        return length;
    }

    public void addNode(int index, PointBO node){
        this.nodes.add(index, node);
    }

    public ArrayList<NetworkWay> getSplits(){
        ArrayList<NetworkWay> splitSections = new ArrayList<>();
        ArrayList<PointBO> pointsCurrentSplit = new ArrayList<>();
        for (PointBO currentNode: nodes){
            if (!currentNode.isIntersectionPoint()){
                pointsCurrentSplit.add(currentNode);
            }
            else{
                pointsCurrentSplit.add(currentNode);
                splitSections.add(new NetworkWay(pointsCurrentSplit, this.id));
                pointsCurrentSplit.clear();
                pointsCurrentSplit.add(currentNode);
            }
        }

        if (!pointsCurrentSplit.isEmpty()){
            splitSections.add(new NetworkWay(pointsCurrentSplit, this.id));
        }

        return splitSections;
    }

    public void insertNode(PointBO node, int index){
        int indexLoop = 0;
        for (PointBO point: nodes){
            if (point.getId() == node.getId()){
                nodes.remove(indexLoop);
                nodes.add(indexLoop, point);
            }
            indexLoop++;
        }
        this.nodes.add(index, node);
        this.buildSegmentsByNodes(nodes);
    }

    public int getWaySize(){
        return this.nodes.size();
    }

    public PointBO getNodeAt(int index){
        return this.nodes.get(index);
    }


    public int getId() {
        return id;
    }

    public ArrayList<PointBO> getNodes() {
        return nodes;
    }

    private ArrayList<SegmentBO> buildSegmentsByNodes(ArrayList<PointBO> nodes){
        ArrayList<SegmentBO> segments = new ArrayList<>();

        PointBO lastPoint = null;
        int loopcount = 0;
        for(PointBO node: nodes){
            if (lastPoint != null){
                RouteType segmentType = RouteTypeCreator.segmentType(lastPoint.getType(), node.getType());
                segments.add(new SegmentBO(lastPoint, node, this, loopcount++, segmentType));
            }
            lastPoint = node;
        }

        return segments;
    }


    public ArrayList<SegmentBO> getSegments(){
        return this.segments;
    }



    public boolean removeSegment(SegmentBO segment){
        int index = segments.indexOf(segment);
        boolean removed = segments.remove(segment);
        for (int i = index; i<segments.size(); i++){
            segments.get(i).setCorrespondendIndex(index);
        }
        return removed;
    }

    public void removeSegment(int index){
        // FIXME: The nodes are not synchronized with the segments anymore
        segments.remove(index);
        for (int i = index; i<segments.size(); i++){
            segments.get(i).setCorrespondendIndex(i);
        }
    }

    public void addSegment(int index, SegmentBO segment){
        segments.add(index, segment);
        for (int i = index+1; i<segments.size(); i++){
            segments.get(i).setCorrespondendIndex(i);
        }
    }

    public int indexOfSegment(SegmentBO segmentBO){
        return this.segments.indexOf(segmentBO);
    }


    @Override
    public int compareTo(@NotNull Object o) {
        if (this.getLength() < ((NetworkWay)o).getLength()){
            return 1;
        }
        else return -1;
    }

    @Override
    public String toString() {
        return
                "nodes=" + nodes +
                "   id=" + id;
    }
}
