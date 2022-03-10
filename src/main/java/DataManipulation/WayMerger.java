package DataManipulation;


import DataContainer.OsmDataContainer;
import Types.TrimmedWay;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;

/**
 * This class calculates the similarity of ways with the frechet distance and merges the ways if they are similiar enough.
 */
public class WayMerger {

    private OsmDataContainer dataContainer;
    private OsmDataHandler dataHandler;

    public WayMerger(){

    }

    public ArrayList<TrimmedWay> mergeWays(ArrayList<TrimmedWay> trimmedWays){
        return null;
    }

}
