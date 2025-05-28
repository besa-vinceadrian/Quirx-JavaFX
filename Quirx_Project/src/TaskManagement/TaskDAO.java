package TaskManagement;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import application.TaskModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.PriorityQueue;

public class TaskDAO {

    private static final String URL = "jdbc:sqlserver://10.244.202.169:1433;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
    private static final String USER = "QuirxAdmin";
    private static final String PASSWORD = "admin";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static boolean addTask(TaskModel t, int workspaceID, String workspaceName, String createdByUser) {
        // Use real workspaceID (from DB)
        int actualWorkspaceID = ensureWorkspaceExists(workspaceName, createdByUser);
        if (actualWorkspaceID == -1) {
            System.err.println("❌ Failed to ensure workspace. Aborting task insert.");
            return false;
        }

        String sql = "INSERT INTO TaskTable (taskTitle, userOwner, dueDate, taskStatus, taskPriority, workspaceID) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("🔍 Inserting Task:");
            System.out.println("Title: " + t.getTaskTitle());
            System.out.println("Owner: " + t.getUserOwner());
            System.out.println("Due Date: " + t.getDueDate());
            System.out.println("Status: " + t.getTaskStatus());
            System.out.println("Priority: " + t.getTaskPriority());
            System.out.println("Workspace ID: " + actualWorkspaceID);

            st.setString(1, t.getTaskTitle());
            st.setString(2, t.getUserOwner());

            String dueDateStr = t.getDueDate();
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                try {
                    LocalDate parsedDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
                    st.setDate(3, java.sql.Date.valueOf(parsedDate));
                } catch (DateTimeParseException e) {
                    System.err.println("❌ Invalid date format: " + dueDateStr);
                    st.setNull(3, Types.DATE);
                }
            } else {
                System.out.println("ℹ️ Due date is null or empty. Setting to NULL.");
                st.setNull(3, Types.DATE);
            }

            st.setString(4, t.getTaskStatus());
            st.setString(5, t.getTaskPriority());
            st.setInt(6, actualWorkspaceID);

            int rows = st.executeUpdate();
            if (rows > 0) {
                ResultSet keys = st.getGeneratedKeys();
                if (keys.next()) {
                    t.setTaskID(keys.getInt(1));
                }
                t.setWorkspaceID(actualWorkspaceID);
                System.out.println("✅ Task inserted successfully (ID: " + t.getTaskID() + ")");
                return true;
            } else {
                System.err.println("⚠️ Task insert failed: No rows affected.");
            }

        } catch (SQLIntegrityConstraintViolationException dup) {
            System.err.println("❌ Duplicate or constraint error: " + dup.getMessage());
        } catch (SQLException sqlEx) {
            System.err.println("❌ SQL error: " + sqlEx.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Exception during task insert:");
            e.printStackTrace();
        }

        return false;
    }


    public static boolean wasTaskInserted(TaskModel t) {
        String sql = "SELECT COUNT(*) FROM TaskTable WHERE taskTitle = ? AND userOwner = ? AND workspaceID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, t.getTaskTitle());
            st.setString(2, t.getUserOwner());
            st.setInt(3, t.getWorkspaceID());

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            System.err.println("❌ Error checking if task was inserted:");
            e.printStackTrace();
        }
        return false;
    }

    private static int ensureWorkspaceExists(String workspaceName, String createdByUser) {
        String selectSql = "SELECT workspaceID FROM WorkspaceTable WHERE workspaceName = ? AND createdByUser = ?";
        String insertSql = "INSERT INTO WorkspaceTable (workspaceName, createdByUser) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement selectStmt = con.prepareStatement(selectSql);
             PreparedStatement insertStmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            // Try to find existing workspace
            selectStmt.setString(1, workspaceName);
            selectStmt.setString(2, createdByUser);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int existingID = rs.getInt("workspaceID");
                System.out.println("ℹ️ Workspace already exists. ID = " + existingID);
                return existingID;
            }

            // Insert new workspace
            insertStmt.setString(1, workspaceName);
            insertStmt.setString(2, createdByUser);
            insertStmt.executeUpdate();

            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) {
                int newID = keys.getInt(1);
                System.out.println("✅ Workspace inserted: " + workspaceName + " (ID: " + newID + ")");
                return newID;
            }

        } catch (Exception e) {
            System.err.println("❌ Error ensuring workspace exists:");
            e.printStackTrace();
        }

        return -1;
    }


    public static void deleteTask(int taskID) {
        String sql = "DELETE FROM TaskTable WHERE taskID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, taskID);
            int rows = st.executeUpdate();

            if (rows > 0) {
                System.out.println("🗑️ Task deleted: ID = " + taskID);
            } else {
                System.out.println("⚠️ No task found with ID: " + taskID);
            }

        } catch (Exception e) {
            System.err.println("❌ Error deleting task with ID " + taskID);
            e.printStackTrace();
        }
    }

    public static void updateTask(TaskModel t) {
        String sql = "UPDATE TaskTable SET taskTitle = ?, userOwner = ?, dueDate = ?, taskStatus = ?, taskPriority = ? WHERE taskID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, t.getTaskTitle());
            st.setString(2, t.getUserOwner());

            try {
                LocalDate parsedDate = LocalDate.parse(t.getDueDate(), DATE_FORMATTER);
                st.setDate(3, java.sql.Date.valueOf(parsedDate));
            } catch (DateTimeParseException e) {
                System.err.println("⚠️ Invalid date format during update: " + t.getDueDate());
                st.setNull(3, Types.DATE);
            }

            st.setString(4, t.getTaskStatus());
            st.setString(5, t.getTaskPriority());
            st.setInt(6, t.getTaskID());

            int rows = st.executeUpdate();
            if (rows > 0) {
                System.out.println("✏️ Task updated: ID = " + t.getTaskID());
            } else {
                System.out.println("⚠️ Update failed. Task not found: ID = " + t.getTaskID());
            }

        } catch (Exception e) {
            System.err.println("❌ Error updating task with ID " + t.getTaskID());
            e.printStackTrace();
        }
    }

    public static TaskModel getTaskByID(int taskID) {
        String sql = "SELECT * FROM TaskTable WHERE taskID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, taskID);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                String formattedDate = "";
                Date sqlDate = rs.getDate("dueDate");
                if (sqlDate != null) {
                    formattedDate = sqlDate.toLocalDate().format(DATE_FORMATTER);
                }

                TaskModel t = new TaskModel(
                        rs.getString("taskTitle"),
                        rs.getString("userOwner"),
                        rs.getString("taskStatus"),
                        formattedDate,
                        rs.getString("taskPriority")
                );

                t.setTaskID(rs.getInt("taskID"));
                t.setWorkspaceID(rs.getInt("workspaceID"));
                return t;
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching task by ID: " + taskID);
            e.printStackTrace();
        }

        return null;
    }
    
    // === Static Utilities for Task Comparison ===

    public static int mapPriority(String priority) {
        if (priority == null) return 0;
        switch (priority.toLowerCase()) {
            case "high": return 3;
            case "medium": return 2;
            case "low": return 1;
            default:
                System.out.println("⚠️ Unknown priority: " + priority);
                return 0;
        }
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<TaskModel> getTasksByWorkspace(String username, int workspaceID) {
        List<TaskModel> taskList = new ArrayList<>();

        // Only fetch tasks from the given workspace
        String sql = "SELECT * FROM TaskTable WHERE workspaceID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, workspaceID);

            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String formattedDate = "";
                Date sqlDate = rs.getDate("dueDate");
                if (sqlDate != null) {
                    formattedDate = sqlDate.toLocalDate().format(DATE_FORMATTER);
                }

                TaskModel task = new TaskModel(
                    rs.getString("taskTitle"),
                    rs.getString("userOwner"),
                    rs.getString("taskStatus"),
                    formattedDate,
                    rs.getString("taskPriority")
                );

                task.setTaskID(rs.getInt("taskID"));
                task.setWorkspaceID(rs.getInt("workspaceID"));

                taskList.add(task);
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching tasks for workspace");
            e.printStackTrace();
        }

        // ✅ Now use PriorityQueue to sort
        PriorityQueue<TaskModel> pq = new PriorityQueue<>((t1, t2) -> {
            int p1 = mapPriority(t1.getPriority());
            int p2 = mapPriority(t2.getPriority());

            if (p1 != p2) {
                return Integer.compare(p2, p1); // Higher priority first
            }

            LocalDate d1 = parseDate(t1.getDueDate());
            LocalDate d2 = parseDate(t2.getDueDate());

            if (d1 != null && d2 != null) return d1.compareTo(d2);
            if (d1 != null) return -1;
            if (d2 != null) return 1;
            return 0;
        });

        pq.addAll(taskList);

        List<TaskModel> sortedTasks = new ArrayList<>();
        while (!pq.isEmpty()) {
            sortedTasks.add(pq.poll());
        }

        // Debug print
        System.out.println("✅ Sorted Tasks by Priority using PriorityQueue:");
        for (TaskModel t : sortedTasks) {
            System.out.println(t.getPriority() + " - " + t.getDueDate());
        }

        return sortedTasks;
    }

    
    public static List<TaskModel> getTasksByUserOrWorkspace(String username, int workspaceID) {
        List<TaskModel> taskList = new ArrayList<>();

        String sql = "SELECT * FROM TaskTable WHERE userOwner = ? " +
                     "UNION " +
                     "SELECT * FROM TaskTable WHERE workspaceID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, username);
            st.setInt(2, workspaceID);

            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String formattedDate = "";
                Date sqlDate = rs.getDate("dueDate");
                if (sqlDate != null) {
                    formattedDate = sqlDate.toLocalDate().format(DATE_FORMATTER);
                }

                TaskModel task = new TaskModel(
                    rs.getString("taskTitle"),
                    rs.getString("userOwner"),
                    rs.getString("taskStatus"),
                    formattedDate,
                    rs.getString("taskPriority")
                );

                task.setTaskID(rs.getInt("taskID"));
                task.setWorkspaceID(rs.getInt("workspaceID"));

                taskList.add(task);
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching tasks for user or workspace");
            e.printStackTrace();
        }

        // ✅ Now use PriorityQueue to sort
        PriorityQueue<TaskModel> pq = new PriorityQueue<>((t1, t2) -> {
            int p1 = mapPriority(t1.getPriority());
            int p2 = mapPriority(t2.getPriority());

            if (p1 != p2) {
                return Integer.compare(p2, p1); // Higher priority first
            }

            LocalDate d1 = parseDate(t1.getDueDate());
            LocalDate d2 = parseDate(t2.getDueDate());

            if (d1 != null && d2 != null) return d1.compareTo(d2);
            if (d1 != null) return -1;
            if (d2 != null) return 1;
            return 0;
        });

        pq.addAll(taskList);

        List<TaskModel> sortedTasks = new ArrayList<>();
        while (!pq.isEmpty()) {
            sortedTasks.add(pq.poll());
        }

        // Debug print
        System.out.println("✅ Sorted Tasks by Priority using PriorityQueue:");
        for (TaskModel t : sortedTasks) {
            System.out.println(t.getPriority() + " - " + t.getDueDate());
        }

        return sortedTasks;
    }

}
