import org.locationtech.jts.geom.*;
import pl.luwi.series.reducer.Point;

import java.util.*;

/**
 * TrimmedWay which is reduced by the Douglas-Peucker algorithm
 */
public class ReducedWay implements Comparable, Cloneable{
    private List<PixelNode> wayPoints;
    String color;
    public int id;
    private ArrayList<ReducedWay> splitSections = new ArrayList<>();
    private RouteType routeType;
    // Indicates wheter this way has a corresponded way from another route
    public boolean isDuplicate = false;


    public boolean containsCoordinate(Coordinate d){
        for (PixelNode node: wayPoints){
            Coordinate c = node.getCoordinate();
            if (new Coordinate((int) c.getX(), (int) c.getY()).equals2D(d)){
                return true;
            }
        }
        return  false;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public void setRouteType(RouteType routeType) {
        this.routeType = routeType;
    }

    public int getId() {
        return id;
    }

    public ReducedWay setColor(String color) {
        this.color = color;
        return this;
    }

    public PixelNode getNode(int index){
        return wayPoints.get(index);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new ReducedWay(this);
    }

    public ReducedWay(ReducedWay toBecopied) {
        this((ArrayList<PixelNode>) toBecopied.getWayPoints(), toBecopied.id);
    }

    public ReducedWay(ArrayList<PixelNode> nodes, int id) {
        this.wayPoints = new ArrayList<>(nodes);
        this.id = id;
    }

    public ReducedWay(ArrayList<PixelNode> nodes, int id, RouteType type) {
        this.wayPoints = new ArrayList<>(nodes);
        this.id = id;
        this.routeType = type;
    }

    public ReducedWay(List<Point> wayPoints) {
        this.wayPoints = new ArrayList<>();
    }

    public ArrayList<SegmentStroke> getTypeDiffSegments(){
        int counter = 0;
        ArrayList<SegmentStroke> segments = new ArrayList<>();
        PixelNode lastNode = null;
        for (PixelNode current: wayPoints){
            if (lastNode == null){
                lastNode = current;
                continue;
            }
            RouteType segmentType = RouteTypeCreator.segmentType(lastNode.getRouteType(), current.getRouteType());
            if (segmentType == null) {
                System.out.println("hello");
            }
            SegmentStroke currentSegment = new SegmentStroke(lastNode, current, segmentType);
            segments.add(currentSegment);
            lastNode = current;
        }

        return segments;
    }


    public void substitutePartWithRouteType(ReducedWay way){
        int index1 = findNearestPointIndex(way.getWayPoint(0));
        int index2 = findNearestPointIndex(way.getWayPoint(way.getWaySize()-1));
        // The index of the first node
        int startIndex = Math.min(index1, index2);
        // The index of the second node
        int endIndex = Math.max(index1, index2);



        for (int i = startIndex; i<=endIndex; i++){
            RouteType combindeType = RouteTypeCreator.combineTypes(way.getRouteType(), wayPoints.get(i).getRouteType());
            this.wayPoints.get(i).setRouteType(combindeType);
        }
    }

    public ArrayList<ReducedWay> getTypeDiffWays(){
        ArrayList<ReducedWay> typeDiffWays = new ArrayList<>();

        int idCounter = 0;


        ArrayList<PixelNode> currentSplit = new ArrayList<>();
        PixelNode lastNode = null;
        for (PixelNode node: wayPoints){

            if(lastNode == null){
                lastNode = node;
                currentSplit.add(node);
                continue;
            }

            // Type vor Type (Subway, Train, ST, STB, etc.)
            if (lastNode.getRouteType() == node.getRouteType()){
                currentSplit.add(node);
            }
            // SUBWAY/TRAIN vor ST
            else if (lastNode.getRouteType() == this.routeType && lastNode.getRouteType() != node.getRouteType()){
                currentSplit.add(node);
                typeDiffWays.add(new ReducedWay(currentSplit,idCounter++ , this.routeType));
                currentSplit.clear();
                currentSplit.add(node);
            }
            // ST vor subway/train
            else{
                if (currentSplit.size()>1){
                    typeDiffWays.add(new ReducedWay(currentSplit,idCounter++ , lastNode.getRouteType()));
                }
                currentSplit.clear();
                currentSplit.add(lastNode);
                currentSplit.add(node);
            }

            lastNode = node;

        }

        if (currentSplit.size() > 1){
            typeDiffWays.add(new ReducedWay(currentSplit, idCounter++, lastNode.getRouteType()));
        }

        return typeDiffWays;

    }

    public ReducedWay(List<Point> wayPoints, int id) {
        this.wayPoints = new ArrayList<>();
        for (Point point: wayPoints){
            this.wayPoints.add(new PixelNode(point.getX(), point.getY()));
        }
        this.id = id;
    }

    public ReducedWay(LineString way) {
        this.wayPoints = new ArrayList<>();
        Coordinate[] coordinates = way.getCoordinates();
        for (Coordinate coordinate : coordinates) {
            this.wayPoints.add(coordinateToPoint(coordinate));
        }
    }

    public double getLength(){
        double length = 0;
        ArrayList<LineSegment> segments = getSegments();
        for (LineSegment segment: segments){
            length += segment.getLength();
        }
        return length;
    }

    /**
     * Has to be executed after dp algorithm !!
     */
    public void initializeRouteTypeOfNodes(){
        for (PixelNode current: wayPoints){
            current.setRouteType(this.routeType);
        }
    }

    public boolean isEmpty() {
        return this.wayPoints.isEmpty();
    }

    public void removePoint(int index) {
        this.wayPoints.remove(index);
    }

    public void clear() {
        this.wayPoints.clear();
    }

    public List<PixelNode> getWayPoints() {
        return wayPoints;
    }

    public Point getWayPoint(int index) {
        return wayPoints.get(index);
    }

    public int getWaySize() {
        return wayPoints.size();
    }

    public void insertPoint(PixelNode point, int index) {
        this.wayPoints.add(index, point);
    }

    public double length() {
        return this.getLineString().getLength();
    }

    @Override
    public boolean equals(Object obj) {
        ReducedWay way = (ReducedWay) obj;
        if (this.wayPoints.equals(way.getWayPoints())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Finds the index of a point in the list.
     *
     * @param node
     * @return
     */
    public int getIndex(PixelNode node) {
        return this.wayPoints.indexOf(node);
    }

    public List<PixelNode> subList(int startIndex, int endIndex) {
        return this.wayPoints.subList(startIndex, endIndex);
    }

    // Insert a list of points at the index
    public void insertPoints(List<PixelNode> points, int index) {
        this.wayPoints.addAll(index, points);
    }

    /**
     * Projects a point on this {@link ReducedWay}.
     *
     * @param point
     * @return
     */
    public PixelNode projectAndInsertPoint(PixelNode point) {

        boolean pointAlreadyContained = false;
        point.setProjectedNode(true);
        point.setProjectedWay(this);
        //This is for the network ways to recognize which ways are connected
        point.setConnectTwoWays(true);


        int indexToInsert = findClosestSegmentIndex(point);
        // Build line segment on which the point will be projected
        LineSegment segment = new LineSegment(pointToCoordinate(getWayPoint(indexToInsert)), pointToCoordinate(getWayPoint(indexToInsert + 1)));
        Coordinate projection = segment.closestPoint(pointToCoordinate(point));
        point.setPixelX(projection.getX());
        point.setPixelY(projection.getY());


        PixelNode pointToInsert = new PixelNode(point.getX(), point.getY());

        // Check if the given node is projected to a point that is already containd in the way (most likely an endpoint)
        for (int i = 0; i < wayPoints.size(); i++) {
            PixelNode currentPoint = wayPoints.get(i);
            if (currentPoint.getCoordinate().equals2D(projection)) {
                // Substitute the old point with the new point
                PixelNode oldPoint = wayPoints.remove(i);
                point.setRouteType(RouteTypeCreator.combineTypes(oldPoint.getRouteType(), point.getRouteType()));
                pointToInsert.setRouteType(point.getRouteType());
                wayPoints.add(i, pointToInsert);
                pointAlreadyContained = true;
            }
        }

        if (!pointAlreadyContained) {
            insertPoint(pointToInsert, indexToInsert + 1);
        }

        return pointToInsert;
    }

    public boolean cutAtSelfintersection(){
        ArrayList<PixelNode> newWayPoints = new ArrayList<>();
        newWayPoints.add(wayPoints.get(0));
        ArrayList<LineSegment> intersectionsFreeSegments = new ArrayList<>();
        ArrayList<LineSegment> allSegments = getSegments();
        for (int i = 0; i<allSegments.size(); i++){
            LineSegment toCheckForIntersections = allSegments.get(i);
            Coordinate tc1 = toCheckForIntersections.p0;
            Coordinate tc2 = toCheckForIntersections.p1;
            for (int j = 0; j<intersectionsFreeSegments.size(); j++){
                LineSegment intersectionFreeSeg = intersectionsFreeSegments.get(j);
                Coordinate fc1 = intersectionFreeSeg.p0;
                Coordinate fc2 = intersectionFreeSeg.p1;
                if (tc1.equals2D(fc1) || tc1.equals2D(fc2) ||tc2.equals2D(fc1) ||tc2.equals2D(fc2)){
                    continue;
                }
                else{
                    Coordinate intersection = toCheckForIntersections.intersection(intersectionFreeSeg);
                    // Intersection occured so cut the way at this point
                    if (intersection != null){
                        return true;
                    }
                }
            }
            newWayPoints.add(wayPoints.get(i+1));
        }

        this.wayPoints = newWayPoints;
        return false;
    }


    /**
     * Calculates all parts of a way that are not merged onto another way and
     */
    public void splitSectionsNew(){
        splitSections = new ArrayList<>();
        ArrayList<PixelNode> currentSplit = new ArrayList<>();
        PixelNode lastNode = null;

        for (int i = 0; i<wayPoints.size(); i++){
            PixelNode currentNode = wayPoints.get(i);
            if(!currentNode.isProjectedNode() && !currentNode.isNodeOfOtherWay()){
                if (lastNode != null && lastNode.isProjectedNode()){
                    currentSplit.add(lastNode);
                }
                currentSplit.add(currentNode);
            }
            else if (currentNode.isProjectedNode()){
                if (!currentSplit.isEmpty()){
                    currentSplit.add(currentNode);
                    splitSections.add(new ReducedWay(currentSplit, this.id, this.routeType));
                    currentSplit.clear();
                }
                // In this case two adjacent nodes of one way are projected onto seperate ways
                else if (lastNode != null && lastNode.isProjectedNode() && lastNode.getProjectedWay() != currentNode.getProjectedWay()){
                    // Check if the distance of the node is greater than a certain distance, otherwise adding this as a way brings no value
                    if (lastNode.getCoordinate().distance(currentNode.getCoordinate()) > 50){
                        currentSplit.add(lastNode);
                        currentSplit.add(currentNode);
                        splitSections.add(new ReducedWay(currentSplit, this.id, this.routeType));
                        currentSplit.clear();
                    }
                }
            }
            else if (currentNode.isNodeOfOtherWay()){
            }
            lastNode = currentNode;
        }

        if (currentSplit.size()>1){
            splitSections.add(new ReducedWay(currentSplit, this.id, this.routeType));
        }

        // Reset all information about projection of nodes for future mergings
        // Without reset this strategy will not hold for future mergings and splittings
        for (Point p : wayPoints) {
            ((PixelNode) p).setProjectedNode(false);
            ((PixelNode) p).setNodeOfOtherWay(false);
            ((PixelNode) p).setProjectedWay(null);
        }
    }



    /**
     * Necessary to call createSplitSections() method before invoking this method.
     * @return splitsections of this way after merging
     */
    public ArrayList<ReducedWay> getSplitSections() {
        return splitSections;
    }

    /**
     * ! For speicla use case !
     * Searches for the two nodes in the list and deletes all nodes between them.
     * Also marks this points as splitsections for the reducedWay. All nodes between these nodes
     * will be nodes from another way to which this way has been merged.
     *
     * @param p1
     * @param p2
     */
    public void deleteAllNodesBetweenNodes(PixelNode p1, PixelNode p2) {
        int indexP1 = getIndex(p1);
        int indexP2 = getIndex(p2);
        // the index after the first node
        int removeIndex = Math.min(indexP1, indexP2) + 1;
        // Number of nodes between the two given nodes
        int numberRemovedNodes = Math.abs(indexP1 - indexP2) - 1;

        // Remove all points in between
        for (int i = 0; i < numberRemovedNodes; i++) {
            removePoint(removeIndex);
        }

    }



    /**
     * Seraches for the index of the two nodes and retrieves a list of all nodes between these two nodes
     *
     * @param p1
     * @param p2
     * @return
     */
    public List<PixelNode> getListBetweenTwoPoints(PixelNode p1, PixelNode p2) {
        int indexP1 = getIndex(p1);
        int indexP2 = getIndex(p2);
        // The index after the first node
        int startIndex = Math.min(indexP1, indexP2) + 1;
        // The index of the second node
        int endIndex = Math.max(indexP1, indexP2);

        // Endindex is exclusive and will not be included in the sublist
        List<PixelNode> pointsBetween = subList(startIndex, endIndex);
        return pointsBetween;
    }

    public List<PixelNode> getListBetweenTwoPointsInclusive(PixelNode p1, PixelNode p2){
        int indexP1 = getIndex(p1);
        int indexP2 = getIndex(p2);
        // The index after the first node
        int startIndex = Math.min(indexP1, indexP2);
        // The index of the second node
        int endIndex = Math.max(indexP1, indexP2)+1;

        // Endindex is exclusive and will not be included in the sublist
        List<PixelNode> pointsBetween = subList(startIndex, endIndex);
        return pointsBetween;
    }

    /**
     * ! Special use case !
     * Inserts the list and marks every node that is inserted as a node that is already contained in another list.
     * All nodes that are inserted are a sublist of a way this way has been merged to.
     *
     * @param nodes
     */
    public void insertListOnRightIndex(List<PixelNode> nodes) {
        for (PixelNode node : nodes) {
            PixelNode pixelNode = node;
            pixelNode.setNodeOfOtherWay(true);
            int indexToInsert = findClosestSegmentIndex(node);
            insertPoint(pixelNode, indexToInsert + 1);
        }
    }

    public int findClosestSegmentIndex(PixelNode point) {
        ArrayList<LineSegment> lineSegments = getSegments();
        int nearestIndex = 0;
        double nearestDistance = Double.MAX_VALUE;

        // Find index to insert
        for (int i = 0; i < lineSegments.size(); i++) {
            double distance = lineSegments.get(i).distance(pointToCoordinate(point));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    public LineSegment getClosestSegment(LineSegment segment) {
        ArrayList<LineSegment> lineSegments = getSegments();
        LineSegment nearestSegment = null;
        double nearestDistance = Double.MAX_VALUE;

        // Find index to insert
        for (int i = 0; i < lineSegments.size(); i++) {
            double distance = lineSegments.get(i).distance(segment);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestSegment = lineSegments.get(i);
            }
        }
        return nearestSegment;
    }

    /**
     * The coordinate has to lie on the way and is inserted between the two adjacent points.
     *
     * @param coordinate
     * @return the index of the inserted coordinate in the {@link Point} list
     */
    public PixelNode insertCoordinate(Coordinate coordinate) {

        ArrayList<LineSegment> lineSegments = getSegments();
        int nearestIndex = 0;
        double nearestDistance = Double.MAX_VALUE;

        // Find index to insert
        for (int i = 0; i < lineSegments.size(); i++) {
            double distance = lineSegments.get(i).distance(coordinate);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestIndex = i;
            }
        }


        // FIXME: Distance should always be null, can only be null if way is merged with several sublines to the same way and the sublines are not updated
        if (nearestDistance <= 0.02) {
            PixelNode point = coordinateToPoint(coordinate);
            point.setRouteType(this.routeType);
            insertPoint(point, nearestIndex + 1);
            return point;
        } else {
            return null;
        }
    }

    /*public List<ISegment> getSegmentsBO() {
        List<ISegment> segments = new ArrayList<>();

        Point lastPoint = null;
        for (Point point : this.wayPoints) {
            if (lastPoint != null) {
                segments.add(new SegmentBO(pointTransform(lastPoint), pointTransform(point), this.id));
            }
        }
        return segments;
    }*/


    public ArrayList<LineSegment> getSegments() {
        ArrayList<LineSegment> segments = new ArrayList<>();

        Point lastPoint = null;
        for (Point point : this.wayPoints) {
            if (lastPoint != null) {
                segments.add(new LineSegment(pointToCoordinate(lastPoint), pointToCoordinate(point)));
            }
            lastPoint = point;
        }
        return segments;
    }

    /**
     * FIXME!
     * Merges the given way into the calling way
     *
     * @param toBeMerged
     * @param distance
     */
    public void mergeIntoThis(ReducedWay toBeMerged, double distance) {

        Coordinate[] startingCoordinates = toBeMerged.getCoordinates();
        // ToBeMerged = Tbm
        int indexOfStartinigCoordinate = 0;

        for (Coordinate startingCoordinate : startingCoordinates) {
            // Finde index des nähesten punktes aus ziellinie
            int nearestDestinyCoordIndex = findNearestCoordinateIndex(startingCoordinate);
            Coordinate nearestDestinyCoord = this.getCoordinates()[nearestDestinyCoordIndex];

            // Build all segments over destiny and toBeMerge coordinate
            // Destiny Coord is starting / end point of line segment
            LineSegment segmentDestiny1 = this.getLineSegment(nearestDestinyCoordIndex);
            LineSegment segmentDestiny2 = this.getLineSegment(nearestDestinyCoordIndex + 1);
            LineSegment segmentStarting1 = this.getLineSegment(indexOfStartinigCoordinate);
            LineSegment segmentStarting2 = this.getLineSegment(indexOfStartinigCoordinate + 1);

            //Check distance for toBeMergedCoord to both segments of destinyPoint
            double distStartpToDestSeg1 = calcDistance(startingCoordinate, segmentDestiny1);
            double distStartpToDestSeg2 = calcDistance(startingCoordinate, segmentDestiny2);
            // Check which segment from the destiny is the best suitable
            boolean destinySeg1IsCloser = distStartpToDestSeg1 <= distStartpToDestSeg2;

            double distanceSegs1;
            double distanceSegs2;

            //Continue with the nearest segment from the destiny point
            if (destinySeg1IsCloser) {
                // Distance between segment1 of starting point and segment1 of destiny
                distanceSegs1 = calcDistance(segmentDestiny1, segmentStarting1);
                distanceSegs2 = calcDistance(segmentDestiny1, segmentStarting2);
            } else {
                distanceSegs1 = calcDistance(segmentDestiny2, segmentStarting1);
                distanceSegs2 = calcDistance(segmentDestiny2, segmentStarting2);
            }

            // Check which segment of the starting point is the most suitable
            boolean startingSeg1IsCloser = distanceSegs1 <= distanceSegs2;

            //Continue with the most suitable segment of the starting point
            if (startingSeg1IsCloser) {
                //Calculate the angle between the most suitable segments
                //...
                //Merge the startingpoint to the most suitable destiny segment only if the angle between the both exceeds a certain threshold
            }


            indexOfStartinigCoordinate++;
        }
    }

    /**
     * Calculates the distance between two linesegments.
     *
     * @param seg1
     * @param seg2
     * @return
     */
    public double calcDistance(LineSegment seg1, LineSegment seg2) {
        if (seg1 == null || seg2 == null) {
            return Double.MAX_VALUE;
        } else return seg1.distance(seg2);
    }


    /**
     * Calculates the distance between a linesegment and a coordinate
     *
     * @param c
     * @param seg
     * @return
     */
    public double calcDistance(Coordinate c, LineSegment seg) {
        if (c == null || seg == null) {
            return Double.MAX_VALUE;
        } else return seg.distance(c);
    }


    /**
     * Builds a linesegment for the index of the coordinate which should be the end vertex of the segment.
     *
     * @param indexOfEndVertex
     * @return
     */
    public LineSegment getLineSegment(int indexOfEndVertex) {
        if (indexOfEndVertex <= 0 || indexOfEndVertex >= this.getWaySize()) {
            return null;
        }
        Coordinate first = this.getCoordinates()[indexOfEndVertex - 1];
        Coordinate second = this.getCoordinates()[indexOfEndVertex];
        return new LineSegment(first, second);
    }

    /**
     * Merges the points of this way to the segments of way2 when the points are closer than the given pointToSegmentDistance
     * to at least one segment.
     * Caution! KdTree has to be initialized for way2 before using this method!
     *
     * @param way2
     * @param pointToSegmentDistance
     * @return
     */
    public void mergeInto(ReducedWay way2, double pointToSegmentDistance) {
        ArrayList<PixelNode> thisNewPoints = new ArrayList<>();
        ArrayList<PixelNode> wayToMergeNewPoints = new ArrayList<>();
        Coordinate[] way2Coordinates = way2.getCoordinates();
        int coordinateIndex = 0;

        for (Coordinate thisCoordinate : this.getCoordinates()) {
            int nearestPointIndex = way2.findNearestCoordinateIndex(thisCoordinate);
            // Fetch coordinates new because points of way2 have been maybe adjusted
            way2Coordinates = way2.getCoordinates();

            ArrayList<LineSegment> closestSegments = buildSegments(way2Coordinates, nearestPointIndex);
            // Distance between the point and the segments (one or two segments)
            double[] distancesPointSegment = new double[]{10000, 10000};
            for (int i = 0; i < closestSegments.size(); i++) {
                distancesPointSegment[i] = closestSegments.get(i).distance(thisCoordinate);
            }
            // First segment is closer than second segment
            if (distancesPointSegment[0] <= distancesPointSegment[1]) {
                // Point is so close to segment that it should be merged on that segment
                if (distancesPointSegment[0] <= pointToSegmentDistance) {
                    // Calculate the point on the segment that is closest to the given coordinate
                    Coordinate closestPointOnSegment = closestSegments.get(0).closestPoint(thisCoordinate);
                    // Substitute the given coordinate with the coordinate on the segment
                    thisNewPoints.add(coordinateToPoint(closestPointOnSegment));
                    // Insert a coordinate between the two coordinates which build up the closest segment
                    // Between the index and its following coordinate if the point is not already contained in the point list
                    if (nearestPointIndex == 0) {
                        if (!closestPointOnSegment.equals2D(way2Coordinates[0]) && !closestPointOnSegment.equals2D(way2Coordinates[1])) {
                            way2.insertPoint(coordinateToPoint(closestPointOnSegment), 1);
                        }
                    }
                    // Between the index and its previous coordinate
                    else if (nearestPointIndex == way2Coordinates.length - 1) {
                        if (!closestPointOnSegment.equals2D(way2Coordinates[nearestPointIndex - 1]) && !closestPointOnSegment.equals2D(way2Coordinates[nearestPointIndex])) {
                            way2.insertPoint(coordinateToPoint(closestPointOnSegment), way2Coordinates.length - 2);
                        }
                    } else {
                        if (!closestPointOnSegment.equals2D(way2Coordinates[nearestPointIndex - 1]) && !closestPointOnSegment.equals2D(way2Coordinates[nearestPointIndex])) {
                            way2.insertPoint(coordinateToPoint(closestPointOnSegment), nearestPointIndex);
                        }
                    }
                } else {
                    thisNewPoints.add(coordinateToPoint(thisCoordinate));
                }
            }
            // Second segment is closer than the first second which can only happen when there are two segments
            else {
                if (distancesPointSegment[1] <= pointToSegmentDistance) {
                    Coordinate closestPointOnSegment = closestSegments.get(1).closestPoint(thisCoordinate);
                    thisNewPoints.add(coordinateToPoint(closestPointOnSegment));
                    if (!closestPointOnSegment.equals2D(way2Coordinates[nearestPointIndex]) && !closestPointOnSegment.equals2D(way2Coordinates[nearestPointIndex + 1])) {
                        way2.insertPoint(coordinateToPoint(closestPointOnSegment), nearestPointIndex + 1);
                    }
                } else {
                    thisNewPoints.add(coordinateToPoint(thisCoordinate));
                }
            }
        }
        this.wayPoints = thisNewPoints;
    }

    public void setWayPoints(ArrayList<PixelNode> points) {
        this.wayPoints = points;
    }

    public PixelNode coordinateToPoint(Coordinate coordinate) {
        return new PixelNode(coordinate.getX(), coordinate.getY());
    }

    /**
     * Builds up a list of LineSegments based on a list of coordinates and the index of the starting point of the indexes.
     *
     * @param coordinates
     * @param index
     * @return list of one or two segments
     */
    public ArrayList<LineSegment> buildSegments(Coordinate[] coordinates, int index) {
        ArrayList<LineSegment> segments = new ArrayList<>();
        LineSegment segment;
        // First vertex
        if (index == 0) {
            segment = new LineSegment(coordinates[index], coordinates[index + 1]);
        }
        // Last vertex
        else if (index == coordinates.length - 1) {
            segment = new LineSegment(coordinates[index - 1], coordinates[index]);
        }
        // Middle vertex => Conitgous to two segments
        else {
            segment = new LineSegment(coordinates[index - 1], coordinates[index]);
            segments.add(segment);
            segment = new LineSegment(coordinates[index], coordinates[index + 1]);
        }
        segments.add(segment);
        return segments;
    }


    /**
     * Converts {@link Coordinate} to double array
     *
     * @param coordinate
     * @return
     */
    public int findNearestCoordinateIndex(Coordinate coordinate) {
        //nothing
        return 0;
    }

    /**
     * @param coordinate
     * @return
     */
    public double[] coordinateToDouble(Coordinate coordinate) {
        return new double[]{coordinate.getX(), coordinate.getY()};
    }


    /**
     * Finds the index of the pointlist of the nearest point to the given point.
     *
     * @param point
     * @return index of nearest point in pointlist
     */
    public int findNearestPointIndex(Point point) {
        int nearestIndex = -1;
        double nearestDistance = Double.MAX_VALUE;
        for(int i = 0; i<wayPoints.size(); i++){
            double currentDistance = new Coordinate(point.getX(), point.getY()).distance(new Coordinate(wayPoints.get(i).getX(), wayPoints.get(i).getY()));
            if (currentDistance < nearestDistance){
                nearestDistance = currentDistance;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    /**
     * Converts {@link Point} to double array
     *
     * @param point
     * @return
     */
    public double[] pointToDouble(Point point) {
        return new double[]{point.getX(), point.getY()};
    }

    /**
     * Converts {@link Point} to {@link Coordinate}
     *
     * @param point
     * @return
     */
    public Coordinate pointToCoordinate(Point point) {
        return new Coordinate(point.getX(), point.getY());
    }

    /**
     * Changes the data structure of the reduced way to a {@link LineString}
     * TODO: Fully functioning!
     *
     * @return
     */
    public LineString getLineString() {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coordinates = getCoordinates();
        LineString lineString = gf.createLineString(coordinates);
        return lineString;
    }

    /**
     * Change data structure of list of points to array of coordinates.
     *
     * @return
     */
    public Coordinate[] getCoordinates() {
        Coordinate[] coordinates = new Coordinate[wayPoints.size()];
        int loopCount = 0;
        for (Point point : this.wayPoints) {
            coordinates[loopCount++] = new Coordinate(point.getX(), point.getY());
        }
        return coordinates;
    }

    @Override
    public int compareTo(Object o) {
        ReducedWay way2 = (ReducedWay) o;
        if (this.length() > way2.length()) {
            return -1;
        } else if (this.length() < way2.length()) {
            return 1;
        }
        return 0;
    }
}
