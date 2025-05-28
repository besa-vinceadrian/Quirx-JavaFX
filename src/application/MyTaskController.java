package application;

import TaskManagement.MyTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    public void initialize() {
        // Make all tables non-clickable/non-selectable
        ongoingTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<MyTask> row = new javafx.scene.control.TableRow<>();
            row.setMouseTransparent(true);
            return row;
        });
        
        notStartedTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<MyTask> row = new javafx.scene.control.TableRow<>();
            row.setMouseTransparent(true);
            return row;
        });
        
        completedTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<MyTask> row = new javafx.scene.control.TableRow<>();
            row.setMouseTransparent(true);
            return row;
        });

        // ONGOING columns
        ongoingTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        ongoingDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        ongoingDueDateColumn.setCellFactory(column -> createCenteredCell());
        
        ongoingPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        ongoingPriorityColumn.setCellFactory(column -> createPriorityCenteredCell());
        
        ongoingWorkspaceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getWorkspaceName() != null ? cellData.getValue().getWorkspaceName() : "N/A"
            )
        );
        ongoingWorkspaceColumn.setCellFactory(column -> createCenteredCell());

        // NOT STARTED columns
        notStartedTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        notStartedDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        notStartedDueDateColumn.setCellFactory(column -> createCenteredCell());
        
        notStartedPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        notStartedPriorityColumn.setCellFactory(column -> createPriorityCenteredCell());
        
        notStartedWorkspaceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getWorkspaceName() != null ? cellData.getValue().getWorkspaceName() : "N/A"
            )
        );
        notStartedWorkspaceColumn.setCellFactory(column -> createCenteredCell());

        // COMPLETED columns
        completedTaskColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
        completedDueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDueDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER));
            } else {
                return new SimpleStringProperty("");
            }
        });
        completedDueDateColumn.setCellFactory(column -> createCenteredCell());
        
        completedPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("taskPriority"));
        completedPriorityColumn.setCellFactory(column -> createPriorityCenteredCell());
        
        completedWorkspaceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getWorkspaceName() != null ? cellData.getValue().getWorkspaceName() : "N/A"
            )
        );
        completedWorkspaceColumn.setCellFactory(column -> createCenteredCell());
    }

    private TableCell<MyTask, String> createCenteredCell() {
        return new TableCell<MyTask, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        };
    }

    private TableCell<MyTask, String> createPriorityCenteredCell() {
        return new TableCell<MyTask, String>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);
                    
                    // Set background color based on priority with center alignment
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
    }

    public void setUsername(String username) {
        this.username = username;
        loadUserTasks(username);
    }

    private void loadUserTasks(String userName) {
        List<MyTask> allTasks = MyTask.getTasksByUser(userName);

        // Filter tasks by status
        List<MyTask> ongoingTasks = allTasks.stream()
            .filter(task -> "IN PROGRESS".equalsIgnoreCase(task.getTaskStatus()))
            .collect(Collectors.toList());

        List<MyTask> notStartedTasks = allTasks.stream()
            .filter(task -> "NOT STARTED".equalsIgnoreCase(task.getTaskStatus()))
            .collect(Collectors.toList());

        List<MyTask> completedTasks = allTasks.stream()
            .filter(task -> "DONE".equalsIgnoreCase(task.getTaskStatus()))
            .collect(Collectors.toList());

        // Apply priority queue sorting to each list
        List<MyTask> sortedOngoingTasks = sortTasksByPriority(ongoingTasks);
        List<MyTask> sortedNotStartedTasks = sortTasksByPriority(notStartedTasks);
        List<MyTask> sortedCompletedTasks = sortTasksByPriority(completedTasks);

        // Set the sorted items to the table views
        ongoingTableView.setItems(FXCollections.observableArrayList(sortedOngoingTasks));
        notStartedTableView.setItems(FXCollections.observableArrayList(sortedNotStartedTasks));
        completedTableView.setItems(FXCollections.observableArrayList(sortedCompletedTasks));
    }

    /**
     * Sorts tasks using a priority queue based on priority level and due date
     * Priority order: High > Medium > Low
     * Within same priority: Earlier due date comes first
     */
    private List<MyTask> sortTasksByPriority(List<MyTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        // Create a priority queue with custom comparator
        PriorityQueue<MyTask> priorityQueue = new PriorityQueue<>(new TaskPriorityComparator());
        
        // Add all tasks to the priority queue
        priorityQueue.addAll(tasks);
        
        // Extract tasks from priority queue to maintain sorted order
        List<MyTask> sortedTasks = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            sortedTasks.add(priorityQueue.poll());
        }
        
        return sortedTasks;
    }

    /**
     * Custom comparator for task priority sorting
     * Sorts by priority first (High > Medium > Low), then by due date (earlier first)
     */
    private static class TaskPriorityComparator implements Comparator<MyTask> {
        @Override
        public int compare(MyTask task1, MyTask task2) {
            // First, compare by priority
            int priority1 = getPriorityValue(task1.getTaskPriority());
            int priority2 = getPriorityValue(task2.getTaskPriority());
            
            if (priority1 != priority2) {
                return Integer.compare(priority2, priority1); // Higher priority first (descending)
            }
            
            // If priorities are equal, compare by due date
            LocalDate date1 = task1.getDueDate();
            LocalDate date2 = task2.getDueDate();
            
            // Handle null dates - tasks with no due date go to the end
            if (date1 == null && date2 == null) {
                return 0;
            } else if (date1 == null) {
                return 1; // task1 goes after task2
            } else if (date2 == null) {
                return -1; // task1 goes before task2
            }
            
            // Both dates are non-null, compare them (earlier date first)
            return date1.compareTo(date2);
        }
        
        /**
         * Converts priority string to numeric value for comparison
         * High = 3, Medium = 2, Low = 1, Unknown = 0
         */
        private int getPriorityValue(String priority) {
            if (priority == null) {
                return 0;
            }
            
            switch (priority.toLowerCase()) {
                case "high":
                    return 3;
                case "medium":
                    return 2;
                case "low":
                    return 1;
                default:
                    return 0;
            }
        }
    }

    /**
     * Refresh tasks for current user - useful for updating the view after changes
     */
    public void refreshTasks() {
        if (username != null && !username.isEmpty()) {
            loadUserTasks(username);
        }
    }

    /**
     * Get the current username
     */
    public String getUsername() {
        return username;
    }
}