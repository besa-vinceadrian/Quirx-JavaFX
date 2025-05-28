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
import javafx.scene.image.Image;

public class MenuController implements Initializable {
    
    private int userId;
    
    @FXML private AnchorPane anchorPaneId;
    @FXML private Pane windowPane;
    @FXML private Button dashboardButton;
    @FXML private Button myTaskButton;
    @FXML private Button workspaceButton;
    @FXML private Button logOutButton;
    @FXML private Button addWorkspaceButton;
    @FXML private VBox menuVBox;
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> anchorPaneId.requestFocus());
    }
    
    @FXML
    private void dashboardButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MyProfile.fxml"));
            Pane view = loader.load();
            
            MyProfileController controller = loader.getController();
            controller.setUserId(userId);
            controller.loadUserData();
            
            windowPane.getChildren().clear();
            windowPane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    
    @FXML
    private void myTaskButton(ActionEvent event) {
        loadView("MyTask.fxml");
    }
    
    @FXML
    private void workspaceButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Workspace.fxml"));
            Pane view = loader.load();

            WorkspaceController controller = loader.getController();
            controller.setUsername(username);          // Pass username

            windowPane.getChildren().clear();
            windowPane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    @FXML
    private void logOutButton(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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
    
    @FXML
    private void handleAddWorkspace(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("New Workspace");
        dialog.setTitle("New Workspace");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter workspace name:");
        
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
        
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
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.setSpacing(5);
        
        Button newWorkspaceBtn = new Button(workspaceName);
        newWorkspaceBtn.getStyleClass().add("menu-button");
        newWorkspaceBtn.setAlignment(Pos.BASELINE_LEFT);
        newWorkspaceBtn.setPrefHeight(30.0);
        newWorkspaceBtn.setPrefWidth(234.0);
        newWorkspaceBtn.setFont(Font.font("Arial", 12));
        newWorkspaceBtn.setPadding(new Insets(0, 0, 0, 40));
        
        Button deleteButton = new Button("ðŸ—‘");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setPrefWidth(40.0);
        deleteButton.setPrefHeight(30.0);
        deleteButton.setFont(Font.font("Arial", 16));
        deleteButton.setOnAction(e -> confirmAndDeleteWorkspace(buttonContainer, workspaceName));
        
        newWorkspaceBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Workspace.fxml"));
                Pane view = loader.load();

                WorkspaceController controller = loader.getController();
                controller.setWorkspaceName(workspaceName);
                controller.setUsername(username);          // Pass username

                windowPane.getChildren().clear();
                windowPane.getChildren().add(view);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        
        buttonContainer.getChildren().addAll(newWorkspaceBtn, deleteButton);
        menuVBox.getChildren().add(menuVBox.getChildren().size() - 1, buttonContainer);
    }
    
    private void confirmAndDeleteWorkspace(HBox buttonContainer, String workspaceName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Workspace");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete '" + workspaceName + "'?");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            menuVBox.getChildren().remove(buttonContainer);
        }
    }
}