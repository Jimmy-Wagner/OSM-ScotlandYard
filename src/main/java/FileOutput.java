import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FileOutput {

    private SimpleWeightedGraph<Integer, DefaultWeightedEdge> graph = CurveVertexes.connectionVertexGraph;
    private HashMap<Integer, Integer> nodeToIdMapping = CurveVertexes.vertexToIdMapping;
    private Set<DefaultWeightedEdge> doneEdges = new HashSet<>();

    public FileOutput(){

    }

    public void createFiles(){
        createScotLinkingMap();
        createScotPosMap();
    }

    private void createScotPosMap(){
        String fileContent = Integer.toString(nodeToIdMapping.keySet().size()) + System.getProperty("line.separator");
        for (int vertex: graph.vertexSet()){
            int vertexId = nodeToIdMapping.get(vertex);
            int xy[] = decodeNumber(vertex);
            int x = xy[0];
            int y = xy[1];
            fileContent += vertexId + " " + x + " " + y + System.getProperty("line.separator");
        }

        File scotPosFile = new File("./ScotlandYardv2.4/bin/SCOTPOS.TXT");
        try {
            FileWriter fileWriter = new FileWriter(scotPosFile);
            fileWriter.write(fileContent);

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int[] decodeNumber(int num) {
        int w = (int) (Math.sqrt(8 * num + 1) - 1) / 2;
        int t = (int) (Math.pow(w, 2) + w) / 2;
        int y = (int) (num - t);
        int x = (int) (w - y);
        return new int[]{x, y};
    }

    /**
     * Creates the map with links.
     * Number of nodes, Number of Links
     * NodeId, NodeId,
     */
    public void createScotLinkingMap(){
        createFile();
    }

    public void createFile(){
        String fileContent = "";

        Set<Integer> vertexes = graph.vertexSet();
        int nodeNumber = vertexes.size();

        Set<DefaultWeightedEdge> edges = graph.edgeSet();
        int linkNumber = 0;

        for (DefaultWeightedEdge edge: edges){
            int source = nodeToIdMapping.get(graph.getEdgeSource(edge));
            int target = nodeToIdMapping.get(graph.getEdgeTarget(edge));
            ArrayList<Integer> types = MyTypeConverter.getTypes((int) graph.getEdgeWeight(edge));

            for (int type: types){
                String typeString = MyTypeConverter.convert(type);
                linkNumber++;
                fileContent += source + " " + target + " " + typeString + System.getProperty("line.separator");
            }
        }

        String file = "";
        file += nodeNumber + " " + linkNumber + System.getProperty("line.separator");
        file += fileContent;


        File scotmapFile = new File("./ScotlandYardv2.4/bin/SCOTMAP.TXT");
        try {
            FileWriter fileWriter = new FileWriter(scotmapFile);
            fileWriter.write(file);

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
