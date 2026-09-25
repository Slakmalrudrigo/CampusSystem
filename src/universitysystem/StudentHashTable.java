package universitysystem;

import java.util.ArrayList;
import java.util.List;

public class StudentHashTable {

    // Node for the hash table
    public static class HashNode {
        public Student student;
        public HashNode next;

        HashNode(Student student) {
            this.student = student;
            this.next = null;
        }
    }

    private static final int TABLE_SIZE = 10;
    private HashNode[] table;

    // Constructor
    public StudentHashTable() {
        table = new HashNode[TABLE_SIZE];
    }

    public int getTableSize() {
        return TABLE_SIZE;
    }

    // Hash function
    public int hashFunction(String studentId) {
        if (studentId == null) return 0;
        int hash = 0;
        for (int i = 0; i < studentId.length(); i++) {
            hash = hash + studentId.charAt(i);
        }
        return Math.abs(hash) % TABLE_SIZE;
    }

    // Add student
    public boolean insert(Student student) {
        if (student == null || student.getStudentId() == null) return false;
        String studentId = student.getStudentId();
        int index = hashFunction(studentId);

        HashNode current = table[index];

        // Check duplicate ID (or update if existing)
        while (current != null) {
            if (current.student.getStudentId().equalsIgnoreCase(studentId)) {
                current.student = student; // Update existing record
                return true;
            }
            current = current.next;
        }

        // Add new node at front of chain
        HashNode newNode = new HashNode(student);
        newNode.next = table[index];
        table[index] = newNode;
        return true;
    }

    // Search student
    public Student search(String studentId) {
        if (studentId == null) return null;
        int index = hashFunction(studentId);
        HashNode current = table[index];

        while (current != null) {
            if (current.student.getStudentId().equalsIgnoreCase(studentId)) {
                return current.student;
            }
            current = current.next;
        }
        return null;
    }

    // Delete student
    public boolean delete(String studentId) {
        if (studentId == null) return false;
        int index = hashFunction(studentId);

        HashNode current = table[index];
        HashNode previous = null;

        while (current != null) {
            if (current.student.getStudentId().equalsIgnoreCase(studentId)) {
                if (previous == null) {
                    table[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    // Get list of all students
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        for (int i = 0; i < TABLE_SIZE; i++) {
            HashNode current = table[i];
            while (current != null) {
                list.add(current.student);
                current = current.next;
            }
        }
        return list;
    }

    // Get collision count
    public int getCollisionCount() {
        int collisions = 0;
        for (int i = 0; i < TABLE_SIZE; i++) {
            int count = 0;
            HashNode current = table[i];
            while (current != null) {
                count++;
                current = current.next;
            }
            if (count > 1) {
                collisions += (count - 1);
            }
        }
        return collisions;
    }

    // Clear hash table
    public void clear() {
        table = new HashNode[TABLE_SIZE];
    }

    // Display hash table
    public void displayTable() {
        System.out.println("\n===== Hash Table =====");
        for (int i = 0; i < TABLE_SIZE; i++) {
            System.out.print("Index " + i + ": ");
            HashNode current = table[i];
            if (current == null) {
                System.out.println("Empty");
                continue;
            }
            while (current != null) {
                System.out.print("[" + current.student.getStudentId() + ": " + current.student.getName() + "]");
                if (current.next != null) {
                    System.out.print(" -> ");
                }
                current = current.next;
            }
            System.out.println();
        }
    }

    // JSON output for web inspection
    public String toTableJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < TABLE_SIZE; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"index\":").append(i).append(",\"chain\":[");
            HashNode current = table[i];
            boolean first = true;
            while (current != null) {
                if (!first) sb.append(",");
                sb.append(String.format("{\"id\":\"%s\",\"name\":\"%s\"}",
                        escapeJson(current.student.getStudentId()),
                        escapeJson(current.student.getName())));
                first = false;
                current = current.next;
            }
            sb.append("]}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
