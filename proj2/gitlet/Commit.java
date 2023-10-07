package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *
 *  @author Hannah Nguyen
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    private String parentId;
    private String parentId2;
    private String timestamp;
    private String message;
    private HashMap<String, String> blobs;

    public Commit(
            String parentId,
            String parentId2,
            String message,
            HashMap<String, String> blobs,
            String timestamp) {
        this.parentId = parentId;
        this.parentId2 = parentId2;
        this.message = message;
        this.blobs = blobs;
        this.timestamp = timestamp;
    }

    public String getParentId() {
        return parentId;
    }

    public String getParentId2() {
        return parentId2;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
    /** returns file names mapped to their sha1 hash */
    public HashMap<String, String> getBlobs() {
        return blobs;
    }
}
