package Test;

import Types.ReducedWay;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DrawTests {

    private JFrame frame;
    // This is the map which will be drawn to
    private BufferedImage mapImage;
    // Graphics reference to imageToDisplay for drawing on that image
    private Graphics2D g;


    public DrawTests() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("/Users/jimmy/Desktop/White_Background_(To_id_screen_dust_during_cleanup).jpeg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame = new JFrame();
        frame.setSize(1800,1200);
        frame.add(new JLabel(new ImageIcon(img)));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mapImage = img;
        this.g = (Graphics2D) this.mapImage.getGraphics();
        // For smooth drawings
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // For smooth drawing of text
    }



    public void drawReducedWay(ReducedWay way, Color color, int width){
        g.setColor(color);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int [] xPixelValues = new int[way.getWaySize()];
        int [] yPixelValues = new int[way.getWaySize()];
        for (int i=0; i<way.getWaySize(); i++){
            xPixelValues[i] = (int) way.getWayPoint(i).getX();
            yPixelValues[i] = (int) way.getWayPoint(i).getY();
        }
        g.drawPolyline(xPixelValues, yPixelValues, way.getWaySize());
        // Set stroke back to basic stroke
        g.setStroke(new BasicStroke());
    }

    public void setVisible(){
        this.frame.setVisible(true);
    }
}