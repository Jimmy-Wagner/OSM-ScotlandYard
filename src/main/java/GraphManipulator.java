
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.buffer.BufferOp;

import java.util.*;

public class GraphManipulator {
    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph;
    // This is the graph for importing in scotland yard
    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> connectionVertexGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    private HashSet<Integer> removeedVertexes = new HashSet<>();
    private HashSet<Integer> mergedStopPoints = new HashSet<>();
    private HashMap<Coordinate, Integer> stopPointWithRoutetype = new HashMap<>();
    private HashSet<Integer> doneVertexes = new HashSet<>();
    private SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> directedGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    public GraphManipulator(SimpleWeightedGraph graph) {
        this.graph = graph;
    }

    public void adjustGraph() {
        // Currently not implemented
        //snapCloseEndpoints();

        removeDeadEnds(graph);

        //mergeCloseConnectionPoints();

        Set<Graph<Integer, DefaultWeightedEdge>> connectedComponents = new BiconnectivityInspector<Integer, DefaultWeightedEdge>(graph).getConnectedComponents();

        Graph<Integer, DefaultWeightedEdge> biggestConnectedGraph = null;

        for (Graph currentGraph: connectedComponents){
            if (biggestConnectedGraph == null){
                biggestConnectedGraph = currentGraph;
            }
            else {
                if (biggestConnectedGraph.vertexSet().size() < currentGraph.vertexSet().size()){
                    biggestConnectedGraph = currentGraph;
                }
            }
        }

        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addGraph(graph, biggestConnectedGraph);

        mergeCloseStopPoints();
        //addStopPointRoutetype();

        removeDeadEnds(graph);


        createStopPointConnections();
        CurveVertexes.connectionVertexGraph = connectionVertexGraph;

        CurveVertexes.stopPointVertexes = new ArrayList<>(connectionVertexGraph.vertexSet());

        createDirectedGraph();
        CurveVertexes.directedGraph = directedGraph;
    }

    private void createDirectedGraph() {
        for (int stopVertex : mergedStopPoints) {
            // Für jeden stopvertex alle pfade zu nächsten stopvertexes laufen die noch nicht gelaufen wurden
            directedGraph.addVertex(stopVertex);
            findPathToNextStopVertex(stopVertex);
        }
    }


    private void findPathToNextStopVertex(Integer stopVertex) {
        List<Integer> neighbors = Graphs.neighborListOf(graph, stopVertex);
        neighbors.removeAll(doneVertexes);
        // Für jeden nachbarn pfad entlang gehn bis stopVertex gefunden
        for (int neighbor : neighbors) {
            buildPathTillStopVertex(neighbor, stopVertex);
        }
    }

    private void buildPathTillStopVertex(Integer vertex, Integer lastVertex) {
        doneVertexes.add(lastVertex);
        directedGraph.addVertex(vertex);
        DefaultWeightedEdge edgeOld = graph.getEdge(lastVertex, vertex);
        int weight = (int) graph.getEdgeWeight(edgeOld);

        DefaultWeightedEdge edgeNew = directedGraph.addEdge(lastVertex, vertex);
        directedGraph.setEdgeWeight(edgeNew, weight);

        if (mergedStopPoints.contains(vertex)) {
            return;
        } else {
            Integer nextVertex = getNextVertex(vertex, lastVertex);
            buildPathTillStopVertex(nextVertex, vertex);
        }


    }

    /**
     * Only applicable if there are only two neighbors and the last neighbor is given.
     *
     * @param vertex
     * @param lastVertex
     * @return
     */
    private int getNextVertex(Integer vertex, Integer lastVertex) {
        List<Integer> neighbors = Graphs.neighborListOf(graph, vertex);
        neighbors.remove(lastVertex);
        return neighbors.get(0);
    }


    private HashMap<Integer, Integer> findConnectedConnectionPoints(int stopVertex) {
        List<Integer> neighbors = Graphs.neighborListOf(graph, stopVertex);
        // Für jeden nachbarn pfad entlang gehn bis stopVertex gefunden
        HashMap<Integer, Integer> connectedConnectionPoints = new HashMap<>();
        for (int neighbor : neighbors) {
            DefaultWeightedEdge edge = graph.getEdge(stopVertex, neighbor);
            int weight = (int) graph.getEdgeWeight(edge);

            int nextStopVertex = findNextStopVertex(neighbor, stopVertex);
            connectedConnectionPoints.put(nextStopVertex, weight);
        }
        return connectedConnectionPoints;

    }

    private int findNextStopVertex(int vertex, Integer lastVertex) {
        if (mergedStopPoints.contains(vertex)) {
            return vertex;
        }

        List<Integer> neighbors = Graphs.neighborListOf(graph, vertex);
        neighbors.remove(lastVertex);
        int nextVertex = neighbors.get(0);

        return findNextStopVertex(nextVertex, vertex);

    }


    private void createStopPointConnections() {
        for (int stopVertex : mergedStopPoints) {
            HashMap<Integer, Integer> connectedStopVertexes = findConnectedConnectionPoints(stopVertex);
            connectionVertexGraph.addVertex(stopVertex);
            for (int connected : connectedStopVertexes.keySet()) {
                connectionVertexGraph.addVertex(connected);
                DefaultWeightedEdge edge = connectionVertexGraph.addEdge(stopVertex, connected);
                if (edge != null) {
                    graph.setEdgeWeight(edge, connectedStopVertexes.get(connected));
                }
            }
        }

    }


    private void addStopPointRoutetype() {
        for (int vertex : mergedStopPoints) {
            Set<DefaultWeightedEdge> edges = graph.edgesOf(vertex);
            for (DefaultWeightedEdge edge : edges) {

            }
        }
    }


    /**
     * Identifys all vertexes that are eligible for a stop position.
     * 1. All connection nodes
     * 2. All nodes that are a sharp curve
     * 3. All nodes on which the routetype is changed
     *
     * @return
     */
    private ArrayList<Integer> identifyStopPoints() {
        ArrayList<Integer> connectionVertexes = identifyConnectionsVertexes();
        ArrayList<Integer> curveVertexes = identifyCurveVertexes();
        ArrayList<Integer> routeChangeVertexes = identifyRouteChangeVertexes();
        Set<Integer> allStopPoints = new HashSet<>();
        allStopPoints.addAll(connectionVertexes);
        allStopPoints.addAll(curveVertexes);
        allStopPoints.addAll(routeChangeVertexes);
        ArrayList<Integer> allStopPointsList = new ArrayList<>(allStopPoints);
        return allStopPointsList;
    }


    private void mergeCloseStopPoints() {
        ArrayList<Integer> stopPoints = identifyStopPoints();
        mergeStopPoints(stopPoints);

    }

    private void mergeStopPoints(ArrayList<Integer> points) {
        ArrayList<Coordinate> coordsVertexes = getCoords(points);
        HashSet<Coordinate> alreadyMerged = new HashSet<>();
        for (int i = 0; i < coordsVertexes.size(); i++) {
            Coordinate c1 = coordsVertexes.get(i);
            int vertex1 = points.get(i);
            if (alreadyMerged.contains(c1)) {
                continue;
            }
            alreadyMerged.add(c1);
            ArrayList<Coordinate> currentMergeGroupCoords = new ArrayList<>();
            ArrayList<Integer> currentMergeGroupVertexes = new ArrayList<>();
            currentMergeGroupCoords.add(c1);
            currentMergeGroupVertexes.add(vertex1);
            for (int j = i + 1; j < points.size(); j++) {
                Coordinate c2 = coordsVertexes.get(j);
                int vertex2 = points.get(j);
                if (c1.distance(c2) < 30) {
                    currentMergeGroupCoords.add(c2);
                    currentMergeGroupVertexes.add(vertex2);
                    alreadyMerged.add(c2);
                }
            }
            mergeCurrentGroupNew(currentMergeGroupCoords, currentMergeGroupVertexes);
        }
    }

    /**
     * Merges connectionpoints that are too close together that there signs in the map will not overlap
     */
    private void mergeCloseConnectionPoints() {
        ArrayList<Integer> connectionVertexes = identifyConnectionsVertexes();
        ArrayList<Coordinate> coordsVertexes = getCoords(connectionVertexes);
        HashSet<Coordinate> alreadyMerged = new HashSet<>();
        for (int i = 0; i < coordsVertexes.size(); i++) {
            Coordinate c1 = coordsVertexes.get(i);
            int vertex1 = connectionVertexes.get(i);
            if (alreadyMerged.contains(c1)) {
                continue;
            }
            alreadyMerged.add(c1);
            ArrayList<Coordinate> currentMergeGroupCoords = new ArrayList<>();
            ArrayList<Integer> currentMergeGroupVertexes = new ArrayList<>();
            currentMergeGroupCoords.add(c1);
            currentMergeGroupVertexes.add(vertex1);
            for (int j = i + 1; j < connectionVertexes.size(); j++) {
                Coordinate c2 = coordsVertexes.get(j);
                int vertex2 = connectionVertexes.get(j);
                if (c1.distance(c2) < 30) {
                    currentMergeGroupCoords.add(c2);
                    currentMergeGroupVertexes.add(vertex2);
                    alreadyMerged.add(c2);
                }
            }
            mergeCurrentGroupNew(currentMergeGroupCoords, currentMergeGroupVertexes);
        }
    }

    private void mergeCurrentGroupNew(ArrayList<Coordinate> mergeGroupCoords, ArrayList<Integer> mergeGroupVertexes) {

        // There is nothing to merge, that means there is no other connection point close to the given point
        if (mergeGroupCoords.size() == 1) {
            mergedStopPoints.add(mergeGroupVertexes.get(0));
            return;
        }

        MultiPoint mergeGroupMultiPoint = mergeGroupMultipoint(mergeGroupCoords);

        ConvexHull boundary = new ConvexHull(mergeGroupMultiPoint);
        Geometry boundaryMergeGroup = boundary.getConvexHull();

        if (boundaryMergeGroup.getDimension() == 1) {
            boundaryMergeGroup = boundaryMergeGroup.buffer(10, 8, BufferOp.CAP_ROUND);
        } else {
            boundaryMergeGroup = boundaryMergeGroup.buffer(5, 8, BufferOp.CAP_ROUND);
        }

        HashSet<Integer> deletionGroup = getDeleteionSet(boundaryMergeGroup, mergeGroupVertexes);
        ArrayList<Integer> deletionGroupList = new ArrayList<>(deletionGroup);
        MultiPoint deletionGroupPoints = createMulitPoint(deletionGroup);
        Coordinate centroid = Centroid.getCentroid(deletionGroupPoints);
        int centroidID = encodeNumbers((int) centroid.getX(), (int) centroid.getY());

        HashMap<Integer, Integer> neighboursOfDeletionGroup = getConnectedNodes(deletionGroupList);

        graph.removeAllVertices(deletionGroup);
        addCentroidWIthNeighbours(centroidID, neighboursOfDeletionGroup);
        mergedStopPoints.add(centroidID);


    }

    private void addCentroidWIthNeighbours(int centroidVertex, HashMap<Integer, Integer> neighboursOfDeletionGroup) {
        graph.addVertex(centroidVertex);

        for (int neighbour : neighboursOfDeletionGroup.keySet()) {
            if (graph.containsVertex(neighbour)) {
                if (centroidVertex == neighbour) {
                    System.out.println("Fehler");
                }
                DefaultWeightedEdge edge = graph.addEdge(centroidVertex, neighbour);
                graph.setEdgeWeight(edge, neighboursOfDeletionGroup.get(neighbour));
            }
        }
    }


    private void mergeCurrentGroup(ArrayList<Coordinate> mergeGroupCoords, ArrayList<Integer> mergeGroupVertexes) {
        if (mergeGroupCoords.size() == 1) {
            return;
        }

        MultiPoint mergeGroupMulitPoint = mergeGroupMultipoint(mergeGroupCoords);
        Coordinate mergeGroupCentroid = Centroid.getCentroid(mergeGroupMulitPoint);
        int centroidVertex = encodeNumbers((int) mergeGroupCentroid.getX(), (int) mergeGroupCentroid.getY());
        HashMap<Integer, Integer> neighboursOfMergeGroup = getConnectedNodes(mergeGroupVertexes);

        ConvexHull boundary = new ConvexHull(mergeGroupMulitPoint);
        Geometry boundaryMergeGroup = boundary.getConvexHull();

        if (boundaryMergeGroup.getDimension() == 1) {
            boundaryMergeGroup = boundaryMergeGroup.buffer(10, 8, BufferOp.CAP_BUTT);
        }

        deleteVertexesBetween(boundaryMergeGroup, mergeGroupVertexes);
        graph.addVertex(centroidVertex);

        for (int neighbour : neighboursOfMergeGroup.keySet()) {
            if (graph.containsVertex(neighbour)) {
                if (centroidVertex == neighbour) {
                    System.out.println("Fehler");
                }
                DefaultWeightedEdge edge = graph.addEdge(centroidVertex, neighbour);
                graph.setEdgeWeight(edge, neighboursOfMergeGroup.get(neighbour));
            }
        }


    }

    private HashMap<Integer, Integer> getConnectedNodes(ArrayList<Integer> vertexes) {
        HashMap<Integer, Integer> allNeighbours = new HashMap<>();
        for (int vertex : vertexes) {
            Set<Integer> currentNeighbors = Graphs.neighborSetOf(graph, vertex);
            currentNeighbors.removeAll(vertexes);
            for (Integer neighbor : currentNeighbors) {
                if (!allNeighbours.keySet().contains(neighbor)) {
                    int connectionWeight = (int) graph.getEdgeWeight(graph.getEdge(vertex, neighbor));
                    allNeighbours.put(neighbor, connectionWeight);
                }
            }
        }
        return allNeighbours;
    }

    private HashSet<Integer> getDeleteionSet(Geometry geom, ArrayList<Integer> mergeVertexes) {
        HashSet<Integer> deletionGroup = new HashSet<>();
        HashSet<Integer> allVertex = new HashSet<Integer>(graph.vertexSet());
        for (int vertex : allVertex) {
            if (mergeVertexes.contains(vertex)) {
                deletionGroup.add(vertex);
            } else {
                Point p = new GeometryFactory().createPoint(getCoord(vertex));
                if (geom.intersects(p)) {
                    deletionGroup.add(vertex);
                }
            }
        }
        return deletionGroup;
    }

    private void deleteVertexesBetween(Geometry geom, ArrayList<Integer> mergeGroupVertexes) {

        // Remove all vertexes that lie between merge group (inclusive merge group)
        HashSet<Integer> allVertex = new HashSet<Integer>(graph.vertexSet());
        for (int vertex : allVertex) {
            if (mergeGroupVertexes.contains(vertex)) {
                graph.removeVertex(vertex);
            } else {
                Point p = new GeometryFactory().createPoint(getCoord(vertex));
                if (geom.intersects(p)) {
                    Graphs.removeVertexAndPreserveConnectivity(graph, vertex);
                }
            }
        }
    }

    private ArrayList<Coordinate> getAllCoordinates() {
        ArrayList<Coordinate> allCoords = new ArrayList<>();
        for (Object i : graph.vertexSet()) {
            int iInt = (int) i;
            int[] xy = decodeNumber(iInt);
            allCoords.add(new Coordinate(xy[0], xy[1]));
        }
        return allCoords;
    }

    private ArrayList<Coordinate> createCoordinates(ArrayList<Integer> vertexes) {
        ArrayList<Coordinate> vertexCoordinates = new ArrayList<>();
        for (int vertex : vertexes) {
            vertexCoordinates.add(getCoord(vertex));
        }
        return vertexCoordinates;
    }


    private MultiPoint createMulitPoint(HashSet<Integer> vertexes) {
        ArrayList<Integer> vertexesList = new ArrayList<>(vertexes);
        ArrayList<Coordinate> vertexCoords = createCoordinates(vertexesList);
        MultiPoint points = mergeGroupMultipoint(vertexCoords);
        return points;
    }


    /**
     * Creates a mulitppoint given by an array of coordinates
     *
     * @param coords
     * @return
     */
    private MultiPoint mergeGroupMultipoint(ArrayList<Coordinate> coords) {
        ArrayList<Point> points = new ArrayList<>();
        for (Coordinate c : coords) {
            points.add(new GeometryFactory().createPoint(c));
        }
        Object[] pointArrayObjects = points.toArray();
        Point[] pointArray = new Point[pointArrayObjects.length];
        for (int i = 0; i < pointArrayObjects.length; i++) {
            pointArray[i] = (Point) pointArrayObjects[i];
        }
        MultiPoint multiPoint = new GeometryFactory().createMultiPoint(pointArray);

        return multiPoint;
    }

    private ArrayList<Coordinate> getCoords(ArrayList<Integer> vertexes) {
        ArrayList<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < vertexes.size(); i++) {
            coords.add(getCoord(vertexes.get(i)));
        }
        return coords;
    }

    private Coordinate getCoord(int i) {
        int[] xy = decodeNumber(i);
        return new Coordinate(xy[0], xy[1]);
    }

    private ArrayList<Integer> identifyConnectionsVertexes() {
        ArrayList<Integer> connectionVertexes = new ArrayList<>();
        Set<Integer> vertexes = graph.vertexSet();

        for (int vertexId : vertexes) {
            List<Integer> connectedVertexes = Graphs.neighborListOf(graph, vertexId);
            if (connectedVertexes.size() > 2) {
                connectionVertexes.add(vertexId);
            }
        }
        return connectionVertexes;
    }

    private ArrayList<Integer> identifyRouteChangeVertexes() {
        ArrayList<Integer> routeChangeVertexes = new ArrayList<>();
        Set<Integer> vertexes = graph.vertexSet();

        for (int vertex : vertexes) {
            List<Integer> neighbors = Graphs.neighborListOf(graph, vertex);
            Set<Integer> connectedRouteTypes = new HashSet<>();
            for (int neighborVertex : neighbors) {
                DefaultWeightedEdge edge = graph.getEdge(vertex, neighborVertex);
                int weight = (int) graph.getEdgeWeight(edge);
                connectedRouteTypes.add(weight);
            }

            if (connectedRouteTypes.size() >= 2) {
                routeChangeVertexes.add(vertex);
            }
        }

        return routeChangeVertexes;
    }


    private ArrayList<Integer> identifyCurveVertexes() {
        ArrayList<Integer> curveVertexes = new ArrayList<>();
        Set<Integer> vertexes = graph.vertexSet();

        for (int vertex : vertexes) {
            List<Integer> neighbors = Graphs.neighborListOf(graph, vertex);
            // All vertexes with more than two neighbours are already added as connection vertexes
            if (neighbors.size() == 2) {
                if (angle(neighbors.get(0), neighbors.get(1), vertex) < 110) {
                    curveVertexes.add(vertex);
                }
            }
        }

        return curveVertexes;
    }

    /**
     * Removes recursivly all dead ends. New dead ends can emerge when deleting some, which will be also deleted.
     */
    private void removeDeadEnds(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        removeedVertexes.clear();
        HashSet<Integer> endVertexes = identifyEndPoints(graph);
        for (int endVertex : endVertexes) {
            if (!removeedVertexes.contains(endVertex)) {
                removeDeadEnds(endVertex);
            }
        }
    }

    private void removeDeadEnds(Integer endvertex) {
        List<Integer> connectedVertexes = Graphs.neighborListOf(graph, endvertex);
        if (connectedVertexes.size() == 1) {
            graph.removeVertex(endvertex);
            mergedStopPoints.remove(endvertex);
            removeedVertexes.add(endvertex);
            removeDeadEnds(connectedVertexes.get(0));
        }
        if (connectedVertexes.size() == 0) {
            graph.removeVertex(endvertex);
            mergedStopPoints.remove(endvertex);
            removeedVertexes.add(endvertex);
        }
    }


    private void snapCloseEndpoints() {
        HashSet<Integer> endVertexes = identifyEndPoints(graph);
        ArrayList<SegmentStroke> segments = buildSegments();

    }

    /**
     * Should build sgements to check if endpoints can be merged to close segments
     * FIXME: currently completely broken and unused
     *
     * @return
     */
    private ArrayList<SegmentStroke> buildSegments() {
        Set<DefaultWeightedEdge> edges = graph.edgeSet();
        ArrayList<SegmentStroke> segments = new ArrayList<>();

        return null;
    }

    private HashSet<Integer> identifyEndPoints(SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph) {
        // Identify endpoints, points which are connected to only one other vertex
        Set<Integer> vertexes = graph.vertexSet();
        HashSet<Integer> endVertexes = new HashSet<>();

        for (int vertexId : vertexes) {
            Set<Integer> connectedVertexes = Graphs.neighborSetOf(graph, vertexId);
            if (connectedVertexes.size() == 1) {
                endVertexes.add(vertexId);
            }
        }
        return endVertexes;
    }

    public int[] decodeNumber(int num) {
        int w = (int) (Math.sqrt(8 * num + 1) - 1) / 2;
        int t = (int) (Math.pow(w, 2) + w) / 2;
        int y = (int) (num - t);
        int x = (int) (w - y);
        return new int[]{x, y};
    }

    private int encodeNumbers(int x, int y) {
        return (((x + y) * (x + y + 1)) / 2) + y;
    }

    public double angle(int tip1, int tip2, int tail) {
        Coordinate tip1C = getCoord(tip1);
        Coordinate tip2C = getCoord(tip2);
        Coordinate tailC = getCoord(tail);
        return Angle.toDegrees(Angle.angleBetween(tip1C, tailC, tip2C));
    }
}