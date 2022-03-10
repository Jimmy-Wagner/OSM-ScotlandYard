package Types;

import org.jgrapht.graph.DefaultEdge;

public class MyEdge extends DefaultEdge {
    private final RouteType type;

    public MyEdge(RouteType type) {
        this.type = type;
    }

    public RouteType getType() {
        return type;
    }
}
