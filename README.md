
## Project Information

- **Module**: CIT300 - Data Structures and Algorithms
- **Project Title**: CampusSystem (University Student Record and Campus Route Management System)
- **Coverage**: Linear Data Structures, Stacks, Queues, Binary Search Trees, Hashing, and Graphs.

---

## Team Responsibilities

| Student ID | GitHub Username | Full Name | Responsibility | Key Contributions |
|:---|:---|:---|:---|:---|
| **23DA2_0416** | `slakmalrudrigo` | **K.S. Lakmal Rudrigo** | Linked List & Student Records Management | Designed `Student` class, implemented `StudentLinkedList` CRUD operations, input validation, and student data model. |
| **23DA2_0100** | `thasrif06` | **M.I. Mohamed Thasrif** | Stack & Queue Operations | Implemented `ActionStack` with Undo history engine and `ServiceRequestQueue` FIFO ticket processing system. |
| **23DA2_0106** | `fazamaserofwolf17` | **M.H. Fathima Mafaza** | BST Tree & Hashing Search | Developed `StudentBST` (insert, delete with 0/1/2 children handling, traversals) and `StudentHashTable` (O(1) lookup & bucket chaining). |
| **23DA2_0310** | `Naufagit96` | **Naufa Begum** | Campus Graph & Navigation | Built `CampusGraph` with weighted connections, adjacency list representation, BFS, DFS, and Dijkstra's Shortest Path algorithm. |
| **All Members** | — | **All Team Members** | System Integration & Testing | Integration in `DataManager`, automated unit testing suite (`TestRunner`), CLI menu, and Web Visualizer Dashboard. |

---

## System Requirements & Implemented Data Structures

The system completely satisfies all functional requirements:

1. **Student Record Model (`Student.java`)**: Stores Student ID, Name, Programme, Marks, Letter Grade (A+-F), GPA (4.00 scale), and Student Email.
2. **Linked List (`StudentLinkedList.java`)**: Manages sequential student records, list displays, updates, and searches.
3. **Stack (`ActionStack.java`)**: LIFO (Last-In First-Out) stack for tracking recent actions (ADD, UPDATE, DELETE) with complete **Undo** capability.
4. **Queue (`ServiceRequestQueue.java`)**: FIFO (First-In First-Out) queue for handling student service tickets in order of arrival.
5. **Binary Search Tree (`StudentBST.java`)**: Organizes student records by Student ID. Supports search, In-Order, Pre-Order, Post-Order traversals, and full BST node deletion (leaf, 1-child, 2-children with in-order successor).
6. **Hash Table (`StudentHashTable.java`)**: $O(1)$ constant time lookup by Student ID using custom hash function and separate chaining linked lists for collision resolution.
7. **Graph & Campus Map (`CampusGraph.java`)**: Represents campus locations (vertices) and weighted roads/connections (edges) in meters using an **Adjacency List**.
8. **Graph Traversal & Pathfinding**: Supports **BFS (Breadth-First Search)**, **DFS (Depth-First Search)**, and **Dijkstra's Algorithm** for finding the exact shortest route between campus buildings.
9. **Console Menu Interface (`Main.java`)**: Features a clean 1-16 console menu matching specifications, plus an interactive Web Dashboard launcher (Option 16).
10. **Data Synchronization & Persistence (`DataManager.java`)**: Central source of truth that synchronizes all data structures concurrently.

---

## Console Menu Structure 

```text
=================== SYSTEM MENU ===================
1.  Add Student Record
2.  Update Student Record
3.  Delete Student Record
4.  Display All Records using Linked List
5.  Add Service Request to Queue
6.  Process Next Service Request
7.  Display Recent Actions using Stack (With Undo)
8.  Display Students using BST/AVL
9.  Search Student using Hashing
10. Add Campus Location
11. Remove Campus Location
12. Add Campus Connection/Road
13. Remove Campus Connection/Road
14. Display Campus Connections
15. Traverse Campus Locations using BFS or DFS (and Shortest Path)
16. Launch Interactive Web Visualizer Dashboard
17. Exit
===================================================
```

---

## How to Build and Run

### Prerequisites
- **JDK**: Java 8 or higher (Java 24 verified).
- No external libraries required (uses Java standard library).

### 1. Compile the Source Code
Navigate to the `CampusSystem` root directory and run:
```cmd
javac -d out src/universitysystem/*.java
```

### 2. Run Automated Unit Test Suite
```cmd
java -cp out universitysystem.TestRunner
```
*(Runs 49 automated unit tests verifying all data structures and algorithms)*

### 3. Launch Console Application
```cmd
java -cp out universitysystem.Main
```

### 4. Launch Interactive Web Dashboard (Extra)
Run the application and select **Option 16** from the menu, or open your web browser at:
`http://localhost:8080`
