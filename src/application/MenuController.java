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

/**
 * Controller for the main menu of the application.
 * Handles navigation between different views (Dashboard, My Tasks, Workspace),
 * workspace creation/deletion, and logout logic.
 */
public class MenuController implements Initializable {
    
	/**
	 * Default constructor for MenuController.
	 * Used by JavaFX for controller initialization.
	 */
	public MenuController() {
	    // Default constructor
	}
	
	/** Root anchor pane. */
	@FXML
    private AnchorPane anchorPaneId;
    
	/** Pane used to display loaded views. */
	@FXML
    private Pane windowPane;
    
	/** Button to navigate to Dashboard/Profile view. */
	@FXML
    private Button dashboardButton;
    
	/** Button to navigate to My Task view. */
	@FXML
    private Button myTaskButton;
    
	/** Button to navigate to Workspace view. */
	@FXML
    private Button workspaceButton;
    
	/** Button to log out from the application. */
	@FXML
    private Button logOutButton;
    
	/** Button to add a new workspace dynamically. */
	@FXML
    private Button addWorkspaceButton;
    
	/** VBox that holds the menu buttons and dynamically added workspaces. */
	@FXML
    private VBox menuVBox;
    
	/**
     * Initializes the controller.
     * Removes focus from input elements on launch.
     *
     * @param location  location used to resolve relative paths
     * @param resources resources used to localize root object
     */
	@Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> anchorPaneId.requestFocus());
    }
    
	/**
     * Handles click on Dashboard button.
     * Loads the "MyProfile.fxml" view.
     *
     * @param event ActionEvent triggered by the button
     */
	@FXML
    private void dashboardButton(ActionEvent event) {
        loadView("MyProfile.fxml");
    }
    
	/**
     * Handles click on My Task button.
     * Loads the "MyTask.fxml" view.
     *
     * @param event ActionEvent triggered by the button
     */
	@FXML
    private void myTaskButton(ActionEvent event) {
        loadView("MyTask.fxml");
    }
    
	/**
     * Handles click on Workspace button.
     * Loads the "Workspace.fxml" view.
     *
     * @param event ActionEvent triggered by the button
     */
	@FXML
    private void workspaceButton(ActionEvent event) {
        loadView("Workspace.fxml");
    }
    
	/**
     * Handles click on Log Out button.
     * Shows confirmation dialog and logs out if confirmed.
     *
     * @param event ActionEvent triggered by the button
     */
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
    
	/**
     * Handles click on Add Workspace button.
     * Prompts user for workspace name and creates new workspace button dynamically.
     *
     * @param event ActionEvent triggered by the button
     */
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
    
	/**
     * Loads the specified FXML view into the window pane.
     *
     * @param fxmlFile the FXML file name to load
     */
	private void loadView(String fxmlFile) {
        try {
            Pane view = FXMLLoader.load(getClass().getResource(fxmlFile));
            windowPane.getChildren().clear();
            windowPane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	/**
     * Dynamically creates a workspace button with delete functionality.
     *
     * @param workspaceName the name of the new workspace
     */
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
        newWorkspaceBtn.setPrefWidth(234.0); // Reduced width to fit delete button
        newWorkspaceBtn.setFont(Font.font("Arial", 12));
        newWorkspaceBtn.setPadding(new Insets(0, 0, 0, 40));
        
        // Delete button (small "×" button)
        Button deleteButton = new Button("🗑");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setPrefWidth(40.0);
        deleteButton.setPrefHeight(30.0);
        deleteButton.setFont(Font.font("Arial", 16));
        deleteButton.setOnAction(e -> confirmAndDeleteWorkspace(buttonContainer, workspaceName));
        
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
    
	/**
     * Confirms and deletes a workspace from the menu.
     *
     * @param buttonContainer the HBox container for the workspace
     * @param workspaceName   the name of the workspace to delete
     */
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
            // Here you could also add code to delete from database if needed
        }
    }
}