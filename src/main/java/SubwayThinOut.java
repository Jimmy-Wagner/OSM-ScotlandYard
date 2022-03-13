import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SubwayThinOut {

    private HashSet<TrimmedWay> ways;
    private OsmDataContainer dataContainer;
    private OsmDataHandler dataHandler;

    // search for crossings of ways
    public SubwayThinOut(OsmDataHandler dataHandler){
        this.dataHandler = dataHandler;
    }

    public ArrayList<Point> getWayCrossings(HashMap<Long, ArrayList<TrimmedWay>> trimmedWays){
        ArrayList<Node> wayNodes;
        ArrayList<Segment> data = new ArrayList<>();
        //Go through all route relations
        for (long relationID: trimmedWays.keySet()){
            // Go through all trimmed ways of this relation
            for (TrimmedWay trimmedWay: trimmedWays.get(relationID)){
                // trimmedWay can be null when this relation has no ways inside the bounding box (maybe only one stop or something like that)
                if (trimmedWay != null){
                    wayNodes = this.dataHandler.getFullNodesForIds(trimmedWay.getWaynodes());
                    Node lastNode = null;
                    for (Node node: wayNodes){
                        if (lastNode != null){
                            Point p = new Point(lastNode.getLongitude(), lastNode.getLatitude());
                            Point lastP = new Point(node.getLongitude(), node.getLatitude());
                            data.add(new Segment(p, lastP));
                        }
                        lastNode=node;
                    }
                }
            }
        }
        BentleyOttmann bent = new BentleyOttmann(data);
        bent.find_intersections();
        ArrayList<Point> intersections = bent.get_intersections();
        return intersections;
    }

}
