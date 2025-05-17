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
        private String assignedUserID;     
        private String assignedUserName;   

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

        @Override
        public int compareTo(Task o) {
            return priorityVal(this.taskPriority) - priorityVal(o.taskPriority);
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
        private static final String DB_URL =
            "jdbc:sqlserver://0.tcp.ap.ngrok.io:14438;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
        private static final String DB_USER = "QuirxAdmin";
        private static final String DB_PASS = "admin";

        public static Connection connect() throws SQLException {
            try { Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); }
            catch (ClassNotFoundException e) { e.printStackTrace(); }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }

        public static void addTask(Task t) {
            String sql = """
                INSERT INTO TaskTable (taskTitle, taskStatus, dueDate,
                                       taskPriority, taskNote, assignedUserID)
                VALUES (?,?,?,?,?,?)
                """;
            try (Connection c = connect();
                 PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, t.getTaskTitle());
                st.setString(2, t.getTaskStatus());
                st.setDate  (3, t.getDueDate());
                st.setString(4, t.getTaskPriority());
                st.setString(5, t.getTaskNote());
                st.setString(6, t.getAssignedUserID());
                st.executeUpdate();
                ResultSet rs = st.getGeneratedKeys();
                System.out.println(rs.next()
                    ? "✅ Task added with ID: "+ rs.getInt(1)
                    : "⚠ Task added, but no ID returned.");
            } catch (SQLException e) { e.printStackTrace(); }
        }

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

        public static void deleteTask(int id) {
            try (Connection c = connect();
                 PreparedStatement st = c.prepareStatement("DELETE FROM TaskTable WHERE taskID=?")) {
                st.setInt(1, id);
                st.executeUpdate();
                System.out.println("✅ Task deleted!");
            } catch (SQLException e) { e.printStackTrace(); }
        }

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
            int ch = sc.nextInt();
            sc.nextLine();         

            switch (ch) {
                case 1 -> { 
                    System.out.print("Title: "); String title = sc.nextLine();
                    String status;
                    do {
                        System.out.print("Status (DONE / IN PROGRESS / NOT STARTED): ");
                        status = sc.nextLine().toUpperCase();
                    } while (!status.matches("DONE|IN PROGRESS|NOT STARTED"));

                    System.out.print("Due Date (yyyy-mm-dd): ");
                    java.sql.Date due = java.sql.Date.valueOf(LocalDate.parse(sc.nextLine()));

                    String priority;
                    do {
                        System.out.print("Priority (HIGH / MEDIUM / LOW): ");
                        priority = sc.nextLine().toUpperCase();
                    } while (!priority.matches("HIGH|MEDIUM|LOW"));

                    System.out.print("Note: ");   String note = sc.nextLine();
                    System.out.print("Assigned User ID: "); String uid = sc.nextLine();

                    DatabaseManager.addTask(
                        new Task(title, status, due, priority, note, uid));
                }
                case 2 -> { 
                    List<Task> tasks = DatabaseManager.getAllTasks();
                    PriorityQueue<Task> pq = new PriorityQueue<>(tasks);
                    System.out.println("\n📋 Tasks (by priority):");
                    while (!pq.isEmpty()) {
                        Task t = pq.poll();
                        System.out.println(t.getTaskID() + ": " + t.getTaskTitle()
                            + " | Status: "   + t.getTaskStatus()
                            + " | Priority: " + t.getTaskPriority()
                            + " | Due: "      + t.getDueDate()
                            + " | Assigned to: " + t.getAssignedUserID()
                            + " (" + t.getAssignedUserName() + ")");
                    }
                }
                case 3 -> { 
                    System.out.print("Task ID to update: "); int id = sc.nextInt(); sc.nextLine();
                    System.out.print("New Title: "); String title = sc.nextLine();

                    String status;
                    do {
                        System.out.print("New Status (DONE / IN PROGRESS / NOT STARTED): ");
                        status = sc.nextLine().toUpperCase();
                    } while (!status.matches("DONE|IN PROGRESS|NOT STARTED"));

                    System.out.print("New Due Date (yyyy-mm-dd): ");
                    java.sql.Date due = java.sql.Date.valueOf(LocalDate.parse(sc.nextLine()));

                    String priority;
                    do {
                        System.out.print("New Priority (HIGH / MEDIUM / LOW): ");
                        priority = sc.nextLine().toUpperCase();
                    } while (!priority.matches("HIGH|MEDIUM|LOW"));

                    System.out.print("New Note: "); String note = sc.nextLine();
                    System.out.print("New Assigned User ID: "); String uid = sc.nextLine();

                    DatabaseManager.updateTask(
                        new Task(id, title, status, due, priority, note, uid, null)); 
                }
                case 4 -> { 
                    System.out.print("Task ID to delete: "); int id = sc.nextInt();
                    DatabaseManager.deleteTask(id);
                }
                case 5 -> run = false;
                default -> System.out.println("❌ Invalid choice! Try again.");
            }
        }
        sc.close();
        System.out.println("👋 Bye! Program exited.");
    }
}
