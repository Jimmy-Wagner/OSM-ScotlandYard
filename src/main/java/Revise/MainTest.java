package Revise;

import org.openstreetmap.osmosis.core.domain.v0_6.Bound;

public class MainTest {
    public static void main(String[] args) {
        Bound bound = new Bound(-0.1018810272216797, -0.15337944030761722, 51.52335065301965 , 51.4912972909506, "");
        PbfFileReader reader = new PbfFileReader(bound);
        OsmDataContainer container = reader.readFile("/Users/jimmy/Desktop/london/boundingtest.osm.pbf");
        System.out.println(container.getAllContainedNodes().size());
        /*
        System.out.println("Osm bounding: " + reader.osmDataBoundingBox.getLeft() + " | " + reader.osmDataBoundingBox.getRight() +
                " | " +reader.osmDataBoundingBox.getTop() + " | " + reader.osmDataBoundingBox.getBottom());
        System.out.println("Map bounding: " + reader.imageBoundingbox.getLeft() + " | " + reader.imageBoundingbox.getRight() +
                " | " +reader.imageBoundingbox.getTop());
        System.out.println("Contained nodes: " + reader.allContainedNodes.size());
        System.out.println("Bus relations: " + reader.busRouteRelations.size());
        System.out.println("Ways: " + reader.allContainedWays.size());
        OsmDataContainer container = new OsmDataContainer(reader.allContainedNodes,
                reader.allContainedWays,
                reader.busRouteRelations,
                reader.platformRelations);
        System.out.println("Platform relations before: " + reader.platformRelations.size());
        System.out.println("Platform relations after: " + container.platformRelations.size());
        System.out.println("Busrelations before:" + reader.busRouteRelations.size());
        System.out.println("Busrewlaitons after: " + container.getBusRouteRelations().size());*/
    }
}
