package application;

import javafx.beans.property.*;

public class Task {
    private final StringProperty task;
    private final StringProperty owner;
    private final ObjectProperty<Status> status;
    private final StringProperty dueDate;
    private final ObjectProperty<Priority> priority;
    private final BooleanProperty completed; // ✅ Added checkbox property

    // === Enums for status and priority with CSS class support ===
    public enum Status {
        EMPTY("", ""),
        NOT_STARTED("Not Started", "status-not_started"),
        ONGOING("Ongoing", "status-ongoing"),
        DONE("Done", "status-done");

        private final String displayName;
        private final String styleClass;

        Status(String displayName, String styleClass) {
            this.displayName = displayName;
            this.styleClass = styleClass;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }

    public enum Priority {
        EMPTY("", ""),
        LOW("Low", "priority-low"),
        MEDIUM("Medium", "priority-medium"),
        HIGH("High", "priority-high");

        private final String displayName;
        private final String styleClass;

        Priority(String displayName, String styleClass) {
            this.displayName = displayName;
            this.styleClass = styleClass;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }

    // === Constructor ===
    public Task(String task, String owner, Status status, String dueDate, Priority priority) {
        this.task = new SimpleStringProperty(task);
        this.owner = new SimpleStringProperty(owner);
        this.status = new SimpleObjectProperty<>(status);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.priority = new SimpleObjectProperty<>(priority);
        this.completed = new SimpleBooleanProperty(false); // default unchecked
    }

    // === Static factory for empty task ===
    public static Task createEmptyTask() {
        return new Task("", "", Status.EMPTY, "", Priority.EMPTY);
    }

    // === Getters ===
    public String getTask() { return task.get(); }
    public String getOwner() { return owner.get(); }
    public Status getStatus() { return status.get(); }
    public String getDueDate() { return dueDate.get(); }
    public Priority getPriority() { return priority.get(); }
    public boolean isCompleted() { return completed.get(); }

    // === Property getters ===
    public StringProperty taskProperty() { return task; }
    public StringProperty ownerProperty() { return owner; }
    public ObjectProperty<Status> statusProperty() { return status; }
    public StringProperty dueDateProperty() { return dueDate; }
    public ObjectProperty<Priority> priorityProperty() { return priority; }
    public BooleanProperty completedProperty() { return completed; }

    // === Setters ===
    public void setTask(String value) { task.set(value); }
    public void setOwner(String value) { owner.set(value); }
    public void setStatus(Status value) { status.set(value); }
    public void setDueDate(String value) { dueDate.set(value); }
    public void setPriority(Priority value) { priority.set(value); }
    public void setCompleted(boolean value) { completed.set(value); }

    // === Convenience string-based setters for deserialization ===
    public void setStatus(String value) {
        try {
            status.set(Status.valueOf(value.replace(" ", "_").toUpperCase()));
        } catch (IllegalArgumentException e) {
            status.set(Status.EMPTY);
        }
    }

    public void setPriority(String value) {
        try {
            priority.set(Priority.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            priority.set(Priority.EMPTY);
        }
    }

    // === CSS Style class accessors ===
    public String getStatusStyleClass() {
        return getStatus() != null ? getStatus().getStyleClass() : "";
    }

    public String getPriorityStyleClass() {
        return getPriority() != null ? getPriority().getStyleClass() : "";
    }
}
