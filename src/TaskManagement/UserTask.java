package TaskManagement;

import java.sql.Date;
import javafx.beans.property.*;

public class UserTask implements Comparable<UserTask> {

    public enum Status {
        DONE, IN_PROGRESS, NOT_STARTED;

        public String toDatabaseValue() {
            return switch (this) {
                case DONE -> "DONE";
                case IN_PROGRESS -> "IN PROGRESS";
                case NOT_STARTED -> "NOT STARTED";
            };
        }

        public static Status fromDatabaseValue(String dbValue) {
            return switch (dbValue.toUpperCase()) {
                case "DONE" -> DONE;
                case "IN PROGRESS" -> IN_PROGRESS;
                case "NOT STARTED" -> NOT_STARTED;
                default -> throw new IllegalArgumentException("Unknown status: " + dbValue);
            };
        }
    }

    public enum Priority {
        HIGH, MEDIUM, LOW;

        public static Priority fromString(String val) {
            return switch (val.toUpperCase()) {
                case "HIGH" -> HIGH;
                case "MEDIUM" -> MEDIUM;
                case "LOW" -> LOW;
                default -> throw new IllegalArgumentException("Unknown priority: " + val);
            };
        }

        public String toDatabaseValue() {
            return name();
        }
    }

    private final IntegerProperty taskID = new SimpleIntegerProperty();
    private final StringProperty taskTitle = new SimpleStringProperty();
    private final ObjectProperty<Status> taskStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<Date> dueDate = new SimpleObjectProperty<>();
    private final ObjectProperty<Priority> taskPriority = new SimpleObjectProperty<>();
    private final StringProperty userOwner = new SimpleStringProperty();
    private final IntegerProperty workspaceID = new SimpleIntegerProperty();
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    // Full constructor
    public UserTask(int taskID, String taskTitle, Status taskStatus, Date dueDate,
                    Priority taskPriority, String userOwner, int workspaceID, boolean completed) {
        this.taskID.set(taskID);
        this.taskTitle.set(taskTitle);
        this.taskStatus.set(taskStatus);
        this.dueDate.set(dueDate);
        this.taskPriority.set(taskPriority);
        this.userOwner.set(userOwner);
        this.workspaceID.set(workspaceID);
        this.completed.set(completed);
    }

    // Constructor without ID
    public UserTask(String taskTitle, Status taskStatus, Date dueDate,
                    Priority taskPriority, String userOwner, int workspaceID, boolean completed) {
        this(0, taskTitle, taskStatus, dueDate, taskPriority, userOwner, workspaceID, completed);
    }

    // Getters
    public int getTaskID() { return taskID.get(); }
    public String getTaskTitle() { return taskTitle.get(); }
    public Status getTaskStatus() { return taskStatus.get(); }
    public Date getDueDate() { return dueDate.get(); }
    public Priority getTaskPriority() { return taskPriority.get(); }
    public String getUserOwner() { return userOwner.get(); }
    public int getWorkspaceID() { return workspaceID.get(); }
    public boolean isCompleted() { return completed.get(); }

    // JavaFX Properties
    public IntegerProperty taskIDProperty() { return taskID; }
    public StringProperty taskTitleProperty() { return taskTitle; }
    public ObjectProperty<Status> taskStatusProperty() { return taskStatus; }
    public ObjectProperty<Date> dueDateProperty() { return dueDate; }
    public ObjectProperty<Priority> taskPriorityProperty() { return taskPriority; }
    public StringProperty userOwnerProperty() { return userOwner; }
    public IntegerProperty workspaceIDProperty() { return workspaceID; }
    public BooleanProperty completedProperty() { return completed; }

    // Computed String Properties for TableView
    public StringProperty taskStatusStringProperty() {
        return new SimpleStringProperty(taskStatus.get() != null ? taskStatus.get().toDatabaseValue() : "");
    }

    public StringProperty dueDateStringProperty() {
        return new SimpleStringProperty(dueDate.get() != null ? dueDate.get().toLocalDate().toString() : "");
    }

    public StringProperty taskPriorityStringProperty() {
        return new SimpleStringProperty(taskPriority.get() != null ? taskPriority.get().name() : "");
    }

    // Setters
    public void setTaskTitle(String taskTitle) { this.taskTitle.set(taskTitle); }
    public void setTaskStatus(Status taskStatus) { this.taskStatus.set(taskStatus); }
    public void setDueDate(Date dueDate) { this.dueDate.set(dueDate); }
    public void setTaskPriority(Priority taskPriority) { this.taskPriority.set(taskPriority); }
    public void setUserOwner(String userOwner) { this.userOwner.set(userOwner); }
    public void setWorkspaceID(int workspaceID) { this.workspaceID.set(workspaceID); }
    public void setCompleted(boolean completed) { this.completed.set(completed); }

    // Sorting logic
    @Override
    public int compareTo(UserTask o) {
        return Integer.compare(priorityValue(this.taskPriority.get()), priorityValue(o.taskPriority.get()));
    }

    private int priorityValue(Priority p) {
        return switch (p) {
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }
}
