package universitysystem;

import java.util.List;

public class TestRunner {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   UNIVERSITY STUDENT CAMPUS SYSTEM - TEST SUITE   ");
        System.out.println("==================================================");

        testStudentModel();
        testStudentLinkedList();
        testStudentBST();
        testStudentHashTable();
        testActionStackUndo();
        testServiceRequestQueue();
        testCampusGraphDijkstra();
        testDataManagerSync();

        System.out.println("\n--------------------------------------------------");
        System.out.println("TEST SUMMARY: Passed = " + testsPassed + " | Failed = " + testsFailed);
        System.out.println("==================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertCondition(String testName, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void testStudentModel() {
        System.out.println("\n--- Testing Student Model ---");
        Student s = new Student("STU999", "Test User", "CS", 87.5);
        assertCondition("Student Grade is A+", "A+".equals(s.getGrade()));
        assertCondition("Student GPA is 4.00", s.getGpa() == 4.00);
        assertCondition("Student Email contains name & id", s.getEmail().contains("testuser") && s.getEmail().contains("stu999"));
        assertCondition("Student JSON is non-empty", s.toJson().contains("STU999"));
    }

    private static void testStudentLinkedList() {
        System.out.println("\n--- Testing Student LinkedList ---");
        StudentLinkedList list = new StudentLinkedList();
        Student s1 = new Student("L1", "Link One", "IT", 70);
        Student s2 = new Student("L2", "Link Two", "SE", 80);

        assertCondition("Add s1 to LinkedList", list.addStudent(s1));
        assertCondition("Add s2 to LinkedList", list.addStudent(s2));
        assertCondition("Prevent duplicate add", !list.addStudent(s1));
        assertCondition("Search s1", list.searchStudent("L1") != null);
        assertCondition("Update s1", list.updateStudent("L1", "Link One Updated", "IT", 75));
        assertCondition("Verify updated name", "Link One Updated".equals(list.searchStudent("L1").getName()));
        assertCondition("Delete s1", list.deleteStudent("L1"));
        assertCondition("Verify s1 deleted", list.searchStudent("L1") == null);
    }

    private static void testStudentBST() {
        System.out.println("\n--- Testing Student BST ---");
        StudentBST bst = new StudentBST();
        Student s1 = new Student("M20", "Middle", "CS", 80);
        Student s2 = new Student("A10", "Left", "CS", 85);
        Student s3 = new Student("Z90", "Right", "CS", 90);

        assertCondition("Insert BST Root", bst.insert(s1));
        assertCondition("Insert BST Left", bst.insert(s2));
        assertCondition("Insert BST Right", bst.insert(s3));
        assertCondition("Prevent Duplicate BST", !bst.insert(s1));

        assertCondition("BST Search existing", bst.search("A10") != null);
        assertCondition("BST Search missing", bst.search("MISSING") == null);

        List<Student> inOrder = bst.getInOrderList();
        assertCondition("In-Order traversal size is 3", inOrder.size() == 3);
        assertCondition("In-Order first element is A10", "A10".equals(inOrder.get(0).getStudentId()));

        assertCondition("BST Delete Leaf (A10)", bst.delete("A10"));
        assertCondition("Verify Leaf Deleted", bst.search("A10") == null);
        assertCondition("BST Node Count after deletion", bst.getNodeCount() == 2);
    }

    private static void testStudentHashTable() {
        System.out.println("\n--- Testing Student HashTable ---");
        StudentHashTable ht = new StudentHashTable();
        Student s1 = new Student("H100", "Hash One", "DS", 82);
        Student s2 = new Student("H200", "Hash Two", "AI", 91);

        assertCondition("HashTable Insert s1", ht.insert(s1));
        assertCondition("HashTable Insert s2", ht.insert(s2));
        assertCondition("HashTable Search s1", ht.search("H100") != null);

        assertCondition("HashTable Delete s1", ht.delete("H100"));
        assertCondition("Verify s1 Deleted from HT", ht.search("H100") == null);
    }

    private static void testActionStackUndo() {
        System.out.println("\n--- Testing ActionStack & Undo ---");
        ActionStack stack = new ActionStack();
        Student s1 = new Student("S1", "Stack One", "SE", 60);

        stack.push(ActionStack.ActionType.ADD, s1, null);
        assertCondition("Stack size is 1", stack.getSize() == 1);
        assertCondition("Peek returns ADD", stack.peek().getType() == ActionStack.ActionType.ADD);

        ActionStack.ActionRecord popped = stack.pop();
        assertCondition("Popped item equals s1 ID", "S1".equals(popped.getTargetStudent().getStudentId()));
        assertCondition("Stack is empty after pop", stack.isEmpty());
    }

    private static void testServiceRequestQueue() {
        System.out.println("\n--- Testing ServiceRequestQueue ---");
        ServiceRequestQueue queue = new ServiceRequestQueue();
        ServiceRequestQueue.ServiceRequest r1 = queue.enqueue("STU1", "IT", "Forgot Password");
        ServiceRequestQueue.ServiceRequest r2 = queue.enqueue("STU2", "Academic", "Grade Dispute");

        assertCondition("Queue size is 2", queue.getSize() == 2);
        assertCondition("Peek request is r1", "STU1".equals(queue.peek().getStudentId()));

        ServiceRequestQueue.ServiceRequest dequeued = queue.dequeue();
        assertCondition("Dequeued request is r1 (FIFO)", "STU1".equals(dequeued.getStudentId()));
        assertCondition("Dequeued request status is Resolved", "Resolved".equals(dequeued.getStatus()));
        assertCondition("Queue size after dequeue is 1", queue.getSize() == 1);
    }

    private static void testCampusGraphDijkstra() {
        System.out.println("\n--- Testing CampusGraph & Dijkstra Shortest Path ---");
        CampusGraph graph = new CampusGraph();
        graph.addLocation("Gate");
        graph.addLocation("Admin");
        graph.addLocation("Lab");

        graph.addConnection("Gate", "Admin", 100);
        graph.addConnection("Admin", "Lab", 50);
        graph.addConnection("Gate", "Lab", 300);

        List<String> bfs = graph.bfs("Gate");
        assertCondition("BFS starts with Gate", "Gate".equals(bfs.get(0)));

        List<String> dfs = graph.dfs("Gate");
        assertCondition("DFS starts with Gate", "DFS".length() > 0 && "Gate".equals(dfs.get(0)));

        CampusGraph.ShortestPathResult result = graph.findShortestPath("Gate", "Lab");
        assertCondition("Shortest path distance is 150m (Gate->Admin->Lab)", result.getTotalDistance() == 150);
        assertCondition("Shortest path includes Admin", result.getPath().contains("Admin"));
    }

    private static void testDataManagerSync() {
        System.out.println("\n--- Testing DataManager Synchronization & Undo ---");
        DataManager dm = new DataManager();
        Student s = new Student("SYNC01", "Sync Test", "AI", 90);

        assertCondition("DataManager Add Student", dm.addStudent(s));
        assertCondition("Found in LinkedList", dm.getStudentList().searchStudent("SYNC01") != null);
        assertCondition("Found in BST", dm.getStudentBST().search("SYNC01") != null);
        assertCondition("Found in HashTable", dm.getStudentHashTable().search("SYNC01") != null);

        // Test Undo Add
        assertCondition("Undo last action (ADD)", dm.undoLastAction());
        assertCondition("Removed from LinkedList via Undo", dm.getStudentList().searchStudent("SYNC01") == null);
        assertCondition("Removed from BST via Undo", dm.getStudentBST().search("SYNC01") == null);
        assertCondition("Removed from HashTable via Undo", dm.getStudentHashTable().search("SYNC01") == null);
    }
}
