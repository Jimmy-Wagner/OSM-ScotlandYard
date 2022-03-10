package DataManipulation;

import DataManipulation.BO.Container;
import DataManipulation.BO.IntersectionFinder;
import Types.*;
import bentleyottmann.ISegment;
import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.algorithm.distance.DiscreteFrechetDistance;
import org.locationtech.jts.algorithm.match.FrechetSimilarityMeasure;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.*;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.SeriesReducer;

import java.util.*;

/**
 * Provides functionality to manipulate single ways. Way overreaching functionality like merging is performed in {@link Network}.
 */
public class WayManipulator {

    // First find all intersections
    public IntersectionFinder finder = new IntersectionFinder();

    public ArrayList<NetworkWay> manipulateWays(ArrayList<TrimmedWay> trimmedWays, RouteType routeType) {

        // First reduce way with douglas peucker algorithm
        ArrayList<ReducedWay> simplifiedWays = douglasPeuckerAlgo(trimmedWays, 5);
        // Sort simplififedways by their length that the longest ways are inserted in the beginning to the map
        Collections.sort(simplifiedWays);

        ArrayList<ReducedWay> insertedWays = incrementallyMergeWays(simplifiedWays, 0.73, 70, 10000, 60);


        /*int realNumberIntersec = 0;

        for (int i = 0; i<insertedWays.size()-1; i++){
            for (int j = i+1; j<insertedWays.size(); j++){

                Geometry intersec = insertedWays.get(i).getLineString().intersection(insertedWays.get(j).getLineString());

                int length = intersec.getCoordinates().length;

                realNumberIntersec += length;
            }
        }
*/

        // Transform reduced way type to network type because reducedway is too stacked with functions
        ArrayList<NetworkWay> networkways = reducedWaysToNetworkWays(insertedWays);

        int segNumBefore = getAllSegments(networkways).size();

        // Split segments of the ways at intersections
        splitSegmentsAtIntersections(networkways);


        int intersecNumber = Container.intersections.size();

        int segNumAfter = getAllSegments(networkways).size();


        // Split the ways where there segments were splitted
        ArrayList<NetworkWay> splittedWays = splitWaysAtIntersections(networkways);

        // Delete very small ways that can occur when multiple intersections are close (3 lines cross close each other)
        //removeSmallWays(splittedWays);

        // Connect endpoints that are very close to the network to the network
        //mergeDanglingEnds(splittedWays);

        // Remove unnecessary ways

        return splittedWays;
    }

    public void connectDanglingEndpointsToNetwork(ArrayList<NetworkWay> network) {

    }


    public void removeSmallWays(ArrayList<NetworkWay> network) {
        ArrayList<NetworkWay> allWays = new ArrayList<>(network);
        for (NetworkWay way : allWays) {
            double length = way.getLength();
            if (length < 20) {
                network.remove(way);
            }
        }
    }

    public ArrayList<NetworkWay> splitWaysAtIntersections(ArrayList<NetworkWay> network) {

        // The splits of all major ways
        ArrayList<NetworkWay> splittedNetwork = new ArrayList<>();

        for (NetworkWay unsplitted : network) {
            ArrayList<NetworkWay> splits = unsplitted.getSplits();
            splittedNetwork.addAll(splits);
        }

        return splittedNetwork;

    }

    public void splitSegmentsAtIntersections(ArrayList<NetworkWay> network) {
        // the listener of the finder splits the ways of the network every time an intersection occured
        finder.findIntersectionsNetwork(network);

    }

    public List<ISegment> getAllSegments(ArrayList<NetworkWay> ways) {
        List<ISegment> segments = new ArrayList<>();
        for (NetworkWay way : ways) {
            segments.addAll(way.getSegments());
        }
        return segments;
    }

    public void mergeDanglingEnds(ArrayList<NetworkWay> network) {
        // Try to merge ends first
        mergeWayEnds(network);
        // If ends were not merged to another end try to merge on segment
        mergeEndsToSegments(network);
    }

    public void mergeEndsToSegments(ArrayList<NetworkWay> network) {
        ArrayList<SegmentBO> allSegments = getNetworkSegments(network);
        ArrayList<SegmentBO> otherSegments;

        for (NetworkWay current : network) {

            otherSegments = new ArrayList<>(allSegments);
            otherSegments.removeAll(current.getSegments());

            PointBO start = current.getNodeAt(0);
            PointBO end = current.getNodeAt(current.getWaySize() - 1);

            if (!start.isConnectTwoWays() && !start.isIntersectionPoint() && !start.isWasMerged()) {
                tryToMergeOnSegment(start, otherSegments);
            }
            if (!end.isConnectTwoWays() && !end.isIntersectionPoint() && !end.isWasMerged()) {
                tryToMergeOnSegment(end, otherSegments);
            }
        }
    }

    public void tryToMergeOnSegment(PointBO point, ArrayList<SegmentBO> segments) {
        double minDistance = Double.MAX_VALUE;
        SegmentBO nearestSeg = null;
        for (SegmentBO currentSeg : segments) {
            double currentDist = currentSeg.getLineSegment().distance(point.getCoordinate());
            if (currentDist < minDistance) {
                nearestSeg = currentSeg;
                minDistance = currentDist;
            }
        }

        // FIXME: Adapt size of distance
        if (minDistance < 25) {
            point.project(nearestSeg);
        }
    }

    public void mergeWayEnds(ArrayList<NetworkWay> network) {
        ArrayList<PointBO> wayStartsAndEnds = new ArrayList<>();
        for (NetworkWay way : network) {
            PointBO wayStart = way.getNodeAt(0);
            PointBO wayEnd = way.getNodeAt(way.getWaySize() - 1);
            wayStartsAndEnds.add(wayStart);
            wayStartsAndEnds.add(wayEnd);
        }
        // FIXME: Use kd-tree and later for segments R-tree
        for (int i = 0; i < wayStartsAndEnds.size() - 1; i++) {
            PointBO node1 = wayStartsAndEnds.get(i);
            for (int j = i + 1; j < wayStartsAndEnds.size(); j++) {
                PointBO node2 = wayStartsAndEnds.get(j);
                // FIXME: Adapt size and the size of small ways that are getting removed
                if (node1.distance(node2) < 25) {
                    node1.addSnapBrother(node2);
                }
            }
        }

        mergeCloseEnds(wayStartsAndEnds);
    }

    public void mergeCloseEnds(ArrayList<PointBO> points) {

        HashSet<PointBO> currentMerge;
        HashSet<PointBO> alreadyMerged = new HashSet<>();

        for (PointBO current : points) {
            if (!alreadyMerged.contains(current)) {
                currentMerge = getSnapGroup(current.getSnapBrothers(), 0);
                mergePoints(currentMerge);
                alreadyMerged.addAll(currentMerge);
            }
        }
    }

    public void mergePoints(HashSet<PointBO> points) {
        // Build median of these points
        org.locationtech.jts.geom.Point[] points1 = new org.locationtech.jts.geom.Point[points.size()];
        GeometryFactory gf = new GeometryFactory();

        int loopCount = 0;
        for (PointBO current : points) {
            points1[loopCount++] = gf.createPoint(current.getCoordinate());
        }

        MultiPoint multiPoint = gf.createMultiPoint(points1);
        Coordinate center = Centroid.getCentroid(multiPoint);

        for (PointBO current : points) {
            current.setWasMerged(true);
            current.setX(center.getX());
            current.setY(center.getY());
        }
    }

    public HashSet<PointBO> getSnapGroup(HashSet<PointBO> currentSnapGroup, int size) {

        if (currentSnapGroup.size() == size) {
            return currentSnapGroup;
        } else {
            size = currentSnapGroup.size();
            HashSet<PointBO> extendedSnapgroup = new HashSet<>(currentSnapGroup);

            for (PointBO current : currentSnapGroup) {
                extendedSnapgroup.addAll(current.getSnapBrothers());
            }
            return getSnapGroup(extendedSnapgroup, size);
        }
    }


    public ArrayList<SegmentBO> getNetworkSegments(ArrayList<NetworkWay> network) {
        ArrayList<SegmentBO> segments = new ArrayList<>();
        for (NetworkWay way : network) {
            ArrayList<SegmentBO> waySegments = way.getSegments();
            segments.addAll(waySegments);
        }
        return segments;
    }


    public ArrayList<NetworkWay> reducedWaysToNetworkWays(ArrayList<ReducedWay> redWays) {

        ArrayList<NetworkWay> networkWays = new ArrayList<>();
        int loopcount = 0;
        for (ReducedWay redWay : redWays) {
            networkWays.add(new NetworkWay(redWay, loopcount++));
        }

        return networkWays;
    }

    public ArrayList<ReducedWay> removeUnnecessaryWays() {
        return null;
    }


    /**
     * Merges the given ways one by one into the already merged ways which build up a network.
     *
     * @param ways
     * @param frechetSim
     * @param bufferSize
     * @param maxNetworkSize
     * @return
     */
    public ArrayList<ReducedWay> incrementallyMergeWays(ArrayList<ReducedWay> ways, double frechetSim, int bufferSize, int maxNetworkSize, int minimalWayLength) {
        // The ways that are possibly splitted and merged in the network
        ArrayList<ReducedWay> alreadyInsertedWays = new ArrayList<>();
        double currentNetworkSize = 0;
        for (ReducedWay way : ways) {
            if (currentNetworkSize > maxNetworkSize || way.getLength() < minimalWayLength) {
                break;
            }
            ArrayList<ReducedWay> splitsOfWayMerging = mergeWayOnNetworkBus(way, alreadyInsertedWays, frechetSim, bufferSize);
            alreadyInsertedWays.addAll(splitsOfWayMerging);
            currentNetworkSize += addedNetworksize(splitsOfWayMerging);
        }
        return alreadyInsertedWays;
    }

    /**
     * Returns the length of multiple ways
     *
     * @param ways
     * @return
     */
    public double addedNetworksize(ArrayList<ReducedWay> ways) {
        double size = 0;
        for (ReducedWay way : ways) {
            size += way.getLength();
        }
        return size;
    }

    /**
     * Trys to merge a way to an already existing network of ways based on a buffer size and a frechet similiarity measure.
     * Returns the sections of the way that are not similiar to the existing network and augment the network.
     *
     * @param way
     * @param network
     * @param frechetSim [0,1]
     * @param buffersize [0,1200]
     * @return lines that needs to be inserted into the network
     */
    public ArrayList<ReducedWay> mergeWayOnNetworkBus(ReducedWay way, ArrayList<ReducedWay> network, double frechetSim, int buffersize) {

        way.splitSectionsNew();
        ArrayList<ReducedWay> splits = way.getSplitSections();

        // If its empty its the first way that is inserted and there is nothing to merge
        if (!network.isEmpty()) {
            ArrayList<ReducedWay> newSplits = new ArrayList<>();

            // Repeat merge function with buffer size 70,60,50,40,30,20,10
            for (int i = buffersize; i > 0; i -= 10) {

                for (ReducedWay networkWay : network) {

                    for (ReducedWay splitOfCurrentWay : splits) {
                        // Try to merge the way onto the existing network
                        mergeWays(splitOfCurrentWay, networkWay, frechetSim, i);
                    }

                    newSplits.clear();
                    for (ReducedWay splitOfCurrentWay : splits) {
                        splitOfCurrentWay.splitSectionsNew();
                        // look here
                        ArrayList<ReducedWay> splitsections = splitOfCurrentWay.getSplitSections();
                        newSplits.addAll(splitsections);
                    }

                    splits.clear();
                    // All new independent sections of the current way are added to split list
                    splits.addAll(newSplits);
                }
            }
        }


        // Return the sections of this way that hasnt been merged to the network and therefore augment the network
        return splits;
    }


    /**
     * Trys to merge a way to an already existing network of ways based on a buffer size and a frechet similiarity measure.
     * Returns the sections of the way that are not similiar to the existing network and augment the network.
     *
     * @param way
     * @param network
     * @param frechetSim [0,1]
     * @param buffersize [0,1200]
     * @return lines that needs to be inserted into the network
     */
    public ArrayList<ReducedWay> mergeWayOnNetwork(ReducedWay way, ArrayList<ReducedWay> network, double frechetSim, int buffersize) {

        way.splitSectionsNew();
        ArrayList<ReducedWay> splits = way.getSplitSections();

        // If its empty its the first way that is inserted and there is nothing to merge
        if (!network.isEmpty()) {
            ArrayList<ReducedWay> newSplits = new ArrayList<>();

            // Repeat merge function with buffer size 70,60,50,40,30,20,10
            for (int i = buffersize; i > 0; i -= 10) {
                // Try to merge every section of the current way
                // This sections are created while merged iteratively
                for (ReducedWay splitOfCurrentWay : splits) {

                    for (ReducedWay networkWay : network) {
                        // Try to merge the way onto the existing network
                        mergeWays(splitOfCurrentWay, networkWay, frechetSim, i);
                    }
                }


                newSplits.clear();
                for (ReducedWay splitOfCurrentWay : splits) {
                    splitOfCurrentWay.splitSectionsNew();
                    // look here
                    ArrayList<ReducedWay> splitsections = splitOfCurrentWay.getSplitSections();
                    newSplits.addAll(splitsections);
                }

                splits.clear();
                // All new independent sections of the current way are added to split list
                splits.addAll(newSplits);
            }
        }

        // Return the sections of this way that hasnt been merged to the network and therefore augment the network
        return splits;
    }


    public double calulcateFrechetDistance(LineString a, LineString b) {
        Coordinate firstA = a.getCoordinateN(0);
        Coordinate firstB = b.getCoordinateN(0);
        Coordinate lastB = b.getCoordinateN(b.getNumPoints() - 1);
        double similiarity;
        LineString aDens = (LineString) Densifier.densify(a, 10);

        if (firstA.distance(lastB) < firstA.distance(firstB)) {
            LineString reverseB = b.reverse();
            LineString reverseBDens = (LineString) Densifier.densify(reverseB, 10);
            similiarity = DiscreteFrechetDistance.distance(aDens, reverseBDens);
        } else {
            LineString densB = (LineString) Densifier.densify(b, 10);
            similiarity = DiscreteFrechetDistance.distance(aDens, densB);
        }
        return similiarity;
    }


    public double calculateFrechetSimilarities(LineString a, LineString b) {

        FrechetSimilarityMeasure sm = new FrechetSimilarityMeasure();
        Coordinate firstA = a.getCoordinateN(0);
        Coordinate firstB = b.getCoordinateN(0);
        Coordinate lastB = b.getCoordinateN(b.getNumPoints() - 1);
        double similiarity;
        LineString aDens = (LineString) Densifier.densify(a, 10);


        if (firstA.distance(lastB) < firstA.distance(firstB)) {
            LineString reverseB = b.reverse();
            LineString reverseBDens = (LineString) Densifier.densify(reverseB, 10);
            similiarity = sm.measure(aDens, reverseBDens);
        } else {
            LineString densB = (LineString) Densifier.densify(b, 10);
            similiarity = sm.measure(aDens, densB);
        }


        return similiarity;
    }


    /**
     * Merges the subline of way1 to way2. And splits way1 into subways which endpoints are the ones
     * that are projected onto way2.
     *
     * @param mainWay1
     * @param subWay1
     * @param mainWay2
     */
    private void mergeSubWays(ReducedWay mainWay1, LineString subWay1, ReducedWay mainWay2) {


        if (mainWay2.getId() == 8) {
            System.out.println("This way makes problems");
        }

        LineString mainWay1LineString = mainWay1.getLineString();

        // The whole way that is to be merged is within the buffer of the other way
        // In this case the whole way1 can be merge onto way2 so way1 is just discarded
        if (mainWay1LineString.equals(subWay1)) {
            mainWay2.substitutePartWithRouteType(mainWay1);
            mainWay1.clear();
            return;
        }

        // These are the endpoints of the subway
        // And also the intersection points of mainway1 with the buffer of mainway2 if one ending of mainway1 is not completely contained in the buffer of mainway2
        Coordinate coord1SubWay1 = subWay1.getCoordinateN(0);
        Coordinate coord2Subway1 = subWay1.getCoordinateN(subWay1.getNumPoints() - 1);


        // This nodes represent the endings of the subway1 and will be projected to mainway2
        // If they are also endings of mainway1 fetch the ending of mainway1 otherwise insert them as new nodes to mainway1
        PixelNode p1 = retrieveNodeToProject(mainWay1, coord1SubWay1);
        PixelNode p2 = retrieveNodeToProject(mainWay1, coord2Subway1);


        // project the endpoints of subway1 to mainway2
        // The projection also affects the pixelnodes mainway1
        PixelNode projectedPoint1InWay2 = mainWay2.projectAndInsertPoint(p1);
        PixelNode projectedPoint2InWay2 = mainWay2.projectAndInsertPoint(p2);

        // Delete all nodes of mainway1 that lie between the two nodes that have been projected to mainway2
        // because these are the points that are coherent to mainway2
        mainWay1.deleteAllNodesBetweenNodes(p1, p2);

        // Substitute in mainway1 all vertexes between the projected vertexes through the vertexes of way2
        //Insert all points from way2 between the two inserted points in way1
        List<PixelNode> pointsToInsert = mainWay2.getListBetweenTwoPoints(projectedPoint1InWay2, projectedPoint2InWay2);
        mainWay1.insertListOnRightIndex(pointsToInsert);

        setTypesOfInvolvedNodes(p1, p2, projectedPoint1InWay2, projectedPoint2InWay2, pointsToInsert, mainWay1.getRouteType(), mainWay2.getRouteType());
    }


    /**
     * Adds another routetype to a node if its used by ways of multiple route types.
     *
     * @param p1
     * @param p2
     * @param pointsBetween
     * @param type1
     * @param type2
     */
    public void setTypesOfInvolvedNodes(PixelNode p1, PixelNode p2, PixelNode p3, PixelNode p4, List<PixelNode> pointsBetween, RouteType type1, RouteType type2) {

        RouteType combinedType = RouteTypeCreator.combineTypes(type2, p1.getRouteType());
        p1.setRouteType(combinedType);
        p3.setRouteType(combinedType);
        combinedType = RouteTypeCreator.combineTypes(type2, p2.getRouteType());
        p2.setRouteType(combinedType);
        p4.setRouteType(combinedType);
        combinedType = RouteTypeCreator.combineTypes(type2, type1);

        for (PixelNode current : pointsBetween) {
            current.setRouteType(combinedType);
        }
    }


    /**
     * Decides which node of the given way will be projected on another way.
     * If the coordinate is a endpoint of the way, the endpoint will be projected.
     * This is the case when the ending of the given way lies inside the buffer of another way.
     * If the coordinate is no endpoint of the way the coordinate will be inserted in the way and later projected on the other way.
     *
     * @param way
     * @param coordinate
     * @return
     */
    public PixelNode retrieveNodeToProject(ReducedWay way, Coordinate coordinate) {
        PixelNode nodeToProject;

        // Retrieve endnodes of mthe way to check if the way has the coordinate as one ending
        Coordinate coordBeginningMainWay1 = way.getCoordinates()[0];
        Coordinate coordEndingMainWay1 = way.getCoordinates()[way.getWaySize() - 1];

        // The start of mainway1 is covered by subway1
        if (coordinate.equals2D(coordBeginningMainWay1)) {
            nodeToProject = (PixelNode) way.getWayPoint(0);
        }
        // the end of mainway1 is covered by subway1
        else if (coordinate.equals2D(coordEndingMainWay1)) {
            nodeToProject = (PixelNode) way.getWayPoint(way.getWaySize() - 1);
        } else {
            // Insert the coordinate to the way
            nodeToProject = way.insertCoordinate(coordinate);
        }


        return nodeToProject;
    }

    /**
     * Merges a subway of way1 onto way2 if they have a great frechet similarity
     *
     * @param way1
     * @param subWay1
     * @param way2
     * @param subWay2
     * @param frechetSim
     */
    private void mergeLineStrings(ReducedWay way1, LineString subWay1, ReducedWay way2, LineString subWay2, double frechetSim) {

        double similarity = calculateFrechetSimilarities(subWay1, subWay2);
        if (similarity > frechetSim) {
            // Subway1 is merged onto way2
            mergeSubWays(way1, subWay1, way2);
        }
    }

    private void merge_MultiLineString_to_LineString(ReducedWay way1, MultiLineString subWays1, ReducedWay way2, LineString subWay2, double frechetSim) {
        for (int i = 0; i < subWays1.getNumGeometries(); i++) {
            LineString subway1 = (LineString) subWays1.getGeometryN(i);
            double similarity = calculateFrechetSimilarities(subway1, subWay2);
            if (similarity > frechetSim) {
                mergeSubWays(way1, subway1, way2);
            }
        }
    }


    private void merge_LineString_to_MultilineString(ReducedWay way1, LineString subway1, ReducedWay way2, MultiLineString subways2, double frechetSim) {
        for (int i = 0; i < subways2.getNumGeometries(); i++) {

            LineString subway2 = (LineString) subways2.getGeometryN(i);
            double similarity = calculateFrechetSimilarities(subway1, subway2);
            if (similarity > frechetSim) {
                mergeSubWays(way1, subway1, way2);
                break;
            }
        }
    }

    /**
     * This method merges the subways of way1 onto way2 if the subways have a great frechet similarity to the subways of way2.
     *
     * @param way1
     * @param subWays1
     * @param way2
     * @param subWays2
     * @param frechetSim
     */
    private void mergeMultiLineStrings(ReducedWay way1, MultiLineString subWays1, ReducedWay way2, MultiLineString subWays2, double frechetSim) {


        if (way1.getId()== 8){
            System.out.println("This way");
        }
        ArrayList<ReducedWay> subways1ToMerge = new ArrayList<>();


        for (int i = 0; i < subWays1.getNumGeometries(); i++) {
            LineString first = (LineString) subWays1.getGeometryN(i);
            for (int j = 0; j < subWays2.getNumGeometries(); j++) {
                LineString second = (LineString) subWays2.getGeometryN(j);
                double similarity = calculateFrechetSimilarities(first, second);
                if (similarity > frechetSim) {

                    // Synchronize subways of way1 with way1 so that they will be consistent with way1 after one subway of way1 is merged to way2
                    ReducedWay subwayToMerge = synchronizeSubwayWithWay(way1, first);
                    subways1ToMerge.add(subwayToMerge);

                    //mergeSubWays(way1, first, way2);
                    // Necessary to recalculate the subways of way1 because they can be not consistent with way1 anymore after merging
                    //FIXME
                    break;
                }
            }
        }

        for (ReducedWay subway : subways1ToMerge) {
            mergeSychronizedSubways(way1, subway, way2);
        }
    }

    public void mergeSychronizedSubways(ReducedWay way1, ReducedWay subway1, ReducedWay way2) {


        if (way2.getId() == 8){
            System.out.println("Problems");
        }
        PixelNode p1 = (PixelNode) subway1.getWayPoint(0);
        PixelNode p2 = (PixelNode) subway1.getWayPoint(subway1.getWaySize() - 1);

        // project the endpoints of subway1 to mainway2
        // The projection also affects the pixelnodes mainway1
        // project the endpoints of subway1 to mainway2
        // The projection also affects the pixelnodes mainway1
        PixelNode projectedPoint1InWay2 = way2.projectAndInsertPoint(p1);
        PixelNode projectedPoint2InWay2 = way2.projectAndInsertPoint(p2);

        // Delete all nodes of mainway1 that lie between the two nodes that have been projected to mainway2
        // because these are the points that are coherent to mainway2
        way1.deleteAllNodesBetweenNodes(p1, p2);

        // Substitute in mainway1 all vertexes between the projected vertexes through the vertexes of way2
        //Insert all points from way2 between the two inserted points in way1
        List<PixelNode> pointsToInsert = way2.getListBetweenTwoPoints(projectedPoint1InWay2, projectedPoint2InWay2);
        way1.insertListOnRightIndex(pointsToInsert);


        setTypesOfInvolvedNodes(p1, p2, projectedPoint1InWay2, projectedPoint2InWay2, pointsToInsert, way1.getRouteType(), way2.getRouteType());
    }

    /**
     * Synchronizes a sublist as a linestring of a way to the way by converting the linestring to a subway
     *
     * @param way
     * @param subway
     * @return
     */
    public ReducedWay synchronizeSubwayWithWay(ReducedWay way, LineString subway) {
        Coordinate coord1sub = subway.getCoordinateN(0);
        Coordinate coord2sub = subway.getCoordinateN(subway.getNumPoints() - 1);

        PixelNode synchronizedNode1 = retrieveNodeToProject(way, coord1sub);
        PixelNode synchronizedNode2 = retrieveNodeToProject(way, coord2sub);

        List<PixelNode> synchronizedNodesBetween = way.getListBetweenTwoPointsInclusive(synchronizedNode1, synchronizedNode2);
        ArrayList<PixelNode> synchroAsArrayList = new ArrayList<>(synchronizedNodesBetween);

        ReducedWay synchronizedSubway = new ReducedWay(synchroAsArrayList, way.getId());

        return synchronizedSubway;

    }


    public void mergeWays(ReducedWay way1, ReducedWay way2, double frechetSim, int bufferdistance) {


        if (way1.isEmpty() || way2.isEmpty()) {
            return;
        }
        LineString lineString1 = way1.getLineString();
        LineString lineString2 = way2.getLineString();
        Polygon buffer1 = (Polygon) lineString1.buffer(bufferdistance);
        Polygon buffer2 = (Polygon) lineString2.buffer(bufferdistance);
        Geometry intersBuffer1Line2 = buffer1.intersection(lineString2);
        Geometry intersBuffer2Line1 = buffer2.intersection(lineString1);
        String typeIntersec1 = intersBuffer1Line2.getGeometryType();
        String typeIntersec2 = intersBuffer2Line1.getGeometryType();
        boolean inters1Empty = intersBuffer1Line2.isEmpty();
        boolean inters2Empty = intersBuffer2Line1.isEmpty();

        if (!inters1Empty && !inters2Empty) {

            // Alle kombinationen von geomtrietypen durchgehen Point, LineString, MultiLineString
            // Intersection of Line2 with Buffer1 is linestring
            if (isLineString(typeIntersec1)) {
                //intersection of line1 with Buffer2 is Point
                if (isPoint(typeIntersec2)) {
                    //FIXME: Now Points are just not merged
                    return;
                }
                //intersection of line1 with Buffer2 is LineString
                else if (isLineString(typeIntersec2)) {
                    mergeLineStrings(way1, (LineString) intersBuffer2Line1, way2, (LineString) intersBuffer1Line2, frechetSim);
                }
                //intersection of line1 with Buffer2 is MultiLineString
                else if (isMultiLineString(typeIntersec2)) {
                    //FIXME: Not implemented and in my opinion not possible
                    merge_MultiLineString_to_LineString(way1, (MultiLineString) intersBuffer2Line1, way2, (LineString) intersBuffer1Line2, frechetSim);
                }
                //Intersection of Line2 with Buffer1 is Multilinestring
            } else if (isMultiLineString(typeIntersec1)) {
                if (isPoint(typeIntersec2)) {
                    //FIXME: Point handling
                    return;
                }
                // Intersection of Buffer2 with Line1 is LineString
                else if (isLineString(typeIntersec2)) {
                    merge_LineString_to_MultilineString(way1, (LineString) intersBuffer2Line1, way2, (MultiLineString) intersBuffer1Line2, frechetSim);
                } else if (isMultiLineString(typeIntersec2)) {
                    mergeMultiLineStrings(way1, (MultiLineString) intersBuffer2Line1, way2, (MultiLineString) intersBuffer1Line2, frechetSim);
                }
            } else if (isPoint(typeIntersec1)) {
                // FIXME: How do i handle points?
                if (isPoint(typeIntersec2)) {
                } else if (isLineString(typeIntersec2)) {
                } else if (isMultiLineString(typeIntersec2)) {
                }
            }

        }
    }


    public boolean isPoint(String type) {
        return type.equalsIgnoreCase("Point");
    }

    public boolean isLineString(String type) {
        return type.equalsIgnoreCase("LineString");
    }

    public boolean isMultiLineString(String type) {
        return type.equalsIgnoreCase("MultiLineString");
    }

    public ArrayList<LineString> getLineStringsMultiLine(MultiLineString mulitLine) {
        ArrayList<LineString> lineStrings = new ArrayList<>();
        for (int i = 0; i < mulitLine.getNumGeometries(); i++) {
            lineStrings.add((LineString) mulitLine.getGeometryN(i));
        }
        return lineStrings;
    }


    /**
     * Applys Ramen-Douglas-Peucker Algortihm on all trimmed ways to simplify further calculations in time consuming algorithms.
     * FIXME: Trimmedways need to be intialized before calling this method! (intitializeTrimmedWaysPixels)
     *
     * @param trimmedWays
     * @return
     */
    public static ArrayList<ReducedWay> douglasPeuckerAlgo(ArrayList<TrimmedWay> trimmedWays, double epsilon) {
        ArrayList<ReducedWay> simplifiedWays = new ArrayList<>();
        ReducedWay simplifiedWay;
        int loopCount = 0;
        for (TrimmedWay trimmedWay : trimmedWays) {
            simplifiedWay = douglasPeuckerAlgo(trimmedWay, epsilon, loopCount++);
            simplifiedWays.add(simplifiedWay);
        }
        return simplifiedWays;
    }


    /**
     * Applys Ramen-Douglas-Peucker algorithm on the given way.
     *
     * @param trimmedWay
     * @return
     */
    public static ReducedWay douglasPeuckerAlgo(TrimmedWay trimmedWay, double epsilon, int id) {
        ArrayList<Point> wayPoints = new ArrayList<>();
        // Points are Pixelnodes
        wayPoints.addAll(trimmedWay.getCopyOfPixelNodes());
        List<Point> points = SeriesReducer.reduce(wayPoints, epsilon);
        return new ReducedWay(points, id);
    }


}
