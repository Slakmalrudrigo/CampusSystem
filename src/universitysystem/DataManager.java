package universitysystem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DataManager {

    private final StudentLinkedList studentList;
    private final StudentBST studentBST;
    private final StudentHashTable studentHashTable;
    private final ActionStack actionStack;
    private final ServiceRequestQueue serviceQueue;
    private final CampusGraph campusGraph;

    private static final String DATA_DIR = "data";
    private static final String STUDENTS_FILE = DATA_DIR + "/students.txt";
    private static final String CAMPUS_FILE = DATA_DIR + "/campus.txt";
    private static final String REQUESTS_FILE = DATA_DIR + "/requests.txt";

    public DataManager() {
        this.studentList = new StudentLinkedList();
        this.studentBST = new StudentBST();
        this.studentHashTable = new StudentHashTable();
        this.actionStack = new ActionStack();
        this.serviceQueue = new ServiceRequestQueue();
        this.campusGraph = new CampusGraph();

        // Pre-seed sample data automatically
        loadSampleData();
    }

    public StudentLinkedList getStudentList() { return studentList; }
    public StudentBST getStudentBST() { return studentBST; }
    public StudentHashTable getStudentHashTable() { return studentHashTable; }
    public ActionStack getActionStack() { return actionStack; }
    public ServiceRequestQueue getServiceQueue() { return serviceQueue; }
    public CampusGraph getCampusGraph() { return campusGraph; }

    // Synchronized Add Student
    public synchronized boolean addStudent(Student student) {
        if (student == null || student.getStudentId() == null) return false;
        String id = student.getStudentId();

        // Check if student already exists in list or table
        if (studentList.searchStudent(id) != null) {
            return false;
        }

        studentList.addStudent(student);
        studentBST.insert(student);
        studentHashTable.insert(student);
        actionStack.push(ActionStack.ActionType.ADD, student, null);
        return true;
    }

    // Synchronized Update Student
    public synchronized boolean updateStudent(String studentId, String name, String programme, double marks) {
        Student current = studentList.searchStudent(studentId);
        if (current == null) return false;

        // Clone previous state for Undo
        Student previousState = new Student(current.getStudentId(), current.getName(), current.getProgramme(), current.getMarks());

        // Update in LinkedList
        boolean updatedList = studentList.updateStudent(studentId, name, programme, marks);
        if (!updatedList) return false;

        // Re-sync BST (delete old and insert updated node)
        studentBST.delete(studentId);
        Student updatedStudent = studentList.searchStudent(studentId);
        studentBST.insert(updatedStudent);

        // Update in HashTable
        studentHashTable.insert(updatedStudent);

        // Push to stack
        actionStack.push(ActionStack.ActionType.UPDATE, updatedStudent, previousState);
        return true;
    }

    // Synchronized Delete Student
    public synchronized boolean deleteStudent(String studentId) {
        Student studentToDelete = studentList.searchStudent(studentId);
        if (studentToDelete == null) return false;

        // Clone state for Undo
        Student clone = new Student(studentToDelete.getStudentId(), studentToDelete.getName(), studentToDelete.getProgramme(), studentToDelete.getMarks());

        studentList.deleteStudent(studentId);
        studentBST.delete(studentId);
        studentHashTable.delete(studentId);

        actionStack.push(ActionStack.ActionType.DELETE, clone, null);
        return true;
    }

    // Undo Last Action
    public synchronized boolean undoLastAction() {
        if (actionStack.isEmpty()) return false;

        ActionStack.ActionRecord record = actionStack.pop();
        if (record == null) return false;

        ActionStack.ActionType type = record.getType();
        Student target = record.getTargetStudent();

        if (type == ActionStack.ActionType.ADD) {
            // Reverse ADD -> Delete student
            String id = target.getStudentId();
            studentList.deleteStudent(id);
            studentBST.delete(id);
            studentHashTable.delete(id);
        } else if (type == ActionStack.ActionType.DELETE) {
            // Reverse DELETE -> Re-add student
            studentList.addStudent(target);
            studentBST.insert(target);
            studentHashTable.insert(target);
        } else if (type == ActionStack.ActionType.UPDATE) {
            // Reverse UPDATE -> Restore previous state
            Student prev = record.getPreviousStudentState();
            if (prev != null) {
                String id = prev.getStudentId();
                studentList.updateStudent(id, prev.getName(), prev.getProgramme(), prev.getMarks());
                studentBST.delete(id);
                Student restored = studentList.searchStudent(id);
                studentBST.insert(restored);
                studentHashTable.insert(restored);
            }
        }

        return true;
    }

    // Pre-seed sample campus map, students, and requests
    public void loadSampleData() {
        // Pre-seed Students
        addStudent(new Student("STU101", "Alice Johnson", "Computer Science", 88.5));
        addStudent(new Student("STU102", "Bob Smith", "Software Engineering", 74.0));
        addStudent(new Student("STU103", "Charlie Brown", "Data Science", 92.0));
        addStudent(new Student("STU104", "Diana Prince", "Cyber Security", 65.5));
        addStudent(new Student("STU105", "Evan Wright", "Artificial Intelligence", 81.0));
        addStudent(new Student("STU106", "Fiona Gallagher", "Information Tech", 58.0));
        addStudent(new Student("STU107", "George Clark", "Computer Science", 49.5));
        addStudent(new Student("STU108", "Hannah Abbott", "Software Engineering", 95.0));

        // Pre-seed Campus Locations & Distances
        campusGraph.addLocation("Main Gate");
        campusGraph.addLocation("Administration");
        campusGraph.addLocation("Library");
        campusGraph.addLocation("Science Complex");
        campusGraph.addLocation("Computer Lab");
        campusGraph.addLocation("Student Center");
        campusGraph.addLocation("Sports Arena");
        campusGraph.addLocation("Hostel Village");

        campusGraph.addConnection("Main Gate", "Administration", 150);
        campusGraph.addConnection("Main Gate", "Student Center", 200);
        campusGraph.addConnection("Administration", "Library", 120);
        campusGraph.addConnection("Administration", "Science Complex", 250);
        campusGraph.addConnection("Library", "Computer Lab", 80);
        campusGraph.addConnection("Library", "Student Center", 100);
        campusGraph.addConnection("Science Complex", "Computer Lab", 180);
        campusGraph.addConnection("Student Center", "Sports Arena", 300);
        campusGraph.addConnection("Student Center", "Hostel Village", 350);
        campusGraph.addConnection("Sports Arena", "Hostel Village", 150);

        // Pre-seed Service Requests
        serviceQueue.enqueue("STU101", "Academic", "Request official transcript for graduate application.");
        serviceQueue.enqueue("STU103", "Financial", "Inquiry regarding scholarship disbursement status.");
        serviceQueue.enqueue("STU104", "IT Support", "Reset Wi-Fi portal password.");
        serviceQueue.enqueue("STU105", "Hostel", "Room maintenance request - air conditioning check.");
    }

    // Benchmark search across data structures
    public String runBenchmark(String searchId) {
        int iterations = 100000;

        // 1. Linked List Search
        long startList = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            studentList.searchStudent(searchId);
        }
        long timeList = System.nanoTime() - startList;

        // 2. BST Search
        long startBST = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            studentBST.search(searchId);
        }
        long timeBST = System.nanoTime() - startBST;

        // 3. Hash Table Search
        long startHT = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            studentHashTable.search(searchId);
        }
        long timeHT = System.nanoTime() - startHT;

        double avgListUs = (double) timeList / (iterations * 1000);
        double avgBSTUs = (double) timeBST / (iterations * 1000);
        double avgHTUs = (double) timeHT / (iterations * 1000);

        return String.format(
            "{\"iterations\":%d,\"searchTarget\":\"%s\"," +
            "\"linkedListNano\":%d,\"linkedListAvgUs\":%.4f," +
            "\"bstNano\":%d,\"bstAvgUs\":%.4f," +
            "\"hashTableNano\":%d,\"hashTableAvgUs\":%.4f}",
            iterations, searchId,
            timeList, avgListUs,
            timeBST, avgBSTUs,
            timeHT, avgHTUs
        );
    }

    // Save Data Persistence to Disk
    public boolean saveAllData() {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) dir.mkdirs();

            // Save Students
            PrintWriter pwStudents = new PrintWriter(new FileWriter(STUDENTS_FILE));
            for (Student s : studentHashTable.getAllStudents()) {
                pwStudents.println(s.getStudentId() + "," + s.getName() + "," + s.getProgramme() + "," + s.getMarks());
            }
            pwStudents.close();

            // Save Requests
            PrintWriter pwReqs = new PrintWriter(new FileWriter(REQUESTS_FILE));
            for (ServiceRequestQueue.ServiceRequest r : serviceQueue.getAllRequests()) {
                pwReqs.println(r.getStudentId() + "|" + r.getCategory() + "|" + r.getDescription());
            }
            pwReqs.close();

            return true;
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
            return false;
        }
    }

    // Load Data Persistence from Disk
    public boolean loadAllData() {
        try {
            if (Files.exists(Paths.get(STUDENTS_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(STUDENTS_FILE));
                studentBST.clear();
                studentHashTable.clear();
                actionStack.clear();

                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        Student s = new Student(parts[0].trim(), parts[1].trim(), parts[2].trim(), Double.parseDouble(parts[3].trim()));
                        studentList.addStudent(s);
                        studentBST.insert(s);
                        studentHashTable.insert(s);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            return false;
        }
    }
}
