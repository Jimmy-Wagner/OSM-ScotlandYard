
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class should thin out a list of bus stops
 */
public class ThinOut {
    private OsmDataHandler dataHandler;

    public ThinOut(OsmDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }


    /**
     * This method gets rid of all bus stops that are travelled from or to less than 3 other bus stops by bus routes.
     *
     * @param stops                     bus stops from all routes
     * @param platformToStopNodeMapping to see how the platforms are presented in the stops
     *                                  They are presented as the first node of the platform or a coherent stop node.
     *                                  Platforms or stops which nodes are contained in a Relation but not contained as
     *                                  a full node should not be contained in here!!
     * @return thinned out list of stops based on their connection degree
     */
    public ArrayList<Node> thinOutStops(ArrayList<Node> stops,
                                        HashMap<Long, Long> platformToStopNodeMapping,
                                        HashMap<Long, Long> platformToPlatformMapping,
                                        HashSet<Long> allPlatformAndStopIds,
                                        ArrayList<Relation> routeRelations) {

        /*System.out.println("Platform To Stopnode mapping: ");
        for (long hi: platformToStopNodeMapping.keySet()){
            if (hi == 3977690257L){
                System.out.println("Sucy stop (3977690257) gemappt auf: " + platformToStopNodeMapping.get(hi));
            }
            else if(hi == 4005786051L){
                System.out.println("Sucy platform (4005786051) gemappt auf: " + platformToStopNodeMapping.get(hi));
            }
        }*/


        // Retrieve for every platform and stop their connections. Even the ones for which the full nodes are not contained in the data.
        HashMap<Long, HashSet<Long>> connectionsOfAllStopsAndPlatforms =
                retrieveConnectionsOfAllStopsAndPlatforms(allPlatformAndStopIds, routeRelations);



        // Get rid of the connection information about stops and platforms that dont have a corresponding fullnode and are therefore not contained in the osm data.
        HashMap<Long, HashSet<Long>> connectionsOfContainedStopsAndPlatforms =
                retrieveOnlyContainedConnections(connectionsOfAllStopsAndPlatforms, platformToStopNodeMapping.keySet());

        // Merge the platforms to their corresponding stop in the connection map if there is one
        HashMap<Long, HashSet<Long>> connectionsOfMergedStopsAndPlatforms =
                mergeConnectionsOfStopsAndPlatforms(connectionsOfContainedStopsAndPlatforms, platformToPlatformMapping, platformToStopNodeMapping);


        /*System.out.println("Number of connectionsOfMergedStopsAndPlatforms " + connectionsOfMergedStopsAndPlatforms.size());
        for (long hi : connectionsOfMergedStopsAndPlatforms.keySet()) {
            System.out.println(";;;;;;;;;;;;;;;;;;;;;");
            System.out.println("Stop id: " + hi);
            for (long ki : connectionsOfMergedStopsAndPlatforms.get(hi)) {
                System.out.println("Connected to: " + ki);
            }
        }*/

        HashMap<Long, HashSet<Long>> connectionsOfNodes = getPlatformNodesConnections(platformToStopNodeMapping, connectionsOfMergedStopsAndPlatforms);

        HashMap<Long, Integer> degreeOfConnection = getConnectionDegree(connectionsOfNodes);


        /*System.out.println("---------------------");
        System.out.println("degreeofConnections: ");
        for (long hi : degreeOfConnection.keySet()) {
            System.out.println("StopNodeID: " + hi);
            System.out.println("Degree: " + degreeOfConnection.get(hi));
        }*/

        ArrayList<Node> thinnedStops = thinStopsBasedOnDegree(stops, degreeOfConnection);


        return thinnedStops;
    }


    private HashMap<Long, HashSet<Long>> getPlatformNodesConnections(HashMap<Long, Long> platformToStopNodeMapping,
                                                                     HashMap<Long, HashSet<Long>> connectionsOfMergedStopsAndPlatforms) {
        HashMap<Long, HashSet<Long>> connectionsNodes = new HashMap<Long, HashSet<Long>>();
        // Substitute left side with right side
        for (long i: connectionsOfMergedStopsAndPlatforms.keySet()){
            connectionsNodes.put(platformToStopNodeMapping.get(i), connectionsOfMergedStopsAndPlatforms.get(i));
        }
        return connectionsNodes;
    }


    /**
     * Merges the connections of coherent stops and platforms into one entry with the key of the stop.
     * After this method the platformnodes ids are on the left side of the mapping instead of the platform ids
     *
     * @param connectionsOfContainedStopsAndPlatforms coherent stops and platforms are seperated (stopnode->stopnodes, platforms, platforms->stonodes,platforms)
     * @param platformToPlatformMapping               indicates which platform needs to be merged with which stop (platform->platform,stopnode | stopnode-> stopnode)
     * @return mergedConnections coherent stops and platforms have a combined entry
     */
    private HashMap<Long, HashSet<Long>> mergeConnectionsOfStopsAndPlatforms(HashMap<Long, HashSet<Long>> connectionsOfContainedStopsAndPlatforms,
                                                                             HashMap<Long, Long> platformToPlatformMapping,
                                                                             HashMap<Long, Long> platformToStopNodeMapping) {

        HashMap<Long, HashSet<Long>> mergedConnections = new HashMap<Long, HashSet<Long>>();
        HashSet<Long> connectionsLeftSide;
        HashSet<Long> connectionsRightSide;
        HashSet<Long> connectionsBothSides;
        long rightSideOfMapping;

        for (long leftSideOfMapping : platformToStopNodeMapping.keySet()) {

            rightSideOfMapping = platformToPlatformMapping.get(leftSideOfMapping);

            // These are connections of a stopnode or a platform
            connectionsRightSide = connectionsOfContainedStopsAndPlatforms.get(rightSideOfMapping);
            // platform ids are substituted through platformnode ids or stopnode ids
            connectionsRightSide = mergeStopsAndPlatforms(connectionsRightSide, platformToPlatformMapping);

            // These are connections of a platform or of a stopnode (stopnode, platform)
            connectionsLeftSide = connectionsOfContainedStopsAndPlatforms.get(leftSideOfMapping);
            // platform ids are substituted through platformnode ids or stopnode ids
            connectionsLeftSide = mergeStopsAndPlatforms(connectionsLeftSide, platformToPlatformMapping);

            connectionsBothSides = new HashSet<Long>();

             HashSet<Long> merge = mergedConnections.get(rightSideOfMapping);
             if (merge != null){
                 connectionsBothSides.addAll(mergedConnections.get(rightSideOfMapping));
             }

            connectionsBothSides.addAll(connectionsRightSide);
            connectionsBothSides.addAll(connectionsLeftSide);

            // test should be false every time because the platform id is removed but thge platform id should be already substituted with its platfromnode
            boolean test = connectionsBothSides.remove(leftSideOfMapping);
            if (test){
                System.out.println("Fehler !!!!!!!!");
            }

            connectionsBothSides.remove(rightSideOfMapping);
            connectionsBothSides.remove(leftSideOfMapping);

            mergedConnections.put(rightSideOfMapping, connectionsBothSides);
        }

        return mergedConnections;
    }


    /**
     * FIXME! This method is currently broken
     * Substitute platform through stopnode or platform node.
     * If theres no platformnode or stopnode for a platform add the platform or platformnode.
     * After this method the platform ids in the right side of connectionLists are substituted through its stopnode ids
     *
     * @param connections               list of platforms and stopnodes
     * @param platformToPlatformMapping (platform -> platformNode, stopnode,       stopnode->stopnode)
     * @return
     */
    private HashSet<Long> mergeStopsAndPlatforms(HashSet<Long> connections, HashMap<Long, Long> platformToPlatformMapping) {

        HashSet<Long> mergedConnection = new HashSet<Long>();
        long rightSideMapping;
        long leftSideMapping;

        if (connections != null){
            for (long idOfStopOrPlatform : connections) {
                leftSideMapping = idOfStopOrPlatform;
                if (platformToPlatformMapping.get(leftSideMapping) == null){

                }
                else{
                    rightSideMapping = platformToPlatformMapping.get(leftSideMapping);
                    mergedConnection.add(rightSideMapping);
                }
            }
        }


        return mergedConnection;
    }


    /**
     * Gets rid of stops nodes that are connected to less than 3 other bus stops.
     *
     * @param stops
     * @param degrees
     * @return
     */
    private ArrayList<Node> thinStopsBasedOnDegree(ArrayList<Node> stops, HashMap<Long, Integer> degrees) {
        ArrayList<Node> thinnedStops = new ArrayList<Node>();
        for (Node currentNode : stops) {
            // Only add the stops that are connected to at least 3 other stops
            if (degrees.get(currentNode.getId()) >= 3) {
                thinnedStops.add(currentNode);
            }
        }
        return thinnedStops;
    }

    /**
     * Only evaluates the number of connections for every stop node and returns the size.
     * This enables a evaluation for the importance of every stop.
     * Stops that are a starting or end point of a route have the degree 100.
     *
     * @param connections for merged stop nodes
     * @return connectionSize for each stop node
     */
    private HashMap<Long, Integer> getConnectionDegree(HashMap<Long, HashSet<Long>> connections) {
        HashMap<Long, Integer> connectionsDegrees = new HashMap<Long, Integer>();
        int numberOfConnections;
        HashSet<Long> connectionsOfCurrent;

        if (connections.keySet() != null && connections != null){

            for (long stopId : connections.keySet()) {

                connectionsOfCurrent = connections.get(stopId);
                // In this case this stop is a starting or endpoint of a route
                if (connectionsOfCurrent.contains(0L) || connectionsOfCurrent.contains(-1L)) {
                    numberOfConnections = 100;
                } else {
                    numberOfConnections = connectionsOfCurrent.size();
                }
                connectionsDegrees.put(stopId, numberOfConnections);
            }
        }


        return connectionsDegrees;
    }


    /**
     * Replaces all connection lists of all stops with connection lists in which platforms and stops are merged if possible.
     *
     * @param unmergedConnectionLists
     * @param platformToStopNodeMapping
     * @retrun mergedConnectionLists
     */
    private HashMap<Long, HashSet<Long>> mergeConnectionLists(HashMap<Long, HashSet<Long>> unmergedConnectionLists, HashMap<Long, Long> platformToStopNodeMapping) {

        HashMap<Long, HashSet<Long>> mergedConnectionLists = new HashMap<Long, HashSet<Long>>();
        long mappingLeft;
        long mappingRight;
        HashSet<Long> mergedConnectionList;

        for (long stopID : unmergedConnectionLists.keySet()) {

            // This contains the merged connection list for the stop with id=stopID
            mergedConnectionList = new HashSet<Long>();

            for (long connectedStopOrPlatformId : unmergedConnectionLists.get(stopID)) {

                mappingLeft = connectedStopOrPlatformId;
                mappingRight = platformToStopNodeMapping.get(connectedStopOrPlatformId);
                // Only add the stopnodes and get rid of the platform nodes that point to the stop nodes
                if (mappingLeft == mappingRight) {
                    mergedConnectionList.add(connectedStopOrPlatformId);
                }
            }
            // Replace the unmerged connection list for this stop with the merged connection list
            mergedConnectionLists.put(stopID, mergedConnectionList);
        }
        return mergedConnectionLists;
    }


    /**
     * Gets rid of platform and stop keys which full nodes arent contained in the osm data.
     *
     * @param allConnections             for every platform and stop
     * @param containedStopsAndPlatforms only platforms and stop ids which full nodes are contained in the data are contained here
     * @return containedConnections
     */
    private HashMap<Long, HashSet<Long>> retrieveOnlyContainedConnections(HashMap<Long, HashSet<Long>> allConnections,
                                                                          Set<Long> containedStopsAndPlatforms) {

        HashMap<Long, HashSet<Long>> containedConnections = new HashMap<Long, HashSet<Long>>();

        //platformToStopNodeMapping contains only the platforms and stops which full nodes are contained in the osm data
        for (long idOfContained : containedStopsAndPlatforms) {
            containedConnections.put(idOfContained, allConnections.get(idOfContained));
        }

        return containedConnections;
    }


    /**
     * Retrieves for every stop or platform their corresponding connected stops and platforms for all platforms
     * and stops contained in all public transport routes.
     * Keep in mind that platforms and stops are also connected when they are in reality one stop and should be merged.
     *
     * @param allPlatformAndStopIds The ids of all platforms and stops even the ones which full nodes are not contained in the osm data
     * @param routeRelations        relations of a certain route
     * @return stopConnections for every platform and stop their connected platforms and stops.
     */
    private HashMap<Long, HashSet<Long>> retrieveConnectionsOfAllStopsAndPlatforms(HashSet<Long> allPlatformAndStopIds,
                                                                                   ArrayList<Relation> routeRelations) {
        // Save all other stops that are connected in some routes to this stop
        // degreeOfConnectionPerStop only contains the stops and platforms as keys which full nodes are contained in the osm data
        // Starting nodes have the id =0, ending nodes have the id=-1
        HashMap<Long, HashSet<Long>> stopConnections = new HashMap<Long, HashSet<Long>>();

        for (long currentStopId : allPlatformAndStopIds) {
            stopConnections.put(currentStopId, new HashSet<Long>());
        }


        long currentMemberId;
        long lastMemberId;
        RelationMember lastMember;
        // Indicates wheter a route starts at this node
        boolean isFirstMember;

        for (Relation currentRelation : routeRelations) {
            lastMember = null;
            isFirstMember = true;

            for (RelationMember currentMember : currentRelation.getMembers()) {
                currentMemberId = currentMember.getMemberId();
                // The stops end here so its the last stop
                if (dataHandler.hasNoRole(currentMember)) {
                    // If there was actually a last stop
                    if (lastMember != null) {
                        lastMemberId = lastMember.getMemberId();
                        // Mark the last member as end station
                        if (stopConnections.get(lastMemberId) != null){
                            stopConnections.get(lastMemberId).add(-1L);
                        }
                    }
                    // Go to the next relation because there wont follow any stops
                    break;
                } else if (dataHandler.hasRolePlatform(currentMember) || dataHandler.hasRoleStop(currentMember)) {

                    // Add the connection to node 0 if its the starting point of a route
                    if (isFirstMember) {
                        stopConnections.get(currentMemberId).add(0L);
                    }
                    // In this case you have the last member connected to this member
                    else {
                        lastMemberId = lastMember.getMemberId();
                        stopConnections.get(currentMemberId).add(lastMemberId);
                        if (stopConnections.get(lastMemberId) != null){
                            stopConnections.get(lastMemberId).add(currentMemberId);
                        }
                    }

                }
                lastMember = currentMember;
                isFirstMember = false;
            }
        }

        return stopConnections;
    }
}
