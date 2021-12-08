import com.mapbox.geojson.Point;

/**
 * This class contains all the necessary data of static image that is fetched with {@link ImageApiCaller}.
 * The contained data is necessary for the api call and drawing on the image in
 */
public class ImageData {
    // Zoom level for the map image. This determines the size of the bounding box.
    private final float ZOOMLEVEL = 13f;
    // [lon(min),lat(min),lon(max),lat(max)] for apicall
    // [west, south, east, north]
    private double [] boundingbox;
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
        this.boundingbox = calculateBoundingBox(cameraPoint);
    }

    /**
     * @return boundingbox of the image which has been retrieved in lat,lon
     */
    public double[] getBoundingbox(){
        return boundingbox;
    }

    /**
     * This method calculates the bounding box in lat, lon by a given image size in pixels, a center coordinate and a zoomlevel.
     * @param cameraPoint
     * @return
     */
    private double[] calculateBoundingBox(Point cameraPoint){
        double lat = cameraPoint.latitude();
        double lng = cameraPoint.longitude();
        // For calculation of bounding box
        final double  EARTH_CIR_METERS = 40075016.686;
        final double  degreesPerMeter = 360 / EARTH_CIR_METERS;
        // ZOOMLEVEL + 1 because somehow the mapbox api returns one zoom less
        double metersPerPixelEW = EARTH_CIR_METERS / Math.pow(2, ZOOMLEVEL+1 + 8);
        double metersPerPixelNS = EARTH_CIR_METERS / Math.pow(2, ZOOMLEVEL+1 + 8) * Math.cos(toRadians(lat));

        double shiftMetersEW = (PIXELWIDTH/2) * metersPerPixelEW;
        double shiftMetersNS = (PIXELHEIGHT/2) * metersPerPixelNS;

        double shiftDegreesEW = shiftMetersEW * degreesPerMeter;
        double shiftDegreesNS = shiftMetersNS * degreesPerMeter;

        double [] returnValue = new double[4];
        returnValue[0] = lng-shiftDegreesEW;
        returnValue[1] = lat-shiftDegreesNS;
        returnValue[2] = lng+shiftDegreesEW;
        returnValue[3] = lat+shiftDegreesNS;


        // [east, south, west, north]
        return returnValue;
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
}
