import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.util.ArrayList;
import java.util.HashMap;

public class OsmDataHandler {

    // Member nodes in relations are only saved with their id
    // The id is then used to retrieve all informations about the member node
    private HashMap<Long, Node> allNodes;
    // Contains all ways with their node members and tags
    // Necessary because relations with ways as members contain only their id and not the node member of the way
    private HashMap<Long, Way> allWays;
    // All route relations
    private ArrayList<Relation> busRouteRelations;
    private ArrayList<Relation> trolleybusRouteRelations;
    private ArrayList<Relation> trainRouteRelations;
    private ArrayList<Relation> tramRouteRelations;
    // Necessary because some platforms are mapped as relations (currently not in use because osm data is premanipulated with osmosis)
    private HashMap<Long, Relation> otherRelations;

    /**
     * Initialization of the Data through {@link OSM_Pbf_Reader}
     *
     * @param allNodes
     * @param allWays
     * @param busRouteRelations
     * @param trolleybusRouteRelations
     * @param trainRouteRelations
     * @param tramRouteRelations
     */
    public OsmDataHandler(HashMap<Long, Node> allNodes,
                          HashMap<Long, Way> allWays,
                          ArrayList<Relation> busRouteRelations,
                          ArrayList<Relation> trolleybusRouteRelations,
                          ArrayList<Relation> trainRouteRelations,
                          ArrayList<Relation> tramRouteRelations,
                          HashMap<Long, Relation> platformRelations) {
        this.allNodes = allNodes;
        this.allWays = allWays;
        this.busRouteRelations = busRouteRelations;
        this.trolleybusRouteRelations = trolleybusRouteRelations;
        this.trainRouteRelations = trainRouteRelations;
        this.tramRouteRelations = tramRouteRelations;
        this.otherRelations = platformRelations;
    }

    public HashMap<Long, Node> getAllNodes() {
        return allNodes;
    }

    public HashMap<Long, Way> getAllWays() {
        return allWays;
    }

    public ArrayList<Relation> getBusRouteRelations() {
        return busRouteRelations;
    }

    public ArrayList<Relation> getTrolleybusRouteRelations() {
        return trolleybusRouteRelations;
    }

    public ArrayList<Relation> getTrainRouteRelations() {
        return trainRouteRelations;
    }

    public ArrayList<Relation> getTramRouteRelations() {
        return tramRouteRelations;
    }

    /**
     * Retrieves all bus stops which are contained in the bus routes
     *
     * @return busStops
     */
    public ArrayList<Node> getBusStops() {
        return getStreetStops(busRouteRelations);
    }

    /**
     * Retrieves all trolleybus stops which are contained in the trolleybus routes
     *
     * @return busStops
     */
    public ArrayList<Node> getTrolleybusStops() {
        return getStreetStops(trolleybusRouteRelations);
    }

    /**
     * Retrieves all bus stops which are contained in the bus routes
     *
     * @return trainStops
     */
    public ArrayList<Node> getTrainStops() {
        return getRailwayStops(trainRouteRelations);
    }

    /**
     * Retrieves all tram stops which are contained in the tram routes
     *
     * @return tramStops
     */
    public ArrayList<Node> getTramStops() {
        return getRailwayStops(tramRouteRelations);
    }


    /**
     * Retrieves stops which are contained in the resepective routes
     *
     * @param routeRelations
     * @return
     */
    public ArrayList<Node> getStreetStops(ArrayList<Relation> routeRelations) {

        ArrayList<Node> stops = new ArrayList<Node>();
        // Additional array for ids for faster lookup
        ArrayList<Long> stopIDs = new ArrayList<Long>();

        for (Relation busRelation : routeRelations) {

            // Go through all members to retain all nodes which are stops
            for (RelationMember relationMember : busRelation.getMembers()) {

                // "Although many people use the role "stop" for the bus stops, the role is now discouraged." - OSM Wiki
                // bus stops are mapped as a platform (Node, Way, Relation) and sometimes a stop (node) => use the platform
                if (relationMember.getMemberRole().equalsIgnoreCase("platform") ||
                        relationMember.getMemberRole().equalsIgnoreCase("platform_exit_only") ||
                        relationMember.getMemberRole().equalsIgnoreCase("platform_entry_only")) {

                    // Current member is node
                    if (relationMember.getMemberType() == EntityType.Node) {
                        // Get the node with all its information
                        Node stopNode = allNodes.get(relationMember.getMemberId());
                        // The nodemember of the relation is maybe out of bounds of the osm data so the node is not contained in allNodes
                        if (stopNode != null) {
                            long stopNodeID = stopNode.getId();
                            if (!stopIDs.contains(stopNodeID)) {
                                // Add station to bus stops list
                                stops.add(stopNode);
                                stopIDs.add(stopNodeID);
                            }
                        }
                    }
                    //current member is way
                    // Necessary because some bus platforms are mapped as ways
                    else if (relationMember.getMemberType() == EntityType.Way) {
                        //Get the first node of the way as representation for the platform
                        Way platformWay = allWays.get(relationMember.getMemberId());
                        if (platformWay != null) {
                            WayNode firstWayNode = platformWay.getWayNodes().get(0);
                            Node stopNode = allNodes.get(firstWayNode.getNodeId());

                            // The nodemember of the relation is maybe out of bounds of the osm data so the node is not contained in allNodes
                            if (stopNode != null) {
                                long stopNodeID = stopNode.getId();
                                if (!stopIDs.contains(stopNodeID)) {
                                    // Add station to bus stops list
                                    stops.add(stopNode);
                                    stopIDs.add(stopNodeID);
                                }
                            }
                        }
                    }

                    //current member is a relation
                    // Necessary because some platforms are mapped as relations (these platforms have tags public_transport=platform!)
                    else if (relationMember.getMemberType() == EntityType.Relation) {
                        // Add the relation platform only for buses because for trains have a corresponding stop node that is used
                        //Get the first node of the relation for the platform representation
                        Relation platformRelation = otherRelations.get(relationMember.getMemberId());
                        if (platformRelation != null) {
                            Node stopNode = null;
                            for (RelationMember member : platformRelation.getMembers()) {
                                if (member.getMemberType() == EntityType.Node) {
                                    stopNode = allNodes.get(member.getMemberId());
                                    break;
                                }
                            }
                            // The nodemember of the relation is maybe out of bounds of the osm data so the node is not contained in allNodes
                            if (stopNode != null) {
                                long stopNodeID = stopNode.getId();
                                if (!stopIDs.contains(stopNodeID)) {
                                    // Add station to bus stops list
                                    stops.add(stopNode);
                                    stopIDs.add(stopNodeID);
                                }
                            }
                        }
                    }
                }
            }
        }
        return stops;
    }

    /**
     * Retrieves stops which are contained in the resepective routes
     *
     * @param routeRelations
     * @return stops
     */
    public ArrayList<Node> getRailwayStops(ArrayList<Relation> routeRelations) {
        ArrayList<Node> stops = new ArrayList<Node>();
        // Additional array for ids for faster lookup
        ArrayList<Long> stopIDs = new ArrayList<Long>();
        for (Relation busRelation : routeRelations) {
            // Go through all members to retain all nodes which are stops
            for (RelationMember relationMember : busRelation.getMembers()) {
                // Current member is node
                if (relationMember.getMemberRole().equalsIgnoreCase("stop") ||
                        relationMember.getMemberRole().equalsIgnoreCase("stop_exit_only") ||
                        relationMember.getMemberRole().equalsIgnoreCase("stop_entry_only")) {
                    if (relationMember.getMemberType() == EntityType.Node) {
                        // "Although many people use the role "stop" for the bus stops, the role is now discouraged." - OSM Wiki
                        // bus stops are mapped as a platform (Node, Way, Relation) and sometimes a stop (node) => use the platform
                        // Get the node with all its information
                        Node stopNode = allNodes.get(relationMember.getMemberId());
                        // The nodemember of the relation is maybe out of bounds of the osm data so the node is not contained in allNodes
                        if (stopNode != null) {
                            long stopNodeID = stopNode.getId();
                            if (!stopIDs.contains(stopNodeID)) {
                                // Add station to bus stops list
                                stops.add(stopNode);
                                stopIDs.add(stopNodeID);
                            }
                        }
                    }
                }
            }
        }
        return stops;
    }


    /**
     * Retrieves all ways which are used in bus routes
     *
     * @return busRouteWays
     */
    public ArrayList<Way> getBusRouteWays() {
        return getRouteWays(busRouteRelations);
    }

    /**
     * Retrieves all ways which are used in train routes
     *
     * @return trainRouteWays
     */
    public ArrayList<Way> getTrainRouteWays() {
        return getRouteWays(trainRouteRelations);
    }

    /**
     * Retrieves all ways which are used in tram routes
     *
     * @return trainRouteWays
     */
    public ArrayList<Way> getTramRouteWays() {
        return getRouteWays(tramRouteRelations);
    }


    /**
     * Retrieves all ways which are used in tram routes
     *
     * @return trainRouteWays
     */
    public ArrayList<Way> getTrolleybusRouteWays() {
        return getRouteWays(trolleybusRouteRelations);
    }

    /**
     * This method returns all ways that are contained in a relation
     *
     * @param routeRelations
     * @return ways
     */
    public ArrayList<Way> getRouteWays(ArrayList<Relation> routeRelations) {
        ArrayList<Way> ways = new ArrayList<Way>();
        // Search in all bus relations for their ways
        // Arraylist of ids for quicker lookup
        ArrayList<Long> wayIDs = new ArrayList<Long>();
        for (Relation relation : routeRelations) {
            // Go through all members to retain all nodes which are stops
            for (RelationMember relationMember : relation.getMembers()) {
                // Current member is way
                if (relationMember.getMemberType() == EntityType.Way) {
                    // Retrieve node from allNodes hashmap through id
                    Way routeWay = allWays.get(relationMember.getMemberId());
                    if (routeWay != null) {
                        if (!wayIDs.contains(routeWay.getId())) {
                            ways.add(routeWay);
                            wayIDs.add(routeWay.getId());
                        }
                    }
                }
            }
        }
        return ways;
    }


}
