package application;

import javafx.application.Platform;


import TaskManagement.Authentication;
//import javafx.application.Platform;
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

public class SignUp implements Initializable {

    @FXML
    private AnchorPane rightPane; 	
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordFieldSU;
    @FXML
    private TextField showPasswordFieldSU;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private PasswordField confirmPasswordFieldSU;
    @FXML
    private TextField showConfirmPasswordFieldSU;
    @FXML
    private Button toggleConfirmPasswordButton;
    
    private Authentication authService;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

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
            togglePasswordButton.setText("Hide"); // update icon or text
        } else {
            passwordFieldSU.setVisible(true);
            passwordFieldSU.setManaged(true);
            showPasswordFieldSU.setVisible(false);
            showPasswordFieldSU.setManaged(false);
            passwordFieldSU.requestFocus();
            passwordFieldSU.positionCaret(passwordFieldSU.getText().length());
            togglePasswordButton.setText("Show"); // update icon or text
        }
    }
    
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
    
 // For the button
    public void handleSignUpButtonClick(ActionEvent event) {
    	String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordFieldSU.getText().trim();
        String confirmPassword = confirmPasswordFieldSU.getText().trim();
        
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
    
    private void showAlert(AlertType alertType, String title, String message) {
    	Alert alert = new Alert(alertType);
    	alert.setTitle(title);
    	alert.setContentText(message);
    	alert.showAndWait();
    }

    // For the label
    public void handleLoginClick(MouseEvent event) {
        loadLoginScene(event.getSource());
    }

    // Reusable method to load login.fxml
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
