import org.locationtech.jts.geom.*;

public class ConfiguredLineString extends LineString {

    public ConfiguredLineString(Coordinate[] points, PrecisionModel precisionModel, int SRID) {
        super(points, precisionModel, SRID);
    }

    public ConfiguredLineString(CoordinateSequence points, GeometryFactory factory) {
        super(points, factory);
    }


}
