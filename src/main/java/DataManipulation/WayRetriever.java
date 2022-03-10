package DataManipulation;

import DataContainer.OsmDataContainer;
import DataContainer.RelationMemberHelper;
import DataManipulation.RouteWays.Bentley_Ottman_Algorithmn.BentleyOttmann;
import DataManipulation.RouteWays.Bentley_Ottman_Algorithmn.Point;
import DataManipulation.RouteWays.Bentley_Ottman_Algorithmn.Segment;
import Draw.DrawToGraphics;
import Types.TrimmedWay;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Retrieving all contained ways for set of relations.
 */
public class WayRetriever {

    private OsmDataContainer dataContainer;
    private RelationMemberHelper helper;
    private OsmDataHandler dataHandler;
    private Bound boundingbox;
    // Contains all ways that are already saved in the trimmedways arraylist
    private HashSet<Long> addedWays = new HashSet<>();
    private HashSet<Way> addedFullWays = new HashSet<>();

    public WayRetriever(OsmDataContainer dataContainer, RelationMemberHelper helper, OsmDataHandler dataHandler, Bound bound) {
        this.dataContainer = dataContainer;
        this.helper = helper;
        this.dataHandler = dataHandler;
        this.boundingbox = bound;
    }


    /**
     * Returns for all given relations its unconnected ways.
     *
     * @param routeRelations
     * @return unconnected ways for every relation
     */
    public ArrayList<TrimmedWay> getTrimmedWays(HashSet<Relation> routeRelations) {
        ArrayList<TrimmedWay> trimmedWays = new ArrayList<TrimmedWay>();
        ArrayList<TrimmedWay> trimmedWaysOneRelation;

        for (Relation relation : routeRelations) {
            // Save all unconnected ways for the current relation in the map if there are ways
            trimmedWaysOneRelation = getTrimmedWays(relation);
            trimmedWays.addAll(trimmedWaysOneRelation);
        }
        addedWays.clear();
        return trimmedWays;
    }

    /**
     * Returns a List of all unconnected ways of this relation. Ways that are connected result in one trimmed way.
     *
     * @param relation
     * @return unconnected ways of the relation
     */
    private ArrayList<TrimmedWay> getTrimmedWays(Relation relation) {
        ArrayList<TrimmedWay> trimmedWays = new ArrayList<TrimmedWay>();
        Way fullWay;


        for (RelationMember member : relation.getMembers()) {
            // Check if it has no role because some platforms are ways and you dont want to draw them in the routes
            if (member.getMemberType() == EntityType.Way && helper.hasNoRole(member)) {
                // Way is contained in data
                if (helper.dataContains(member)) {
                    fullWay = helper.getFullWay(member);
                    if (!addedWays.contains(member.getMemberId())) {
                        // Probably only one trimmedway is added but also possible that there are mutliple in one relation
                        ArrayList<TrimmedWay> wayT = getTrimmedWays(fullWay);
                        trimmedWays.addAll(wayT);
                        // save the current way as already added as a trimmed way
                        // Otherwise the same way when contained in different routes is added multiple times as a trimmedway
                        addedWays.add(member.getMemberId());
                    }
                }
            }
        }
        //Merge connected trimmedWays into one trimmedway

        //FIXME: Important for rail routes but bad for bus
        trimmedWays = mergeConnected(trimmedWays);

        return trimmedWays;
    }


    /**
     * Ways over relations are merged when the same way occurs in mutltiple relations.
     *
     * @return
     */
    public ArrayList<TrimmedWay> getMergedTrimmedWays(HashSet<Relation> relations) {
        HashSet<Long> containedWayIds = getContainedWays(relations);
        ArrayList<TrimmedWay> trimmedWays = new ArrayList<>();
        Way fullWay;
        for (long wayId : containedWayIds) {
            fullWay = dataContainer.getFullWayById(wayId);
            trimmedWays.addAll(getTrimmedWays(fullWay));
        }
        return trimmedWays;
    }

    public HashSet<Long> getContainedWays(HashSet<Relation> relations) {
        HashSet<Long> containedWays = new HashSet<>();
        for (Relation relation : relations) {
            containedWays.addAll(getContainedWays(relation));
        }
        return containedWays;
    }

    public HashSet<Long> getContainedWays(Relation relation) {
        HashSet<Long> containedWays = new HashSet<>();
        for (RelationMember member : relation.getMembers()) {
            if (member.getMemberType() == EntityType.Way && helper.hasNoRole(member)) {
                if (helper.dataContains(member)) {
                    containedWays.add(member.getMemberId());
                }
            }
        }
        return containedWays;
    }

    /**
     * Splits a way into several ways that are connected in the bounding box of the image.
     * One way can for example go out of the image and return back to the image on another point.
     * If thats the case the way is split into two trimmedways because they are not connected in the bounding box.
     *
     * @param way
     * @return list of trimmed ways
     */
    private ArrayList<TrimmedWay> getTrimmedWays(Way way) {
        ArrayList<TrimmedWay> trimmedWays = new ArrayList<TrimmedWay>();
        TrimmedWay currentTrimmedWay = new TrimmedWay();
        long wayNodeId;
        for (WayNode wayNode : way.getWayNodes()) {
            wayNodeId = wayNode.getNodeId();
            // The waynode is contained in the data
            if (dataContainer.inContainedNodes(wayNodeId)) {
                currentTrimmedWay.addNode(wayNodeId);
            }
            // The current node is not contained in the data so make a cut
            else {
                // If there are more than one node to the way (otherwise it would be no way)
                if (currentTrimmedWay.getWaySize() > 1) {
                    trimmedWays.add(currentTrimmedWay);
                }
                // Make a cut for this way because it is not connected to the following ways of this relation because the
                // ways of the relation go out the bounding box of the map image.
                currentTrimmedWay = new TrimmedWay();
            }
        }
        if (currentTrimmedWay.getWaySize() > 1) {
            trimmedWays.add(currentTrimmedWay);
        }
        if (trimmedWays.contains(null)) {
            System.out.println("Error");
        }
        return trimmedWays;
    }


    /**
     * Merges the trimmed ways that are connected. To be connected the last node of the first trimmed way has to be identical
     * to the first node of the second trimmed way.
     *
     * @param trimmedWays
     * @return merged trimmed ways
     */
    private ArrayList<TrimmedWay> mergeConnected(ArrayList<TrimmedWay> trimmedWays) {
        ArrayList<TrimmedWay> mergedTrimmedWays = new ArrayList<TrimmedWay>();
        TrimmedWay lastWay = null;
        boolean wasMerged;

        for (TrimmedWay trimmedWay : trimmedWays) {
            if (lastWay != null) {
                //try to merge the current trimmed way into the last trimmed way
                wasMerged = lastWay.mergeInto(trimmedWay);
                // If the current way cannot be merged into the lastway change the lastWay to the current way, otherwise keep the lastway
                if (!wasMerged) {
                    mergedTrimmedWays.add(lastWay);
                    lastWay = trimmedWay;
                }
            } else {
                lastWay = trimmedWay;
            }
        }
        // Add the lastway only if there is one
        if (lastWay != null) {
            mergedTrimmedWays.add(lastWay);
        }

        return mergedTrimmedWays;
    }


    /**
     * Calculates for a list of lines every intersection
     * Lines which have the same node do not intersect.
     *
     * @param trimmedWays
     * @return
     */
    public ArrayList<Point> getWayIntersectionsGeo(ArrayList<TrimmedWay> trimmedWays) {
        // All fullnodes for the current way
        ArrayList<Node> wayNodes;
        // All segments for all ways. Every two connected nodes are one segment.
        ArrayList<Segment> data = new ArrayList<>();

        for (TrimmedWay trimmedWay : trimmedWays) {
            wayNodes = this.dataHandler.getFullNodesForIds(trimmedWay.getWaynodes());
            Node lastNode = null;
            for (Node wayNode : wayNodes) {
                if (lastNode != null) {
                    Point p1 = new Point(lastNode.getLongitude(), lastNode.getLatitude());
                    Point p2 = new Point(wayNode.getLongitude(), wayNode.getLatitude());
                    data.add(new Segment(p1, p2));
                }
                lastNode = wayNode;
            }
        }

        ArrayList<Point> intersections = calculateIntersections(data);
        return intersections;
    }

    /**
     * Calculates for a list of lines every intersection
     * Lines which have the same node do not intersect.
     *
     * @param trimmedWays
     * @return
     */
    public ArrayList<Point> getWayIntersectionsPixel(ArrayList<TrimmedWay> trimmedWays) {
        // All fullnodes for the current way
        ArrayList<Node> wayNodes;
        // All segments for all ways. Every two connected nodes are one segment.
        ArrayList<Segment> data = new ArrayList<>();
        int[] pixelXY;

        for (TrimmedWay trimmedWay : trimmedWays) {
            wayNodes = this.dataHandler.getFullNodesForIds(trimmedWay.getWaynodes());
            Node lastNode = null;
            for (Node wayNode : wayNodes) {
                if (lastNode != null) {
                    pixelXY = DrawToGraphics.convertGeoToPixel(lastNode.getLatitude(), lastNode.getLongitude(), 1200, 1200, boundingbox);
                    Point p1 = new Point(pixelXY[0], pixelXY[1]);
                    pixelXY = DrawToGraphics.convertGeoToPixel(wayNode.getLatitude(), wayNode.getLongitude(), 1200, 1200, boundingbox);
                    Point p2 = new Point(pixelXY[0], pixelXY[1]);
                    data.add(new Segment(p1, p2));
                }
                lastNode = wayNode;
            }
        }

        //checkWhatIntersected(data, mapping);

        ArrayList<Point> intersections = calculateIntersections(data);
        return intersections;
    }

    /**
     * Calculates for a list of lines every intersection.
     * Lines which have the same node do not intersect.
     *
     * @param lines
     * @return
     */
    private ArrayList<Point> calculateIntersections(ArrayList<Segment> lines) {
        BentleyOttmann ottman = new BentleyOttmann(lines);
        ottman.find_intersections();
        return ottman.get_intersections();
    }
}

