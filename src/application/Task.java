package application;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Task {
    private final SimpleStringProperty task;
    private final SimpleStringProperty owner;
    private final SimpleStringProperty status;
    private final SimpleStringProperty dueDate;
    private final SimpleStringProperty priority;
    private final SimpleBooleanProperty completed; // ✅ Added for checkbox

    public Task(String task, String owner, String status, String dueDate, String priority) {
        this.task = new SimpleStringProperty(task);
        this.owner = new SimpleStringProperty(owner);
        this.status = new SimpleStringProperty(status);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.priority = new SimpleStringProperty(priority);
        this.completed = new SimpleBooleanProperty(false); // default unchecked
    }

    // Getters and setters
    public String getTask() { return task.get(); }
    public String getOwner() { return owner.get(); }
    public String getStatus() { return status.get(); }
    public String getDueDate() { return dueDate.get(); }
    public String getPriority() { return priority.get(); }
    public boolean isCompleted() { return completed.get(); } // Getter for checkbox

    // Property getters
    public SimpleStringProperty taskProperty() { return task; }
    public SimpleStringProperty ownerProperty() { return owner; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty dueDateProperty() { return dueDate; }
    public SimpleStringProperty priorityProperty() { return priority; }
    public SimpleBooleanProperty completedProperty() { return completed; } // Property getter for checkbox

    // Setters
    public void setTask(String value) { task.set(value); }
    public void setOwner(String value) { owner.set(value); }
    public void setStatus(String value) { status.set(value); }
    public void setDueDate(String value) { dueDate.set(value); }
    public void setPriority(String value) { priority.set(value); }
    public void setCompleted(boolean value) { completed.set(value); } // Setter for checkbox
}
