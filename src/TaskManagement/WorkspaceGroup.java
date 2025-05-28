package TaskManagement;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class WorkspaceGroup {
    private int id;
    private String name;

    public WorkspaceGroup (int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    private static final String FROM = "quirxg8@gmail.com";
    private static final String PASSWORD = "vnks zpzg decz gyzx";
    
    public static boolean sendInvitationEmail(String recipientEmail, String workspaceName, String inviterName) {
        // Email properties
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        // Create session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });
        
        try {
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("You've been invited to join a workspace on Quirx");
            
            String htmlContent = "<html><body>" +
            		"<h2>Workspace Invitation</h2>" +
            		"<p>Hello,</p>" +
            		"<p>You've been added to the workspace <strong>" + workspaceName + 
            		"</strong> on Quirx by <strong>" + inviterName + "</strong>.</p>" +
            		"<p>Please log in to your Quirx account to start collaborating.</p>" +
            		"<br/><p>Best regards,<br/>The Quirx Team</p>" +
            		"</body></html>";
            
            message.setContent(htmlContent, "text/html");
            
            // Send message
            Transport.send(message);
            System.out.println("✅ Email sent successfully to " + recipientEmail);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email to " + recipientEmail);
            e.printStackTrace();
            return false;
        }
    }
    
}

