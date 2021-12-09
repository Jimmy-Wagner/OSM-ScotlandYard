import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
    private ArrayList<Relation> subwayRouteRelations;
    private ArrayList<Relation> monorailRouteRelations;
    private ArrayList<Relation> lightRailRouteRelations;
    // Necessary because some platforms are mapped as relations (currently not in use because osm data is premanipulated with osmosis)
    private HashMap<Long, Relation> platformRelations;

    public HashMap<Long, Long> getPlatformToStopNodeMappingBus() {
        return platformToStopNodeMappingBus;
    }

    // Stops are mapped to their own id
    // PLatforms are mapped to their first node or the coherent stopNode
    private HashMap<Long, Long> platformToStopNodeMappingBus;
    private HashMap<Long, Long> platfromToStopMappingSubway;


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
                          ArrayList<Relation> subwayRouteRelations,
                          ArrayList<Relation> monorailRouteRelations,
                          ArrayList<Relation> lightRailRouteRelations,
                          HashMap<Long, Relation> platformRelations) {
        this.allNodes = allNodes;
        this.allWays = allWays;
        this.busRouteRelations = busRouteRelations;
        this.trolleybusRouteRelations = trolleybusRouteRelations;
        this.trainRouteRelations = trainRouteRelations;
        this.subwayRouteRelations = subwayRouteRelations;
        this.lightRailRouteRelations = lightRailRouteRelations;
        this.monorailRouteRelations = monorailRouteRelations;
        this.tramRouteRelations = tramRouteRelations;
        this.platformRelations = platformRelations;
        // Initialize this list
        platformToStopNodeMappingBus = new HashMap<Long, Long>();
        platfromToStopMappingSubway = new HashMap<Long, Long>();
    }



    /**
     * Calls the getStreetStopsTest function with bus relation
     * @return mergedStops
     */
    public ArrayList<Node> getStreetBusStopsRichtig() {
        return getStreetStopsRichtig(busRouteRelations, platformToStopNodeMappingBus);
    }

    /**
     * Calls the getStreetStopsTest function with bus relation
     * @return mergedStops
     */
    public ArrayList<Node> getSubwayStopsRichtig() {
        return getStreetStopsRichtig(subwayRouteRelations, platformToStopNodeMappingBus);
    }


    /**
     * This method returns all platforms and stops as stops and
     * fills the platformToStopNodeMapping with the corresponding values.
     * @param routeRelations
     * @return mergedStreetStops stops that are mapped as platform and stop are merged to one stop
     */
    private ArrayList<Node> getStreetStopsRichtig(ArrayList<Relation> routeRelations, HashMap<Long, Long> platformToStopNodeMapping) {
        ArrayList<Node> stops = new ArrayList<Node>();
        // Additional array for ids for faster lookup
        ArrayList<Long> stopIDs = new ArrayList<Long>();
        ArrayList<Node> removeableStops = new ArrayList<Node>();

        RelationMember lastMember;
        boolean mergeable;


        for (Relation currentRelation : routeRelations) {
            lastMember = null;

            // Go through all members to retain all nodes which are stops
            for (RelationMember relationMember : currentRelation.getMembers()) {
                // in that case all stops have been read already
                if (hasNoRole(relationMember)) break;

                // "Although many people use the role "stop" for the bus stops, the role is now discouraged." - OSM Wiki
                // bus stops are mapped as a platform (Node, Way, Relation) and sometimes a stop (node) => use the platform
                if (hasRolePlatform(relationMember) || hasRoleStop(relationMember)) {


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
                                // Map the stop to itself initially
                                platformToStopNodeMapping.put(relationMember.getMemberId(), stopNodeID);
                            }
                        }
                    }
                    //current member is way
                    // Necessary because some bus platforms are mapped as ways
                    else if (relationMember.getMemberType() == EntityType.Way) {
                        //Get the first node of the way as representation for the platform
                        Way platformWay = allWays.get(relationMember.getMemberId());
                        Node stopNode = getFirstNode(platformWay);
                        // The nodemember of the relation is maybe out of bounds of the osm data so the node is not contained in allNodes
                        if (stopNode != null) {
                            long stopNodeID = stopNode.getId();
                            if (!stopIDs.contains(stopNodeID)) {
                                // Add station to bus stops list
                                stops.add(stopNode);
                                stopIDs.add(stopNodeID);
                                // Map the way relation to the first node of the relation
                                platformToStopNodeMapping.put(relationMember.getMemberId(), stopNodeID);
                            }
                        }
                    }

                    //current member is a relation
                    // Necessary because some platforms are mapped as relations (these platforms have tags public_transport=platform!)
                    else if (relationMember.getMemberType() == EntityType.Relation) {
                        // Add the relation platform only for buses because for trains have a corresponding stop node that is used
                        //Get the first node of the relation for the platform representation
                        Relation platformRelation = platformRelations.get(relationMember.getMemberId());
                        if (platformRelation != null) {
                            Node stopNode = getFirstNode(platformRelation);
                            // The nodemember of the relation is maybe out of bounds of the osm data so the node is not contained in allNodes
                            if (stopNode != null) {
                                long stopNodeID = stopNode.getId();
                                if (!stopIDs.contains(stopNodeID)) {
                                    // Add station to bus stops list
                                    stops.add(stopNode);
                                    stopIDs.add(stopNodeID);
                                    // Map the platform relation to the first node of the relation
                                    platformToStopNodeMapping.put(relationMember.getMemberId(), stopNodeID);
                                }
                            }
                        }
                    }
                }
                if (hasRolePlatform(relationMember) && lastMember != null && hasRoleStop(lastMember)) {
                    Node lastFullNode = allNodes.get(lastMember.getMemberId());
                    if (relationMember.getMemberType() == EntityType.Node) {
                        Node currentFullNode = allNodes.get(relationMember.getMemberId());
                        if (lastFullNode != null && currentFullNode != null) {
                            mergeable = tryToMerge(lastFullNode, currentFullNode);
                            if (mergeable && !removeableStops.contains(currentFullNode)) {
                                removeableStops.add(currentFullNode);
                                // Map the current platform to the coherent stop node
                                platformToStopNodeMapping.put(relationMember.getMemberId(), lastFullNode.getId());
                            }
                        }
                    } else if (relationMember.getMemberType() == EntityType.Way) {
                        Way way = allWays.get(relationMember.getMemberId());
                        Node stopNode = getFirstNode(way);
                        if (lastFullNode != null && stopNode != null) {
                            mergeable = tryToMerge(lastFullNode, stopNode);
                            if (mergeable && !removeableStops.contains(stopNode)) {
                                removeableStops.add(stopNode);
                                platformToStopNodeMapping.put(relationMember.getMemberId(), lastFullNode.getId());
                            }
                        }
                    } else if (relationMember.getMemberType() == EntityType.Relation) {
                        Relation platformRelation = platformRelations.get(relationMember.getMemberId());
                        if (platformRelation != null) {
                            Node stopNode = getFirstNode(platformRelation);
                            if (lastFullNode != null && stopNode != null) {
                                mergeable = tryToMerge(lastFullNode, stopNode);
                                if (mergeable && !removeableStops.contains(stopNode)) {
                                    removeableStops.add(stopNode);
                                    platformToStopNodeMapping.put(relationMember.getMemberId(), lastFullNode.getId());
                                }
                            }
                        }
                    }
                }
                lastMember = relationMember;
            }
        }
        stops.removeAll(removeableStops);
        return stops;
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

                // in that case all stops have been read already
                if (hasNoRole(relationMember)) break;

                // "Although many people use the role "stop" for the bus stops, the role is now discouraged." - OSM Wiki
                // bus stops are mapped as a platform (Node, Way, Relation) and sometimes a stop (node) => use the platform
                if (hasRolePlatform(relationMember) || hasRoleStop(relationMember)) {

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
                        Node stopNode = getFirstNode(platformWay);
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

                    //current member is a relation
                    // Necessary because some platforms are mapped as relations (these platforms have tags public_transport=platform!)
                    else if (relationMember.getMemberType() == EntityType.Relation) {
                        // Add the relation platform only for buses because for trains have a corresponding stop node that is used
                        //Get the first node of the relation for the platform representation
                        Relation platformRelation = platformRelations.get(relationMember.getMemberId());
                        if (platformRelation != null) {
                            Node stopNode = getFirstNode(platformRelation);
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


    public boolean hasNoRole(RelationMember member) {
        return member.getMemberRole().equalsIgnoreCase("");
    }


    public boolean hasRoleStop(RelationMember member) {
        if (member.getMemberRole().equalsIgnoreCase("stop") ||
                member.getMemberRole().equalsIgnoreCase("stop_entry_only") ||
                member.getMemberRole().equalsIgnoreCase("stop_exit_only")) {
            return true;
        }
        return false;
    }

    public boolean hasRolePlatform(RelationMember member) {
        if (member.getMemberRole().equalsIgnoreCase("platform") ||
                member.getMemberRole().equalsIgnoreCase("platform_entry_only") ||
                member.getMemberRole().equalsIgnoreCase("platform_exit_only")) {
            return true;
        }
        return false;
    }

    /**
     * This method takes two Relationmembers (which should be one stop and one platform) and trys to merge them into one {@link MergedStopAndPlatform}
     * and adds this eventually to mergedBusStopsAndPlatforms
     *
     * @param stop     should be a stop of Type Node
     * @param platform should be a platform of type Node, Way or Relation
     * @return wasMerged true if the parameters have been merged into one {@link MergedStopAndPlatform}
     */
    private boolean mergeBusStopsAndPlatforms(RelationMember stop, RelationMember platform) {
        // Only for platform necessary because stop is always a node
        EntityType platformType = platform.getMemberType();
        long stopID = stop.getMemberId();
        long platformID = platform.getMemberId();
        EntityType node = EntityType.Node;
        EntityType way = EntityType.Way;
        EntityType relation = EntityType.Relation;
        Node fullStopNode = allNodes.get(stopID);
        Node fullNodePlatformNode;
        // Of way and relation only one because there can only one member which is relation or way (platform)
        // Because at least one member of a merge has to be a stop which is always a node
        Way fullWayPlatform;
        Relation fullRelationPlatform;
        boolean wasMerged = false;

        switch (platformType) {
            case Node:
                fullNodePlatformNode = allNodes.get(platformID);
                // Try to merge platform into stop
                wasMerged = tryToMerge(fullStopNode, fullNodePlatformNode);
                break;
            case Way:
                fullWayPlatform = allWays.get(platformID);
                wasMerged = tryToMerge(fullStopNode, fullWayPlatform);
                break;
            case Relation:
                fullRelationPlatform = platformRelations.get(platformID);
                wasMerged = tryToMerge(fullStopNode, fullRelationPlatform);
                break;
        }
        return wasMerged;

    }


    /**
     * This method checks if a stop and a platform are coherent and merges them if they are into one {@link MergedStopAndPlatform}
     *
     * @param stop
     * @param platform
     * @return wasMerged
     */
    public boolean tryToMerge(Node stop, Node platform) {
        if (stop == null || platform == null) {
            return false;
        }
        if (calculateDistanceTwoNodes(stop, platform) < 50) {
            return true;
        }
        return false;
    }

    /**
     * This method checks if a stop and a platform are coherent and merges them if they are into one {@link MergedStopAndPlatform}
     *
     * @param stop
     * @param platform
     * @return wasMerged
     */
    private boolean tryToMerge(Node stop, Way platform) {
        Node firstNode = getFirstNode(platform);
        if (calculateDistanceTwoNodes(stop, firstNode) < 50) {
            return true;
        }
        return false;
    }

    /**
     * Returns the first node of the way that is contained in scope of the osm data of the pbf file that is read in {@link OSM_Pbf_Reader}
     *
     * @param way
     * @return firstContainedNode
     */
    private Node getFirstNode(Way way) {
        if (way == null) {
            return null;
        }
        Node firstNode = allNodes.get(way.getWayNodes().get(0).getNodeId());
        return firstNode;
    }

    /**
     * This method checks if a stop and a platform are coherent and merges them if they are into one {@link MergedStopAndPlatform}
     *
     * @param stop
     * @param platform
     * @return wasMerged
     */
    private boolean tryToMerge(Node stop, Relation platform) {
        Node firstNode = getFirstNode(platform);
        if (calculateDistanceTwoNodes(stop, firstNode) < 50) {
            return true;
        }
        return false;
    }

    /**
     * Returns the first node of the way that is contained in scope of the osm data of the pbf file that is read in {@link OSM_Pbf_Reader}
     * TODO: funktioniert einwandfrei
     *
     * @param relation
     * @return firstContainedNode
     */
    public Node getFirstNode(Relation relation) {
        Node firstNodeNotNull = null;
        long relationMemberID;
        for (RelationMember member : relation.getMembers()) {

            relationMemberID = member.getMemberId();

            if (member.getMemberType() == EntityType.Node) {
                firstNodeNotNull = allNodes.get(relationMemberID);
                return firstNodeNotNull;

            } else if (member.getMemberType() == EntityType.Way) {
                Way currentWay = allWays.get(relationMemberID);
                if (currentWay != null) {
                    // Retrieve the first node that is not null from that way
                    firstNodeNotNull = getFirstNode(currentWay);
                    return firstNodeNotNull;
                }
            }
        }
        return null;
    }


    /**
     * Checks if the first member has the role stop and the second member the role platform.
     *
     * @param member1
     * @param member2
     * @return stopAndPlatform
     */
    public boolean firstStopSecondPlatform(RelationMember member1, RelationMember member2) {
        if (member1 == null || member2 == null) {
            return false;
        }
        // 1 is stop
        if (member1.getMemberRole().equalsIgnoreCase("stop") ||
                member1.getMemberRole().equalsIgnoreCase("stop_entry_only") ||
                member1.getMemberRole().equalsIgnoreCase("stop_exit_only")) {
            // 2 is platform
            if (member2.getMemberRole().equalsIgnoreCase("platform") ||
                    member2.getMemberRole().equalsIgnoreCase("platform_entry_only") ||
                    member2.getMemberRole().equalsIgnoreCase("platform_exit_only")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the distance in meters of two nodes
     * TODO: Funktioniert einwandfrei
     *
     * @param node1
     * @param node2
     * @return distance in meters
     */
    public double calculateDistanceTwoNodes(Node node1, Node node2) {
        if (node1 == null || node2 == null) {
            return Double.MAX_VALUE;
        }
        final double p = 0.017453292519943295;    // Math.PI / 180
        double lat1 = node1.getLatitude();
        double lon1 = node1.getLongitude();
        double lat2 = node2.getLatitude();
        double lon2 = node2.getLongitude();
        double a = 0.5 - Math.cos((lat2 - lat1) * p) / 2 +
                Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                        (1 - Math.cos((lon2 - lon1) * p)) / 2;

        return 12742 * Math.asin(Math.sqrt(a)) * 1000; // 2 * R; R = 6371 km
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
     * Retrieves all train stops which are contained in the bus routes
     *
     * @return trainStops
     */
    public ArrayList<Node> getTrainStops() {
        return getRailwayStops(trainRouteRelations);
    }

    /**
     * Retrieves all subway stops which are contained in the bus routes
     *
     * @return trainStops
     */
    public ArrayList<Node> getSubwayStops() {
        return getRailwayStops(subwayRouteRelations);
    }

    /**
     * Retrieves all lightrail stops which are contained in the bus routes
     *
     * @return trainStops
     */
    public ArrayList<Node> getlightRailStops() {
        return getRailwayStops(lightRailRouteRelations);
    }

    /**
     * Retrieves all monotrail stops which are contained in the bus routes
     *
     * @return trainStops
     */
    public ArrayList<Node> getmonoRailStops() {
        return getRailwayStops(monorailRouteRelations);
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
                if (hasRoleStop(relationMember)) {
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
     * Retrieves all ways which are used in train routes
     *
     * @return trainRouteWays
     */
    public ArrayList<Way> getSubwayRouteWays() {
        return getRouteWays(subwayRouteRelations);
    }

    /**
     * Retrieves all ways which are used in train routes
     *
     * @return trainRouteWays
     */
    public ArrayList<Way> getLightrailRouteWays() {
        return getRouteWays(lightRailRouteRelations);
    }

    /**
     * Retrieves all ways which are used in train routes
     *
     * @return trainRouteWays
     */
    public ArrayList<Way> getmonorailRouteWays() {
        return getRouteWays(monorailRouteRelations);
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
