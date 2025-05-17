package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class WorkspaceController implements Initializable {

    @FXML private TableView<Task> tableView;
    @FXML private TableColumn<Task, String> taskColumn;
    @FXML private TableColumn<Task, String> ownerColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, Boolean> completedColumn;

    @FXML private TableView<Task> completedTable;
    @FXML private TableColumn<Task, String> taskColumnCompleted;
    @FXML private TableColumn<Task, String> ownerColumnCompleted;
    @FXML private TableColumn<Task, String> statusColumnCompleted;
    @FXML private TableColumn<Task, String> dueDateColumnCompleted;
    @FXML private TableColumn<Task, String> priorityColumnCompleted;

    private final ObservableList<Task> data = FXCollections.observableArrayList();
    private final ObservableList<Task> completedData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup To-Do TableView columns
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));

        tableView.setEditable(true);
        taskColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        taskColumn.setEditable(true);

        // Use CheckBoxTableCell for the checkbox column
        completedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completedColumn));

        // Add listener to handle checkbox changes for each task added to 'data'
        data.addListener((javafx.collections.ListChangeListener.Change<? extends Task> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Task t : change.getAddedSubList()) {
                        addCompletedListener(t);
                    }
                }
            }
        });

        // Editing logic: allow creating a new task by editing empty task name row
        taskColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            String newValue = event.getNewValue();
            if (task.getOwner() == null || task.getOwner().isEmpty()) {
                // New task input; create a full task and add an empty row for next input
                Task newTask = new Task(
                    newValue,
                    "Me",
                    "Open",
                    LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    "Medium"
                );
                data.set(event.getTablePosition().getRow(), newTask);
                data.add(new Task("", "", "", "", ""));
            } else {
                task.setTask(newValue);
            }
        });

        // Add initial empty row for input
        data.add(new Task("", "", "", "", ""));
        tableView.setItems(data);

        // Setup Completed TableView columns
        taskColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("task"));
        ownerColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("owner"));
        statusColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("status"));
        dueDateColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        priorityColumnCompleted.setCellValueFactory(new PropertyValueFactory<>("priority"));

        completedTable.setItems(completedData);
    }

    // Add listener for completed property changes to move tasks between lists
    private void addCompletedListener(Task task) {
        task.completedProperty().addListener((obs, wasCompleted, isNowCompleted) -> {
            if (isNowCompleted) {
                if (data.contains(task)) {
                    data.remove(task);
                    completedData.add(task);
                }
            } else {
                // Optional: allow moving task back to To-Do if unchecked in completed table
                if (completedData.contains(task)) {
                    completedData.remove(task);
                    data.add(task);
                }
            }
        });
    }
}
