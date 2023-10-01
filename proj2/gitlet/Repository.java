package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
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
    public HashMap<String, String> branches = new HashMap<>();
    public String activeBranch;


    /* TODO: fill in the rest of this class. */

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message `initial commit` and time
     * stamp.
     */
    public void initCommand() throws IOException {
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
        }

        CWD.mkdir();
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();

        Commit initCommit = new Commit(null, null, "initial commit", null, Date.from(Instant.EPOCH));
        saveCommit(initCommit);
        branches.put("master", headCommit);
        activeBranch = "master";
    }

    public void addCommand(String fileName) throws IOException {
        if (!plainFilenamesIn(CWD).contains(fileName)) {
            Utils.message("File does not exist.");
        }

        File originalFile = Utils.join(CWD, fileName);
        byte[] fileContents = readContents(originalFile);

        Blob newBlob = new Blob(fileName, fileContents);
        FileData fileData = Helpers.getObjectAndId(newBlob);

        String blobId = fileData.id;
        byte[] serializedBlob = fileData.serialized;

        if (!plainFilenamesIn(BLOB_DIR).contains(blobId)) {
            stagingArea.put(fileName, blobId);
            File blobFile = Utils.join(BLOB_DIR, blobId);
            blobFile.createNewFile();
            writeContents(blobFile, serializedBlob);
        }
    }

    public void commitCommand(String message) throws IOException {
        if (message == null || message.isEmpty()) {
            Utils.message("Please enter a commit message.");
        }

        if (stagingArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
        }

        HashMap<String, String> blobs = new HashMap<>(stagingArea);
        stagingArea.clear();

        Date timeStamp = Date.from(Instant.now());
        Commit newCommit = new Commit(headCommit, null, message, blobs, timeStamp);
        saveCommit(newCommit);
    }

    public void rmCommand(String fileName) {
        stagingArea.remove(fileName);

        File currCommitFile = Utils.join(COMMIT_DIR, headCommit);
        Commit currCommit = readObject(currCommitFile, Commit.class);

        if (currCommit.getBlobs() != null) {
            if (currCommit.getBlobs().containsKey(fileName) && plainFilenamesIn(CWD).contains(fileName)) {
                Utils.restrictedDelete(fileName);
                removalArea.add(fileName);
            } else if (currCommit.getBlobs().containsKey(fileName) && !plainFilenamesIn(GITLET_DIR).contains(fileName)) {
                removalArea.add(fileName);
            }

            if (!stagingArea.containsKey(fileName) && !currCommit.getBlobs().containsKey(fileName)) {
                Utils.message("No reason to remove the file.");
            }
        }
    }

    public void logCommand() {
        CommitData currCommit = commitHistory.get(headCommit);
        String currCommitId = headCommit;
        while (currCommit != null) {
            Utils.message("===");
            Utils.message("commit " + currCommitId);
            Utils.message("Date: " + currCommit.commitTimestamp);
            Utils.message(currCommit.commitMessage);

            currCommitId = currCommit.commitParentId;
            currCommit = commitHistory.get(currCommit.commitParentId);
        }
    }

    public void globalLogCommand() {
        List<String> files = Utils.plainFilenamesIn(COMMIT_DIR);

        assert files != null;
        for (String file : files) {
            CommitData currCommit = commitHistory.get(file);

            Utils.message("===");
            Utils.message("commit " + file);
            Utils.message("Date: " + currCommit.commitTimestamp);
            Utils.message(currCommit.commitMessage);
        }
    }

    public void findCommand(String message) {
        CommitData currCommit = commitHistory.get(headCommit);
        String currCommitId = headCommit;
        ArrayList<String> messagesToPrint = new ArrayList<>();

        while (currCommit != null) {
            if (currCommit.commitMessage.contains(message)) {
                messagesToPrint.add(currCommitId);
            }
            currCommitId = currCommit.commitParentId;
            currCommit = commitHistory.get(currCommit.commitParentId);
        }
        if (messagesToPrint.isEmpty()) {
            Utils.message("Found no commit with that message.");
        }
        for (String msg: messagesToPrint) {
            Utils.message(msg);
        }
    }

    public void statusCommand() {
        Utils.message("=== Branches ===");
        String[] branchKeys = branches.keySet().toArray(new String[0]);
        Arrays.sort(branchKeys);
        for (String key: branchKeys) {
            if (key.equals(activeBranch)) {
                Utils.message("*" + key);
            } else {
                Utils.message(key);
            }
        }

        Utils.message("=== Staged Files ===");
        if (!stagingArea.isEmpty()) {
            String[] stagingKeys = stagingArea.keySet().toArray(new String[0]);
            Arrays.sort(stagingKeys);
            for (String key : stagingKeys) {
                Utils.message(key);
            }
        }

        Utils.message("=== Removed Files ===");
        if (!removalArea.isEmpty()) {
            Collections.sort(removalArea);
            for (String file: removalArea) {
                Utils.message(file);
            }
        }

        Utils.message("=== Modifications Not Staged For Commit ===");
        //TODO: DO THIS LATER

        Utils.message("=== Untracked Files ===");
        //TODO: DO THIS LATER
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

        File commitFile = Utils.join(COMMIT_DIR, commitId);
        commitFile.createNewFile();
        writeContents(commitFile, serializedCommit);

        CommitData commitData = new CommitData(commit.getParentId(), commit.getTimestamp(), commit.getMessage());
        commitHistory.put(commitId, commitData);
        headCommit = commitId;
        removalArea.clear();
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