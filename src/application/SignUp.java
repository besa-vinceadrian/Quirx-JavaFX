
package application;

import javafx.application.Platform;

import TaskManagement.Authentication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the Sign-Up screen.
 * Handles user registration, password visibility toggles, and navigation to the login screen.
 */
public class SignUp implements Initializable {

	/**
	 * Default constructor for SignUp controller.
	 */
	public SignUp() {
	    // Default constructor
	}
	
	/** The container pane for UI elements */
	@FXML
    private AnchorPane rightPane; 	
	
	/** TextField for user's first name */
    @FXML
    private TextField firstNameField;
    
    /** TextField for user's last name */
    @FXML
    private TextField lastNameField;
    
    /** TextField for username input */
    @FXML
    private TextField usernameField;
    
    /** TextField for email input */
    @FXML
    private TextField emailField;
    
    /** Hidden password input field */
    @FXML
    private PasswordField passwordFieldSU;
    
    /** Visible version of the password input */
    @FXML
    private TextField showPasswordFieldSU;
    
    /** Button to toggle password visibility */
    @FXML
    private Button togglePasswordButton;
    
    /** Hidden confirm password input field */
    @FXML
    private PasswordField confirmPasswordFieldSU;
    
    /** Visible version of the confirm password input */
    @FXML
    private TextField showConfirmPasswordFieldSU;
    
    /** Button to toggle confirm password visibility */
    @FXML
    private Button toggleConfirmPasswordButton;
    
    /** Service for user authentication and registration logic */
    private Authentication authService;
    
    /** Flag indicating if the password is visible */
    private boolean isPasswordVisible = false;
    
    /** Flag indicating if the confirm password is visible */
    private boolean isConfirmPasswordVisible = false;

    /**
     * Initializes the controller.
     * Sets up bindings between password fields and sets default visibility.
     *
     * @param location  The location used to resolve relative paths.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Delay focus so TextField doesn't auto-focus and hide prompt text
        Platform.runLater(() -> rightPane.requestFocus());
        authService = new Authentication();
        
        // Bind text of visible and hidden fields
        showPasswordFieldSU.textProperty().bindBidirectional(passwordFieldSU.textProperty());
        showConfirmPasswordFieldSU.textProperty().bindBidirectional(confirmPasswordFieldSU.textProperty());

        // Start with password hidden (PasswordField shown)
        showPasswordFieldSU.setVisible(false);
        showPasswordFieldSU.setManaged(false);
        
        showConfirmPasswordFieldSU.setVisible(false);
        showConfirmPasswordFieldSU.setManaged(false);
    } 
    
    /**
     * Handles the toggle button click to show/hide password field.
     *
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void togglePasswordButton(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            showPasswordFieldSU.setVisible(true);
            showPasswordFieldSU.setManaged(true);
            passwordFieldSU.setVisible(false);
            passwordFieldSU.setManaged(false);
            showPasswordFieldSU.requestFocus();
            showPasswordFieldSU.positionCaret(showPasswordFieldSU.getText().length());
            togglePasswordButton.setText("Hide");
        } else {
            passwordFieldSU.setVisible(true);
            passwordFieldSU.setManaged(true);
            showPasswordFieldSU.setVisible(false);
            showPasswordFieldSU.setManaged(false);
            passwordFieldSU.requestFocus();
            passwordFieldSU.positionCaret(passwordFieldSU.getText().length());
            togglePasswordButton.setText("Show");
        }
    }
    
    /**
     * Handles the toggle button click to show/hide confirm password field.
     *
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void toggleConfirmPasswordButton(ActionEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            showConfirmPasswordFieldSU.setVisible(true);
            showConfirmPasswordFieldSU.setManaged(true);
            confirmPasswordFieldSU.setVisible(false);
            confirmPasswordFieldSU.setManaged(false);
            showConfirmPasswordFieldSU.requestFocus();
            showConfirmPasswordFieldSU.positionCaret(showConfirmPasswordFieldSU.getText().length());
            toggleConfirmPasswordButton.setText("Hide");
        } else {
            confirmPasswordFieldSU.setVisible(true);
            confirmPasswordFieldSU.setManaged(true);
            showConfirmPasswordFieldSU.setVisible(false);
            showConfirmPasswordFieldSU.setManaged(false);
            confirmPasswordFieldSU.requestFocus();
            confirmPasswordFieldSU.positionCaret(confirmPasswordFieldSU.getText().length());
            toggleConfirmPasswordButton.setText("Show");
        }
    }
    
    /**
     * Handles the sign-up button click.
     * Validates user input and attempts to register a new user.
     *
     * @param event The ActionEvent triggered by the Sign-Up button.
     */
    public void handleSignUpButtonClick(ActionEvent event) {
    	String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordFieldSU.getText().trim();
        String confirmPassword = confirmPasswordFieldSU.getText().trim();
        
        if (password.length() < 8 || password.length() > 64) {
            showAlert(AlertType.ERROR, "Error", "Password must be 8 characters long and above.");
            return;
        }
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Error", "Passwords do not match.");
            return;
        }

        try {
            if (authService.isUsernameTaken(username)) {
                showAlert(AlertType.ERROR, "Error", "Username is already taken.");
                return;
            }
            if (authService.isEmailTaken(email)) {
                showAlert(AlertType.ERROR, "Error", "Email is already registered.");
                return;
            }
            if (!authService.isValidEmail(email)) {
                showAlert(AlertType.ERROR, "Error", "Invalid email format.");
                return;
            }
            
            boolean success = authService.registerUser(firstName, lastName, username, email, password);
            if (success) {
                showAlert(AlertType.INFORMATION, "Success", "Registration successful!");
                loadLoginScene(event.getSource());
            } else {
                showAlert(AlertType.ERROR, "Error", "Registration failed. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred. Please try again.");
        }
    }
    
    /**
     * Displays an alert with a given type, title, and message.
     *
     * @param alertType The type of alert (e.g., ERROR, INFORMATION).
     * @param title     The title of the alert.
     * @param message   The message displayed in the alert.
     */
    private void showAlert(AlertType alertType, String title, String message) {
    	Alert alert = new Alert(alertType);
    	alert.setTitle(title);
    	alert.setContentText(message);
    	alert.showAndWait();
    }

    /**
     * Handles the click event for navigating to the login screen.
     *
     * @param event The MouseEvent triggered by clicking a login label.
     */
    public void handleLoginClick(MouseEvent event) {
        loadLoginScene(event.getSource());
    }

    /**
     * Loads the LogIn.fxml scene and replaces the current window content.
     *
     * @param source The source object that triggered the event (used to get the current stage).
     */
    private void loadLoginScene(Object source) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("LogIn.fxml"));
            Stage stage = (Stage)((javafx.scene.Node)source).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
