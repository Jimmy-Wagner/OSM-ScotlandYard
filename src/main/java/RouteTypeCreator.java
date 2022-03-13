public class RouteTypeCreator {

    /**
     * Combines routetypes into one routetype
     * FIXME: Implement all combinations of routetypes
     * @param type1
     * @param type2
     * @return
     */
    public static RouteType combineTypes(RouteType type1, RouteType type2){
        switch (type1){
            case SUBWAY:
                switch (type2){
                    case TRAIN:
                        return RouteType.ST;
                    case ST:
                        return RouteType.ST;
                    case SUBWAY:
                        return RouteType.SUBWAY;
                    case BUS:
                        return RouteType.SB;
                    case SB:
                        return RouteType.SB;
                    case STB:
                        return RouteType.STB;
                    default:
                        System.out.println("Severe error because not implemented1");
                        return null;
                }
            case ST:
                switch (type2){
                    case ST:
                        return RouteType.ST;
                    case TRAIN:
                        return RouteType.ST;
                    case SUBWAY:
                        return RouteType.ST;
                    case BUS:
                        return RouteType.STB;
                    case SB:
                        return RouteType.STB;
                    case STB:
                        return RouteType.STB;
                    default:
                        System.out.println("Severe error because not implemented3");
                        return null;
                }
            case TRAIN:
                switch (type2){
                    case ST:
                        return RouteType.ST;
                    case TRAIN:
                        return RouteType.TRAIN;
                    case SUBWAY:
                        return RouteType.ST;
                    case BUS:
                        return RouteType.TB;
                    case STB:
                        return RouteType.STB;
                    case TB:
                        return RouteType.TB;
                    case SB:
                        return RouteType.STB;
                    default:
                        System.out.println("Severe error because not implemented4");
                        return null;
                }
            case BUS:
                switch (type2){
                    case ST:
                        return RouteType.STB;
                    case TRAIN:
                        return RouteType.TB;
                    case SUBWAY:
                        return RouteType.SB;
                    case BUS:
                        return RouteType.BUS;
                    case SB:
                        return RouteType.SB;
                    case TB:
                        return RouteType.TB;
                    case STB:
                        return RouteType.STB;
                    default:
                        System.out.println("Severe error because not implemented4");
                        return null;
                }
            case SB:
                switch (type2){
                    case ST:
                        return RouteType.STB;
                    case TRAIN:
                        return RouteType.STB;
                    case SUBWAY:
                        return RouteType.SB;
                    case BUS:
                        return RouteType.SB;
                    case SB:
                        return RouteType.SB;
                    case TB:
                        return RouteType.STB;
                    case STB:
                        return RouteType.STB;
                    default:
                        System.out.println("Severe error because not implemented4");
                        return null;
                }

            case STB:
                return RouteType.STB;
            default:
                System.out.println("Severe error because not implemented default 123");
                return null;


        }
    }


    public static RouteType segmentType(RouteType type1, RouteType type2){
        switch (type1){
            case SUBWAY:
                switch (type2){
                    case SUBWAY:
                        return RouteType.SUBWAY;
                    case ST:
                        return RouteType.SUBWAY;
                    case STB:
                        return RouteType.SUBWAY;
                    case SB:
                        return RouteType.SUBWAY;
                    case BUS:
                        return RouteType.SUBWAY;
                    case TRAIN:
                        return RouteType.SUBWAY;
                    default:
                        System.out.println("Severe error segmenttype!");
                        return null;
                }
            case ST:
                switch (type2){
                    case SUBWAY:
                        return RouteType.SUBWAY;
                    case ST:
                        return RouteType.ST;
                    case TRAIN:
                        return RouteType.TRAIN;
                    case STB:
                        return RouteType.ST;
                    case SB:
                        return RouteType.ST;
                    case TB:
                        return RouteType.TRAIN;
                    default:
                        System.out.println("Sever error 190");
                        return null;
                }
            case TRAIN:
                switch (type2){
                    case ST:
                        return RouteType.TRAIN;
                    case TRAIN:
                        return RouteType.TRAIN;
                    case STB:
                        return RouteType.TRAIN;
                    case TB:
                        return RouteType.TRAIN;
                    case SB:
                        return RouteType.TRAIN;
                    default:
                        System.out.println("Sever error 201");
                        return null;
                }
            case BUS:
                return RouteType.BUS;
            case SB:
                switch (type2){
                    case ST:
                        return RouteType.ST;
                    case BUS:
                        return RouteType.BUS;
                    case STB:
                        return RouteType.SB;
                    case TB:
                        return RouteType.BUS;
                    case SB:
                        return RouteType.SB;
                    case SUBWAY:
                        return RouteType.SUBWAY;
                    case TRAIN:
                        return RouteType.TRAIN;
                    default:
                        System.out.println("Sever error 201");
                        return null;
                }
            case TB:
                switch (type2){
                    case ST:
                        return RouteType.TRAIN;
                    case BUS:
                        return RouteType.BUS;
                    case STB:
                        return RouteType.TB;
                    case TB:
                        return RouteType.TB;
                    case SB:
                        return RouteType.BUS;
                    case TRAIN:
                        return RouteType.TRAIN;
                    default:
                        System.out.println("Sever error 201");
                        return null;
                }
            case STB:
                switch (type2){
                    case ST:
                        return RouteType.ST;
                    case BUS:
                        return RouteType.BUS;
                    case STB:
                        return RouteType.STB;
                    case TB:
                        return RouteType.TB;
                    case SB:
                        return RouteType.SB;
                    case SUBWAY:
                        return RouteType.SUBWAY;
                    case TRAIN:
                        return RouteType.TRAIN;
                    default:
                        System.out.println("Sever error 201");
                        return null;
                }
            default:
                System.out.println("Severe implementation error");
                return null;
        }
    }
}
