package DataManipulation.BO;

import Types.PointBO;
import bentleyottmann.IPoint;
import bentleyottmann.IPointFactory;
import org.jetbrains.annotations.NotNull;

public class PointFactoryBO implements IPointFactory {
    @Override
    public @NotNull IPoint create(double v, double v1) {
        return new PointBO(v, v1);
    }
}
