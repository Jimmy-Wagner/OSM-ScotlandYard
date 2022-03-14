import com.mapbox.geojson.Point;
import crosby.binary.osmosis.OsmosisReader;
import org.locationtech.jts.util.Stopwatch;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class CenterPbfReader implements Sink {

    private Point centerOfOsmData;
    private Bound bound;

    @Override
    public void process(EntityContainer entityContainer) {
        if (entityContainer instanceof BoundContainer){
            this.bound = ((BoundContainer) entityContainer).getEntity();
            double centerLatide = (bound.getTop()+bound.getBottom())/2;
            double centerLongitude = (bound.getRight()+bound.getLeft())/2;
            this.centerOfOsmData = Point.fromLngLat(centerLongitude, centerLatide);
            return;
        }
    }

    @Override
    public void initialize(Map<String, Object> map) {

    }

    @Override
    public void complete() {

    }

    @Override
    public void close() {

    }


    public Point readFile(String pathToFile){
        Stopwatch watch = new Stopwatch();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(pathToFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Could not find that file!");
        }
        OsmosisReader reader = new OsmosisReader(inputStream);
        reader.setSink(this);
        reader.run();

        return centerOfOsmData;

    }
}
