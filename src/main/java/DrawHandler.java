
import org.jgrapht.graph.SimpleWeightedGraph;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class DrawHandler {

    private DrawToGraphics drawToGraphics;
    private OsmDataHandler dataHandler;
    public HashMap<Integer, Node> nodesTest = new HashMap<>();
    public ArrayList<Point> intersectionsTest;

    public DrawHandler(DrawToGraphics drawToGraphics,
                       OsmDataHandler dataHandler) {
        this.drawToGraphics = drawToGraphics;
        this.dataHandler = dataHandler;
    }

    public void test() {
        Node node4 = nodesTest.get(4);
        Node node7 = nodesTest.get(7);
        Node node6 = nodesTest.get(6);
        Node node3 = nodesTest.get(3);
        Node node2 = nodesTest.get(2);
        if (intersectionsTest.get(0).get_x_coord() < node6.getLongitude()) {
            System.out.println("0 int Links von 6");
        }
        if (intersectionsTest.get(1).get_x_coord() < node6.getLongitude()) {
            System.out.println("1 int Links von 6");
        }
        if (intersectionsTest.get(0).get_x_coord() < node3.getLongitude()) {
            System.out.println("0 intersec Links von 3");
        }
        if (intersectionsTest.get(1).get_x_coord() < node3.getLongitude()) {
            System.out.println("1 intersec Links von 3");
        }
        Point p3 = new Point(node3.getLongitude(), node3.getLatitude());
        Point p4 = new Point(node4.getLongitude(), node4.getLatitude());
        Point p6 = new Point(node6.getLongitude(), node6.getLatitude());
        Point p7 = new Point(node7.getLongitude(), node7.getLatitude());
        Point p2 = new Point(node2.getLongitude(), node2.getLatitude());
        BentleyOttmann ot;
        Segment seg23 = new Segment(p2, p3);
        Segment seg34 = new Segment(p3, p4);
        Segment seg67 = new Segment(p6, p7);
        ArrayList<Segment> data = new ArrayList<>();
        data.add(seg34);
        data.add(seg67);
        data.add(seg23);
        ot = new BentleyOttmann(data);
        ot.find_intersections();
        ArrayList<Point> intersections = ot.get_intersections();
        for (Point p : intersections) {
            System.out.println("Gefunden int: " + p.get_x_coord() + " | " + p.get_y_coord());
        }

    }

    /**
     * Draws the selected details of the given route types (e.g. busroutes, trainroutes, etc.)
     *
     * @param stopsOrRoutes
     * @param routeTypes
     */
    public void draw(DetailsOfRoute stopsOrRoutes, ArrayList<RouteType> routeTypes) {
        switch (stopsOrRoutes) {
            case HALT:
                drawHalts(routeTypes);
                break;
            case ROUTE:
                drawRoutes(routeTypes);
                //drawIntersectionsGeo(routeTypes);
                //test();
                break;
            case HALTANDROUTE:
                drawRoutes(routeTypes);
                //drawIntersectionsGeo(routeTypes);
                drawHalts(routeTypes);
                break;
        }
    }

    /**
     * Draws all halts for the given types of routes.
     * E.g. all stop for all bus routes.
     *
     * @param routeTypes
     */
    private void drawHalts(ArrayList<RouteType> routeTypes) {
        for (RouteType type : routeTypes) {
            drawHalts(type);
        }
    }

    /**
     * Draws all halts for all Relations of the given RouteType
     *
     * @param type
     */
    private void drawHalts(RouteType type) {
        HashSet<Node> halts = this.dataHandler.getMergedHalts(type);
        //HashSet<Node> halts = this.dataHandler.getThinnedMergedHalts(type);
        //HashSet<Node> halts = this.dataHandler.getDifference(type);
        // HashSet<Node> halts = this.dataHandler.getAllStopAndPlatformNodes(type);
        this.drawToGraphics.drawNodes(halts, ColorPicker.colorForHalts(type), new Font("TimesRoman", Font.PLAIN, 20));
    }

    /**
     * Draws routes for all given route types
     *
     * @param routeTypes
     */
    private void drawRoutes(ArrayList<RouteType> routeTypes) {
        for (RouteType type : routeTypes) {
            drawTest(type);
        }
        //drawTest();
    }

    /**
     * Draws all route ways for all reltions of the given type
     *
     * @param type
     */
    private void drawRoutes(RouteType type) {
        Color color = ColorPicker.colorForRoutes(type);
        ArrayList<TrimmedWay> trimmedWays = this.dataHandler.getTrimmedWays(type);
        System.out.println(trimmedWays.size());
        ArrayList<Node> wayNodes;
        int number = 0;
        int relationNumber = 0;
        // Go through all trimmed ways of this relation
        for (TrimmedWay trimmedWay : trimmedWays) {
            wayNodes = getFullNodes(trimmedWay.getWaynodes());
            this.drawToGraphics.drawWay(wayNodes, Color.YELLOW, 3);
            for (Node node : wayNodes) {
                //this.drawToGraphics.drawNode(node, number);
                this.nodesTest.put(number, node);
                System.out.println("Node id : " + node.getId());
                System.out.println("Node number: " + number);
                System.out.println("Lon: " + node.getLongitude() + " | Lat: " + node.getLatitude());
                number++;
            }
        }
    }


    private void drawRoutesBasedOnPixels(RouteType type) {
        Color color = ColorPicker.colorForRoutes(type);
        ArrayList<ReducedWay> trimmedWays = this.dataHandler.getDouglasPeuckerWays(type);
        ArrayList<Node> wayNodes;
        int number = 0;
        // Go through all trimmed ways of this relation
        for (ReducedWay trimmedWay : trimmedWays) {
            this.drawToGraphics.drawReducedWay(trimmedWay, Color.RED, 3);
            for (pl.luwi.series.reducer.Point point : trimmedWay.getWayPoints()) {
                //this.drawToGraphics.drawCoordinate((int) point.getX(), (int) point.getY(), number++);
            }
        }
        System.out.println(number);
    }

    /**
     * DRaws the oriinal trimmed ways and the ways which are reduced in their shape with the douglas peucker algorithm
     *
     * @param type
     */
    private void drawDouglasPeukerComparisonOneAndSix(RouteType type) {
        ArrayList<TrimmedWay> trimmedWays = this.dataHandler.getTrimmedWays(type);

        /*ArrayList<Node> wayNodes;
        for (TrimmedWay trimmedWay : trimmedWays) {
            wayNodes = getFullNodes(trimmedWay.getWaynodes());
            this.drawToGraphics.drawWay(wayNodes, Color.YELLOW, 3);
        }*/
        //this.drawToGraphics.drawWay(getFullNodes(trimmedWays.get(1).getWaynodes()), Color.YELLOW, 3);
        //this.drawToGraphics.drawWay(getFullNodes(trimmedWays.get(6).getWaynodes()), Color.YELLOW, 3);


        ArrayList<ReducedWay> reducedWays = this.dataHandler.getDouglasPeuckerWays(type);
        this.drawToGraphics.drawReducedWay(reducedWays.get(1), Color.RED, 3);
        this.drawToGraphics.drawReducedWay(reducedWays.get(6), Color.YELLOW, 3);
        /*int number = 0;
        for (ReducedWay reducedWay : reducedWays) {
            this.drawToGraphics.drawReducedWay(reducedWay, Color.RED, 3);
            for (pl.luwi.series.reducer.Point point: reducedWay.getWayPoints()){
                this.drawToGraphics.drawCoordinate((int) point.getX(), (int) point.getY(), number++);
            }
        }*/
        int no = 0;
        for (pl.luwi.series.reducer.Point point : reducedWays.get(1).getWayPoints()) {
            this.drawToGraphics.drawCoordinate((int) point.getX(), (int) point.getY(), no++, Color.PINK);
        }
        no = 0;
        for (pl.luwi.series.reducer.Point point : reducedWays.get(6).getWayPoints()) {
            this.drawToGraphics.drawCoordinate((int) point.getX(), (int) point.getY(), no++, Color.BLUE);
        }
    }

    private ArrayList<LineString> initializeLineStrings() throws ParseException {
        ArrayList<LineString> lineStrings = new ArrayList<>();
        Geometry blue = new WKTReader().read("LINESTRING (400 100, 400 200, 500 300, 700 300, 800 400, 900 500, 1000 600, 1100 500)");
        blue.setUserData("blue");
        Geometry pink = new WKTReader().read("LINESTRING (1000 200, 800 300, 900 600, 1000 1000)");
        pink.setUserData("pink");
        Geometry yellow = new WKTReader().read("LINESTRING (0 600, 100 700, 100 800, 200 900, 200 1000, 100 1100)");
        yellow.setUserData("yellow");
        Geometry lila = new WKTReader().read("LINESTRING (200 600, 300 700, 300 800, 400 900, 400 1000, 300 1100, 500 1100)");
        lila.setUserData("lila");
        Geometry orange = new WKTReader().read("LINESTRING (200 1100, 900 1100, 900 900, 800 500, 500 500, 600 0)");
        orange.setUserData("orange");
        Geometry red = new WKTReader().read("LINESTRING (200 1100, 300 1000, 300 900, 200 800, 200 700, 100 600)");
        red.setUserData("red");
        lineStrings.add((LineString) blue);
        lineStrings.add((LineString) pink);
        lineStrings.add((LineString) yellow);
        lineStrings.add((LineString) lila);
        lineStrings.add((LineString) orange);
        lineStrings.add((LineString) red);
        return lineStrings;
    }

    public Color colorPicker(String color) {
        switch (color) {
            case "blue":
                return Color.BLUE;
            case "pink":
                return Color.PINK;
            case "orange":
                return Color.ORANGE;
            case "yellow":
                return Color.YELLOW;
            case "red":
                return Color.RED;
            case "lila":
                return Color.magenta;
            default:
                return Color.WHITE;
        }
    }

    public double addedNetworksize(ArrayList<ReducedWay> ways) {
        double size = 0;
        for (ReducedWay way : ways) {
            size += way.getLength();
        }
        return size;
    }

    public int[] decodeNumber(int num) {
        int w = (int) (Math.sqrt(8 * num + 1) - 1) / 2;
        int t = (int) (Math.pow(w, 2) + w) / 2;
        int y = (int) (num - t);
        int x = (int) (w - y);
        return new int[]{x, y};
    }

    public void drawTest(RouteType type) {


        ArrayList<TrimmedWay> trimmedSubwayWays = this.dataHandler.getTrimmedWays(RouteType.SUBWAY);
        ArrayList<TrimmedWay> trimmedTrainWays = this.dataHandler.getTrimmedWays(RouteType.TRAIN);
        ArrayList<TrimmedWay> trimmedWaysBus = this.dataHandler.getTrimmedWays(RouteType.BUS);


        Network subwayNetwork = new Network(type, trimmedSubwayWays);
        // Completely merged
        subwayNetwork.dpAndMergeIntern();


        Network trainNetwork = new Network(RouteType.TRAIN, trimmedTrainWays);
        trainNetwork.applyDouglasPeucker();

        Network subwayAndTrain = subwayNetwork.mergeIntoThis(trainNetwork);


        Network busNetwork = new Network(RouteType.BUS, trimmedWaysBus);
        busNetwork.applyDouglasPeucker();
        busNetwork.buildStrokes();
        ArrayList<ReducedWay> busWays = busNetwork.getMergedWays();



        Network allPublicTransport = subwayAndTrain.mergeBusInto(busNetwork);

        allPublicTransport.splitSegmentsAtIntersections();

        //allPublicTransport.initializeConnectionList();

        SimpleWeightedGraph graph = allPublicTransport.getPlanarGraph();

        //this.drawToGraphics.drawGraph(graph);
        //this.drawToGraphics.drawGraph(CurveVertexes.connectionVertexGraph);
        this.drawToGraphics.drawDirectedGraph(CurveVertexes.directedGraph);
        int num = 0;
        for (int vertex: CurveVertexes.stopPointVertexes){
            CurveVertexes.vertexToIdMapping.put(vertex, num);
            int xy[] = decodeNumber(vertex);

            this.drawToGraphics.drawStopPoint(new Coordinate(xy[0], xy[1]), 0, num++);
        }

        /*


        for (ReducedWay way: allPublicTransport.getMergedWays()){
            this.drawToGraphics.drawMultiTypeReducedway(way, 6);
        }

        int num = 0;
        for (PointBoNew point: INtersectionContainer.intersections){
            //this.drawToGraphics.drawCoordinate((int) point.x(), (int) point.y(), num++);
        }

        HashMap<Integer, Integer> nodeOccurences = new HashMap<>();
        for(SegmentBoNew segs: allPublicTransport.getSegments()){
            PointBoNew p1 = (PointBoNew) segs.getP1();
            PointBoNew p2 = (PointBoNew) segs.getP2();

            Integer i1 = nodeOccurences.get(p1.getId());
            if (i1 != null){
                i1++;
                nodeOccurences.put(p1.getId(), i1);
            }
            else{
                nodeOccurences.put(p1.getId(), 1);
            }

            Integer i2 = nodeOccurences.get(p2.getId());
            if (i2 != null){
                i2++;
                nodeOccurences.put(p2.getId(), i2);
            }
            else{
                nodeOccurences.put(p2.getId(), 1);
            }
        }
        int numm = 0;
        for (int hi: nodeOccurences.keySet()){
            if (nodeOccurences.get(hi) > 2){
                PointBoNew p = new PointBoNew(hi);
                this.drawToGraphics.drawCoordinate((int) p.x(), (int) p.y(), numm++, Color.GREEN);
            }
        }

        */



/*

        ArrayList<ReducedWay> allMergedWays = new ArrayList<>(subwayAndTrain.getMergedWays());

        ArrayList<ReducedWay> allMergedTypeDiff = new ArrayList<>();


        for (ReducedWay way: allMergedWays){
            ArrayList<ReducedWay> typeDiffwaysCurrent = way.getTypeDiffWays();
            allMergedTypeDiff.addAll(typeDiffwaysCurrent);
        }


        ArrayList<ReducedWay> insertedSplits = null;
        double lengthIns = 0;

        int number = 0;

        for (ReducedWay wayToMerge: busWays){
            if (number++ > NumberHelper.number) break;
            double length = wayToMerge.length();
            if (length < 30){
                System.out.println("Way to small");
                break;
            }
            if (lengthIns > 20000){
                break;
            }
            System.out.println("Number ways: " + busWays.size());
            System.out.println("Length: " + lengthIns);
            wayToMerge.setId(number);
            System.out.println("Waytomerge id: " + wayToMerge.getId());


            insertedSplits = new WayManipulator().mergeWayOnNetworkBus(wayToMerge, allMergedTypeDiff, 0.73, 30);




            for (ReducedWay split: insertedSplits){
                split.setRouteType(wayToMerge.getRouteType());
                lengthIns += split.length();
            }

            allMergedTypeDiff.addAll(insertedSplits);
        }

        this.drawToGraphics.drawReducedWay(busWays.get(number-2), Color.BLACK, 20);
        if (insertedSplits == null) return;

        for (ReducedWay networkWay: allMergedTypeDiff){
            this.drawToGraphics.drawMultiTypeReducedway(networkWay, 6);
        }

*/


        /*

        subwayAndTrain.splitSegmentsAtIntersection();

        ArrayList<NetworkWay> snappedSTWays = subwayAndTrain.getSnappedWays();

        Collections.sort(snappedSTWays);





        for (PointBO point: Container.intersections){
            this.drawToGraphics.drawPointBO(point);
        }

       // this.drawToGraphics.drawNetworkWay(snappedSTWays.get(4), Color.cyan, 10);


        NetworkWay way1 = snappedSTWays.get(1);
        NetworkWay way4 = snappedSTWays.get(4);

        for (PointBO point1: way1.getNodes()){
            for (PointBO point4: way4.getNodes()){
                if (point1.getCoordinate().equals2D(point4.getCoordinate())){
                    System.out.println("Hi");
                }
            }
        }


*/


        /*
        ArrayList<ReducedWay> subWayAfterMergedAndDp = subwayNetwork.getDpSimplifiedWays();

        WayManipulator manipulator = new WayManipulator();


        ArrayList<ReducedWay> insertedSplits = new ArrayList<>();
        int number = 150;

        //this.drawToGraphics.drawReducedWay(trainAfterDp.get(number), Color.cyan, 12);

        // Ab index 51 wege zu klein zum mergen
        for (int i = 0; i<number+1; i++){
            if (trainAfterDp.get(i).length() < 70){
                System.out.println("Last loop index: "+ i);
                break;
            }

            if (i==24){
                System.out.println("hi");
            }

            insertedSplits = manipulator.mergeWayOnNetwork(trainAfterDp.get(i), subWayAfterMergedAndDp, 0.73, 70);

            for (ReducedWay split: insertedSplits){
                if (split.length()<100){
                    System.out.println("Take a look!");
                }
                split.setRouteType(trainAfterDp.get(i).getRouteType());
            }

            subWayAfterMergedAndDp.addAll(insertedSplits);
        }

        this.drawToGraphics.drawReducedWay(trainAfterDp.get(number), Color.cyan, 14);

        for (ReducedWay networkWay: subWayAfterMergedAndDp){
            this.drawToGraphics.drawMultiTypeReducedway(networkWay, 6);
        }

        if (insertedSplits.size() > 0){
            System.out.println("look");
        }

        for (ReducedWay way: insertedSplits){
            this.drawToGraphics.drawReducedWay(way, Color.BLACK, 20);
        }

*/


    }

    /**
     * Draws the original trimmed ways and the ways which are reduced in their shape with the douglas peucker algorithm
     * !!!!!! Wichtigste methode zum testen von merging
     *
     * @param type
     */
    private void drawDouglasPeukerComparison(RouteType type) {
        ArrayList<TrimmedWay> trimmedWays = this.dataHandler.getTrimmedWays(type);
        ArrayList<Node> wayNodes;
        WayManipulator manipulator = new WayManipulator();
        int buffersize = 70;
        // frechetsim < 0.69 Evidence 4, frechetSim < 0.62 (Evidence 5)
        double frechetSim = 0.73;
        /*for (TrimmedWay trimmedWay : trimmedWays) {
            wayNodes = getFullNodes(trimmedWay.getWaynodes());
            this.drawToGraphics.drawWay(wayNodes, Color.YELLOW, 3);
        }*/
        //this.drawToGraphics.drawWay(getFullNodes(trimmedWays.get(21).getWaynodes()), Color.YELLOW, 3);
        //this.drawToGraphics.drawWay(getFullNodes(trimmedWays.get(6).getWaynodes()), Color.YELLOW, 3);

        ArrayList<ReducedWay> reducedWays = this.dataHandler.getDouglasPeuckerWays(type);
        ArrayList<ReducedWay> copysOriginals = this.dataHandler.getDouglasPeuckerWays(type);
        Collections.sort(reducedWays);
        Collections.sort(copysOriginals);

        ArrayList<ReducedWay> alreadyInsertedWays = new ArrayList<>();
        ArrayList<ReducedWay> insertedWaysUnmerged = new ArrayList<>();
        int idLastWay = -1;

        ArrayList<ReducedWay> splitsToDrawLast = new ArrayList<>();

        int numberOuter = -1;
        double networkSize = 0;

        for (int i = 0; i < reducedWays.size(); i++) {
            numberOuter++;
            if (numberOuter > 8
            ) {
                break;
            }
            ReducedWay currentWay = reducedWays.get(i);
            int currentId = currentWay.getId();
            ReducedWay originalTemplate = copysOriginals.get(i);
            idLastWay = currentWay.getId();
            if (alreadyInsertedWays.isEmpty()) {
                insertedWaysUnmerged.add(currentWay);
                currentWay.splitSectionsNew();
                ArrayList<ReducedWay> splitSections = currentWay.getSplitSections();
                alreadyInsertedWays.addAll(splitSections);
                networkSize += addedNetworksize(splitSections);
                continue;
            }
            boolean cutted = true;//currentWay.cutAtSelfintersection();

            if (numberOuter == 2) {
                System.out.println(2);
            }

            /*double minDistance = Double.MAX_VALUE;
            for (ReducedWay insertedWay : insertedWaysUnmerged) {
                double distanceFrechet = manipulator.calulcateFrechetDistance(currentWay.getLineString(), insertedWay.getLineString());
                if (minDistance > distanceFrechet) {
                    minDistance = distanceFrechet;
                }
            }*/
            //System.out.println(minDistance);
            // If the current way has a smaller frechet distance to at least one other way continue with the next way and dont insert this way
            // if (minDistance < 100) {
            //   continue;
            //}
            System.out.println("Lastway that was merged: " + currentId);

            // if the whole way is inside the buffer of all ways just continue with the next way
            // Necessary because these ways can cause problems when merged completely to different ways
            // (EVIDENCE 3)


            currentWay.splitSectionsNew();
            ArrayList<ReducedWay> splits = currentWay.getSplitSections();
            ArrayList<ReducedWay> newSplits = new ArrayList<>();


            for (int ik = 70; ik > 0; ik -= 10) {
                for (ReducedWay split1 : splits) {
                    // Merge the way that is about to be inserted to all ways that are already inserted
                    for (ReducedWay insertedWay : alreadyInsertedWays) {
                        System.out.println("Merge: " + currentWay.getId() + " to way: " + insertedWay.getId());
                        manipulator.mergeWays(split1, insertedWay, frechetSim, ik);
                    }
                }

                newSplits.clear();
                for (ReducedWay split1 : splits) {
                    split1.splitSectionsNew();
                    newSplits.addAll(split1.getSplitSections());
                }

                splits.clear();
                splits.addAll(newSplits);
            }


            networkSize += addedNetworksize(splits);
            alreadyInsertedWays.addAll(splits);
            insertedWaysUnmerged.add(originalTemplate);
            splitsToDrawLast = splits;
        }


        /*int counter = 0;
        for (ReducedWay way: alreadyInsertedWays) {
            if (way.getId() == 84) {
                this.drawToGraphics.drawReducedWay(way, Color.RED, 10);
            }
        }*/


        for (
                ReducedWay way : alreadyInsertedWays) {
            if (way.getId() == idLastWay) {
                System.out.println("Lastway!!!");
                continue;
            }
            if (way.getWaySize() < 2) {
                System.out.println("eroor");
            }
            this.drawToGraphics.drawReducedWay(way, Color.RED, 10);
            Polygon buffer = (Polygon) way.getLineString().buffer(buffersize);
            LineString outerRing = buffer.getExteriorRing();
            Coordinate[] coords = outerRing.getCoordinates();
            this.drawToGraphics.drawWayByCoordinates(Color.BLUE, coords);

        }
        // Way der inserted wird original zustand
        this.drawToGraphics.drawReducedWay(copysOriginals.get(numberOuter - 1), Color.CYAN, 12);
        // Way nach merging
        //this.drawToGraphics.drawReducedWay(reducedWays.get(numberOuter-1), Color.YELLOW, 8);

        for (ReducedWay sectionsOfLastWay : splitsToDrawLast) {
            this.drawToGraphics.drawReducedWay(sectionsOfLastWay, Color.YELLOW, 7);
        }


        /*for (ReducedWay way: reducedWays){
            LineString linestring = way.getLineString();
            Polygon buffer = (Polygon) linestring.buffer(30);
            LineString outerRing = buffer.getExteriorRing();
            java.awt.Polygon polygon = new java.awt.Polygon()
                    int [] x = outerRing.get
            this.drawToGraphics.drawPolygon();
        }*/
        /*
        int numberWays = 0;
        for (ReducedWay way: reducedWays){
            this.drawToGraphics.drawReducedWay(way, Color.RED, 10);
            numberWays++;
        }
        int num = 0;
        for (ReducedWay way: reducedWays) {
            num++;
            if (num == numberWays) this.drawToGraphics.drawReducedWay(way, Color.YELLOW, 3);
        }*/



        /*LineString s1 = zielWay.getLineString();
        LineString werteString = werteWay.getLineString();
        Geometry buffer1 = s1.buffer(30);
        Geometry buffer2 = werteString.buffer(30);
        LineString intersecBuffer1Line2 = (LineString) buffer1.intersection(werteString);
        LineString intersecBuffer2Line1 = (LineString) buffer2.intersection(s1);
        ReducedWay way1 = new ReducedWay(intersecBuffer1Line2);
        ReducedWay way2 = new ReducedWay(intersecBuffer2Line1);
        this.drawToGraphics.drawReducedWay(way1, Color.MAGENTA, 10);
        this.drawToGraphics.drawReducedWay(way2, Color.WHITE, 3);
        FrechetSimilarityMeasure sm = new FrechetSimilarityMeasure();
        double distance = sm.measure(intersecBuffer2Line1.reverse(), intersecBuffer1Line2);
        System.out.println(distance);*/





        /*
        for (ReducedWay reducedWay : reducedWays) {
            now = reducedWay.getWayPoints().size()/2;
            int number = 0;
            this.drawToGraphics.drawReducedWay(reducedWay, Color.RED, 3);
            if (wayNumber == 51){
                LineString lineString = reducedWay.getLineString();
                BufferParameters params = new BufferParameters(0, BufferParameters.CAP_SQUARE, BufferParameters.JOIN_MITRE, 5);
                Polygon buffer = (Polygon) BufferOp.bufferOp(lineString, 20, params);
                LinearRing outer = buffer.getExteriorRing();
                Coordinate [] coordinatesOuter = outer.getCoordinates();
                this.drawToGraphics.drawWayByCoordinates(Color.CYAN, coordinatesOuter);

            }
            for (pl.luwi.series.reducer.Point point: reducedWay.getWayPoints()){
                if (number == now){
                    this.drawToGraphics.drawCoordinate((int) point.getX(), (int) point.getY(), wayNumber++);
                }
                number++;
            }
        }
        */

        /*this.drawToGraphics.drawReducedWay(reducedWays.get(59), Color.RED, 6);
        for (pl.luwi.series.reducer.Point point : reducedWays.get(59).getWayPoints()) {
            this.drawToGraphics.drawCoordinate((int) point.getX(), (int) point.getY(), wayNumber++, Color.cyan);
        }
        wayNumber = 0;
        this.drawToGraphics.drawReducedWay(reducedWays.get(67), Color.YELLOW, 6);
        for (pl.luwi.series.reducer.Point point : reducedWays.get(67).getWayPoints()) {
            this.drawToGraphics.drawCoordinate((int) point.getX(), (int) point.getY(), wayNumber++);
        }*/

        /*
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(100,300), new Coordinate(100,100), new Coordinate(200,100), new Coordinate(200, 200), new Coordinate(50,200)};
        //Coordinate[] coordinates1 = new Coordinate[]{new Coordinate(0,0.5), new Coordinate(1,1.5), new Coordinate(2,2.5), new Coordinate(3,3.5)};
        GeometryFactory gf = new GeometryFactory();
        Geometry line1 = gf.createLineString(coordinates);
        //Geometry line2 = gf.createLineString(coordinates1);
        FrechetSimilarityMeasure sm = new FrechetSimilarityMeasure();
        //double similarity = sm.measure(line1, line2);
        //Geometry nodedLine1 = LineStringSelfIntersections.getEndPoints(line1);
        //nodedLine1 = line1.union(nodedLine1);
        Polygon bufferLine1 = (Polygon) line1.buffer(50, 0, BufferOp.CAP_SQUARE);

        BufferParameters params = new BufferParameters(0, BufferParameters.CAP_SQUARE, BufferParameters.JOIN_MITRE, 10);
        Geometry buf = new BufferOp(line1, params).getResultGeometry(20);

        System.out.println("Hello");
        boolean hey = bufferLine1.contains(gf.createPoint(new Coordinate(1,1 )));
        String hi = bufferLine1.getGeometryType();
        LinearRing outerRing = ((Polygon) buf).getExteriorRing();
        LinearRing innerRing = ((Polygon) buf).getInteriorRingN(0);
        Coordinate [] coordinatesOuter = outerRing.getCoordinates();
        Coordinate[] coordinatesInner = innerRing.getCoordinates();
        this.drawToGraphics.drawWayByCoordinates(Color.YELLOW, coordinates);
        this.drawToGraphics.drawWayByCoordinates(Color.RED, coordinatesInner);
        this.drawToGraphics.drawWayByCoordinates(Color.CYAN, coordinatesOuter);*/
    }


    private void drawIntersectionsGeo(ArrayList<RouteType> types) {
        for (RouteType type : types) {
            drawIntersectionsPixel(type);
        }
    }

    /**
     * Draws all intersections for all ways of the given type.
     *
     * @param type
     */
    private void drawIntersectionsGeo(RouteType type) {
        ArrayList<Point> intersections = this.dataHandler.getIntersectionsGeo(type);
        this.intersectionsTest = intersections;
        for (Point point : intersections) {
            System.out.println("Intersection in: " + point.get_x_coord() + " | " + point.get_y_coord());
            this.drawToGraphics.drawGeoPoint(point);
        }
    }

    private void drawIntersectionsPixel(RouteType type) {
        ArrayList<Point> intersections = this.dataHandler.getIntersectionsPixel(type);
        for (Point point : intersections) {
            this.drawToGraphics.drawPixelPoint(point);
        }
    }


    /**
     * Substitutes a list of node ids with full nodes.
     *
     * @param nodeIds
     * @return full node list
     */
    private ArrayList<Node> getFullNodes(ArrayList<Long> nodeIds) {
        return this.dataHandler.getFullNodesForIds(nodeIds);
    }

}
