import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.*;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import crosby.binary.osmosis.OsmosisReader;

/**
 * Receives data from the Osmosis pipeline as pbf file and stores Data in {@link OsmDataHandler} class.
 * The Data that is being read should be reduced beforehand to only public transport relations (including the nodes and ways
 * that are used in the public transport relations) for performance reasons.
 * One possibility to reduce a given pbf file to only public transport routes is with Osmosis, a command line Java application for processing OSM data.
 */
public class OSM_Pbf_Reader implements Sink {

    // Member nodes in relations are only saved with their id
    // The id is then used to retrieve all informations about the member node
    private HashMap<Long, Node> allNodes = new HashMap<Long, Node>();
    // Contains all ways with their node members and tags
    // Necessary because relations with ways as members contain only their id and not the node member of the way
    private HashMap<Long, Way> allWays = new HashMap<Long, Way>();
    // All route relations
    private ArrayList<Relation> busRouteRelations = new ArrayList<Relation>();
    private ArrayList<Relation> trolleybusRouteRelations = new ArrayList<Relation>();
    private ArrayList<Relation> trainRouteRelations = new ArrayList<Relation>();
    private ArrayList<Relation> tramRouteRelations = new ArrayList<Relation>();
    private ArrayList<Relation> subwayRouteRelations = new ArrayList<Relation>();
    private ArrayList<Relation> monorailRouteRelations = new ArrayList<Relation>();
    private ArrayList<Relation> lightRailRouteRelations = new ArrayList<Relation>();
    // Necessary because some platforms are mapped as relations (currently not in use because osm data is premanipulated with osmosis)
    private HashMap<Long, Relation> platformRelations = new HashMap<Long, Relation>();

    @Override
    public void initialize(Map<String, Object> arg0) {
    }

    @Override
    public void process(EntityContainer entityContainer) {
        if (entityContainer instanceof NodeContainer) {
            // Save all nodes
            Node currentNode = ((NodeContainer) entityContainer).getEntity();
            allNodes.put(currentNode.getId(), currentNode);
        } else if (entityContainer instanceof WayContainer) {
            // Save all ways
            Way currentWay = ((WayContainer) entityContainer).getEntity();
            allWays.put(currentWay.getId(), currentWay);
        } else if (entityContainer instanceof RelationContainer) {
            Relation currentRelation = ((RelationContainer) entityContainer).getEntity();
            organizeRelations(currentRelation);
        }
        //FIXME: Save bounds and check if chosen center of map with zoomlevel is inside this bounds
        else if (entityContainer instanceof BoundContainer) {
            Bound bound = ((BoundContainer) entityContainer).getEntity();
        } else {
            System.out.println("Reading the pbf file finished!");
        }
    }

    /**
     * Puts the given relation in the right List of relations (bus-, train-, tram-, trolleybus-routes) based on its tags.
     *
     * @param relation
     */
    private void organizeRelations(Relation relation) {

        for (Tag currrentTag : relation.getTags()) {
            // When premanipulated with osmosis command line application, pbf file should already only contain route relations
            if (currrentTag.getKey().equalsIgnoreCase("route")) {
                // route=bus
                if (currrentTag.getValue().equalsIgnoreCase("bus")) {
                    busRouteRelations.add(relation);
                }
                // route=trolleybus
                else if (currrentTag.getValue().equalsIgnoreCase("trolleybus")) {
                    trolleybusRouteRelations.add(relation);
                }
                // route=train
                else if (currrentTag.getValue().equalsIgnoreCase("train")) {
                    trainRouteRelations.add(relation);
                }
                // route=tram
                else if (currrentTag.getValue().equalsIgnoreCase("tram")) {
                    tramRouteRelations.add(relation);
                } else if (currrentTag.getValue().equalsIgnoreCase("subway")) {
                    subwayRouteRelations.add(relation);
                } else if (currrentTag.getValue().equalsIgnoreCase("light_rail")) {
                    lightRailRouteRelations.add(relation);
                } else if (currrentTag.getValue().equalsIgnoreCase("monorail")) {
                    monorailRouteRelations.add(relation);
                }
                // route is not of public transport type (which should not occur when data is manipulated before reading with this class)
                else {
                }
            } else if (currrentTag.getKey().equalsIgnoreCase("platform")) {
                platformRelations.put(relation.getId(), relation);
            } else {
                //Something went wrong because after osmosis premanipulation there should only be pulbic transport route relations and platform relations for stops
            }
        }
    }

    @Override
    public void complete() {
    }

    @Override
    public void close() {
    }


    /**
     * This method initiates the reading of the given pbf file
     *
     * @return dataHandler the object of the Data class
     */
    public OsmDataHandler readOsmPbfFile() {
        //FIXME: Take path to string as input parameter
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("/Users/jimmy/Desktop/london/publictransportRoutesAllInclusive.osm.pbf");
        } catch (FileNotFoundException e) {
            System.out.println("Could not find that file!");
            e.printStackTrace();
            return null;
        }
        OsmosisReader reader = new OsmosisReader(inputStream);
        reader.setSink(this);
        System.out.println("Reading the file");
        reader.run();
        // Adde alle szugähnlichen routes zu trainroutes
        tramRouteRelations.addAll(monorailRouteRelations);
        System.out.println("IN OSM_Pbf_reader:");
        System.out.println("allNodes size: " + allNodes.size());
        System.out.println("busRouteRelations size: " + busRouteRelations.size());
        return new OsmDataHandler(allNodes, allWays, busRouteRelations,
                trolleybusRouteRelations, trainRouteRelations, tramRouteRelations,
                subwayRouteRelations, monorailRouteRelations, lightRailRouteRelations, platformRelations);
    }
}
