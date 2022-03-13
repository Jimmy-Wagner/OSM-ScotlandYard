import java.util.ArrayList;

public class MyTypeConverter {

    public static String convert(int weight){
        switch (weight){
            // Subway
            case 1:
                return "U";
            case 10:
                // Train (Taxi)
                return "T";
            case 100:
                // Bus
                return "B";
            default:
                System.out.println("Severe implemntation error!");
                return "Error";
        }
    }

    public static ArrayList<Integer> getTypes(int weight){
        ArrayList<Integer> types = new ArrayList<>();
        switch (weight){
            case 1:
                types.add(1);
                break;
            case 10:
                types.add(10);
                break;
            case 100:
                types.add(100);
                break;
            case 11:
                types.add(1);
                types.add(10);
                break;
            case 101:
                types.add(1);
                types.add(100);
                break;
            case 110:
                types.add(10);
                types.add(100);
                break;
            case 111:
                types.add(1);
                types.add(10);
                types.add(100);
                break;
            default:
                System.out.println("Sever implemntation error");
                break;

        }

        return types;
    }
}
