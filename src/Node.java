import java.util.*;

public class Node {
    public String id;
    public double lat;
    public double lon;
    public String airfield;
    public double waitCost;
    public LinkedList<Node> adjacents;
    public LinkedList<Double> distance;

    public Node(String id, double lat, double lon, String airfield, double waitCost, LinkedList<Node> adjacents, LinkedList<Double> distance) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.airfield = airfield;
        this.waitCost = waitCost;
        this.adjacents = adjacents;
        this.distance = distance;
    }

    public boolean equals(Node node2){
        return (id.equals(node2.id));
    }
}


