package application;

import TaskManagement.Authentication;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
	
	// FXML UI components (Login & Forgot Password)
    
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
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
    /**
     * Initializes the controller after the FXML is loaded.
     *
     * @param location  The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // delay focus so TextField doesn't auto-focus and hide prompt text
        Platform.runLater(() -> rightPane.requestFocus());
        
        // bind text of visible and hidden fields
        showPasswordFieldLI.textProperty().bindBidirectional(passwordFieldLI.textProperty());
        showNewPasswordFP.textProperty().bindBidirectional(newPasswordFP.textProperty());
        showConfirmPasswordFP.textProperty().bindBidirectional(confirmPasswordFP.textProperty());
        
        // start with password hidden (PasswordField shown)
        showPasswordFieldLI.setVisible(false);
        showPasswordFieldLI.setManaged(false);
        showNewPasswordFP.setVisible(false);
        showNewPasswordFP.setManaged(false);
        showConfirmPasswordFP.setVisible(false);
        showConfirmPasswordFP.setManaged(false);
        
        // initialization for every TextField
        setupCodeField(code1, code2, null);    // First field has no previous
        setupCodeField(code2, code3, code1);
        setupCodeField(code3, code4, code2);
        setupCodeField(code4, code5, code3);
        setupCodeField(code5, code6, code4);
        setupCodeField(code6, null, code5);    // Last field has no next
        
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
            togglePasswordButtonLI.setText("Hide"); // update icon or text
        } else {
            passwordFieldLI.setVisible(true);
            passwordFieldLI.setManaged(true);
            showPasswordFieldLI.setVisible(false);
            showPasswordFieldLI.setManaged(false);
            passwordFieldLI.requestFocus();
            passwordFieldLI.positionCaret(passwordFieldLI.getText().length());
            togglePasswordButtonLI.setText("Show"); // update icon or text
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
        // center text and set font size
        current.setAlignment(Pos.CENTER);
        current.setFont(Font.font(18));

        // limit input length to 1 char, auto-move forward on input
        current.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 1) {
                current.setText(newVal.substring(0, 1));
            }
            if (!newVal.isEmpty() && next != null) {
                next.requestFocus();
                next.positionCaret(0);
            }
        });

        // handle backspace key press
        current.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                // only jump back if caret is at start AND current field is empty
                if (current.getCaretPosition() == 0 && (current.getText() == null || current.getText().isEmpty()) && previous != null) {
                    previous.requestFocus();
                    // put caret at the end of previous field text so user can delete
                    if (previous.getText() != null) {
                        previous.positionCaret(previous.getText().length());
                    }
                    event.consume(); // prevent backspace from deleting previous text immediately
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
    void handleForgotPassword(MouseEvent event) {
		mainAnchorPane.setVisible(true);
	    mainAnchorPane.setManaged(true);
		showPage(pageEmail);
	}
	
	/**
     * Sends an OTP to the provided email after validation.
     *
     * @param event The action event.
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
		usernameField.clear(); // clear before leaving
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
		usernameField.clear(); // clear before leaving
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
            showAlert(AlertType.ERROR, "Error", "Username and Password are required.");
            return;
        }

        try {
            boolean isAuthenticated = authService.loginUserByUsername(username, password);
            if (isAuthenticated) {
                showAlert(AlertType.INFORMATION, "Success", "Login successful!");
                loadMenuScene(event.getSource());
            } else {
                showAlert(AlertType.ERROR, "Error", "Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred. Please try again.");
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
        alert.showAndWait();
    }
    
    /**
     * Loads the main menu scene after successful login.
     *
     * @param source The source object (UI Node).
     */
    private void loadMenuScene(Object source) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Menu.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) source).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	    
}