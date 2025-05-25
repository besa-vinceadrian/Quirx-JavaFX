package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.util.converter.DefaultStringConverter;
import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Optional;

/**
 * Controller for the workspace interface of the Quirx application.
 * Manages tasks, including adding, deleting, editing, and marking tasks as completed.
 * Also handles UI logic for inviting friends and switching between panes.
 */
public class WorkspaceController implements Initializable {

	/**
	 * Default constructor for Workspace controller.
	 */
	public WorkspaceController() {
	    // Default constructor
	}
	
	/** Table displaying active tasks. */
	@FXML private TableView<Task> tableView;
	
	/** Column for task titles in active table. */
    @FXML private TableColumn<Task, String> taskColumn;
    
    /** Column for task owners in active table. */
    @FXML private TableColumn<Task, String> ownerColumn;
    
    /** Column for task statuses in active table. */
    @FXML private TableColumn<Task, Task.Status> statusColumn;
    
    /** Column for task due dates in active table. */
    @FXML private TableColumn<Task, String> dueDateColumn;
    
    /** Column for task priorities in active table. */
    @FXML private TableColumn<Task, Task.Priority> priorityColumn;
    
    /** Column indicating task completion in active table. */
    @FXML private TableColumn<Task, Boolean> completedColumn;

    /** Table displaying completed tasks. */
    @FXML private TableView<Task> completedTable;
    
    /** Column for task titles in completed table. */
    @FXML private TableColumn<Task, String> taskColumnCompleted;
    
    /** Column for task owners in completed table. */
    @FXML private TableColumn<Task, String> ownerColumnCompleted;
    
    /** Column for task statuses in completed table. */
    @FXML private TableColumn<Task, String> statusColumnCompleted;
    
    /** Column for task due dates in completed table. */
    @FXML private TableColumn<Task, String> dueDateColumnCompleted;
    
    /** Column for task priorities in completed table. */
    @FXML private TableColumn<Task, String> priorityColumnCompleted;
    
    /** Button to resend invite. */
    @FXML private Button inviteAgain;
    
    /** Button to initiate invite. */
    @FXML private Button inviteButton;
    
    /** Pane containing invite form. */
    @FXML private AnchorPane invitePane;
    
    /** Main workspace anchor pane. */
    @FXML private AnchorPane mainAnchorPane;
    
    /** Notification pane shown after invite is sent. */
    @FXML private AnchorPane notifiedPane;
    
    /** StackPane wrapping workspace and invite panes. */
    @FXML private StackPane inviteStackPane;
    
    /** TextField to input email addresses. */
    @FXML private TextField emailField;
    
    /** Label displaying the workspace title. */
    @FXML private Label workspaceTitle;
    
    /** Button to add a new task. */
    @FXML private Button addTaskButton;
    
    /** Button to delete the selected task. */
    @FXML private Button deleteTaskButton;

    /** Observable list containing active tasks. */
    private final ObservableList<Task> data = FXCollections.observableArrayList();
    
    /** Observable list containing completed tasks. */
    private final ObservableList<Task> completedData = FXCollections.observableArrayList();
    
    /**
     * Sets the name of the current workspace in the UI.
     * 
     * @param name The name to display.
     */
    public void setWorkspaceName(String name) {
        if (workspaceTitle != null) {
            workspaceTitle.setText(name);
        }
    }

    /**
     * Initializes the controller and sets up table views.
     * 
     * @param url Not used.
     * @param rb  Not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTodoTable();
        setupCompletedTable();
        
        // Initial blank row
        data.add(Task.createEmptyTask());
        tableView.setItems(data);
        completedTable.setItems(completedData);
        
        // Disable delete button when no task is selected
        deleteTaskButton.disableProperty().bind(
            tableView.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    /**
     * Handles the event of adding a new task to the table.
     * 
     * @param event The ActionEvent triggered by the add button.
     */
    @FXML
    private void handleAddTask(ActionEvent event) {
        Task newTask = Task.createEmptyTask();
        data.add(newTask);
        addCompletedListener(newTask);
        
        Platform.runLater(() -> {
            tableView.scrollTo(newTask);
            tableView.getSelectionModel().select(newTask);
            tableView.edit(data.size() - 1, taskColumn);
        });
    }

    /**
     * Handles the deletion of a selected task with confirmation.
     * 
     * @param event The ActionEvent triggered by the delete button.
     */
    @FXML
    private void handleDeleteTask(ActionEvent event) {
    	Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        
        if (selectedTask != null) {
            // Remove from both tables
        	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	    	alert.setTitle("Delete Task");
	        alert.setHeaderText(null);
	        alert.setContentText("Are you sure you want to delete the task ?");
	        
	        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
	        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
	        
	        Optional<ButtonType> result = alert.showAndWait();
	        if (result.isPresent() && result.get() == ButtonType.OK) {     	
	            data.remove(selectedTask);
	            completedData.remove(selectedTask);
	        }
            
            // Ensure there's always at least one empty task
            if (data.isEmpty()) {
                handleAddTask(event);
            }
        } else {
            showNoSelectionAlert();
        }
    }

    /**
     * Displays a warning when no task is selected for deletion.
     */
    private void showNoSelectionAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Selection");
        alert.setHeaderText(null);
        alert.setContentText("Please select a task to delete.");
        alert.showAndWait();
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
        
    }
    
    /**
     * Deletes all tasks from the completed task table after confirmation.
     */
    @FXML
    private void handleDeleteAll() {
        // Clear all items from the completed table
    	Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Completed Task");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete all completed task ?");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));
    	
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {     	
        	completedTable.getItems().clear();
        }       
    }

    /**
     * Configures the task table for active (to-do) tasks.
     */
    private void setupTodoTable() {
        tableView.setEditable(true);
        tableView.getSelectionModel().setCellSelectionEnabled(true);

        // Cell value factories
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        
        // Custom cell factories
        taskColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ownerColumn.setCellFactory(this::createCenteredTextFieldCell);
        dueDateColumn.setCellFactory(this::createCenteredTextFieldCell);
        
        statusColumn.setCellFactory(this::createStatusComboBoxCell);
        priorityColumn.setCellFactory(this::createPriorityComboBoxCell);
        completedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completedColumn));

        // Edit commit handlers
        taskColumn.setOnEditCommit(event -> handleTaskEdit(event));
        ownerColumn.setOnEditCommit(event -> event.getRowValue().setOwner(event.getNewValue()));
        dueDateColumn.setOnEditCommit(event -> event.getRowValue().setDueDate(event.getNewValue()));
        statusColumn.setOnEditCommit(event -> event.getRowValue().setStatus(event.getNewValue()));
        priorityColumn.setOnEditCommit(event -> event.getRowValue().setPriority(event.getNewValue()));

        // Add listener for new tasks
        data.addListener((javafx.collections.ListChangeListener.Change<? extends Task> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Task t : change.getAddedSubList()) {
                        addCompletedListener(t);
                    }
                }
            }
        });
    }

    /**
     * Configures the table for completed tasks.
     */
    private void setupCompletedTable() {
        // Cell value factories using the same Task properties
        taskColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("task"));
        ownerColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("owner"));
        dueDateColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        statusColumnCompleted.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().toString()));
        priorityColumnCompleted.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPriority().toString()));

        // owner column
        ownerColumnCompleted.setCellFactory(column -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // due date column
        dueDateColumnCompleted.setCellFactory(column -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // priority column 
        priorityColumnCompleted.setCellFactory(column -> new TableCell<Task, String>() {
            private final Label label = new Label();
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    label.setText(priority);
                    label.getStyleClass().removeAll("priority-low", "priority-medium", "priority-high");
                    switch (priority) {
                        case "Low": label.getStyleClass().add("priority-low"); break;
                        case "Medium": label.getStyleClass().add("priority-medium"); break;
                        case "High": label.getStyleClass().add("priority-high"); break;
                    }
                    label.setMinHeight(Region.USE_PREF_SIZE);
                    label.setMinWidth(Region.USE_PREF_SIZE);
                    label.setPadding(new Insets(3, 10, 3, 10));
                    label.setStyle("-fx-alignment: CENTER;");
                    setGraphic(label);
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Status column 
        statusColumnCompleted.setCellFactory(column -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    getStyleClass().removeAll("status-not_started", "status-ongoing", "status-done");
                    switch (status) {
                        case "Not Started": getStyleClass().add("status-not_started"); break;
                        case "Ongoing": getStyleClass().add("status-ongoing"); break;
                        case "Done": getStyleClass().add("status-done"); break;
                    }
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    /**
     * Creates a centered text field cell for table columns.
     * 
     * @param column The column to apply the cell to.
     * @return A centered TextFieldTableCell.
     */
    private TextFieldTableCell<Task, String> createCenteredTextFieldCell(TableColumn<Task, String> column) {
        TextFieldTableCell<Task, String> cell = new TextFieldTableCell<>(new DefaultStringConverter());
        cell.setStyle("-fx-alignment: CENTER;");
        return cell;
    }

    /**
     * Creates a ComboBox cell for task status.
     * 
     * @param column The column to apply the cell to.
     * @return A ComboBoxTableCell for Task.Status.
     */
    private ComboBoxTableCell<Task, Task.Status> createStatusComboBoxCell(TableColumn<Task, Task.Status> column) {
        ComboBoxTableCell<Task, Task.Status> cell = new ComboBoxTableCell<>(Task.Status.values()) {
            @Override
            public void updateItem(Task.Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    getStyleClass().removeAll("status-not_started", "status-ongoing", "status-done");
                } else {
                    setText(status.toString());
                    getStyleClass().removeAll("status-not_started", "status-ongoing", "status-done");
                    getStyleClass().add(status.getStyleClass());
                }
            }
        };
        cell.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && !cell.isEmpty()) {
                tableView.edit(cell.getIndex(), statusColumn);
            }
        });
        return cell;
    }

    /**
     * Creates a ComboBox cell for task priority.
     * 
     * @param column The column to apply the cell to.
     * @return A ComboBoxTableCell for Task.Priority.
     */
    private ComboBoxTableCell<Task, Task.Priority> createPriorityComboBoxCell(TableColumn<Task, Task.Priority> column) {
        return new ComboBoxTableCell<Task, Task.Priority>(Task.Priority.values()) {
            private final Label label = new Label();
            @Override
            public void updateItem(Task.Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setGraphic(null);
                    setText(null);
                    label.getStyleClass().clear();
                } else {
                    label.setText(priority.toString());
                    label.getStyleClass().removeAll("priority-low", "priority-medium", "priority-high");
                    label.getStyleClass().add(priority.getStyleClass());
                    label.setMinHeight(Region.USE_PREF_SIZE);
                    label.setMinWidth(Region.USE_PREF_SIZE);
                    label.setPadding(new Insets(3, 10, 3, 10));
                    setGraphic(label);
                    setText(null);
                }
            }
        };
    }

    /**
     * Handles editing of task name and fills in default values if needed.
     * 
     * @param event The cell edit event triggered on the task column.
     */
    private void handleTaskEdit(TableColumn.CellEditEvent<Task, String> event) {
        Task task = event.getRowValue();
        String newValue = event.getNewValue();
        if (task.getOwner() == null || task.getOwner().isEmpty()) {
            Task newTask = new Task(
                newValue,
                "Owner",
                Task.Status.NOT_STARTED,
                "YYYY-MM-DD",
                Task.Priority.LOW
            );
            data.set(event.getTablePosition().getRow(), newTask);
        } else {
            task.setTask(newValue);
        }
    }

    /**
     * Adds a listener to track completion changes for a task.
     * 
     * @param task The task to listen on.
     */
    private void addCompletedListener(Task task) {
        task.completedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Ask for confirmation
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Complete Task");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to mark this task as completed?");
                
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image("file:QuirxImages/LogoYellow.png"));

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    data.remove(task);
                    completedData.add(task);
                } else {
                    // Revert checkbox state (uncheck)
                    task.setCompleted(false);
                }
            }
        });
    }

    
    /**
     * Handles switching to the notified pane after inviting a friend.
     * 
     * @param event The ActionEvent from the invite button.
     */
    @FXML
    void handleInviteFriend(ActionEvent event) {
        showNotifiedPane();
    }

    /**
     * Handles clicking the return button from the invite pane.
     * 
     * @param event The mouse event from the return button.
     */
    @FXML
    void handleReturnClick(MouseEvent event) {
        emailField.clear();
        showWorkspace();
    }

    /**
     * Displays the invite form pane.
     * 
     * @param event The ActionEvent from the invite button.
     */
    @FXML
    void inviteButton(ActionEvent event) {
        showInvitePane();
    }

    /**
     * Handles clicking the invite again button from the notification pane.
     * 
     * @param event The ActionEvent from the invite again button.
     */
    @FXML
    void inviteAgain(ActionEvent event) {
        emailField.clear(); 
        showInvitePane();
    }

    /**
     * Handles clicking the continue button from the notification pane.
     * 
     * @param event The ActionEvent from the continue button.
     */
    @FXML
    void handleContinue(ActionEvent event) {
        emailField.clear(); 
        showWorkspace();
    }

    /**
     * Shows the invite pane and hides others.
     */
    private void showInvitePane() {
        inviteStackPane.setVisible(true);
        mainAnchorPane.setVisible(true);
        invitePane.setVisible(true);
        notifiedPane.setVisible(false);
    }

    /**
     * Shows the notification pane and hides others.
     */
    private void showNotifiedPane() {
        inviteStackPane.setVisible(true);
        mainAnchorPane.setVisible(true);
        invitePane.setVisible(false);
        notifiedPane.setVisible(true);
    }

    /**
     * Hides all overlay panes and shows the main workspace.
     */
    private void showWorkspace() {
        inviteStackPane.setVisible(false);
        mainAnchorPane.setVisible(false);
        invitePane.setVisible(false);
        notifiedPane.setVisible(false);  
    }
}