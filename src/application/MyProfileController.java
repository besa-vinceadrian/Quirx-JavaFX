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


/**
 * Controller class for handling the user's profile and password visibility toggle.
 */
public class MyProfileController implements Initializable {

	/**
     * Default constructor for the MyProfileController.
     * Required by JavaFX for FXML loading.
     */
    public MyProfileController() {
        // Default constructor
    }
	
	/** The pane that contains the profile center content. */
	@FXML
    private Pane centerPane; 	
    
	/** Password field for entering a new password (masked input). */
	@FXML
    private PasswordField newPasswordField;

	/** Text field for displaying the new password in plain text. */
	@FXML
    private TextField showNewPasswordField;

	/** Button to toggle visibility of the new password. */
	@FXML
    private Button togglePasswordButton;
    
	/** Password field for confirming the new password (masked input). */
	@FXML
    private PasswordField confirmNewPasswordField;

	/** Text field for displaying the confirmation password in plain text. */
	@FXML
    private TextField showConfirmNewPasswordField;

	/** Button to toggle visibility of the confirm password. */
	@FXML
    private Button toggleConfirmPasswordButton;

	/** Flag to track if the new password is visible. */
	private boolean isPasswordVisible = false;
	
	/** Flag to track if the confirmation password is visible. */
    private boolean isConfirmPasswordVisible = false;

    /**
     * Initializes the controller. Binds the password fields and sets initial visibility.
     *
     * @param location  the location used to resolve relative paths
     * @param resources the resources used to localize the root object
     */
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
    
    /**
     * Toggles the visibility of the new password input field.
     *
     * @param event the ActionEvent triggered by clicking the toggle button
     */
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
    
    /**
     * Toggles the visibility of the confirm password input field.
     *
     * @param event the ActionEvent triggered by clicking the toggle button
     */
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