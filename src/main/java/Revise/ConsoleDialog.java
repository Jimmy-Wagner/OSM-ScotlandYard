package Revise;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * This class handles user communication over console
 */
public class ConsoleDialog {
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Asks user which routes to dispaly
     *
     * @return routetype
     */
    public static ArrayList<RouteType> selectRouteTypes() {
        System.out.println("Do you want route information for 1:bus, 10:train, 100:tram, 1000:subway, 10000:light rail, 100000:monorail");
        System.out.println("Type the sum of the values which you want to select");
        int routeType = scanner.nextInt();
        ArrayList<RouteType> selectedTypes = new ArrayList<RouteType>();
        // Whats addded first will be drawn also first
        switch (routeType) {
            case 1:
                selectedTypes.add(RouteType.BUS);
                break;
            case 10:
                selectedTypes.add(RouteType.TRAIN);
                break;
            case 100:
                selectedTypes.add(RouteType.TRAM);
                break;
            case 1000:
                selectedTypes.add(RouteType.SUBWAY);
                break;
            case 10000:
                selectedTypes.add(RouteType.LIGHTRAIL);
                break;
            case 100000:
                selectedTypes.add(RouteType.MONORAIL);
                break;
            case 11:
                selectedTypes.add(RouteType.TRAIN);
                selectedTypes.add(RouteType.BUS);
                break;
            case 1001:
                selectedTypes.add(RouteType.BUS);
                selectedTypes.add(RouteType.SUBWAY);
                break;
            case 1011:
                selectedTypes.add(RouteType.TRAIN);
                selectedTypes.add(RouteType.BUS);
                selectedTypes.add(RouteType.SUBWAY);
                break;
            default:
                System.out.println("Sorry your input is not yet implemented!");
                break;
        }
        return selectedTypes;
    }

    /**
     * Asks user whether stops, routes or both should be displayed
     *
     * @return stopOrRoute
     */
    public static DetailsOfRoute selectStopsOrRoutes() {
        System.out.println("Do you want to display 0:stops, routes:1 or both:2");
        int details = scanner.nextInt();
        if (details < 0 || details > 2) {
            System.out.println("Fehlerhafte Eingabe!");
        }
        switch (details) {
            case 0:
                return DetailsOfRoute.HALT;
            case 1:
                return DetailsOfRoute.ROUTE;
            case 2:
                return DetailsOfRoute.HALTANDROUTE;
            default:
                System.out.println("Fehlerhafte eingabe resultiert in Halt and Route");
                return DetailsOfRoute.HALTANDROUTE;
        }
    }


    /**
     * Asks for address the user want to load the image of
     *
     * @return Point
     */
    public static String selectAddress() {
        System.out.println("Which address do you want to select as center? Type a valid address:");
        return scanner.nextLine();
    }




    /*
     * Asks user for coordinates of the center of the map
     * @return coordinates [lat,lon]
     *
    *public static Point selectCenter(){
        System.out.println("Select center coordinates. First for latitude then for longtitude");
        System.out.println("latitude");
        double[] coordinates = new double[2];
        String latitude = scanner.next();
        coordinates[0] = Double.parseDouble(latitude);
        System.out.println("longtitude");
        String longtitude = scanner.next();
        coordinates[1] = Double.parseDouble(longtitude);
        return Point.fromLngLat(coordinates[1], coordinates[0]);
    }*/

}
