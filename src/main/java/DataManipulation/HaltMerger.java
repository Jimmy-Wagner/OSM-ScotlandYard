package DataManipulation;

import HelperClasses.DistanceCalculator;
import DataContainer.RelationMemberHelper;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Merges platforms and stop nodes. Responsible for platform to stopnode mapping creation.
 */
public class HaltMerger {

    private RelationMemberHelper helper;
    private ThinOut thinOutHelper;


    public HaltMerger(RelationMemberHelper helper){
        this.helper = helper;
        this.thinOutHelper = new ThinOut(helper);
    }

    /**
     * Removes all platforms that have a corresponding stop node from the given list.
     * @param unmergedPlatformAndStopNodeIds
     * @param routeRelations
     * @return list of stop ids and of platform ids that have no corresponding stop
     */
    public HashSet<Long> getMergedHalts(HashSet<Long> unmergedPlatformAndStopNodeIds, HashSet<Relation> routeRelations){
        HashSet<Long> mergedPlatformAndStopNodeIds = new HashSet<Long>(unmergedPlatformAndStopNodeIds);
        HashMap<Long, Long> platformToStopNodeMapping = createPlatformToStopNodeMapping(routeRelations);

        for (long platformId: platformToStopNodeMapping.keySet()){
            mergedPlatformAndStopNodeIds.remove(platformId);
        }
        return mergedPlatformAndStopNodeIds;
    }

    /**
     * Merges platforms with corresponding stops and gets rid of halts that are connected to less than 3 other halts.
     * @param unmergedPlatformAndStopNodeIds
     * @param routeRelations
     * @return
     */
    public HashSet<Long> getThinnedOutHalts(HashSet<Long> unmergedPlatformAndStopNodeIds, HashSet<Relation> routeRelations){
        HashMap<Long, Long> platformToStopNodeMapping = createPlatformToStopNodeMapping(routeRelations);
        // Halts are implicitly merged
        HashSet<Long> thinnedOutHalts = thinOutHelper.thinOutHalts(routeRelations, unmergedPlatformAndStopNodeIds, platformToStopNodeMapping);

        return thinnedOutHalts;
    }


    /**
     * Returns only the halts which consist of a platform and a stopnode.
     * @param routeRelations
     * @return halts with platform and stop node
     */
    public HashSet<Long> getOnlyHaltsWithPlatformsAndStops(HashSet<Relation> routeRelations){
        HashSet<Long> doubleHalts = new HashSet<Long>();
        HashMap<Long, Long> platformToStopNodeMapping = createPlatformToStopNodeMapping(routeRelations);
        for (long platformId: platformToStopNodeMapping.keySet()){
            // Add the id of the corresponding stopnode of the current platform to double halts
            doubleHalts.add(platformToStopNodeMapping.get(platformId));
        }
        return doubleHalts;
    }


    /**
     * Maps every platform thats within the bounding box of the map image to its correspondend stopnode
     * when there is one.
     * If the platform has no correspondend stopnode in the boundingbox or the platform itself is not in the
     * boduning box the patform is not contained in the keyset of the HashMap.
     * TODO: Maybe calculate the distance for the nearest node of ways and relations to a node instead of the first node
     * @param routeRelations
     * @return platformToStopNodeMapping
     */
    private HashMap<Long, Long> createPlatformToStopNodeMapping(HashSet<Relation> routeRelations){

        HashMap<Long, Long> platformToStopNodeMapping = new HashMap<Long, Long>();

        for (Relation relation: routeRelations){
            // Reset the last member for the new relation
            RelationMember lastMember = null;


            for (RelationMember member: relation.getMembers()){

                // When the last member is null there is nothing to merge
                if (lastMember != null){
                    // in that case all stops have been read already
                    if (helper.hasNoRole(member)) break;
                    // Stops with a platform are mapped before the platform in the route relations
                    else if (helper.hasRolePlatform(member) && helper.hasRoleStop(lastMember)){
                        // Check if the stopnode and the platform is contained in the bounding box of the image
                        if (helper.dataContains(member) && helper.dataContains(lastMember)){
                            Node currentFullNode = helper.getFullNode(member);
                            Node lastFullNode = helper.getFullNode(lastMember);
                            // check for the distance of the two nodes
                            if (nearBy(currentFullNode, lastFullNode)){
                                platformToStopNodeMapping.put(member.getMemberId(), lastMember.getMemberId());
                            }
                        }
                    }
                }
                lastMember = member;
            }
        }
        return platformToStopNodeMapping;
    }

    /**
     * Checks if the two nodes are closer than 50 meters
     * @param node1
     * @param node2
     * @return distance is less than 50 meters
     */
    private boolean nearBy(Node node1, Node node2){
        double distance = DistanceCalculator.calculateDistanceTwoNodes(node1, node2);
        if (distance<=100){
            return true;
        }
        else{
            return false;
        }
    }
}
