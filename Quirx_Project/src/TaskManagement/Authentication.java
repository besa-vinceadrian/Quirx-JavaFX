package TaskManagement;

import java.sql.*;
import java.util.*;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class Authentication {
    static Scanner scanner = new Scanner(System.in);
    static String generatedOTP = "";
    static long otpGenerationTime = 0;
    static final long OTP_VALID_DURATION = 5 * 60 * 1000; // 5 minutes
    static int attemptsRemaining = 3;
    static boolean hasResentOTP = false;

    // DATABASE CONNECTION
    public static class DatabaseManager {
        private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Quirx_TMS;encrypt=true;trustServerCertificate=true";
        private static final String DB_USER = "LMS_Admin";
        private static final String DB_PASS = "benchbrian";

        public static Connection connect() throws SQLException {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
    }

    // SEND EMAIL OTP
    public static void sendOTP(String to, String otp) {
        final String from = "quirxg8@gmail.com";
        final String password = "vnks zpzg decz gyzx"; // App password

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

        session.setDebug(true); // Enable SMTP logs

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Password Reset OTP");
            message.setText("Your OTP is: " + otp + "\n\nThis OTP is valid for 5 minutes.");

            Transport.send(message);
            System.out.println("OTP sent to " + to);
        } catch (MessagingException e) {
            System.out.println("Failed to send OTP email.");
            e.printStackTrace();
        }
    }

    // Generate and send new OTP
    public static void generateAndSendOTP(String email) {
        generatedOTP = String.format("%06d", new Random().nextInt(1000000));
        otpGenerationTime = System.currentTimeMillis();
        attemptsRemaining = 3;
        sendOTP(email, generatedOTP);
    }

    // CHECK IF USERNAME EXISTS
    public static boolean isUsernameTaken(Connection con, String uname) throws SQLException {
        String query = "SELECT userName FROM UserTable WHERE userName = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, uname);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    // REGISTER USER
    public static void registerUser() {
        try (Connection con = DatabaseManager.connect()) {
            System.out.print("First Name: ");
            String fname = scanner.nextLine();
            System.out.print("Last Name: ");
            String lname = scanner.nextLine();

            String uname;
            while (true) {
                System.out.print("Username: ");
                uname = scanner.nextLine();

                if (isUsernameTaken(con, uname)) {
                    System.out.println("Username already exists. Please choose another one.");
                } else {
                    break;
                }
            }

            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Password: ");
            String pass = scanner.nextLine();

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO UserTable (userFirstName, userLastName, userName, userEmail, userPassword) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, fname);
            ps.setString(2, lname);
            ps.setString(3, uname);
            ps.setString(4, email);
            ps.setString(5, pass);
            ps.executeUpdate();

            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // RESET PASSWORD
    public static void resetPassword() {
        try (Connection con = DatabaseManager.connect()) {
            System.out.print("Enter your registered email: ");
            String email = scanner.nextLine();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM UserTable WHERE userEmail = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                generateAndSendOTP(email);

                while (true) {
                    System.out.print("Enter the OTP sent to your email (or type 'resend' to get a new one): ");
                    String enteredOTP = scanner.nextLine();

                    if (enteredOTP.equalsIgnoreCase("resend")) {
                        if (hasResentOTP) {
                            System.out.println("You can only resend OTP once.");
                            continue;
                        }
                        generateAndSendOTP(email);
                        hasResentOTP = true;
                        continue;
                    }

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - otpGenerationTime > OTP_VALID_DURATION) {
                        System.out.println("OTP expired. Please restart the reset process.");
                        return;
                    }

                    if (enteredOTP.equals(generatedOTP)) {
                        System.out.print("Enter new password: ");
                        String newPassword = scanner.nextLine();

                        PreparedStatement updatePs = con.prepareStatement(
                            "UPDATE UserTable SET userPassword = ? WHERE userEmail = ?"
                        );
                        updatePs.setString(1, newPassword);
                        updatePs.setString(2, email);
                        updatePs.executeUpdate();
                        System.out.println("Password reset successful.");
                        return;
                    } else {
                        attemptsRemaining--;
                        if (attemptsRemaining > 0) {
                            System.out.println("Invalid OTP. Attempts remaining: " + attemptsRemaining);
                        } else {
                            System.out.println("Too many failed attempts. Password reset cancelled.");
                            return;
                        }
                    }
                }
            } else {
                System.out.println("Email not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // MAIN PROGRAM
    public static void main(String[] args) {
        System.out.println("1. Register");
        System.out.println("2. Reset Password");
        System.out.print("Choose option: ");
        int choice = Integer.parseInt(scanner.nextLine());

        if (choice == 1) {
            registerUser();
        } else if (choice == 2) {
            resetPassword();
        } else {
            System.out.println("Invalid choice.");
        }
    }
}
