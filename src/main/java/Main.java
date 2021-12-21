import Revise.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        // TODO: 1. Read pbf file
        OSM_Pbf_Reader reader = new OSM_Pbf_Reader();
        //TODO: read path to input file
        OsmDataHandler dataHandler = reader.readOsmPbfFile();

        // TODO: 2. Let user select address
        GeoApiCaller geocoder = new GeoApiCaller();
        String address = ConsoleDialog.selectAddress();
        geocoder.callApiAsynchronously(address);
        // Wait for api response of geocoder
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ImageData imageData = new ImageData(geocoder.getCameraPoint());
        //TODO: 3. Send api call for image of selected center
        ImageApiCaller imageApiCaller = new ImageApiCaller(imageData);
        BufferedImage staticImage = imageApiCaller.callApi();
        ImageFrame imageFrame = new ImageFrame(staticImage, imageData.getPIXELWIDTH(), imageData.getPIXELHEIGHT());

        // 1= bus, 10= trolleybus, 100=train, 1000=tram, 10000=subway, 100000=lightrail, 1000000=monorail
        ArrayList<RouteType> routeType = ConsoleDialog.selectRouteTypes();
        DetailsOfRoute stopsOrRoutes = ConsoleDialog.selectStopsOrRoutes();

        //TODO: 4. Draw stops and routes on image
        //DrawHandler drawHandler = new DrawHandler(imageFrame, imageData, dataHandler, routeType, stopsOrRoutes);
        //drawHandler.draw();

        imageFrame.setVisible();

    }
}
