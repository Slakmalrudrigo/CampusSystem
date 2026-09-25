package universitysystem;

import java.util.*;

public class CampusGraph {

    public static class Edge {
        private final String target;
        private final int distanceInMeters;

        public Edge(String target, int distanceInMeters) {
            this.target = target;
            this.distanceInMeters = distanceInMeters;
        }

        public String getTarget() { return target; }
        public int getDistanceInMeters() { return distanceInMeters; }
    }

    public static class ShortestPathResult {
        private final List<String> path;
        private final int totalDistance;

        public ShortestPathResult(List<String> path, int totalDistance) {
            this.path = path;
            this.totalDistance = totalDistance;
        }

        public List<String> getPath() { return path; }
        public int getTotalDistance() { return totalDistance; }

        @Override
        public String toString() {
            if (path.isEmpty()) return "No route available.";
            return String.join(" -> ", path) + " (Total Distance: " + totalDistance + " meters)";
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"totalDistance\":").append(totalDistance).append(",\"path\":[");
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(path.get(i).replace("\"", "\\\"")).append("\"");
            }
            sb.append("]}");
            return sb.toString();
        }
    }

    private final Map<String, List<Edge>> adjacencyList;

    public CampusGraph() {
        adjacencyList = new LinkedHashMap<>();
    }

    // Add a location
    public boolean addLocation(String location) {
        if (location == null || location.trim().isEmpty() || adjacencyList.containsKey(location.trim())) {
            return false;
        }
        adjacencyList.put(location.trim(), new ArrayList<>());
        return true;
    }

    // Remove location
    public boolean removeLocation(String location) {
        if (location == null || !adjacencyList.containsKey(location.trim())) {
            return false;
        }
        String loc = location.trim();
        for (List<Edge> edges : adjacencyList.values()) {
            edges.removeIf(e -> e.getTarget().equalsIgnoreCase(loc));
        }
        adjacencyList.remove(loc);
        return true;
    }

    // Add connection with distance (default 100 meters if unassigned)
    public boolean addConnection(String loc1, String loc2) {
        return addConnection(loc1, loc2, 100);
    }

    public boolean addConnection(String loc1, String loc2, int distanceInMeters) {
        if (loc1 == null || loc2 == null) return false;
        String l1 = loc1.trim();
        String l2 = loc2.trim();
        if (!adjacencyList.containsKey(l1) || !adjacencyList.containsKey(l2) || l1.equalsIgnoreCase(l2)) {
            return false;
        }

        // Check if edge already exists
        for (Edge e : adjacencyList.get(l1)) {
            if (e.getTarget().equalsIgnoreCase(l2)) return false;
        }

        adjacencyList.get(l1).add(new Edge(l2, distanceInMeters));
        adjacencyList.get(l2).add(new Edge(l1, distanceInMeters));
        return true;
    }

    // Remove connection
    public boolean removeConnection(String loc1, String loc2) {
        if (loc1 == null || loc2 == null) return false;
        String l1 = loc1.trim();
        String l2 = loc2.trim();
        if (!adjacencyList.containsKey(l1) || !adjacencyList.containsKey(l2)) {
            return false;
        }

        boolean r1 = adjacencyList.get(l1).removeIf(e -> e.getTarget().equalsIgnoreCase(l2));
        boolean r2 = adjacencyList.get(l2).removeIf(e -> e.getTarget().equalsIgnoreCase(l1));
        return r1 && r2;
    }

    // Get list of all locations
    public Set<String> getLocations() {
        return adjacencyList.keySet();
    }

    // Display all campus connections
    public void displayConnections() {
        if (adjacencyList.isEmpty()) {
            System.out.println("No campus locations found.");
            return;
        }

        System.out.println("\n===== Campus Graph Connections =====");
        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            System.out.print(entry.getKey() + " -> ");
            if (entry.getValue().isEmpty()) {
                System.out.println("No connections");
            } else {
                List<String> desc = new ArrayList<>();
                for (Edge e : entry.getValue()) {
                    desc.add(e.getTarget() + " (" + e.getDistanceInMeters() + "m)");
                }
                System.out.println(String.join(", ", desc));
            }
        }
    }

    // BFS Traversal
    public List<String> bfs(String startLocation) {
        List<String> visitedOrder = new ArrayList<>();
        if (startLocation == null || !adjacencyList.containsKey(startLocation.trim())) {
            return visitedOrder;
        }
        String start = startLocation.trim();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            visitedOrder.add(current);

            for (Edge edge : adjacencyList.get(current)) {
                if (!visited.contains(edge.getTarget())) {
                    visited.add(edge.getTarget());
                    queue.add(edge.getTarget());
                }
            }
        }
        return visitedOrder;
    }

    // DFS Traversal
    public List<String> dfs(String startLocation) {
        List<String> visitedOrder = new ArrayList<>();
        if (startLocation == null || !adjacencyList.containsKey(startLocation.trim())) {
            return visitedOrder;
        }
        Set<String> visited = new HashSet<>();
        dfsRecursive(startLocation.trim(), visited, visitedOrder);
        return visitedOrder;
    }

    private void dfsRecursive(String current, Set<String> visited, List<String> visitedOrder) {
        visited.add(current);
        visitedOrder.add(current);
        for (Edge edge : adjacencyList.get(current)) {
            if (!visited.contains(edge.getTarget())) {
                dfsRecursive(edge.getTarget(), visited, visitedOrder);
            }
        }
    }

    // Dijkstra's Shortest Path Algorithm
    public ShortestPathResult findShortestPath(String startLocation, String targetLocation) {
        if (startLocation == null || targetLocation == null) {
            return new ShortestPathResult(Collections.emptyList(), 0);
        }
        String start = startLocation.trim();
        String target = targetLocation.trim();

        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(target)) {
            return new ShortestPathResult(Collections.emptyList(), 0);
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (String node : adjacencyList.keySet()) {
            if (node.equalsIgnoreCase(start)) {
                distances.put(node, 0);
            } else {
                distances.put(node, Integer.MAX_VALUE);
            }
            pq.add(node);
        }

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (current.equalsIgnoreCase(target)) break;
            if (distances.get(current) == Integer.MAX_VALUE) break;

            for (Edge edge : adjacencyList.get(current)) {
                String neighbor = edge.getTarget();
                int newDist = distances.get(current) + edge.getDistanceInMeters();
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    // Re-order PQ
                    pq.remove(neighbor);
                    pq.add(neighbor);
                }
            }
        }

        if (!distances.containsKey(target) || distances.get(target) == Integer.MAX_VALUE) {
            return new ShortestPathResult(Collections.emptyList(), 0);
        }

        List<String> path = new LinkedList<>();
        String curr = target;
        while (curr != null) {
            path.add(0, curr);
            curr = previous.get(curr);
        }

        return new ShortestPathResult(path, distances.get(target));
    }

    // JSON export for Web UI Canvas rendering
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"nodes\":[");
        boolean firstNode = true;
        for (String loc : adjacencyList.keySet()) {
            if (!firstNode) sb.append(",");
            sb.append("{\"id\":\"").append(escapeJson(loc)).append("\"}");
            firstNode = false;
        }
        sb.append("],\"edges\":[");
        boolean firstEdge = true;
        Set<String> processedEdges = new HashSet<>();

        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            String source = entry.getKey();
            for (Edge edge : entry.getValue()) {
                String target = edge.getTarget();
                String key1 = source + "->" + target;
                String key2 = target + "->" + source;
                if (!processedEdges.contains(key1) && !processedEdges.contains(key2)) {
                    if (!firstEdge) sb.append(",");
                    sb.append(String.format("{\"source\":\"%s\",\"target\":\"%s\",\"distance\":%d}",
                            escapeJson(source), escapeJson(target), edge.getDistanceInMeters()));
                    processedEdges.add(key1);
                    firstEdge = false;
                }
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

