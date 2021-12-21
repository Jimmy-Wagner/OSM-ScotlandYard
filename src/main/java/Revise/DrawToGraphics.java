package Revise;

import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DrawToGraphics {
    private Graphics2D graphics2D;
    private Bound boundingboxOfImage;
    private ImageData imageData;

    public DrawToGraphics(Graphics2D graphics2D, ImageData imageData){
        this.graphics2D = graphics2D;
        this.imageData = imageData;
        this.boundingboxOfImage = this.imageData.getBoundingBox();
    }


    /**
     * Way is drawn through a line connection through its consecutive nodes.
     * @param nodesOnWay
     * @param color
     * @param width of the line
     */
    public void drawWay(ArrayList<Node> nodesOnWay, Color color, int width){
        graphics2D.setColor(color);
        graphics2D.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int [] xPixelValues = new int[nodesOnWay.size()];
        int [] yPixelValues = new int[nodesOnWay.size()];
        int [] xyPixelCurrentNode;
        Node currentNode;
        for (int i= 0; i<nodesOnWay.size(); i++){
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
     * @param nodes
     * @param color
     * @param font
     */
    public void drawNodes(HashSet<Node> nodes, Color color, Font font){
        graphics2D.setColor(color);
        graphics2D.setFont(font);
        int nodeNumber = 1;
        for (Node node: nodes){
            drawNode(node, nodeNumber++);
        }
        System.out.println(nodeNumber + " stops has been drawn");
    }

    /**
     * Draws a node though its nodeNumber to the {@link Graphics2D} object of this class.
     * @param node
     * @param nodeNumber number of the node on the image
     */
    private void drawNode(Node node, int nodeNumber){
        int [] pixelXY = convertGeoNodeToPixelValues(node);
        graphics2D.drawString(Integer.toString(nodeNumber), pixelXY[0], pixelXY[1]);
    }

    /**
     * Returns the x and y pixel value for a node.
     * Takes the {@link ImageData} and boundingboxOfImage objects into account.
     * @param node
     * @return X, Y pixel value
     */
    private int[] convertGeoNodeToPixelValues(Node node){
        return convertGeoToPixel(node.getLatitude(), node.getLongitude(), imageData.getPIXELWIDTH(),
                imageData.getPIXELHEIGHT(), boundingboxOfImage);
    }

    /**
     * This method does the mercator projection
     * @param latitude of the node
     * @param longitude of the node
     * @param pixelWidth of the image
     * @param pixelHeight of the image
     * @param boundingboxOfImage (borders of the image in latitude and longitude)
     * @return X, Y pixel value
     */
    private int[] convertGeoToPixel(double latitude, double longitude, int pixelWidth, int pixelHeight, Bound boundingboxOfImage) {

        double mapLonDelta = boundingboxOfImage.getRight() - boundingboxOfImage.getLeft();
        double mapLatBottomDegree = boundingboxOfImage.getBottom() * Math.PI / 180;

        double x = (longitude - boundingboxOfImage.getLeft()) * (pixelWidth / mapLonDelta);

        latitude = latitude * Math.PI / 180;
        double worldMapWidth = ((pixelWidth / mapLonDelta) * 360) / (2 * Math.PI);
        double mapOffsetY = (worldMapWidth / 2 * Math.log((1 + Math.sin(mapLatBottomDegree)) / (1 - Math.sin(mapLatBottomDegree))));
        double y = pixelHeight - ((worldMapWidth / 2 * Math.log((1 + Math.sin(latitude)) / (1 - Math.sin(latitude)))) - mapOffsetY);

        int [] xy = {(int) x,(int) y};
        return xy;
    }

}
