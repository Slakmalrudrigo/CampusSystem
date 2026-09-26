package universitysystem;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DataManager dataManager = new DataManager();
    private static WebServer webServer = null;

    public static void main(String[] args) {

        System.out.println("==================================================================");
        System.out.println("  CIT300 Data Structures and Algorithms - Assignment 1           ");
        System.out.println("  UNIVERSITY STUDENT RECORD & CAMPUS ROUTE MANAGEMENT SYSTEM     ");
        System.out.println("==================================================================");

        boolean running = true;

        while (running) {

            displayMenu();

            int choice = readInt("Enter your choice (1-17): ");

            switch (choice) {

                case 1:
                    addStudentRecord();
                    break;

                case 2:
                    updateStudentRecord();
                    break;

                case 3:
                    deleteStudentRecord();
                    break;

                case 4:
                    displayAllRecordsLinkedList();
                    break;

                case 5:
                    addServiceRequestToQueue();
                    break;

                case 6:
                    processNextServiceRequest();
                    break;

                case 7:
                    displayRecentActionsStack();
                    break;

                case 8:
                    displayStudentsBST();
                    break;

                case 9:
                    searchStudentHashing();
                    break;

                case 10:
                    addCampusLocation();
                    break;

                case 11:
                    removeCampusLocation();
                    break;

                case 12:
                    addCampusConnection();
                    break;

                case 13:
                    removeCampusConnection();
                    break;

                case 14:
                    displayCampusConnections();
                    break;

                case 15:
                    traverseCampusLocations();
                    break;

                case 16:
                    launchWebVisualizer();
                    break;

                case 17:
                    running = false;
                    if (webServer != null) webServer.stop();
                    System.out.println("\nThank you for using the University System. Exiting...");
                    break;

                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 17.");
            }
        }

        scanner.close();
    }

    // Display Main Menu (Specification Options 1 - 16 + Web Dashboard Option 17)
    private static void displayMenu() {
        System.out.println("\n=================== SYSTEM MENU ===================");
        System.out.println("1.  Add Student Record");
        System.out.println("2.  Update Student Record");
        System.out.println("3.  Delete Student Record");
        System.out.println("4.  Display All Records using Linked List");
        System.out.println("5.  Add Service Request to Queue");
        System.out.println("6.  Process Next Service Request");
        System.out.println("7.  Display Recent Actions using Stack");
        System.out.println("8.  Display Students using BST/AVL");
        System.out.println("9.  Search Student using Hashing");
        System.out.println("10. Add Campus Location");
        System.out.println("11. Remove Campus Location");
        System.out.println("12. Add Campus Connection/Road");
        System.out.println("13. Remove Campus Connection/Road");
        System.out.println("14. Display Campus Connections");
        System.out.println("15. Traverse Campus Locations using BFS or DFS (and Shortest Path)");
        System.out.println("16. Launch Interactive Web Visualizer Dashboard");
        System.out.println("17. Exit");
        System.out.println("===================================================");
    }

    // 1. Add Student Record
    private static void addStudentRecord() {
        System.out.println("\n--- 1. Add Student Record ---");
        String studentId = readText("Enter Student ID: ");

        if (dataManager.getStudentList().searchStudent(studentId) != null) {
            System.out.println("❌ Error: Duplicate Student ID. Record already exists.");
            return;
        }

        String name = readText("Enter Student Name: ");
        String programme = readText("Enter Programme: ");
        double marks = readMarks("Enter Marks (0-100): ");

        Student student = new Student(studentId, name, programme, marks);
        if (dataManager.addStudent(student)) {
            System.out.println("✅ Student added successfully to Linked List, BST, and Hash Table.");
        } else {
            System.out.println("❌ Failed to add student record.");
        }
    }

    // 2. Update Student Record
    private static void updateStudentRecord() {
        System.out.println("\n--- 2. Update Student Record ---");
        String studentId = readText("Enter Student ID to update: ");

        if (dataManager.getStudentList().searchStudent(studentId) == null) {
            System.out.println("❌ Error: Student ID not found.");
            return;
        }

        String name = readText("Enter New Name: ");
        String programme = readText("Enter New Programme: ");
        double marks = readMarks("Enter New Marks (0-100): ");

        if (dataManager.updateStudent(studentId, name, programme, marks)) {
            System.out.println("✅ Student updated successfully across all data structures.");
        } else {
            System.out.println("❌ Failed to update student record.");
        }
    }

    // 3. Delete Student Record
    private static void deleteStudentRecord() {
        System.out.println("\n--- 3. Delete Student Record ---");
        String studentId = readText("Enter Student ID to delete: ");

        if (dataManager.deleteStudent(studentId)) {
            System.out.println("✅ Student deleted successfully. Operation pushed to Action Stack (Undo available).");
        } else {
            System.out.println("❌ Error: Student ID not found.");
        }
    }

    // 4. Display All Records using Linked List
    private static void displayAllRecordsLinkedList() {
        dataManager.getStudentList().displayAllStudents();
    }

    // 5. Add Service Request to Queue
    private static void addServiceRequestToQueue() {
        System.out.println("\n--- 5. Add Service Request to Queue ---");
        String studentId = readText("Enter Student ID: ");
        String category = readText("Enter Request Category (Academic / IT / Hostel / Financial): ");
        String description = readText("Enter Request Description: ");

        ServiceRequestQueue.ServiceRequest request = dataManager.getServiceQueue().enqueue(studentId, category, description);
        System.out.println("✅ Service request added to Queue: Ticket #" + request.getRequestId());
    }

    // 6. Process Next Service Request
    private static void processNextServiceRequest() {
        System.out.println("\n--- 6. Process Next Service Request ---");
        ServiceRequestQueue.ServiceRequest processed = dataManager.getServiceQueue().dequeue();

        if (processed != null) {
            System.out.println("✅ Processed Service Request (FIFO):");
            System.out.println(processed);
        } else {
            System.out.println("⚠️ Service request queue is empty.");
        }
    }

    // 7. Display Recent Actions using Stack
    private static void displayRecentActionsStack() {
        dataManager.getActionStack().displayActions();
        if (!dataManager.getActionStack().isEmpty()) {
            System.out.print("\nDo you want to UNDO the most recent action? (y/n): ");
            String choice = scanner.nextLine().trim();
            if (choice.equalsIgnoreCase("y")) {
                if (dataManager.undoLastAction()) {
                    System.out.println("✅ Last action undone successfully.");
                }
            }
        }
    }

    // 8. Display Students using BST/AVL
    private static void displayStudentsBST() {
        System.out.println("\n--- 8. Display Students using Binary Search Tree (BST) ---");
        System.out.println("Tree Height: " + dataManager.getStudentBST().getHeight() + " | Total Nodes: " + dataManager.getStudentBST().getNodeCount());
        dataManager.getStudentBST().displayInOrder();
    }

    // 9. Search Student using Hashing
    private static void searchStudentHashing() {
        System.out.println("\n--- 9. Search Student using Hashing ---");
        String studentId = readText("Enter Student ID to search: ");

        long startTime = System.nanoTime();
        Student student = dataManager.getStudentHashTable().search(studentId);
        long elapsedTime = System.nanoTime() - startTime;

        if (student != null) {
            System.out.println("✅ Student Found via Hash Table O(1) Search:");
            System.out.println(student);
            System.out.println("⚡ Search Time: " + elapsedTime + " nanoseconds");
            int bucket = dataManager.getStudentHashTable().hashFunction(studentId);
            System.out.println("📍 Hash Index / Bucket: " + bucket);
        } else {
            System.out.println("❌ Student not found in Hash Table.");
        }
    }

    // 10. Add Campus Location
    private static void addCampusLocation() {
        System.out.println("\n--- 10. Add Campus Location ---");
        String location = readText("Enter Location Name: ");

        if (dataManager.getCampusGraph().addLocation(location)) {
            System.out.println("✅ Campus location '" + location + "' added to graph.");
        } else {
            System.out.println("❌ Location already exists or invalid.");
        }
    }

    // 11. Remove Campus Location
    private static void removeCampusLocation() {
        System.out.println("\n--- 11. Remove Campus Location ---");
        String location = readText("Enter Location Name to remove: ");

        if (dataManager.getCampusGraph().removeLocation(location)) {
            System.out.println("✅ Location '" + location + "' and its connections removed.");
        } else {
            System.out.println("❌ Location not found in campus graph.");
        }
    }

    // 12. Add Campus Connection/Road
    private static void addCampusConnection() {
        System.out.println("\n--- 12. Add Campus Connection/Road ---");
        String loc1 = readText("Enter First Location: ");
        String loc2 = readText("Enter Second Location: ");
        int distance = readInt("Enter Distance in meters (e.g., 150): ");

        if (dataManager.getCampusGraph().addConnection(loc1, loc2, distance)) {
            System.out.println("✅ Connection created between '" + loc1 + "' and '" + loc2 + "' (" + distance + "m).");
        } else {
            System.out.println("❌ Failed to create connection. Check if locations exist and are not already connected.");
        }
    }

    // 13. Remove Campus Connection/Road
    private static void removeCampusConnection() {
        System.out.println("\n--- 13. Remove Campus Connection/Road ---");
        String loc1 = readText("Enter First Location: ");
        String loc2 = readText("Enter Second Location: ");

        if (dataManager.getCampusGraph().removeConnection(loc1, loc2)) {
            System.out.println("✅ Connection removed between '" + loc1 + "' and '" + loc2 + "'.");
        } else {
            System.out.println("❌ Connection not found.");
        }
    }

    // 14. Display Campus Connections
    private static void displayCampusConnections() {
        dataManager.getCampusGraph().displayConnections();
    }

    // 15. Traverse Campus Locations using BFS or DFS
    private static void traverseCampusLocations() {
        System.out.println("\n--- 15. Traverse Campus Locations ---");
        System.out.println("1. Breadth-First Search (BFS)");
        System.out.println("2. Depth-First Search (DFS)");
        System.out.println("3. Find Shortest Path (Dijkstra's Algorithm)");
        int choice = readInt("Select traversal option (1-3): ");

        String startLoc = readText("Enter Starting Location: ");

        if (choice == 1) {
            List<String> bfsOrder = dataManager.getCampusGraph().bfs(startLoc);
            if (bfsOrder.isEmpty()) {
                System.out.println("❌ Location not found.");
            } else {
                System.out.println("\n===== BFS Campus Traversal =====");
                System.out.println(String.join(" -> ", bfsOrder));
            }
        } else if (choice == 2) {
            List<String> dfsOrder = dataManager.getCampusGraph().dfs(startLoc);
            if (dfsOrder.isEmpty()) {
                System.out.println("❌ Location not found.");
            } else {
                System.out.println("\n===== DFS Campus Traversal =====");
                System.out.println(String.join(" -> ", dfsOrder));
            }
        } else if (choice == 3) {
            String targetLoc = readText("Enter Target Destination Location: ");
            CampusGraph.ShortestPathResult result = dataManager.getCampusGraph().findShortestPath(startLoc, targetLoc);
            System.out.println("\n🎯 Dijkstra Shortest Path Result:");
            System.out.println(result);
        }
    }

    // 16. Launch Interactive Web Visualizer Dashboard
    private static void launchWebVisualizer() {
        try {
            if (webServer == null) {
                webServer = new WebServer(dataManager, 8080);
                webServer.start();
            } else {
                System.out.println("Web server is already running at http://localhost:8080");
            }

            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            System.err.println("Failed to launch web server: " + e.getMessage());
        }
    }

    // Input Helpers
    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static String readText(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty.");
        }
    }

    private static double readMarks(String message) {
        while (true) {
            System.out.print(message);
            try {
                double marks = Double.parseDouble(scanner.nextLine().trim());
                if (marks >= 0 && marks <= 100) return marks;
                System.out.println("Marks must be between 0 and 100.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
