package application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
        Platform.runLater(() -> anchorPaneId.requestFocus());
    }
    
    @FXML
    private void dashboardButton(ActionEvent event) {
        loadView("MyProfile.fxml");
    }
    
    @FXML
    private void myTaskButton(ActionEvent event) {
        loadView("MyTask.fxml");
    }
    
    @FXML
    private void workspaceButton(ActionEvent event) {
        loadView("Workspace.fxml");
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
    
    private void loadView(String fxmlFile) {
        try {
            Pane view = FXMLLoader.load(getClass().getResource(fxmlFile));
            windowPane.getChildren().clear();
            windowPane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createWorkspaceButton(String workspaceName) {
        // Create a container for the workspace button and delete button
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.setSpacing(5);
        
        // Workspace button
        Button newWorkspaceBtn = new Button(workspaceName);
        newWorkspaceBtn.getStyleClass().add("menu-button");
        newWorkspaceBtn.setAlignment(Pos.BASELINE_LEFT);
        newWorkspaceBtn.setPrefHeight(30.0);
        newWorkspaceBtn.setPrefWidth(250.0); // Reduced width to fit delete button
        newWorkspaceBtn.setFont(Font.font("Arial", 12));
        newWorkspaceBtn.setPadding(new Insets(0, 0, 0, 40));
        
        // Delete button (small "×" button)
        Button deleteButton = new Button("×");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setPrefSize(20, 20);
        deleteButton.setOnAction(e -> confirmAndDeleteWorkspace(buttonContainer, workspaceName));
        
        // Add context menu for alternative deletion method
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> confirmAndDeleteWorkspace(buttonContainer, workspaceName));
        contextMenu.getItems().add(deleteItem);
        newWorkspaceBtn.setContextMenu(contextMenu);
        
        // Set action to load workspace
        newWorkspaceBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Workspace.fxml"));
                Pane view = loader.load();
                
                WorkspaceController controller = loader.getController();
                controller.setWorkspaceName(workspaceName);
                
                windowPane.getChildren().clear();
                windowPane.getChildren().add(view);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Add the container to the VBox
        buttonContainer.getChildren().addAll(newWorkspaceBtn, deleteButton);
        menuVBox.getChildren().add(menuVBox.getChildren().size() - 1, buttonContainer);
    }
    
    private void confirmAndDeleteWorkspace(HBox buttonContainer, String workspaceName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Workspace");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete '" + workspaceName + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            menuVBox.getChildren().remove(buttonContainer);
            // Here you could also add code to delete from database if needed
            System.out.println("Deleted workspace: " + workspaceName);
        }
    }
}