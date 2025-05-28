package application;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Task model class representing a task in the workspace application.
 * Uses JavaFX properties for data binding with UI components.
 */
public class TaskModel {

    private final IntegerProperty taskID = new SimpleIntegerProperty(); // For database
    private final IntegerProperty workspaceID = new SimpleIntegerProperty(); // For workspace grouping
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    private final StringProperty taskTitle = new SimpleStringProperty();
    private final StringProperty userOwner = new SimpleStringProperty();
    private final StringProperty taskStatus = new SimpleStringProperty("Not Started");
    private final StringProperty dueDate = new SimpleStringProperty();
    private final StringProperty taskPriority = new SimpleStringProperty("Low");

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /** Constructor for a new Task (default: not completed). */
    public TaskModel(String title, String owner, String status, String dueDate, String priority) {
        this.taskTitle.set(title != null ? title : "");
        this.userOwner.set(owner != null ? owner : "");
        this.taskStatus.set(status != null ? status : "Not Started");
        this.dueDate.set(dueDate != null ? dueDate : "");
        this.taskPriority.set(priority != null ? priority : "Low");
    }

    /** Constructor including completion state. */
    public TaskModel(String title, String owner, String status, String dueDate, String priority, boolean completed) {
        this(title, owner, status, dueDate, priority);
        this.completed.set(completed);
    }

    // === DAO-compatible getter aliases ===
    
    public String getTask() {
        return taskTitle.get() != null ? taskTitle.get() : "";
    }

    public String getOwner() {
        return userOwner.get() != null ? userOwner.get() : "";
    }

    public String getStatus() {
        return taskStatus.get() != null ? taskStatus.get() : "Not Started";
    }

    public String getDueDate() {
        return dueDate.get() != null ? dueDate.get() : "";
    }

    public String getPriority() {
        return taskPriority.get() != null ? taskPriority.get() : "Low";
    }


    public String getTaskTitle() {
        return getTask();
    }

    public String getUserOwner() {
        return getOwner();
    }

    public String getTaskStatus() {
        return getStatus();
    }

    public String getTaskPriority() {
        return getPriority();
    }

    // === Property Getters and Setters ===

    public int getTaskID() {
        return taskID.get();
    }

    public void setTaskID(int id) {
        this.taskID.set(id);
    }

    public IntegerProperty taskIDProperty() {
        return taskID;
    }

    public int getWorkspaceID() {
        return workspaceID.get();
    }

    public void setWorkspaceID(int value) {
        workspaceID.set(value);
    }

    public IntegerProperty workspaceIDProperty() {
        return workspaceID;
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public void setCompleted(boolean value) {
        completed.set(value);
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    public void setTask(String value) {
        taskTitle.set(value != null ? value : "");
    }

    public StringProperty taskProperty() {
        return taskTitle;
    }

    public void setOwner(String value) {
        userOwner.set(value != null ? value : "");
    }

    public StringProperty ownerProperty() {
        return userOwner;
    }


    public void setStatus(String value) {
        taskStatus.set(value != null ? value : "Not Started");
    }

    public StringProperty statusProperty() {
        return taskStatus;
    }

    public void setDueDate(String value) {
        dueDate.set(value != null ? value : "");
    }

    public StringProperty dueDateProperty() {
        return dueDate;
    }

    public void setPriority(String value) {
        taskPriority.set(value != null ? value : "Low");
    }

    public StringProperty priorityProperty() {
        return taskPriority;
    }

    // === Utility Methods ===

    public boolean isValid() {
        return !getTask().trim().isEmpty() && !getOwner().trim().isEmpty();
    }

    public boolean isOverdue() {
        if (isCompleted() || getDueDate().isEmpty()) return false;

        try {
            LocalDate due = LocalDate.parse(getDueDate(), FORMATTER);
            return due.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public String getFormattedDueDate() {
        if (getDueDate().isEmpty()) return "No due date";

        try {
            LocalDate date = LocalDate.parse(getDueDate(), FORMATTER);
            return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (DateTimeParseException e) {
            return getDueDate();
        }
    }

    public boolean isHighPriority() {
        return "High".equalsIgnoreCase(getPriority());
    }

    public TaskModel copy() {
        TaskModel t = new TaskModel(getTask(), getOwner(), getStatus(), getDueDate(), getPriority(), isCompleted());
        t.setTaskID(getTaskID());
        t.setWorkspaceID(getWorkspaceID());
        return t;
    }

    @Override
    public String toString() {
        return String.format("Task{task='%s', owner='%s', status='%s', dueDate='%s', priority='%s', completed=%s}",
                getTask(), getOwner(), getStatus(), getDueDate(), getPriority(), isCompleted());
    }
    
    // === Compatibility Setters for UI/DAO integration ===

    public void setTaskTitle(String title) {
        setTask(title);
    }

    public void setUserOwner(String owner) {
        setOwner(owner);
    }

    public void setTaskStatus(String status) {
        setStatus(status);
    }

    public void setTaskPriority(String priority) {
        setPriority(priority);
    }

    public void setDueDateFormatted(String date) {
        setDueDate(date);
    }
    
}
