package Types;

import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import java.util.ArrayList;

public class Stroke implements Comparable{
    private ArrayList<SegmentStroke> segments = new ArrayList<>();
    private ArrayList<Coordinate> nodes = new ArrayList<>();
    private int id;

    public Stroke(int id){
        this.id = id;
    }

    public void addSegment(int index, SegmentStroke segmentStroke){
        this.segments.add(index, segmentStroke);
    }

    public void addSegment(SegmentStroke segment){
        this.segments.add(segment);
    }

    public SegmentStroke lastSegment(){
        return segments.get(segments.size()-1);
    }

    public int segmentSize(){
        return segments.size();
    }

    public ArrayList<Coordinate> getNodes(){
        boolean first = true;
        for (SegmentStroke seg: segments){
            if (first){
                nodes.add(seg.getStartNode());
                first = false;
            }
            nodes.add(seg.getEndNode());
        }
        return nodes;
    }

    public SegmentStroke getSegment(int index){
        return segments.get(index);
    }

    public double length(){
        double lengthOverall = 0;
        for (SegmentStroke seg: segments){
            LineSegment lineSeg = seg.getLineSegment();
            lengthOverall += lineSeg.getLength();
        }
        return lengthOverall;
    }


    @Override
    public int compareTo(@NotNull Object o) {
        Stroke way2 = (Stroke) o;
        if (this.length() > way2.length()) {
            return -1;
        } else if (this.length() < way2.length()) {
            return 1;
        }
        return 0;
    }

    public ReducedWay toReducedWay(){
        getNodes();
        ArrayList<PixelNode> pixelNodes = new ArrayList<>();
        for (Coordinate coord: nodes){
            pixelNodes.add(new PixelNode(coord.x, coord.y));
        }
        return new ReducedWay(pixelNodes, 0);
    }
}
