import java.util.Scanner;

/**
 * This class handles user communication over console
 */
public class ConsoleDialog {
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Asks user which routes to dispaly
     * @return routetype
     */
    public static int selectRouteTypes(){
        System.out.println("Do you want route information for 1:bus, 10:trolleybus, 100:train, 1000:tram");
        System.out.println("Type the sum of the values which you want to select");
        return scanner.nextInt();
    }

    /**
     * Asks user whether stops, routes or both should be displayed
     * @return stopOrRoute
     */
    public static int selectStopsOrRoutes(){
        System.out.println("Do you want to display 0:stops, routes:1 or both:2");
        return scanner.nextInt();
    }


    /**
     * Asks for address the user want to load the image of
     * @return Point
     */
    public static String selectAddress(){
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
