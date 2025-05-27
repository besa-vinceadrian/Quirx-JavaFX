package application;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Task model class representing a task in the workspace application.
 * Uses JavaFX properties for data binding with UI components.
 */
public class Task {
    private final BooleanProperty completed;
    private final StringProperty task;
    private final StringProperty owner;
    private final StringProperty status;
    private final StringProperty dueDate;
    private final StringProperty priority;
    
    /**
     * Constructor to create a new Task with all properties.
     * 
     * @param task The task description
     * @param owner The person responsible for the task
     * @param status Current status of the task
     * @param dueDate Due date for the task (as string)
     * @param priority Priority level of the task
     */
    public Task(String task, String owner, String status, String dueDate, String priority) {
        this.completed = new SimpleBooleanProperty(false);
        this.task = new SimpleStringProperty(task);
        this.owner = new SimpleStringProperty(owner);
        this.status = new SimpleStringProperty(status);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.priority = new SimpleStringProperty(priority);
    }
    
    /**
     * Alternative constructor for creating a task that's already completed.
     */
    public Task(String task, String owner, String status, String dueDate, String priority, boolean completed) {
        this(task, owner, status, dueDate, priority);
        this.completed.set(completed);
    }
    
    // Completed property methods
    public boolean isCompleted() { 
        return completed.get(); 
    }
    
    public void setCompleted(boolean completed) { 
        this.completed.set(completed); 
    }
    
    public BooleanProperty completedProperty() { 
        return completed; 
    }
    
    // Task property methods
    public String getTask() { 
        return task.get(); 
    }
    
    public void setTask(String task) { 
        this.task.set(task != null ? task : ""); 
    }
    
    public StringProperty taskProperty() { 
        return task; 
    }
    
    // Owner property methods
    public String getOwner() { 
        return owner.get(); 
    }
    
    public void setOwner(String owner) { 
        this.owner.set(owner != null ? owner : ""); 
    }
    
    public StringProperty ownerProperty() { 
        return owner; 
    }
    
    // Status property methods
    public String getStatus() { 
        return status.get(); 
    }
    
    public void setStatus(String status) { 
        this.status.set(status != null ? status : "Not Started"); 
    }
    
    public StringProperty statusProperty() { 
        return status; 
    }
    
    // Due date property methods
    public String getDueDate() { 
        return dueDate.get(); 
    }
    
    public void setDueDate(String dueDate) { 
        this.dueDate.set(dueDate != null ? dueDate : ""); 
    }
    
    public StringProperty dueDateProperty() { 
        return dueDate; 
    }
    
    // Priority property methods
    public String getPriority() { 
        return priority.get(); 
    }
    
    public void setPriority(String priority) { 
        this.priority.set(priority != null ? priority : "Medium"); 
    }
    
    public StringProperty priorityProperty() { 
        return priority; 
    }
    
    /**
     * Utility method to check if the task is overdue.
     * Note: This assumes the due date is in YYYY-MM-DD format.
     * 
     * @return true if the task is overdue, false otherwise
     */
    public boolean isOverdue() {
        if (isCompleted() || getDueDate().isEmpty()) {
            return false;
        }
        
        try {
            java.time.LocalDate dueLocalDate = java.time.LocalDate.parse(getDueDate());
            return dueLocalDate.isBefore(java.time.LocalDate.now());
        } catch (Exception e) {
            // If date parsing fails, assume not overdue
            return false;
        }
    }
    
    /**
     * Get a formatted display string for the due date.
     * 
     * @return formatted due date string
     */
    public String getFormattedDueDate() {
        if (getDueDate().isEmpty()) {
            return "No due date";
        }
        
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(getDueDate());
            return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return getDueDate(); // Return original if parsing fails
        }
    }
    
    /**
     * Check if this task has high priority.
     * 
     * @return true if priority is "High"
     */
    public boolean isHighPriority() {
        return "High".equals(getPriority());
    }
    
    /**
     * Get a summary string representation of the task.
     * 
     * @return formatted task summary
     */
    @Override
    public String toString() {
        return String.format("Task{task='%s', owner='%s', status='%s', dueDate='%s', priority='%s', completed=%s}",
                getTask(), getOwner(), getStatus(), getDueDate(), getPriority(), isCompleted());
    }
    
    /**
     * Create a copy of this task.
     * 
     * @return new Task instance with same values
     */
    public Task copy() {
        return new Task(getTask(), getOwner(), getStatus(), getDueDate(), getPriority(), isCompleted());
    }
    
    /**
     * Validate if the task has all required fields filled.
     * 
     * @return true if task is valid
     */
    public boolean isValid() {
        return !getTask().trim().isEmpty() && !getOwner().trim().isEmpty();
    }
}