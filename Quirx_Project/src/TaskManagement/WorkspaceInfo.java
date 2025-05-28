package TaskManagement;

public class WorkspaceInfo {
    private int workspaceId;
    private String workspaceName;

    public WorkspaceInfo(int workspaceId, String workspaceName) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
    }

    public int getWorkspaceId() {
        return workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }
}
