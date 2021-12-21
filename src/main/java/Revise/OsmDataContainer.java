package Revise;

import org.openstreetmap.osmosis.core.domain.v0_6.*;


import java.util.HashMap;
import java.util.HashSet;

/**
 * This class should only be filled through the {@link PbfFileReader} class.
 * Contains all osm data that is read with {@link PbfFileReader}.
 */
public class OsmDataContainer {
    // Provides helper functions for relation members
    private RelationMemberHelper helper;
    // Bounding box of the retrieved map image
    private Bound imageBoundingbox;
    // The bounding box that is covered by the given osm data
    private Bound osmDataBoundingBox;
    // All nodes that are contained in the bounding box of the static map image
    // Use ID as key for quicker lookup if a node is contained
    private HashMap<Long, Node> allContainedNodes;
    // all ways that are within the bounds of the map image
    private HashMap<Long, Way> allContainedWays;
    // route=bus,trolleybus relations
    private HashSet<Relation> busRouteRelations;
    // route=train relations
    private HashSet<Relation> trainRouteRelations;
    // route=subway relations
    private HashSet<Relation> subwayRouteRelations;
    // route=tram relations
    private HashSet<Relation> tramRouteRelations;
    // route=light_rail relations
    private HashSet<Relation> lightrailRouteRelations;
    // route=monorail relations
    private HashSet<Relation> monorailRouteRelations;
    // platform=* relations
    private HashMap<Long, Relation> platformRelations;

    // Mapping for every platform relation to its first node for distance calculations
    // Implicitly initialized in preserveOnlyContainedlLatformRelations() method
    private HashMap<Long, Long> platformToPlatformNodeMapping = new HashMap<Long, Long>();


    protected OsmDataContainer(Bound imageBoundingbox,
                               Bound osmDataBoundingBox,
                               HashMap<Long, Node> allContainedNodes,
                               HashMap<Long, Way> allContainedWays,
                               HashSet<Relation> busRouteRelations,
                               HashSet<Relation> trainRouteRelations,
                               HashSet<Relation> subwayRouteRelations,
                               HashSet<Relation> tramRouteRelations,
                               HashSet<Relation> lightrailRouteRelations,
                               HashSet<Relation> monorailRouteRelations,
                               HashSet<Relation> platformRelations) {

        this.helper = new RelationMemberHelper(this);
        this.imageBoundingbox = imageBoundingbox;
        this.osmDataBoundingBox = osmDataBoundingBox;
        this.allContainedNodes = allContainedNodes;
        this.allContainedWays = allContainedWays;
        this.platformRelations = preserveOnlyContainedPlatformRelations(platformRelations);
        this.busRouteRelations = preserveContainedRouteRelations(busRouteRelations);
        this.tramRouteRelations = preserveContainedRouteRelations(tramRouteRelations);
        this.trainRouteRelations = preserveContainedRouteRelations(trainRouteRelations);
        this.subwayRouteRelations = preserveContainedRouteRelations(subwayRouteRelations);
        this.lightrailRouteRelations = preserveContainedRouteRelations(lightrailRouteRelations);
        this.monorailRouteRelations = preserveContainedRouteRelations(monorailRouteRelations);
    }


    /**
     * Gets rid of all the route relations that are completely out of the bound of the bounding box of the map image.
     * This means that they have not one single node inside the bounding box.
     *
     * @param routeRelation
     */
    public HashSet<Relation> preserveContainedRouteRelations(HashSet<Relation> routeRelation) {
        HashSet<Relation> containedRouteRelations = new HashSet<Relation>();

        for (Relation relation : routeRelation) {
            for (RelationMember member : relation.getMembers()) {

                if (helper.dataContains(member)) {
                    containedRouteRelations.add(relation);
                    break;
                }
            }
        }

        return containedRouteRelations;
    }

    /**
     * Preserve only the platform relations that are within the bounding box of the static map image.
     * Contained platform relations are the ones that have at least one node or way as a member that is inside the bounding box.
     * Additionally this method initializes the platformToPlatformNodeMapping Map.
     */
    public HashMap<Long, Relation> preserveOnlyContainedPlatformRelations(HashSet<Relation> allPlatformRelations) {

        HashMap<Long, Relation> containedPlatformRelations = new HashMap<Long, Relation>();

        for (Relation relation : allPlatformRelations) {
            long relationId = relation.getId();

            for (RelationMember member : relation.getMembers()) {
                long memberId = member.getMemberId();
                EntityType memberType = member.getMemberType();

                // current member is a node
                if (memberType == EntityType.Node) {
                    // Check if the node is inside the bounds
                    if (inContainedNodes(memberId)) {
                        // Node member of the relation is inside the image bounds therefore preserve this relation
                        containedPlatformRelations.put(relationId, relation);
                        // Set the correspondend node to this platform to the current node
                        platformToPlatformNodeMapping.put(relationId, memberId);
                        break;
                    }
                }
                // current member is way
                else if (memberType == EntityType.Way) {
                    // Check if the way is inside the bounds
                    if (isInAllContainedWays(memberId)) {
                        // Way member of the relation is inside the image bounds therefore preserve this relation
                        containedPlatformRelations.put(relationId, relation);
                        // Set the corresponded node to the first node of the way that is contained in the bounding box
                        long platformNodeid = getFirstContainedWayNodeId(memberId);
                        platformToPlatformNodeMapping.put(relationId, platformNodeid);
                        break;
                    }
                }
            }
        }

        return containedPlatformRelations;
    }

    /**
     * Returns the id of the first node of the way that is contained in the bounding box of the image.
     * Always check before hand with isInAllContainedWays() if the way is contained in the data !!
     *
     * @param wayId
     * @return first contained node id, 0 if not contained
     */
    public long getFirstContainedWayNodeId(long wayId) {
        Way way = allContainedWays.get(wayId);
        for (WayNode wayNode : way.getWayNodes()) {
            if (inContainedNodes(wayNode.getNodeId())) {
                return wayNode.getNodeId();
            }
        }
        System.out.println("Wanted to retrieve first way node without checking before if way exists in data.");
        return 0;
    }


    /**
     * Checks if a node given by its id is contained in allContainedNodes
     *
     * @param nodeId
     * @return contained
     */
    public boolean inContainedNodes(long nodeId) {
        if (allContainedNodes.get(nodeId) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a way given by a id is contained in allWays
     *
     * @param wayId
     * @return is contained
     */
    public boolean isInAllContainedWays(long wayId) {
        if (allContainedWays.get(wayId) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a given platform relation given by its id is contained in platformRelations
     *
     * @param relationId
     * @return is contained
     */
    public boolean isInAllContainedPlatformRelations(long relationId) {
        if (platformRelations.get(relationId) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the first node of a platform relation that is contained in the bounding box of the image.
     *
     * @param relationId
     * @return nodeId
     */
    public Long getFirstContainedNodeOfPlatformRelation(long relationId) {
        return this.platformToPlatformNodeMapping.get(relationId);
    }

    /**
     * Returns a full node for a given id. The id can be of a node or relation.
     * Always check before if relation is contained in the data with isInAllContainedRelations()
     *
     * @param id of relation or node
     * @return first fullnode
     */
    public Node getFullNodeOfRelationId(long id) {
        long fullNodeid = getFirstContainedNodeOfPlatformRelation(id);
        Node fullNode = allContainedNodes.get(fullNodeid);
        return fullNode;
    }


    /**
     * Returns a full way selected by an id.
     * Check before if the way is contained in the data to avoid Nullpointer exception.
     *
     * @param id
     * @return full way
     */
    public Way getFullWayById(long id) {
        return allContainedWays.get(id);
    }

    public void setOsmDataBoundingBox(Bound osmDataBoundingBox) {
        this.osmDataBoundingBox = osmDataBoundingBox;
    }

    public HashMap<Long, Node> getAllContainedNodes() {
        return allContainedNodes;
    }

    public Bound getOsmDataBoundingBox() {
        return osmDataBoundingBox;
    }

    public HashMap<Long, Way> getAllContainedWays() {
        return allContainedWays;
    }

    public HashSet<Relation> getBusRouteRelations() {
        return busRouteRelations;
    }

    public HashSet<Relation> getTrainRouteRelations() {
        return trainRouteRelations;
    }

    public HashSet<Relation> getSubwayRouteRelations() {
        return subwayRouteRelations;
    }

    public HashSet<Relation> getTramRouteRelations() {
        return tramRouteRelations;
    }

    public HashSet<Relation> getLightrailRouteRelations() {
        return lightrailRouteRelations;
    }

    public HashSet<Relation> getMonorailRouteRelations() {
        return monorailRouteRelations;
    }

    public HashMap<Long, Relation> getPlatformRelations() {
        return platformRelations;
    }

    public Bound getImageBoundingbox() {
        return imageBoundingbox;
    }

    public HashMap<Long, Long> getPlatformToPlatformNodeMapping() {
        return platformToPlatformNodeMapping;
    }
}
