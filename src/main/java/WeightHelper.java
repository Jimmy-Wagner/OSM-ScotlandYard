public class WeightHelper {

    public static int combineWeights(int weight1, int weight2){
        switch (weight1){
            case 1:
                switch (weight2){
                    case 1:
                        return 1;
                    case 10:
                        return 11;
                    case 100:
                        return 101;
                    case 11:
                        return 11;
                    case 101:
                        return 101;
                    case 110:
                        return 111;
                    case 111:
                        return 111;
                    default:
                        System.out.println("Error");
                        return 0;
                }
            case 10:
                switch (weight2){
                    case 1:
                        return 11;
                    case 10:
                        return 10;
                    case 100:
                        return 110;
                    case 11:
                        return 11;
                    case 101:
                        return 111;
                    case 110:
                        return 110;
                    case 111:
                        return 111;
                    default:
                        System.out.println("Error");
                        return 0;
                }
            case 100:
                switch (weight2){
                    case 1:
                        return 101;
                    case 10:
                        return 110;
                    case 100:
                        return 100;
                    case 11:
                        return 111;
                    case 101:
                        return 101;
                    case 110:
                        return 110;
                    case 111:
                        return 111;
                    default:
                        System.out.println("Error");
                        return 0;
                }
            case 11:
                switch (weight2){
                    case 1:
                        return 11;
                    case 10:
                        return 11;
                    case 100:
                        return 111;
                    case 11:
                        return 11;
                    case 101:
                        return 111;
                    case 110:
                        return 111;
                    case 111:
                        return 111;
                    default:
                        System.out.println("Error");
                        return 0;
                }
            case 101:
                switch (weight2){
                    case 1:
                        return 101;
                    case 10:
                        return 111;
                    case 100:
                        return 101;
                    case 11:
                        return 111;
                    case 101:
                        return 101;
                    case 110:
                        return 111;
                    case 111:
                        return 111;
                    default:
                        System.out.println("Error");
                        return 0;
                }
            case 110:
                switch (weight2){
                    case 1:
                        return 111;
                    case 10:
                        return 110;
                    case 100:
                        return 111;
                    case 11:
                        return 111;
                    case 101:
                        return 111;
                    case 110:
                        return 110;
                    case 111:
                        return 111;
                    default:
                        System.out.println("Error");
                        return 0;
                }
            case 111:
                return 111;
            default:
                System.out.println("error");
                return 0;
        }
    }
}
