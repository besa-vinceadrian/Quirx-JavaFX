package TaskManagement;

import java.sql.*;
import java.util.*;

public class Crud_updated {

    public static class Task implements Comparable<Task> {
        private int taskID;
        private String taskTitle;
        private String taskStatus;
        private java.sql.Date dueDate;
        private String taskPriority;
        private String userOwner;

        public Task(int taskID, String taskTitle, String taskStatus,
                    java.sql.Date dueDate, String taskPriority, String userOwner) {
            this.taskID = taskID;
            this.taskTitle = taskTitle;
            this.taskStatus = taskStatus;
            this.dueDate = dueDate;
            this.taskPriority = taskPriority;
            this.userOwner = userOwner;
        }

        public Task(String taskTitle, String taskStatus, java.sql.Date dueDate,
                    String taskPriority, String userOwner) {
            this.taskTitle = taskTitle;
            this.taskStatus = taskStatus;
            this.dueDate = dueDate;
            this.taskPriority = taskPriority;
            this.userOwner = userOwner;
        }

        public int getTaskID() { return taskID; }
        public String getTaskTitle() { return taskTitle; }
        public String getTaskStatus() { return taskStatus; }
        public java.sql.Date getDueDate() { return dueDate; }
        public String getTaskPriority() { return taskPriority; }
        public String getUserOwner() { return userOwner; }

        public void setTaskTitle(String v) { this.taskTitle = v; }
        public void setTaskStatus(String v) { this.taskStatus = v; }
        public void setDueDate(java.sql.Date v) { this.dueDate = v; }
        public void setTaskPriority(String v) { this.taskPriority = v; }
        public void setUserOwner(String v) { this.userOwner = v; }

        @Override
        public int compareTo(Task o) {
            return Integer.compare(priorityVal(this.taskPriority), priorityVal(o.taskPriority));
        }

        private int priorityVal(String p) {
            return switch (p.toUpperCase()) {
                case "HIGH" -> 1;
                case "MEDIUM" -> 2;
                case "LOW" -> 3;
                default -> 4;
            };
        }
    }

    public static class DatabaseManager {
        private static final String DB_URL = "jdbc:sqlserver://10.244.202.169:1433;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
        private static final String DB_USER = "QuirxAdmin";
        private static final String DB_PASS = "admin";

        public static Connection connect() throws SQLException {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQL Server Driver not found", e);
            }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }

        public static boolean addTask(Task t) {
            String sql = """
                INSERT INTO TaskTable (taskTitle, taskStatus, dueDate, taskPriority, userOwner)
                VALUES (?,?,?,?,?)
            """;
            try (Connection c = connect();
                 PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                st.setString(1, t.getTaskTitle());
                st.setString(2, t.getTaskStatus());
                st.setDate(3, t.getDueDate());
                st.setString(4, t.getTaskPriority());
                st.setString(5, t.getUserOwner());
                
                int rowsAffected = st.executeUpdate();
                if (rowsAffected > 0) {
                    ResultSet rs = st.getGeneratedKeys();
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        System.out.println("✅ Task added with ID: " + generatedId);
                        // If needed: t.setTaskID(generatedId);
                    }
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("❌ Error adding task: " + e.getMessage());
            }
            return false;
        }

        public static List<Task> getAllTasks(String userOwner) {
            List<Task> list = new ArrayList<>();
            String sql = """
                SELECT taskID, taskTitle, taskStatus, dueDate, taskPriority, userOwner
                FROM TaskTable
                WHERE userOwner = ?
            """;
            try (Connection c = connect(); PreparedStatement st = c.prepareStatement(sql)) {
                st.setString(1, userOwner);
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    list.add(new Task(
                        rs.getInt("taskID"),
                        rs.getString("taskTitle"),
                        rs.getString("taskStatus"),
                        rs.getDate("dueDate"),
                        rs.getString("taskPriority"),
                        rs.getString("userOwner")
                    ));
                }
            } catch (SQLException e) {
                System.err.println("❌ Error retrieving tasks: " + e.getMessage());
            }
            return list;
        }

        public static void updateTask(Task t) {
            String sql = """
                UPDATE TaskTable
                SET taskTitle=?, taskStatus=?, dueDate=?, taskPriority=?
                WHERE taskID=? AND userOwner=?
            """;
            try (Connection c = connect(); PreparedStatement st = c.prepareStatement(sql)) {
                st.setString(1, t.getTaskTitle());
                st.setString(2, t.getTaskStatus());
                st.setDate(3, t.getDueDate());
                st.setString(4, t.getTaskPriority());
                st.setInt(5, t.getTaskID());
                st.setString(6, t.getUserOwner());
                int updated = st.executeUpdate();
                if (updated > 0) System.out.println("✅ Task updated!");
                else System.out.println("⚠️ Task not found or unauthorized.");
            } catch (SQLException e) {
                System.err.println("❌ Error updating task: " + e.getMessage());
            }
        }

        public static void deleteTask(int id, String userOwner) {
            try (Connection c = connect();
                 PreparedStatement st = c.prepareStatement("DELETE FROM TaskTable WHERE taskID=? AND userOwner=?")) {
                st.setInt(1, id);
                st.setString(2, userOwner);
                int deleted = st.executeUpdate();
                if (deleted > 0) System.out.println("✅ Task deleted!");
                else System.out.println("⚠️ Task not found or unauthorized.");
            } catch (SQLException e) {
                System.err.println("❌ Error deleting task: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your email to log in: ");
        String userEmail = sc.nextLine().trim();

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
            int ch = sc.hasNextInt() ? sc.nextInt() : -1;
            sc.nextLine(); // consume newline

            switch (ch) {
                case 1 -> {
                    System.out.print("Title: ");
                    String title = sc.nextLine().trim();

                    System.out.print("Status (DONE / IN PROGRESS / NOT STARTED): ");
                    String status = sc.nextLine().trim().toUpperCase();

                    System.out.print("Due Date (yyyy-mm-dd): ");
                    java.sql.Date due = parseDate(sc.nextLine().trim());

                    System.out.print("Priority (HIGH / MEDIUM / LOW): ");
                    String priority = sc.nextLine().trim().toUpperCase();

                    if (due != null) {
                        DatabaseManager.addTask(new Task(title, status, due, priority, userEmail));
                    } else {
                        System.out.println("❌ Invalid date format!");
                    }
                }
                case 2 -> {
                    List<Task> tasks = DatabaseManager.getAllTasks(userEmail);
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
                                + " | Due: " + t.getDueDate());
                        }
                    }
                }
                case 3 -> {
                    System.out.print("Task ID to update: ");
                    int id = sc.nextInt(); sc.nextLine();

                    System.out.print("New Title: ");
                    String title = sc.nextLine().trim();

                    System.out.print("New Status (DONE / IN PROGRESS / NOT STARTED): ");
                    String status = sc.nextLine().trim().toUpperCase();

                    System.out.print("New Due Date (yyyy-mm-dd): ");
                    java.sql.Date due = parseDate(sc.nextLine().trim());

                    System.out.print("New Priority (HIGH / MEDIUM / LOW): ");
                    String priority = sc.nextLine().trim().toUpperCase();

                    if (due != null) {
                        DatabaseManager.updateTask(new Task(id, title, status, due, priority, userEmail));
                    } else {
                        System.out.println("❌ Invalid date format!");
                    }
                }
                case 4 -> {
                    System.out.print("Task ID to delete: ");
                    int id = sc.nextInt(); sc.nextLine();
                    DatabaseManager.deleteTask(id, userEmail);
                }
                case 5 -> run = false;
                default -> System.out.println("❌ Invalid choice!");
            }
        }
        sc.close();
        System.out.println("👋 Bye! Program exited.");
    }

    private static java.sql.Date parseDate(String input) {
        try {
            return java.sql.Date.valueOf(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}