package HelperClasses;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class CurveVertexes {

    public static ArrayList<Coordinate> stopPoints = new ArrayList<>();
    public static HashMap<Coordinate, Integer> stopPointsWithType = new HashMap<>();
    public static SimpleWeightedGraph<Integer, DefaultWeightedEdge> connectionVertexGraph;
    public static  SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> directedGraph;

}
