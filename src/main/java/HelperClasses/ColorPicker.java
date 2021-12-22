package HelperClasses;

import Types.RouteType;

import java.awt.*;

public class ColorPicker {
    // FIXME
    public static Color colorForHalts(RouteType type){
        switch (type){
            case BUS:
                return new Color(92, 255, 0);
            case TRAIN:
                return new Color(255, 12, 18);
            case SUBWAY:
                return new Color(143, 0, 242);
            case LIGHTRAIL:
                return new Color(253, 251, 0);
            case MONORAIL:
                return new Color(0, 207, 251);
            case TRAM:
                return new Color(253, 174, 50);
            default:
                return Color.BLACK;
        }
    }

    public static Color colorForRoutes(RouteType type){
        switch (type){
            case BUS:
                return new Color(0,100,0);
            case TRAIN:
                return Color.PINK;
            case SUBWAY:
                return new Color(209, 166, 234);
            case TRAM:
                return Color.WHITE;
            case MONORAIL:
                return Color.BLUE;
            case LIGHTRAIL:
                return Color.MAGENTA;
            default:
                return Color.BLACK;
        }
    }
}
