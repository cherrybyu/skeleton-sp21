package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
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
    /*TODO: add instance variables here.
      List all instance variables of the Repository class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    private final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    private final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commit directory */
    private File COMMIT_DIR = join(GITLET_DIR, "commits");
    /** The blob directory */
    private File BLOB_DIR = join(GITLET_DIR, "blobs");
    private HashMap<String, String> branches = new HashMap<>();
    private String activeBranch;
    private HashMap<String, CommitData> commitHistory = new HashMap<>();
    private HashMap<String, String> stagingArea = new HashMap<>();
    private ArrayList<String> removalArea = new ArrayList<>();
    private String headCommit;
    private SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");



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

        Date timeStamp = Date.from(Instant.EPOCH);
        String formattedDate = sdf.format(timeStamp);
        Commit initCommit = new Commit(null, null, "initial commit", new HashMap<>(), formattedDate);
        saveCommit(initCommit);
        branches.put("master", headCommit);
        activeBranch = "master";
    }

    public void addCommand(String fileName) throws IOException {
        try {
            if (!plainFilenamesIn(CWD).contains(fileName)) {
                throw new IllegalArgumentException("File does not exist");
            }

            if (removalArea.contains(fileName)) {
                removalArea.remove(fileName);
                return;
            }

            File originalFile = Utils.join(CWD, fileName);
            byte[] fileContents = readContents(originalFile);

            Blob newBlob = new Blob(fileName, fileContents);
            FileData fileData = Helpers.getObjectAndId(newBlob);

            String blobId = fileData.id;
            byte[] serializedBlob = fileData.serialized;

            File currCommitFile = Utils.join(COMMIT_DIR, headCommit);
            Commit currCommit = readObject(currCommitFile, Commit.class);
            HashMap currBlobs = currCommit.getBlobs();

            if (!plainFilenamesIn(BLOB_DIR).contains(blobId) && currBlobs.get(fileName) != blobId) {
                stagingArea.put(fileName, blobId);
                File blobFile = Utils.join(BLOB_DIR, blobId);
                blobFile.createNewFile();
                writeContents(blobFile, serializedBlob);
            }

        } catch (IllegalArgumentException e) {
            Utils.message(e.getMessage());
        }
    }

    public void commitCommand(String message) throws IOException {
        if (message == null || message.isEmpty()) {
            Utils.message("Please enter a commit message.");
            return;
        }

        if (stagingArea.isEmpty() && removalArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        HashMap<String, String> blobs = new HashMap<>(stagingArea);
        stagingArea.clear();

        Date timeStamp = Date.from(Instant.now());
        String formattedDate = sdf.format(timeStamp);
        Commit newCommit = new Commit(headCommit, null, message, blobs, formattedDate);
        saveCommit(newCommit);
    }

    public void rmCommand(String fileName) {
        try {
            if (stagingArea.containsKey(fileName)) {
                stagingArea.remove(fileName);
            } else {
                File currCommitFile = Utils.join(COMMIT_DIR, headCommit);
                Commit currCommit = readObject(currCommitFile, Commit.class);
                HashMap currBlobs = currCommit.getBlobs();

                if (currBlobs.containsKey(fileName)) {
                    if (plainFilenamesIn(CWD).contains(fileName)) {
                        Utils.restrictedDelete(fileName);
                        removalArea.add(fileName);
                    } else {
                        removalArea.add(fileName);
                    }
                } else {
                    throw new IllegalArgumentException("No reason to remove the file.");
                }
            }
        } catch (IllegalArgumentException e){
            Utils.message(e.getMessage());
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
            System.out.println();

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
            System.out.println();
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
        System.out.println();

        Utils.message("=== Staged Files ===");
        String[] stagingKeys = stagingArea.keySet().toArray(new String[0]);
        Arrays.sort(stagingKeys);
        for (String key : stagingKeys) {
            Utils.message(key);
        }
        System.out.println();

        Utils.message("=== Removed Files ===");
        Collections.sort(removalArea);
        for (String file: removalArea) {
            Utils.message(file);
        }
        System.out.println();

        Utils.message("=== Modifications Not Staged For Commit ===");
        //TODO: DO THIS LATER
        System.out.println();

        Utils.message("=== Untracked Files ===");
        //TODO: DO THIS LATER
        System.out.println();
    }

    public void checkoutCommand(String[] args) throws IOException {
        Commit currCommit;
        HashMap currBlobs;

        if (args.length == 3) {
            // `checkout -- [fileName]` command
            if (!Objects.equals(args[1], "--")) {
                Utils.message("Incorrect operands.");
                return;
            }

            String fileName = args[2];
            File currCommitFile = Utils.join(COMMIT_DIR, headCommit);
            currCommit = readObject(currCommitFile, Commit.class);
            currBlobs = currCommit.getBlobs();
            byte[] headFileContents = null;

            if (currBlobs.containsKey(fileName)) {
                Object headFileId = currBlobs.get(fileName);
                File headFile = Utils.join(BLOB_DIR, (String) headFileId);
                Blob headFileObject = readObject(headFile, Blob.class);
                headFileContents= headFileObject.getFileContents();
            } else {
                Utils.message("File does not exist in that commit.");
                return;
            }

            if (plainFilenamesIn(CWD).contains(fileName) && headFileContents != null) {
                File originalFile = Utils.join(CWD, fileName);
                Utils.writeContents(originalFile, headFileContents);
            } else {
                File newFile = Utils.join(CWD, fileName);
                newFile.createNewFile();
                Utils.writeContents(newFile, headFileContents);
            }
        } else if (args.length == 4) {
            // TODO: handle the `checkout [commit id] -- [fileName]` command
            if (!Objects.equals(args[2], "--")) {
                Utils.message("Incorrect operands.");
                return;
            }
            String commitId = args[1];
            String fileName = args[3];
            Blob currFileObject = null;
            byte[] currFileContents = null;

            if (commitHistory.containsKey(commitId)) {
                File currCommitFile = Utils.join(COMMIT_DIR, commitId);
                currCommit = readObject(currCommitFile, Commit.class);
                currBlobs = currCommit.getBlobs();

                if (currBlobs.containsKey(fileName)) {
                    Object currFileId = currBlobs.get(fileName);
                    File currFile = Utils.join(BLOB_DIR, (String) currFileId);
                    currFileObject = readObject(currFile, Blob.class);
                    currFileContents = currFileObject.getFileContents();
                } else {
                    Utils.message("File does not exist in that commit.");
                    return;
                }

                File originalFile = Utils.join(CWD, fileName);
                originalFile.createNewFile();
                Utils.writeContents(originalFile, currFileContents);
            } else {
                Utils.message("No commit with that id exists.");
            }
        } else if (args.length == 2) {
            // TODO: handle the `checkout [branchName]` command
            String branchName = args[1];

            if (!branches.containsKey(branchName)) {
                Utils.message("No such branch exists.");
                return;
            }

            if (Objects.equals(activeBranch, branchName)) {
                Utils.message("No need to checkout the current branch.");
                return;
            }

            File currCommitFile = Utils.join(COMMIT_DIR, headCommit);
            currCommit = readObject(currCommitFile, Commit.class);
            currBlobs = currCommit.getBlobs();
            Set currBlobKeys = currBlobs.keySet();

            List <String> workingFiles = plainFilenamesIn(CWD);
            if (workingFiles != null) {
                for (String file: workingFiles) {
                    if (!currBlobKeys.contains(file)) {
                        Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                        Utils.message(currBlobKeys.toString());
                        Utils.message(workingFiles.toString());

                        return;
                    }
                }
            }

            if (!stagingArea.isEmpty()) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }

            String branchHeadId = branches.get(branchName);
            File branchHeadCommitFile = Utils.join(COMMIT_DIR, branchHeadId);
            Commit branchHeadCommit = readObject(branchHeadCommitFile, Commit.class);

            HashMap branchHeadBlobs = branchHeadCommit.getBlobs();
            Set branchHeadBlobKeys = branchHeadBlobs.keySet();

            for (Object fileName: currBlobKeys) {
                if (!branchHeadBlobs.containsKey(fileName)) {
                    File toDelete = Utils.join(CWD, (String) fileName);
                    Utils.restrictedDelete(toDelete);
                }
            }

            for (Object fileName: branchHeadBlobKeys) {
                Object fileId = branchHeadBlobs.get(fileName);

                File currFile = Utils.join(BLOB_DIR, (String) fileId);
                Blob currFileObject = readObject(currFile, Blob.class);

                byte[] currFileContents = currFileObject.getFileContents();
                File newFile = Utils.join(CWD, (String) fileName);
                Utils.message((String) fileName);
                newFile.createNewFile();
                Utils.writeContents(newFile, currFileContents);
            }

            stagingArea.clear();
            activeBranch = branchName;
            headCommit = branchHeadId;
        }
    }

    public void branchCommand(String branchName) {
        if (branches.containsKey(branchName)) {
            Utils.message("A branch with that name already exists.");
            return;
        }
        branches.put(branchName, headCommit);
    }

    public void rmBranchCommand(String branchName) {
        if (!branches.containsKey(branchName)) {
            Utils.message("A branch with that name does not exist.");
            return;
        }

        if (Objects.equals(activeBranch, branchName)) {
            Utils.message("Cannot remove the current branch.");
            return;
        }

        branches.remove(branchName);
//        Set<String> branchKeys = branches.keySet();
//        for (String branch: branchKeys) {
//            System.out.println(branch);
//        }
    }


    /**
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
    public String commitTimestamp;
    public String commitMessage;

    public CommitData(String commitParentId, String commitTimestamp, String commitMessage) {
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