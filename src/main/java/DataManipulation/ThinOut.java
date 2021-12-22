package DataManipulation;

import DataContainer.RelationMemberHelper;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;


import java.util.HashMap;
import java.util.HashSet;

public class ThinOut {
    private RelationMemberHelper memberHelper;


    public ThinOut(RelationMemberHelper helper){
        this.memberHelper = helper;

    }

    public HashSet<Long> thinOutHalts(HashSet<Relation> routeRelations, HashSet<Long> unmergedPlatformAndStopIds, HashMap<Long, Long> platformToStopMapping){
        HashSet<Long> valuableHalts = new HashSet<Long>();
        HashMap<Long, HashSet<Long>> connectionLists = createConnectionLists(unmergedPlatformAndStopIds, routeRelations);
        // Merge lists for stops and platforms that will be merged
        connectionLists = mergeConnectionLists(platformToStopMapping, connectionLists);
        // Get rid of the merged platforms in all connection lists of all halts
        connectionLists = removeMergedPlatforms(connectionLists, platformToStopMapping);


        HashSet<Long> haltConnections;
        for (long haltId: connectionLists.keySet()){
            haltConnections = connectionLists.get(haltId);
            if (haltConnections.size()>=3){
                valuableHalts.add(haltId);
            }
        }
        return valuableHalts;
    }

    /**
     * Substitutes all platforms that are merged into stops from all connection lists with the stops.
     * @param connectionLists
     * @param platformToStopMapping
     * @return
     */
    public HashMap<Long, HashSet<Long>> removeMergedPlatforms(HashMap<Long, HashSet<Long>> connectionLists, HashMap<Long, Long> platformToStopMapping){
        HashMap<Long, HashSet<Long>> cleanedConnectionLists = new HashMap<Long, HashSet<Long>>(connectionLists);
        HashSet<Long> haltConnections;
        //needs this copy because otherwise the haltConnections list would be manipulated while iterating over it in the for loop
        HashSet<Long> copyHaltConnections;
        long stopId;
        // Go through all connection lists
        for (long haltId: cleanedConnectionLists.keySet()){
            haltConnections = connectionLists.get(haltId);
            // Go through each halt of that connection list
            copyHaltConnections = new HashSet<Long>(haltConnections);
            for (long connectedHaltId: copyHaltConnections){
                // If the halt is a aplatform that is mergeable
                if (platformToStopMapping.get(connectedHaltId) != null){
                    stopId = platformToStopMapping.get(connectedHaltId);
                    // Substitute the platform with the stop and get rid of the platform in that list
                    haltConnections.remove(connectedHaltId);
                    haltConnections.add(stopId);
                }
            }
        }
        return cleanedConnectionLists;
    }

    /**
     * Takes connection lists of platforms and stops. Merges the connection lists of platforms that will be merged into stops,
     * to the connection list of that stop.
     * Merges implicitly platforms into stops if possible.
     * @param platformToStopMapping
     * @param connectionLists
     * @return connection Lists of merged Halts
     */
    private HashMap<Long, HashSet<Long>> mergeConnectionLists(HashMap<Long, Long> platformToStopMapping, HashMap<Long, HashSet<Long>> connectionLists){
        HashMap<Long, HashSet<Long>> mergedConnectionLists = new HashMap<Long, HashSet<Long>>(connectionLists);
        HashSet<Long> platformConnections;
        HashSet<Long> stopConnections;
        long stopId;
        // Merge all connections of mergable platforms into the stop node connection
        for (long platformId: platformToStopMapping.keySet()){
            stopId = platformToStopMapping.get(platformId);

            platformConnections =  connectionLists.get(platformId);
            stopConnections = connectionLists.get(stopId);

            stopConnections.addAll(platformConnections);
            stopConnections.remove(platformId);
            stopConnections.remove(stopId);
            // Remove platform fromm the connection lists because its contained in its corresponding stop node
            mergedConnectionLists.remove(platformId);
        }
        return mergedConnectionLists;
    }


    /**
     * TODO: maybe merge with HaltMeger createPlatformToStopNodeMapping() method
     * Creates for every platform and node a list with all its connected platforms and stops.
     * @return connection lists
     */
    private HashMap<Long, HashSet<Long>> createConnectionLists(HashSet<Long> unmergedPlatformAndStopIds, HashSet<Relation> routeRelations){
        // For every stop and platform its connected stops and platforms
        HashMap<Long, HashSet<Long>> connectionLists = new HashMap<Long, HashSet<Long>>();
        // Initialize the connection list for every stop and platform
        for (long haltId: unmergedPlatformAndStopIds){
            connectionLists.put(haltId, new HashSet<Long>());
        }

        HashSet<Long> connectionListMember;
        HashSet<Long> connectionListLastMember;
        long memberId;
        long lastMemberId;


        for (Relation relation: routeRelations){
            RelationMember lastMember = null;

            for (RelationMember member: relation.getMembers()){
                memberId = member.getMemberId();
                if (lastMember != null){
                    lastMemberId = lastMember.getMemberId();

                    if (memberHelper.hasNoRole(member)) break;
                    else if (memberHelper.hasRolePlatform(member) || memberHelper.hasRoleStop(member)){
                        if (memberHelper.dataContains(member) && memberHelper.dataContains(lastMember)){
                            // Insert both halts in the connection list of the other halt
                            connectionListMember = connectionLists.get(memberId);
                            connectionListLastMember = connectionLists.get(lastMemberId);
                            connectionListMember.add(lastMemberId);
                            connectionListLastMember.add(memberId);
                        }
                    }
                }
                lastMember = member;
            }
        }

        return connectionLists;
    }
}
