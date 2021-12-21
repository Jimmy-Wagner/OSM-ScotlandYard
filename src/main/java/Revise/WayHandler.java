package Revise;

import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class WayHandler {

    private OsmDataContainer dataContainer;
    private Bound boundingBox;
    private RelationMemberHelper helper;

    public WayHandler(OsmDataContainer dataContainer, RelationMemberHelper helper){
        this.dataContainer = dataContainer;
        this.helper = helper;
        this.boundingBox = dataContainer.getImageBoundingbox();
    }

    /**
     * Returns for all given relations its unconnected ways.
     * @param routeRelations
     * @return unconnected ways for every relation
     */
    public HashMap<Long, ArrayList<TrimmedWay>> getTrimmedWays(HashSet<Relation> routeRelations){
        HashMap<Long, ArrayList<TrimmedWay>> trimmedWays = new HashMap<Long, ArrayList<TrimmedWay>>();
        ArrayList<TrimmedWay> trimmedWaysOneRelation;

        for (Relation relation: routeRelations){
            // Save all unconnected ways for the current relation in the map if there are ways
            trimmedWaysOneRelation = getTrimmedWays(relation);
            if (!trimmedWaysOneRelation.isEmpty()){
                trimmedWays.put(relation.getId(), trimmedWaysOneRelation);
            }
        }
        return trimmedWays;
    }

    /**
     * Returns a List of all unconnected ways of this relation. Ways that are connected result in one trimmed way.
     * @param relation
     * @return unconnected ways of the relation
     */
    private ArrayList<TrimmedWay> getTrimmedWays(Relation relation){
        ArrayList<TrimmedWay> trimmedWays = new ArrayList<TrimmedWay>();
        Way fullWay;

        for (RelationMember member:relation.getMembers()){
            if (member.getMemberType() == EntityType.Way){
                // Way is contained in data
                if (helper.dataContains(member)){
                    fullWay = helper.getFullWay(member);
                    // Probably only one trimmedway is added but also possible that there are mutliple in one relation
                    trimmedWays.addAll(getTrimmedWays(fullWay));
                }
            }
        }

        //Merge connected trimmedWays into one trimmedway
        trimmedWays = mergeConnected(trimmedWays);
        return trimmedWays;
    }

    /**
     * Splits a way into several ways that are connected in the bounding box of the image.
     * One way can for example go out of the image and return back to the image on another point.
     * If thats the case the way is split into two trimmedways because they are not connected in the bounding box.
     * @param way
     * @return list of trimmed ways
     */
    private ArrayList<TrimmedWay> getTrimmedWays(Way way){
        ArrayList<TrimmedWay> trimmedWays = new ArrayList<TrimmedWay>();
        TrimmedWay currentTrimmedWay = new TrimmedWay();
        long wayNodeId;
        for (WayNode wayNode: way.getWayNodes()){
            wayNodeId = wayNode.getNodeId();
            // The waynode is contained in the data
            if (dataContainer.inContainedNodes(wayNodeId)){
                currentTrimmedWay.addNode(wayNodeId);
            }
            // The current node is not contained in the data so make a cut
            else{
                // If there are more than one node to the way (otherwise it would be no way)
                if (currentTrimmedWay.getWaySize() > 1){
                    trimmedWays.add(currentTrimmedWay);
                }
                // Make a cut for this way because it is not connected to the following ways of this relation because the
                // ways of the relation go out the bounding box of the map image.
                currentTrimmedWay = new TrimmedWay();
            }
        }
        if (currentTrimmedWay.getWaySize() > 1){
            trimmedWays.add(currentTrimmedWay);
        }
        return trimmedWays;
    }


    /**
     * Merges the trimmed ways that are connected. To be connected the last node of the first trimmed way has to be identical
     * to the first node of the second trimmed way.
     * @param trimmedWays
     * @return merged trimmed ways
     */
    private ArrayList<TrimmedWay> mergeConnected(ArrayList<TrimmedWay> trimmedWays){
        ArrayList<TrimmedWay> mergedTrimmedWays = new ArrayList<TrimmedWay>();
        TrimmedWay lastWay = null;
        for (TrimmedWay trimmedWay: trimmedWays){
            if (lastWay != null){
                // If the current way cannot be merged into the lastway change the lastWay to the current way, otherwise keep the lastway
                if (!lastWay.mergeInto(trimmedWay)){
                    mergedTrimmedWays.add(lastWay);
                    lastWay = trimmedWay;
                }
            }
            else{
                lastWay = trimmedWay;
            }
        }
        mergedTrimmedWays.add(lastWay);
        return mergedTrimmedWays;
    }


    /**
     * Returns the id of the way nodes for a way given by its id
     * @param wayId
     * @return list of ids of waynodes
     */
    private ArrayList<Long> getWayNodeIds(long wayId){
        Way fullWay = this.dataContainer.getFullWayById(wayId);
        return (ArrayList) fullWay.getWayNodes();
    }


}
