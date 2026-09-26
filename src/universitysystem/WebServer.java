package universitysystem;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WebServer {

    private final DataManager dataManager;
    private final int port;
    private HttpServer server;

    public WebServer(DataManager dataManager, int port) {
        this.dataManager = dataManager;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints
        server.createContext("/api/students", new StudentsHandler());
        server.createContext("/api/students/add", new AddStudentHandler());
        server.createContext("/api/students/update", new UpdateStudentHandler());
        server.createContext("/api/students/delete", new DeleteStudentHandler());
        server.createContext("/api/undo", new UndoHandler());

        server.createContext("/api/bst", new BSTHandler());
        server.createContext("/api/hashtable", new HashTableHandler());
        server.createContext("/api/requests", new RequestsHandler());
        server.createContext("/api/requests/dequeue", new DequeueRequestHandler());

        server.createContext("/api/stack", new StackHandler());
        server.createContext("/api/campus", new CampusHandler());
        server.createContext("/api/shortest-path", new ShortestPathHandler());
        server.createContext("/api/benchmark", new BenchmarkHandler());

        // Static Web Files Handler
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // default executor
        server.start();
        System.out.println("\n=======================================================");
        System.out.println(" 🚀 Web Dashboard Running at: http://localhost:" + port);
        System.out.println("=======================================================\n");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // --- Handlers ---

    private class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                StringBuilder sb = new StringBuilder("[");
                java.util.List<Student> list = dataManager.getStudentList().searchStudent("") != null ?
                        dataManager.getStudentHashTable().getAllStudents() : dataManager.getStudentHashTable().getAllStudents();
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(list.get(i).toJson());
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class AddStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> body = parseFormData(readBody(exchange));
                String id = body.get("studentId");
                String name = body.get("name");
                String prog = body.get("programme");
                String marksStr = body.get("marks");

                if (id == null || name == null || prog == null || marksStr == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing required fields\"}");
                    return;
                }

                try {
                    double marks = Double.parseDouble(marksStr);
                    Student s = new Student(id.trim(), name.trim(), prog.trim(), marks);
                    boolean ok = dataManager.addStudent(s);
                    if (ok) {
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student added successfully\"}");
                    } else {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Student ID already exists\"}");
                    }
                } catch (NumberFormatException e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid marks numeric value\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class UpdateStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> body = parseFormData(readBody(exchange));
                String id = body.get("studentId");
                String name = body.get("name");
                String prog = body.get("programme");
                String marksStr = body.get("marks");

                try {
                    double marks = Double.parseDouble(marksStr);
                    boolean ok = dataManager.updateStudent(id.trim(), name.trim(), prog.trim(), marks);
                    if (ok) {
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student updated successfully\"}");
                    } else {
                        sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Student not found\"}");
                    }
                } catch (Exception e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid request format\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class DeleteStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) || "DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String id = params.get("id");
                if (id == null) {
                    Map<String, String> body = parseFormData(readBody(exchange));
                    id = body.get("id");
                }
                if (id != null && dataManager.deleteStudent(id.trim())) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student deleted successfully\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Student not found\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class UndoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                boolean ok = dataManager.undoLastAction();
                if (ok) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Last action undone successfully\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"No actions in stack to undo\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class BSTHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                StudentBST bst = dataManager.getStudentBST();
                String treeJson = bst.toTreeJson();

                StringBuilder inOrderSb = new StringBuilder("[");
                java.util.List<Student> inOrderList = bst.getInOrderList();
                for (int i = 0; i < inOrderList.size(); i++) {
                    if (i > 0) inOrderSb.append(",");
                    inOrderSb.append(inOrderList.get(i).toJson());
                }
                inOrderSb.append("]");

                String response = String.format("{\"height\":%d,\"nodeCount\":%d,\"tree\":%s,\"inOrder\":%s}",
                        bst.getHeight(), bst.getNodeCount(), treeJson, inOrderSb.toString());
                sendJsonResponse(exchange, 200, response);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class HashTableHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                StudentHashTable ht = dataManager.getStudentHashTable();
                String json = String.format("{\"tableSize\":%d,\"collisions\":%d,\"table\":%s}",
                        ht.getTableSize(), ht.getCollisionCount(), ht.toTableJson());
                sendJsonResponse(exchange, 200, json);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class RequestsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, dataManager.getServiceQueue().toJson());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> body = parseFormData(readBody(exchange));
                String id = body.get("studentId");
                String cat = body.get("category");
                String desc = body.get("description");
                if (id != null && cat != null && desc != null) {
                    ServiceRequestQueue.ServiceRequest req = dataManager.getServiceQueue().enqueue(id.trim(), cat.trim(), desc.trim());
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"request\":" + req.toJson() + "}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing request parameters\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class DequeueRequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                ServiceRequestQueue.ServiceRequest processed = dataManager.getServiceQueue().dequeue();
                if (processed != null) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"processed\":" + processed.toJson() + "}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Service queue is empty\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class StackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, dataManager.getActionStack().toJson());
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class CampusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, dataManager.getCampusGraph().toJson());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> body = parseFormData(readBody(exchange));
                String type = body.get("type");
                if ("location".equalsIgnoreCase(type)) {
                    String name = body.get("name");
                    if (name != null && dataManager.getCampusGraph().addLocation(name.trim())) {
                        sendJsonResponse(exchange, 200, "{\"success\":true}");
                    } else {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Location exists or invalid\"}");
                    }
                } else if ("connection".equalsIgnoreCase(type)) {
                    String loc1 = body.get("loc1");
                    String loc2 = body.get("loc2");
                    String distStr = body.get("distance");
                    int dist = 100;
                    try { if (distStr != null) dist = Integer.parseInt(distStr); } catch (Exception ignored) {}
                    if (dataManager.getCampusGraph().addConnection(loc1, loc2, dist)) {
                        sendJsonResponse(exchange, 200, "{\"success\":true}");
                    } else {
                        sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Connection invalid or exists\"}");
                    }
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class ShortestPathHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String start = params.get("start");
                String target = params.get("target");

                if (start != null && target != null) {
                    CampusGraph.ShortestPathResult result = dataManager.getCampusGraph().findShortestPath(start.trim(), target.trim());
                    sendJsonResponse(exchange, 200, result.toJson());
                } else {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Missing start or target parameter\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class BenchmarkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String id = params.get("id");
                if (id == null || id.isEmpty()) id = "STU101";
                String json = dataManager.runBenchmark(id.trim());
                sendJsonResponse(exchange, 200, json);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                sendJsonResponse(exchange, 404, "<h1>404 Not Found</h1>");
                return;
            }

            String contentType = "text/html";
            if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".json")) contentType = "application/json";
            else if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";

            byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // --- Helpers ---

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private Map<String, String> parseFormData(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;

        // Support JSON input if sent as JSON
        if (body.trim().startsWith("{") && body.trim().endsWith("}")) {
            String clean = body.trim().substring(1, body.trim().length() - 1);
            String[] pairs = clean.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String k = kv[0].trim().replaceAll("^\"|\"$", "");
                    String v = kv[1].trim().replaceAll("^\"|\"$", "");
                    map.put(k, v);
                }
            }
            return map;
        }

        // Support URL form encoded input
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                    String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                    map.put(k, v);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                    String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                    map.put(k, v);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }
}
