
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * This class opens a gui.
 * First the constrcutor takes an image and the boundingbox in lat, lon of the image which will be drawn to.
 * Secondly you can draw with the draw methods on the image.
 * Thirdly you need to call the showframe method to open the gui.
 */
public class ImageFrame extends JComponent{
    private JFrame frame;
    // Most likely the same size as the image from the mapbox api (1200x1200 currently)
    private int pixelWidth;
    private int pixelHeight;
    // This is the map which will be drawn to
    private BufferedImage imageToDisplay;
    // Graphics reference to imageToDisplay for drawing on that image
    private Graphics2D g;
    // Bounding box is calculated in ImageData from center Point
    private double[] boundingBoxOfImage;


    public ImageFrame(BufferedImage imageToDisplay, ImageData imageData){
        frame = new JFrame();
        pixelWidth = imageData.getPIXELWIDTH();
        pixelHeight = imageData.getPIXELHEIGHT();
        frame.setSize(pixelWidth, pixelHeight);
        frame.add(new JLabel(new ImageIcon(imageToDisplay)));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.imageToDisplay = imageToDisplay;
        this.g = (Graphics2D) this.imageToDisplay.getGraphics();
        // For smooth drawings
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // For smooth drawing of text
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        this.boundingBoxOfImage = imageData.getBoundingbox();
    }


    /**
     * The points will be drawn enumerated to the BufferedImage with the given style
     * @param points
     * @param color
     * @param font
     */
    public void drawPoints(ArrayList<Node> points, Color color, Font font){

        System.out.println("drawPoints() inside:");;
        g.setColor(color);
        g.setFont(font);
        // Draw each point on the map image
        int pointNumber = 1;
        for(Node point: points){
            // contains the x and y pixel coordinate for the point to draw
            double [] pixelXY = convertGeoToPixel(point.getLatitude(), point.getLongitude());
            //FIXME: pointNumber++
            g.drawString(Integer.toString(pointNumber), (int)pixelXY[0], (int)pixelXY[1]);
            System.out.println("Number: " + pointNumber++);
            for (Tag tag: point.getTags()){
                System.out.println(tag.getKey() + ": " + tag.getValue());
            }
            System.out.println("X: " + point.getLongitude() + " | Y: " + point.getLatitude());
        }
    }

    /**
     * The wys are drawn to the image through combining lines through every consecutive nodes
     * @param wayToDraw The way that contains the nodes which are the line path
     * @param color
     */
    public void drawWay(ArrayList<Node> wayToDraw, Color color){

        g.setColor(color);
        g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));


        //Start der linie
        Node currentNode;
        // All x pixel values
        int[] pixelsX = new int[wayToDraw.size()];
        // All y pixel values
        int[] pixelsY = new int[wayToDraw.size()];
        // x, y pixel
        double[] pixelCurrentNode;

        // Retrieve all x and y pixel values
        for(int i=0; i<wayToDraw.size(); i++){
            currentNode = wayToDraw.get(i);
            pixelCurrentNode = convertGeoToPixel(currentNode.getLatitude(), currentNode.getLongitude());
            pixelsX[i] = (int) pixelCurrentNode[0];
            pixelsY[i] = (int) pixelCurrentNode[1];
        }

        // Draw polyline for all nodes in the way
        g.drawPolyline(pixelsX, pixelsY, wayToDraw.size());

        // Set stroke to default value
        g.setStroke(new BasicStroke());
    }


    /**
     * This method does the mercator projection
     * @param latitude
     * @param longitude
     * @return xy pixel position
     */
    public double[] convertGeoToPixel(double latitude, double longitude) {

        double mapLonDelta = boundingBoxOfImage[2] - boundingBoxOfImage[0];
        double mapLatBottomDegree = boundingBoxOfImage[1] * Math.PI / 180;

        double x = (longitude - boundingBoxOfImage[0]) * (pixelWidth / mapLonDelta);

        latitude = latitude * Math.PI / 180;
        double worldMapWidth = ((pixelWidth / mapLonDelta) * 360) / (2 * Math.PI);
        double mapOffsetY = (worldMapWidth / 2 * Math.log((1 + Math.sin(mapLatBottomDegree)) / (1 - Math.sin(mapLatBottomDegree))));
        double y = pixelHeight - ((worldMapWidth / 2 * Math.log((1 + Math.sin(latitude)) / (1 - Math.sin(latitude)))) - mapOffsetY);

        double [] xy = {x,y};
        return xy;
    }

    /**
     * Open the gui to see the map with the drawings on it
     */
    public void setVisible(){
        this.frame.setVisible(true);
    }
}

