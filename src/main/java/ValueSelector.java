/**
 * Returns the adjusted values for the algortihms applied to the ways based on the routetype of the ways.
 */
public class ValueSelector {


    private RouteType type;

    public ValueSelector(RouteType type){
        this.type = type;
    }


    public int minimalWayLength(){
        switch (type){
            case SUBWAY:
                return 60;
            case BUS:
                return 60;
            case TRAIN:
                return 70;
            default:
                return 60;
        }
    }


    public int maxNetworkSize(){
        switch (type){
            case SUBWAY:
                return 10000;
            case BUS:
                return 10000;
            case TRAIN:
                return 10000;
            default:
                return 10000;
        }
    }


    public int maxBufferValue(){
        switch (type){
            case SUBWAY:
                return 70;
            case BUS:
                return 70;
            case TRAIN:
                return 70;
            default:
                return 50;
        }
    }

    public double frechetSimValue(){
        switch (type){
            case SUBWAY:
                return 0.73;
            case BUS:
                return 0.62;
            case TRAIN:
                return 0.73;
            default:
                return 0.5;
        }
    }


    public int douglasPeuckerDistance(){
        switch (type){
            case SUBWAY:
                return 5;
            case BUS:
                return 5;
            case TRAIN:
                return 5;
            default:
                return 5;
        }
    }
}
