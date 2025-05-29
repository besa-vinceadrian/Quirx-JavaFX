package TaskManagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyProfile {
    private int userId;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String password;

    private static final String DB_URL = "jdbc:sqlserver://10.244.202.169:1433;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "QuirxAdmin";
    private static final String DB_PASS = "admin";

    public MyProfile(int userId, String firstName, String lastName, String userName, String email, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static MyProfile loadUserById(int userId) throws SQLException {
        String query = "SELECT * FROM UserTable WHERE userID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new MyProfile(
                    rs.getInt("userID"),
                    rs.getString("userFirstName"),
                    rs.getString("userLastName"),
                    rs.getString("userName"),
                    rs.getString("userEmail"),
                    rs.getString("userPassword")
                );
            } else {
                throw new SQLException("User not found.");
            }
        }
    }

    public void updateUser() throws SQLException {
        String query = "UPDATE UserTable SET userFirstName = ?, userLastName = ?, userEmail = ?, userPassword = ? WHERE userID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setInt(5, userId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Validates the provided password against the stored password in the database.
     * 
     * @param inputPassword The password to validate.
     * @return true if the password matches, false otherwise.
     */
    		
    public boolean validatePassword(String inputPassword) {
    	String query = "SELECT userPassword FROM UserTable WHERE userID = ?";
    	
    	try (Connection conn = getConnection();
    		PreparedStatement stmt = conn.prepareStatement(query)) {
    		
    		stmt.setInt(1, this.userId);
    		ResultSet rs = stmt.executeQuery();
    		
    		if (rs.next()) {
    			String storedPassword = rs.getString("userPassword");
    			return storedPassword.equals(inputPassword);
    		}
    	} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    /**
     * Deletes the user account from the database
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteAccount() {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Get all workspaceIDs created by this user
            String getWorkspacesSQL = "SELECT workspaceID FROM WorkspaceTable WHERE createdByUser = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getWorkspacesSQL)) {
                stmt.setString(1, this.userName);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int workspaceId = rs.getInt("workspaceID");

                    // Step 2: Delete from WorkspaceMembersTable where workspaceID = ?
                    String deleteWorkspaceMembersSQL = "DELETE FROM WorkspaceMembersTable WHERE workspaceID = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteWorkspaceMembersSQL)) {
                        deleteStmt.setInt(1, workspaceId);
                        deleteStmt.executeUpdate();
                    }
                }
            }

            // Step 3: Delete tasks assigned to this user
            String deleteTasksSQL = "DELETE FROM TaskTable WHERE userOwner = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteTasksSQL)) {
                stmt.setString(1, this.userName);
                stmt.executeUpdate();
            }

            // Step 4: Delete workspaces owned by this user
            String deleteWorkspacesSQL = "DELETE FROM WorkspaceTable WHERE createdByUser = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteWorkspacesSQL)) {
                stmt.setString(1, this.userName);
                stmt.executeUpdate();
            }

            // Step 2.5: Delete all memberships where user is a member of any workspace
            String deleteUserMembershipSQL = "DELETE FROM WorkspaceMembersTable WHERE memberUserName = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteUserMembershipSQL)) {
                stmt.setString(1, this.userName);
                stmt.executeUpdate();
            }

            // Step 5: Delete user
            String deleteUserSQL = "DELETE FROM UserTable WHERE userID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteUserSQL)) {
                stmt.setInt(1, this.userId);
                int rowsAffected = stmt.executeUpdate();
                conn.commit(); // Commit transaction
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Getters and setters
    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}