package application;

import javafx.application.Platform;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;


import java.net.URL;
import java.util.ResourceBundle;


public class MyProfileController implements Initializable {

    @FXML
    private Pane centerPane; 	
    
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

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Delay focus so TextField doesn't auto-focus and hide prompt text
        Platform.runLater(() -> centerPane.requestFocus());
        
     // Bind text of visible and hidden fields
        showNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
        showConfirmNewPasswordField.textProperty().bindBidirectional(confirmNewPasswordField.textProperty());

        // Start with password hidden (PasswordField shown)
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
            togglePasswordButton.setText("Hide"); // update icon or text
        } else {
        	newPasswordField.setVisible(true);
        	newPasswordField.setManaged(true);
            showNewPasswordField.setVisible(false);
            showNewPasswordField.setManaged(false);
            newPasswordField.requestFocus();
            newPasswordField.positionCaret(newPasswordField.getText().length());
            togglePasswordButton.setText("Show"); // update icon or text
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
}