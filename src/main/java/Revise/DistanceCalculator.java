package Revise;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;

public class DistanceCalculator {
    /**
     * Calculates the distance in meters of two nodes
     * TODO: Funktioniert einwandfrei
     *
     * @param node1
     * @param node2
     * @return distance in meters
     */
    public static double calculateDistanceTwoNodes(Node node1, Node node2) {
        if (node1 == null || node2 == null) {
            return Double.MAX_VALUE;
        }
        final double p = 0.017453292519943295;    // Math.PI / 180
        double lat1 = node1.getLatitude();
        double lon1 = node1.getLongitude();
        double lat2 = node2.getLatitude();
        double lon2 = node2.getLongitude();
        double a = 0.5 - Math.cos((lat2 - lat1) * p) / 2 +
                Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                        (1 - Math.cos((lon2 - lon1) * p)) / 2;

        return 12742 * Math.asin(Math.sqrt(a)) * 1000; // 2 * R; R = 6371 km
    }
}
