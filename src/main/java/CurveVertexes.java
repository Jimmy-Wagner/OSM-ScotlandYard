import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class CurveVertexes {

    public static ArrayList<Coordinate> stopPointsCoordinates = new ArrayList<>();
    // These are the stop points that will be drawn to the map
    public static ArrayList<Integer> stopPointVertexes = new ArrayList<>();
    public static HashMap<Coordinate, Integer> stopPointsWithType = new HashMap<>();
    // This is the graph to go.
    public static SimpleWeightedGraph<Integer, DefaultWeightedEdge> connectionVertexGraph;
    public static  SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> directedGraph;
    public static HashMap<Integer, Integer> vertexToIdMapping = new HashMap<>();

}
