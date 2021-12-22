package Draw;

import DataManipulation.OsmDataHandler;
import HelperClasses.ColorPicker;
import Types.DetailsOfRoute;
import Types.RouteType;
import Types.TrimmedWay;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DrawHandler {

    private DrawToGraphics drawToGraphics;
    private OsmDataHandler dataHandler;

    public DrawHandler(DrawToGraphics drawToGraphics,
                       OsmDataHandler dataHandler) {
        this.drawToGraphics = drawToGraphics;
        this.dataHandler = dataHandler;
    }

    /**
     * Draws the selected details of the given route types (e.g. busroutes, trainroutes, etc.)
     * @param stopsOrRoutes
     * @param routeTypes
     */
    public void draw(DetailsOfRoute stopsOrRoutes, ArrayList<RouteType> routeTypes) {
        switch (stopsOrRoutes) {
            case HALT:
                drawHalts(routeTypes);
                break;
            case ROUTE:
                drawRoutes(routeTypes);
                break;
            case HALTANDROUTE:
                drawRoutes(routeTypes);
                drawHalts(routeTypes);
                break;
        }
    }

    /**
     * Draws all halts for the given types of routes.
     * E.g. all stop for all bus routes.
     * @param routeTypes
     */
    private void drawHalts(ArrayList<RouteType> routeTypes){
        for (RouteType type: routeTypes){
            drawHalts(type);
        }
    }

    /**
     * Draws all halts for all Relations of the given RouteType
     * @param type
     */
    private void drawHalts(RouteType type){
        HashSet<Node> halts = this.dataHandler.getMergedHalts(type);
        //HashSet<Node> halts = this.dataHandler.getThinnedMergedHalts(type);
        //HashSet<Node> halts = this.dataHandler.getDifference(type);
        // HashSet<Node> halts = this.dataHandler.getAllStopAndPlatformNodes(type);
        this.drawToGraphics.drawNodes(halts, ColorPicker.colorForHalts(type), new Font("TimesRoman", Font.PLAIN, 20));
    }

    /**
     * Draws routes for all given route types
     * @param routeTypes
     */
    private void drawRoutes(ArrayList<RouteType> routeTypes){
        for (RouteType type: routeTypes){
            drawRoutes(type);
        }
    }


    /**
     * Draws all route ways for all reltions of the given type
     * @param type
     */
    private void drawRoutes(RouteType type){
        Color color = ColorPicker.colorForRoutes(type);
        HashMap<Long, ArrayList<TrimmedWay>> trimmedWays = this.dataHandler.getTrimmedWays(type);
        ArrayList<Node> wayNodes;
        //Go through all route relations
        for (long relationID: trimmedWays.keySet()){
            // Go through all trimmed ways of this relation
            for (TrimmedWay trimmedWay: trimmedWays.get(relationID)){
                // trimmedWay can be null when this relation has no ways inside the bounding box (maybe only one stop or something like that)
                if (trimmedWay != null){
                    wayNodes = getFullNodes(trimmedWay.getWaynodes());
                    this.drawToGraphics.drawWay(wayNodes, color, 2);
                }
            }
        }
    }

    /**
     * Substitutes a list of node ids with full nodes.
     * @param nodeIds
     * @return full node list
     */
    private ArrayList<Node> getFullNodes(ArrayList<Long> nodeIds){
        return this.dataHandler.getFullNodesForIds(nodeIds);
    }







}
