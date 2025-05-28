package TaskManagement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MyTask {
    private int taskID;
    private String taskTitle;
    private String taskStatus;
    private LocalDate dueDate;
    private String taskPriority;
    private int workspaceID;

    private static final String DB_URL = "jdbc:sqlserver://10.244.202.169:1433;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "QuirxAdmin";
    private static final String DB_PASS = "admin";

    // Constructor
    public MyTask(int taskID, String taskTitle, String taskStatus, LocalDate dueDate, String taskPriority, int workspaceID) {
        this.taskID = taskID;
        this.taskTitle = taskTitle;
        this.taskStatus = taskStatus;
        this.dueDate = dueDate;
        this.taskPriority = taskPriority;
        this.workspaceID = workspaceID;
    }

    // Getters
    public int getTaskID() { return taskID; }
    public String getTaskTitle() { return taskTitle; }
    public String getTaskStatus() { return taskStatus; }
    public LocalDate getDueDate() { return dueDate; }
    public String getTaskPriority() { return taskPriority; }
    public int getWorkspaceID() { return workspaceID; }

    // Static method to fetch tasks for a user from DB
    public static List<MyTask> getTasksByUser(String userName) {
        List<MyTask> tasks = new ArrayList<>();
        String sql = "SELECT taskID, taskTitle, taskStatus, dueDate, taskPriority, workspaceID " +
                     "FROM TaskTable WHERE userOwner = ? ORDER BY dueDate ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int taskID = rs.getInt("taskID");
                String title = rs.getString("taskTitle");
                String status = rs.getString("taskStatus");
                LocalDate dueDate = rs.getDate("dueDate").toLocalDate();
                String priority = rs.getString("taskPriority");
                int workspaceID = rs.getInt("workspaceID");

                tasks.add(new MyTask(taskID, title, status, dueDate, priority, workspaceID));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // You can handle or rethrow exceptions as needed
        }

        return tasks;
    }
}
