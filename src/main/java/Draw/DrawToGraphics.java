package Draw;

import DataContainer.ImageData;
import DataManipulation.RouteWays.Bentley_Ottman_Algorithmn.Point;
import HelperClasses.RouteTypeToWeightConverter;
import Types.*;
import Types.Stroke;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DrawToGraphics {
    private Graphics2D graphics2D;
    private Bound boundingboxOfImage;
    private ImageData imageData;

    public DrawToGraphics(Graphics2D graphics2D, ImageData imageData) {
        this.graphics2D = graphics2D;
        this.imageData = imageData;
        this.boundingboxOfImage = this.imageData.getBoundingBox();
    }

    public void clear() {
    }

    public void drawDirectedGraph(SimpleDirectedWeightedGraph graph) {

        Color color;




        Set<DefaultWeightedEdge> edges = graph.edgeSet();


        /*for (DefaultWeightedEdge edge : edges) {

            RouteType type = RouteTypeToWeightConverter.intToRouteType((int) graph.getEdgeWeight(edge));
            int width = 0;
            switch (type) {
                case BUS:
                    width = 9;
                    break;
                case TRAIN:
                    width = 9;
                    break;
                case SUBWAY:
                    width = 9;
                    break;
                case ST:
                    width = 11;
                    break;
                case STB:
                    width = 14;
                    break;
                case TB:
                    width = 11;
                    break;
                case SB:
                    width = 11;
                    break;
                default:
                    System.out.println("Error");
                    color = Color.cyan;
            }

            drawEdgeNew(graph, edge, Color.WHITE, width, 0);
        }*/

            for (DefaultWeightedEdge edge : edges) {

                RouteType type = RouteTypeToWeightConverter.intToRouteType((int) graph.getEdgeWeight(edge));
                switch (type) {
                    case BUS:
                        drawEdgeNew(graph, edge, Color.GREEN, 8, 0);
                        break;
                    case TRAIN:
                        drawEdgeNew(graph, edge, Color.BLUE, 8, 0);
                        break;
                    case SUBWAY:
                        drawEdgeNew(graph, edge, Color.RED, 8, 0);
                        break;
                    case ST:
                        drawEdgeNew(graph, edge, Color.RED, 6, -3);
                        drawEdgeNew(graph, edge, Color.BLUE, 6, 3);
                        break;
                    case STB:
                        drawEdgeNew(graph, edge, Color.RED, 4, -4);
                        drawEdgeNew(graph, edge, Color.BLUE, 4, 0);
                        drawEdgeNew(graph, edge, Color.GREEN, 4, 4);
                        break;
                    case TB:
                        drawEdgeNew(graph, edge, Color.BLUE, 6, -3);
                        drawEdgeNew(graph, edge, Color.GREEN, 6, 3);
                        break;
                    case SB:
                        drawEdgeNew(graph, edge, Color.RED, 6, -3);
                        drawEdgeNew(graph, edge, Color.GREEN, 6, 3);
                        break;
                    default:
                        System.out.println("Error");
                        color = Color.cyan;
                }

        }
    }

    public void drawGraph(SimpleWeightedGraph graph) {
        Color color;


        Set<DefaultWeightedEdge> edges = graph.edgeSet();

        for (DefaultWeightedEdge edge : edges) {

            RouteType type = RouteTypeToWeightConverter.intToRouteType((int) graph.getEdgeWeight(edge));
            switch (type) {
                case BUS:
                    color = Color.GREEN;
                    break;
                case TRAIN:
                    color = Color.BLUE;
                    break;
                case SUBWAY:
                    color = Color.RED;
                    break;
                case ST:
                    color = Color.YELLOW;
                    break;
                case STB:
                    color = Color.MAGENTA;
                    break;
                case TB:
                    color = Color.PINK;
                    break;
                case SB:
                    color = Color.WHITE;
                    break;
                default:
                    System.out.println("Error");
                    color = Color.cyan;
            }


            //drawEdge(graph, edge, color, 3);

        }
    }

    public void drawEdgeNew(Graph graph, DefaultWeightedEdge edge, Color color, int width, int offset){
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        int[] xPixelValues = new int[2];
        int[] yPixelValues = new int[2];
        int number = 0;
        int edgeSource = (int) graph.getEdgeSource(edge);
        int edgeTarget = (int) graph.getEdgeTarget(edge);
        int[] xySource = decodeNumber(edgeSource);
        int[] xyTarget = decodeNumber(edgeTarget);

        xPixelValues[0] = (int) xySource[0];
        yPixelValues[0] = (int) xySource[1];
        xPixelValues[1] = (int) xyTarget[0];
        yPixelValues[1] = (int) xyTarget[1];


        graphics2D.setColor(color);
        drawParallelLine(xPixelValues[0], xPixelValues[1], yPixelValues[0], yPixelValues[1], offset);
        //graphics2D.drawString("!", xPixelValues[0], yPixelValues[0]);
        //graphics2D.drawString("!", xPixelValues[1], yPixelValues[1]);
        graphics2D.setStroke(new BasicStroke());
    }

    public void drawEdge(Graph graph, DefaultWeightedEdge edge, Color color, int width) {

        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        int[] xPixelValues = new int[2];
        int[] yPixelValues = new int[2];
        int number = 0;
        int edgeSource = (int) graph.getEdgeSource(edge);
        int edgeTarget = (int) graph.getEdgeTarget(edge);
        int[] xySource = decodeNumber(edgeSource);
        int[] xyTarget = decodeNumber(edgeTarget);

        xPixelValues[0] = (int) xySource[0];
        yPixelValues[0] = (int) xySource[1];
        xPixelValues[1] = (int) xyTarget[0];
        yPixelValues[1] = (int) xyTarget[1];


        graphics2D.setColor(color);
        graphics2D.drawPolyline(xPixelValues, yPixelValues, 2);

        drawParallelLine(xPixelValues[0], xPixelValues[1], yPixelValues[0], yPixelValues[1], 3);
        // Set stroke back to basic stroke
        graphics2D.setColor(Color.GREEN);
        //graphics2D.drawString("!", xPixelValues[0], yPixelValues[0]);
        //graphics2D.drawString("!", xPixelValues[1], yPixelValues[1]);
        graphics2D.setStroke(new BasicStroke());

    }

    public void drawParallelLine(int x1, int x2, int y1, int y2, int offset) {
        double L = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

        // source line from given start and end coordinate
        LineSegment sourceLine = new LineSegment(new Coordinate(x1, y1), new Coordinate(x2, y2));
        int parallelDistance = offset;

// left from start- to end-point (note negative offset distance!)
        Coordinate startLeft = sourceLine.pointAlongOffset(0, -parallelDistance);
        Coordinate endLeft = sourceLine.pointAlongOffset(1, -parallelDistance);
        LineString leftLine = new GeometryFactory().createLineString(new Coordinate[]{startLeft, endLeft});
// right from start- to end-point (note positive offset distance!)
        Coordinate startRight = sourceLine.pointAlongOffset(0, parallelDistance);
        Coordinate endRight = sourceLine.pointAlongOffset(1, parallelDistance);
        LineString rightLine = new GeometryFactory().createLineString(new Coordinate[]{startRight, endRight});


        int[] xPixelValues = new int[2];
        int[] yPixelValues = new int[2];
        xPixelValues[0] = (int) startLeft.getX();
        xPixelValues[1] = (int) endLeft.getX();
        yPixelValues[0] = (int) startLeft.getY();
        yPixelValues[1] = (int) endLeft.getY();

        graphics2D.drawPolyline(xPixelValues, yPixelValues, 2);

    }


    public void drawNetworkWay(NetworkWay way, Color color, int width) {
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 50));
        graphics2D.setColor(Color.GREEN);
        int[] xPixelValues = new int[way.getWaySize()];
        int[] yPixelValues = new int[way.getWaySize()];
        int number = 0;
        for (int i = 0; i < way.getWaySize(); i++) {
            xPixelValues[i] = (int) way.getNodeAt(i).x();
            yPixelValues[i] = (int) way.getNodeAt(i).y();
            if (i == 0 || i == way.getWaySize() - 1) {
                //graphics2D.drawString("!", xPixelValues[i], yPixelValues[i]);
            }
        }
        graphics2D.setColor(color);
        graphics2D.drawPolyline(xPixelValues, yPixelValues, way.getWaySize());
        // Set stroke back to basic stroke
        graphics2D.setStroke(new BasicStroke());
    }

    public int[] decodeNumber(int num) {
        var w = (int) (Math.sqrt(8 * num + 1) - 1) / 2;
        var t = (int) (Math.pow(w, 2) + w) / 2;
        int y = (int) (num - t);
        int x = (int) (w - y);
        return new int[]{x, y};
    }

    public void drawReducedWayWithType(ReducedWay way, int width) {
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        graphics2D.setColor(Color.GREEN);
        int[] xPixelValues = new int[way.getWaySize()];
        int[] yPixelValues = new int[way.getWaySize()];
        int number = 0;
        for (int i = 0; i < way.getWaySize(); i++) {
            xPixelValues[i] = (int) way.getWayPoint(i).getX();
            yPixelValues[i] = (int) way.getWayPoint(i).getY();
            graphics2D.drawString("!", xPixelValues[i], yPixelValues[i]);
        }
        graphics2D.setColor(pickColorForType(way));
        graphics2D.drawPolyline(xPixelValues, yPixelValues, way.getWaySize());
        // Set stroke back to basic stroke
        graphics2D.setStroke(new BasicStroke());
    }

    /**
     * Examines with which route types parts of the way can be travelled and mark these different travable ways in different colors.
     *
     * @param way
     * @param width
     */
    public void drawMultiTypeReducedway(ReducedWay way, int width) {
        ArrayList<SegmentStroke> segments = way.getTypeDiffSegments();
        Color color;
        for (SegmentStroke currentSplit : segments) {
            width = 7;//(int) (Math.random()*10+1);
            if (currentSplit.getType() == RouteType.SUBWAY) {
                color = Color.RED;
            } else if (currentSplit.getType() == RouteType.ST) {
                color = Color.YELLOW;
            } else if (currentSplit.getType() == RouteType.TRAIN) {
                color = Color.BLUE;
            } else if (currentSplit.getType() == RouteType.STB) {
                color = Color.MAGENTA;
            } else if (currentSplit.getType() == RouteType.BUS) {
                color = Color.GREEN;
            } else if (currentSplit.getType() == RouteType.TB) {
                color = Color.pink;
            } else if (currentSplit.getType() == RouteType.SB) {
                color = Color.WHITE;
            } else {
                System.out.println("Color draw rror in drawtographics");
                color = Color.CYAN;
            }

            drawSegmentStroke(currentSplit, color, width);

        }
    }

    public void drawSegmentStroke(SegmentStroke segment, Color color, int width) {
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        graphics2D.setColor(Color.GREEN);
        int[] xPixelValues = new int[2];
        int[] yPixelValues = new int[2];
        int number = 0;
        xPixelValues[0] = (int) segment.getStartNode().getX();
        yPixelValues[0] = (int) segment.getStartNode().getY();
        xPixelValues[1] = (int) segment.getEndNode().getX();
        yPixelValues[1] = (int) segment.getEndNode().getY();
        graphics2D.setColor(color);
        graphics2D.drawPolyline(xPixelValues, yPixelValues, 2);
        // Set stroke back to basic stroke
        graphics2D.setStroke(new BasicStroke());
    }


    public Color pickColorForType(ReducedWay way) {
        switch (way.getRouteType()) {
            case SUBWAY:
                return Color.RED;
            case TRAIN:
                return Color.BLUE;
            default:
                return Color.WHITE;
        }
    }

    public void drawStroke(Stroke stroke, Color color, int width) {
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        graphics2D.setColor(Color.GREEN);
        ArrayList<Coordinate> coords = stroke.getNodes();
        int[] xPixelValues = new int[coords.size()];
        int[] yPixelValues = new int[coords.size()];
        int number = 0;
        for (int i = 0; i < coords.size(); i++) {
            xPixelValues[i] = (int) coords.get(i).getX();
            yPixelValues[i] = (int) coords.get(i).getY();
            if (number < 4) {
                graphics2D.drawString(Integer.toString(number++), xPixelValues[i], yPixelValues[i]);
                System.out.println(xPixelValues[i] + " | " + yPixelValues[i]);
            }
        }
        graphics2D.setColor(color);
        graphics2D.drawPolyline(xPixelValues, yPixelValues, coords.size());
        // Set stroke back to basic stroke
        graphics2D.setStroke(new BasicStroke());
    }

    public void drawReducedWay(ReducedWay way, Color color, int width) {
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        graphics2D.setColor(Color.GREEN);
        int[] xPixelValues = new int[way.getWaySize()];
        int[] yPixelValues = new int[way.getWaySize()];
        int number = 0;
        for (int i = 0; i < way.getWaySize(); i++) {
            xPixelValues[i] = (int) way.getWayPoint(i).getX();
            yPixelValues[i] = (int) way.getWayPoint(i).getY();
            //graphics2D.drawString("!", xPixelValues[i], yPixelValues[i]);
        }
        graphics2D.setColor(color);
        graphics2D.drawPolyline(xPixelValues, yPixelValues, way.getWaySize());
        // Set stroke back to basic stroke
        graphics2D.setStroke(new BasicStroke());
    }

    public void drawWayByCoordinates(Color color, Coordinate[] coordinates) {
        graphics2D.setColor(color);
        graphics2D.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] xPixelValues = new int[coordinates.length];
        int[] yPixelValues = new int[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            xPixelValues[i] = (int) coordinates[i].getX();
            yPixelValues[i] = (int) coordinates[i].getY();
        }
        graphics2D.drawPolyline(xPixelValues, yPixelValues, coordinates.length);
        // Set stroke back to basic stroke
        graphics2D.setStroke(new BasicStroke());
    }


    public void drawPolygon(Polygon polygon) {
        this.graphics2D.drawPolygon(polygon);
    }

    public void drawTrimmedWay(TrimmedWay way, Color color, int width) {
        graphics2D.setColor(color);
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] xPixelValues = new int[way.getWaySize()];
        int[] yPixelValues = new int[way.getWaySize()];
        for (int i = 0; i < way.getWaySize(); i++) {
            xPixelValues[i] = (int) way.getPixelNode(i).getX();
            yPixelValues[i] = (int) way.getPixelNode(i).getY();
        }
        graphics2D.drawPolyline(xPixelValues, yPixelValues, way.getWaySize());
        // Set stroke back to basic stroke
        graphics2D.setStroke(new BasicStroke());
    }

    public void drawCoordinate(int x, int y, int number) {
        graphics2D.setColor(Color.YELLOW);
        graphics2D.drawString(Integer.toString(number), x, y);
    }

    public void drawCoordinate(int x, int y, int number, Color color) {
        this.graphics2D.setColor(color);
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        graphics2D.drawString(Integer.toString(number), x, y);
    }

    public void drawPointBO(PointBO intersec) {
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 40));
        Color color;
        String string;
        if (intersec.getType() == RouteType.SUBWAY) {
            color = Color.WHITE;
            string = "+";
        } else if (intersec.getType() == RouteType.TRAIN) {
            color = Color.GREEN;
            string = "!";
        } else {
            color = Color.GREEN;
            string = "!";
        }
        graphics2D.setColor(color);
        graphics2D.drawString(string, (int) intersec.x(), (int) intersec.y());
    }


    /**
     * Way is drawn through a line connection through its consecutive nodes.
     *
     * @param nodesOnWay
     * @param color
     * @param width      of the line
     */
    public void drawWay(ArrayList<Node> nodesOnWay, Color color, int width) {
        graphics2D.setColor(color);
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int[] xPixelValues = new int[nodesOnWay.size()];
        int[] yPixelValues = new int[nodesOnWay.size()];
        int[] xyPixelCurrentNode;
        Node currentNode;
        for (int i = 0; i < nodesOnWay.size(); i++) {
            currentNode = nodesOnWay.get(i);
            xyPixelCurrentNode = convertGeoNodeToPixelValues(currentNode);
            xPixelValues[i] = xyPixelCurrentNode[0];
            yPixelValues[i] = xyPixelCurrentNode[1];
        }

        graphics2D.drawPolyline(xPixelValues, yPixelValues, nodesOnWay.size());
        // Set stroke back to basic stroke
        graphics2D.setStroke(new BasicStroke());
    }

    /**
     * Draws a list of nodes to the {@link Graphics2D} object.
     * The first node is drawn with the number 1 and the last node with the number nodes.size().
     *
     * @param nodes
     * @param color
     * @param font
     */
    public void drawNodes(HashSet<Node> nodes, Color color, Font font) {
        graphics2D.setColor(color);
        graphics2D.setFont(font);
        int nodeNumber = 1;
        for (Node node : nodes) {
            drawNode(node, nodeNumber++);
        }
        System.out.println(nodeNumber - 1 + " stops has been drawn");
    }

    /**
     * Draws a node though its nodeNumber to the {@link Graphics2D} object of this class.
     *
     * @param node
     * @param nodeNumber number of the node on the image
     */
    public void drawNode(Node node, int nodeNumber) {
        graphics2D.setColor(Color.YELLOW);
        int[] pixelXY = convertGeoNodeToPixelValues(node);
        graphics2D.drawString(Integer.toString(nodeNumber), pixelXY[0], pixelXY[1]);
    }

    public void drawStopPoint(Coordinate c, int routeType, int number) {
        Font font = new Font("TimesRoman", Font.PLAIN, 15);
        int recWidth = 25;
        int recHeight = 15;
        int xRec = (int) c.getX() - recWidth / 2;
        int yRec = (int) c.getY() - recHeight / 2;
        Rectangle rec = new Rectangle(xRec, yRec, recWidth, recHeight);

        graphics2D.setColor(Color.WHITE);
        graphics2D.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.fillRect(xRec, yRec, recWidth, recHeight);
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawRect(xRec, yRec, recWidth, recHeight);
        drawCenteredString(font, rec, Integer.toString(number));
        System.out.println("id: " + Integer.toString(number) + " ( " + (int) c.getX() + " | " + (int) c.getY() + " )" + "    |    realID: " + encodeNumbers((int) c.getX(), (int) c.getY()));
    }

    public void drawCenteredString(Font font, Rectangle rect, String text) {
        // Get the FontMetrics
        FontMetrics metrics = graphics2D.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent() + 1;
        // Set the font
        graphics2D.setFont(font);
        // Draw the String
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawString(text, x, y);
    }

    private int encodeNumbers(int x, int y) {
        return (((x + y) * (x + y + 1)) / 2) + y;
    }

    public void drawPixelNode(PixelNode node, int nodeNumber) {
        graphics2D.setColor(Color.YELLOW);
        graphics2D.drawString(Integer.toString(nodeNumber), (int) node.getX(), (int) node.getY());
    }

    public void drawNode(Node node) {
        graphics2D.setColor(Color.YELLOW);
        int[] pixelXY = convertGeoNodeToPixelValues(node);
        graphics2D.drawString(Double.toString(node.getId()), pixelXY[0], pixelXY[1]);
    }

    public void drawPixelPoint(Point point) {
        graphics2D.setColor(Color.GREEN);
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        graphics2D.drawString("!", (int) point.get_x_coord(), (int) point.get_y_coord());
    }

    public void drawGeoPoint(Point point) {
        graphics2D.setColor(Color.GREEN);
        graphics2D.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        graphics2D.drawString("!", convertGeoPointToPixelValues(point)[0], convertGeoPointToPixelValues(point)[1]);
    }

    public int[] convertGeoPointToPixelValues(Point point) {
        return convertGeoToPixel(point.get_y_coord(), point.get_x_coord(), imageData.getPIXELWIDTH(), imageData.getPIXELHEIGHT(),
                boundingboxOfImage);
    }

    /**
     * Returns the x and y pixel value for a node.
     * Takes the {@link ImageData} and boundingboxOfImage objects into account.
     *
     * @param node
     * @return X, Y pixel value
     */
    private int[] convertGeoNodeToPixelValues(Node node) {
        return convertGeoToPixel(node.getLatitude(), node.getLongitude(), imageData.getPIXELWIDTH(),
                imageData.getPIXELHEIGHT(), boundingboxOfImage);
    }

    /**
     * This method does the mercator projection
     *
     * @param latitude           of the node
     * @param longitude          of the node
     * @param pixelWidth         of the image
     * @param pixelHeight        of the image
     * @param boundingboxOfImage (borders of the image in latitude and longitude)
     * @return X, Y pixel value
     */
    public static int[] convertGeoToPixel(double latitude, double longitude, int pixelWidth, int pixelHeight, Bound boundingboxOfImage) {

        double mapLonDelta = boundingboxOfImage.getRight() - boundingboxOfImage.getLeft();
        double mapLatBottomDegree = boundingboxOfImage.getBottom() * Math.PI / 180;

        double x = (longitude - boundingboxOfImage.getLeft()) * (pixelWidth / mapLonDelta);

        latitude = latitude * Math.PI / 180;
        double worldMapWidth = ((pixelWidth / mapLonDelta) * 360) / (2 * Math.PI);
        double mapOffsetY = (worldMapWidth / 2 * Math.log((1 + Math.sin(mapLatBottomDegree)) / (1 - Math.sin(mapLatBottomDegree))));
        double y = pixelHeight - ((worldMapWidth / 2 * Math.log((1 + Math.sin(latitude)) / (1 - Math.sin(latitude)))) - mapOffsetY);

        int[] xy = {(int) x, (int) y};
        return xy;
    }

}
