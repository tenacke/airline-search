import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Main {
    public record Node(String id, double lat, double lon, String airfield, double waitCost, LinkedList<Node> adjacents, LinkedList<Double> distance) { }

    public record Airfield(String id, long time, int weather) { }    

    public record PriorityObject(String id, long time, PriorityObject parent, double g, double h) { }

    public record Mission(String startNode, String endNode, long startTime, long deadline) { }

    static final String input = "AS-1";
    static final boolean edges = true; // Change this to false to calculate edges
    static final boolean dijkstra = false;  // Change this to false to use A* algorithm
    static final long sixHours = 21600; // 6 hours in seconds
    static final long minTime = 1680307200;
    static final long maxTime = 1682899200; // Maximum time for the weather data
    String aircraftType; // Maximum edge distance in meters
    int edgeConstraint;
    int _18hourLimit;
    int _12hourLimit;

    HashMap<String, Node> nodes = new HashMap<>(); // Key: node_id
    HashMap<String, Airfield> airfields = new HashMap<>(); // Key: "<airfield_id>_<time>" so all airfields are unique
    LinkedList<Mission> missions = new LinkedList<>();
    
    BufferedReader inputReader;
    BufferedReader dataReader;
    BufferedReader missionReader;
    BufferedReader edgeReader;

    BufferedWriter edgesWriter;
    BufferedWriter pathWriter;

    public Main() throws IOException { // INITIALIZE READERS
        inputReader = new BufferedReader(new FileReader(new File("cases/data/" + input + ".csv")));
        dataReader = new BufferedReader(new FileReader(new File("cases/weather.csv")));
        missionReader = new BufferedReader(new FileReader(new File("cases/flights/" + input + ".in")));
        if (edges){
            edgeReader = new BufferedReader(new FileReader(new File("cases/edges/" + input + ".csv")));
            pathWriter = new BufferedWriter(new FileWriter(new File("path.out")));
        }
        else 
            edgesWriter = new BufferedWriter(new FileWriter(new File("cases/edges/" + input + ".csv")));
    }

    private void readInput() throws IOException { // INITIALIZE NODES
        inputReader.readLine(); // Skip first line
        // read the input as csv format for the columns (id, lat, lon, airfield, waitCost)
        String data;
        while ((data = inputReader.readLine()) != null) {
            String[] line = data.split(",");
            String id = line[0];
            String airfield = line[1];
            double lat = Double.parseDouble(line[2]);
            double lon = Double.parseDouble(line[3]);
            double waitCost = Double.parseDouble(line[4]);
            nodes.put(id, new Node(id, lat, lon, airfield, waitCost, new LinkedList<>(), new LinkedList<>())); // Create node
        }
    }

    private void readData() throws IOException { // INITIALIZE AIRFIELDS
        dataReader.readLine(); // Skip first line
        // read the data as csv format for the columns (airfield, time, weather)
        String line;
        while ((line = dataReader.readLine()) != null) {
            String[] data = line.split(",");
            String airfield = data[0];
            long time = Long.parseLong(data[1]);
            int weather = Integer.parseInt(data[2]);
            airfields.put(airfield + "_" + time, new Airfield(airfield, time, weather));
        }
    }

    private void readEdges() throws IOException { // FILL ADJACENT NODES
        edgeReader.readLine(); // Skip first line
        // read the input as csv format for the columns (from, to, distance)
        String line;
        while ((line = edgeReader.readLine()) != null) {
            String[] data = line.split(",");
            String from = data[0];
            String to = data[1];
            double distance = Double.parseDouble(data[2]);
            Node node = nodes.get(from);
            node.adjacents.add(nodes.get(to));
            node.distance.add(distance);
        }
    }

    private void readLimits() throws IOException { // RUN BEFORE MISSIONS
        aircraftType = missionReader.readLine().strip(); 
        switch (aircraftType) {
            case "Carreidas 160":
                edgeConstraint = 500;
                _18hourLimit = 400;
                _12hourLimit = 300;
                break;
            
            case "Orion III":
                edgeConstraint = 1000;
                _18hourLimit = 750;
                _12hourLimit = 500;
                break;

            case "Skyfleet S570":
                edgeConstraint = 1500;
                _18hourLimit = 1200;
                _12hourLimit = 900;
                break;

            case "T-16 Skyhopper":
                edgeConstraint = 2000;
                _18hourLimit = 1500;
                _12hourLimit = 1000;
                break;
        }
    }

    private void readMissions() throws IOException { // READ MISSIONS
        String line;
        while ((line = missionReader.readLine()) != null) {
            if (line.charAt(0) == '#')
                continue;
            String[] data = line.split(" ");
            String startNode = data[0];
            String endNode = data[1];
            long startTime = Long.parseLong(data[2]);
            Long deadline = Long.parseLong(data[3]);
            // TODO - Add deadline and start time
            missions.add(new Mission(startNode, endNode, startTime, deadline));
        }
    }

    private void applyAlgorithm(Mission mission) throws IOException {
        // TODO - Add deadline
        PriorityQueue<PriorityObject> queue = new PriorityQueue<>((a, b) -> { 
            return Double.compare(a.g + a.h, b.g + b.h);
        });
        PriorityObject result = null;
        HashSet<String> visited = new HashSet<>(); // Key: "<node_id>_<time>" so all nodes are unique
        queue.add(new PriorityObject(mission.startNode, mission.startTime, null, 0, 0));
        while (!queue.isEmpty()){
            PriorityObject current = queue.poll();
            visited.add(current.id + "_" + current.time);
            // System.out.println(current.id + " " + current.time);
            if (current.id.equals(mission.endNode)) {
                result = current;
                break;
            }
            Node currentNode = nodes.get(current.id);
            LinkedList<Node> adjacents = currentNode.adjacents;
            LinkedList<Double> distances = currentNode.distance;
            if ((current.time + sixHours <= mission.deadline) && !visited.contains(current.id + "_" + (current.time + sixHours)))
                queue.add(new PriorityObject(current.id, current.time + sixHours, current, current.g + currentNode.waitCost, current.h));
            for (int i = 0; i < adjacents.size(); i++) {
                Node adjacent = adjacents.get(i);
                double distance = distances.get(i);
                // System.out.println(adjacent.id + " " + distance);
                long time = current.time + calculateTravelTime(distance);
                String key = adjacent.id + "_" + time;
                if (time <= mission.deadline && !visited.contains(key)) {
                    double g = current.g + calculateCost(currentNode, adjacent, distance, current.time, time);
                    double h = calculateHeuristic(adjacent, mission.endNode, time);
                    queue.add(new PriorityObject(adjacent.id, time, current, g, h));
                }
            }
        }
        if (result != null) {
            double cost = 0d;
            StringBuilder builder = new StringBuilder();
            while (result != null) {
                builder.append(result.id).append(" ");
                cost += result.g;
                result = result.parent;
            }
            builder.append(cost);
            pathWriter.write(builder.toString() + "\n");
            System.out.println(builder.toString());
        } else {
            pathWriter.write("No feasible path\n");
            System.out.println("No feasible path");
        }
        pathWriter.flush();
    }

    private double calculateWeatherMultplier(int weather) {
        int Bw = (weather & 0b00010000) >> 4;
        int Br = (weather & 0b00001000) >> 3;
        int Bs = (weather & 0b00000100) >> 2;
        int Bh = (weather & 0b00000010) >> 1;
        int Bb = weather & 0b00000001;
        double result = (Bw * 1.05d + (1 - Bw)) * 
                        (Br * 1.05d + (1 - Br)) * (Bs * 1.10d + (1 - Bs))*
                        (Bh * 1.15d + (1 - Bh)) * (Bb * 1.20d + (1 - Bb));
        return result;
    }

    private double calculateCost(Node node1, Node node2, double distance, long time1, long time2) {
        int fixedCost = 300;
        int weather1 = airfields.get(node1.airfield + "_" + time1).weather;
        int weather2 = airfields.get(node2.airfield + "_" + time2).weather;
        double departureMultiplier = calculateWeatherMultplier(weather1);
        double arrivalMultiplier = calculateWeatherMultplier(weather2);
        double result = fixedCost * departureMultiplier * arrivalMultiplier + distance;
        return result;
    }

    private long calculateTravelTime(double distance) {
        if (distance > _18hourLimit)
            return 3*sixHours;
        else if (distance > _12hourLimit)
            return 2*sixHours;
        else
            return sixHours;
    }

    private double calculateHeuristic(Node node, String target, long time) {
        if (dijkstra)
            return 0d;
        Node targetNode = nodes.get(target);
        return calculateDistance(node, targetNode); // TODO - Implement this
    }

    private void run() throws IOException {
        readData();
        readInput();
        readEdges();
        readLimits();
        readMissions();
        for (Mission mission : missions)
            applyAlgorithm(mission);
    }

    private void runGraph() throws IOException {
        readInput();
        readLimits();
        calculateEdges();
        printEdges();
    }

    private void calculateEdges() {
        for (Node node : nodes.values()) {
            for (Node adjacent : nodes.values()) {
                if (node.id.equals(adjacent.id))
                    continue;
                double distance = calculateDistance(node, adjacent);
                // System.out.println(distance);
                if (distance <= edgeConstraint) {
                    // System.out.println("Added" + node.id + " " + adjacent.id);
                    node.adjacents.add(adjacent);
                    node.distance.add(distance);
                }
            }
        }
    }

    private void printEdges() throws IOException {
        edgesWriter.write("from,to,distance\n"); // TODO - Might need to change this
        for (Node node : nodes.values()) {
            for (int i = 0; i < node.adjacents.size(); i++) {
                Node adjacent = node.adjacents.get(i);
                double distance = node.distance.get(i);
                edgesWriter.write(node.id + "," + adjacent.id + "," + distance + "\n");
            }
            edgesWriter.flush();
        }
    }

    private double calculateDistance(Node node1, Node node2) {
        return haversine(node1.lat, node1.lon, node2.lat, node2.lon);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double result = 6371 * c;
        return result;
    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        if (edges)
            main.run();
        else
            main.runGraph();
    }
}
