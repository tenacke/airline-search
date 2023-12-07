import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Main {
    public record Node() {
        int id;
        int lat;
        int lon;
        int airfield;
        int waitCost;
        LinkedList<Node> adjacents;
        LinkedList<Integer> distance;
    }

    public record Airfield() {
        int id;
        int time;
        int weather;
    }    

    public record PriorityObject() {
        int id;
        int time;
        int parent;
        float g;
        float h;
    }

    public record Mission() {
        int startNode;
        int startTime;
        int endNode;
    }

    static final String printFormat = "%d %d %d\n"; // id, time, cost
    static final boolean dijkstra = true;  // Change this to false to use A* algorithm
    static final long sixHours = 21600; // 6 hours in seconds
    static final int maxTime = 30; // Maximum time for the weather data
    static final int edgeConstraint = 500; // Maximum edge distance in meters

    HashMap<Integer, Node> nodes = new HashMap<>(); // Key: node_id
    HashMap<String, Airfield> airfields = new HashMap<>(); // Key: "<airfield_id>_<time>" so all airfields are unique
    
    BufferedReader inputReader = new BufferedReader(new FileReader(new File("input.txt")));
    BufferedReader dataReader = new BufferedReader(new FileReader(new File("data.csv")));

    private void readInput() throws IOException {
        int n = Integer.parseInt(reader.readLine());
        for (int i = 0; i < n; i++) {
            String[] line = reader.readLine().split(" ");
            int id = Integer.parseInt(line[0]);
            int lat = Integer.parseInt(line[1]);
            int lon = Integer.parseInt(line[2]);
            int airfield = Integer.parseInt(line[3]);
            int waitCost = Integer.parseInt(line[4]);
            nodes.put(id, new Node(id, lat, lon, airfield, waitCost, new LinkedList<>(), new LinkedList<>())); // Create node
        }
    }

    private void readData() throws IOException {
        dataReader.readLine(); // Skip first line
        // read the data as csv format for the columns (airfield, time, weather)
        String line;
        while ((line = dataReader.readLine()) != null) {
            String[] data = line.split(",");
            int airfield = Integer.parseInt(data[0]);
            long time = Long.parseLong(data[1]);
            int weather = Integer.parseInt(data[2]);
            airfields.put(airfield + "_" + time, new Airfield(airfield, time, weather));
        }
    }

    private void readEdges() throws IOException {
        String line;
        while ((line = inputReader.readLine()) != null) {
            String[] data = line.split(" ");
            int from = Integer.parseInt(data[0]);
            int to = Integer.parseInt(data[1]);
            int distance = Integer.parseInt(data[2]);
            Node node = nodes.get(from);
            node.adjacents.add(nodes.get(to));
            node.distance.add(distance);
        }
    }

    private void applyAlgorithm(Mission mission) throws IOException {
        PriorityQueue<PriorityObject> queue = new PriorityQueue<>((a, b) -> { 
            return Float.compare(a.g + a.h, b.g + b.h);
        });
        PriorityObject result;
        HashSet<String> visited = new HashSet<>(); // Key: "<node_id>_<time>" so all nodes are unique
        queue.add(new PriorityObject(mission.startNode, mission.startTime, -1, 0, 0));
        while (!queue.isEmpty()){
            PriorityObject current = queue.poll();
            visited.add(current.id + "_" + current.time);
            if (current.id == mission.endNode) {
                result = current;
                break;
            }
            Node currentNode = nodes.get(current.id);
            LinkedList<Node> adjacents = currentNode.adjacents;
            LinkedList<Integer> distances = currentNode.distance;
            if (!visited.contains(current.id + "_" + (current.time + sixHours)))
                queue.add(new PriorityObject(current.id, current.time + sixHours, current.id, current.g + currentNode.waitCost, current.h));
            for (int i = 0; i < adjacents.size(); i++) {
                Node adjacent = adjacents.get(i);
                int distance = distances.get(i);
                int time = current.time + calculateTravelTime(currentNode, adjacent, distance);
                if (time > maxTime)
                    continue;
                String key = adjacent.id + "_" + time;
                if (!visited.contains(key)) {
                    int parent = current.id;
                    int g = current.g + calculateCost(currentNode, adjacent, distance, current.time, time);
                    int h = calculateHeuristic(adjacent, time);
                    queue.add(new PriorityObject(adjacent.id, time, current.id, g, h));
                }
            }
        }
        if (result) {
            while (result.parent != -1) {
                System.out.printf(printFormat, result.id, result.time, result.g);
                result = result.parent;
            }
        } else 
            System.out.println("No feasible path");
    }

    private int calculateDepartureConditions(int weather) {
        return 0;
    }

    private int calculateArrivalConditions(int weather) {
        return 0;
    }

    private int calculateCost(Node node1, Node node2, int distance, int time1, int time2) {
        int weather1 = airfields.get(node1.airfield + "_" + time1).weather;
        int weather2 = airfields.get(node2.airfield + "_" + time2).weather;
        int departureConditions = calculateDepartureConditions(weather1);
        int arrivalConditions = calculateArrivalConditions(weather2);
        return 0; // TODO - Implement this
    }

    private int calculateTravelTime(int distance) {
        return 0; 
    }

    private int calculateHeuristic(Node node, int time) {
        if (dijkstra)
            return 0;
        return 0; // TODO - Implement this
    }

    private void run() throws IOException {
        readData();
        readInput();
        readEdges();
        readMissions();
        for (Mission mission : missions)
            applyAlgorithm(mission);
    }

    private void runGraph() throws IOException {
        readInput();
        for (Node node : nodes.values()) {
            for (Node adjacent : nodes.values()) {
                int distance = calculateDistance(node, adjacent);
                if (distance < edgeConstraint) {
                    node.adjacents.add(adjacent);
                    node.distance.add(distance);
                }
            }
        }
    }

    private int calculateDistance(Node node1, Node node2) {
        return haversine(node1.lat, node1.lon, node2.lat, node2.lon);
    }

    private int haversine(int lat1, int lon1, int lat2, int lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        int result = (int) Math.round(6371000 * c);
        return result;
    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.runGraph();
        main.run();
    }
}