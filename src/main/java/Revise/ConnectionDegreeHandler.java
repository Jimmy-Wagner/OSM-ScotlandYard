package Revise;

import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.util.HashMap;
import java.util.HashSet;

public class ConnectionDegreeHandler {
    private OsmDataContainer dataContainer;
    private RelationMemberHelper helper;

    public ConnectionDegreeHandler(OsmDataContainer dataContainer, RelationMemberHelper helper) {
        this.dataContainer = dataContainer;
        this.helper = helper;
    }

    /**
     * Merges the connection lists of corresponding stop nodes and platforms and gets rid of the platforms in this list.
     * @param connectionLists necessary that connectionLists only contains
     * @param platformToStopNodeMapping
     * @return merged connection lists
     */
    public HashMap<Long, HashSet<Long>> mergeConnections(HashMap<Long, HashSet<Long>> connectionLists, HashMap<Long, Long> platformToStopNodeMapping){

        HashMap<Long, HashSet<Long>> mergedConnectionLists = new HashMap<Long, HashSet<Long>>(connectionLists);
        long correspondingStopNodeId;
        HashSet<Long> connectionsPlatform;
        HashSet<Long> connectionsStop;


        for (long platformId: platformToStopNodeMapping.keySet()){
            connectionsPlatform = connectionLists.get(platformId);
            correspondingStopNodeId = platformToStopNodeMapping.get(platformId);
            connectionsStop = connectionLists.get(correspondingStopNodeId);
            connectionsStop.addAll(connectionsPlatform);
            connectionsStop.remove(platformId);
            connectionsStop.remove(correspondingStopNodeId);
            mergedConnectionLists.remove(platformId);
        }

        return mergedConnectionLists;
    }


    /**
     * Returns for every contained stop and platform a list with its connected platforms and stops.
     * Every node and platform is depicted with its id.
     *
     * @param routeRelation
     * @param containedStopsAndPlatforms
     * @return connectionLists
     */
    public HashMap<Long, HashSet<Long>> getConnectionsOfContainedStopsAndPlatforms(HashSet<Relation> routeRelation,
                                                                                   HashSet<Long> containedStopsAndPlatforms) {

        HashMap<Long, HashSet<Long>> connectionLists = new HashMap<Long, HashSet<Long>>();
        // Initialize empty sets for every contained stop and platform
        for (long idOfContained: containedStopsAndPlatforms){
            connectionLists.put(idOfContained, new HashSet<Long>());
        }

        RelationMember lastMember;
        long memberID;
        HashSet<Long> connectionsMember;
        HashSet<Long> connectionsLastMember;

        for (Relation relation : routeRelation) {
            lastMember = null;
            for (RelationMember member : relation.getMembers()) {
                memberID = member.getMemberId();
                // All stops have been read
                if (helper.hasNoRole(member)) break;
                else if (helper.hasRolePlatform(member) || helper.hasRoleStop(member)) {
                    if (lastMember != null){
                        // Both stops/platforms are contained in the data
                        if (helper.dataContains(member) && helper.dataContains(lastMember)){
                            // Add mutual connections
                            connectionsMember = connectionLists.get(memberID);
                            connectionsMember.add(lastMember.getMemberId());
                            connectionsLastMember = connectionLists.get(lastMember.getMemberId());
                            connectionsLastMember.add(memberID);
                        }
                    }
                }
                lastMember = member;
            }
        }
        return connectionLists;
    }
}
