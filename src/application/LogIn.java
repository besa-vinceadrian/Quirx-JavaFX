package application;

import javafx.application.Platform;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
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
import java.util.ResourceBundle;

public class LogIn implements Initializable {
	
    @FXML
    private AnchorPane rightPane; 
    @FXML
    private AnchorPane mainAnchorPane;
    @FXML
    private PasswordField passwordFieldLI;
    @FXML
    private TextField showPasswordFieldLI;
    @FXML
    private Button togglePasswordButtonLI;    
    @FXML
    private PasswordField newPasswordFP;
    @FXML
    private TextField showNewPasswordFP;    
    @FXML
    private PasswordField confirmPasswordFP;
    @FXML
    private TextField showConfirmPasswordFP;    
    @FXML
    private Button toggleNewPasswordFP;    
    @FXML
    private Button toggleConfirmPasswordFP; 
    @FXML
    private AnchorPane pageEmail;
    @FXML
    private AnchorPane pageVerifyOTP;
    @FXML
    private AnchorPane pageResetPassword;
    @FXML
    private Button logInButton;
    @FXML
    private Button saveChangesButton;
    
    @FXML
    private TextField code1, code2, code3, code4, code5, code6;
    
    @FXML
    private TextField emailField;
    
    private boolean isPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
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

    }
    
    // show and hide password in the main interface
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

    // set up each code input field to accept only 1 character  
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
	
	// Shows only the requested page, hides the others
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
	void handleSendCode(ActionEvent event) {
	    showPage(pageVerifyOTP);
	}
	
	@FXML
	void handleVerifyOTP(ActionEvent event) {
	    showPage(pageResetPassword);
	}
	
	@FXML
    void handleReturnClick(MouseEvent event) {
		emailField.clear(); // clear before leaving
		mainAnchorPane.setVisible(false);
	    mainAnchorPane.setManaged(false); 
	    rightPane.requestFocus();
	}
	// close the forgot password after pressing the cancel
	@FXML
	private void handleCancel(ActionEvent event) {
		emailField.clear(); // clear before leaving
	    mainAnchorPane.setVisible(false);
	    mainAnchorPane.setManaged(false);
	    rightPane.requestFocus();
	}
	

    // switch pages between sign up and login
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
    	try {
            Parent root = FXMLLoader.load(getClass().getResource("Menu.fxml"));
            Stage stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void handleSaveChangesClick(ActionEvent event) {
    	try {
            Parent root = FXMLLoader.load(getClass().getResource("LogIn.fxml"));
            Stage stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	    
}