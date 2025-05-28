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
    private String workspaceName;  // New field

    private static final String DB_URL = "jdbc:sqlserver://10.244.202.169:1433;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "QuirxAdmin";
    private static final String DB_PASS = "admin";

    // Constructor updated to include workspaceName
    public MyTask(int taskID, String taskTitle, String taskStatus, LocalDate dueDate,
                  String taskPriority, int workspaceID, String workspaceName) {
        this.taskID = taskID;
        this.taskTitle = taskTitle;
        this.taskStatus = taskStatus;
        this.dueDate = dueDate;
        this.taskPriority = taskPriority;
        this.workspaceID = workspaceID;
        this.workspaceName = workspaceName;
    }

    // Getters
    public int getTaskID() { return taskID; }
    public String getTaskTitle() { return taskTitle; }
    public String getTaskStatus() { return taskStatus; }
    public LocalDate getDueDate() { return dueDate; }
    public String getTaskPriority() { return taskPriority; }
    public int getWorkspaceID() { return workspaceID; }
    public String getWorkspaceName() { return workspaceName; }

    // Static method to fetch tasks with workspace name
    public static List<MyTask> getTasksByUser(String userName) {
        List<MyTask> tasks = new ArrayList<>();
        String sql = "SELECT t.taskID, t.taskTitle, t.taskStatus, t.dueDate, t.taskPriority, t.workspaceID, w.workspaceName " +
                     "FROM TaskTable t " +
                     "LEFT JOIN WorkspaceTable w ON t.workspaceID = w.workspaceID " +
                     "WHERE t.userOwner = ? " +
                     "ORDER BY t.dueDate ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int taskID = rs.getInt("taskID");
                String title = rs.getString("taskTitle");
                String status = rs.getString("taskStatus");
                Date date = rs.getDate("dueDate");
                LocalDate dueDate = (date != null) ? date.toLocalDate() : null;
                String priority = rs.getString("taskPriority");
                int workspaceID = rs.getInt("workspaceID");
                String workspaceName = rs.getString("workspaceName");

                tasks.add(new MyTask(taskID, title, status, dueDate, priority, workspaceID, workspaceName));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions as needed
        }

        return tasks;
    }
}