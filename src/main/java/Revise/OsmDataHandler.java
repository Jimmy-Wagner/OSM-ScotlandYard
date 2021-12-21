package Revise;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class takes the data from the {@link OsmDataContainer} object and prepares this
 * data for drawing to the map.
 */
public class OsmDataHandler {

    private OsmDataContainer dataContainer;
    private RelationMemberHelper helper;
    private HaltMerger haltMerger;
    private WayHandler wayHandler;
    private HashMap<Long, Node> allNodes;


    public OsmDataHandler(OsmDataContainer dataContainer) {
        this.dataContainer = dataContainer;
        this.allNodes = dataContainer.getAllContainedNodes();
        this.helper = new RelationMemberHelper(dataContainer);
        this.haltMerger = new HaltMerger(this.helper);
        this.wayHandler = new WayHandler(this.dataContainer, this.helper);
    }

    /**
     * Returns a list of fullnodes for a given list of ids of nodes.
     *
     * @param ids
     * @return list of full nodes
     */
    public ArrayList<Node> getFullNodesForIds(ArrayList<Long> ids) {
        ArrayList<Node> fullNodes = new ArrayList<Node>();
        for (long id : ids) {
            fullNodes.add(allNodes.get(id));
        }
        return fullNodes;
    }

    /**
     * Returns a list of fullnodes for a given list of ids of nodes.
     *
     * @param ids
     * @return list of full nodes
     */
    public HashSet<Node> getFullNodesForIds(HashSet<Long> ids) {
        HashSet<Node> fullNodes = new HashSet<Node>();
        for (long id : ids) {
            fullNodes.add(allNodes.get(id));
        }
        return fullNodes;
    }


    /**
     * Returns for every relation of the given type its unconnected ways.
     * Be careful! TrimmedWay can be null for a relation that has no ways in the bounding box! It can
     * happen that a relation has only one stop in the bounding box and therefore no way.
     *
     * @param routeType
     * @return list of unconnected ways for every relation. (Can be null for some relations!)
     */
    public HashMap<Long, ArrayList<TrimmedWay>> getTrimmedWays(RouteType routeType) {
        return this.wayHandler.getTrimmedWays(getRelationsByType(routeType));
    }

    /**
     * Returns all stopnodes and platforms of the given relation type. If possible the stopnodes and platforms are merged into the stopnode.
     *
     * @param routeType
     * @return
     */
    public HashSet<Node> getMergedHalts(RouteType routeType) {
        return getMergedHalts(getRelationsByType(routeType));
    }


    /**
     * Get all stop nodes and all platform nodes that have no corresponding stop node.
     * The platforms that have a corresponding stop node are merged into that stopnode.
     *
     * @param routeRelations
     * @return
     */
    public HashSet<Node> getMergedHalts(HashSet<Relation> routeRelations) {
        HashSet<Long> unmergedIds = extractAllContainedStopAndPlatformIDs(routeRelations);
        HashSet<Long> mergedIds = haltMerger.getMergedHalts(unmergedIds, routeRelations);
        HashSet<Node> mergedNodes = getFullNodesForIds(mergedIds);

        return mergedNodes;
    }


    /**
     * Returns all ids of platforms (nodes, relations) and stops (node) which are contained in the bounding box of the map image.
     *
     * @param routeRelations
     * @return contained platform and stop ids
     */
    private HashSet<Long> extractAllContainedStopAndPlatformIDs(HashSet<Relation> routeRelations) {
        HashSet<Long> containedStopAndPlatformIDs = new HashSet<Long>();
        for (Relation relation : routeRelations) {
            for (RelationMember member : relation.getMembers()) {
                if (helper.hasNoRole(member)) break;
                else if (helper.hasRoleStop(member) || helper.hasRolePlatform(member)) {
                    if (helper.dataContains(member)) {
                        containedStopAndPlatformIDs.add(member.getMemberId());
                    }
                }
            }
        }
        return containedStopAndPlatformIDs;
    }

    /**
     * Returns all halts which consist of a platform and a stop node.
     *
     * @param routeType
     * @return halts with platform and stop node
     */
    public HashSet<Node> getOnlyHaltsWithStopAndPlatform(RouteType routeType) {
        // double halts only contains the stop nodes
        HashSet<Long> doubleHalts = haltMerger.getOnlyHaltsWithPlatformsAndStops(getRelationsByType(routeType));
        HashSet<Node> doubleHaltNodes = new HashSet<Node>();
        Node fullNode;
        for (long id : doubleHalts) {
            fullNode = allNodes.get(id);
            doubleHaltNodes.add(fullNode);
        }
        return doubleHaltNodes;
    }

    /**
     * Return all platform nodes and stop nodes without merging platforms into corresponding stop nodes.
     * @param routeType
     * @return
     */
    public HashSet<Node> getAllStopAndPlatformNodes(RouteType routeType) {
        return getAllStopAndPlatformNodes(getRelationsByType(routeType));
    }

    /**
     * TODO: curently unused!
     * Returns all stop nodes and platform nodes unmerged for the given relations.
     *
     * @param routeRelations
     * @return stop and platform nodes unmerged
     */
    private HashSet<Node> getAllStopAndPlatformNodes(HashSet<Relation> routeRelations) {
        HashSet<Long> containedStopAndPlatformIds = extractAllContainedStopAndPlatformIDs(routeRelations);
        HashSet<Node> stopAndPlatformNodes = new HashSet<Node>();
        Node fullNode;
        for (long stopOrPlatformId : containedStopAndPlatformIds) {
            // To check if its a platform relation or a node (stop or platform node)
            if (this.dataContainer.isInAllContainedPlatformRelations(stopOrPlatformId)) {
                fullNode = this.dataContainer.getFullNodeOfRelationId(stopOrPlatformId);
                stopAndPlatformNodes.add(fullNode);
            } else {
                stopAndPlatformNodes.add(allNodes.get(stopOrPlatformId));
            }
        }
        return stopAndPlatformNodes;
    }

    /**
     * Returns all relations for a selected type of public transport relations.
     *
     * @param routeType
     * @return all relations of selected type
     */
    public HashSet<Relation> getRelationsByType(RouteType routeType) {
        switch (routeType) {
            case BUS:
                return this.dataContainer.getBusRouteRelations();
            case TRAIN:
                return this.dataContainer.getTrainRouteRelations();
            case SUBWAY:
                return this.dataContainer.getSubwayRouteRelations();
            case TRAM:
                return this.dataContainer.getTramRouteRelations();
            case MONORAIL:
                return this.dataContainer.getMonorailRouteRelations();
            case LIGHTRAIL:
                return this.dataContainer.getLightrailRouteRelations();
            default:
                return null;
        }
    }
}
