/**
 * Package for task management functionalities, including adding, updating,
 * deleting, and retrieving tasks from a Microsoft SQL Server database.
 */
package TaskManagement;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Contains classes for managing tasks in a task management system.
 */
public class Crud_updated {

	/**
     * Represents a task with various properties such as title, status, due date,
     * priority, note, and assignment details.
     */
	public static class Task implements Comparable<Task> {
        private int taskID;
        private String taskTitle;
        private String taskStatus;
        private java.sql.Date dueDate;
        private String taskPriority;
        private String taskNote;
        private String assignedUserID;     
        private String assignedUserName;   

        /**
         * Constructs a task with all attributes.
         *
         * @param taskID           the task ID
         * @param taskTitle        the title of the task
         * @param taskStatus       the status of the task
         * @param dueDate          the due date of the task
         * @param taskPriority     the priority of the task
         * @param taskNote         any notes about the task
         * @param assignedUserID   the user ID to whom the task is assigned
         * @param assignedUserName the user name of the assigned user
         */
        public Task(int taskID, String taskTitle, String taskStatus,
                    java.sql.Date dueDate, String taskPriority,
                    String taskNote, String assignedUserID, String assignedUserName) {
            this.taskID = taskID;
            this.taskTitle = taskTitle;
            this.taskStatus = taskStatus;
            this.dueDate = dueDate;
            this.taskPriority = taskPriority;
            this.taskNote = taskNote;
            this.assignedUserID = assignedUserID;
            this.assignedUserName = assignedUserName;
        }

        /**
         * Constructs a task without specifying task ID and user name (for insertion).
         *
         * @param taskTitle      the title of the task
         * @param taskStatus     the status of the task
         * @param dueDate        the due date of the task
         * @param taskPriority   the priority of the task
         * @param taskNote       any notes about the task
         * @param assignedUserID the user ID to whom the task is assigned
         */
        public Task(String taskTitle, String taskStatus, java.sql.Date dueDate,
                    String taskPriority, String taskNote, String assignedUserID) {
            this.taskTitle = taskTitle;
            this.taskStatus = taskStatus;
            this.dueDate = dueDate;
            this.taskPriority = taskPriority;
            this.taskNote = taskNote;
            this.assignedUserID = assignedUserID;
        }

        public int getTaskID()               { return taskID; }
        public String getTaskTitle()         { return taskTitle; }
        public String getTaskStatus()        { return taskStatus; }
        public java.sql.Date getDueDate()    { return dueDate; }
        public String getTaskPriority()      { return taskPriority; }
        public String getTaskNote()          { return taskNote; }
        public String getAssignedUserID()    { return assignedUserID; } 
        public String getAssignedUserName()  { return assignedUserName; }

        public void setTaskTitle(String v)   { this.taskTitle = v; }
        public void setTaskStatus(String v)  { this.taskStatus = v; }
        public void setDueDate(java.sql.Date v){ this.dueDate = v; }
        public void setTaskPriority(String v){ this.taskPriority = v; }
        public void setTaskNote(String v)    { this.taskNote = v; }
        public void setAssignedUserID(String v)   { this.assignedUserID = v; } 
        public void setAssignedUserName(String v) { this.assignedUserName = v; } 

        // Getters and setters...

        /**
         * Compares tasks based on their priority level.
         *
         * @param o the task to compare to
         * @return an integer indicating priority comparison
         */
        @Override
        public int compareTo(Task o) {
            return priorityVal(this.taskPriority) - priorityVal(o.taskPriority);
        }
        /**
         * Converts priority string to numeric value for sorting.
         *
         * @param p the priority string
         * @return numeric representation of priority
         */
        private int priorityVal(String p) {
            return switch (p.toUpperCase()) {
                case "HIGH" -> 1;
                case "MEDIUM" -> 2;
                case "LOW" -> 3;
                default -> 4;
            };
        }
    }

	/**
     * Handles database operations for tasks such as adding, updating, deleting,
     * and retrieving task records.
     */
	public static class DatabaseManager {
        private static final String DB_URL =
            "jdbc:sqlserver://0.tcp.ap.ngrok.io:19058;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
        private static final String DB_USER = "QuirxAdmin";
        private static final String DB_PASS = "admin";

        /**
         * Establishes a connection to the SQL Server database.
         *
         * @return a {@link Connection} object
         * @throws SQLException if a database access error occurs
         */
        public static Connection connect() throws SQLException {
            try { Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); }
            catch (ClassNotFoundException e) { e.printStackTrace(); }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }

        /**
         * Adds a new task to the database.
         *
         * @param t the {@link Task} to add
         */
        public static void addTask(Task t) {
            String sql = """
                INSERT INTO TaskTable (taskTitle, taskStatus, dueDate,
                                       taskPriority, taskNote, assignedUserID)
                VALUES (?,?,?,?,?,?)
                """;
            try (Connection c = connect();
                 PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
            	// Validate and set parameters
                if (t.getTaskTitle() == null || t.getTaskTitle().isEmpty()) {
                    throw new IllegalArgumentException("Task title cannot be null or empty.");
                }
                st.setString(1, t.getTaskTitle());

                if (t.getTaskStatus() == null || !t.getTaskStatus().matches("DONE|IN PROGRESS|NOT STARTED")) {
                    throw new IllegalArgumentException("Invalid task status: " + t.getTaskStatus());
                }
                st.setString(2, t.getTaskStatus());

                if (t.getDueDate() == null) {
                    throw new IllegalArgumentException("Due date cannot be null.");
                }
                st.setDate(3, t.getDueDate());

                if (t.getTaskPriority() == null || !t.getTaskPriority().matches("HIGH|MEDIUM|LOW")) {
                    throw new IllegalArgumentException("Invalid task priority: " + t.getTaskPriority());
                }
                st.setString(4, t.getTaskPriority());

                st.setString(5, t.getTaskNote() != null ? t.getTaskNote() : ""); // Default to empty string if null

                if (t.getAssignedUserID() == null || t.getAssignedUserID().isEmpty()) {
                    throw new IllegalArgumentException("Assigned User ID cannot be null or empty.");
                }
                st.setString(6, t.getAssignedUserID());

                // Execute the query
                st.executeUpdate();
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("✅ Task added with ID: " + rs.getInt(1));
                } else {
                    System.out.println("⚠ Task added, but no ID returned.");
                }
            } catch (SQLException e) {
                System.err.println("❌ SQL Error: " + e.getMessage());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Validation Error: " + e.getMessage());
            }
        }


        /**
         * Retrieves all tasks from the database.
         *
         * @return a {@link List} of {@link Task} objects
         */
        public static List<Task> getAllTasks() {
            List<Task> list = new ArrayList<>();
            String sql = """
                SELECT  t.taskID, t.taskTitle, t.taskStatus, t.dueDate,
                        t.taskPriority, t.taskNote,
                        t.assignedUserID,
                        u.userName AS assignedUserName
                FROM    TaskTable t
                LEFT JOIN UserTable u ON t.assignedUserID = u.userID
                """;
            try (Connection c = connect();
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Task(
                        rs.getInt("taskID"),
                        rs.getString("taskTitle"),
                        rs.getString("taskStatus"),
                        rs.getDate("dueDate"),
                        rs.getString("taskPriority"),
                        rs.getString("taskNote"),
                        rs.getString("assignedUserID"),   
                        rs.getString("assignedUserName")  
                    ));
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return list;
        }

        /**
         * Deletes a task from the database.
         *
         * @param id the task ID to delete
         */
        public static void deleteTask(int id) {
            try (Connection c = connect();
                 PreparedStatement st = c.prepareStatement("DELETE FROM TaskTable WHERE taskID=?")) {
                st.setInt(1, id);
                st.executeUpdate();
                System.out.println("✅ Task deleted!");
            } catch (SQLException e) { e.printStackTrace(); }
        }

        /**
         * Updates an existing task in the database.
         *
         * @param t the {@link Task} containing updated data
         */
        public static void updateTask(Task t) {
            String sql = """
                UPDATE TaskTable
                SET taskTitle=?, taskStatus=?, dueDate=?, taskPriority=?,
                    taskNote=?, assignedUserID=?
                WHERE taskID=?
                """;
            try (Connection c = connect();
                 PreparedStatement st = c.prepareStatement(sql)) {
                st.setString(1, t.getTaskTitle());
                st.setString(2, t.getTaskStatus());
                st.setDate  (3, t.getDueDate());
                st.setString(4, t.getTaskPriority());
                st.setString(5, t.getTaskNote());
                st.setString(6, t.getAssignedUserID());
                st.setInt   (7, t.getTaskID());
                st.executeUpdate();
                System.out.println("✅ Task updated!");
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

	/**
     * Main method providing a console-based interface for task management.
     *
     * @param args command-line arguments
     */
	public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("""
                --- Task Management System ---
                1. Add Task
                2. View All Tasks (sorted by priority)
                3. Update Task
                4. Delete Task
                5. Exit
                """);

            System.out.print("Choose option: ");
            while (!sc.hasNextInt()) {
                System.out.println("❌ Invalid input! Please enter a number between 1 and 5.");
                sc.next(); // Clear invalid input
                System.out.print("Choose option: ");
            }
            int ch = sc.nextInt();
            sc.nextLine(); // Consume newline

            switch (ch) {
                case 1 -> {
                    System.out.print("Title: ");
                    String title = sc.nextLine().trim();
                    while (title.isEmpty()) {
                        System.out.println("❌ Title cannot be empty. Please enter a valid title.");
                        System.out.print("Title: ");
                        title = sc.nextLine().trim();
                    }

                    String status;
                    do {
                        System.out.print("Status (DONE / IN PROGRESS / NOT STARTED): ");
                        status = sc.nextLine().toUpperCase().trim();
                        if (!status.matches("DONE|IN PROGRESS|NOT STARTED")) {
                            System.out.println("❌ Invalid status! Please enter DONE, IN PROGRESS, or NOT STARTED.");
                        }
                    } while (!status.matches("DONE|IN PROGRESS|NOT STARTED"));

                    System.out.print("Due Date (yyyy-mm-dd): ");
                    java.sql.Date due = null;
                    while (due == null) {
                        try {
                            due = java.sql.Date.valueOf(LocalDate.parse(sc.nextLine().trim()));
                        } catch (Exception e) {
                            System.out.println("❌ Invalid date format! Please enter a valid date in yyyy-mm-dd format.");
                            System.out.print("Due Date (yyyy-mm-dd): ");
                        }
                    }

                    String priority;
                    do {
                        System.out.print("Priority (HIGH / MEDIUM / LOW): ");
                        priority = sc.nextLine().toUpperCase().trim();
                        if (!priority.matches("HIGH|MEDIUM|LOW")) {
                            System.out.println("❌ Invalid priority! Please enter HIGH, MEDIUM, or LOW.");
                        }
                    } while (!priority.matches("HIGH|MEDIUM|LOW"));

                    System.out.print("Note: ");
                    String note = sc.nextLine().trim();

                    System.out.print("Assigned User ID: ");
                    String uid = sc.nextLine().trim();
                    while (uid.isEmpty()) {
                        System.out.println("❌ User ID cannot be empty. Please enter a valid User ID.");
                        System.out.print("Assigned User ID: ");
                        uid = sc.nextLine().trim();
                    }

                    DatabaseManager.addTask(new Task(title, status, due, priority, note, uid));
                }
                case 2 -> {
                    List<Task> tasks = DatabaseManager.getAllTasks();
                    if (tasks.isEmpty()) {
                        System.out.println("📋 No tasks found.");
                    } else {
                        PriorityQueue<Task> pq = new PriorityQueue<>(tasks);
                        System.out.println("\n📋 Tasks (by priority):");
                        while (!pq.isEmpty()) {
                            Task t = pq.poll();
                            System.out.println(t.getTaskID() + ": " + t.getTaskTitle()
                                + " | Status: " + t.getTaskStatus()
                                + " | Priority: " + t.getTaskPriority()
                                + " | Due: " + t.getDueDate()
                                + " | Assigned to: " + t.getAssignedUserID()
                                + " (" + t.getAssignedUserName() + ")");
                        }
                    }
                }
                case 3 -> {
                    System.out.print("Task ID to update: ");
                    int id = -1;
                    while (id < 0) {
                        if (sc.hasNextInt()) {
                            id = sc.nextInt();
                            sc.nextLine(); // Consume newline
                        } else {
                            System.out.println("❌ Invalid input! Please enter a valid Task ID.");
                            sc.next(); // Clear invalid input
                            System.out.print("Task ID to update: ");
                        }
                    }

                    System.out.print("New Title: ");
                    String title = sc.nextLine().trim();
                    while (title.isEmpty()) {
                        System.out.println("❌ Title cannot be empty. Please enter a valid title.");
                        System.out.print("New Title: ");
                        title = sc.nextLine().trim();
                    }

                    String status;
                    do {
                        System.out.print("New Status (DONE / IN PROGRESS / NOT STARTED): ");
                        status = sc.nextLine().toUpperCase().trim();
                        if (!status.matches("DONE|IN PROGRESS|NOT STARTED")) {
                            System.out.println("❌ Invalid status! Please enter DONE, IN PROGRESS, or NOT STARTED.");
                        }
                    } while (!status.matches("DONE|IN PROGRESS|NOT STARTED"));

                    System.out.print("New Due Date (yyyy-mm-dd): ");
                    java.sql.Date due = null;
                    while (due == null) {
                        try {
                            due = java.sql.Date.valueOf(LocalDate.parse(sc.nextLine().trim()));
                        } catch (Exception e) {
                            System.out.println("❌ Invalid date format! Please enter a valid date in yyyy-mm-dd format.");
                            System.out.print("New Due Date (yyyy-mm-dd): ");
                        }
                    }

                    String priority;
                    do {
                        System.out.print("New Priority (HIGH / MEDIUM / LOW): ");
                        priority = sc.nextLine().toUpperCase().trim();
                        if (!priority.matches("HIGH|MEDIUM|LOW")) {
                            System.out.println("❌ Invalid priority! Please enter HIGH, MEDIUM, or LOW.");
                        }
                    } while (!priority.matches("HIGH|MEDIUM|LOW"));

                    System.out.print("New Note: ");
                    String note = sc.nextLine().trim();

                    System.out.print("New Assigned User ID: ");
                    String uid = sc.nextLine().trim();
                    while (uid.isEmpty()) {
                        System.out.println("❌ User ID cannot be empty. Please enter a valid User ID.");
                        System.out.print("New Assigned User ID: ");
                        uid = sc.nextLine().trim();
                    }

                    DatabaseManager.updateTask(new Task(id, title, status, due, priority, note, uid, null));
                }
                case 4 -> {
                    System.out.print("Task ID to delete: ");
                    int id = -1;
                    while (id < 0) {
                        if (sc.hasNextInt()) {
                            id = sc.nextInt();
                            sc.nextLine(); // Consume newline
                        } else {
                            System.out.println("❌ Invalid input! Please enter a valid Task ID.");
                            sc.next(); // Clear invalid input
                            System.out.print("Task ID to delete: ");
                        }
                    }
                    DatabaseManager.deleteTask(id);
                }
                case 5 -> run = false;
                default -> System.out.println("❌ Invalid choice! Please enter a number between 1 and 5.");
            }
        }
        sc.close();
        System.out.println("👋 Bye! Program exited.");
    }
}