package application;

import TaskManagement.MyProfile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
    
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void loadUserData() {
        try {
            userProfile = MyProfile.loadUserById(userId);
            Platform.runLater(() -> {
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
        
        if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
        	System.out.println("Password do not match.");
            return;
        }
        
        try {
            userProfile.setFirstName(firstNameField.getText());
            userProfile.setLastName(lastNameField.getText());
            if (!newPassword.isEmpty()) {
                userProfile.setPassword(newPassword);
                showAlert(AlertType.INFORMATION, "Success", "Password updated successfully.");
            }
            else {
            	showAlert(AlertType.INFORMATION, "Info", "Personal information updated successfully.");
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

                 // Show dialog and wait for input
                 passwordDialog.showAndWait().ifPresent(password -> {
                	 try {
                    // Attempt to delete the account
                		 if (userProfile.validatePassword(password)) {
                			 if (userProfile.deleteAccount()) {
                				 // Account deleted successfully
                				 showAlert(AlertType.INFORMATION, "Success", "Your account has been deleted successfully.");
                        
                				 // Close the application or return to login screen
                				 Stage stage = (Stage) deleteAccountButton.getScene().getWindow();
                				 Parent root = FXMLLoader.load(getClass().getResource("SignUp.fxml"));
                				 stage.setScene(new Scene(root));
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
        alert.showAndWait();
    }
}