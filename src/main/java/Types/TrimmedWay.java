package Types;

import java.util.ArrayList;

/**
 * A trimmed way consistes of multiple connected ways of one relation in the bounding box of the map image.
 * If two ways of one relation are not connected in the bounding box of the map image bacause the route goes out and later on again back to
 * the map image, it will result in two seperate trimmedways.
 */
public class TrimmedWay {
    ArrayList<Long> waynodes = new ArrayList<Long>();

    public TrimmedWay(){

    }

    public void addNode(long id){
        this.waynodes.add(id);
    }

    public void addAllNodes(ArrayList<Long> nodes){
        this.waynodes.addAll(nodes);
    }

    public void removeFirst(){
        this.waynodes.remove(0);
    }

    public int getWaySize(){
        return this.waynodes.size();
    }

    public long getFirstWayNode(){
        return this.waynodes.get(0);
    }

    public long getLastWayNode(){
        return this.waynodes.get(waynodes.size()-1);
    }

    public ArrayList<Long> getWaynodes() {
        return waynodes;
    }

    /**
     * Merges the parameter into this object when the parameter way follows this way.
     * @param toBeMerged
     * @return wasMerged
     */
    public boolean mergeInto(TrimmedWay toBeMerged){
        if (this.getLastWayNode() == toBeMerged.getFirstWayNode()){
            toBeMerged.removeFirst();
            this.addAllNodes(toBeMerged.getWaynodes());
            return true;
        }
        return false;
    }
}
