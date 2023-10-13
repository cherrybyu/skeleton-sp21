package gitlet;

import java.io.Serializable;

class CommitData implements Serializable {
    /** Commit parent ID */
    private final String commitParentId;
    /** Commit second parent ID */
    private final String commitParentId2;
    /** Commit timestamp */
    private final String commitTimestamp;
    /** Commit message */
    private final String commitMessage;

    /** Class constructor */
    CommitData(String commitParentId,
               String commitParentId2,
               String commitTimestamp,
               String commitMessage) {
        this.commitParentId = commitParentId;
        this.commitParentId2 = commitParentId2;
        this.commitTimestamp = commitTimestamp;
        this.commitMessage = commitMessage;
    }

    /** Gets parent ID */
    public String getCommitParentId() {
        return commitParentId;
    }

    /** Gets second parent ID */
    public String getCommitParentId2() {
        return commitParentId2;
    }

    /** Gets commit timestamp */
    public String getCommitTimestamp() {
        return commitTimestamp;
    }

    /** Gets commit message */
    public String getCommitMessage() {
        return commitMessage;
    }
}
