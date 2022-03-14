
import com.mapbox.geojson.Point;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        String pathToFile = ConsoleDialog.selectPathToOsmFile();

        int manuelOrAutomatedCenter = ConsoleDialog.selectAutomateORManuelCenter();

        Point pointOfAddress;

        if (manuelOrAutomatedCenter == 0) {
            // 1. Read the location of the map
            String address = ConsoleDialog.selectAddress();
            // "london";

            // 2. Retrieve the latitude and longitude of this address
            GeoApiCaller geoApiCaller = new GeoApiCaller();
            // ZUm zeigen warum buffer inkrementell kleiner machen einmal (-0.124,51.5006) und (-0.124, 51.6005) subway netzwerk mergen
            pointOfAddress = geoApiCaller.callApiSycnhrounisly(address);
            // Point.fromLngLat(-0.124, 51.5006);
            // Point.fromLngLat(-0.1115, 51.496);
            // geoApiCaller.callApiSycnhrounisly(address);
        }
        else {
            CenterPbfReader reader = new CenterPbfReader();
            pointOfAddress = reader.readFile(pathToFile);
        }

        // 3. Prepare information to retrieve the map image
        ImageData imageData = new ImageData(pointOfAddress);

        // 4. Call the api for the image of the map
        ImageApiCaller imageApiCaller = new ImageApiCaller(imageData);
        BufferedImage mapImage = imageApiCaller.callApi();

        // 5. Read the necessary osm data
        PbfFileReader reader = new PbfFileReader(imageData.getBoundingBox());
        OsmDataContainer dataContainer =
                reader.readFile(pathToFile);
        // /Users/jimmy/Desktop/OSM-ScotlandYard/geofiles/greater-london.osm.pbf
        // london/0.publictransportRoutesAllInclusive.osm.pbf
        // Stuttgart/stuttgart-publicTransport.osm.pbf
        // Bietigheim/test.osm.pbf

        System.out.println("Data reading...");

        // 6. Create Data Handler for extracting information from the relations
        OsmDataHandler dataHandler = new OsmDataHandler(dataContainer, imageData);

        // 7. Create the new image frame and draw object of that frame
        ImageFrame frame = new ImageFrame(mapImage, imageData.getPIXELWIDTH() + 100,
                imageData.getPIXELHEIGHT() + 100, dataHandler);
        DrawToGraphics drawToGraphics = new DrawToGraphics(frame.getG(), imageData);

        // 7. Select what to draw
        ArrayList<RouteType> routeTypes = ConsoleDialog.selectRouteTypes();
        DetailsOfRoute detailsOfRoute = ConsoleDialog.selectStopsOrRoutes();

        System.out.println("Map generation...");
        // 8. draw selected stops and routes
        DrawHandler drawHandler = new DrawHandler(drawToGraphics, dataHandler);
        drawHandler.draw(detailsOfRoute, routeTypes);

        // last. make frame visible
        //frame.setVisible();
        // Saves image to map file!
        frame.saveImage();
        new FileOutput().createFiles();
        System.out.println("Finish!");


        /*System.out.println("Number nodes total: " +RuntimeListener.totalNumberOfNodes );
        System.out.println("Number ways total: " + RuntimeListener.totalNumberOfWays);
        System.out.println("Number contained nodes: " + RuntimeListener.totalNumberOfContainedNodes);
        System.out.println("Number contained ways: " + RuntimeListener.totalNumberOfContainedWays);
        System.out.println("Read time: " + RuntimeListener.readTime + "miliseconds");*/

        System.exit(0);
    }
}
