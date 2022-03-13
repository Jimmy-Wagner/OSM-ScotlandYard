import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.HashMap;

/**
 * This class provides helper functions for Relation members which are only given by their id.
 */
public class RelationMemberHelper {
    private HashMap<Long, Node> allNodes;
    private OsmDataContainer dataContainer;

    public RelationMemberHelper(OsmDataContainer dataContainer){
        this.dataContainer = dataContainer;
        this.allNodes = dataContainer.getAllContainedNodes();
    }

    /**
     * Returns the full way for a way member if the way is contained in allways, otherwise retunrs null.
     * @param member
     * @return fullway for a waymember if its contained, otherwise null
     */
    public Way getFullWay(RelationMember member){
        long memberId = member.getMemberId();
        Way fullWay = dataContainer.getFullWayById(memberId);
        return fullWay;
    }

    /**
     * Returns the fullnode for a relation member.
     * Retrieves for ways and relations the first contained node in the bounding box.
     * @param member
     * @return fullNode
     */
    public Node getFullNode(RelationMember member){
        long memberId = member.getMemberId();
        EntityType memberType = member.getMemberType();

        if (memberType == EntityType.Node){
            return allNodes.get(memberId);
        }
        else if (memberType == EntityType.Way){
            return dataContainer.getFullNodeOfWayId(memberId);
        }
        else if (memberType == EntityType.Relation){
            return dataContainer.getFullNodeOfRelationId(memberId);
        }
        System.out.println("Haltmeger | getFullNode() Fehler, weder node, way, relation");
        return null;
    }


    /**
     * Checks if a given stop (node) or platform (node, way, relation) are contained in the bounding box
     * of the static map image.
     * @param member
     * @return is contained
     */
    public boolean dataContains(RelationMember member){
        EntityType memberType = member.getMemberType();
        long memberId = member.getMemberId();

        if (memberType == EntityType.Node){
            if (this.dataContainer.inContainedNodes(memberId)){
                return true;
            }
            else{
                return false;
            }
        }
        else if(memberType == EntityType.Way){
            if (this.dataContainer.inContainedWays(memberId)){
                return true;
            }
            else{
                return false;
            }
        }
        else if (memberType == EntityType.Relation){
            if (this.dataContainer.inContainedPlatformRelations(memberId)){
                return true;
            }
            else{
                return false;
            }
        }
        System.out.println("Fehler HaltMerger | dataContains()! Weeder Node noch way noch relation");
        return false;
    }


    /**
     * Checks if a relation member has no role.
     * @param member
     * @return has no role
     */
    public boolean hasNoRole(RelationMember member) {
        return member.getMemberRole().equalsIgnoreCase("");
    }


    /**
     * Checks if a relation member is a stop node.
     * @param member
     * @return is stop node
     */
    public boolean hasRoleStop(RelationMember member) {
        if (member.getMemberRole().equalsIgnoreCase("stop") ||
                member.getMemberRole().equalsIgnoreCase("stop_entry_only") ||
                member.getMemberRole().equalsIgnoreCase("stop_exit_only")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a relation member is a platform
     * @param member
     * @return is platform
     */
    public boolean hasRolePlatform(RelationMember member) {
        if (member.getMemberRole().equalsIgnoreCase("platform") ||
                member.getMemberRole().equalsIgnoreCase("platform_entry_only") ||
                member.getMemberRole().equalsIgnoreCase("platform_exit_only")) {
            return true;
        }
        return false;
    }
}
