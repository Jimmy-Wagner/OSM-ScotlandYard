import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * This class serves for communication with mapbox static image api
 */
public class ImageApiCaller {

    // Personal Mapbox access token for api requests
    private final static String ACCESS_TOKEN = "pk.eyJ1IjoiamltbXl3YWduZXIiLCJhIjoiY2t2emZ3M3hnNHppdDJudGtubnZybHdtYSJ9.JXJW_2riuBye8QTdCy1NGw";
    // Satelite Map
    private final static String MAPSTYLE = StaticMapCriteria.SATELLITE_STYLE;
    // Contains data for api call
    private ImageData imageData;

    public ImageApiCaller(ImageData imageData){
        this.imageData = imageData;
    }

    /**
     * Calls Mapbox api for static map images.
     *
     * @return static map image
     */
    public BufferedImage callApi() {
        String urlAsString = constructUrl();
        // variable for retrieved image
        BufferedImage image = null;
        // Try to read image from url
        try {
            URL url = new URL(urlAsString);
            image = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Constructs an url for the api call.
     *
     * @return url
     */
    private String constructUrl() {
        MapboxStaticMap staticImage = MapboxStaticMap.builder()
                .accessToken(ACCESS_TOKEN)
                .styleId(MAPSTYLE)
                .cameraPoint(imageData.getCameraPoint())
                .cameraZoom(imageData.getZOOMLEVEL())
                .width(imageData.getPIXELWIDTH())
                .height(imageData.getPIXELHEIGHT())
                .build();
        String url = staticImage.url().toString();
        return url;
    }
}
