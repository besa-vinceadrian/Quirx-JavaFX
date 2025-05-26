package TaskManagement;

import java.sql.*;
import java.util.*;


public class TaskDAO {
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

    // ✅ Check if workspace exists by name
    public static Integer getWorkspaceIDByName(String workspaceName) {
        String sql = "SELECT workspaceID FROM WorkspaceTable WHERE workspaceName = ?";
        try (Connection c = connect(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, workspaceName);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("workspaceID");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking workspace by name: " + e.getMessage());
        }
        return null;
    }

    // ✅ Create or return existing workspace ID
    public static int getOrCreateWorkspace(String workspaceName) {
        Integer existingID = getWorkspaceIDByName(workspaceName);
        if (existingID != null) {
            return existingID;
        }

        String sql = "INSERT INTO WorkspaceTable (workspaceName) VALUES (?)";
        try (Connection c = connect();
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, workspaceName);
            st.executeUpdate();

            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                int generatedID = rs.getInt(1);
                System.out.println("✅ Workspace created with ID: " + generatedID);
                return generatedID;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error creating workspace: " + e.getMessage());
        }
        return -1;
    }

    // ✅ Add task after getting/creating a workspace
 // ✅ Add task after getting/creating a workspace
    public static boolean addTask(UserTask t) {
        String workspaceName = "Default Workspace " + t.getWorkspaceID(); // Or t.getWorkspaceName() if available
        int actualWorkspaceID = getOrCreateWorkspace(workspaceName);
        if (actualWorkspaceID == -1) return false;

        // ✅ Ensure the task has the correct workspace ID before inserting
        t.setWorkspaceID(actualWorkspaceID);

        String sql = """
            INSERT INTO TaskTable (taskTitle, taskStatus, dueDate, taskPriority, userOwner, workspaceID, completed)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = connect();
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, t.getTaskTitle());
            st.setString(2, t.getTaskStatus().toDatabaseValue());
            st.setDate(3, t.getDueDate());
            st.setString(4, t.getTaskPriority().toDatabaseValue());
            st.setString(5, t.getUserOwner());
            st.setInt(6, actualWorkspaceID); // ✅ use the actual ID from DB
            st.setBoolean(7, t.completedProperty().get());

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    System.out.println("✅ Task added with ID: " + generatedId);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding task: " + e.getMessage());
        }
        return false;
    }

    public static List<UserTask> getAllTasks(String userOwner) {
        List<UserTask> list = new ArrayList<>();
        String sql = """
            SELECT taskID, taskTitle, taskStatus, dueDate, taskPriority, userOwner, workspaceID, completed
            FROM TaskTable
            WHERE userOwner = ?
        """;
        try (Connection c = connect(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, userOwner);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new UserTask(
                    rs.getInt("taskID"),
                    rs.getString("taskTitle"),
                    UserTask.Status.fromDatabaseValue(rs.getString("taskStatus")),
                    rs.getDate("dueDate"),
                    UserTask.Priority.fromString(rs.getString("taskPriority")),
                    rs.getString("userOwner"),
                    rs.getInt("workspaceID"),
                    rs.getBoolean("completed")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving tasks: " + e.getMessage());
        }
        return list;
    }

    public static List<UserTask> getTasksByWorkspace(String userOwner, int workspaceID) {
        List<UserTask> list = new ArrayList<>();
        String sql = """
        	    SELECT taskID, taskTitle, taskStatus, dueDate, taskPriority, userOwner, workspaceID, completed
        	    FROM TaskTable
        	    WHERE userOwner = ? AND workspaceID = ?
        	    ORDER BY 
        	        CASE taskPriority
        	            WHEN 'HIGH' THEN 1
        	            WHEN 'MEDIUM' THEN 2
        	            WHEN 'LOW' THEN 3
        	            ELSE 4
        	        END
        	""";

        System.out.println("➡️ Querying tasks for user: " + userOwner + ", workspaceID: " + workspaceID);

        try (Connection c = connect(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, userOwner);
            st.setInt(2, workspaceID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                UserTask task = new UserTask(
                    rs.getInt("taskID"),
                    rs.getString("taskTitle"),
                    UserTask.Status.fromDatabaseValue(rs.getString("taskStatus")),
                    rs.getDate("dueDate"),
                    UserTask.Priority.fromString(rs.getString("taskPriority")),
                    rs.getString("userOwner"),
                    rs.getInt("workspaceID"),
                    rs.getBoolean("completed")
                );
                System.out.println("✅ Found task: " + task.getTaskTitle());
                list.add(task);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving tasks by workspace: " + e.getMessage());
        }
        return list;
    }

    public static void updateTask(UserTask t) {
        String sql = """
            UPDATE TaskTable
            SET taskTitle=?, taskStatus=?, dueDate=?, taskPriority=?, workspaceID=?, completed=?
            WHERE taskID=? AND userOwner=?
        """;
        try (Connection c = connect(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, t.getTaskTitle());
            st.setString(2, t.getTaskStatus().toDatabaseValue());
            st.setDate(3, t.getDueDate());
            st.setString(4, t.getTaskPriority().toDatabaseValue());
            st.setInt(5, t.getWorkspaceID());
            st.setBoolean(6, t.completedProperty().get());
            st.setInt(7, t.getTaskID());
            st.setString(8, t.getUserOwner());

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
