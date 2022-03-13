public class PixelSegment {
    private RealPixelNode node1;
    private RealPixelNode node2;
    private RouteType type;

    public PixelSegment(RealPixelNode node1, RealPixelNode node2, RouteType type){
        this.node1 = node1;
        this.node2 = node2;
        this.type = type;
    }

}
