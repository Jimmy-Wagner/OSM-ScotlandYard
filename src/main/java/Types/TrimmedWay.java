package Types;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import pl.luwi.series.reducer.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * A trimmed way consists of multiple connected way segments of one relation in the bounding box of the map image.
 * If two ways of one relation are not connected in the bounding box of the map image bacause the route goes out and later on again back to
 * the map image, it will result in two seperate trimmedways.
 */
public class TrimmedWay {
    ArrayList<Long> waynodes = new ArrayList<Long>();
    // This list has to be initialized when working further with the way (applying manipulating algorithms)
    ArrayList<PixelNode> pixelNodes = new ArrayList<>();
    boolean pixelNodesInitilaized = false;

    public TrimmedWay(){

    }

    public TrimmedWay(ArrayList<PixelNode> pixelNodes) {
        this.pixelNodes = pixelNodes;
    }

    public TrimmedWay(List<Point> points, RouteType routeType) {
        for (Point point : points) {
            this.pixelNodes.add(new PixelNode(point.getX(), point.getY()));
        }
    }

    public ReducedWay getReducedWay(){
        return new ReducedWay(this.pixelNodes, 1);
    }

    /**
     * Sets the pixel values for all the nodes in the trimmedways when drawn to the map.
     *
     * @param pixelNodes
     */
    public void initializePixelNodes(ArrayList<PixelNode> pixelNodes) {
        this.pixelNodes = pixelNodes;
        pixelNodesInitilaized = true;
    }

    public ArrayList<PixelNode> getCopyOfPixelNodes() {
        ArrayList<PixelNode> copys = new ArrayList<>();
        for (PixelNode pixelNode : pixelNodes) {
            copys.add(new PixelNode(pixelNode.getX(), pixelNode.getY()));
        }
        return copys;
    }

    public ArrayList<PixelNode> getPixelNodes() {
        return pixelNodes;
    }


    public PixelNode getPixelNode(int index) {
        return this.pixelNodes.get(index);
    }

    public void manipulateRemoveNode(int index) {
        this.waynodes.remove(index);
        this.pixelNodes.remove(index);
    }


    /*
    Use the following methods before the pixel list has been initialized.
     */

    public void addNode(long id) {
        this.waynodes.add(id);
    }

    public void addAllNodes(ArrayList<Long> nodes) {
        this.waynodes.addAll(nodes);
    }

    public void removeFirst() {
        this.waynodes.remove(0);
    }

    public int getWaySize() {
        return this.waynodes.size();
    }

    public long getFirstWayNode() {
        return this.waynodes.get(0);
    }

    public long getLastWayNode() {
        return this.waynodes.get(waynodes.size() - 1);
    }

    public ArrayList<Long> getWaynodes() {
        return waynodes;
    }

    /**
     * Merges the parameter into this object when the parameter way follows this way.
     *
     * @param toBeMerged
     * @return wasMerged
     */
    public boolean mergeInto(TrimmedWay toBeMerged) {
        if (this.getLastWayNode() == toBeMerged.getFirstWayNode()) {
            toBeMerged.removeFirst();
            this.addAllNodes(toBeMerged.getWaynodes());
            return true;
        }
        return false;
    }
}
