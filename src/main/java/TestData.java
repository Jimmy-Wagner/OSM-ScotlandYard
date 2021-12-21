import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TestData {
    private ArrayList<Relation> relations;
    private ArrayList<Node> stops;
    private HashMap<Long, Long> platformToStopNodeMapping;
    private ArrayList<Node> allNodes;

    public TestData(){
        allNodes = initalizeNodes(1, 10);
        relations = initializeRelations(2);
        stops = initializeStops();
        platformToStopNodeMapping = initializePlatFormToStopNodeMapping();
    }

    public Node createNode(long id, double lat, double lon){
        CommonEntityData data  = new CommonEntityData(id , 0, new Date(), new OsmUser(0, "Jimmy"), 100);
        return new Node(data, lat, lon);
    }

    public RelationMember createRelationMember(long id, String role, EntityType type){
        RelationMember member = new RelationMember(id, type, role);
        return member;
    }

    public Relation createRelation(ArrayList<RelationMember> members, long id){
        CommonEntityData data = new CommonEntityData (id, 0, new Date(), new OsmUser(0, "Jimmy"), 100, (List)members);
        Relation relation = new Relation(data, members);
        return relation;
    }


    public void printData(){
        System.out.println("Nodes:");
        int number = 1;
        for (Node node:allNodes){
            System.out.println("---------------------");
            System.out.println("Node " + number++);
            System.out.println("Id: " + node.getId());
            System.out.println("Lat: " + node.getLatitude() + " | Lon: " + node.getLongitude());
        }
        number = 1;
        System.out.println("------------------------");
        System.out.println("------------------------");
        for (Relation relation: relations){
            System.out.println("++++++++++++++++++++");
            System.out.println("Relation: " + relation.getId());
            System.out.println("Members:");
            for (RelationMember member: relation.getMembers()){
                System.out.println("Member: " + number++);
                System.out.println("Memberrole: " + member.getMemberRole());
                System.out.println("membertype: " +member.getMemberType());
                System.out.println("-------------------");
            }
        }
    }


    private  ArrayList<Node> initalizeNodes(long startId, int number) {
        // public CommonEntityData(long id, int version, Date timestamp, OsmUser user, long changesetId)
        ArrayList<Node> nodes = new ArrayList<Node>();
        CommonEntityData data;
        Node newNode;
        for (int i = (int) startId; i<=startId+number; i++){
            data = new CommonEntityData(i, 0, new Date(), new OsmUser(0, "Jimmy"), 100);
            newNode = new Node(data, i, i);
            nodes.add(newNode);
        }
        // (Id: 100, lat:100, lon:100), (Id:101, lat:101, lon:101)
        return nodes;
    }


    private ArrayList<Relation> initializeRelations(int number) {
        ArrayList<Relation> relations = new ArrayList<Relation>();
        int startIdMember = 1;
        for (int i=1000; i<=1000+number; i++){
            Relation relation = initializeRelation(i, startIdMember++, 4);
            relations.add(relation);
        }
        return relations;
    }

    private Relation initializeRelation(long id, long startIdMember, int numberMembers) {
        ArrayList<RelationMember> members = initializeRelationMembers(numberMembers, EntityType.Node, "stop", startIdMember);
        CommonEntityData data;
        data = new CommonEntityData (id, 0, new Date(), new OsmUser(0, "Jimmy"), 100, (List)members);
        Relation relation = new Relation(data, members);
        return relation;
    }


    private ArrayList<RelationMember> initializeRelationMembers(int number, EntityType type, String role, long startIDMember){
        RelationMember member;
        ArrayList<RelationMember> allMembers = new ArrayList<RelationMember>();

        for (long i=startIDMember; i<=number+startIDMember; i++){
            member = new RelationMember(i, type, role);
            allMembers.add(member);
        }

        return allMembers;
    }

    private ArrayList<Node> initializeStops() {
        return null;
    }

    private HashMap<Long, Long> initializePlatFormToStopNodeMapping() {
        return null;
    }
}
