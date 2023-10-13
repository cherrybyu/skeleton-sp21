package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *
 *  @author Hannah Nguyen
 */
public class Commit implements Serializable {
    /** Parent ID */
    private String parentId;
    /** Second parent ID from merge */
    private String parentId2;
    /** Time of commit creation */
    private String timestamp;
    /** Commit message */
    private String message;
    /** Mapping of file names to their Blob ID */
    private HashMap<String, String> blobs;

    /** Class constructor */
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

    /** Gets first parent ID */
    public String getParentId() {
        return parentId;
    }

    /** Gets second parent ID */
    public String getParentId2() {
        return parentId2;
    }

    /** Gets timestamp */
    public String getTimestamp() {
        return timestamp;
    }

    /** Gets Message */
    public String getMessage() {
        return message;
    }

    /** Gets blobs */
    public HashMap<String, String> getBlobs() {
        return blobs;
    }
}
