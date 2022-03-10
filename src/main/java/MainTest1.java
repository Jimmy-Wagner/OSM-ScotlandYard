import DataManipulation.BO.IntersectionFinder;
import DataManipulation.BO.IntersectionListener;
import DataManipulation.BO.PointFactoryBO;
import DataManipulation.RouteWays.Bentley_Ottman_Algorithmn.Point;
import DataManipulation.WayManipulator;
import Test.DrawTests;
import Types.*;
import bentleyottmann.BentleyOttmann;
import bentleyottmann.IPoint;
import bentleyottmann.ISegment;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainTest1 {
    public static void main(String[] args) {
        /*BentleyOttmann bo = new BentleyOttmann(PointBO::new);
        PointBO p1 = new PointBO(0,0);
        PointBO p2 = new PointBO(1,1);
        PointBO p3 = new PointBO(0,1);
        PointBO p4 = new PointBO(1,0);
        SegmentBO seg1 = new SegmentBO(p1, p2, 0);
        SegmentBO seg2 = new SegmentBO(p3, p4, 1);
        ArrayList<ISegment> segments = new ArrayList<>();
        segments.add(seg1);
        segments.add(seg2);
        bo.addSegments(segments);
        IntersectionListener listener = new IntersectionListener();
        bo.setListener(listener);
        bo.findIntersections();
        List<IPoint> intersectionPoints = bo.intersections();
        System.out.println("Hello");
        HashMap<IPoint, ISegment[]> mapping = listener.getIntersectionPointsWithSegments();
        System.out.println("hello");
        PointBO bo1 = new PointBO(1,2);
        makePoint(bo1);
        System.out.println("Hello");*/

        /*DrawTests test = new DrawTests();
        ArrayList<PixelNode> points = new ArrayList<>();
        PixelNode n1 = new PixelNode(100, 100);
        PixelNode n2 = new PixelNode(100, 800);
        points.add(n1);
        points.add(new PixelNode(100, 150));
        points.add(n2);
        ReducedWay brown = new ReducedWay(points, 0);
        points.clear();

        n1 = new PixelNode(700, 100);
        n2 = new PixelNode(700, 800);
        points.add(n1);
        points.add(n2);
        ReducedWay yellow = new ReducedWay(points, 1);
        points.clear();

        points.add(new PixelNode(250, 80));
        points.add(new PixelNode(150, 100));
        n1 = new PixelNode(120, 100);
        n2 = new PixelNode(120, 350);
        PixelNode n3 = new PixelNode(680, 400);
        PixelNode n4 = new PixelNode(680, 750);
        points.add(n1);
        points.add(n2);
        points.add(new PixelNode(500, 500));
        points.add(n3);
        points.add(n4);
        ReducedWay greenRed = new ReducedWay(points, 2);

        test.drawReducedWay(brown, Color.BLACK, 5);
        test.drawReducedWay(yellow, Color.YELLOW, 5);
        test.drawReducedWay(greenRed, Color.GREEN, 5);


        WayManipulator manipulator = new WayManipulator();
        manipulator.mergeWays(greenRed, brown, 0.73, 30);
        manipulator.mergeWays(greenRed, yellow, 0.73, 30);
        greenRed.splitSectionsNew();
        ArrayList<ReducedWay> splits = greenRed.getSplitSections();
        for (ReducedWay split: splits){
            test.drawReducedWay(split, Color.BLUE, 10);
        }


        test.setVisible();*/
        /*PixelNode hi = new PixelNode(1,1);
        ArrayList<PixelNode> n1 = new ArrayList<>();
        n1.add(hi);
        ReducedWay way = new ReducedWay(n1, 1);*/
        /*PointBO p = new PointBO(2.2,1);
        PointBO p1 = new PointBO(400.3, 300.6);
        ArrayList<PointBO> list = new ArrayList<>();
        list.add(p);
        list.add(p1);
        NetworkWay way = new NetworkWay(list);
        list.clear();
        list.add(new PointBO(2, 300));
        list.add(new PointBO(400, 100));
        NetworkWay way1 = new NetworkWay(list);
        IntersectionFinder hi= new IntersectionFinder();

        HashMap<IPoint, ISegment[]> map = hi.findIntersectionsNetwork(nets);*/


        /*WayManipulator manipulator = new WayManipulator();
        PointBO p = new PointBO(100,100);
        PointBO p7 = new PointBO(300, 300);
        ArrayList<PointBO> list = new ArrayList<>();
        list.add(p);
        list.add(p7);
        list.add(new PointBO(305, 305));

        NetworkWay way = new NetworkWay(list, 0);
        ArrayList<PointBO> list2 = new ArrayList<>();
        list2.add(new PointBO(100, 200));
        list2.add(new PointBO(200, 100));
        NetworkWay way1 = new NetworkWay(list2, 1);
        ArrayList<NetworkWay> nets = new ArrayList<>();
        nets.add(way);
        nets.add(way1);

        ArrayList<PointBO> list3 = new ArrayList<>();
        list3.add(new PointBO(300, 100));
        list3.add(new PointBO(100, 300));
        NetworkWay way2 = new NetworkWay(list3, 2);
        nets.add(way2);*/

        /*ArrayList<PointBO> list4 = new ArrayList<>();
        list4.add(new PointBO(195, 105));
        list4.add(new PointBO(100, 300));
        NetworkWay way3 = new NetworkWay(list4, 3);
        nets.add(way3);*/

        //manipulator.mergeEndsToSegments(nets);

        // Split the ways where there segments were splitted
        //ArrayList<NetworkWay> splittedWays = manipulator.splitWaysAtIntersections(networkways);

        /*
        PointBO point0 = new PointBO(0,1);
        PointBO point1 = new PointBO(1,1);
        PointBO point2 = new PointBO(2,1);
        SegmentBO segment1 = new SegmentBO(point0, point1, null, 0, null);
        SegmentBO segment2 = new SegmentBO(point1, point2, null, 1, null);

        PointBO point3 = new PointBO(1,0);
        PointBO point4 = new PointBO(1,1);
        PointBO point5 = new PointBO(1,2);
        SegmentBO segment3 = new SegmentBO(point3, point4, null, 0, null);
        SegmentBO segment4 = new SegmentBO(point4, point5, null, 0, null);


        ArrayList<ISegment> segments = new ArrayList<>();
        segments.add(segment1);
        segments.add(segment2);
        segments.add(segment3);
        segments.add(segment4);
        for (ISegment seg: segments){
            ((SegmentBO) seg).initializeForBO();
        }
        BentleyOttmann bo = new BentleyOttmann(PointBO::new);
        bo.addSegments(segments);
        bo.findIntersections();
        List<IPoint> intersecs =  bo.intersections();


        PointBO p = new PointBO(1200,1200);

        PointBO [] p1 = new PointBO[3000000];
        p1[0] = p;*/

        /*SegmentStroke stroke1 = new SegmentStroke();
        stroke1.setStartNode(new Coordinate(386,544));
        stroke1.setEndNode(new Coordinate(401,547));

        SegmentStroke stroke2 = new SegmentStroke();
        stroke2.setStartNode(new Coordinate(386,544));
        stroke2.setEndNode(new Coordinate(395,541));

        double angle2 = stroke2.angleNew(stroke1);

        System.out.println(angle2);*/

        System.out.println(encodeNumbers(284, 882));


    }

    private static int encodeNumbers(int x, int y) {
        return (((x + y) * (x + y + 1)) / 2) + y;
    }
}
