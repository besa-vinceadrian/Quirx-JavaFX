package application;

import javafx.beans.property.*;

/**
 * Represents a task in the Quirx task management system.
 * Each task has a description, owner, status, due date, priority level,
 * and a completion flag (used with a checkbox in the UI).
 */
public class Task {
	
	/** The task description. */
	private final StringProperty task;
	
	/** The owner of the task. */
    private final StringProperty owner;
    
    /** The status of the task. */
    private final ObjectProperty<Status> status;
    
    /** The due date of the task. */
    private final StringProperty dueDate;
    
    /** The priority level of the task. */
    private final ObjectProperty<Priority> priority;
    
    /** Indicates whether the task is completed (for checkbox use). */
    private final BooleanProperty completed; // ✅ Added checkbox property

    /**
     * Represents the status of a task, with a display name and associated CSS style class.
     * Used to indicate progress state in the UI.
     */
    public enum Status {
    	/** No status specified. */
    	EMPTY("", ""),
        
    	/** Task has not yet started. */
    	NOT_STARTED("Not Started", "status-not_started"),
        
    	/** Task is currently in progress. */
    	ONGOING("Ongoing", "status-ongoing"),
        
    	/** Task has been completed. */
    	DONE("Done", "status-done");

    	/** Display name for the status. */
    	private final String displayName;
    	
    	/** CSS style class for the status. */
        private final String styleClass;

        /**
         * Constructs a new Status enum value.
         *
         * @param displayName the display name
         * @param styleClass the associated CSS class
         */
        Status(String displayName, String styleClass) {
            this.displayName = displayName;
            this.styleClass = styleClass;
        }

        @Override
        public String toString() {
            return displayName;
        }

        /**
         * Gets the CSS style class associated with the status.
         * @return the CSS style class
         */
        public String getStyleClass() {
            return styleClass;
        }
    }

    /**
     * Represents the priority of a task, with a display name and associated CSS style class.
     * Used to visually differentiate urgency levels in the UI.
     */
    public enum Priority {
    	/** No priority specified. */
    	EMPTY("", ""),
        
    	/** Low priority. */
    	LOW("Low", "priority-low"),
        
    	/** Medium priority. */
    	MEDIUM("Medium", "priority-medium"),
        
    	/** High priority. */
    	HIGH("High", "priority-high");

    	/** Display name for the priority. */
    	private final String displayName;
    	
    	/** CSS style class for the priority. */
        private final String styleClass;

        /**
         * Constructs a new Priority enum value.
         *
         * @param displayName the display name
         * @param styleClass the associated CSS class
         */
        Priority(String displayName, String styleClass) {
            this.displayName = displayName;
            this.styleClass = styleClass;
        }

        @Override
        public String toString() {
            return displayName;
        }

        /**
         * Gets the CSS style class associated with the priority.
         * @return the CSS style class
         */
        public String getStyleClass() {
            return styleClass;
        }
    }

    /**
     * Constructs a Task with specified values.
     *
     * @param task the description of the task
     * @param owner the person responsible for the task
     * @param status the current status of the task
     * @param dueDate the due date of the task
     * @param priority the priority level of the task
     */
    public Task(String task, String owner, Status status, String dueDate, Priority priority) {
        this.task = new SimpleStringProperty(task);
        this.owner = new SimpleStringProperty(owner);
        this.status = new SimpleObjectProperty<>(status);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.priority = new SimpleObjectProperty<>(priority);
        this.completed = new SimpleBooleanProperty(false); // default unchecked
    }

    /**
     * Creates and returns a Task with default empty values.
     *
     * @return an empty Task instance
     */
    public static Task createEmptyTask() {
        return new Task("", "", Status.EMPTY, "", Priority.EMPTY);
    }

    // === Getters ===
    /**
     * Returns the task description.
     *
     * @return the task description
     */
    public String getTask() { return task.get(); }
    
    /**
     * Returns the task owner.
     *
     * @return the task owner
     */
    public String getOwner() { return owner.get(); }
    
    /**
     * Returns the task status.
     *
     * @return the current task status
     */
    public Status getStatus() { return status.get(); }
    
    /**
     * Returns the due date of the task.
     *
     * @return the task due date
     */
    public String getDueDate() { return dueDate.get(); }
    
    /**
     * Returns the task priority.
     *
     * @return the task priority
     */
    public Priority getPriority() { return priority.get(); }
    
    /**
     * Returns whether the task is completed.
     *
     * @return true if the task is marked as completed, false otherwise
     */
    public boolean isCompleted() { return completed.get(); }

    // === Property getters ===
    public StringProperty taskProperty() { return task; }
    public StringProperty ownerProperty() { return owner; }
    public ObjectProperty<Status> statusProperty() { return status; }
    public StringProperty dueDateProperty() { return dueDate; }
    public ObjectProperty<Priority> priorityProperty() { return priority; }
    public BooleanProperty completedProperty() { return completed; }

    // === Setters ===
    /**
     * Sets the task description.
     *
     * @param value the new task description
     */
    public void setTask(String value) { task.set(value); }
    
    /**
     * Sets the task owner.
     *
     * @param value the new task owner
     */
    public void setOwner(String value) { owner.set(value); }
    
    /**
     * Sets the task status.
     *
     * @param value the new task status
     */
    public void setStatus(Status value) { status.set(value); }
    
    /**
     * Sets the task due date.
     *
     * @param value the new due date
     */
    public void setDueDate(String value) { dueDate.set(value); }
    
    /**
     * Sets the task priority.
     *
     * @param value the new priority
     */
    public void setPriority(Priority value) { priority.set(value); }
    
    /**
     * Marks the task as completed or not.
     *
     * @param value whether the task is completed
     */
    public void setCompleted(boolean value) { completed.set(value); }

    // === Convenience string-based setters for deserialization ===
    /**
     * Sets the task status from a string.
     * @param value the status string (e.g., "Not Started", "Done")
     */
    public void setStatus(String value) {
        try {
            status.set(Status.valueOf(value.replace(" ", "_").toUpperCase()));
        } catch (IllegalArgumentException e) {
            status.set(Status.EMPTY);
        }
    }

    /**
     * Sets the task priority from a string.
     * @param value the priority string (e.g., "Low", "High")
     */
    public void setPriority(String value) {
        try {
            priority.set(Priority.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            priority.set(Priority.EMPTY);
        }
    }

    // === CSS Style class accessors ===
    /**
     * Gets the CSS class for the task's current status.
     * @return the CSS class string for the status
     */
    public String getStatusStyleClass() {
        return getStatus() != null ? getStatus().getStyleClass() : "";
    }

    /**
     * Gets the CSS class for the task's current priority.
     * @return the CSS class string for the priority
     */
    public String getPriorityStyleClass() {
        return getPriority() != null ? getPriority().getStyleClass() : "";
    }
}
