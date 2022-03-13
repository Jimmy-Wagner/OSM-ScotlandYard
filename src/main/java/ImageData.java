import com.mapbox.geojson.Point;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;

/**
 * This class contains all the necessary data of static image that is fetched with {@link ImageApiCaller}.
 * The contained data is necessary for the api call and drawing on the image in
 */
public class ImageData {
    // Zoom level for the map image. This determines the size of the bounding box.
    // zoomlevel in leaflet = this zoom level + 1
    // 13f best
    private final float ZOOMLEVEL = 13f;
    // Bounding box of the image map
    private Bound boundingBox;
    // width and height of the returned image
    private final int PIXELWIDTH = 1200;
    private final int PIXELHEIGHT = 1200;
    // Center of the image
    private Point cameraPoint;

    /**
     * Constructor
     * @param cameraPoint
     */
    public ImageData(Point cameraPoint){
        this.cameraPoint = cameraPoint;
        this.boundingBox = calculateBoundingBoxNew(cameraPoint);
    }


    /**
     * This method calculates the bounding box in latitude and longitude by a given image size in pixels, a center coordinate and a zoomlevel.
     * @param cameraPoint
     * @return boundingBox
     */
    private Bound calculateBoundingBoxNew(Point cameraPoint){
        double lat = cameraPoint.latitude();
        double lng = cameraPoint.longitude();
        // For calculation of bounding box
        final double  EARTH_CIR_METERS = 40075016.686;
        final double  degreesPerMeter = 360 / EARTH_CIR_METERS;
        // ZOOMLEVEL + 1 because somehow the mapbox api returns one zoom less
        double metersPerPixelEW = EARTH_CIR_METERS / Math.pow(2, ZOOMLEVEL+1 + 8);
        double metersPerPixelNS = metersPerPixelEW * Math.cos(toRadians(lat));

        double shiftMetersEW = (PIXELWIDTH/2) * metersPerPixelEW;
        double shiftMetersNS = (PIXELHEIGHT/2) * metersPerPixelNS;

        double shiftDegreesEW = shiftMetersEW * degreesPerMeter;
        double shiftDegreesNS = shiftMetersNS * degreesPerMeter;


        double left = lng-shiftDegreesEW;
        double bottom = lat-shiftDegreesNS;
        double right = lng+shiftDegreesEW;
        double top = lat+shiftDegreesNS;

        Bound bound = new Bound(right, left, top, bottom, "");

        return bound;
    }


    /**
     * This method is a helper function for the calculation of the bounding box
     * @param degrees
     * @return
     */
    static double toRadians(double degrees) {
        return degrees * Math.PI / 180;
    };

    public float getZOOMLEVEL() {
        return ZOOMLEVEL;
    }

    public int getPIXELWIDTH() {
        return PIXELWIDTH;
    }

    public int getPIXELHEIGHT() {
        return PIXELHEIGHT;
    }

    public Point getCameraPoint() {
        return cameraPoint;
    }

    public Bound getBoundingBox() {
        return boundingBox;
    }
}
