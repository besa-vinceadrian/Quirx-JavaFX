
package application;

import TaskManagement.Authentication;
import TaskManagement.WorkspaceInfo;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;


/**
 * Controller class for handling login and password recovery in the application.
 * Manages UI events such as logging in, toggling password visibility, verifying OTP,
 * and resetting the password. 
 * 
 * This class interacts with the {@link TaskManagement.Authentication} service.
 */
public class LogIn implements Initializable {
    
	/**
     * Default constructor for the LogIn controller.
     * Initializes the controller used for handling login and password recovery.
     */
    public LogIn() {
        // Default constructor
    }
    
	
    /** The pane for displaying the login area. */
	@FXML
    private AnchorPane rightPane; 
	
	/** The main container pane for Forgot Password pages. */
	@FXML
    private AnchorPane mainAnchorPane;
	
	/** Password field used in login. */
	@FXML
    private PasswordField passwordFieldLI;
	
	/** Text field used to show password in plain text. */
	@FXML
    private TextField showPasswordFieldLI;
	
	/** Button to toggle visibility of login password. */
	@FXML
    private Button togglePasswordButtonLI;    
	
	/** Password field for new password during reset. */
	@FXML
    private PasswordField newPasswordFP;
	
	/** Text field to show new password in plain text. */
    @FXML
    private TextField showNewPasswordFP;  
    
    /** Password field for confirm password during reset. */
    @FXML
    private PasswordField confirmPasswordFP;
    
    /** Text field to show confirm password in plain text. */
    @FXML
    private TextField showConfirmPasswordFP;
    
    /** Button to toggle visibility of new password. */
    @FXML
    private Button toggleNewPasswordFP;
    
    /** Button to toggle visibility of confirm password. */
    @FXML
    private Button toggleConfirmPasswordFP;
    
    /** Pages used in Forgot Password flow. */
    @FXML
    private AnchorPane pageEmail;
    
    /** Pages used in Forgot Password flow. */
    @FXML
    private AnchorPane pageVerifyOTP;
    
    /** Pages used in Forgot Password flow. */
    @FXML
    private AnchorPane pageResetPassword;
    
    /** Button to log in. */
    @FXML
    private Button logInButton;
    
    /** Button to save password reset changes. */
    @FXML
    private Button saveChangesButton;
    
    /** OTP input fields. */
    @FXML
    private TextField code1, code2, code3, code4, code5, code6;

    /** Email field for password recovery. */
    @FXML
    private TextField emailField;
    
    /** Username field for login. */
    @FXML
    private TextField usernameField;
    
    /** Authentication service instance. */
    private Authentication authService;
    
    /** Visibility toggle state trackers. */
    private boolean isPasswordVisible = false;
    
    /** Visibility state of the New password field. */
    private boolean isNewPasswordVisible = false;
    
    /** Visibility state of the Confirm password field. */
    private boolean isConfirmPasswordVisible = false;
    
    /**
     * Initializes the controller after the FXML is loaded.
     *
     * @param location  The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> rightPane.requestFocus());
        
        showPasswordFieldLI.textProperty().bindBidirectional(passwordFieldLI.textProperty());
        showNewPasswordFP.textProperty().bindBidirectional(newPasswordFP.textProperty());
        showConfirmPasswordFP.textProperty().bindBidirectional(confirmPasswordFP.textProperty());
        
        showPasswordFieldLI.setVisible(false);
        showPasswordFieldLI.setManaged(false);
        showNewPasswordFP.setVisible(false);
        showNewPasswordFP.setManaged(false);
        showConfirmPasswordFP.setVisible(false);
        showConfirmPasswordFP.setManaged(false);
        
        setupCodeField(code1, code2, null);
        setupCodeField(code2, code3, code1);
        setupCodeField(code3, code4, code2);
        setupCodeField(code4, code5, code3);
        setupCodeField(code5, code6, code4);
        setupCodeField(code6, null, code5);
        
        mainAnchorPane.setVisible(false);
        mainAnchorPane.setManaged(false);
        
        authService = new Authentication();
    }
    
    /**
     * Toggles the visibility of the login password field.
     *
     * @param event The action event triggered.
     */
    @FXML
    void togglePasswordButtonLI(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            showPasswordFieldLI.setVisible(true);
            showPasswordFieldLI.setManaged(true);
            passwordFieldLI.setVisible(false);
            passwordFieldLI.setManaged(false);
            showPasswordFieldLI.requestFocus();
            showPasswordFieldLI.positionCaret(showPasswordFieldLI.getText().length());
            togglePasswordButtonLI.setText("Hide");
        } else {
            passwordFieldLI.setVisible(true);
            passwordFieldLI.setManaged(true);
            showPasswordFieldLI.setVisible(false);
            showPasswordFieldLI.setManaged(false);
            passwordFieldLI.requestFocus();
            passwordFieldLI.positionCaret(passwordFieldLI.getText().length());
            togglePasswordButtonLI.setText("Show");
        }
    }
    
    /**
     * Toggles visibility of the new password field in reset form.
     *
     * @param event The action event.
     */
    @FXML
    void toggleNewPasswordFP(ActionEvent event) {
        isNewPasswordVisible = !isNewPasswordVisible;

        if (isNewPasswordVisible) {
            showNewPasswordFP.setVisible(true);
            showNewPasswordFP.setManaged(true);
            newPasswordFP.setVisible(false);
            newPasswordFP.setManaged(false);
            showNewPasswordFP.requestFocus();
            showNewPasswordFP.positionCaret(showNewPasswordFP.getText().length());
            toggleNewPasswordFP.setText("Hide");
        } else {
            newPasswordFP.setVisible(true);
            newPasswordFP.setManaged(true);
            showNewPasswordFP.setVisible(false);
            showNewPasswordFP.setManaged(false);
            newPasswordFP.requestFocus();
            newPasswordFP.positionCaret(newPasswordFP.getText().length());
            toggleNewPasswordFP.setText("Show");
        }
    }
    
    /**
     * Toggles visibility of the confirm password field in reset form.
     *
     * @param event The action event.
     */
    @FXML
    void toggleConfirmPasswordFP(ActionEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            showConfirmPasswordFP.setVisible(true);
            showConfirmPasswordFP.setManaged(true);
            confirmPasswordFP.setVisible(false);
            confirmPasswordFP.setManaged(false);
            showConfirmPasswordFP.requestFocus();
            showConfirmPasswordFP.positionCaret(showConfirmPasswordFP.getText().length());
            toggleConfirmPasswordFP.setText("Hide");
        } else {
            confirmPasswordFP.setVisible(true);
            confirmPasswordFP.setManaged(true);
            showConfirmPasswordFP.setVisible(false);
            showConfirmPasswordFP.setManaged(false);
            confirmPasswordFP.requestFocus();
            confirmPasswordFP.positionCaret(confirmPasswordFP.getText().length());
            toggleConfirmPasswordFP.setText("Show");
        }
    }

    /**
     * Sets up OTP input fields to allow 1-character input and auto-navigation.
     *
     * @param current  Current field.
     * @param next     Next field to move to.
     * @param previous Previous field to move to on backspace.
     */  
    private void setupCodeField(TextField current, TextField next, TextField previous) {
        current.setAlignment(Pos.CENTER);
        current.setFont(Font.font(18));

        current.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 1) {
                current.setText(newVal.substring(0, 1));
            }
            if (!newVal.isEmpty() && next != null) {
                next.requestFocus();
                next.positionCaret(0);
            }
        });

        current.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                if (current.getCaretPosition() == 0 && (current.getText() == null || current.getText().isEmpty()) && previous != null) {
                    previous.requestFocus();
                    if (previous.getText() != null) {
                        previous.positionCaret(previous.getText().length());
                    }
                    event.consume();
                }
            }
        });
    }
    
    /**
     * Displays only the specified page and hides others.
     *
     * @param pageToShow The AnchorPane to be displayed.
     */
    private void showPage(AnchorPane pageToShow) {
        pageEmail.setVisible(false);
        pageVerifyOTP.setVisible(false);
        pageResetPassword.setVisible(false);

        pageEmail.setManaged(false);
        pageVerifyOTP.setManaged(false);
        pageResetPassword.setManaged(false);
        
        pageToShow.setVisible(true);
        pageToShow.setManaged(true);
    }    
    
    /**
     * Handles the Forgot Password link click event.
     *
     * @param event MouseEvent trigger.
     */
	@FXML
    /**
     * handleForgotPassword method.
     *
     * @param event
     */
    void handleForgotPassword(MouseEvent event) {
        mainAnchorPane.setVisible(true);
        mainAnchorPane.setManaged(true);
        showPage(pageEmail);
    }
    
	/**
	 * Handles sending the verification code when triggered.
	 *
	 * @param event the action event triggered by the user
	 * @throws SQLException if a database access error occurs
	 */
	@FXML
    void handleSendCode(ActionEvent event) throws SQLException {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Email address field cannot be empty.");
            return;
        }
        if (!authService.isValidEmail(email)) {
            showAlert(AlertType.ERROR, "Error", "Invalid email address format.");
            return;
        }
        if (!authService.isEmailTaken(email)) {
            showAlert(AlertType.ERROR, "Error", "The email is not registered.");
            return;
        }
        
        authService.generateAndSendOTP(email);
        showPage(pageVerifyOTP);
    }
    
	/**
     * Verifies the user-entered OTP code.
     *
     * @param event The action event.
     */
	@FXML
    void handleVerifyOTP(ActionEvent event) {
        String enteredOTP = code1.getText() + code2.getText() + code3.getText() + 
                code4.getText() + code5.getText() + code6.getText();
        
        if(enteredOTP.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "OTP cannot be empty. Please enter the OTP.");
            return;
        }
        if (!authService.verifyOTP(enteredOTP)) {
            showAlert(AlertType.ERROR, "Error", "Invalid OTP. Please try again.");
            return;
        }
        showPage(pageResetPassword);
    }
    
	/**
     * Resends the OTP to the provided email address if allowed.
     *
     * @param event Mouse event.
     */
	@FXML
    public void handleResendOTP(MouseEvent event) {
        try {
            if (!authService.canResendOTP()) {
                showAlert(AlertType.ERROR, "Error", "You must wait 1 minute before resending the OTP.");
                return;
            }
            
            String email = emailField.getText().trim();

            if (email.isEmpty()) {
                showAlert(AlertType.ERROR, "Error", "Email address field cannot be empty.");
                return;
            }
            if (!authService.isValidEmail(email)) {
                showAlert(AlertType.ERROR, "Error", "Invalid email address format.");
                return;
            }
            if (!authService.isEmailTaken(email)) {
                showAlert(AlertType.ERROR, "Error", "The email is not registered.");
                return;
            }

            authService.generateAndSendOTP(email);
            showAlert(AlertType.INFORMATION, "Success", "A new OTP has been sent to your email.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred while resending the OTP. Please try again.");
        }
    }

	/**
     * Closes the Forgot Password view and resets focus.
     *
     * @param event MouseEvent trigger.
     */
	@FXML
    void handleReturnClick(MouseEvent event) {
        usernameField.clear();
        mainAnchorPane.setVisible(false);
        mainAnchorPane.setManaged(false); 
        rightPane.requestFocus();
    }
    
	/**
     * Cancels the Forgot Password process and returns to login screen.
     *
     * @param event Action event.
     */
	@FXML
    private void handleCancel(ActionEvent event) {
        usernameField.clear();
        mainAnchorPane.setVisible(false);
        mainAnchorPane.setManaged(false);
        rightPane.requestFocus();
    }
    
	/**
     * Switches to the Sign Up page.
     *
     * @param event Mouse event.
     */
	@FXML
    void handleSignUpClick(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("SignUp.fxml"));
            Stage stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
    
	/**
     * Authenticates user login credentials.
     *
     * @param event Action event.
     */
	@FXML
	void handleLogInClick(ActionEvent event) {
	    String username = usernameField.getText().trim();
	    String password = passwordFieldLI.getText().trim();

	    if (username.isEmpty() || password.isEmpty()) {
	        showAlert(Alert.AlertType.ERROR, "Error", "Username and Password are required.");
	        return;
	    }

	    try {
	        // Authenticate user by username and password
	        boolean isAuthenticated = authService.loginUserByUsername(username, password);
	        if (isAuthenticated) {
	            // Get user ID by username
	            int userId = authService.getUserIdByUsername(username);
	            if (userId != -1) {
	                // Get or create workspace for this user using your authService method
	                WorkspaceInfo workspaceInfo = authService.getOrCreateWorkspaceForUser(username);

	                if (workspaceInfo == null || workspaceInfo.getWorkspaceId() == -1) {
	                    showAlert(Alert.AlertType.ERROR, "Error", "Workspace info not found or could not be created for user.");
	                    return;
	                }

	                showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");

	                // Get the Stage from the event source (button, etc)
	                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

	                // Load your next scene, passing user and workspace info
	                loadMenuScene(stage, userId, username, workspaceInfo.getWorkspaceId(), workspaceInfo.getWorkspaceName());

	            } else {
	                showAlert(Alert.AlertType.ERROR, "Error", "User ID not found.");
	            }
	        } else {
	            showAlert(Alert.AlertType.ERROR, "Error", "Incorrect username or password.");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred. Please try again.");
	    }
	}

    
	/**
     * Handles saving a new password after reset.
     *
     * @param event Action event.
     */
	@FXML
    void handleSaveChangesClick(ActionEvent event) {
        String newPassword = newPasswordFP.getText().trim();
        String confirmPassword = confirmPasswordFP.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Both password fields are required.");
            return;
        }
        if (newPassword.length() < 8 || confirmPassword.length() < 8) {
        	showAlert(AlertType.ERROR, "Error", "Password must be at least 8 characters long and above.");
			return;
		}
        if (!newPassword.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        try {
            String email = emailField.getText().trim();
            if (authService.resetPassword(email, newPassword)) {
                showAlert(AlertType.INFORMATION, "Success", "Password reset successfully!");
                handleReturnClick(null);
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to reset password. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred. Please try again.");
        }
    }
    
	/**
     * Displays an alert dialog.
     *
     * @param alertType Alert type (INFO, ERROR, etc.)
     * @param title     Alert title.
     * @param message   Alert message.
     */
	private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        
        // Add icon to alert window
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
        } catch (Exception e) {
            // If icon loading fails, continue without icon
            System.err.println("Could not load alert icon: " + e.getMessage());
        }
        
        alert.showAndWait();
    }
    
	/**
	 * Loads the main menu scene after successful login.
	 *
	 * @param source The source object (UI Node).
	 * @param userId The ID of the logged-in user.
	 * @param username The username of the logged-in user.
	 */
	public void loadMenuScene(Stage stage, int userId, String username, int workspaceId, String workspaceName) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
	        Parent root = loader.load();

	        // Get the controller of Menu.fxml
	        MenuController menuController = loader.getController();
	        menuController.initData(userId, username, workspaceId, workspaceName);

	        // Set scene and show
	        stage.setScene(new Scene(root));
	        stage.show();

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}


}