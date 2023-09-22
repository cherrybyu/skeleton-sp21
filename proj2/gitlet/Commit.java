package gitlet;

// TODO: any imports you need here
import static gitlet.Utils.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    private String parentId;
    private String parentId2;
    private Date timestamp;
    private String message;
    private HashMap blobs = new HashMap();


    /* TODO: fill in the rest of this class. */
    public Commit(String parentId, String parentId2, String message, HashMap blobs, Date timestamp) {
        this.parentId = parentId;
        this.parentId2 = parentId2;
        this.message = message;
        this.blobs = blobs;
        this.timestamp = timestamp;
    }

    public String getParentId() {
        return parentId;
    }

    public String getParentId2 () {
        return parentId2;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
