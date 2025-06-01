package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import TaskManagement.TaskDAO;
import TaskManagement.WorkspaceGroup;

public class GroupWorkspaceController implements Initializable {

	@FXML private Label workspaceTitle;
    @FXML private TableView<TaskModel> tableView;
    @FXML private TableView<TaskModel> completedTable;
    
    // TO-DO Table Columns
    @FXML private TableColumn<TaskModel, Boolean> completedColumn;
    @FXML private TableColumn<TaskModel, String> taskColumn;
    @FXML private TableColumn<TaskModel, String> ownerColumn;
    @FXML private TableColumn<TaskModel, String> statusColumn;
    @FXML private TableColumn<TaskModel, String> dueDateColumn;
    @FXML private TableColumn<TaskModel, String> priorityColumn;
    
    // COMPLETED Table Columns
    @FXML private TableColumn<TaskModel, String> taskColumnCompleted;
    @FXML private TableColumn<TaskModel, String> ownerColumnCompleted;
    @FXML private TableColumn<TaskModel, String> statusColumnCompleted;
    @FXML private TableColumn<TaskModel, String> dueDateColumnCompleted;
    @FXML private TableColumn<TaskModel, String> priorityColumnCompleted;
    
    // Buttons
    @FXML private Button addTaskButton;
    @FXML private Button editTaskButton;
    @FXML private Button deleteTaskButton;
    @FXML private Button deleteAllButton;
    @FXML private Button inviteButton;
    @FXML private Button inviteAgain;
    
    // Invite Components
    @FXML private StackPane inviteStackPane;
    @FXML private AnchorPane mainAnchorPane;
    @FXML private AnchorPane invitePane;
    @FXML private AnchorPane notifiedPane;
    @FXML private TextField usernameField;
    
    // Data Lists
    private ObservableList<TaskModel> todoTasks;
    private ObservableList<TaskModel> completedTasks;
    private static Set<Integer> workspacesWithAlertShown = new HashSet<>();
    
    private int currentWorkspaceIDG; // Example workspace ID, replace with actual logic to get current workspace
    private String currentWorkspaceName; // Example workspace name, replace with actual logic
    private String username;

    // Must be called after username is set
    public void setWorkspaceData(int workspaceId, String workspaceName) {
        this.currentWorkspaceIDG = workspaceId;
        this.currentWorkspaceName = workspaceName;
        
        // Update the workspace title label
        updateWorkspaceTitle(workspaceName);
        
        loadTasks(); // Load tasks for this workspace
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        todoTasks      = FXCollections.observableArrayList();
        completedTasks = FXCollections.observableArrayList();

        setupTodoTable();
        setupCompletedTable();

        tableView.setItems(todoTasks);
        completedTable.setItems(completedTasks);
    }

    private void loadTasks() {
        if (username == null || username.isBlank()) {
            return;
        }

        if (currentWorkspaceIDG <= 0) {
            return;
        }

        todoTasks.clear();
        completedTasks.clear();

        List<TaskModel> relevantTasks = TaskDAO.getTasksByWorkspace(username, currentWorkspaceIDG);
        
        // Check for urgent tasks
        List<TaskModel> urgentTasks = new ArrayList<>();
        
        for (TaskModel task : relevantTasks) {
            String status = task.getStatus();
            System.out.println(task.getPriority() + " - " + task.getDueDate());
            if ("Done".equalsIgnoreCase(status)) {
                completedTasks.add(task);
            } else {
                todoTasks.add(task);
                
                // Check if task is urgent (due today or tomorrow)
                if (isTaskUrgent(task)) {
                    urgentTasks.add(task);
                }
            }
        }

        tableView.setItems(todoTasks);
        
        // Show urgent tasks alert if there are any and it hasn't been shown for this workspace in this session
        if (!urgentTasks.isEmpty() && !workspacesWithAlertShown.contains(currentWorkspaceIDG)) {
            showUrgentTasksAlert(urgentTasks);
            workspacesWithAlertShown.add(currentWorkspaceIDG); // Mark this workspace as alerted for this session
        }
    }

    
    // Reset workspace when needed
    public static void resetAllWorkspaceAlerts() {
        workspacesWithAlertShown.clear();
    }
    
    private boolean isTaskUrgent(TaskModel task) {
        try {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate dueDate = LocalDate.parse(task.getDueDate(), formatter);
            
            return dueDate.isEqual(today) || dueDate.isEqual(today.plusDays(1));
        } catch (Exception e) {
            return false;
        }
    }
    
    private void showUrgentTasksAlert(List<TaskModel> urgentTasks) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Urgent Tasks Reminder");
        alert.setHeaderText("You have tasks due soon!");
        
        // Create main container with better spacing
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: white;");
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
        // Separate tasks due today and tomorrow
        List<TaskModel> todayTasks = new ArrayList<>();
        List<TaskModel> tomorrowTasks = new ArrayList<>();
        
        for (TaskModel task : urgentTasks) {
            try {
                LocalDate dueDate = LocalDate.parse(task.getDueDate(), formatter);
                if (dueDate.isEqual(today)) {
                    todayTasks.add(task);
                } else {
                    tomorrowTasks.add(task);
                }
            } catch (Exception e) {
                // Skip if date parsing fails
            }
        }
        
        // Create section for today's tasks
        if (!todayTasks.isEmpty()) {
            VBox todaySection = new VBox(8);
            
            Label todayLabel = new Label("📅 Tasks Due Today (" + todayTasks.size() + ")");
            todayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #d32f2f;");
            
            // Create simple text list instead of table
            VBox todayTasksList = createSimpleTasksList(todayTasks);
            
            todaySection.getChildren().addAll(todayLabel, todayTasksList);
            mainContainer.getChildren().add(todaySection);
        }
        
        // Create section for tomorrow's tasks
        if (!tomorrowTasks.isEmpty()) {
            VBox tomorrowSection = new VBox(8);
            
            Label tomorrowLabel = new Label("⏰ Tasks Due Tomorrow (" + tomorrowTasks.size() + ")");
            tomorrowLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #f57c00;");
            
            // Create simple text list instead of table
            VBox tomorrowTasksList = createSimpleTasksList(tomorrowTasks);
            
            tomorrowSection.getChildren().addAll(tomorrowLabel, tomorrowTasksList);
            mainContainer.getChildren().add(tomorrowSection);
        }
        
        // Add a helpful message at the bottom
        Label reminderText = new Label("💡 Tip: Review and prioritize these tasks to stay on track!");
        reminderText.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 12px;");
        reminderText.setWrapText(true);
        mainContainer.getChildren().add(reminderText);
        
        // Wrap in ScrollPane with increased height
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(400); // Increased from 300
        scrollPane.setStyle("-fx-background-color: white;");
        
        // Set the custom content
        alert.getDialogPane().setContent(scrollPane);
        
        // Set icon for alert
        setAlertIcon(alert);
        
        // Make the alert resizable and set larger size
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(550); // Increased from 500
        
        // Increased height calculation with higher minimums and multipliers
        int calculatedHeight = Math.min(600, Math.max(400, urgentTasks.size() * 40 + 200));
        alert.getDialogPane().setPrefHeight(calculatedHeight);
        
        // Add custom button with better text
        ButtonType acknowledgeButton = new ButtonType("I'll Review These Tasks", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(acknowledgeButton);
        
        alert.showAndWait();
    }

    private VBox createSimpleTasksList(List<TaskModel> tasks) {
        VBox tasksList = new VBox(8); // Increased spacing from 5 to 8
        tasksList.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 12px; -fx-background-radius: 5px;"); // Increased padding
        
        for (TaskModel task : tasks) {
            // Create a container for each task
            VBox taskContainer = new VBox(5); // Increased spacing from 3 to 5
            taskContainer.setStyle("-fx-background-color: white; -fx-padding: 12px; -fx-background-radius: 3px; -fx-border-color: #e0e0e0; -fx-border-radius: 3px;"); // Increased padding
            
            // Task name label
            Label taskNameLabel = new Label("• " + task.getTask());
            taskNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");
            taskNameLabel.setWrapText(true);
            
            // Priority info only
            String priorityText = "Priority: " + task.getPriority().toUpperCase();
            
            Label infoLabel = new Label(priorityText);
            infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
            
            taskContainer.getChildren().addAll(taskNameLabel, infoLabel);
            tasksList.getChildren().add(taskContainer);
        }
        
        return tasksList;
    }
        
    private void setupTodoTable() {
        // Set up columns for TO-DO table (non-editable except for completion checkbox)
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        completedColumn.setCellFactory(column -> {
            CheckBoxTableCell<TaskModel, Boolean> cell = new CheckBoxTableCell<>();
            return cell;
        });
        completedColumn.setEditable(true);
        
        // All other columns are read-only - just display the data
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
        
        // OWNER COLUMN WITH CENTER ALIGNMENT
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        ownerColumn.setCellFactory(column -> {
            return new TableCell<TaskModel, String>() {
                @Override
                protected void updateItem(String owner, boolean empty) {
                    super.updateItem(owner, empty);
                    
                    if (empty || owner == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(owner);
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            };
        });
        
        // STATUS COLUMN WITH COLORS
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> {
            return new TableCell<TaskModel, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    
                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);
                        
                        // Set background color based on status with center alignment
                        switch (status.toLowerCase()) {
                            case "not started":
                                setStyle("-fx-background-color: #BABABA; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            case "in progress":
                                setStyle("-fx-background-color: #FFC107; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            case "done":
                                setStyle("-fx-background-color: #2DCA7B; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            default:
                                setStyle("-fx-alignment: CENTER;");
                                break;
                        }
                    }
                }
            };
        });
        
        // DUE DATE COLUMN WITH CENTER ALIGNMENT
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateColumn.setCellFactory(column -> {
            return new TableCell<TaskModel, String>() {
                @Override
                protected void updateItem(String dueDate, boolean empty) {
                    super.updateItem(dueDate, empty);
                    
                    if (empty || dueDate == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(dueDate);
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            };
        });
        
        // PRIORITY COLUMN WITH UPDATED COLORS
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setCellFactory(column -> {
            return new TableCell<TaskModel, String>() {
                @Override
                protected void updateItem(String priority, boolean empty) {
                    super.updateItem(priority, empty);
                    
                    if (empty || priority == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(priority);
                        
                        // Set background color based on priority - updated colors with center alignment
                        switch (priority.toLowerCase()) {
                            case "low":
                                setStyle("-fx-background-color: #FFF3C1; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            case "medium":
                                setStyle("-fx-background-color: #FFC107; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            case "high":
                                setStyle("-fx-background-color: #FF6B2C; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            default:
                                setStyle("-fx-alignment: CENTER;");
                                break;
                        }
                    }
                }
            };
        });
        
        // Set table as non-editable except for the completed column
        tableView.setEditable(true); // Only needed for the checkbox column
        
        // Add listener to each task's completed property
        todoTasks.addListener((javafx.collections.ListChangeListener<TaskModel>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (TaskModel task : change.getAddedSubList()) {
                        task.completedProperty().addListener((obs, oldVal, newVal) -> {
                            handleTaskCompletion(task, newVal);

                            // 🔽 CRUD Integration: Persist checkbox update to DB
                            try {
                                TaskDAO.updateTask(task); // Replace with your DAO update call
                            } catch (Exception e) {
                                e.printStackTrace(); // Optional: Show alert to user
                            }
                        });
                    }
                }
            }
        });
    }

    
    private void setupCompletedTable() {
        // Set up columns for COMPLETED table (read-only)
        taskColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("task"));
        
        // OWNER COLUMN WITH CENTER ALIGNMENT (COMPLETED TABLE)
        ownerColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("owner"));
        ownerColumnCompleted.setCellFactory(column -> {
            return new TableCell<TaskModel, String>() {
                @Override
                protected void updateItem(String owner, boolean empty) {
                    super.updateItem(owner, empty);
                    
                    if (empty || owner == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(owner);
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            };
        });
        
        // STATUS COLUMN WITH COLORS (COMPLETED TABLE)
        statusColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumnCompleted.setCellFactory(column -> {
            return new TableCell<TaskModel, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    
                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);
                        
                        // Set background color based on status with center alignment
                        switch (status.toLowerCase()) {
                            case "not started":
                                setStyle("-fx-background-color: #BABABA; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            case "in progress":
                                setStyle("-fx-background-color: #FFC107; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            case "done":
                                setStyle("-fx-background-color: #2DCA7B; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            default:
                                setStyle("-fx-alignment: CENTER;");
                                break;
                        }
                    }
                }
            };
        });
        
        // DUE DATE COLUMN WITH CENTER ALIGNMENT (COMPLETED TABLE)
        dueDateColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateColumnCompleted.setCellFactory(column -> {
            return new TableCell<TaskModel, String>() {
                @Override
                protected void updateItem(String dueDate, boolean empty) {
                    super.updateItem(dueDate, empty);
                    
                    if (empty || dueDate == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(dueDate);
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            };
        });
        
        // PRIORITY COLUMN WITH UPDATED COLORS (COMPLETED TABLE)
        priorityColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumnCompleted.setCellFactory(column -> {
            return new TableCell<TaskModel, String>() {
                @Override
                protected void updateItem(String priority, boolean empty) {
                    super.updateItem(priority, empty);
                    
                    if (empty || priority == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(priority);
                        
                        // Set background color based on priority - updated colors with center alignment
                        switch (priority.toLowerCase()) {
                            case "low":
                                setStyle("-fx-background-color: #FFF3C1; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            case "medium":
                                setStyle("-fx-background-color: #FFC107; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            case "high":
                                setStyle("-fx-background-color: #FF6B2C; -fx-text-fill: #000000; -fx-alignment: CENTER;");
                                break;
                            default:
                                setStyle("-fx-alignment: CENTER;");
                                break;
                        }
                    }
                }
            };
        });
    }
    
    // FXML Components
    @FXML
    private void handleEditTask() {
        TaskModel selectedTask = tableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            Optional<TaskModel> result = showEditTaskDialog(selectedTask);
            result.ifPresent(editedTask -> {
                try {
                    TaskDAO.updateTask(editedTask);
                    loadTasks(); // ✅ Refresh after update
                    showAlertWithType(Alert.AlertType.INFORMATION, "Success", "Task edited successfully.");
                } catch (Exception e) {
                    showAlertWithType(Alert.AlertType.ERROR, "Database Error", "Failed to update the task in the database.");
                }
            });
        } else {
            showAlertWithType(Alert.AlertType.WARNING, "No Selection", "Please select a task to edit.");
        }
    }


    
    private Optional<TaskModel> showEditTaskDialog(TaskModel taskToEdit) {
        Dialog<TaskModel> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit task details:");

        try {
            TextField taskField = new TextField(taskToEdit.getTask());
            taskField.setPromptText("Task description");

            ComboBox<String> ownerCombo = new ComboBox<>();
            try {
                ownerCombo.setItems(TaskDAO.getAllWorkspaceMembers(username, currentWorkspaceIDG));
                ownerCombo.setValue(taskToEdit.getOwner());
                ownerCombo.setEditable(true);
            } catch (Exception e) {
                System.err.println("❌ Error loading workspace members: " + e.getMessage());
                ownerCombo.setEditable(true);
                ownerCombo.setValue(taskToEdit.getOwner());
            }

            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("Not Started", "In Progress", "Done");
            statusCombo.setValue(taskToEdit.getStatus());

            DatePicker dueDateField = new DatePicker();
            dueDateField.setPromptText("MM-dd-yy");
            if (taskToEdit.getDueDate() != null && !taskToEdit.getDueDate().isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(taskToEdit.getDueDate(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    dueDateField.setValue(date);
                } catch (Exception e) {
                    System.err.println("⚠️ Could not parse existing due date: " + taskToEdit.getDueDate());
                    // Leave date picker empty if parsing fails
                }
            }

            ComboBox<String> priorityCombo = new ComboBox<>();
            priorityCombo.getItems().addAll("Low", "Medium", "High");
            priorityCombo.setValue(taskToEdit.getPriority());

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Task:"), 0, 0);
            grid.add(taskField, 1, 0);
            grid.add(new Label("Owner:"), 0, 1);
            grid.add(ownerCombo, 1, 1);
            grid.add(new Label("Status:"), 0, 2);
            grid.add(statusCombo, 1, 2);
            grid.add(new Label("Due Date:"), 0, 3);
            grid.add(dueDateField, 1, 3);
            grid.add(new Label("Priority:"), 0, 4);
            grid.add(priorityCombo, 1, 4);

            dialog.getDialogPane().setContent(grid);

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            
            // Add comprehensive validation with event filter
            saveButton.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    // Validate required fields
                    String taskDescription = taskField.getText() != null ? taskField.getText().trim() : "";
                    String ownerValue = ownerCombo.getValue() != null ? ownerCombo.getValue().trim() : "";

                    // Check if both Task description and Owner are empty
                    if (taskDescription.isEmpty() && ownerValue.isEmpty()) {
                        showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "Task description and owner cannot be empty");
                        event.consume();
                        return;
                    } else if (taskDescription.isEmpty()) {
                        showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "Task description cannot be empty");
                        event.consume();
                        return;
                    } else if (ownerValue.isEmpty()) {
                        showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "Owner cannot be empty");
                        event.consume();
                        return;
                    }

                    // Validate due date is selected first
                    LocalDate selectedDate = dueDateField.getValue();
                    if (selectedDate == null) {
                        showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "The due date field must not be empty. Please select a due date.");
                        event.consume();
                        return;
                    }

                    // Then validate due date is not in the past
                    if (selectedDate.isBefore(LocalDate.now())) {
                        showAlertWithType(Alert.AlertType.WARNING, "Invalid Due Date", 
                            "Due date cannot be set to a date before today. Please select today's date or a future date.");
                        event.consume();
                        return;
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error during validation: " + e.getMessage());
                    e.printStackTrace();
                    showAlertWithType(Alert.AlertType.ERROR, "Validation Error", 
                        "An unexpected error occurred while validating the task: " + e.getMessage());
                    event.consume();
                }
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String taskTitle = taskField.getText() != null ? taskField.getText().trim() : "";
                        String ownerRaw = ownerCombo.getValue() != null ? ownerCombo.getValue().trim() : "";
                        String owner = ownerRaw.contains(" (") ? ownerRaw.substring(0, ownerRaw.indexOf(" (")) : ownerRaw;
                        String status = statusCombo.getValue();
                        String priority = priorityCombo.getValue();

                        String dueDate = "";
                        if (dueDateField.getValue() != null) {
                            dueDate = dueDateField.getValue().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        }

                        // Final validation check including due date
                        if (taskTitle.isEmpty() || owner.isEmpty()) {
                            showAlertWithType(Alert.AlertType.WARNING, "Input Error", 
                                "All fields must be filled. Please complete all required fields.");
                            return null;
                        }

                        if (dueDate.isEmpty()) {
                            showAlertWithType(Alert.AlertType.ERROR, "Validation Error", 
                                "The due date field must not be empty. Please select a due date.");
                            return null;
                        }

                        // Update the task object
                        taskToEdit.setTask(taskTitle);
                        taskToEdit.setOwner(owner);
                        taskToEdit.setStatus(status);
                        taskToEdit.setDueDate(dueDate);
                        taskToEdit.setPriority(priority);
                        
                        return taskToEdit;
                    } catch (Exception e) {
                        System.err.println("❌ Error creating updated task: " + e.getMessage());
                        e.printStackTrace();
                        showAlertWithType(Alert.AlertType.ERROR, "Error", 
                            "An unexpected error occurred while updating the task: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            // Set icon for dialog with error handling
            try {
                dialog.setOnShown(e -> {
                    try {
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
                    } catch (Exception iconError) {
                        System.err.println("⚠️ Could not set dialog icon: " + iconError.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Could not setup dialog icon handler: " + e.getMessage());
            }

            return dialog.showAndWait();
            
        } catch (Exception e) {
            System.err.println("❌ Error creating edit dialog: " + e.getMessage());
            e.printStackTrace();
            showAlertWithType(Alert.AlertType.ERROR, "Dialog Creation Error", 
                "An unexpected error occurred while creating the edit dialog: " + e.getMessage());
            return Optional.empty();
        }
    }

    @FXML
    private void handleAddTask() {
        try {
            // Create a new task dialog
            Dialog<TaskModel> dialog = new Dialog<>();
            dialog.setTitle("Add New Task");
            dialog.setHeaderText("Enter task details:");

            // Create form fields
            TextField taskField = new TextField();
            taskField.setPromptText("Task description");

            // Changed from TextField to ComboBox for owner
            ComboBox<String> ownerCombo = new ComboBox<>();
            try {
                ownerCombo.setItems(TaskDAO.getAllWorkspaceMembers(username, currentWorkspaceIDG));
                ownerCombo.setPromptText("Select owner");
                ownerCombo.setEditable(false); // Allow custom input if needed
            } catch (Exception e) {
                System.err.println("❌ Error loading workspace members: " + e.getMessage());
                ownerCombo.setEditable(true);
                ownerCombo.setPromptText("Enter owner username");
            }

            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("Not Started", "In Progress", "Done");
            statusCombo.setValue("Not Started");

            // FIXED: Replace TextField with DatePicker
            DatePicker dueDateField = new DatePicker();
            dueDateField.setPromptText("MM-DD-YY");

            ComboBox<String> priorityCombo = new ComboBox<>();
            priorityCombo.getItems().addAll("Low", "Medium", "High");
            priorityCombo.setValue("Low");

            // Create dialog content
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            grid.add(new Label("Task:"), 0, 0);
            grid.add(taskField, 1, 0);
            grid.add(new Label("Owner:"), 0, 1);
            grid.add(ownerCombo, 1, 1);
            grid.add(new Label("Status:"), 0, 2);
            grid.add(statusCombo, 1, 2);
            grid.add(new Label("Due Date:"), 0, 3);
            grid.add(dueDateField, 1, 3);
            grid.add(new Label("Priority:"), 0, 4);
            grid.add(priorityCombo, 1, 4);

            dialog.getDialogPane().setContent(grid);

            // Add buttons
            ButtonType okButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            // Get the Add button and disable it initially if needed
            Button addButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);

            // Add event filter to the Add button to prevent dialog from closing on validation failure
            addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                try {
                    // Validate required fields
                    String taskDescription = taskField.getText() != null ? taskField.getText().trim() : "";
                    String ownerValue = ownerCombo.getValue() != null ? ownerCombo.getValue().trim() : "";

                    // Check if both Task description and Owner are empty
                    if (taskDescription.isEmpty() && ownerValue.isEmpty()) {
                        showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "Task description and owner cannot be empty");
                        event.consume();
                        return;
                    } else if (taskDescription.isEmpty()) {
                        showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "Task description cannot be empty");
                        event.consume();
                        return;
                    } else if (ownerValue.isEmpty()) {
                        showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "Owner cannot be empty");
                        event.consume();
                        return;
                    }

                    // Validate due date is selected first
                    LocalDate selectedDate = dueDateField.getValue();
                    if (selectedDate == null) {
                        showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "The due date field must not be empty. Please select a due date.");
                        event.consume();
                        return;
                    }

                    // Then validate due date is not in the past
                    if (selectedDate.isBefore(LocalDate.now())) {
                        showAlertWithType(Alert.AlertType.WARNING, "Invalid Due Date", "Due date cannot be set to a date before today. Please select today's date or a future date.");
                        event.consume();
                        return;
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error during validation: " + e.getMessage());
                    e.printStackTrace();
                    showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "An unexpected error occurred while validating the task: " + e.getMessage());
                    event.consume();
                }
            });

            // Result converter
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    try {
                        String taskTitle = taskField.getText().trim();
                        String ownerRaw = ownerCombo.getValue() != null ? ownerCombo.getValue().trim() : "";
                        String owner = ownerRaw.contains(" (") ? ownerRaw.substring(0, ownerRaw.indexOf(" (")) : ownerRaw;
                        String status = statusCombo.getValue();
                        String priority = priorityCombo.getValue();

                        String dueDate = "";
                        if (dueDateField.getValue() != null) {
                            dueDate = dueDateField.getValue().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        }

                        // Final validation check including due date
                        if (taskTitle.isEmpty() || owner.isEmpty()) {
                            showAlertWithType(Alert.AlertType.WARNING, "Input Error", "All fields must be filled. Please complete all required fields.");
                            return null;
                        }

                        if (dueDate.isEmpty()) {
                            showAlertWithType(Alert.AlertType.ERROR, "Validation Error", "The due date field must not be empty. Please select a due date.");
                            return null;
                        }

                        TaskModel newTask = new TaskModel(taskTitle, owner, status, dueDate, priority);
                        newTask.setCompleted(false);
                        newTask.setWorkspaceID(currentWorkspaceIDG);
                        return newTask;
                    } catch (Exception e) {
                        System.err.println("❌ Error creating new task: " + e.getMessage());
                        e.printStackTrace();
                        showAlertWithType(Alert.AlertType.ERROR, "Error", "An unexpected error occurred while creating the task: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            // Set icon for dialog
            try {
                dialog.setOnShown(e -> {
                    try {
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
                    } catch (Exception iconError) {
                        System.err.println("⚠️ Could not set dialog icon: " + iconError.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Could not setup dialog icon handler: " + e.getMessage());
            }

            // Show dialog and handle result
            Optional<TaskModel> result = dialog.showAndWait();
            result.ifPresent(task -> {
                try {
                    boolean inserted = TaskDAO.addTaskSimple(task, currentWorkspaceIDG);
                    if (inserted) {
                        loadTasks();
                    } else {
                        System.err.println("❌ Task not inserted. Checking DB manually...");
                        if (TaskDAO.wasTaskInserted(task)) {
                            System.out.println("⚠️ DB says task *was* inserted, but method returned false.");
                            todoTasks.add(task);
                            showAlertWithType(Alert.AlertType.INFORMATION, "Success", "Task added successfully.");
                        } else {
                            System.err.println("❌ Task truly not inserted.");
                            showAlertWithType(Alert.AlertType.ERROR, "Database Error", "The task could not be inserted into the database. Please try again.");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error adding task to database: " + e.getMessage());
                    e.printStackTrace();
                    showAlertWithType(Alert.AlertType.ERROR, "Database Error", "An unexpected error occurred while adding the task: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error in add task dialog: " + e.getMessage());
            e.printStackTrace();
            showAlertWithType(Alert.AlertType.ERROR, "Dialog Error", "An unexpected error occurred while opening the add task dialog: " + e.getMessage());
        }
    }

    @FXML
    private ComboBox<String> ownerComboBox;

    public void getOwnerOptions() {
        // assuming 'username' is set somewhere before initialize or passed in
        ObservableList<String> options = TaskDAO.getAllWorkspaceMembers(username, currentWorkspaceIDG);
        ownerComboBox.setItems(options);
    }
    
    @FXML
    private void handleDeleteTask() {
        TaskModel selectedTask = tableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Task");
            alert.setHeaderText("Are you sure you want to delete this task?");
            alert.setContentText(selectedTask.getTask());
            
            // Set icon for alert
            setAlertIcon(alert);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                TaskDAO.deleteTask(selectedTask.getTaskID());
                loadTasks(); // Reload tasks to ensure UI reflects changes
            }
        } else {
            showAlertWithType(Alert.AlertType.WARNING, "No Selection", "Please select a task to delete.");
        }
    }
    
    @FXML
    private void handleDeleteAll() {
        if (!completedTasks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete All Completed Tasks");
            alert.setHeaderText("Are you sure you want to delete all completed tasks?");
            alert.setContentText("This action cannot be undone.");
            
            // Set icon for alert
            setAlertIcon(alert);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                // Delete each task from the database
                for (TaskModel task : completedTasks) {
                    TaskDAO.deleteTask(task.getTaskID());
                }
                loadTasks();
                // Clear the observable list
                completedTasks.clear();
            }
        } else {
            showAlertWithType(Alert.AlertType.INFORMATION, "No Tasks", "There are no completed tasks to delete.");
        }
    }
    
    private void handleTaskCompletion(TaskModel task, boolean isCompleted) {
        if (isCompleted) {
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Complete Task");
            confirmAlert.setHeaderText("Mark task as completed?");
            confirmAlert.setContentText("Are you sure you want to mark \"" + task.getTask() + "\" as completed?\n\nThis will move the task to the completed section.");

            // Set icon for alert
            setAlertIcon(confirmAlert);

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // ✅ Update task status in the database
                boolean success = TaskDAO.updateTaskStatus(task.getTaskID(), "Done");
                if (success) {
                    task.setStatus("Done"); // Update the local object if needed
                    moveTaskToCompleted(task); // Move it to the UI completed section
                } else {
                    showError("Failed to update task status in database.");
                    task.setCompleted(false); // Revert checkbox
                }
            } else {
                // User cancelled - uncheck the checkbox
                task.setCompleted(false);
            }
        }
    }

    private void showError(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText("An error occurred");
        errorAlert.setContentText(message);
        setAlertIcon(errorAlert);
        errorAlert.showAndWait();
    }

    // Method to move completed tasks from todo to completed table
    public void moveTaskToCompleted(TaskModel task) {
        if (todoTasks.contains(task)) {
            task.setStatus("Done");
            task.setCompleted(true);
            todoTasks.remove(task);
            completedTasks.add(task);
        }
    }
    
    @FXML
    private void inviteButton() {
        setWorkspaceData(currentWorkspaceIDG, currentWorkspaceName);
        inviteStackPane.setVisible(true);
        mainAnchorPane.setVisible(true);
        invitePane.setVisible(true);
        notifiedPane.setVisible(false);
    }
    
    @FXML
    private void handleInviteFriend() {
        setWorkspaceData(currentWorkspaceIDG, currentWorkspaceName);
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlertWithType(Alert.AlertType.WARNING, "Invalid Input", "Please enter a username.");
            return;
        }

        // Check if the username exists
        if (!TaskDAO.doesUserExist(username)) {
            showAlertWithType(Alert.AlertType.ERROR, "User Not Found", "The specified username does not exist.");
            return;
        }

        // Get user email
        String recipientEmail = TaskDAO.getUserEmail(username);
        if (recipientEmail == null) {
            showAlertWithType(Alert.AlertType.ERROR, "Error", "Could not retrieve email for the user.");
            return;
        }

        int currentWorkspaceID = currentWorkspaceIDG;
        String workspaceName = currentWorkspaceName;

        // Call DAO method to add member
        boolean success = TaskDAO.addMemberToWorkspace(currentWorkspaceID, username);

        if (success) {
            // Send email notification
            boolean emailSent = WorkspaceGroup.sendInvitationEmail(
                recipientEmail, 
                workspaceName, 
                this.username // current user who is inviting
            );
            
            if (emailSent) {
                invitePane.setVisible(false);
                notifiedPane.setVisible(true);
                usernameField.clear();
            } else {
                showAlertWithType(Alert.AlertType.WARNING, 
                    "Invitation Sent", 
                    "User was added to workspace but email notification failed to send.");
            }
        } else {
            showAlertWithType(Alert.AlertType.ERROR, "Failed", "User is already in the workspace or an error occurred.");
        }
    }
  
    @FXML
    private void handleContinue() {
        inviteStackPane.setVisible(false);
        mainAnchorPane.setVisible(false);
        invitePane.setVisible(false);
        notifiedPane.setVisible(false);
    }
    
    @FXML
    private void inviteAgain() {
        notifiedPane.setVisible(false);
        invitePane.setVisible(true);
    }
    
    @FXML
    private void handleReturnClick() {
        handleContinue();
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Set icon for alert
        setAlertIcon(alert);
        
        alert.showAndWait();
    }
    
    private void showAlertWithType(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Set icon for alert
        setAlertIcon(alert);
        
        alert.showAndWait();
    }

    private void setAlertIcon(Alert alert) {
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
        } catch (Exception e) {
            // If icon loading fails, continue without icon
            System.err.println("Warning: Could not load alert icon: " + e.getMessage());
        }
    }
    
    private void setDialogIcon(Dialog<?> dialog) {
        try {
            // Set icon when dialog is shown
            dialog.setOnShowing(event -> {
                try {
                    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
                } catch (Exception e) {
                    // If icon loading fails, continue without icon
                    System.err.println("Warning: Could not load dialog icon: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            // If setting up icon fails, continue without icon
            System.err.println("Warning: Could not set up dialog icon: " + e.getMessage());
        }
    }
    
    // Add this new method to update the title
    public void updateWorkspaceTitle(String workspaceName) {
        if (workspaceTitle != null && workspaceName != null && !workspaceName.trim().isEmpty()) {
            workspaceTitle.setText(workspaceName.trim());
        }
    }

    public void setWorkspaceName(String workspaceName) {
        if (workspaceName != null && !workspaceName.trim().isEmpty()) {
            workspaceTitle.setText(workspaceName.trim());
        }
    }
    

    public String getWorkspaceName(String workspaceName) {
        return workspaceName != null ? workspaceName.trim() : "";
    }
}