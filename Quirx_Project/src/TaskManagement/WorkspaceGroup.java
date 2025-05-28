package TaskManagement;

public class WorkspaceGroup {
    private int id;
    private String name;

    public WorkspaceGroup (int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

