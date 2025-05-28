package application;

import TaskManagement.MyTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MyTaskController {

    // ONGOING table and columns
    @FXML
    private TableView<MyTask> ongoingTableView;
    @FXML
    private TableColumn<MyTask, String> ongoingTaskColumn;
    @FXML
    private TableColumn<MyTask, String> ongoingDueDateColumn;
    @FXML
    private TableColumn<MyTask, String> ongoingPriorityColumn;
    @FXML
    private TableColumn<MyTask, String> ongoingTeamColumn;      // You may need to set up team data accordingly
    @FXML
    private TableColumn<MyTask, String> ongoingAssigneeColumn;  // Likewise for assignee

    // NOT STARTED table and columns
    @FXML
    private TableView<MyTask> notStartedTableView;
    @FXML
    private TableColumn<MyTask, String> notStartedTaskColumn;
    @FXML
    private TableColumn<MyTask, String> notStartedDueDateColumn;
    @FXML
    private TableColumn<MyTask, String> notStartedPriorityColumn;
    @FXML
    private TableColumn<MyTask, String> notStartedTeamColumn;
    @FXML
    private TableColumn<MyTask, String> notStartedAssigneeColumn;

    // COMPLETED table and columns
    @FXML
    private TableView<MyTask> completedTableView;
    @FXML
    private TableColumn<MyTask, String> completedTaskColumn;
    @FXML
    private TableColumn<MyTask, String> completedDueDateColumn;
    @FXML
    private TableColumn<MyTask, String> completedPriorityColumn;
    @FXML
    private TableColumn<MyTask, String> completedTeamColumn;
    @FXML
    private TableColumn<MyTask, String> completedAssigneeColumn;

    private String username;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // Set up ONGOING columns
        ongoingTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        ongoingDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        ongoingPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        // TODO: Setup ongoingTeamColumn and ongoingAssigneeColumn if you have these data in MyTask
        ongoingTeamColumn.setCellValueFactory(cellData -> new SimpleStringProperty("N/A"));
        ongoingAssigneeColumn.setCellValueFactory(cellData -> new SimpleStringProperty("N/A"));

        // Set up NOT STARTED columns
        notStartedTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        notStartedDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        notStartedPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        notStartedTeamColumn.setCellValueFactory(cellData -> new SimpleStringProperty("N/A"));
        notStartedAssigneeColumn.setCellValueFactory(cellData -> new SimpleStringProperty("N/A"));

        // Set up COMPLETED columns
        completedTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        completedDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        completedPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        completedTeamColumn.setCellValueFactory(cellData -> new SimpleStringProperty("N/A"));
        completedAssigneeColumn.setCellValueFactory(cellData -> new SimpleStringProperty("N/A"));
    }

    /**
     * This method is called from outside after the controller is loaded,
     * to set the username and trigger the loading of tasks for this user.
     */
    public void setUsername(String username) {
        this.username = username;
        loadUserTasks(username);
    }

    /**
     * Loads tasks from DB for the given username,
     * separates them by status, and sets them in the respective TableViews.
     */
    private void loadUserTasks(String userName) {
        List<MyTask> allTasks = MyTask.getTasksByUser(userName);

        List<MyTask> ongoingTasks = allTasks.stream()
            .filter(task -> "IN PROGRESS".equalsIgnoreCase(task.getTaskStatus()))
            .collect(Collectors.toList());

        List<MyTask> notStartedTasks = allTasks.stream()
            .filter(task -> "NOT STARTED".equalsIgnoreCase(task.getTaskStatus()))
            .collect(Collectors.toList());

        List<MyTask> completedTasks = allTasks.stream()
            .filter(task -> "DONE".equalsIgnoreCase(task.getTaskStatus()))
            .collect(Collectors.toList());

        ongoingTableView.setItems(FXCollections.observableArrayList(ongoingTasks));
        notStartedTableView.setItems(FXCollections.observableArrayList(notStartedTasks));
        completedTableView.setItems(FXCollections.observableArrayList(completedTasks));
    }
}
