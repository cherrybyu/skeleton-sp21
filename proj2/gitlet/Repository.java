package gitlet;



import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public final File GITLET_DIR = join(CWD, ".gitlet");

    public File COMMIT_DIR = join(GITLET_DIR, "commits");
    public File BLOB_DIR = join(GITLET_DIR, "blobs");
    public HashMap<String, String> stagingArea = new HashMap<>();
    public ArrayList<String> removalArea = new ArrayList<>();
    public HashMap<String, CommitData> commitHistory = new HashMap<>();
    private String headCommit;

    /* TODO: fill in the rest of this class. */

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message `initial commit` and time
     * stamp.
     */
    public void initCommand() throws IOException {
        //TODO: IF .gitlet directory already exists, throw error
        CWD.mkdir();
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();

        Commit initCommit = new Commit(null, null, "initial commit", null, Date.from(Instant.EPOCH));
        saveCommit(initCommit);
    }

    public void addCommand(String fileName) throws IOException {
        File originalFile = Utils.join(CWD, fileName);
        byte[] fileContents = readContents(originalFile);

        Blob newBlob = new Blob(fileName, fileContents);
        FileData fileData = Helpers.getObjectAndId(newBlob);

        String blobId = fileData.id;
        byte[] serializedBlob = fileData.serialized;

        if (!commitHistory.containsValue(blobId)) {
            stagingArea.put(fileName, blobId);
            File blobFile = Utils.join(BLOB_DIR, blobId.substring(0,6));
            blobFile.createNewFile();
            writeContents(blobFile, serializedBlob);
        } else {
            //TODO: throw error if sha1 already exists
        }
    }

    public void commitCommand(String message) throws IOException {
        HashMap<String, String> blobs = new HashMap<>();
        blobs.putAll(stagingArea);
        stagingArea.clear();

        Date timeStamp = Date.from(Instant.now());
        Commit newCommit = new Commit(headCommit, null, message, blobs, timeStamp);
        saveCommit(newCommit);

        System.out.println(commitHistory.get(headCommit).commitMessage);
        System.out.println(commitHistory.get(headCommit).commitTimestamp);
        System.out.println(commitHistory.get(headCommit).commitParentId);
    }

    public void rmCommand(String fileName) {
        if (stagingArea.containsKey(fileName)) {
            stagingArea.remove(fileName);
            System.out.println(fileName + "removed");
        }

        File currCommitFile = Utils.join(COMMIT_DIR, headCommit.substring(0,6));
        Commit currCommit = readObject(currCommitFile, Commit.class);
        System.out.println(plainFilenamesIn(CWD).contains(fileName));
        System.out.println(currCommit.getBlobs().containsKey(fileName) );

        //TODO: WHY ISN'T IT WORKING
        if (currCommit.getBlobs().containsKey(fileName) && plainFilenamesIn(CWD).contains(fileName)) {
            Utils.restrictedDelete(fileName);
            removalArea.add(fileName);
            System.out.println(fileName);
            System.out.println(removalArea.get(0));
        } else if (currCommit.getBlobs().containsKey(fileName) && !plainFilenamesIn(GITLET_DIR).contains(fileName)) {
            removalArea.add(fileName);
        }

        if (!stagingArea.containsKey(fileName) && !currCommit.getBlobs().containsKey(fileName)) {
            //TODO: error if there is no file to be removed
        }
    }

//    private Commit createCommit(String parentId, String parentId2, String message, HashMap blobs, Date timestamp) {
//        Commit newCommit = new Commit(parentId, parentId2, message, blobs, timestamp);
//        return newCommit;
//    }


    /**
     * saves commit into a file in the .gitlet/commits directory. adds reference to
     * said commit to commitHistory and assigns headCommit to the commit's id
     * */
    private void saveCommit(Commit commit) throws IOException {
        FileData fileData = Helpers.getObjectAndId(commit);

        String commitId = fileData.id;
        byte[] serializedCommit = fileData.serialized;

        File commitFile = Utils.join(COMMIT_DIR, commitId.substring(0,6));
        commitFile.createNewFile();
        writeContents(commitFile, serializedCommit);

        CommitData commitData = new CommitData(commit.getParentId(), commit.getTimestamp(), commit.getMessage());
        commitHistory.put(commitId, commitData);
        headCommit = commitId;
    }
    private void saveBlob(Blob blob) {

    }

    private void stageBlob(String fileName, Blob blob) {

    }

}

class CommitData implements Serializable {
    public String commitParentId;
    public Date commitTimestamp;
    public String commitMessage;

    public CommitData(String commitParentId, Date commitTimestamp, String commitMessage) {
        this.commitParentId = commitParentId;
        this.commitTimestamp = commitTimestamp;
        this.commitMessage = commitMessage;
    }
}

class FileData {
    String id;
    byte[] serialized;
    public FileData(String objectId, byte[] serializedObject) {
        this.id = objectId;
        this.serialized = serializedObject;
    }
}