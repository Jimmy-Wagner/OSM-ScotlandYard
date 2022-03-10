package DataPresentation;

import DataManipulation.OsmDataHandler;
import Types.NumberHelper;
import Types.ReducedWay;
import Types.RouteType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class opens a gui.
 * First the constructor takes an image and the size of the frame which should be larger than the image size.
 * Secondly you need to call the showframe method to open the gui.
 */
public class ImageFrame extends JComponent implements KeyListener {
    private JFrame frame;
    // This is the map which will be drawn to
    private BufferedImage mapImage;
    // Graphics reference to imageToDisplay for drawing on that image
    private Graphics2D g;
    private OsmDataHandler handler;
    private ArrayList<ReducedWay> ways;
    private ArrayList<ReducedWay> insertedWays = new ArrayList<>();
    private int indexOfNextWay = 0;
    private JLabel label;


    public ImageFrame(BufferedImage imageToDisplay, int pixelWidth, int pixelHeight, OsmDataHandler handler) {
        frame = new JFrame();
        frame.setSize(pixelWidth, pixelHeight);
        label = new JLabel(new ImageIcon(imageToDisplay));
        frame.add(label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mapImage = imageToDisplay;
        this.handler = handler;
        this.g = (Graphics2D) this.mapImage.getGraphics();
        this.ways = this.handler.getDouglasPeuckerWays(RouteType.SUBWAY);
        Collections.sort(ways);
        // For smooth drawings
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // For smooth drawing of text
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public void saveImage(){
        File outputfile = new File("/Users/jimmy/Desktop/REFINE MAP/"+ 3 +")image.jpg");
        try {
            ImageIO.write(mapImage, "jpg", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * replace old mapImage with new mapImage
     *
     * @param mapImage
     */
    public void changeMapImage(BufferedImage mapImage) {
        frame.removeAll();
        frame.add(new JLabel(new ImageIcon(mapImage)));
        this.mapImage = mapImage;
    }

    /**
     * Open the gui to see the map with the drawings on it
     */
    public void setVisible() {
        this.frame.setVisible(true);
    }

    public Graphics2D getG() {
        return g;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("Hello");
        insertNextWay();
    }

    public void insertNextWay() {
        g.clearRect(0,0,200,200);
    }


    public void drawReducedWay(ReducedWay way, Color color, int width){
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        g.setColor(Color.GREEN);
        int [] xPixelValues = new int[way.getWaySize()];
        int [] yPixelValues = new int[way.getWaySize()];
        int number = 0;
        for (int i=0; i<way.getWaySize(); i++){
            xPixelValues[i] = (int) way.getWayPoint(i).getX();
            yPixelValues[i] = (int) way.getWayPoint(i).getY();
            //graphics2D.drawString(Integer.toString(number++), xPixelValues[i], yPixelValues[i]);
        }
        g.setColor(color);
        g.drawPolyline(xPixelValues, yPixelValues, way.getWaySize());
        // Set stroke back to basic stroke
        g.setStroke(new BasicStroke());
    }
}

