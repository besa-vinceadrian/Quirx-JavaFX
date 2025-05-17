package TaskManagement;

import java.sql.*;
import java.util.*;
import java.util.Properties;
import java.io.Console;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * The {@code Authentication} class handles user registration and password reset
 * functionalities using email-based OTP verification. It connects to a SQL Server
 * database, validates user information, and sends OTPs via Gmail SMTP.
 */
public class Authentication {
    static Scanner scanner = new Scanner(System.in);
    static String generatedOTP = "";
    static long otpGenerationTime = 0;
    static long lastOTPSentTime = 0;
    static final long OTP_VALID_DURATION = 5 * 60 * 1000; 
    static final long OTP_RESEND_INTERVAL = 60 * 1000; 
    static int attemptsRemaining = 3;
    static boolean hasResentOTP = false;

    /**
     * Manages the database connection to the SQL Server.
     */
    public static class DatabaseManager {
        private static final String DB_URL = "jdbc:sqlserver://0.tcp.ap.ngrok.io:18980;databaseName=QUIRX;encrypt=true;trustServerCertificate=true";
        private static final String DB_USER = "QuirxAdmin";
        private static final String DB_PASS = "admin";

        /**
         * Connects to the SQL Server database using the provided credentials.
         *
         * @return a {@link Connection} object to the database
         * @throws SQLException if a database access error occurs
         */
        public static Connection connect() throws SQLException {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
    }

    /**
     * Sends an OTP to the specified email address using Gmail SMTP.
     *
     * @param to  recipient email address
     * @param otp the one-time password to send
     */
    public static void sendOTP(String to, String otp) {
        final String from = "quirxg8@gmail.com";
        final String password = "vnks zpzg decz gyzx"; //

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
            message.setSubject("Email Verification OTP");
            message.setText("Your OTP is: " + otp + "\n\nThis OTP is valid for 5 minutes.");

            Transport.send(message);
            lastOTPSentTime = System.currentTimeMillis(); // Track last OTP time
            System.out.println("OTP sent to " + to);
        } catch (MessagingException e) {
            System.out.println("Failed to send OTP email.");
            e.printStackTrace();
        }
    }

    /**
     * Generates a random 6-digit OTP and sends it to the specified email.
     *
     * @param email recipient email address
     */
    public static void generateAndSendOTP(String email) {
        generatedOTP = String.format("%06d", new Random().nextInt(1000000));
        otpGenerationTime = System.currentTimeMillis();
        attemptsRemaining = 3;
        sendOTP(email, generatedOTP);
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param con   active database connection
     * @param uname username to check
     * @return {@code true} if the username is taken, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean isUsernameTaken(Connection con, String uname) throws SQLException {
        String query = "SELECT userName FROM UserTable WHERE userName = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, uname);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    /**
     * Checks if an email is already registered in the database.
     *
     * @param con   active database connection
     * @param email email address to check
     * @return {@code true} if the email is taken, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public static boolean isEmailTaken(Connection con, String email) throws SQLException {
        String query = "SELECT userEmail FROM UserTable WHERE userEmail = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    /**
     * Reads a password input from the user. Attempts to hide input if supported by the console.
     *
     * @param prompt prompt to display
     * @return the entered password as a {@link String}
     */
    public static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] passwordArray = console.readPassword(prompt);
            return new String(passwordArray);
        } else {
            System.out.print(prompt + " (warning: input visible): ");
            return scanner.nextLine();
        }
    }

    /**
     * Registers a new user by collecting personal information, validating uniqueness,
     * and verifying the email using an OTP.
     */
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

            String email;
            while (true) {
                System.out.print("Email: ");
                email = scanner.nextLine();

                if (isEmailTaken(con, email)) {
                    System.out.println("Email already exists. Please use a different one.");
                } else {
                    break;
                }
            }

            // Send verification OTP
            generateAndSendOTP(email);
            hasResentOTP = false;

            while (true) {
                System.out.print("Enter the OTP sent to your email for verification (or type 'resend' to get a new one): ");
                String enteredOTP = scanner.nextLine();

                if (enteredOTP.equalsIgnoreCase("resend")) {
                    long now = System.currentTimeMillis();
                    if (now - lastOTPSentTime < OTP_RESEND_INTERVAL) {
                        System.out.println("Please wait at least 1 minute before resending the OTP.");
                        continue;
                    }
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
                    System.out.println("OTP expired. Please restart registration.");
                    return;
                }

                if (enteredOTP.equals(generatedOTP)) {
                    break; // Proceed with registration
                } else {
                    attemptsRemaining--;
                    if (attemptsRemaining > 0) {
                        System.out.println("Invalid OTP. Attempts remaining: " + attemptsRemaining);
                    } else {
                        System.out.println("Too many failed attempts. Registration cancelled.");
                        return;
                    }
                }
            }

            String pass = readPassword("Password: ");

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO UserTable (userFirstName, userLastName, userName, userEmail, userPassword) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, fname);
            ps.setString(2, lname);
            ps.setString(3, uname);
            ps.setString(4, email);
            ps.setString(5, pass);
            ps.executeUpdate();

            System.out.println("Email verified! User registered successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the user's password after validating their email and verifying OTP.
     */
    public static void resetPassword() {
        try (Connection con = DatabaseManager.connect()) {
            System.out.print("Enter your registered email: ");
            String email = scanner.nextLine();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM UserTable WHERE userEmail = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                generateAndSendOTP(email);
                hasResentOTP = false;

                while (true) {
                    System.out.print("Enter the OTP sent to your email (or type 'resend' to get a new one): ");
                    String enteredOTP = scanner.nextLine();

                    if (enteredOTP.equalsIgnoreCase("resend")) {
                        long now = System.currentTimeMillis();
                        if (now - lastOTPSentTime < OTP_RESEND_INTERVAL) {
                            System.out.println("Please wait at least 1 minute before resending the OTP.");
                            continue;
                        }
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
                        String newPassword = readPassword("Enter new password: ");

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

    /**
     * Main method to run the authentication system.
     *
     * @param args command-line arguments (not used)
     */
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
