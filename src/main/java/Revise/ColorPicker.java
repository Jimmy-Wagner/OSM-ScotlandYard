package Revise;

import java.awt.*;

public class ColorPicker {
    // FIXME
    public static Color colorForHalts(RouteType type){
        switch (type){
            case BUS:
                return Color.GREEN;
            default:
                return Color.BLACK;
        }
    }

    public static Color colorForRoutes(RouteType type){
        switch (type){
            case BUS:
                return Color.YELLOW;
            default:
                return Color.BLACK;
        }
    }
}
