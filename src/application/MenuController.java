package application;

import javafx.application.Platform;
import TaskManagement.TaskDAO;
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
import java.util.ArrayList;
import java.util.List;
import TaskManagement.WorkspaceGroup;

public class MenuController implements Initializable {
	
	private int userId;
    
    @FXML
    private AnchorPane anchorPaneId;
    
    @FXML
    private Pane windowPane;
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button myTaskButton;
    
    @FXML
    private Button personalWorkspaceButton;
    
    @FXML
    private Button groupWorkspaceButton;
    
    @FXML
    private Button logOutButton;
    
    @FXML
    private Button addWorkspaceButton;
    
    @FXML
    private VBox menuVBox;
    
    @FXML
    private Font originalFont;
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    // Container for group workspaces with scroll support
    private ScrollPane groupWorkspaceScrollPane;
    private VBox groupWorkspaceContainer;
    private boolean isGroupWorkspaceExpanded = false;
    private List<HBox> createdWorkspaces = new ArrayList<>();
    
    private int workspaceId;
    private String workspaceName;

    // Add a method to receive these parameters from the previous scene (the login scene)
    public void initData(int userId, String username, int workspaceId, String workspaceName) {
        this.userId = userId;
        this.username = username;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;

        // Optionally, initialize UI elements here based on workspaceName, etc.
        System.out.println("User logged in: " + username);
        System.out.println("Workspace: " + workspaceName + " (ID: " + workspaceId + ")");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> anchorPaneId.requestFocus());
        originalFont = groupWorkspaceButton.getFont(); // Store original font
        initializeGroupWorkspaceContainer();
    }
    
    private void initializeGroupWorkspaceContainer() {
        // Create container for group workspaces
        groupWorkspaceContainer = new VBox();
        groupWorkspaceContainer.setSpacing(2);
        groupWorkspaceContainer.setPadding(new Insets(0, 0, 0, 0));
        
        // Create ScrollPane to wrap the VBox container
        groupWorkspaceScrollPane = new ScrollPane();
        groupWorkspaceScrollPane.setContent(groupWorkspaceContainer);
        groupWorkspaceScrollPane.setVisible(false);
        groupWorkspaceScrollPane.setManaged(false);
        
        // Configure ScrollPane properties
        groupWorkspaceScrollPane.setFitToWidth(true);
        groupWorkspaceScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide horizontal scrollbar
        groupWorkspaceScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Show vertical scrollbar when needed
        groupWorkspaceScrollPane.setPrefViewportHeight(400); // Maximum height before scrolling kicks in
        groupWorkspaceScrollPane.setMaxHeight(400); // Prevent the scroll pane from expanding beyond this height
        groupWorkspaceScrollPane.getStyleClass().add("edge-to-edge"); // Remove border/padding if needed
        
        // Set style to make scrollbar less intrusive
        groupWorkspaceScrollPane.setStyle(
        		 "-fx-background-color: transparent;" +
	            "-fx-background: transparent;" +
	            "-fx-border-color: transparent;" +
	            "-fx-focus-color: transparent;" +
	            "-fx-faint-focus-color: transparent;" +
	            "-fx-padding: 0;" +
	            "-fx-background-insets: 0;" +
	            "-fx-border-insets: 0;"
        		);
        
     // Add CSS class for scrollbar styling
        groupWorkspaceScrollPane.getStyleClass().add("custom-scrollbar");
        
        // Find the index of group workspace button and add scroll pane after it
        int groupWorkspaceIndex = -1;
        for (int i = 0; i < menuVBox.getChildren().size(); i++) {
            if (menuVBox.getChildren().get(i) == groupWorkspaceButton) {
                groupWorkspaceIndex = i;
                break;
            }
        }
        
        if (groupWorkspaceIndex != -1) {
            menuVBox.getChildren().add(groupWorkspaceIndex + 1, groupWorkspaceScrollPane);
        }
        
        // Update group workspace button text to show arrow
        updateGroupWorkspaceButtonText();
    }
    
    private void updateGroupWorkspaceButtonText() {
        String arrow = isGroupWorkspaceExpanded ? "▼" : "▶";
        String baseText = "Group Workspace";
        groupWorkspaceButton.setText(arrow + " " + baseText);
        
     // Restore the original font
        if (originalFont != null) {
            groupWorkspaceButton.setFont(originalFont);
        }
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
    	try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MyTask.fxml"));
            Pane view = loader.load();

            MyTaskController controller = loader.getController();
            controller.setUsername(username); // Pass username here

            windowPane.getChildren().clear();
            windowPane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void personalWorkspaceButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PersonalWorkspace.fxml"));
            Pane view = loader.load();

            PersonalWorkspaceController controller = loader.getController();
            controller.setUsername(username);      
            controller.setWorkspaceData(workspaceId, workspaceName);
            
            // Set the workspace name in the UI
            controller.setWorkspaceName(workspaceName);
            
            windowPane.getChildren().clear();
            windowPane.getChildren().add(view);
            
            // The alert will automatically show after tasks are loaded due to the 
            // Platform.runLater call in the modified loadTasks method
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void groupWorkspaceButton(ActionEvent event) {
        toggleGroupWorkspaceDropdown();
    }
    
    private void toggleGroupWorkspaceDropdown() {
        isGroupWorkspaceExpanded = !isGroupWorkspaceExpanded;
        groupWorkspaceScrollPane.setVisible(isGroupWorkspaceExpanded);
        groupWorkspaceScrollPane.setManaged(isGroupWorkspaceExpanded);
        updateGroupWorkspaceButtonText();
        refreshWorkspaceList();

        if (isGroupWorkspaceExpanded) {
            groupWorkspaceContainer.getChildren().clear();
            createdWorkspaces.clear(); // Clear the tracking list

            List<String> workspaces = TaskDAO.getUserGroupWorkspaces(username);
            for (String workspaceName : workspaces) {
                // Use the existing createWorkspaceButton method to maintain consistent layout
                createWorkspaceButtonFromDB(workspaceName);
            }
            
            // Auto-scroll to top when dropdown expands
            Platform.runLater(() -> groupWorkspaceScrollPane.setVvalue(0));
        }
    }

    // New helper method to create workspace buttons from database without auto-expanding
    private void createWorkspaceButtonFromDB(String workspaceName) {
        // Create a container for the workspace button and delete button
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.setSpacing(0);
        buttonContainer.setPrefWidth(274.0); // Total width to match the main menu items
        
        // Workspace button (smaller and indented)
        Button newWorkspaceBtn = new Button("  " + workspaceName); // Added spaces for visual indentation
        newWorkspaceBtn.getStyleClass().add("menu-button");
        newWorkspaceBtn.setAlignment(Pos.BASELINE_LEFT);
        newWorkspaceBtn.setPrefHeight(30.0); // Slightly smaller than main buttons
        newWorkspaceBtn.setPrefWidth(234.0); // Increased to accommodate smaller spacer
        newWorkspaceBtn.setFont(Font.font("Arial", 12)); // Slightly smaller font
        newWorkspaceBtn.setPadding(new Insets(0, 10, 0, 40)); // Less padding since it's already indented
        
        // Spacer to push delete button to the center position of add button
        Region spacer = new Region();
        spacer.setPrefWidth(5.0); // Reduced to move delete button more to the left
        
        // Delete button 
        Button deleteButton = new Button("🗑");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setPrefWidth(40.0); // Match add button width
        deleteButton.setPrefHeight(30.0);
        deleteButton.setFont(Font.font("Arial", 16));
        newWorkspaceBtn.setPadding(new Insets(0, 0, 0, 10)); 
        deleteButton.setOnAction(e -> confirmAndDeleteWorkspaceFromDB(buttonContainer, workspaceName));
        
        // Set action to load workspace
        newWorkspaceBtn.setOnAction(e -> {
            try {
                WorkspaceGroup workspace = TaskDAO.getCurrentWorkspace(username, workspaceName);
                if (workspace == null) {
                    System.out.println("❌ Could not load workspace.");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("GroupWorkspace.fxml"));
                Pane view = loader.load();

                GroupWorkspaceController controller = loader.getController();
                controller.setUsername(username);
                controller.setWorkspaceData(workspace.getId(), workspace.getName());

                windowPane.getChildren().clear();
                windowPane.getChildren().add(view);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        
        // Add the container to the group workspace container
        buttonContainer.getChildren().addAll(newWorkspaceBtn, spacer, deleteButton);
        groupWorkspaceContainer.getChildren().add(buttonContainer);
        createdWorkspaces.add(buttonContainer);
    }

    // Updated delete method that also removes from database
    private void confirmAndDeleteWorkspaceFromDB(HBox buttonContainer, String fullWorkspaceDisplayName) {
        // Extract the actual workspace name before " (ID:"
        String workspaceName = fullWorkspaceDisplayName.split(" \\(ID:")[0];

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Workspace");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete '" + workspaceName + "'?");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Integer workspaceId = TaskDAO.fetchWorkspaceIdForUser(workspaceName, username);

            if (workspaceId == null) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Workspace Not Found");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Could not find workspace '" + workspaceName + "' associated with your account.");
                errorAlert.showAndWait();
                return;
            }

            boolean deleted = TaskDAO.deleteWorkspace(workspaceId, username);

            if (deleted) {
                // Remove from UI
                groupWorkspaceContainer.getChildren().remove(buttonContainer);
                createdWorkspaces.remove(buttonContainer);

                // Force refresh the workspace list
                refreshWorkspaceList();
                
                if (createdWorkspaces.isEmpty() && isGroupWorkspaceExpanded) {
                    toggleGroupWorkspaceDropdown();
                }
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Delete Failed");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Failed to delete workspace from database.");
                errorAlert.showAndWait();
            }
        }
    }

    
 // Add this new method to refresh the workspace list
    private void refreshWorkspaceList() {
        if (isGroupWorkspaceExpanded) {
            groupWorkspaceContainer.getChildren().clear();
            createdWorkspaces.clear();
            
            List<String> workspaces = TaskDAO.getUserGroupWorkspaces(username);
            for (String workspaceName : workspaces) {
                createWorkspaceButtonFromDB(workspaceName);
            }
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
        		
        		// Reset the urgent task alert everytime the user logged out
                PersonalWorkspaceController.resetSession();
                // When switching away from a workspace or logging out:
                GroupWorkspaceController.resetAlertForWorkspace(workspaceId);

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

        dialog.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                dialog.getEditor().setStyle("-fx-text-fill: red;");
            } else {
                dialog.getEditor().setStyle("-fx-text-fill: black;");
            }
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(workspaceName -> {
            if (!workspaceName.trim().isEmpty()) {
                List<String> existingWorkspaces = TaskDAO.getUserGroupWorkspaces(username);
                if (existingWorkspaces.contains(workspaceName.trim())) {
                    showAlert("Duplicate Workspace", "A workspace with this name already exists. Please choose a different name.");
                    return;
                }

                int workspaceId = TaskDAO.createWorkspaceForUser(workspaceName.trim(), username);
                if (workspaceId != -1) {
                    // ✅ Now we fetch the workspace directly by ID and user
                    WorkspaceGroup workspace = TaskDAO.getWorkspaceByIdAndUser(workspaceId, username);
                    if (workspace != null) {
                        createWorkspaceButton(workspace.getName() + " (ID: " + workspace.getId() + ")");

                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("GroupWorkspace.fxml"));
                            Pane view = loader.load();

                            GroupWorkspaceController controller = loader.getController();
                            controller.setUsername(username);
                            controller.setWorkspaceData(workspace.getId(), workspace.getName());

                            windowPane.getChildren().clear();
                            windowPane.getChildren().add(view);

                            refreshWorkspaceList(); // ✅ REFRESH UI right away
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
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
        buttonContainer.setSpacing(0);
        buttonContainer.setPrefWidth(274.0); // Total width to match the main menu items
        
        // Workspace button (smaller and indented)
        Button newWorkspaceBtn = new Button("  " + workspaceName); // Added spaces for visual indentation
        newWorkspaceBtn.getStyleClass().add("menu-button");
        newWorkspaceBtn.setAlignment(Pos.BASELINE_LEFT);
        newWorkspaceBtn.setPrefHeight(30.0); // Slightly smaller than main buttons
        newWorkspaceBtn.setPrefWidth(234.0); // Increased to accommodate smaller spacer
        newWorkspaceBtn.setFont(Font.font("Arial", 12)); // Slightly smaller font
        newWorkspaceBtn.setPadding(new Insets(0, 10, 0, 40)); // Less padding since it's already indented
        
        // Spacer to push delete button to the center position of add button
        Region spacer = new Region();
        spacer.setPrefWidth(5.0); // Reduced to move delete button more to the left
        
        // Delete button 
        Button deleteButton = new Button("🗑");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setPrefWidth(40.0); // Match add button width
        deleteButton.setPrefHeight(30.0);
        deleteButton.setFont(Font.font("Arial", 16));
        newWorkspaceBtn.setPadding(new Insets(0, 0, 0, 10)); 
        deleteButton.setOnAction(e -> confirmAndDeleteWorkspace(buttonContainer, workspaceName));
        
        // Set action to load workspace
        newWorkspaceBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GroupWorkspace.fxml"));
                Pane view = loader.load();
                
                GroupWorkspaceController controller = loader.getController();
                controller.setWorkspaceName(workspaceName);
                
                windowPane.getChildren().clear();
                windowPane.getChildren().add(view);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Add the container to the group workspace container
        buttonContainer.getChildren().addAll(newWorkspaceBtn, spacer, deleteButton);
        groupWorkspaceContainer.getChildren().add(buttonContainer);
        createdWorkspaces.add(buttonContainer);
        
        // Automatically expand the dropdown when a new workspace is added
        if (!isGroupWorkspaceExpanded) {
            toggleGroupWorkspaceDropdown();
        }
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
            groupWorkspaceContainer.getChildren().remove(buttonContainer);
            createdWorkspaces.remove(buttonContainer);
            
            // If no workspaces left, collapse the dropdown
            if (createdWorkspaces.isEmpty() && isGroupWorkspaceExpanded) {
                toggleGroupWorkspaceDropdown();
            }
        }
    }
}