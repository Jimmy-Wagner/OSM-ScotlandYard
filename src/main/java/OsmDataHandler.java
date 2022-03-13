
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class takes the data from the {@link OsmDataContainer} object and prepares this
 * data for drawing to the map.
 */
public class OsmDataHandler {

    private OsmDataContainer dataContainer;
    private RelationMemberHelper helper;
    private HaltMerger haltMerger;
    private WayRetriever wayRetriever;
    private HashMap<Long, Node> allNodes;
    private ImageData imageData;
    private WayManipulator wayManipulator;


    public OsmDataHandler(OsmDataContainer dataContainer, ImageData imageData) {
        this.dataContainer = dataContainer;
        this.allNodes = dataContainer.getAllContainedNodes();
        this.helper = new RelationMemberHelper(dataContainer);
        this.haltMerger = new HaltMerger(this.helper);
        this.imageData = imageData;
        this.wayRetriever = new WayRetriever(this.dataContainer, this.helper, this, imageData.getBoundingBox());
        this.wayManipulator = new WayManipulator();
    }

    /**
     * Returns all stopnodes and platforms of the given relation type. If possible the stopnodes and platforms are merged into the stopnode.
     *
     * @param routeType
     * @return
     */
    public HashSet<Node> getMergedHalts(RouteType routeType) {
        return getMergedHalts(getRelationsByType(routeType));
    }
    /**
     * Get all stop nodes and all platform nodes that have no corresponding stop node.
     * The platforms that have a corresponding stop node are merged into that stopnode.
     *
     * @param routeRelations
     * @return
     */
    public HashSet<Node> getMergedHalts(HashSet<Relation> routeRelations) {
        // Ids of all stops and platforms (node / platform ids)
        HashSet<Long> unmergedIds = extractAllContainedStopAndPlatformIDs(routeRelations);
        // Merge platforms into stops
        HashSet<Long> mergedIds = haltMerger.getMergedHalts(unmergedIds, routeRelations);
        HashSet<Node> mergedNodes = getFullNodesForHalts(mergedIds);

        return mergedNodes;
    }

    /**
     * Returns all ids of platforms (nodes, ways, relations) and stops (node) which are contained in the bounding box of the map image.
     *
     * @param routeRelations
     * @return contained platform and stop ids
     */
    private HashSet<Long> extractAllContainedStopAndPlatformIDs(HashSet<Relation> routeRelations) {
        HashSet<Long> containedStopAndPlatformIDs = new HashSet<Long>();
        for (Relation relation : routeRelations) {
            for (RelationMember member : relation.getMembers()) {
                if (helper.hasNoRole(member)) break;
                else if (helper.hasRoleStop(member) || helper.hasRolePlatform(member)) {
                    if (helper.dataContains(member)) {
                        containedStopAndPlatformIDs.add(member.getMemberId());
                    }
                }
            }
        }
        return containedStopAndPlatformIDs;
    }


    /**
     * Returns a list of fullnodes for a given list of ids of stops or platforms.
     * Note that a platform can be a relation or a node.
     *
     * @param ids
     * @return list of full nodes
     */
    public HashSet<Node> getFullNodesForHalts(HashSet<Long> ids) {
        Node fullNode;
        HashSet<Node> fullNodes = new HashSet<Node>();
        for (long id : ids) {
            fullNode = dataContainer.getFullNodeByid(id);
            fullNodes.add(fullNode);
        }
        return fullNodes;
    }


    /**
     * Returns all intersections of contained ways for routes of the given type.
     * @param type
     * @return
     */
    public ArrayList<Point> getIntersectionsGeo(RouteType type){
        ArrayList<TrimmedWay> trimmedWays = getTrimmedWays(type);
        ArrayList<Point> intersections = this.wayRetriever.getWayIntersectionsGeo(trimmedWays);
        System.out.println(intersections.size() + " intersections");
        return intersections;
    }

    /**
     * Returns all intersections of contained ways for routes of the given type.
     * @param type
     * @return
     */
    public ArrayList<Point> getIntersectionsPixel(RouteType type){
        ArrayList<TrimmedWay> trimmedWays = getTrimmedWays(type);
        ArrayList<Point> intersections = this.wayRetriever.getWayIntersectionsPixel(trimmedWays);
        System.out.println(intersections.size() + " intersections");
        return intersections;
    }


    /**
     * Returns a list of fullnodes for a given list of ids of nodes.
     *
     * @param ids
     * @return list of full nodes
     */
    public ArrayList<Node> getFullNodesForIds(ArrayList<Long> ids) {
        ArrayList<Node> fullNodes = new ArrayList<Node>();
        for (long id : ids) {
            fullNodes.add(allNodes.get(id));
        }
        return fullNodes;
    }




    /**
     * Returns for every relation of the given type its unconnected ways.
     * Be careful! TrimmedWay can be null for a relation that has no ways in the bounding box! It can
     * happen that a relation has only one stop in the bounding box and therefore no way.
     *
     * @param routeType
     * @return list of unconnected ways for every relation. (Can be null for some relations!)
     */
    public ArrayList<TrimmedWay> getTrimmedWays(RouteType routeType) {
        ArrayList<TrimmedWay> trimmedWays = this.wayRetriever.getTrimmedWays(getRelationsByType(routeType));
        initializeTrimmedWaysPixels(trimmedWays);
        return trimmedWays;
    }


    public ArrayList<TrimmedWay> getStrokes(RouteType routeType){
        // All ways inside the bounding box of the map with their node ids
        ArrayList<TrimmedWay> trimmedWays = this.wayRetriever.getTrimmedWays(getRelationsByType(routeType));
        // Create strokes for every way and then combine all strokes
        initializeTrimmedWaysPixels(trimmedWays);


        return trimmedWays;
    }



    /**
     * Apply douglas peucker algorithm on all given ways
     * @param routeType
     * @return
     */
    public ArrayList<ReducedWay> getDouglasPeuckerWays(RouteType routeType){
        // First retrieve the trimmedways with only containing their waynode ids
        ArrayList<TrimmedWay> trimmedWays = this.wayRetriever.getTrimmedWays(getRelationsByType(routeType));
        // Initialize the trimmed ways with the pixel values for all its contained nodes
        initializeTrimmedWaysPixels(trimmedWays);
        // Apply simplifying and merging algorithms on the ways
        ArrayList<ReducedWay> reducedWays = this.wayManipulator.douglasPeuckerAlgo(trimmedWays, 5);
        return reducedWays;
    }


    /**
     * Initializes the pixel values when drawn to the static map image for all nodes in the trimmedways.
     * @param trimmedWays
     */
    private void initializeTrimmedWaysPixels(ArrayList<TrimmedWay> trimmedWays){
        ArrayList<Node> fullNodes;
        ArrayList<PixelNode> pixelNodes;
        for (TrimmedWay way: trimmedWays){
            fullNodes = getFullNodesForIds(way.getWaynodes());
            pixelNodes = calculatePixelNodes(fullNodes);
            // Intialize the pixel node list of the trimmedway
            way.initializePixelNodes(pixelNodes);
        }
    }

    /**
     * Converts a list of fullnodes to a list of pixel nodes which contain only the information about the pixel value of the node when drawn to the map.
     * @param fullNodes
     * @return
     */
    private ArrayList<PixelNode> calculatePixelNodes (ArrayList<Node> fullNodes){
        ArrayList<PixelNode> pixelNodes = new ArrayList<PixelNode>();
        PixelNode currentPixelNode;
        for (Node node: fullNodes) {
            currentPixelNode = convertGeoNodeToPixelNode(node);
            pixelNodes.add(currentPixelNode);
        }
        return pixelNodes;
    }

    /**
     * Converts a fullnode to a pixel node which contains only the information about the pixel position of the node when drawn to the static map image.
     * @param node
     * @return
     */
    private PixelNode convertGeoNodeToPixelNode( Node node){
        int[] pixelValues =  DrawToGraphics.convertGeoToPixel(node.getLatitude(), node.getLongitude(), this.imageData.getPIXELWIDTH(), this.imageData.getPIXELHEIGHT(),
                this.imageData.getBoundingBox());
        return new PixelNode(pixelValues[0], pixelValues[1]);
    }


    /**
     * Returns all relations for a selected type of public transport relations.
     *
     * @param routeType
     * @return all relations of selected type
     */
    public HashSet<Relation> getRelationsByType(RouteType routeType) {
        switch (routeType) {
            case BUS:
                return this.dataContainer.getBusRouteRelations();
            case TRAIN:
                return this.dataContainer.getTrainRouteRelations();
            case SUBWAY:
                return this.dataContainer.getSubwayRouteRelations();
            case TRAM:
                return this.dataContainer.getTramRouteRelations();
            case MONORAIL:
                return this.dataContainer.getMonorailRouteRelations();
            case LIGHTRAIL:
                return this.dataContainer.getLightrailRouteRelations();
            default:
                return null;
        }
    }
}
