package TaskManagement;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import application.TaskModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.PriorityQueue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Set;
import java.util.HashSet;


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

        // Join TaskTable with WorkspaceTable to fetch the workspace name
        String sql = """
            SELECT t.*, w.workspaceName
            FROM TaskTable t
            JOIN WorkspaceTable w ON t.workspaceID = w.workspaceID
            WHERE t.workspaceID = ?
            """;

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
                task.setWorkspaceName(rs.getString("workspaceName")); // New field

                taskList.add(task);
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching tasks for workspace");
            e.printStackTrace();
        }

        // Sort using PriorityQueue
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

        System.out.println("✅ Sorted Tasks by Priority using PriorityQueue:");
        for (TaskModel t : sortedTasks) {
            System.out.println(t.getPriority() + " - " + t.getDueDate() + " | Workspace: " + t.getWorkspaceName());
        }

        return sortedTasks;
    }
    
    public static int createWorkspaceForUser(String workspaceName, String createdByUser) {
        String checkSql = "SELECT workspaceID FROM WorkspaceTable WHERE workspaceName = ? AND createdByUser = ?";
        String insertSql = "INSERT INTO WorkspaceTable (workspaceName, createdByUser) VALUES (?, ?)";
        String addMemberSql = "INSERT INTO WorkspaceMembersTable (workspaceID, memberUserName) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement checkStmt = con.prepareStatement(checkSql);
             PreparedStatement insertStmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement addMemberStmt = con.prepareStatement(addMemberSql)) {

            // Check if workspace already exists
            checkStmt.setString(1, workspaceName);
            checkStmt.setString(2, createdByUser);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int existingID = rs.getInt("workspaceID");
                System.out.println("ℹ️ Workspace already exists. ID = " + existingID);
                return existingID;
            }

            // Insert new workspace
            insertStmt.setString(1, workspaceName);
            insertStmt.setString(2, createdByUser);
            int rows = insertStmt.executeUpdate();

            if (rows > 0) {
                ResultSet keys = insertStmt.getGeneratedKeys();
                if (keys.next()) {
                    int newID = keys.getInt(1);
                    
                    // Add creator as member
                    addMemberStmt.setInt(1, newID);
                    addMemberStmt.setString(2, createdByUser);
                    addMemberStmt.executeUpdate();
                    
                    System.out.println("✅ New workspace created: " + workspaceName + " (ID: " + newID + ")");
                    return newID;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error inserting new workspace: " + workspaceName);
            e.printStackTrace();
        }

        return -1; // Failed to create workspace
    }

    public static List<String> getUserGroupWorkspaces(String username) {
        List<String> workspaces = new ArrayList<>();
        Set<Integer> seenWorkspaceIDs = new HashSet<>();  // To prevent duplicates

        String sql = """
            SELECT w.workspaceID, w.workspaceName
            FROM WorkspaceTable w
            WHERE w.createdByUser = ? AND w.workspaceName != 'Personal Workspace'

            UNION ALL

            SELECT w.workspaceID, w.workspaceName
            FROM WorkspaceTable w
            JOIN WorkspaceMembersTable wm ON w.workspaceID = wm.workspaceID
            WHERE wm.memberUserName = ? AND w.workspaceName != 'Personal Workspace'
            """;

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, username);
            st.setString(2, username);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                int workspaceID = rs.getInt("workspaceID");
                String workspaceName = rs.getString("workspaceName");

                if (!seenWorkspaceIDs.contains(workspaceID)) {
                    seenWorkspaceIDs.add(workspaceID);
                    workspaces.add(workspaceName + " (ID: " + workspaceID + ")");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching group workspaces for user: " + username);
            e.printStackTrace();
        }

        return workspaces;
    }


    public static boolean deleteWorkspace(int workspaceID, String username) {
        String getWorkspaceSql = "SELECT createdByUser FROM WorkspaceTable WHERE workspaceID = ?";
        String checkMemberSql = "SELECT 1 FROM WorkspaceMembersTable WHERE workspaceID = ? AND memberUserName = ?";
        String deleteTasksSql = "DELETE FROM TaskTable WHERE workspaceID = ?";
        String deleteUserTasksSql = "DELETE FROM TaskTable WHERE workspaceID = ? AND userOwner = ?";
        String deleteMembersSql = "DELETE FROM WorkspaceMembersTable WHERE workspaceID = ?";
        String deleteWorkspaceSql = "DELETE FROM WorkspaceTable WHERE workspaceID = ?";
        String removeUserFromMembersSql = "DELETE FROM WorkspaceMembersTable WHERE workspaceID = ? AND memberUserName = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement getWorkspaceStmt = con.prepareStatement(getWorkspaceSql);
             PreparedStatement checkMemberStmt = con.prepareStatement(checkMemberSql);
             PreparedStatement deleteTasksStmt = con.prepareStatement(deleteTasksSql);
             PreparedStatement deleteUserTasksStmt = con.prepareStatement(deleteUserTasksSql);
             PreparedStatement deleteMembersStmt = con.prepareStatement(deleteMembersSql);
             PreparedStatement deleteWorkspaceStmt = con.prepareStatement(deleteWorkspaceSql);
             PreparedStatement removeUserStmt = con.prepareStatement(removeUserFromMembersSql)) {

            // 🧪 Check if workspace exists and get the creator
            getWorkspaceStmt.setInt(1, workspaceID);
            ResultSet rs = getWorkspaceStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("⚠️ Workspace not found with ID: " + workspaceID);
                return false;
            }

            String creator = rs.getString("createdByUser");

            if (creator.equalsIgnoreCase(username)) {
                // 👤 User is the creator — delete the entire workspace and all its data
                deleteTasksStmt.setInt(1, workspaceID);
                deleteTasksStmt.executeUpdate();

                deleteMembersStmt.setInt(1, workspaceID);
                deleteMembersStmt.executeUpdate();

                deleteWorkspaceStmt.setInt(1, workspaceID);
                int rowsAffected = deleteWorkspaceStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("🗑️ Workspace deleted by creator (ID: " + workspaceID + ")");
                    return true;
                } else {
                    System.err.println("❌ Failed to delete the workspace.");
                    return false;
                }
            } else {
                // 🧪 Check if user is a member
                checkMemberStmt.setInt(1, workspaceID);
                checkMemberStmt.setString(2, username);
                ResultSet memberCheck = checkMemberStmt.executeQuery();

                if (memberCheck.next()) {
                    // ✅ Delete user's tasks in that workspace
                    deleteUserTasksStmt.setInt(1, workspaceID);
                    deleteUserTasksStmt.setString(2, username);
                    deleteUserTasksStmt.executeUpdate();

                    // ✅ Remove user from member list
                    removeUserStmt.setInt(1, workspaceID);
                    removeUserStmt.setString(2, username);
                    int memberRows = removeUserStmt.executeUpdate();

                    if (memberRows > 0) {
                        System.out.println("👤 Member '" + username + "' left workspace (ID: " + workspaceID + ") and their tasks were deleted.");
                        return true;
                    } else {
                        System.out.println("⚠️ Could not remove user '" + username + "' from workspace (ID: " + workspaceID + ")");
                        return false;
                    }
                } else {
                    // ❌ User is not a member
                    System.out.println("⚠️ User '" + username + "' is not associated with workspace (ID: " + workspaceID + ")");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Exception while deleting workspace/member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }




    
    public static WorkspaceGroup getCurrentWorkspace(String username, String workspaceNameWithID) {
        int workspaceID = extractID(workspaceNameWithID);
        if (workspaceID == -1) {
            System.err.println("⚠️ Invalid workspace name format: " + workspaceNameWithID);
            return null;
        }

        String sql = """
            SELECT DISTINCT w.workspaceID, w.workspaceName
            FROM WorkspaceTable w
            LEFT JOIN WorkspaceMembersTable wm ON w.workspaceID = wm.workspaceID
            WHERE w.workspaceID = ?
              AND (w.createdByUser = ? OR wm.memberUserName = ?)
            """;

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, workspaceID);       // query by ID now
            st.setString(2, username);
            st.setString(3, username);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("workspaceID");
                String actualName = rs.getString("workspaceName");

                System.out.println("✅ Found workspace: ID = " + id + ", name = " + actualName);
                return new WorkspaceGroup(id, actualName);
            } else {
                System.out.println("⚠️ No accessible workspace found for user: " + username + ", workspace ID: " + workspaceID);
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL error fetching workspace:");
            e.printStackTrace();
        }

        return null; // Not found or error
    }

    // Helper method to extract ID from string like "same (ID: 197)"
    private static int extractID(String workspaceNameWithID) {
        try {
            int start = workspaceNameWithID.indexOf("(ID: ");
            int end = workspaceNameWithID.indexOf(")", start);
            if (start != -1 && end != -1) {
                String idStr = workspaceNameWithID.substring(start + 5, end).trim();
                return Integer.parseInt(idStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // invalid format
    }




    public static ObservableList<String> getAllWorkspaceMembers(String currentUsername, int currentWorkspaceID) {
        String sql = "SELECT DISTINCT memberUserName FROM WorkspaceMembersTable WHERE workspaceID = ?";
        ObservableList<String> members = FXCollections.observableArrayList();

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, currentWorkspaceID);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String name = rs.getString("memberUserName");
                if (name.equals(currentUsername)) {
                    if (!members.contains(currentUsername + " (me)")) {
                        members.add(currentUsername + " (me)");
                    }
                } else if (!members.contains(name)) {
                    members.add(name);
                }
            }

            System.out.println("✅ Fetched workspace members: " + members);
            return members;

        } catch (SQLException e) {
            System.err.println("❌ SQL error fetching workspace members:");
            e.printStackTrace();
        }

        return null; // On error
    }


    public static boolean addMemberToWorkspace(int workspaceID, String memberUsername) {
        String sql = "INSERT INTO WorkspaceMembersTable (workspaceID, memberUserName) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, workspaceID);
            st.setString(2, memberUsername);

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Member '" + memberUsername + "' added to workspace ID: " + workspaceID);
                return true;
            } else {
                System.out.println("⚠️ No rows inserted. Check workspace ID or username.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error adding member to workspace:");
            e.printStackTrace();
        }

        return false;
    }

    public static boolean doesUserExist(String username) {
        String sql = "SELECT COUNT(*) FROM UserTable WHERE userName = ?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error checking user existence: " + e.getMessage());
        }

        return false;
    }
    
    public static String getUserEmail(String username) {
        String sql = "SELECT userEmail FROM UserTable WHERE userName = ?";
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {
            
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("userEmail");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching user email: " + e.getMessage());
        }
        return null;
    }

    public static Integer fetchWorkspaceIdForUser(String workspaceName, String username) {
        String findOwnedSql = "SELECT workspaceID FROM WorkspaceTable WHERE workspaceName = ? AND createdByUser = ?";
        String findMemberSql = "SELECT wm.workspaceID FROM WorkspaceMembersTable wm " +
                               "JOIN WorkspaceTable wt ON wm.workspaceID = wt.workspaceID " +
                               "WHERE wt.workspaceName = ? AND wm.memberUserName = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmtOwner = con.prepareStatement(findOwnedSql);
             PreparedStatement stmtMember = con.prepareStatement(findMemberSql)) {

            // Check if user is the creator
            stmtOwner.setString(1, workspaceName);
            stmtOwner.setString(2, username);
            ResultSet rsOwner = stmtOwner.executeQuery();
            if (rsOwner.next()) {
                return rsOwner.getInt("workspaceID");
            }

            // If not the creator, check if user is a member
            stmtMember.setString(1, workspaceName);
            stmtMember.setString(2, username);
            ResultSet rsMember = stmtMember.executeQuery();
            if (rsMember.next()) {
                return rsMember.getInt("workspaceID");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching workspace ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Not found
    }
    
    public static boolean workspaceNameExists(String workspaceName, String username) {
        String sql = "SELECT COUNT(*) FROM WorkspaceTable WHERE workspaceName = ? AND " +
                     "(createdByUser = ? OR workspaceID IN " +
                     "(SELECT workspaceID FROM WorkspaceMembersTable WHERE memberUserName = ?))";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {
            
            st.setString(1, workspaceName);
            st.setString(2, username);
            st.setString(3, username);
            
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking workspace name existence:");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean addTaskSimple(TaskModel t, int workspaceID) {
        String sql = "INSERT INTO TaskTable (taskTitle, userOwner, dueDate, taskStatus, taskPriority, workspaceID) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("🔍 Inserting Task:");
            System.out.println("Title: " + t.getTaskTitle());
            System.out.println("Owner: " + t.getUserOwner());
            System.out.println("Due Date: " + t.getDueDate());
            System.out.println("Status: " + t.getTaskStatus());
            System.out.println("Priority: " + t.getTaskPriority());
            System.out.println("Workspace ID: " + workspaceID);

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
            st.setInt(6, workspaceID);

            int rows = st.executeUpdate();
            if (rows > 0) {
                ResultSet keys = st.getGeneratedKeys();
                if (keys.next()) {
                    t.setTaskID(keys.getInt(1));
                }
                t.setWorkspaceID(workspaceID);
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

    public static WorkspaceGroup getWorkspaceByIdAndUser(int workspaceID, String username) {
        String sql = """
            SELECT DISTINCT w.workspaceID, w.workspaceName
            FROM WorkspaceTable w
            LEFT JOIN WorkspaceMembersTable wm ON w.workspaceID = wm.workspaceID
            WHERE w.workspaceID = ?
              AND (w.createdByUser = ? OR wm.memberUserName = ?)
        """;

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, workspaceID);
            st.setString(2, username);
            st.setString(3, username);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new WorkspaceGroup(
                    rs.getInt("workspaceID"),
                    rs.getString("workspaceName")
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching workspace by ID:");
            e.printStackTrace();
        }

        return null;
    }

    public static boolean updateTaskStatus(int taskID, String newStatus) {
        String sql = "UPDATE TaskTable SET taskStatus = ? WHERE taskID = ?";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, taskID);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to update task status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    
}
