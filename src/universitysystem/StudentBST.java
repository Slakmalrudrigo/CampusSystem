package universitysystem;

import java.util.ArrayList;
import java.util.List;

public class StudentBST {

    // Node of the Binary Search Tree
    public static class TreeNode {
        public Student student;
        public TreeNode left;
        public TreeNode right;

        TreeNode(Student student) {
            this.student = student;
            this.left = null;
            this.right = null;
        }
    }

    private TreeNode root;

    public TreeNode getRoot() {
        return root;
    }

    // Insert a student
    public boolean insert(Student student) {
        if (student == null || student.getStudentId() == null) return false;
        if (root == null) {
            root = new TreeNode(student);
            return true;
        }
        return insertRecursive(root, student);
    }

    private boolean insertRecursive(TreeNode current, Student student) {
        int comparison = student.getStudentId().compareToIgnoreCase(current.student.getStudentId());

        if (comparison == 0) {
            return false; // Duplicate ID
        }

        if (comparison < 0) {
            if (current.left == null) {
                current.left = new TreeNode(student);
                return true;
            }
            return insertRecursive(current.left, student);
        } else {
            if (current.right == null) {
                current.right = new TreeNode(student);
                return true;
            }
            return insertRecursive(current.right, student);
        }
    }

    // Search student by ID
    public Student search(String studentId) {
        if (studentId == null) return null;
        TreeNode current = root;
        while (current != null) {
            int comparison = studentId.compareToIgnoreCase(current.student.getStudentId());
            if (comparison == 0) {
                return current.student;
            }
            if (comparison < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    // Delete student by ID
    public boolean delete(String studentId) {
        if (studentId == null || search(studentId) == null) {
            return false;
        }
        root = deleteRecursive(root, studentId);
        return true;
    }

    private TreeNode deleteRecursive(TreeNode current, String studentId) {
        if (current == null) {
            return null;
        }

        int comparison = studentId.compareToIgnoreCase(current.student.getStudentId());

        if (comparison < 0) {
            current.left = deleteRecursive(current.left, studentId);
        } else if (comparison > 0) {
            current.right = deleteRecursive(current.right, studentId);
        } else {
            // Node to delete found

            // Case 1: No children (leaf node)
            if (current.left == null && current.right == null) {
                return null;
            }

            // Case 2: One child
            if (current.left == null) {
                return current.right;
            } else if (current.right == null) {
                return current.left;
            }

            // Case 3: Two children -> find smallest in right subtree (in-order successor)
            TreeNode smallest = findMin(current.right);
            current.student = smallest.student;
            current.right = deleteRecursive(current.right, smallest.student.getStudentId());
        }

        return current;
    }

    private TreeNode findMin(TreeNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    // Get all students sorted in-order
    public List<Student> getInOrderList() {
        List<Student> list = new ArrayList<>();
        inOrderCollect(root, list);
        return list;
    }

    private void inOrderCollect(TreeNode node, List<Student> list) {
        if (node == null) return;
        inOrderCollect(node.left, list);
        list.add(node.student);
        inOrderCollect(node.right, list);
    }

    // Pre-order list
    public List<Student> getPreOrderList() {
        List<Student> list = new ArrayList<>();
        preOrderCollect(root, list);
        return list;
    }

    private void preOrderCollect(TreeNode node, List<Student> list) {
        if (node == null) return;
        list.add(node.student);
        preOrderCollect(node.left, list);
        preOrderCollect(node.right, list);
    }

    // Post-order list
    public List<Student> getPostOrderList() {
        List<Student> list = new ArrayList<>();
        postOrderCollect(root, list);
        return list;
    }

    private void postOrderCollect(TreeNode node, List<Student> list) {
        if (node == null) return;
        postOrderCollect(node.left, list);
        postOrderCollect(node.right, list);
        list.add(node.student);
    }

    // Display In-Order
    public void displayInOrder() {
        if (root == null) {
            System.out.println("BST is empty.");
            return;
        }
        System.out.println("\n===== Students in BST (In-Order) =====");
        inOrderPrint(root);
    }

    private void inOrderPrint(TreeNode node) {
        if (node == null) return;
        inOrderPrint(node.left);
        System.out.println(node.student);
        inOrderPrint(node.right);
    }

    // BST Height
    public int getHeight() {
        return calculateHeight(root);
    }

    private int calculateHeight(TreeNode node) {
        if (node == null) return 0;
        return 1 + Math.max(calculateHeight(node.left), calculateHeight(node.right));
    }

    // Total Node Count
    public int getNodeCount() {
        return countNodes(root);
    }

    private int countNodes(TreeNode node) {
        if (node == null) return 0;
        return 1 + countNodes(node.left) + countNodes(node.right);
    }

    // Clear BST
    public void clear() {
        root = null;
    }

    // JSON Tree structure for web rendering
    public String toTreeJson() {
        return buildNodeJson(root);
    }

    private String buildNodeJson(TreeNode node) {
        if (node == null) return "null";
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"marks\":%.1f,\"left\":%s,\"right\":%s}",
            escapeJson(node.student.getStudentId()),
            escapeJson(node.student.getName()),
            node.student.getMarks(),
            buildNodeJson(node.left),
            buildNodeJson(node.right)
        );
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
