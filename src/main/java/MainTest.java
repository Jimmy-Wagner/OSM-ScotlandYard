import APIs.GeoApiCaller;
import APIs.ImageApiCaller;
import DataContainer.ImageData;
import DataContainer.OsmDataContainer;
import DataManipulation.OsmDataHandler;
import DataManipulation.RouteWays.Bentley_Ottman_Algorithmn.BentleyOttmann;
import DataManipulation.RouteWays.Bentley_Ottman_Algorithmn.Segment;
import DataManipulation.StrokeBuilder;
import DataManipulation.WayManipulator;
import DataReader.PbfFileReader;
import HelperClasses.AngleHelper;
import Test.DrawTests;
import Types.*;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KDTree;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeySizeException;
import org.locationtech.jts.algorithm.distance.DiscreteFrechetDistance;
import org.locationtech.jts.algorithm.match.FrechetSimilarityMeasure;
import org.locationtech.jts.algorithm.match.SimilarityMeasure;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jtsexample.technique.LineStringSelfIntersections;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import pl.luwi.series.reducer.Line;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.SeriesReducer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainTest {
    public static void main(String[] args) throws ParseException {

        /*Coordinate[] coordinates = new Coordinate[]{new Coordinate(0,-1), new Coordinate(0,2), new Coordinate(2,2), new Coordinate(2, 0), new Coordinate(-1,0)};
        Coordinate[] coordinates1 = new Coordinate[]{new Coordinate(1,2.5), new Coordinate(1,3)};
        GeometryFactory gf = new GeometryFactory();
        Geometry line1 = gf.createLineString(coordinates);
        Geometry line2 = gf.createLineString(coordinates1);
        FrechetSimilarityMeasure sm = new FrechetSimilarityMeasure();
        //double similarity = sm.measure(line1, line2);
        //Geometry nodedLine1 = LineStringSelfIntersections.getEndPoints(line1);
        //nodedLine1 = line1.union(nodedLine1);
        Polygon bufferLine1 = (Polygon) line1.buffer(0.5, 1, BufferOp.CAP_SQUARE);
        String type1 = bufferLine1.getGeometryType();
        Polygon bufferLine2 = (Polygon) line2.buffer(0.5, 1, BufferOp.CAP_SQUARE);
        Geometry intersection1 = bufferLine1.intersection(line2);
        Geometry intersection2 = bufferLine2.intersection(line1);
        System.out.println("Hello");
        boolean hey = bufferLine1.contains(gf.createPoint(new Coordinate(1,1 )));
        String hi = bufferLine1.getGeometryType();
        LinearRing outerRing = bufferLine1.getExteriorRing();
        LinearRing innerRing = bufferLine1.getInteriorRingN(0);

        /*two dimensions
        KDTree<Integer> kdTree = new KDTree<Integer>(2);
        try {
            kdTree.insert(new double[]{1,1}, 1);
            kdTree.insert(new double[]{2,2}, 2);
            kdTree.insert(new double[]{1,4}, 1);
            kdTree.insert(new double[]{2,4}, 2);
            kdTree.insert(new double[]{1,0}, 1);
            kdTree.insert(new double[]{2,-4}, 2);
        } catch (KeySizeException e) {
            e.printStackTrace();
        } catch (KeyDuplicateException e) {
            e.printStackTrace();
        }
        int i;
        try {
            i = kdTree.nearest(new double[]{1.3, 1.3});
        } catch (KeySizeException e) {
            e.printStackTrace();
        }
        try {
            kdTree.edit(new double[]{1,0}, new MyEditor(5));
        } catch (KeySizeException e) {
            e.printStackTrace();
        } catch (KeyDuplicateException e) {
            e.printStackTrace();
        }
        System.out.println("hi");

*/

       /* ArrayList<LineSegment> buildSegments = buildSegments(new Coordinate[]{new Coordinate(0,0),
        new Coordinate(1,1),
        new Coordinate(2,2)}, 2);
        System.out.println(buildSegments);*/
        // Distance 0,71 as sample

        //LINESTRING (2.5 2.2, 2.5 2, 1 2, 0.7999999999999998 2.2)
        //LINESTRING (0.8 0.8, 1 1, 2 1, 3 1, 3.2 0.8)


        DrawTests test = new DrawTests();
        LineString line1 = (LineString) new WKTReader().read("LINESTRING (386 544, 401 547)");
        LineString line2 = (LineString) new WKTReader().read("LINESTRING (386 544, 395 541)");
        Polygon buffer1 = (Polygon) line1.buffer(40);
        Polygon buffer2 = (Polygon) line2.buffer(40);
        Geometry intersBuffer1Line2 = buffer1.intersection(line2);
        Geometry intersBuffer2Line1 = buffer2.intersection(line1);
        LineString buffer1Outerring = buffer1.getExteriorRing();
        LineString buffer2Outerring = buffer2.getExteriorRing();

        test.drawReducedWay(new ReducedWay(buffer1Outerring), Color.CYAN, 4);
        test.drawReducedWay(new ReducedWay(buffer2Outerring), Color.GREEN, 4);
        test.drawReducedWay(new ReducedWay(line1), Color.BLUE, 4);
        test.drawReducedWay(new ReducedWay(line2), Color.YELLOW, 4);
        test.setVisible();

        /*ArrayList<NetworkWay> ways = new ArrayList<>();
        ArrayList<PointBO> points1 = new ArrayList<>();
        PointBO p1 = new PointBO(1,1);
        PointBO p2 = new PointBO(8,8);
        points1.add(p1);
        points1.add(p2);
        ways.add(new NetworkWay(points1, 1));

        points1.clear();
        p1 = new PointBO(0.5,2);
        p2 = new PointBO(2,0.5);
        points1.add(p1);
        points1.add(p2);
        ways.add(new NetworkWay(points1, 2));*/

        /*points1.clear();
        p1 = new PointBO(1.5,3);
        p2 = new PointBO(3,1.5);
        points1.add(p1);
        points1.add(p2);
        ways.add(new NetworkWay(points1, 3));

        points1.clear();
        p1 = new PointBO(2,4);
        p2 = new PointBO(4,2);
        points1.add(p1);
        points1.add(p2);
        ways.add(new NetworkWay(points1, 4));

        points1.clear();
        p1 = new PointBO(3,2);
        p2 = new PointBO(4,3);
        points1.add(p1);
        points1.add(p2);
        ways.add(new NetworkWay(points1, 100));*/




        /*
        ArrayList<PixelNode> schwarzPoints = new ArrayList<>();
        PixelNode s1 = new PixelNode(1,1);
        PixelNode s2 = new PixelNode(3,1);
        PixelNode s3 = new PixelNode(4,3);
        schwarzPoints.add(s1);
        schwarzPoints.add(s2);
        schwarzPoints.add(s3);

        ArrayList<PixelNode> redPoints = new ArrayList<>();
        PixelNode r1 = new PixelNode(3,1);
        PixelNode r2 = new PixelNode(7, 1);
        redPoints.add(r1);
        redPoints.add(r2);

        ArrayList<PixelNode> blauPoints = new ArrayList<>();
        PixelNode b1 = new PixelNode(4,3);
        PixelNode b2 = new PixelNode(7,3);
        blauPoints.add(b1);
        blauPoints.add(b2);

        ArrayList<PixelNode> greenPoints = new ArrayList<>();
        PixelNode g1 = new PixelNode(4,3);
        PixelNode g2 = new PixelNode(4,10);
        PixelNode g3 = new PixelNode(5,10);
        greenPoints.add(g3);
        greenPoints.add(g2);
        greenPoints.add(g1);


        ArrayList<PixelNode> pinkPoints = new ArrayList<>();
        PixelNode p1 = new PixelNode(1,1);
        PixelNode p2 = new PixelNode(0,1);
        pinkPoints.add(p1);
        pinkPoints.add(p2);

        ArrayList<TrimmedWay> ways = new ArrayList<>();
        TrimmedWay schwarz = new TrimmedWay(schwarzPoints);
        TrimmedWay red = new TrimmedWay(redPoints);
        TrimmedWay blau = new TrimmedWay(blauPoints);
        TrimmedWay green = new TrimmedWay(greenPoints);
        TrimmedWay pink = new TrimmedWay(pinkPoints);
        ways.add(schwarz);
        ways.add(red);
        ways.add(blau);
        ways.add(green);
        ways.add(pink);

        StrokeBuilder builder = new StrokeBuilder(ways);
        builder.buildStrokes();*/

        /*
        SegmentStroke stroke1 = new SegmentStroke();
        stroke1.setStartNode(new Coordinate(1,1));
        stroke1.setEndNode(new Coordinate(2,1));

        SegmentStroke stroke2 = new SegmentStroke();
        stroke2.setStartNode(new Coordinate(1,1));
        stroke2.setEndNode(new Coordinate(0,-1));

        double angle2 = stroke2.angleNew(stroke1);

        System.out.println(angle2);
*/

    }


    public static void changeObject(PixelNode node){
        node.setPixelY(100);
    }

    public static ArrayList<LineSegment> buildSegments(Coordinate[] coordinates, int index) {
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
            segment = new LineSegment(coordinates[index], coordinates[index + 1]);
            segments.add(segment);
            segment = new LineSegment(coordinates[index - 1], coordinates[index]);
        }
        segments.add(segment);
        return segments;
    }

    public static void change(LineString line) {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 2)};
        line.setSRID(100);
    }

    public static void change1(ArrayList<Integer> hi) {
        hi.add(100);
    }
}
