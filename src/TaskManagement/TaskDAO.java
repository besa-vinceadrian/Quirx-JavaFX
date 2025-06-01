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
        int actualWorkspaceID = ensureWorkspaceExists(workspaceName, createdByUser);
        if (actualWorkspaceID == -1) {
            return false;
        }

        String sql = "INSERT INTO TaskTable (taskTitle, userOwner, dueDate, taskStatus, taskPriority, workspaceID) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, t.getTaskTitle());
            st.setString(2, t.getUserOwner());

            String dueDateStr = t.getDueDate();
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                try {
                    LocalDate parsedDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
                    st.setDate(3, java.sql.Date.valueOf(parsedDate));
                } catch (DateTimeParseException e) {
                    st.setNull(3, Types.DATE);
                }
            } else {
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
                return true;
            }

        } catch (SQLIntegrityConstraintViolationException dup) {
            // silently ignore duplicate/constraint errors
        } catch (SQLException sqlEx) {
            // silently ignore SQL exceptions
        } catch (Exception e) {
            // silently ignore any other exceptions
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
            // silently ignore errors
        }
        return false;
    }

    private static int ensureWorkspaceExists(String workspaceName, String createdByUser) {
        String selectSql = "SELECT workspaceID FROM WorkspaceTable WHERE workspaceName = ? AND createdByUser = ?";
        String insertSql = "INSERT INTO WorkspaceTable (workspaceName, createdByUser) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement selectStmt = con.prepareStatement(selectSql);
             PreparedStatement insertStmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            selectStmt.setString(1, workspaceName);
            selectStmt.setString(2, createdByUser);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("workspaceID");
            }

            insertStmt.setString(1, workspaceName);
            insertStmt.setString(2, createdByUser);
            insertStmt.executeUpdate();

            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (Exception e) {
            // silently ignore errors
        }

        return -1;
    }

    public static void deleteTask(int taskID) {
        String sql = "DELETE FROM TaskTable WHERE taskID = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, taskID);
            st.executeUpdate();

        } catch (Exception e) {
            // silently ignore errors
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
                st.setNull(3, Types.DATE);
            }

            st.setString(4, t.getTaskStatus());
            st.setString(5, t.getTaskPriority());
            st.setInt(6, t.getTaskID());

            st.executeUpdate();

        } catch (Exception e) {
            // silently ignore errors
        }
    }

    public static int mapPriority(String priority) {
        if (priority == null) return 0;
        switch (priority.toLowerCase()) {
            case "high": return 3;
            case "medium": return 2;
            case "low": return 1;
            default: return 0;
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
                task.setWorkspaceName(rs.getString("workspaceName"));

                taskList.add(task);
            }

        } catch (Exception e) {
            // silently ignore errors
        }

        PriorityQueue<TaskModel> pq = new PriorityQueue<>((t1, t2) -> {
            int p1 = mapPriority(t1.getPriority());
            int p2 = mapPriority(t2.getPriority());

            if (p1 != p2) {
                return Integer.compare(p2, p1);
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

            checkStmt.setString(1, workspaceName);
            checkStmt.setString(2, createdByUser);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("workspaceID");
            }

            insertStmt.setString(1, workspaceName);
            insertStmt.setString(2, createdByUser);
            int rows = insertStmt.executeUpdate();

            if (rows > 0) {
                ResultSet keys = insertStmt.getGeneratedKeys();
                if (keys.next()) {
                    int newID = keys.getInt(1);

                    addMemberStmt.setInt(1, newID);
                    addMemberStmt.setString(2, createdByUser);
                    addMemberStmt.executeUpdate();

                    return newID;
                }
            }

        } catch (SQLException e) {
            // silently ignore errors
        }

        return -1;
    }

    public static List<WorkspaceGroup> getUserGroupWorkspaces(String username) {
        List<WorkspaceGroup> workspaces = new ArrayList<>();

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

            Set<Integer> seenWorkspaceIDs = new HashSet<>();

            while (rs.next()) {
                int workspaceID = rs.getInt("workspaceID");
                String workspaceName = rs.getString("workspaceName");

                // Prevent duplicate IDs, but allow duplicate names
                if (seenWorkspaceIDs.add(workspaceID)) {
                    workspaces.add(new WorkspaceGroup(workspaceID, workspaceName));
                }
            }

        } catch (SQLException e) {
            // silently ignore errors
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

            getWorkspaceStmt.setInt(1, workspaceID);
            ResultSet rs = getWorkspaceStmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String creator = rs.getString("createdByUser");

            if (creator.equalsIgnoreCase(username)) {
                deleteTasksStmt.setInt(1, workspaceID);
                deleteTasksStmt.executeUpdate();

                deleteMembersStmt.setInt(1, workspaceID);
                deleteMembersStmt.executeUpdate();

                deleteWorkspaceStmt.setInt(1, workspaceID);
                int rowsAffected = deleteWorkspaceStmt.executeUpdate();

                return rowsAffected > 0;
            } else {
                checkMemberStmt.setInt(1, workspaceID);
                checkMemberStmt.setString(2, username);
                ResultSet memberCheck = checkMemberStmt.executeQuery();

                if (memberCheck.next()) {
                    deleteUserTasksStmt.setInt(1, workspaceID);
                    deleteUserTasksStmt.setString(2, username);
                    deleteUserTasksStmt.executeUpdate();

                    removeUserStmt.setInt(1, workspaceID);
                    removeUserStmt.setString(2, username);
                    int memberRows = removeUserStmt.executeUpdate();

                    return memberRows > 0;
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            return false;
        }
    }

    public static WorkspaceGroup getCurrentWorkspace(String username, String workspaceNameWithID) {
        int workspaceID = extractID(workspaceNameWithID);
        if (workspaceID == -1) {
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

            st.setInt(1, workspaceID);
            st.setString(2, username);
            st.setString(3, username);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("workspaceID");
                String actualName = rs.getString("workspaceName");

                return new WorkspaceGroup(id, actualName);
            }

        } catch (SQLException e) {
            // no logging
        }

        return null;
    }

    // Helper method unchanged except removed printStackTrace()
    private static int extractID(String workspaceNameWithID) {
        try {
            int start = workspaceNameWithID.indexOf("(ID: ");
            int end = workspaceNameWithID.indexOf(")", start);
            if (start != -1 && end != -1) {
                String idStr = workspaceNameWithID.substring(start + 5, end).trim();
                return Integer.parseInt(idStr);
            }
        } catch (Exception e) {
            // no logging
        }
        return -1;
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

            return members;

        } catch (SQLException e) {
            // no logging
        }

        return null;
    }

    public static boolean addMemberToWorkspace(int workspaceID, String memberUsername) {
        String sql = "INSERT INTO WorkspaceMembersTable (workspaceID, memberUserName) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, workspaceID);
            st.setString(2, memberUsername);

            int rowsAffected = st.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            // no logging
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
            // no logging
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
            // no logging
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
            // Logging removed, but error handling preserved
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
            // Logging removed
        }
        return false;
    }

    public static boolean addTaskSimple(TaskModel t, int workspaceID) {
        String sql = "INSERT INTO TaskTable (taskTitle, userOwner, dueDate, taskStatus, taskPriority, workspaceID) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, t.getTaskTitle());
            st.setString(2, t.getUserOwner());

            String dueDateStr = t.getDueDate();
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                try {
                    LocalDate parsedDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
                    st.setDate(3, java.sql.Date.valueOf(parsedDate));
                } catch (DateTimeParseException e) {
                    st.setNull(3, Types.DATE);
                }
            } else {
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
                return true;
            }

        } catch (SQLIntegrityConstraintViolationException dup) {
            // Logging removed
        } catch (SQLException sqlEx) {
            // Logging removed
        } catch (Exception e) {
            // Logging removed
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
            // Logging removed
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
            // Logging removed
            return false;
        }
    }
}
