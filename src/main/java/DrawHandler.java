import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrawHandler {
    private ImageFrame imageFrame;
    private double[] boundingbox;
    private int routeType;
    private int stopsOrRoutes;
    private OsmDataHandler dataHandler;

    public DrawHandler(ImageFrame imageFrame, ImageData imageData, OsmDataHandler dataHandler, int routeType ,int stopsOrRoutes){
        this.imageFrame = imageFrame;
        this.boundingbox = imageData.getBoundingbox();
        this.dataHandler = dataHandler;
        this.routeType = routeType;
        this.stopsOrRoutes = stopsOrRoutes;
    }

    /**
     * This method starts the drawing on the image in the ImageFrame
     */
    public void draw(){
        switch (routeType){
            case 1:
                drawBus();
                break;
            case 10:
                drawTrolleybus();
                break;
            case 100:
                drawTrain();
                break;
            case 1000:
                drawTram();
                break;
            case 10000:
                drawSubway();
                break;
            case 10100:
                drawTrain();
                drawSubway();
                break;
            case 10001:
                drawSubway();
                drawBus();
                break;
            case 10101:
                drawTrain();
                drawSubway();
                drawBus();
            case 100000:
                drawLightrail();
                break;
            case 101:
                drawTrain();
                drawBus();
                break;
            case 1100:
                drawTrain();
                drawTram();
                break;
            case 1101:
                drawTrain();
                drawTram();
                drawBus();
                break;
            case 1111:
                drawTrain();
                drawTram();
                drawBus();
                drawTrolleybus();
            default: break;
        }
    }

    /**
     * Draws stops and/or ways for tram
     */
    private void drawLightrail() {
        ArrayList<Node> stops = dataHandler.getlightRailStops();
        ArrayList<Way> routeWays = dataHandler.getLightrailRouteWays();
        actualDrawMethod(stops, routeWays, 100000);
    }
    /**
     * Draws stops and/or ways for tram
     */
    private void drawSubway() {
        ArrayList<Node> stops = dataHandler.getSubwayStops();
        ArrayList<Way> routeWays = dataHandler.getSubwayRouteWays();
        actualDrawMethod(stops, routeWays, 10000);
    }

    /**
     * Draws stops and/or ways for tram
     */
    private void drawTram() {
        ArrayList<Node> stops = dataHandler.getTramStops();
        ArrayList<Way> routeWays = dataHandler.getTramRouteWays();
        actualDrawMethod(stops, routeWays, 1000);
    }

    /**
     * Draws stops and/or ways for train
     */
    private void drawTrain() {
        ArrayList<Node> stops = dataHandler.getTrainStops();
        ArrayList<Way> routeWays = dataHandler.getTrainRouteWays();
        actualDrawMethod(stops, routeWays, 100);
    }

    /**
     * Draws stops and/or ways for trolleybus
     */
    private void drawTrolleybus() {
        ArrayList<Node> stops = dataHandler.getTrolleybusStops();
        ArrayList<Way> routeWays = dataHandler.getTrolleybusRouteWays();
        actualDrawMethod(stops, routeWays, 10);
    }

    /**
     * Draws stops and/or ways for bus
     */
    private void drawBus(){
        ArrayList<Node> stops = dataHandler.getBusStops();
        System.out.println("drawBus() | size of busstoplist: " + stops.size());
        ArrayList<Way> routeWays = dataHandler.getBusRouteWays();
        actualDrawMethod(stops, routeWays, 1);
    }

    private void actualDrawMethod(ArrayList<Node> stops, ArrayList<Way> routeWays, int routeType){
        ArrayList<Node> oneWay = new ArrayList<Node>();
        HashMap<Long, Node> allNodes = dataHandler.getAllNodes();
        Color color;

        switch (stopsOrRoutes){
            //case only stops
            case 0:
                // get rid of the stops that are not contained in the image
                stops = checkForBoundingBox(stops);
                // Pick color based on routetype for stops
                color = pickColorForStop(routeType);
                // Draw stops in picked color on image map
                imageFrame.drawPoints(stops, color, new Font("TimesRoman", Font.PLAIN, 20));
                // Safe storage
                stops.clear();
                break;
            // only routes without stops
            case 1:
                // get rid of the stops that are not contained in the image
                routeWays = trimWaysToBoundingBox(routeWays);

                //pick color for current route type
                color = pickColorForRouteWay(routeType);
                // Convert the way to an array list of full nodes
                for (Way currentWay: routeWays){
                    for (WayNode currentNode: currentWay.getWayNodes()){
                        oneWay.add(allNodes.get(currentNode.getNodeId()));
                    }
                    imageFrame.drawWay(oneWay, color);
                    oneWay.clear();
                }
                break;
            case 2:
                stops = checkForBoundingBox(stops);
                // get rid of the stops that are not contained in the image
                routeWays = trimWaysToBoundingBox(routeWays);
                // pick color for this type of route (bus, train, etc.)
                color = pickColorForRouteWay(routeType);
                // Convert the way to an array list of full nodes
                for (Way currentWay: routeWays){
                    for (WayNode currentNode: currentWay.getWayNodes()){
                        oneWay.add(allNodes.get(currentNode.getNodeId()));
                    }
                    imageFrame.drawWay(oneWay, color);
                    oneWay.clear();
                }
                color = pickColorForStop(routeType);
                imageFrame.drawPoints(stops, color, new Font("TimesRoman", Font.PLAIN, 20));
                break;
            default: break;
        }
    }

    /**
     * Picks different colors for bus, trolleybus, train and tram
     * @param routeType
     * @return color to draw on map image
     */
    private Color pickColorForRouteWay(int routeType){
        Color color;
        switch (routeType){
            case 1:
                color = new Color(0,128,0);
                break;
            case 10:
                color = Color.GRAY;
                break;
            case 100:
                color = Color.PINK;
                break;
            case 1000:
                color = new Color(97, 54, 89);
                break;
            case 10000:
                color = Color.BLUE;
                break;
            case 100000:
                color = Color.LIGHT_GRAY;
                break;
            default:
                color = Color.WHITE;
                break;
        }
        return color;
    }

    /**
     * Picks different colors for bus, trolleybus, train and tram
     * @param routeType
     * @return color to draw on map image
     */
    private Color pickColorForStop(int routeType){
        Color color;
        switch (routeType){
            case 1:
                color = Color.GREEN;
                break;
            case 10:
                color = Color.WHITE;
                break;
            case 100:
                color = Color.RED;
                break;
            case 1000:
                color = new Color(193, 151, 210);
                break;
            case 10000:
                color = new Color(50,200,200);
                break;
            case 100000:
                color = Color.BLACK;
                break;
            default:
                color = Color.WHITE;
                break;
        }
        return color;
    }

    /**
     * This method trims the ways and their nodes to the bounding box.
     * Nodes which are outside the boundingbox are chopped off.
     * Ways which contain no nodes that are in the bounding box are getting rid off.
     * @param ways
     * @return trimmedWays
     */
    private ArrayList<Way> trimWaysToBoundingBox(ArrayList<Way> ways){

        HashMap<Long, Node> allNodes = dataHandler.getAllNodes();
        // This array contains the chopped ways to the bounding box in the end
        ArrayList<Way> trimmedWays = new ArrayList<Way>();
        // Variable for chopped Way to insert to trimmedWays
        Way trimmedWay;
        // The list of wayNodes which will be inserted to trimmedway
        ArrayList<WayNode> choppedWayNodes = new ArrayList<WayNode>();
        List<WayNode> unchoppedWayNodes = new ArrayList<WayNode>();
        ArrayList<Long> choppedFullNodeIDs = new ArrayList<Long>();
        ArrayList<Node> unchoppedFullNodes = new ArrayList<Node>();

        // Go through all ways to chop them
        for (Way currentWayToChop: ways){

            // All nodes in the current way
            unchoppedWayNodes = currentWayToChop.getWayNodes();

            for (WayNode wayNode: unchoppedWayNodes){

                // The wayNode only contains the id but no information about the lat and lon so get the full node
                Node fullNode = allNodes.get(wayNode.getNodeId());
                unchoppedFullNodes.add(fullNode);
            }
            // Chop the fullnodes (after that they are not unchopped anymore)
            unchoppedFullNodes = checkForWayBoundingBox(unchoppedFullNodes);

            for (Node fullNode: unchoppedFullNodes){
                choppedFullNodeIDs.add(fullNode.getId());
            }

            // Now chop the unchopped waynode list also
            for (WayNode currentWayNode: unchoppedWayNodes){
                // Only add to chopped list if the node is contained in the chopped full node list
                if (choppedFullNodeIDs.contains(currentWayNode.getNodeId())){
                    choppedWayNodes.add(currentWayNode);
                }
            }
            // Now the waynodes of the current way are chopped to the bounding box


            // If all nodes are outisde the bounding box get rid of the whole way
            if (choppedWayNodes.size()!=0){
                // Create duplicate of current way but only with contained nodes
                trimmedWay = new Way(
                        currentWayToChop.getId(),
                        currentWayToChop.getVersion(),
                        currentWayToChop.getTimestamp(),
                        currentWayToChop.getUser(),
                        currentWayToChop.getChangesetId(),
                        currentWayToChop.getTags(),
                        choppedWayNodes);
                trimmedWays.add(trimmedWay);
            }
            // Clear list for next loop
            choppedWayNodes.clear();
            choppedFullNodeIDs.clear();
            unchoppedFullNodes.clear();
        }
        return trimmedWays;
    }


    /**
     * Checks if the given nodes are contained in the boundingbox of the image and gets rid of the ones which are not contained
     * @param nodes
     * @return containedNodes
     */
    private ArrayList<Node> checkForBoundingBox(ArrayList <Node> nodes){
        ArrayList<Node> containedNodes = new ArrayList<Node>();
        System.out.println("Number of nodes am Anfang von checkBoundingBox " + nodes.size());
        for (Node currentNodeToCheck: nodes){
            if(currentNodeToCheck == null){
                break;
            }
            if (currentNodeToCheck.getLatitude() < boundingbox[1]){
                continue;
            }
            if(currentNodeToCheck.getLatitude() > boundingbox[3]){
                continue;
            }
            if(currentNodeToCheck.getLongitude() < boundingbox[0]){
                continue;
            }
            if(currentNodeToCheck.getLongitude() > boundingbox[2]){
                continue;
            }
            // Now the node is contained in the boundingbox
            containedNodes.add(currentNodeToCheck);
        }
        System.out.println("Number of nodes am Ende von checkBoundingBox " + containedNodes.size());
        return containedNodes;
    }


    /**
     * Checks if the way is contained in the bounding box.
     * The route starts with the FIRST node that is contained in the bounding box and ends with the first
     * node before the FIRST node that is outside the bounding box.
     * First time one node is outside the bounding box,
     * the rest of the way gets chopped of. Even if the way returns back to the bounding box.
     * @param nodes
     * @return containedNodes
     */
    private ArrayList<Node> checkForWayBoundingBox(ArrayList <Node> nodes){
        ArrayList<Node> containedNodes = new ArrayList<Node>();
        boolean firstNodeToAdd = true;
        for (Node currentNodeToCheck: nodes){
            if (currentNodeToCheck.getLatitude() < boundingbox[1]){
                if(!firstNodeToAdd){
                    break;
                }
                continue;
            }
            if(currentNodeToCheck.getLatitude() > boundingbox[3]){
                if(!firstNodeToAdd)break;
                continue;
            }
            if(currentNodeToCheck.getLongitude() < boundingbox[0]){
                if(!firstNodeToAdd)break;
                continue;
            }
            if(currentNodeToCheck.getLongitude() > boundingbox[2]){
                if(!firstNodeToAdd)break;
                continue;
            }
            if (firstNodeToAdd){
                // Now the node is contained in the boundingbox
                containedNodes.add(currentNodeToCheck);
                firstNodeToAdd = false;
            }
            else{
                containedNodes.add(currentNodeToCheck);
            }
        }
        //System.out.println("Number of nodes am Ende von checkBoudningBox " + containedNodes.size());
        return containedNodes;
    }

}
