package application;

import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.Stage;

public class MenuController implements Initializable {
	
	@FXML
	private AnchorPane anchorPaneId;
	
	@FXML
	private Pane windowPane;
	
	@FXML
	private Button dashboardButton;
	
	@FXML
	private Button myTaskButton;
	
	@FXML
	private Button workspaceButton;
	
	@FXML
	private Button logOutButton;
	
	@Override
    public void initialize(URL location, ResourceBundle resources) {
        // Delay focus so TextField doesn't auto-focus and hide prompt text
        Platform.runLater(() -> anchorPaneId.requestFocus());
        
    }
	
	@FXML
	private void dashboardButton(ActionEvent event) {
	    try {
	        Pane view = FXMLLoader.load(getClass().getResource("MyProfile.fxml"));
	        windowPane.getChildren().clear();      
	        windowPane.getChildren().add(view);    
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@FXML
	private void myTaskButton(ActionEvent event) {
	    try {
	        Pane view = FXMLLoader.load(getClass().getResource("MyTask.fxml"));
	        windowPane.getChildren().clear();      
	        windowPane.getChildren().add(view);   
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@FXML
	private void workspaceButton(ActionEvent event) {
	    try {
	        Pane view = FXMLLoader.load(getClass().getResource("Workspace.fxml"));
	        windowPane.getChildren().clear();    
	        windowPane.getChildren().add(view);  
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@FXML
	private void logOutButton(ActionEvent event) {
		try {
	        BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("LogIn.fxml"));
	        Scene scene = new Scene(root, 1024, 700);
	        scene.getStylesheets().add(getClass().getResource("LogIn.css").toExternalForm());

	        Stage currentStage = (Stage) logOutButton.getScene().getWindow();
	        currentStage.setScene(scene);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
}