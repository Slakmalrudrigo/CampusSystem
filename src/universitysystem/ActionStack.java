package universitysystem;

import java.util.ArrayList;
import java.util.List;

public class ActionStack {

    public enum ActionType {
        ADD, UPDATE, DELETE
    }

    public static class ActionRecord {
        private final ActionType type;
        private final Student targetStudent;
        private final Student previousStudentState; // For UPDATE undo
        private final String timestamp;

        public ActionRecord(ActionType type, Student targetStudent, Student previousStudentState) {
            this.type = type;
            this.targetStudent = targetStudent;
            this.previousStudentState = previousStudentState;
            this.timestamp = java.time.LocalTime.now().toString().substring(0, 8);
        }

        public ActionType getType() {
            return type;
        }

        public Student getTargetStudent() {
            return targetStudent;
        }

        public Student getPreviousStudentState() {
            return previousStudentState;
        }

        public String getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            String details = "";
            if (type == ActionType.ADD) {
                details = "Added student " + targetStudent.getStudentId() + " (" + targetStudent.getName() + ")";
            } else if (type == ActionType.DELETE) {
                details = "Deleted student " + targetStudent.getStudentId() + " (" + targetStudent.getName() + ")";
            } else if (type == ActionType.UPDATE) {
                details = "Updated student " + targetStudent.getStudentId();
            }
            return "[" + timestamp + "] " + type + ": " + details;
        }

        public String toJson() {
            return String.format("{\"timestamp\":\"%s\",\"type\":\"%s\",\"studentId\":\"%s\",\"name\":\"%s\"}",
                    timestamp, type,
                    targetStudent != null ? targetStudent.getStudentId() : "",
                    targetStudent != null ? escapeJson(targetStudent.getName()) : "");
        }

        private String escapeJson(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    // Node for Stack
    private static class StackNode {
        ActionRecord record;
        StackNode next;

        StackNode(ActionRecord record) {
            this.record = record;
            this.next = null;
        }
    }

    private StackNode top;
    private int size = 0;

    // Push an action record onto stack
    public void push(ActionRecord record) {
        StackNode newNode = new StackNode(record);
        newNode.next = top;
        top = newNode;
        size++;
    }

    // Push simple string helper
    public void push(ActionType type, Student targetStudent, Student previousStudentState) {
        push(new ActionRecord(type, targetStudent, previousStudentState));
    }

    // Pop the most recent action record
    public ActionRecord pop() {
        if (top == null) {
            return null;
        }
        ActionRecord record = top.record;
        top = top.next;
        size--;
        return record;
    }

    // View the most recent action
    public ActionRecord peek() {
        if (top == null) {
            return null;
        }
        return top.record;
    }

    // Check if stack is empty
    public boolean isEmpty() {
        return top == null;
    }

    public int getSize() {
        return size;
    }

    // Clear stack
    public void clear() {
        top = null;
        size = 0;
    }

    // Get all records as a list
    public List<ActionRecord> getAllRecords() {
        List<ActionRecord> list = new ArrayList<>();
        StackNode current = top;
        while (current != null) {
            list.add(current.record);
            current = current.next;
        }
        return list;
    }

    // Display all recent actions
    public void displayActions() {
        if (top == null) {
            System.out.println("No recent actions.");
            return;
        }

        System.out.println("\n===== Action History Stack (Top to Bottom) =====");
        StackNode current = top;
        int index = 1;
        while (current != null) {
            System.out.println(index++ + ". " + current.record);
            current = current.next;
        }
    }

    // JSON export for web UI
    public String toJson() {
        StringBuilder sb = new StringBuilder("[");
        StackNode current = top;
        boolean first = true;
        while (current != null) {
            if (!first) sb.append(",");
            sb.append(current.record.toJson());
            first = false;
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
