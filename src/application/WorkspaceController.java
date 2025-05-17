package application;

import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class WorkspaceController implements Initializable {

	@FXML private TableView<Task> tableView;
	@FXML private TableColumn<Task, String> taskColumn;
	@FXML private TableColumn<Task, String> ownerColumn;
	@FXML private TableColumn<Task, String> statusColumn;
	@FXML private TableColumn<Task, String> dueDateColumn;
	@FXML private TableColumn<Task, String> priorityColumn;;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Link columns to model properties
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        
        // Add some data
        ObservableList<Task> data = FXCollections.observableArrayList(
            new Task("Fix bug", "Alice", "Open", "2025-05-17", "High"),
            new Task("Write report", "Bob", "In Progress", "2025-05-20", "Medium"),
            new Task("UI", "Nor", "Done", "2025-05-10", "Low")
        );
        tableView.setItems(data);
    }
}