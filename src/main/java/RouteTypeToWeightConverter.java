public class RouteTypeToWeightConverter {

    public static int routeTypeToInt(RouteType type){
        switch (type){
            case SUBWAY:
                return 1;
            case TRAIN:
                return 10;
            case BUS:
                return 100;
            case STB:
                return 111;
            case SB:
                return 101;
            case TB:
                return 110;
            case ST:
                return 11;
            default:
                System.out.println("Fehler bei type weight conversion!");
                return 0;

        }
    }

    public static RouteType intToRouteType(int i){
        switch (i){
            case 1:
                return RouteType.SUBWAY;
            case 10:
                return RouteType.TRAIN;
            case 100:
                return RouteType.BUS;
            case 111:
                return RouteType.STB;
            case 101:
                return RouteType.SB;
            case 110:
                return RouteType.TB;
            case 11:
                return RouteType.ST;
            default:
                System.out.println("Fehler bei type weight conversion 2");
                return RouteType.MONORAIL;
        }
    }
}
