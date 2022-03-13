
import com.mapbox.geojson.Point;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        // 1. Read the location of the map
        String address = ConsoleDialog.selectAddress();
                // "london";
                // ConsoleDialog.selectAddress();

        // 2. Retrieve the latitude and longitude of this address
        GeoApiCaller geoApiCaller = new GeoApiCaller();
        // ZUm zeigen warum buffer inkrementell kleiner machen einmal (-0.124,51.5006) und (-0.124, 51.6005) subway netzwerk mergen
        Point pointOfAddress = geoApiCaller.callApiSycnhrounisly(address);
                // Point.fromLngLat(-0.124, 51.5006);
                // Point.fromLngLat(-0.1115, 51.496);
                // geoApiCaller.callApiSycnhrounisly(address);

        // 3. Prepare information to retrieve the map image
        ImageData imageData = new ImageData(pointOfAddress);

        // 4. Call the api for the image of the map
        ImageApiCaller imageApiCaller = new ImageApiCaller(imageData);
        BufferedImage mapImage = imageApiCaller.callApi();

        System.out.println("Data reading...");
        // 5. Read the necessary osm data
        PbfFileReader reader = new PbfFileReader(imageData.getBoundingBox());
        OsmDataContainer dataContainer =
                reader.readFile("/Users/jimmy/Desktop/london/0.publictransportRoutesAllInclusive.osm.pbf");
        // london/0.publictransportRoutesAllInclusive.osm.pbf
        // Stuttgart/stuttgart-publicTransport.osm.pbf
        // Bietigheim/test.osm.pbf

        // 6. Create Data Handler for extracting information from the relations
        OsmDataHandler dataHandler = new OsmDataHandler(dataContainer, imageData);

        // 7. Create the new image frame and draw object of that frame
        ImageFrame frame = new ImageFrame(mapImage, imageData.getPIXELWIDTH()+100,
                imageData.getPIXELHEIGHT()+100, dataHandler);
        DrawToGraphics drawToGraphics = new DrawToGraphics(frame.getG(), imageData);
        int [] hi = DrawToGraphics.convertGeoToPixel(51.5171, -0.124,1200, 1200, imageData.getBoundingBox());

        // 7. Select what to draw
        ArrayList<RouteType> routeTypes = ConsoleDialog.selectRouteTypes();
        DetailsOfRoute detailsOfRoute = ConsoleDialog.selectStopsOrRoutes();

        System.out.println("Map generation...");
        // 8. draw selected stops and routes
        DrawHandler drawHandler = new DrawHandler(drawToGraphics, dataHandler);
        drawHandler.draw(detailsOfRoute, routeTypes);

        // last. make frame visible
        //frame.setVisible();
        frame.saveImage();
        new FileOutput().createFiles();
        System.out.println("Finish!");

        System.exit(0);
    }
}
