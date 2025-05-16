package TaskManagement;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class Crud_updated {
    public static class Task implements Comparable<Task> {
        private int taskID;
        private String taskTitle;
        private String taskStatus;
        private java.sql.Date dueDate;
        private String taskPriority;
        private String taskNote;
        private String assignedUser;

        // Constructor WITH taskID (for reading from DB)
        public Task(int taskID, String taskTitle, String taskStatus, java.sql.Date dueDate, String taskPriority, String taskNote, String assignedUser) {
            this.taskID = taskID;
            this.taskTitle = taskTitle;
            this.taskStatus = taskStatus;
            this.dueDate = dueDate;
            this.taskPriority = taskPriority;
            this.taskNote = taskNote;
            this.assignedUser = assignedUser;
        }

        // Constructor WITHOUT taskID (for inserting into DB)
        public Task(String taskTitle, String taskStatus, java.sql.Date dueDate, String taskPriority, String taskNote, String assignedUser) {
            this.taskTitle = taskTitle;
            this.taskStatus = taskStatus;
            this.dueDate = dueDate;
            this.taskPriority = taskPriority;
            this.taskNote = taskNote;
            this.assignedUser = assignedUser;
        }

        public int getTaskID() { return taskID; }
        public String getTaskTitle() { return taskTitle; }
        public String getTaskStatus() { return taskStatus; }
        public java.sql.Date getDueDate() { return dueDate; }
        public String getTaskPriority() { return taskPriority; }
        public String getTaskNote() { return taskNote; }
        public String getAssignedUser() { return assignedUser; }

        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
        public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
        public void setDueDate(java.sql.Date dueDate) { this.dueDate = dueDate; }
        public void setTaskPriority(String taskPriority) { this.taskPriority = taskPriority; }
        public void setTaskNote(String taskNote) { this.taskNote = taskNote; }
        public void setAssignedUser(String assignedUser) { this.assignedUser = assignedUser; }

        @Override
        public int compareTo(Task other) {
            return getPriorityValue(this.taskPriority) - getPriorityValue(other.taskPriority);
        }

        private int getPriorityValue(String priority) {
            return switch (priority.toUpperCase()) {
                case "HIGH" -> 1;
                case "MEDIUM" -> 2;
                case "LOW" -> 3;
                default -> 4;
            };
        }
    }

    public static class DatabaseManager {
        private static final String DB_URL = "jdbc:sqlserver://0.tcp.ap.ngrok.io:18980;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
        private static final String DB_USER = "QuirxAdmin";
        private static final String DB_PASS = "admin";

        public static Connection connect() throws SQLException {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }

        public static void addTask(Task task) {
            String sql = "INSERT INTO TaskTable (taskTitle, taskStatus, dueDate, taskPriority, taskNote, assignedUserID) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, task.getTaskTitle());
                stmt.setString(2, task.getTaskStatus());
                stmt.setDate(3, task.getDueDate());
                stmt.setString(4, task.getTaskPriority());
                stmt.setString(5, task.getTaskNote());
                stmt.setString(6, task.getAssignedUser());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedID = rs.getInt(1);
                    System.out.println("✅ Task added with ID: " + generatedID);
                } else {
                    System.out.println("⚠ Task added, but no ID returned.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static List<Task> getAllTasks() {
            List<Task> tasks = new ArrayList<>();
            String sql = "SELECT * FROM TaskTable";

            try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Task task = new Task(
                        rs.getInt("taskID"),
                        rs.getString("taskTitle"),
                        rs.getString("taskStatus"),
                        rs.getDate("dueDate"),
                        rs.getString("taskPriority"),
                        rs.getString("taskNote"),
                        rs.getString("assignedUserID")
                    );
                    tasks.add(task);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return tasks;
        }

        public static void deleteTask(int taskID) {
            String sql = "DELETE FROM TaskTable WHERE taskID = ?";
            try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, taskID);
                stmt.executeUpdate();
                System.out.println("✅ Task deleted!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static void updateTask(Task task) {
            String sql = "UPDATE TaskTable SET taskTitle=?, taskStatus=?, dueDate=?, taskPriority=?, taskNote=?, assignedUserID=? WHERE taskID=?";
            try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, task.getTaskTitle());
                stmt.setString(2, task.getTaskStatus());
                stmt.setDate(3, task.getDueDate());
                stmt.setString(4, task.getTaskPriority());
                stmt.setString(5, task.getTaskNote());
                stmt.setString(6, task.getAssignedUser());
                stmt.setInt(7, task.getTaskID());
                stmt.executeUpdate();
                System.out.println("✅ Task updated!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- Task Management System ---");
            System.out.println("1. Add Task");
            System.out.println("2. View All Tasks (Sorted by Priority)");
            System.out.println("3. Update Task");
            System.out.println("4. Delete Task");
            System.out.println("5. Exit");
            System.out.print("Choose option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Title: ");
                    String title = scanner.nextLine();

                    String status;
                    do {
                        System.out.print("Status (DONE / IN PROGRESS / NOT STARTED): ");
                        status = scanner.nextLine().toUpperCase();
                    } while (!status.equals("DONE") && !status.equals("IN PROGRESS") && !status.equals("NOT STARTED"));

                    System.out.print("Due Date (yyyy-mm-dd): ");
                    String date = scanner.nextLine();

                    String priority;
                    do {
                        System.out.print("Priority (HIGH / MEDIUM / LOW): ");
                        priority = scanner.nextLine().toUpperCase();
                    } while (!priority.equals("HIGH") && !priority.equals("MEDIUM") && !priority.equals("LOW"));

                    System.out.print("Note: ");
                    String note = scanner.nextLine();
                    System.out.print("Assigned User: ");
                    String assignedUser = scanner.nextLine();

                    java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.parse(date));

                    Task newTask = new Task(title, status, sqlDate, priority, note, assignedUser); // no taskID
                    DatabaseManager.addTask(newTask);
                }
                case 2 -> {
                    List<Task> tasks = DatabaseManager.getAllTasks();
                    PriorityQueue<Task> priorityQueue = new PriorityQueue<>(tasks);
                    System.out.println("\n📋 Tasks (sorted by priority):");
                    while (!priorityQueue.isEmpty()) {
                        Task task = priorityQueue.poll();
                        System.out.println(task.getTaskID() + ": " + task.getTaskTitle()
                                + " | Status: " + task.getTaskStatus()
                                + " | Priority: " + task.getTaskPriority()
                                + " | Due: " + task.getDueDate()
                                + " | Assigned to: " + task.getAssignedUser());
                    }
                }
                case 3 -> {
                    System.out.print("Enter Task ID to update: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("New Title: ");
                    String title = scanner.nextLine();

                    String status;
                    do {
                        System.out.print("New Status (DONE / IN PROGRESS / NOT STARTED): ");
                        status = scanner.nextLine().toUpperCase();
                    } while (!status.equals("DONE") && !status.equals("IN PROGRESS") && !status.equals("NOT STARTED"));

                    System.out.print("New Due Date (yyyy-mm-dd): ");
                    String date = scanner.nextLine();

                    String priority;
                    do {
                        System.out.print("New Priority (HIGH / MEDIUM / LOW): ");
                        priority = scanner.nextLine().toUpperCase();
                    } while (!priority.equals("HIGH") && !priority.equals("MEDIUM") && !priority.equals("LOW"));

                    System.out.print("New Note: ");
                    String note = scanner.nextLine();
                    System.out.print("New Assigned User: ");
                    String assignedUser = scanner.nextLine();

                    java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.parse(date));

                    Task updatedTask = new Task(id, title, status, sqlDate, priority, note, assignedUser);
                    DatabaseManager.updateTask(updatedTask);
                }
                case 4 -> {
                    System.out.print("Enter Task ID to delete: ");
                    int id = scanner.nextInt();
                    DatabaseManager.deleteTask(id);
                }
                case 5 -> running = false;
                default -> System.out.println("❌ Invalid choice! Try again.");
            }
        }

        scanner.close();
        System.out.println("👋 Bye! Program exited.");
    }
}
