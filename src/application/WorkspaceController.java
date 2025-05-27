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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class WorkspaceController implements Initializable {

    // FXML Components
    @FXML
    private void handleEditTask() {
        Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            showEditTaskDialog(selectedTask);
        } else {
            showAlert("No Selection", "Please select a task to edit.");
        }
    }
    
    private void showEditTaskDialog(Task taskToEdit) {
        // Create edit task dialog
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit task details:");
        
        // Create form fields with current values
        TextField taskField = new TextField(taskToEdit.getTask());
        taskField.setPromptText("Task description");
        
        // Changed from TextField to ComboBox for owner
        ComboBox<String> ownerCombo = new ComboBox<>();
        ownerCombo.getItems().addAll(getOwnerOptions());
        ownerCombo.setValue(taskToEdit.getOwner());
        ownerCombo.setEditable(true); // Allow custom input if needed
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Not Started", "Ongoing", "Done");
        statusCombo.setValue(taskToEdit.getStatus());
        
        // FIXED: Replace TextField with DatePicker for due date
        DatePicker dueDateField = new DatePicker();
        dueDateField.setPromptText("MM-DD-YY");
        // Set existing date if available
        if (taskToEdit.getDueDate() != null && !taskToEdit.getDueDate().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yy");
                LocalDate existingDate = LocalDate.parse(taskToEdit.getDueDate(), formatter);
                dueDateField.setValue(existingDate);
            } catch (Exception e) {
                // If date parsing fails, leave empty
            }
        }
        
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("Low", "Medium", "High");
        priorityCombo.setValue(taskToEdit.getPriority());
        
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
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // FIXED: Convert result with proper DatePicker handling
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Task(
                    taskField.getText().trim(),
                    ownerCombo.getValue() != null ? ownerCombo.getValue().trim() : "",
                    statusCombo.getValue(),
                    dueDateField.getValue() != null ? dueDateField.getValue().format(DateTimeFormatter.ofPattern("MM-dd-yy")) : "",
                    priorityCombo.getValue()
                );
            }
            return null;
        });
        
        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(editedTask -> {
            if (!editedTask.getTask().isEmpty()) {
                // Update the existing task with new values
                taskToEdit.setTask(editedTask.getTask());
                taskToEdit.setOwner(editedTask.getOwner());
                taskToEdit.setStatus(editedTask.getStatus());
                taskToEdit.setDueDate(editedTask.getDueDate());
                taskToEdit.setPriority(editedTask.getPriority());
                
                // Refresh the table to show updated values
                tableView.refresh();
            }
        });
    }
    
    @FXML private Label workspaceTitle;
    @FXML private TableView<Task> tableView;
    @FXML private TableView<Task> completedTable;
    
    // TO-DO Table Columns
    @FXML private TableColumn<Task, Boolean> completedColumn;
    @FXML private TableColumn<Task, String> taskColumn;
    @FXML private TableColumn<Task, String> ownerColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    
    // COMPLETED Table Columns
    @FXML private TableColumn<Task, String> taskColumnCompleted;
    @FXML private TableColumn<Task, String> ownerColumnCompleted;
    @FXML private TableColumn<Task, String> statusColumnCompleted;
    @FXML private TableColumn<Task, String> dueDateColumnCompleted;
    @FXML private TableColumn<Task, String> priorityColumnCompleted;
    
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
    private ObservableList<Task> todoTasks;
    private ObservableList<Task> completedTasks;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize data lists
        todoTasks = FXCollections.observableArrayList();
        completedTasks = FXCollections.observableArrayList();
        
        // Set up tables
        setupTodoTable();
        setupCompletedTable();
        
        // Set table data
        tableView.setItems(todoTasks);
        completedTable.setItems(completedTasks);
    }
    
    private void setupTodoTable() {
        // Set up columns for TO-DO table (non-editable except for completion checkbox)
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        completedColumn.setCellFactory(column -> {
            CheckBoxTableCell<Task, Boolean> cell = new CheckBoxTableCell<>();
            return cell;
        });
        completedColumn.setEditable(true);
        
        // All other columns are read-only - just display the data
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
        
        // OWNER COLUMN WITH CENTER ALIGNMENT
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        ownerColumn.setCellFactory(column -> {
            return new TableCell<Task, String>() {
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
            return new TableCell<Task, String>() {
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
                            case "ongoing":
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
            return new TableCell<Task, String>() {
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
            return new TableCell<Task, String>() {
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
        todoTasks.addListener((javafx.collections.ListChangeListener<Task>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Task task : change.getAddedSubList()) {
                        task.completedProperty().addListener((obs, oldVal, newVal) -> {
                            handleTaskCompletion(task, newVal);
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
            return new TableCell<Task, String>() {
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
            return new TableCell<Task, String>() {
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
	                        case "ongoing":
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
            return new TableCell<Task, String>() {
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
            return new TableCell<Task, String>() {
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
    
    @FXML
    private void handleAddTask() {
        // Create a new task dialog
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Enter task details:");
        
        // Create form fields
        TextField taskField = new TextField();
        taskField.setPromptText("Task description");
        
        // Changed from TextField to ComboBox for owner
        ComboBox<String> ownerCombo = new ComboBox<>();
        ownerCombo.getItems().addAll(getOwnerOptions());
        ownerCombo.setPromptText("Select owner");
        ownerCombo.setEditable(false); // Allow custom input if needed
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Not Started", "Ongoing", "Done");
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
                    showAlert("Validation Error", "Task description and owner cannot be empty");
                    event.consume(); // Prevent dialog from closing
                    return;
                }
                // Check if only Task description is empty
                else if (taskDescription.isEmpty()) {
                    showAlert("Validation Error", "Task description cannot be empty");
                    event.consume(); // Prevent dialog from closing
                    return;
                }
                // Check if only Owner is empty
                else if (ownerValue.isEmpty()) {
                    showAlert("Validation Error", "Owner cannot be empty");
                    event.consume(); // Prevent dialog from closing
                    return;
                }
                
                // If validation passes, allow the dialog to close normally
                
            } catch (Exception e) {
                // Handle any other unexpected errors
                showAlert("Error", "An unexpected error occurred while validating the task: " + e.getMessage());
                event.consume(); // Prevent dialog from closing
            }
        });
        
        // Simplified result converter - only creates task if dialog closes normally
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    String taskDescription = taskField.getText().trim();
                    String ownerValue = ownerCombo.getValue().trim();
                    
                    return new Task(
                        taskDescription,
                        ownerValue,
                        statusCombo.getValue(),
                        dueDateField.getValue() != null ? dueDateField.getValue().format(DateTimeFormatter.ofPattern("MM-dd-yy")) : "",
                        priorityCombo.getValue()
                    );
                    
                } catch (Exception e) {
                    showAlert("Error", "An unexpected error occurred while creating the task: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and handle result
        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(task -> {
            try {
                // Add task to the list
                todoTasks.add(task);
                // Listener will be added automatically by the list change listener in setupTodoTable
            } catch (Exception e) {
                showAlert("Error", "Failed to add task to the list: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get the list of owner options for the dropdown
     * You can customize this method to return owners from a database, file, or any other source
     * @return ObservableList of owner names
     */
    private ObservableList<String> getOwnerOptions() {
        // You can replace this with dynamic data from your application
        return FXCollections.observableArrayList(
            "John Doe",
            "Jane Smith", 
            "Mike Johnson",
            "Sarah Wilson",
            "David Brown",
            "Lisa Davis",
            "Chris Miller",
            "Emma Garcia"
        );
    }
    
    @FXML
    private void handleDeleteTask() {
        Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Task");
            alert.setHeaderText("Are you sure you want to delete this task?");
            alert.setContentText(selectedTask.getTask());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                todoTasks.remove(selectedTask);
            }
        } else {
            showAlert("No Selection", "Please select a task to delete.");
        }
    }
    
    @FXML
    private void handleDeleteAll() {
        if (!completedTasks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete All Completed Tasks");
            alert.setHeaderText("Are you sure you want to delete all completed tasks?");
            alert.setContentText("This action cannot be undone.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                completedTasks.clear();
            }
        } else {
            showAlert("No Tasks", "There are no completed tasks to delete.");
        }
    }
    
    private void handleTaskCompletion(Task task, boolean isCompleted) {
        if (isCompleted) {
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Complete Task");
            confirmAlert.setHeaderText("Mark task as completed?");
            confirmAlert.setContentText("Are you sure you want to mark \"" + task.getTask() + "\" as completed?\n\nThis will move the task to the completed section.");
            
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
    public void moveTaskToCompleted(Task task) {
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
            showAlert("Invalid Email", "Please enter an email address.");
            return;
        }
        
        if (!isValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address.");
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
        alert.showAndWait();
    }
    
    /**
     * Set the workspace name/title displayed at the top of the application
     * @param workspaceName The name to display
     */
    public void setWorkspaceName(String workspaceName) {
        if (workspaceName != null && !workspaceName.trim().isEmpty()) {
            workspaceTitle.setText(workspaceName.trim());
        }
    }
    
    /**
     * Get the current workspace name
     * @return The current workspace title
     */
    public String getWorkspaceName() {
        return workspaceTitle.getText();
    }
}