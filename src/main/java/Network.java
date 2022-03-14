
import bentleyottmann.ISegment;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.SeriesReducer;

import java.util.*;

/**
 * Receives only the ways that are within the scope of the static map image.
 * Provides functionality for merging the ways so that they constitute a "good" topology for the sctoland yard game.
 */
public class Network {

    final private RouteType routeType;
    private ArrayList<NetworkWay> snappedWays;
    // Are initialized after calling dp method or when a network is created by merging two other networks which are already simplified with dp algo
    private ArrayList<ReducedWay> mergedWays;
    private ArrayList<TrimmedWay> originalWays;
    private ValueSelector selector;
    private ArrayList<Stroke> busStrokes;
    private WayManipulator manipulator = new WayManipulator();
    private IntersectionFinder intersecFinder = new IntersectionFinder();
    private HashMap<Long, HashSet<Long>> connectionMapping = new HashMap<>();
    private ArrayList<SegmentBoNew> segments = new ArrayList<>();
    private HashMap<Integer, RealPixelNode> idToRealNode = new HashMap<Integer, RealPixelNode>();
    private HashMap<Integer, HashSet<SegmentBoNew>> nodeToSegments = new HashMap<>();
    private ArrayList<PixelSegment> pixelSegments = new ArrayList<>();
    private ArrayList<RealPixelNode> connectionNodes = new ArrayList<>();
    private ArrayList<RealPixelNode> endNodes = new ArrayList<>();

    public Network(RouteType routeType, ArrayList<TrimmedWay> originalWays){
        this.routeType = routeType;
        this.originalWays = originalWays;
        this.selector = new ValueSelector(routeType);
    }

    /**
     * For networks that are a result of merged networks
     * @param mergedWays
     * @param routeType
     */
    public Network(ArrayList<ReducedWay> mergedWays, RouteType routeType){
        this.routeType = routeType;
        this.mergedWays = mergedWays;
        this.selector = new ValueSelector(routeType);
    }

    /**
     * Merges endpoints that are a dead end to close segments if they are really close to elimninate that dead end
     */
    public void mergeEndPointsToSegments(){

    }


    public void snapCloseSegments(){

    }


    public SimpleWeightedGraph getPlanarGraph(){
        retrieveSubSegments();
        SimpleWeightedGraph graph = createEmptyGraph();
        fillGraph(graph);
        GraphManipulator manipulator = new GraphManipulator(graph);
        manipulator.adjustGraph();
        return graph;
    }

    private void fillGraph(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph){
        HashSet<Integer> addedNodes = new HashSet<>();
        for (SegmentBoNew seg: segments) {
            PointBoNew p1 = (PointBoNew) seg.getP1();
            PointBoNew p2 = (PointBoNew) seg.getP2();
            int id1 = p1.getId();
            int id2 = p2.getId();



            addedNodes.add(id1);
            addedNodes.add(id2);

            if (id1 == id2){
                continue;
            }

            graph.addVertex(id1);
            graph.addVertex(id2);


            DefaultWeightedEdge edge = graph.addEdge(id1, id2);
            int edgeWeight = RouteTypeToWeightConverter.routeTypeToInt(seg.getRouteType());
            if (edge != null){
                graph.setEdgeWeight(edge, edgeWeight);
            }
        }
    }


    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> createEmptyGraph(){
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        return graph;
    }



    public void initializeConnectionList(){
        retrieveSubSegments();
        createConnectionList();
        createConnectionListNew();
        createConnectionNodeList();
        //snapEndPointsToConnectionNodes();
        // Try to merge endnodes to endnos

        // Merge multiple close connectionNodes to avoid that they are too close together
       // mergeMulitpleConnectionNodes();
    }

    private void snapEndPointsToConnectionNodes(){
        // Für alle nodes die ins leere laufen
        for (RealPixelNode endNode: endNodes){

            // Schaue ob man sie auf ein connection node mergen kann
            for (RealPixelNode connectionNode: connectionNodes){

                if (endNode.distance(connectionNode) < 20){
                    // Bilde endnode auf selbe pixel wie connection node ab
                    endNode.setId(connectionNode.getId());

                    // Bild alle nodes die vor
                    snapConnectedNodesToConnectionPoint(endNode, connectionNode);
                    break;
                }
            }


        }
    }

    private void snapConnectedNodesToConnectionPoint(RealPixelNode endNode, RealPixelNode connectionNode){
        if (endNode == connectionNode) return;
        for (RealPixelNode connectedNode: endNode.getConnectedNodes()){
            if (connectedNode.distance(connectionNode) < 20){
                connectedNode.setId(connectionNode.getId());
                snapConnectedNodesToConnectionPoint(connectedNode, connectionNode);
            }
        }
    }


    private void mergeMulitpleConnectionNodes(){
        for (int i = 0; i<connectionNodes.size()-1; i++){
            RealPixelNode outerNode = connectionNodes.get(i);
            for (int j = i+1; j<connectionNodes.size(); j++){
                RealPixelNode innerNode = connectionNodes.get(j);
                if (outerNode.distance(innerNode) < 10){

                }
            }
        }
    }

    private void createConnectionNodeList(){
        for (int id: idToRealNode.keySet()){
            RealPixelNode current = idToRealNode.get(id);
            if (current.sizeConnectionList() > 2){
                connectionNodes.add(current);
            }
            if (current.sizeConnectionList() < 2){
                endNodes.add(current);
            }
        }
    }

    private void createConnectionListNew(){
        for (SegmentBoNew seg: segments){

            PointBoNew p1 = (PointBoNew) seg.getP1();
            PointBoNew p2 = (PointBoNew) seg.getP2();
            int id1 = p1.getId();
            int id2 = p2.getId();


            if (id1 == id2){
                System.out.println("" +
                        "");
            }
            RealPixelNode node1 = idToRealNode.get(id1);
            RealPixelNode node2 = idToRealNode.get(id2);

            if (node1 == null){
                node1 = new RealPixelNode(id1);
                idToRealNode.put(node1.getId(), node1);
            }

            if (node2 == null){
                node2 = new RealPixelNode(id2);
                idToRealNode.put(node2.getId(), node2);
            }

            node1.addConnectedNode(node2);
            node2.addConnectedNode(node1);
            pixelSegments.add(new PixelSegment(node1, node2, seg.getRouteType()));
        }
    }



    private void createConnectionList(){
        for(SegmentBoNew seg: segments){
            PointBoNew p1 = (PointBoNew) seg.getP1();
            PointBoNew p2 = (PointBoNew) seg.getP2();

            addToConnectionList(p1,seg);
            addToConnectionList(p2,seg);
        }

        for (int id: nodeToSegments.keySet()){
            idToRealNode.put(id, new RealPixelNode(id));
        }
    }

    private void addToConnectionList(PointBoNew point, SegmentBoNew segment){
        HashSet<SegmentBoNew> segments1 = nodeToSegments.get(point.getId());
        if (segments1 != null){
            segments1.add(segment);
        }
        else{
            segments1 = new HashSet<>();
            segments1.add(segment);
            nodeToSegments.put(point.getId(), segments1);
        }
    }

    /**
     * Retrives all segments for one segment that is possibly splitted because of intersections with other segments.
     */
    private void retrieveSubSegments(){
        ArrayList<SegmentBoNew> subSegments = new ArrayList<>();
        for (SegmentBoNew segment: segments){
            ArrayList<SegmentBoNew> subSegmentsCurrent = segment.getSubSegments();
            subSegments.addAll(subSegmentsCurrent);
        }
        this.segments = subSegments;
    }

    public void splitSegmentsAtIntersections(){
        createSegmentList();
        splitSegmentsIntersectionsNew();
    }

    public void splitSegmentsIntersectionsNew(){
        IntersectionFinderNew finder = new IntersectionFinderNew();
        ArrayList<ISegment> segmentsAbstract = new ArrayList<>(segments);
        finder.findIntersections(segmentsAbstract);
    }


    public void createSegmentList(){
        for (ReducedWay way: mergedWays){
            addToSegmentList(way);
        }
    }

    public void addToSegmentList(ReducedWay way){
        ArrayList<SegmentStroke> segmentStrokes = way.getTypeDiffSegments();
        for (SegmentStroke segmentStroke: segmentStrokes){
            if (segmentStroke.getIdStartNode() != segmentStroke.getIdEndNode()){
                segments.add(segmentStroke.toSegmentBoNew());
            }
        }
    }

    public ArrayList<NetworkWay> getReadyNetwork(){
        return this.snappedWays;
    }

    public void mergeWay(ReducedWay wayToMerge){
        manipulator.mergeWayOnNetworkBus(wayToMerge, mergedWays, 0.73, 70);
    }

    public void dpAndMergeIntern(){
        applyDouglasPeucker();
        incrementallyMergeWays();
        //splitSegmentsAtIntersection();
        //splitWaysAtIntersections();
    }



    public ArrayList<Stroke> buildStrokes(){
        StrokeBuilder builder = new StrokeBuilder(mergedWays);
        builder.buildStrokes();
        busStrokes = builder.getStrokes();
        Collections.sort(busStrokes);

        mergedWays.clear();
        for (Stroke stroke: busStrokes){
            mergedWays.add(stroke.toReducedWay());
        }

        ArrayList<ReducedWay> simpleWays = new ArrayList<>();
        int id = 0;
        for (ReducedWay way: mergedWays){
            ArrayList<Point> wayPoints = new ArrayList<>();
            wayPoints.addAll(way.getWayPoints());
            List<Point> points = SeriesReducer.reduce(wayPoints, 5);
            simpleWays.add(new ReducedWay(points, id++));
        }

        this.mergedWays = simpleWays;


        // Save the routetype in every way
        for (ReducedWay way: mergedWays){
            way.setRouteType(this.routeType);
            way.initializeRouteTypeOfNodes();
        }


        //FIXME: mergedways nochmal durch douglas peucker schicken, da nach merging weiter vereinfacht werden kann und ohne probleme beim mergen entstehen
        return busStrokes;
    }

    public ArrayList<SegmentBoNew> getSegments() {
        return segments;
    }

    public ArrayList<PixelNode> getConnectionNodes(){
        StrokeBuilder builder = new StrokeBuilder(mergedWays);
        builder.buildStrokes();
        return builder.getConnectionPoints();
    }

    public Network mergeBusInto(Network busNetwork){
        ArrayList<ReducedWay> allMergedTypeDiff = new ArrayList<>();
        ArrayList<ReducedWay> busWays = busNetwork.getMergedWays();
        Collections.sort(busWays);

        for (ReducedWay way: mergedWays){
            ArrayList<ReducedWay> typeDiffwaysCurrent = way.getTypeDiffWays();
            allMergedTypeDiff.addAll(typeDiffwaysCurrent);
        }

        ArrayList<ReducedWay> insertedSplits;
        double lengthIns = 0;

        int number = 0;

        for (ReducedWay wayToMerge: busWays){
            double length = wayToMerge.length();
            if (length < 40){
                break;
            }
            // FIXME: Change bus network size
            /*if (lengthIns > 30000){
                break;
            }*/
            wayToMerge.setId(number++);

            insertedSplits = new WayManipulator().mergeWayOnNetworkBus(wayToMerge, allMergedTypeDiff, 0.73, 30);


            for (ReducedWay split: insertedSplits){
                split.setRouteType(wayToMerge.getRouteType());
                lengthIns += split.length();
            }

            allMergedTypeDiff.addAll(insertedSplits);
        }

        return new Network(allMergedTypeDiff, RouteType.STB);
    }


    /**
     * This network has to be initialized with dpAndMergeIntern() before this method.
     * The parameter Network has to be simplified with applyDouglasPeucker before.
     * @param networkToMerge
     * @return merged Network of combined type
     */
    public Network mergeIntoThis(Network networkToMerge){
        ArrayList<ReducedWay> waysToMerge = networkToMerge.getMergedWays();
        ArrayList<ReducedWay> allMergedWays = new ArrayList<>(mergedWays);
        Collections.sort(waysToMerge);
        int minimalWayLength = networkToMerge.selector.minimalWayLength();
        double minFrechetSim = networkToMerge.selector.frechetSimValue();
        int maxBufferSize = networkToMerge.selector.maxBufferValue();

        ArrayList<ReducedWay> insertedSplits;
        double lengthIns = 0;

        for (ReducedWay wayToMerge: waysToMerge){
            double length = wayToMerge.length();
            lengthIns += length;
            if (length < minimalWayLength){
                break;
            }

            insertedSplits = manipulator.mergeWayOnNetworkBus(wayToMerge, allMergedWays, minFrechetSim, maxBufferSize);

            for (ReducedWay split: insertedSplits){
                split.setRouteType(wayToMerge.getRouteType());
            }

            allMergedWays.addAll(insertedSplits);
        }

        return new Network(allMergedWays, RouteTypeCreator.combineTypes(this.routeType, networkToMerge.routeType));
    }








    public void incrementallyMergeWays(){
        //FIXME: Define all values for the current routetype
        double minFrechetSim = selector.frechetSimValue();
        int maxBufferSize = selector.maxBufferValue();
        int maxNetworkSize = selector.maxNetworkSize();
        int minimalWayLength = selector.minimalWayLength();

        // Sort the simplified ways that the largest ways are inserted in the first place
        Collections.sort(mergedWays);
        this.mergedWays = manipulator.incrementallyMergeWays(mergedWays, minFrechetSim, maxBufferSize, maxNetworkSize, minimalWayLength);
        //this.mergedWays = manipulator.reducedWaysToNetworkWays(dpSimplifiedWays);

        // Save the routetype in every way
        for (ReducedWay way: mergedWays){
            way.setRouteType(this.routeType);
        }
    }

    public ArrayList<ReducedWay> getMergedWays(){
        return this.mergedWays;
    }

    /**
     * Applys douglas-peucker algorithm to the originalways and initializes the dpimplifiedWays with the result ways.
     */
    public ArrayList<ReducedWay> applyDouglasPeucker(){
        int epsilon = selector.douglasPeuckerDistance();
        this.mergedWays = WayManipulator.douglasPeuckerAlgo(originalWays, epsilon);

        // Save the routetype in every way
        for (ReducedWay way: mergedWays){
            way.setRouteType(this.routeType);
            way.initializeRouteTypeOfNodes();
        }

        return mergedWays;
    }

    public void splitWaysAtIntersections() {

        // The splits of all major ways
        ArrayList<NetworkWay> splittedNetwork = new ArrayList<>();

        for (NetworkWay unsplitted : snappedWays) {
            ArrayList<NetworkWay> splits = unsplitted.getSplits();
            splittedNetwork.addAll(splits);
        }

        this.snappedWays = splittedNetwork;
    }


    /**
     * Implicitly initializes snappedways which are just converted by the reduced ways.
     * Splits segments that are involved in intersections at their intersection points.
     */
    public void splitSegmentsAtIntersection(){
        this.snappedWays = manipulator.reducedWaysToNetworkWays(mergedWays);
        this.intersecFinder.findIntersectionsNetwork(snappedWays);
    }


    public ArrayList<NetworkWay> getSnappedWays() {
        return snappedWays;
    }

    public void setSnappedWays(ArrayList<NetworkWay> snappedWays) {
        this.snappedWays = snappedWays;
    }
}
