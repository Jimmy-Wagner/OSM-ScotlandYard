

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class StrokeBuilder {
    private ArrayList<ReducedWay> ways;
    private ArrayList<Stroke> strokes;
    private HashMap<Long, HashSet<Long>> pixelIdToConnectedPixelIdMapping = new HashMap<>();
    // Mapping from id of node to all segments this node is involved in
    private HashMap<Long, HashSet<SegmentStroke>> nodePixelIDToSegmentIDsMapping = new HashMap<>();
    private ArrayList<SegmentStroke> segments = new ArrayList<>();
    private ArrayList<Long> editedSegments = new ArrayList<>();
    private long segmentIDCounter = 0;

    public StrokeBuilder(ArrayList<ReducedWay> ways){
        this.ways = ways;
    }

    public ArrayList<PixelNode> getConnectionPoints(){
        ArrayList<Long> nodeIds = new ArrayList<>();
        for (long id: pixelIdToConnectedPixelIdMapping.keySet()){
            HashSet<Long> connectedNodes = pixelIdToConnectedPixelIdMapping.get(id);
            if (connectedNodes.size() >= 3){
                nodeIds.add(id);
            }
        }
        ArrayList<PixelNode> nodes = new ArrayList<>();
        for (long nodeId: nodeIds){
            nodes.add(new PixelNode(nodeId));
        }
        return nodes;
    }

    public void buildStrokes(){
        buildConnections();
        buildSegments();
        buildRealStrokes();
    }

    public ArrayList<Stroke> getStrokes() {
        return strokes;
    }

    public void buildRealStrokes(){
        strokes = new ArrayList<>();
        int strokeId = 0;
        for (SegmentStroke currentSeg: segments){
            // Every segment can only be contained in one stroke
            if (editedSegments.contains(currentSeg.getId())) continue;

            // Build stroke left side
            Stroke leftStroke = new Stroke(strokeId);
            leftStroke.addSegment(currentSeg.reverse());
            editedSegments.add(currentSeg.getId());
            augmentStroke(leftStroke);

            // build stroke right side
            Stroke rightStroke = new Stroke(strokeId++);
            rightStroke.addSegment(currentSeg);
            augmentStroke(rightStroke);

            // Concatenate both strokes, the result is contained in the right stroke
            concatenateStrokes(leftStroke, rightStroke);
            strokes.add(rightStroke);
        }
    }

    public void concatenateStrokes(Stroke left, Stroke right){
        for (int i = 1; i<left.segmentSize(); i++){
            SegmentStroke currentToInsert = left.getSegment(i);
            right.addSegment(0, currentToInsert.reverse());
        }
    }


    private void augmentStroke(Stroke stroke){
        SegmentStroke lastSegmentReversed = stroke.lastSegment().reverse();
        long idStartNode = lastSegmentReversed.getIdStartNode();
        SegmentStroke bestNextSegment = null;
        // A segment needs to exceed this angle to be inserted to this stroke
        double maxAngle = 150;
        HashSet<SegmentStroke> connectedSegments = nodePixelIDToSegmentIDsMapping.get(idStartNode);
        for (SegmentStroke connectedSegment: connectedSegments){

            if (editedSegments.contains(connectedSegment.getId())) continue;

            double currentAngle = Math.abs(lastSegmentReversed.angleNew(connectedSegment));


            if (currentAngle > maxAngle){
                maxAngle = currentAngle;
                bestNextSegment = connectedSegment;
            }
        }
        if (bestNextSegment != null){
            stroke.addSegment(bestNextSegment);
            editedSegments.add(bestNextSegment.getId());
            augmentStroke(stroke);
        }
    }



    private void buildConnections(){
        for (ReducedWay way: ways){
            buildConnectionsNew(way);
        }
    }

    public void buildConnectionsNew(ReducedWay way){
        PixelNode lastNode = null;
        for (PixelNode current: way.getWayPoints()){
            long currentId = current.getId();
            if (lastNode == null){
                lastNode = current;
                continue;
            }
            long lastId = lastNode.getId();


            // In this case two consecutive nodes are mapped to the same pixel, so dont insert the same node to its connected nodes
            if (currentId == lastId){
                continue;
            }

            setNodeMapping(lastId, currentId);
            setNodeMapping(currentId, lastId);

            lastNode = current;
        }
    }



    public void setNodeMapping(long id1, long id2){
        HashSet<Long> connectedNodesId1 = pixelIdToConnectedPixelIdMapping.get(id1);

        if (connectedNodesId1 != null){
            connectedNodesId1.add(id2);
        }
        else{
            connectedNodesId1 = new HashSet<>();
            connectedNodesId1.add(id2);
            pixelIdToConnectedPixelIdMapping.put(id1, connectedNodesId1);
        }
    }

    public void buildSegments(){

        for (long idCurrent: pixelIdToConnectedPixelIdMapping.keySet()){
            HashSet<Long> connectedIds = pixelIdToConnectedPixelIdMapping.get(idCurrent);
            HashSet<Long> copyConnected = new HashSet<>(connectedIds);
            for (long idConnected: copyConnected){
                HashSet<Long> connectedConnectedIds = pixelIdToConnectedPixelIdMapping.get(idConnected);
                SegmentStroke segment = new SegmentStroke(segmentIDCounter, idCurrent, idConnected);
                SegmentStroke segmentReverse = new SegmentStroke(segmentIDCounter++, idConnected, idCurrent);
                segments.add(segment);
                setNodeToSegmentMapping(idCurrent, segment);
                setNodeToSegmentMapping(idConnected, segmentReverse);
                connectedIds.remove(idConnected);
                connectedConnectedIds.remove(idCurrent);
            }
        }
    }

    private void buildConnections(TrimmedWay way){
        PixelNode lastNode = null;
        for (PixelNode current: way.getPixelNodes()){
            long currentId = current.getId();
            if (lastNode == null){
                lastNode = current;
                continue;
            }
            long lastId = lastNode.getId();


            // In this case two consecutive nodes are mapped to the same pixel, so dont insert the same node to its connected nodes
            if (currentId == lastId){
                continue;
            }

            setSegmentMapping(lastId, currentId);

            lastNode = current;
        }
    }

    private void setSegmentMapping(long id1, long id2){
        SegmentStroke segment = new SegmentStroke(segmentIDCounter, id1, id2);
        SegmentStroke segmentReverse = new SegmentStroke(segmentIDCounter++, id2, id1);
        segments.add(segment);
        setNodeToSegmentMapping(id1, segment);
        setNodeToSegmentMapping(id2, segmentReverse);
    }

    public void setNodeToSegmentMapping(long nodeId, SegmentStroke segment){
        HashSet<SegmentStroke> segments = nodePixelIDToSegmentIDsMapping.get(nodeId);
        if (segments != null){
            segments.add(segment);
        }
        else{
            segments = new HashSet<>();
            segments.add(segment);
            nodePixelIDToSegmentIDsMapping.put(nodeId, segments);
        }
    }
}
