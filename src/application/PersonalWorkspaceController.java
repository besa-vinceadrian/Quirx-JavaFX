
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
import javafx.scene.control.DatePicker;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import java.util.ArrayList;
import javafx.application.Platform;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import TaskManagement.TaskDAO;

public class PersonalWorkspaceController implements Initializable {

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
    @FXML private TextField emailField;
    
    // Data Lists
    private ObservableList<TaskModel> todoTasks;
    private ObservableList<TaskModel> completedTasks;
    
    private int currentWorkspaceID; // Example workspace ID, replace with actual logic to get current workspace
    private String currentWorkspaceName; // Example workspace name, replace with actual logic
    private String username;

    public void setWorkspaceData(int currentWorkspaceID, String currentWorkspaceName) {
        this.currentWorkspaceID = currentWorkspaceID;
        this.currentWorkspaceName = currentWorkspaceName;
        loadTasks(); // Load tasks for the current workspace
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
        System.out.println("Fetching tasks for user: " + username + " or workspaceID: " + currentWorkspaceID);
        if (username == null || username.isBlank()) {
            System.out.println("⚠️ Username not set – no tasks loaded.");
            return;
        }

        if (currentWorkspaceID <= 0) {
            System.out.println("⚠️ Invalid workspace ID – no tasks loaded.");
            return;
        }

        todoTasks.clear();
        completedTasks.clear();

        List<TaskModel> relevantTasks = TaskDAO.getTasksByWorkspace(username, currentWorkspaceID);
        System.out.println("✅ Tasks fetched: " + relevantTasks.size());
        

        for (TaskModel task : relevantTasks) {
            String status = task.getStatus();
            System.out.println(task.getPriority() + " - " + task.getDueDate());
            if ("Done".equalsIgnoreCase(status)) {
                completedTasks.add(task);
            } else {
                todoTasks.add(task);
            }
        }

        tableView.setItems(todoTasks);
        
        // Show urgent tasks alert after loading tasks
        Platform.runLater(() -> checkAndShowDueDateAlert());
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

        TextField taskField = new TextField(taskToEdit.getTask());
        taskField.setPromptText("Task description");

        ComboBox<String> ownerCombo = new ComboBox<>();
        String currentUser = username; // your logged-in username
        String workspaceName = taskToEdit.getWorkspaceName(); // this must exist in TaskModel

        if (workspaceName.equalsIgnoreCase("Personal Workspace")) {
            ownerCombo.getItems().add(currentUser);
            ownerCombo.setValue(currentUser);
            ownerCombo.setDisable(true); // prevent selection
        } else {
            ownerCombo.getItems().addAll(getOwnerOptions());
            ownerCombo.setValue(taskToEdit.getOwner());
            ownerCombo.setEditable(false); // only allow valid usernames
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
            } catch (Exception ignored) {}
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
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            LocalDate selectedDate = dueDateField.getValue();
            if (selectedDate != null && selectedDate.isBefore(LocalDate.now())) {
                showAlertWithType(Alert.AlertType.WARNING, "Invalid Due Date", "Due date cannot be in the past.");
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (dueDateField.getValue() == null) {
                    showAlertWithType(Alert.AlertType.WARNING, "Missing Due Date", "Please select a due date.");
                    return null; // Don't close dialog if due date is empty
                }

                taskToEdit.setTask(taskField.getText().trim());
                taskToEdit.setOwner(ownerCombo.getValue().trim());
                taskToEdit.setStatus(statusCombo.getValue());
                taskToEdit.setDueDate(
                	    dueDateField.getValue().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                	);
                taskToEdit.setPriority(priorityCombo.getValue());
                return taskToEdit;
            }
            return null;
        });

        dialog.setOnShown(e -> {
            try {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
            } catch (Exception ignored) {}
        });

        return dialog.showAndWait();
    }

    @FXML
    private void handleAddTask() {
        // Create a new task dialog
        Dialog<TaskModel> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Enter task details:");

        // Create form fields
        TextField taskField = new TextField();
        taskField.setPromptText("Task description");

        ComboBox<String> ownerCombo = new ComboBox<>();
        String currentUser = username; // your logged-in user
        String workspaceName = currentWorkspaceName; // You must provide this method or value

        if (workspaceName.equalsIgnoreCase("Personal Workspace")) {
            ownerCombo.getItems().add(currentUser + " (me)");
            ownerCombo.setValue(currentUser + " (me)");
            ownerCombo.setDisable(true); // lock the field
        } else {
            ownerCombo.getItems().addAll(getOwnerOptions());
            ownerCombo.setPromptText("Select owner");
            ownerCombo.setEditable(false);
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

                // NEW: Validate due date is not in the past
                LocalDate selectedDate = dueDateField.getValue();
                if (selectedDate != null && selectedDate.isBefore(LocalDate.now())) {
                    showAlertWithType(Alert.AlertType.WARNING, "Invalid Due Date", "Due date cannot be set to a date before today. Please select today's date or a future date.");
                    event.consume();
                    return;
                }

            } catch (Exception e) {
                showAlertWithType(Alert.AlertType.ERROR, "Error", "An unexpected error occurred while validating the task: " + e.getMessage());
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

                	if (taskTitle.isEmpty() || owner.isEmpty()) {
                	    Alert alert = new Alert(Alert.AlertType.WARNING);
                	    alert.setTitle("Input Error");
                	    alert.setHeaderText("All fields must be filled.");
                	    alert.setContentText("Please complete all required fields.");
                	    alert.showAndWait();
                	    return null;
                	}

                    TaskModel newTask = new TaskModel(taskTitle, owner, status, dueDate, priority);
                    newTask.setCompleted(false);
                    newTask.setWorkspaceID(currentWorkspaceID);
                    return newTask;
                } catch (Exception e) {
                    showAlertWithType(Alert.AlertType.ERROR, "Error", "An unexpected error occurred while creating the task: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Set icon for dialog
        try {
            dialog.setOnShown(e -> {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
            });
        } catch (Exception e) {
            System.err.println("Could not set dialog icon: " + e.getMessage());
        }

        // Show dialog and handle result
        Optional<TaskModel> result = dialog.showAndWait();
        result.ifPresent(task -> {
            boolean inserted = TaskDAO.addTask(task, currentWorkspaceID, currentWorkspaceName, username);
            if (inserted) {
                loadTasks();
            } else {
                System.err.println("❌ Task not inserted. Checking DB manually...");
                if (TaskDAO.wasTaskInserted(task)) {
                    System.out.println("⚠️ DB says task *was* inserted, but method returned false.");
                    todoTasks.add(task);
                } else {
                    System.err.println("❌ Task truly not inserted.");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Error");
                    alert.setHeaderText("Task not added");
                    alert.setContentText("The task could not be inserted into the database. Please try again.");
                    alert.showAndWait();
                }
            }
        });
    }

    private ObservableList<String> getOwnerOptions() {
        // Ideally fetch from DB, but here’s a placeholder
        ObservableList<String> owners = FXCollections.observableArrayList(username);
        return owners;
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
                // User confirmed - move task to completed
                moveTaskToCompleted(task);
            } else {
                // User cancelled - uncheck the checkbox
                task.setCompleted(false);
            }
        }
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
        inviteStackPane.setVisible(true);
        mainAnchorPane.setVisible(true);
        invitePane.setVisible(true);
        notifiedPane.setVisible(false);
    }
    
    @FXML
    private void handleInviteFriend() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlertWithType(Alert.AlertType.WARNING, "Invalid Email", "Please enter an email address.");
            return;
        }
        
        if (!isValidEmail(email)) {
            showAlertWithType(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        }
        
        // Simulate sending invitation
        invitePane.setVisible(false);
        notifiedPane.setVisible(true);
        emailField.clear();
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
    
    private void checkAndShowDueDateAlert() {
        if (username == null || username.isBlank() || currentWorkspaceID <= 0) {
            return;
        }
        
        List<TaskModel> allTasks = TaskDAO.getTasksByWorkspace(username, currentWorkspaceID);
        List<TaskModel> urgentTasks = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
        for (TaskModel task : allTasks) {
            // Skip completed tasks
            if ("Done".equalsIgnoreCase(task.getStatus())) {
                continue;
            }
            
            String dueDateStr = task.getDueDate();
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                try {
                    LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
                    
                    // Check if task is due today or tomorrow
                    if (dueDate.isEqual(today) || dueDate.isEqual(tomorrow)) {
                        urgentTasks.add(task);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing date for task: " + task.getTask() + " - " + e.getMessage());
                }
            }
        }
        
        if (!urgentTasks.isEmpty()) {
            showUrgentTasksAlert(urgentTasks);
        }
    }

    private void showUrgentTasksAlert(List<TaskModel> urgentTasks) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Urgent Tasks Alert");
        alert.setHeaderText("You have tasks due soon!");
        
        StringBuilder content = new StringBuilder();
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
        
        // Add today's tasks
        if (!todayTasks.isEmpty()) {
            content.append("📅 DUE TODAY:\n");
            for (TaskModel task : todayTasks) {
                content.append("• ").append(task.getTask())
                       .append(" (").append(task.getPriority()).append(" priority)")
                       .append("\n");
            }
            content.append("\n");
        }
        
        // Add tomorrow's tasks
        if (!tomorrowTasks.isEmpty()) {
            content.append("⏰ DUE TOMORROW:\n");
            for (TaskModel task : tomorrowTasks) {
                content.append("• ").append(task.getTask())
                       .append(" (").append(task.getPriority()).append(" priority)")
                       .append("\n");
            }
        }
        
        alert.setContentText(content.toString());
        
        // Set icon for alert
        setAlertIcon(alert);
        
        // Make the alert resizable and larger
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(450);
        alert.getDialogPane().setPrefHeight(300);
        
        alert.showAndWait();
    }

    public void setWorkspaceName(String workspaceName) {
        if (workspaceName != null && !workspaceName.trim().isEmpty()) {
            workspaceTitle.setText(workspaceName.trim());
        }
    }
    

    public String getWorkspaceName() {
        return workspaceTitle.getText();
    }
}