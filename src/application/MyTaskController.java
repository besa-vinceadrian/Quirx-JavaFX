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

    @FXML private TableView<MyTask> ongoingTableView;
    @FXML private TableColumn<MyTask, String> ongoingTaskColumn;
    @FXML private TableColumn<MyTask, String> ongoingDueDateColumn;
    @FXML private TableColumn<MyTask, String> ongoingPriorityColumn;
    @FXML private TableColumn<MyTask, String> ongoingWorkspaceColumn;

    @FXML private TableView<MyTask> notStartedTableView;
    @FXML private TableColumn<MyTask, String> notStartedTaskColumn;
    @FXML private TableColumn<MyTask, String> notStartedDueDateColumn;
    @FXML private TableColumn<MyTask, String> notStartedPriorityColumn;
    @FXML private TableColumn<MyTask, String> notStartedWorkspaceColumn;

    @FXML private TableView<MyTask> completedTableView;
    @FXML private TableColumn<MyTask, String> completedTaskColumn;
    @FXML private TableColumn<MyTask, String> completedDueDateColumn;
    @FXML private TableColumn<MyTask, String> completedPriorityColumn;
    @FXML private TableColumn<MyTask, String> completedWorkspaceColumn;

    private String username;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // ONGOING columns
        ongoingTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        ongoingDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        ongoingPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        ongoingWorkspaceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getWorkspaceName() != null ? cellData.getValue().getWorkspaceName() : "N/A"
            )
        );

        // NOT STARTED columns
        notStartedTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        notStartedDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        notStartedPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        notStartedWorkspaceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getWorkspaceName() != null ? cellData.getValue().getWorkspaceName() : "N/A"
            )
        );

        // COMPLETED columns
        completedTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        completedDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        completedPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        completedWorkspaceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getWorkspaceName() != null ? cellData.getValue().getWorkspaceName() : "N/A"
            )
        );
    }

    public void setUsername(String username) {
        this.username = username;
        loadUserTasks(username);
    }

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
