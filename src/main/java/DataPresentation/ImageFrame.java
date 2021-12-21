package DataPresentation;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This class opens a gui.
 * First the constructor takes an image and the size of the frame which should be larger than the image size.
 * Secondly you need to call the showframe method to open the gui.
 */
public class ImageFrame extends JComponent{
    private JFrame frame;
    // This is the map which will be drawn to
    private BufferedImage mapImage;
    // Graphics reference to imageToDisplay for drawing on that image
    private Graphics2D g;


    public ImageFrame(BufferedImage imageToDisplay, int pixelWidth, int pixelHeight ){
        frame = new JFrame();
        frame.setSize(pixelWidth, pixelHeight);
        frame.add(new JLabel(new ImageIcon(imageToDisplay)));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mapImage = imageToDisplay;
        this.g = (Graphics2D) this.mapImage.getGraphics();
        // For smooth drawings
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // For smooth drawing of text
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /**
     * replace old mapImage with new mapImage
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
    public void setVisible(){
        this.frame.setVisible(true);
    }

    public Graphics2D getG() {
        return g;
    }
}

