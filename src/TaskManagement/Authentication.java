package TaskManagement;

import java.sql.*;
import java.util.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import javax.naming.directory.*;
import javax.naming.NamingException;
import javax.naming.Context;

public class Authentication {
    private static final String DB_URL = "jdbc:sqlserver://10.244.202.169:1433;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "QuirxAdmin";
    private static final String DB_PASS = "admin";

    private static final long OTP_VALID_DURATION = 5 * 60 * 1000; // 5 minutes
    private static final long OTP_RESEND_INTERVAL = 60 * 1000; // 1 minute

    private String generatedOTP;
    private long otpGenerationTime;
    private long lastOTPSentTime;
    private boolean hasResentOTP = false;

    public Connection connect() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
    
    public WorkspaceInfo getOrCreateWorkspaceForUser(String username) {
        String workspaceName = "Personal Workspace";
        int workspaceId = -1;

        String checkQuery = "SELECT workspaceID, workspaceName FROM WorkspaceTable WHERE createdByUser = ?";
        // For SQL Server, you can retrieve inserted ID like this:
        String insertQuery = "INSERT INTO WorkspaceTable (workspaceName, createdByUser) OUTPUT INSERTED.workspaceID VALUES (?, ?)";

        try (Connection con = connect();
             PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                workspaceId = rs.getInt("workspaceID");
                workspaceName = rs.getString("workspaceName");
            } else {
                try (PreparedStatement insertStmt = con.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, workspaceName);
                    insertStmt.setString(2, username);
                    ResultSet generatedKeys = insertStmt.executeQuery();
                    if (generatedKeys.next()) {
                        workspaceId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new WorkspaceInfo(workspaceId, workspaceName);
    }

    public int getUserIdByUsername(String username) throws SQLException {
        try (Connection con = connect()) {
            String query = "SELECT userID FROM UserTable WHERE userName = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("userID");
            }
            return -1; // User not found
        }
    }

    public boolean hasMXRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            return attrs.get("MX") != null;
        } catch (NamingException e) {
            return false;
        }
    }
    
    public boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9.-]+)\\.[A-Za-z]{2,}$";
        if (email == null || !email.matches(regex)) {
            return false;
        }

        String domain = email.substring(email.indexOf('@') + 1);
        return hasMXRecord(domain);
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        try (Connection con = connect()) {
            String query = "SELECT userName FROM UserTable WHERE userName = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public boolean isEmailTaken(String email) throws SQLException {
        try (Connection con = connect()) {
            String query = "SELECT userEmail FROM UserTable WHERE userEmail = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public void sendOTP(String to, String otp) {
        final String from = "quirxg8@gmail.com";
        final String password = "vnks zpzg decz gyzx";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Email Verification OTP");
            
            String htmlContent = "<html>" +
            	    "<body style='font-family: Arial, sans-serif;'>" +
            	    "<h1 style='color: #2E86C1;'>Email Verification</h1>" +
            	    "<p>Dear user,</p>" +
            	    "<p>To complete your email verification, please use the following One-Time Password (OTP):</p>" +
            	    "<h1 style='color: #28a745; letter-spacing: 8px;'>" + otp + "</h1>" +
            	    "<p>This OTP is valid for <strong>5 minutes</strong>. Please do not share it with anyone.</p>" +
            	    "<p>If you did not request this verification, please ignore this email.</p>" +
            	    "<br><p>Thank you,<br>The Quirx Team</p>" +
            	    "</body>" +
            	    "</html>";

            message.setContent(htmlContent, "text/html");

            Transport.send(message);
            lastOTPSentTime = System.currentTimeMillis();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void generateAndSendOTP(String email) {
        generatedOTP = String.format("%06d", new Random().nextInt(1000000));
        otpGenerationTime = System.currentTimeMillis();
        sendOTP(email, generatedOTP);
        hasResentOTP = false;
    }

    public boolean verifyOTP(String enteredOTP) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - otpGenerationTime > OTP_VALID_DURATION) {
            return false; // OTP expired
        }
        return enteredOTP.equals(generatedOTP);
    }
    
    public boolean canResendOTP() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastOTPSentTime) >= OTP_RESEND_INTERVAL;
    }

    public boolean registerUser(String fname, String lname, String uname, String email, String password) {
        try (Connection con = connect()) {
            String query = "INSERT INTO UserTable (userFirstName, userLastName, userName, userEmail, userPassword) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, fname);
            ps.setString(2, lname);
            ps.setString(3, uname);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUser(String email, String password) {
        try (Connection con = connect()) {
            String query = "SELECT * FROM UserTable WHERE userEmail = ? AND userPassword = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean loginUserByUsername(String username, String password) {
        try (Connection con = connect()) {
            String query = "SELECT * FROM UserTable WHERE userName = ? AND userPassword = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPassword(String email, String newPassword) {
        try (Connection con = connect()) {
            String query = "UPDATE UserTable SET userPassword = ? WHERE userEmail = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, newPassword);
            ps.setString(2, email);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}