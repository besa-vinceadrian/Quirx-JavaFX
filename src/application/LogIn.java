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

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LogIn implements Initializable {
    
    @FXML private AnchorPane rightPane; 
    @FXML private AnchorPane mainAnchorPane;
    @FXML private PasswordField passwordFieldLI;
    @FXML private TextField showPasswordFieldLI;
    @FXML private Button togglePasswordButtonLI;    
    @FXML private PasswordField newPasswordFP;
    @FXML private TextField showNewPasswordFP;  
    @FXML private PasswordField confirmPasswordFP;
    @FXML private TextField showConfirmPasswordFP;
    @FXML private Button toggleNewPasswordFP;
    @FXML private Button toggleConfirmPasswordFP;
    @FXML private AnchorPane pageEmail;
    @FXML private AnchorPane pageVerifyOTP;
    @FXML private AnchorPane pageResetPassword;
    @FXML private Button logInButton;
    @FXML private Button saveChangesButton;
    @FXML private TextField code1, code2, code3, code4, code5, code6;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    
    private Authentication authService;
    private boolean isPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
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
    
    @FXML
    void handleForgotPassword(MouseEvent event) {
        mainAnchorPane.setVisible(true);
        mainAnchorPane.setManaged(true);
        showPage(pageEmail);
    }
    
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

    @FXML
    void handleReturnClick(MouseEvent event) {
        usernameField.clear();
        mainAnchorPane.setVisible(false);
        mainAnchorPane.setManaged(false); 
        rightPane.requestFocus();
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        usernameField.clear();
        mainAnchorPane.setVisible(false);
        mainAnchorPane.setManaged(false);
        rightPane.requestFocus();
    }
    
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
                int userId = authService.getUserIdByUsername(username);
                if (userId != -1) {
                    showAlert(AlertType.INFORMATION, "Success", "Login successful!");
                    loadMenuScene(event.getSource(), userId);
                } else {
                    showAlert(AlertType.ERROR, "Error", "User ID not found.");
                }
            } else {
                showAlert(AlertType.ERROR, "Error", "Incorrect username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred. Please try again.");
        }
    }
    
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
    
    private void loadMenuScene(Object source, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
            Parent root = loader.load();
            
            MenuController menuController = loader.getController();
            menuController.setUserId(userId);
            
            Stage stage = (Stage) ((javafx.scene.Node) source).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}