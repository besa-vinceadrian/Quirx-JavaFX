package application;

import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import java.util.Optional;

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
	
	@FXML
    private Button addWorkspaceButton;
    
    @FXML
    private VBox menuVBox;
	
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
	
	@FXML
    private void handleAddWorkspace(ActionEvent event) {
        // Create a dialog to get the workspace name
        TextInputDialog dialog = new TextInputDialog("New Workspace");
        dialog.setTitle("New Workspace");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter workspace name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(workspaceName -> {
            if (!workspaceName.trim().isEmpty()) {
                createWorkspaceButton(workspaceName);
            }
        });
    }
    
    private void createWorkspaceButton(String workspaceName) {
        Button newWorkspaceBtn = new Button(workspaceName);
        newWorkspaceBtn.getStyleClass().add("menu-button");
        newWorkspaceBtn.setAlignment(Pos.BASELINE_LEFT);
        newWorkspaceBtn.setPrefHeight(30.0);
        newWorkspaceBtn.setPrefWidth(297.0);
        newWorkspaceBtn.setFont(Font.font("Arial", 12));
        newWorkspaceBtn.setPadding(new Insets(0, 0, 0, 40));
        
        // Set action to load workspace content
        newWorkspaceBtn.setOnAction(e -> {
            try {
                Pane view = FXMLLoader.load(getClass().getResource("Workspace.fxml"));
                windowPane.getChildren().clear();
                windowPane.getChildren().add(view);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Add the new button just before the Group in the VBox
        // The Group is the last element, so we add at size()-1
        menuVBox.getChildren().add(menuVBox.getChildren().size() - 1, newWorkspaceBtn);
    }
	
}