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
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.util.converter.DefaultStringConverter;
import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;

public class WorkspaceController implements Initializable {

    @FXML private TableView<Task> tableView;
    @FXML private TableColumn<Task, String> taskColumn;
    @FXML private TableColumn<Task, String> ownerColumn;
    @FXML private TableColumn<Task, Task.Status> statusColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, Task.Priority> priorityColumn;
    @FXML private TableColumn<Task, Boolean> completedColumn;

    @FXML private TableView<Task> completedTable;
    @FXML private TableColumn<Task, String> taskColumnCompleted;
    @FXML private TableColumn<Task, String> ownerColumnCompleted;
    @FXML private TableColumn<Task, String> statusColumnCompleted;
    @FXML private TableColumn<Task, String> dueDateColumnCompleted;
    @FXML private TableColumn<Task, String> priorityColumnCompleted;
    
    @FXML private Button inviteAgain;
    @FXML private Button inviteButton;
    @FXML private AnchorPane invitePane;
    @FXML private AnchorPane mainAnchorPane;
    @FXML private AnchorPane notifiedPane;
    @FXML private StackPane inviteStackPane;
    @FXML private TextField emailField;
    @FXML private Label workspaceTitle;
    @FXML private Button addTaskButton;
    @FXML private Button deleteTaskButton;

    private final ObservableList<Task> data = FXCollections.observableArrayList();
    private final ObservableList<Task> completedData = FXCollections.observableArrayList();
    
    public void setWorkspaceName(String name) {
        if (workspaceTitle != null) {
            workspaceTitle.setText(name);
        }
    }

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

    @FXML
    private void handleDeleteTask(ActionEvent event) {
        Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        
        if (selectedTask != null) {
            // Remove from both tables
            data.remove(selectedTask);
            completedData.remove(selectedTask);
            
            // Ensure there's always at least one empty task
            if (data.isEmpty()) {
                handleAddTask(event);
            }
        } else {
            showNoSelectionAlert();
        }
    }

    private void showNoSelectionAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Selection");
        alert.setHeaderText(null);
        alert.setContentText("Please select a task to delete.");
        alert.showAndWait();
    }
    
    @FXML
    private void handleDeleteAll() {
        // Clear all items from the completed table
        completedTable.getItems().clear();
        
    }

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

    // Helper methods
    private TextFieldTableCell<Task, String> createCenteredTextFieldCell(TableColumn<Task, String> column) {
        TextFieldTableCell<Task, String> cell = new TextFieldTableCell<>(new DefaultStringConverter());
        cell.setStyle("-fx-alignment: CENTER;");
        return cell;
    }

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
            data.add(Task.createEmptyTask());
        } else {
            task.setTask(newValue);
        }
    }

    private void addCompletedListener(Task task) {
        task.completedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                data.remove(task);
                completedData.add(task);
            } else {
                completedData.remove(task);
                data.add(task);
            }
        });
    }
    
    // switching thru invite pane
    @FXML
    void handleInviteFriend(ActionEvent event) {
        showNotifiedPane();
    }

    @FXML
    void handleReturnClick(MouseEvent event) {
        emailField.clear();
        showWorkspace();
    }

    @FXML
    void inviteButton(ActionEvent event) {
        showInvitePane();
    }

    @FXML
    void inviteAgain(ActionEvent event) {
        emailField.clear(); 
        showInvitePane();
    }

    @FXML
    void handleContinue(ActionEvent event) {
        emailField.clear(); 
        showWorkspace();
    }

    private void showInvitePane() {
        inviteStackPane.setVisible(true);
        mainAnchorPane.setVisible(true);
        invitePane.setVisible(true);
        notifiedPane.setVisible(false);
    }

    private void showNotifiedPane() {
        inviteStackPane.setVisible(true);
        mainAnchorPane.setVisible(true);
        invitePane.setVisible(false);
        notifiedPane.setVisible(true);
    }

    private void showWorkspace() {
        inviteStackPane.setVisible(false);
        mainAnchorPane.setVisible(false);
        invitePane.setVisible(false);
        notifiedPane.setVisible(false);  
    }
}