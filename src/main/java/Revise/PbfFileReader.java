package Revise;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.*;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import crosby.binary.osmosis.OsmosisReader;

/**
 * Receives data from the Osmosis pipeline and prints ways which have the
 * 'highway key.
 *
 * @author pa5cal
 */
public class PbfFileReader implements Sink {

    // Bounding box of the retrieved map image
    public Bound imageBoundingbox;
    // The bounding box that is covered by the given osm data
    public Bound osmDataBoundingBox;
    // All nodes that are contained in the bounding box of the static map image
    // Use ID as key for quicker lookup if a node is contained
    public HashMap<Long, Node> allContainedNodes = new HashMap<Long, Node>();
    // all ways that are within the bounds of the map image
    public HashMap<Long, Way> allContainedWays = new HashMap<Long, Way>();
    // route=bus,trolleybus relations
    public HashSet<Relation> busRouteRelations = new HashSet<Relation>();
    // route=train relations
    private HashSet<Relation> trainRouteRelations = new HashSet<Relation>();
    // route=subway relations
    private HashSet<Relation> subwayRouteRelations = new HashSet<Relation>();
    // route=tram relations
    private HashSet<Relation> tramRouteRelations = new HashSet<Relation>();
    // route=light_rail relations
    private HashSet<Relation> lightrailRouteRelations = new HashSet<Relation>();
    // route=monorail relations
    private HashSet<Relation> monorailRouteRelations = new HashSet<Relation>();
    // platform=* relations
    public HashSet<Relation> platformRelations = new HashSet<Relation>();


    public PbfFileReader(Bound imageBoundingbox){
        this.imageBoundingbox = imageBoundingbox;
    }


    @Override
    public void initialize(Map<String, Object> arg0) {
    }

    @Override
    public void process(EntityContainer entityContainer) {
        // Add only contained nodes
        if (entityContainer instanceof NodeContainer) {
            Node node = ((NodeContainer) entityContainer).getEntity();
            addContainedNode(node);
        }
        // Add only contained ways
        else if (entityContainer instanceof WayContainer) {
            Way way = ((WayContainer) entityContainer).getEntity();
            addContainedWay(way);
        }
        // Add all relations
        else if (entityContainer instanceof RelationContainer) {
            Relation relation = ((RelationContainer) entityContainer).getEntity();
            addRelation(relation);
        }
        // Boundingbox of osm data is normally at beginning of the pbf file
        else if (entityContainer instanceof BoundContainer) {
            osmDataBoundingBox = ((BoundContainer) entityContainer).getEntity();
        }
    }

    @Override
    public void complete() {
    }

    @Override
    public void close() {
    }

    /**
     * This methods adds the given relation to the right set of relations.
     * When the data is premanipulated with osmosis as it should then there are only following types of relations:
     * 1. public transport route relations (route=bus,train,subway,trolleybus,light_rail,monorail,tram)
     * 2. platform route relations (platform=*)
     *
     * @param relation
     */
    public void addRelation(Relation relation) {
        for (Tag currrentTag : relation.getTags()) {
            if (currrentTag.getKey().equalsIgnoreCase("route")) {
                String value = currrentTag.getValue();
                // route=bus | route=trolleybus
                if (value.equalsIgnoreCase("bus") ||
                        value.equalsIgnoreCase("trolleybus")) {
                    busRouteRelations.add(relation);
                }
                // route=train
                else if (value.equalsIgnoreCase("train")) {
                    trainRouteRelations.add(relation);
                }
                // route=tram
                else if (value.equalsIgnoreCase("tram")) {
                    tramRouteRelations.add(relation);
                } else if (value.equalsIgnoreCase("subway")) {
                    subwayRouteRelations.add(relation);
                } else if (value.equalsIgnoreCase("light_rail")) {
                    lightrailRouteRelations.add(relation);
                } else if (value.equalsIgnoreCase("monorail")) {
                    monorailRouteRelations.add(relation);
                }
                break;
            }
            else if (currrentTag.getKey().equalsIgnoreCase("platform")) {
                platformRelations.add(relation);
                break;
            }
        }
    }


    /**
     * Adds the given way to all ways.
     *
     * @param way
     */
    private void addContainedWay(Way way) {
        for (WayNode wayNode: way.getWayNodes()){
            if (isInAllContainedNodes(wayNode.getNodeId())){
                allContainedWays.put(way.getId(), way);
                return;
            }
        }
    }

    /**
     * Checks if a node given by its id is contained in allContainedNodes
     * @param nodeId
     * @return contained
     */
    public boolean isInAllContainedNodes(Long nodeId){
        if (allContainedNodes.get(nodeId) != null){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Checks if a node is contained in a given boundingbox and adds the node
     * to allContainedNodes.
     *
     * @param node
     */
    private void addContainedNode(Node node) {
        if (nodeIsContained(node, this.imageBoundingbox)) {
            allContainedNodes.put(node.getId(), node);
        }
    }

    /**
     * Checks if a given node is contained in a given boundingbox
     *
     * @param node
     * @param boundingbox
     * @return is Contained
     */
    private boolean nodeIsContained(Node node, Bound boundingbox) {
        if (node.getLatitude() >= boundingbox.getBottom() &&
                node.getLatitude() <= boundingbox.getTop() &&
                node.getLongitude() >= boundingbox.getLeft() &&
                node.getLongitude() <= boundingbox.getRight()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Initiates reading of the given pbf file.
     *
     * @param pathToFile
     * @return osmData which capsulates the data contained in the pbf file
     */
    public OsmDataContainer readFile(String pathToFile) {

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(pathToFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Could not find that file!");
        }
        OsmosisReader reader = new OsmosisReader(inputStream);
        reader.setSink(this);
        reader.run();

        return new OsmDataContainer(imageBoundingbox,
                osmDataBoundingBox,
                allContainedNodes,
                allContainedWays,
                busRouteRelations,
                trainRouteRelations,
                subwayRouteRelations,
                tramRouteRelations,
                lightrailRouteRelations,
                monorailRouteRelations,
                platformRelations);
    }
}