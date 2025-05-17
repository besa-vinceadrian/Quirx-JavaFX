package application; // <-- replace with your actual package

import javafx.beans.property.SimpleStringProperty;


	public class Task {
	    private final SimpleStringProperty task;
	    private final SimpleStringProperty owner;
	    private final SimpleStringProperty status;
	    private final SimpleStringProperty dueDate;
	    private final SimpleStringProperty priority;

	    public Task(String task, String owner, String status, String dueDate, String priority) {
	        this.task = new SimpleStringProperty(task);
	        this.owner = new SimpleStringProperty(owner);
	        this.status = new SimpleStringProperty(status);
	        this.dueDate = new SimpleStringProperty(dueDate);
	        this.priority = new SimpleStringProperty(priority);
	    }

	    public String getTask() { return task.get(); }
	    public String getOwner() { return owner.get(); }
	    public String getStatus() { return status.get(); }
	    public String getDueDate() { return dueDate.get(); }
	    public String getPriority() { return priority.get(); }
	}

