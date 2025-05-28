package application;

import TaskManagement.Authentication;
import TaskManagement.MyProfile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MyProfileController implements Initializable {
    
    private int userId;
    private MyProfile userProfile;
    
    @FXML 
    private Pane centerPane;
    
    @FXML
    private Label userNameLabel;
    
    @FXML 
    private TextField firstNameField;
    
    @FXML 
    private TextField lastNameField;
    
    @FXML 
    private TextField emailField;
    
    @FXML 
    private PasswordField newPasswordField;
    
    @FXML 
    private TextField showNewPasswordField;
    
    @FXML 
    private Button togglePasswordButton;
    
    @FXML 
    private PasswordField confirmNewPasswordField;
    
    @FXML 
    private TextField showConfirmNewPasswordField;
    
    @FXML 
    private Button toggleConfirmPasswordButton;
    
    @FXML 
    private Button saveChangesButton;
    
    @FXML
    private Button deleteAccountButton;
    
    private Authentication authService = new Authentication();
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void loadUserData() {
        try {
            userProfile = MyProfile.loadUserById(userId);
            Platform.runLater(() -> {
            	userNameLabel.setText(userProfile.getUsername());
                firstNameField.setText(userProfile.getFirstName());
                lastNameField.setText(userProfile.getLastName());
                emailField.setText(userProfile.getEmail());
            });
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message
        }
    }
    
    @FXML
    void handleSaveChanges(ActionEvent event) {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmNewPasswordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (firstName.isEmpty() && lastName.isEmpty() && email.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "All fields cannot be empty.");
            return;
        }
        if (firstName.equals(userProfile.getFirstName()) &&
                lastName.equals(userProfile.getLastName()) &&
                email.equals(userProfile.getEmail()) &&
                newPassword.isEmpty()) {
                showAlert(AlertType.ERROR, "Error", "No changes were made.");
                return;
            }
        if (firstName.isEmpty()) {
        	showAlert(AlertType.ERROR, "Error", "First name cannot be empty.");
			return;
        }
        if (lastName.isEmpty()) {
			showAlert(AlertType.ERROR, "Error", "Last name cannot be empty.");
			return;
        }
        if (email.isEmpty()) {
			showAlert(AlertType.ERROR, "Error", "Email address cannot be empty.");
			return;
		}
        // Validate email format
        if (!isValidEmail(email)) {
            showAlert(AlertType.ERROR, "Error", "Please enter a valid email address.");
            return;
        }
        
        try {
            // Check if the email is already registered
            if (!email.equals(userProfile.getEmail()) && authService.isEmailTaken(email)) {
                showAlert(AlertType.ERROR, "Error", "The email address is already registered.");
                return;
            }
        if (!newPassword.isEmpty() && (newPassword.length() < 8 || newPassword.length() > 64)) {
        	showAlert(AlertType.ERROR, "Error", "Password must be 8 characters long and above.");
        	return;
        }
        if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
        	showAlert(AlertType.ERROR, "Error", "New password and confirm password do not match.");
            return;
        }
        
            userProfile.setFirstName(firstNameField.getText());
            userProfile.setLastName(lastNameField.getText());
            userProfile.setEmail(emailField.getText());
            if (!newPassword.isEmpty()) {
                userProfile.setPassword(newPassword);
            }
            userProfile.updateUser();
            showAlert(AlertType.INFORMATION, "Success", "Profile updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occured while saving changes.");
        }
    }
    
    @FXML
    private void handleDeleteAccount() {
        // Show confirmation dialog
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone. All your data will be permanently deleted.");
        
        // Set custom icon
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));

        // Customize the buttons
        ButtonType buttonTypeYes = new ButtonType("Yes, Delete");
        ButtonType buttonTypeCancel = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeCancel);

        // Show dialog and wait for response
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
            	 TextInputDialog passwordDialog = new TextInputDialog();
                 passwordDialog.setTitle("Password Confirmation");
                 passwordDialog.setHeaderText("Enter your password to confirm account deletion:");
                 passwordDialog.setContentText("Password:");
                 
                 // Set custom icon for password dialog
                 Stage passwordStage = (Stage) passwordDialog.getDialogPane().getScene().getWindow();
                 passwordStage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));

                 // Show dialog and wait for input
                 passwordDialog.showAndWait().ifPresent(password -> {
                	 try {
                    // Attempt to delete the account
                		 if (userProfile.validatePassword(password)) {
                			 if (userProfile.deleteAccount()) {
                				 // Account deleted successfully
                				 showAlert(AlertType.INFORMATION, "Success", "Your account has been deleted successfully.");
                        
                				 // Close the application or return to login screen
                				 Stage mainStage = (Stage) deleteAccountButton.getScene().getWindow();
                				 Parent root = FXMLLoader.load(getClass().getResource("SignUp.fxml"));
                				 mainStage.setScene(new Scene(root));
                			 } else {
                				 showAlert(AlertType.ERROR, "Error", "Failed to delete account. Please try again.");
                			 }
                		} else {
                			showAlert(AlertType.ERROR, "Error", "Incorrect password. Account deletion canceled.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(AlertType.ERROR, "Error", "An error occurred while deleting your account.");
                }
            });
          }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> centerPane.requestFocus());
        
        showNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
        showConfirmNewPasswordField.textProperty().bindBidirectional(confirmNewPasswordField.textProperty());

        showNewPasswordField.setVisible(false);
        showNewPasswordField.setManaged(false);
        showConfirmNewPasswordField.setVisible(false);
        showConfirmNewPasswordField.setManaged(false);
    }
    
    @FXML
    void togglePasswordButton(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            showNewPasswordField.setVisible(true);
            showNewPasswordField.setManaged(true);
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            showNewPasswordField.requestFocus();
            showNewPasswordField.positionCaret(showNewPasswordField.getText().length());
            togglePasswordButton.setText("Hide");
        } else {
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            showNewPasswordField.setVisible(false);
            showNewPasswordField.setManaged(false);
            newPasswordField.requestFocus();
            newPasswordField.positionCaret(newPasswordField.getText().length());
            togglePasswordButton.setText("Show");
        }
    }
    
    @FXML
    void toggleConfirmPasswordButton(ActionEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            showConfirmNewPasswordField.setVisible(true);
            showConfirmNewPasswordField.setManaged(true);
            confirmNewPasswordField.setVisible(false);
            confirmNewPasswordField.setManaged(false);
            showConfirmNewPasswordField.requestFocus();
            showConfirmNewPasswordField.positionCaret(showConfirmNewPasswordField.getText().length());
            toggleConfirmPasswordButton.setText("Hide");
        } else {
            confirmNewPasswordField.setVisible(true);
            confirmNewPasswordField.setManaged(true);
            showConfirmNewPasswordField.setVisible(false);
            showConfirmNewPasswordField.setManaged(false);
            confirmNewPasswordField.requestFocus();
            confirmNewPasswordField.positionCaret(confirmNewPasswordField.getText().length());
            toggleConfirmPasswordButton.setText("Show");
        }
    }
    
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Set custom icon
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
        
        alert.showAndWait();
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}