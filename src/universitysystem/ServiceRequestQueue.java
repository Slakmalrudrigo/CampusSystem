package universitysystem;

import java.util.ArrayList;
import java.util.List;

public class ServiceRequestQueue {

    public static class ServiceRequest {
        private final String requestId;
        private final String studentId;
        private final String category;
        private final String description;
        private String status;
        private final String timestamp;

        public ServiceRequest(String requestId, String studentId, String category, String description) {
            this.requestId = requestId;
            this.studentId = studentId;
            this.category = category;
            this.description = description;
            this.status = "Pending";
            this.timestamp = java.time.LocalTime.now().toString().substring(0, 8);
        }

        public String getRequestId() { return requestId; }
        public String getStudentId() { return studentId; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getTimestamp() { return timestamp; }
        public void setStatus(String status) { this.status = status; }

        public String toJson() {
            return String.format("{\"requestId\":\"%s\",\"studentId\":\"%s\",\"category\":\"%s\",\"description\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
                    escapeJson(requestId), escapeJson(studentId), escapeJson(category),
                    escapeJson(description), escapeJson(status), timestamp);
        }

        private String escapeJson(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        @Override
        public String toString() {
            return String.format("[%s] Ticket #%-6s | Student: %-8s | Cat: %-12s | Status: %-10s | %s",
                    timestamp, requestId, studentId, category, status, description);
        }
    }

    // Node for Queue
    private static class QueueNode {
        ServiceRequest request;
        QueueNode next;

        QueueNode(ServiceRequest request) {
            this.request = request;
            this.next = null;
        }
    }

    private QueueNode front;
    private QueueNode rear;
    private int ticketCounter = 1001;
    private int size = 0;

    // Enqueue a service request object
    public ServiceRequest enqueue(String studentId, String category, String description) {
        String requestId = "REQ-" + ticketCounter++;
        ServiceRequest request = new ServiceRequest(requestId, studentId, category, description);
        enqueue(request);
        return request;
    }

    public void enqueue(ServiceRequest request) {
        QueueNode newNode = new QueueNode(request);
        if (rear == null) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    // Process the next request
    public ServiceRequest dequeue() {
        if (front == null) {
            return null;
        }
        ServiceRequest request = front.request;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        request.setStatus("Resolved");
        return request;
    }

    // View the next request
    public ServiceRequest peek() {
        if (front == null) {
            return null;
        }
        return front.request;
    }

    // Check if queue is empty
    public boolean isEmpty() {
        return front == null;
    }

    public int getSize() {
        return size;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    // Get all pending requests as a list
    public List<ServiceRequest> getAllRequests() {
        List<ServiceRequest> list = new ArrayList<>();
        QueueNode current = front;
        while (current != null) {
            list.add(current.request);
            current = current.next;
        }
        return list;
    }

    // Display all requests
    public void displayRequests() {
        if (front == null) {
            System.out.println("No service requests in the queue.");
            return;
        }

        System.out.println("\n===== Pending Student Service Requests (FIFO) =====");
        QueueNode current = front;
        int count = 1;
        while (current != null) {
            System.out.println(count++ + ". " + current.request);
            current = current.next;
        }
    }

    // JSON export for web UI
    public String toJson() {
        StringBuilder sb = new StringBuilder("[");
        QueueNode current = front;
        boolean first = true;
        while (current != null) {
            if (!first) sb.append(",");
            sb.append(current.request.toJson());
            first = false;
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
