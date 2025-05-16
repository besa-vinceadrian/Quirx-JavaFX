package TaskManagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaskManagementSystem {
    public static class Task {
        private int taskID;
        private String taskTitle;
        private String taskStatus;
        private Date dueDate;
        private String taskPriority;
        private String taskNote;
        private String assignedUser; 

        public Task(int taskID, String taskTitle, String taskStatus, Date dueDate, String taskPriority, String taskNote, String assignedUser) {
            this.taskID = taskID;
            this.taskTitle = taskTitle;
            this.taskStatus = taskStatus;
            this.dueDate = dueDate;
            this.taskPriority = taskPriority;
            this.taskNote = taskNote;
            this.assignedUser = assignedUser; 
        }

        // Getters
        public int getTaskID() { return taskID; }
        public String getTaskTitle() { return taskTitle; }
        public String getTaskStatus() { return taskStatus; }
        public Date getDueDate() { return dueDate; }
        public String getTaskPriority() { return taskPriority; }
        public String getTaskNote() { return taskNote; }
        public String getAssignedUser() { return assignedUser; }

        // Setters
        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
        public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
        public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
        public void setTaskPriority(String taskPriority) { this.taskPriority = taskPriority; }
        public void setTaskNote(String taskNote) { this.taskNote = taskNote; }
        public void setAssignedUser(String assignedUser) { this.assignedUser = assignedUser; } 
    }

    // Database manager
    public static class DatabaseManager {
        private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Quirx_TMS;encrypt=true;trustServerCertificate=true";
        private static final String DB_USER = "LMS_Admin";                            
        private static final String DB_PASS = "benchbrian";                            

        public static Connection connect() throws SQLException {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }

        public static void addTask(Task task) {
            String sql = "INSERT INTO TaskTable (taskTitle, taskStatus, dueDate, taskPriority, taskNote, assignedUser) VALUES (?, ?, ?, ?, ?, ?)"; // <-- Updated

            try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, task.getTaskTitle());
                stmt.setString(2, task.getTaskStatus());
                stmt.setDate(3, task.getDueDate());
                stmt.setString(4, task.getTaskPriority());
                stmt.setString(5, task.getTaskNote());
                stmt.setString(6, task.getAssignedUser()); 
                stmt.executeUpdate();
                System.out.println("✅ Task added!");
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
                        rs.getString("assignedUser") 
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
            String sql = "UPDATE TaskTable SET taskTitle=?, taskStatus=?, dueDate=?, taskPriority=?, taskNote=?, assignedUser=? WHERE taskID=?"; // <-- Updated
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

    // Main Method
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- Task Management System ---");
            System.out.println("1. Add Task");
            System.out.println("2. View All Tasks");
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
                    System.out.print("Status: ");
                    String status = scanner.nextLine();
                    System.out.print("Due Date (yyyy-mm-dd): ");
                    String date = scanner.nextLine();
                    System.out.print("Priority: ");
                    String priority = scanner.nextLine();
                    System.out.print("Note: ");
                    String note = scanner.nextLine();
                    System.out.print("Assigned User: ");
                    String assignedUser = scanner.nextLine();

                    Task newTask = new Task(0, title, status, Date.valueOf(date), priority, note, assignedUser);
                    DatabaseManager.addTask(newTask);
                }
                case 2 -> {
                    List<Task> tasks = DatabaseManager.getAllTasks();
                    for (Task task : tasks) {
                        System.out.println(task.getTaskID() + ": " + task.getTaskTitle() + " | " + task.getTaskStatus() + " | " + task.getDueDate() + " | Assigned to: " + task.getAssignedUser());
                    }
                }
                case 3 -> {
                    System.out.print("Enter Task ID to update: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("New Title: ");
                    String title = scanner.nextLine();
                    System.out.print("New Status: ");
                    String status = scanner.nextLine();
                    System.out.print("New Due Date (yyyy-mm-dd): ");
                    String date = scanner.nextLine();
                    System.out.print("New Priority: ");
                    String priority = scanner.nextLine();
                    System.out.print("New Note: ");
                    String note = scanner.nextLine();
                    System.out.print("New Assigned User: ");
                    String assignedUser = scanner.nextLine(); 

                    Task updatedTask = new Task(id, title, status, Date.valueOf(date), priority, note, assignedUser);
                    DatabaseManager.updateTask(updatedTask);
                }
                case 4 -> {
                    System.out.print("Enter Task ID to delete: ");
                    int id = scanner.nextInt();
                    DatabaseManager.deleteTask(id);
                }
                case 5 -> running = false;
                default -> System.out.println("Invalid choice! Try again.");
            }
        }

        scanner.close();
        System.out.println("👋 Bye! Program exited.");
    }
}
