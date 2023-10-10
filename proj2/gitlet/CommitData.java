package gitlet;

import java.io.Serializable;
class CommitData implements Serializable {
    private final String commitParentId;
    private final String commitParentId2;
    private final String commitTimestamp;
    private final String commitMessage;

    CommitData(String commitParentId, String commitParentId2, String commitTimestamp, String commitMessage) {
        this.commitParentId = commitParentId;
        this.commitParentId2 = commitParentId2;
        this.commitTimestamp = commitTimestamp;
        this.commitMessage = commitMessage;
    }

    public String getCommitParentId() {
        return commitParentId;
    }
    public String getCommitParentId2() {
        return commitParentId2;
    }
    public String getCommitTimestamp() {
        return commitTimestamp;
    }
    public String getCommitMessage() {
        return commitMessage;
    }
}
