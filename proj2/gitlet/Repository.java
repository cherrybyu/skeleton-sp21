package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  // It's a good idea to give a description here of what else this Class does at a high level.
 *
 *  @author Hannah Nguyen
 */
public class Repository implements Serializable {
    /*
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
    /** Stores branches mapped to their head commit */
    private HashMap<String, String> branches = new HashMap<>();
    private String activeBranch;
    /** stores commit ids mapped to that commit's data*/
    private HashMap<String, CommitData> commitHistory = new HashMap<>();
    /** The staging area. stores filenames mapped to their blob's id */
    private HashMap<String, String> stagingArea = new HashMap<>();
    private ArrayList<String> removalArea = new ArrayList<>();
    private String headCommit;
    private SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message `initial commit` and time
     * stamp.
     */
    public void initCommand() throws IOException {
        if (GITLET_DIR.exists()) {
            Utils.message(
                    "A Gitlet version-control system already exists in the current directory."
            );
        }

        CWD.mkdir();
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();

        Date timeStamp = Date.from(Instant.EPOCH);
        String formattedDate = sdf.format(timeStamp);
        Commit initCommit = new Commit(
                null,
                null,
                "initial commit", new HashMap<>(), formattedDate
        );
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

            Blob newBlob = Helpers.fileToBlob(CWD, fileName);
            FileData newBlobData = Helpers.getObjectAndId(newBlob);

            Commit currCommit = Helpers.getCommit(COMMIT_DIR, headCommit);
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            String currFileId = currBlobs.get(fileName);
            List<String> blobFileNames = plainFilenamesIn(BLOB_DIR);

            if (blobFileNames != null && !Objects.equals(currFileId, newBlobData.id)) {
                stagingArea.put(fileName, newBlobData.id);
                File blobFile = Utils.join(BLOB_DIR, newBlobData.id);
                writeContents(blobFile, newBlobData.serialized);
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

        Commit currCommit = Helpers.getCommit(COMMIT_DIR, headCommit);
        HashMap<String, String> blobs = currCommit.getBlobs();

        Set<String> stagingKeys = stagingArea.keySet();

        for (String fileName: stagingKeys) {
            String value;
            if (blobs.containsKey(fileName)) {
                value = stagingArea.get(fileName);
                blobs.replace(fileName, value);
            } else {
                value = stagingArea.get(fileName);
                blobs.put(fileName, value);
            }
        }

        for (String fileName: removalArea) {
            blobs.remove(fileName);
        }

        stagingArea.clear();
        removalArea.clear();

        Commit newCommit = Helpers.createCommit(headCommit, null, message, blobs);
        saveCommit(newCommit);
    }

    public void rmCommand(String fileName) {
        try {
            if (stagingArea.containsKey(fileName)) {
                stagingArea.remove(fileName);
            } else {
                Commit currCommit = Helpers.getCommit(COMMIT_DIR, headCommit);
                HashMap<String, String> currBlobs = currCommit.getBlobs();
                List<String> workingFiles = plainFilenamesIn(CWD);

                if (currBlobs.containsKey(fileName) && workingFiles != null) {
                    if (workingFiles.contains(fileName)) {
                        Utils.restrictedDelete(fileName);
                        removalArea.add(fileName);
                    } else {
                        removalArea.add(fileName);
                    }
                } else {
                    throw new IllegalArgumentException("No reason to remove the file.");
                }
            }
        } catch (IllegalArgumentException e) {
            Utils.message(e.getMessage());
        }
    }

    public void logCommand() {
        CommitData currCommit = commitHistory.get(headCommit);
        String currCommitId = headCommit;

        while (currCommit != null) {
            Utils.message("===");
            Utils.message("commit " + currCommitId);
            Utils.message("Date: " + currCommit.getCommitTimestamp());
            Utils.message(currCommit.getCommitMessage());
            System.out.println();

            currCommitId = currCommit.getCommitParentId();
            currCommit = commitHistory.get(currCommit.getCommitParentId());
        }
    }

    public void globalLogCommand() {
        List<String> files = Utils.plainFilenamesIn(COMMIT_DIR);

        assert files != null;
        for (String fileId : files) {
            CommitData currCommit = commitHistory.get(fileId);

            Utils.message("===");
            Utils.message("commit " + fileId);
            Utils.message("Date: " + currCommit.getCommitTimestamp());
            Utils.message(currCommit.getCommitMessage());
            System.out.println();
        }
    }

    public void findCommand(String message) {
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        ArrayList<String> messagesToPrint = new ArrayList<>();

        assert commitList != null;
        for (String commitId: commitList) {
            Commit currCommit = Helpers.getCommit(COMMIT_DIR, commitId);
            if (currCommit.getMessage().contains(message)) {
                messagesToPrint.add(commitId);
            }
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
        if (args.length == 3) { // `checkout -- [fileName]` command
            if (!Objects.equals(args[1], "--")) {
                Utils.message("Incorrect operands.");
                return;
            }
            String fileName = args[2];
            Commit currCommit = Helpers.getCommit(COMMIT_DIR, headCommit);
            HashMap<String, String> currBlobs = currCommit.getBlobs();

            if (currBlobs.containsKey(fileName)) {
                String fileId = currBlobs.get(fileName);
                Helpers.overwriteWorkingFile(fileId, fileName, CWD, BLOB_DIR);
                return;
            }

            Utils.message("File does not exist in that commit.");
        } else if (args.length == 4) { // `checkout [commit id] -- [fileName]` command
            if (!Objects.equals(args[2], "--")) {
                Utils.message("Incorrect operands.");
                return;
            }
            String commitId = args[1];
            String fileName = args[3];

            if (commitHistory.containsKey(commitId)) {
                Commit currCommit = Helpers.getCommit(COMMIT_DIR, commitId);
                HashMap<String, String> currBlobs = currCommit.getBlobs();

                if (currBlobs.containsKey(fileName)) {
                    String currFileId = currBlobs.get(fileName);
                    Helpers.overwriteWorkingFile(currFileId, fileName, CWD, BLOB_DIR);

//                    FileData filedata = Helpers.getObjectAndId(Helpers.fileToBlob(CWD, fileName));
//                    if (Helpers.isFileInDir(BLOB_DIR, filedata.id)) {
//                        Utils.message("file is in blobdir");
//                    } else {
//                        Utils.message("file not in blobdir");
//                    }
                    return;
                }
                Utils.message("File does not exist in that commit.");
            } else {
                Utils.message("No commit with that id exists.");
            }
        } else if (args.length == 2) { // `checkout [branchName]` command
            String branchName = args[1];
            if (!branches.containsKey(branchName)) {
                Utils.message("No such branch exists.");
                return;
            }
            if (Objects.equals(activeBranch, branchName)) {
                Utils.message("No need to checkout the current branch.");
                return;
            }

            Commit currCommit = Helpers.getCommit(COMMIT_DIR, headCommit);
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            Set<String> currBlobKeys = currBlobs.keySet();
            List<String> workingFiles = plainFilenamesIn(CWD);

            if (workingFiles != null && !workingFiles.isEmpty()) {
                for (String file: workingFiles) {
                    if (!currBlobKeys.contains(file)) {
                        Utils.message(
                                "There is an untracked file in the way; delete it, or add and commit it first."
                        );
                        return;
                    }
                }
            }
            if (!stagingArea.isEmpty()) {
                Utils.message(
                        "There is an untracked file in the way; delete it, or add and commit it first."
                );
                return;
            }

            String branchHeadId = branches.get(branchName);
            Commit branchHeadCommit = Helpers.getCommit(COMMIT_DIR, branchHeadId);
            HashMap<String, String> branchHeadBlobs = branchHeadCommit.getBlobs();
            Set<String> branchHeadBlobKeys = branchHeadBlobs.keySet();

            for (String fileName: currBlobKeys) {
                if (!branchHeadBlobKeys.contains(fileName)) {
                    Utils.restrictedDelete(Utils.join(CWD, fileName));
                }
            }
            for (String fileName: branchHeadBlobKeys) {
                String[] newArgs = new String[] {"checkout", branchHeadId, "--", fileName};
                this.checkoutCommand(newArgs);
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
    }

    public void resetCommand(String commitId) throws IOException {
        if (!Helpers.isFileInDir(COMMIT_DIR, commitId)) {
            Utils.message("No commit with that id exists.");
            return;
        }

        Commit commit = Helpers.getCommit(COMMIT_DIR, commitId);
        HashMap<String, String> blobs = commit.getBlobs();
        Set<String> blobKeys = blobs.keySet();
        List<String> workingFiles = plainFilenamesIn(CWD);

        if (workingFiles != null) {
            for (String file: workingFiles) {
                File originalFile = Utils.join(CWD, file);
                byte[] fileContents = readContents(originalFile);
                Blob newBlob = new Blob(file, fileContents);
                FileData fileData = Helpers.getObjectAndId(newBlob);

                if (!Helpers.isFileInDir(BLOB_DIR, fileData.id)) {
                    Utils.message(
                            "There is an untracked file in the way; delete it, or add and commit it first."
                    );
                    return;
                }
            }
        }

        for (String fileName: blobKeys) {
            String[] args = new String[]{"checkout", commitId, "--", fileName};
            this.checkoutCommand(args);
        }

        if (workingFiles != null) {
            for (String file: workingFiles) {
                if (!blobKeys.contains(file)) {
                    File toDelete = Utils.join(CWD, file);
                    restrictedDelete(toDelete);
                }
            }
        }

        stagingArea.clear();
        headCommit = commitId;
        branches.replace(activeBranch, commitId);
    }

    public void mergeCommand(String branchName) throws IOException {
        List<String> workingFiles = plainFilenamesIn(CWD);
        Commit activeHeadCommit = Helpers.getCommit(COMMIT_DIR, headCommit);
//        HashMap<String, String> activeHeadBlobs = activeHeadCommit.getBlobs();

        assert workingFiles != null;
        for (String fileName: workingFiles) {
            Blob blob = Helpers.fileToBlob(CWD, fileName);
            FileData blobData = Helpers.getObjectAndId(blob);
            if (!Helpers.isFileInDir(BLOB_DIR, blobData.id)) {
                Utils.message(
                        "There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        if (!stagingArea.isEmpty() || !removalArea.isEmpty()) {
            Utils.message("You have uncommitted changes.");
            return;
        }

        if (!branches.containsKey(branchName)) {
            Utils.message("A branch with that name does not exist.");
            return;
        }

        if (Objects.equals(branchName, activeBranch)) {
            Utils.message("Cannot merge a branch with itself.");
            return;
        }

        ArrayList<String> activeCommitHistory =  new ArrayList<>();
        String currCommitId = headCommit;
        while (currCommitId != null) {
            activeCommitHistory.add(currCommitId);
            CommitData currCommitData = commitHistory.get(currCommitId);
            currCommitId = currCommitData.getCommitParentId();
        }

        String branchHeadId = branches.get(branchName);
        String splitPointId = null;
        String currBranchCommitId = branchHeadId;
        while (currBranchCommitId != null) {
            if (activeCommitHistory.contains(currBranchCommitId)) {
                splitPointId = currBranchCommitId;
                break;
            } else {
                CommitData currBranchCommitData = commitHistory.get(currBranchCommitId);
                currBranchCommitId = currBranchCommitData.getCommitParentId();
            }
        }

        if (Objects.equals(splitPointId, branchHeadId)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            return;
        }
        if (Objects.equals(splitPointId, headCommit)) {
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        Commit branchHeadCommit = Helpers.getCommit(COMMIT_DIR, branchHeadId);
        Commit splitPointCommit = Helpers.getCommit(COMMIT_DIR, splitPointId);

        HashMap<String, byte[]> activeHeadFiles =
                Helpers.getFilesFromCommit(activeHeadCommit, BLOB_DIR);
        HashMap<String, byte[]> branchHeadFiles =
                Helpers.getFilesFromCommit(branchHeadCommit, BLOB_DIR);
        HashMap<String, byte[]> splitPointFiles =
                Helpers.getFilesFromCommit(splitPointCommit, BLOB_DIR);


        HashMap <String, byte[]> fileList =
                Helpers.setUpFileList(activeHeadFiles, branchHeadFiles, splitPointFiles, workingFiles);

        Set<String> fileListKeys = fileList.keySet();
        boolean mergeConflictEncountered = false;
        for (String fileName: fileListKeys) {
            byte[] activeHeadFileContent = activeHeadFiles.get(fileName);
            byte[] branchHeadFileContent = branchHeadFiles.get(fileName);
            byte[] splitPointFileContent = splitPointFiles.get(fileName);
//            boolean isWorkingFile = workingFiles.contains(fileName);

            if (branchHeadFileContent != null) {
                if (splitPointFileContent == activeHeadFileContent
                        && splitPointFileContent != branchHeadFileContent) {
                    String[] args = new String[]{"checkout", branchHeadId, "--", fileName};
                    this.checkoutCommand(args);
                    this.addCommand(fileName);
                }

                if (activeHeadFileContent != null) {
                    if (splitPointFileContent != branchHeadFileContent && splitPointFileContent != activeHeadFileContent) {
                        if (activeHeadFileContent != branchHeadFileContent) {
                            File newFile = Utils.join(CWD, fileName);
                            Helpers.overwriteConflictedFile(newFile, activeHeadFileContent, branchHeadFileContent);
                            mergeConflictEncountered = true;
                            this.addCommand(fileName);

//                            Utils.message(fileName);
//                            System.out.println(readContentsAsString(newFile));
                        }
                    }
                }

                if (splitPointFileContent == null) {
                    if (activeHeadFileContent == null) {
                        String[] args = new String[] {"checkout", branchHeadId, "--", fileName};
                        this.checkoutCommand(args);
                        this.addCommand(fileName);
                    }
                }
            } else {
                if (activeHeadFileContent != null && splitPointFileContent != null) {
                    if (!Arrays.equals(activeHeadFileContent, splitPointFileContent)) {
                        File newFile = Utils.join(CWD, fileName);
                        Helpers.overwriteConflictedFile(newFile, activeHeadFileContent, null);
                        mergeConflictEncountered = true;
                        this.addCommand(fileName);

//                            Utils.message(fileName);
//                            System.out.println(readContentsAsString(newFile));
                    }

                    if (Arrays.equals(activeHeadFileContent, splitPointFileContent)) {
                        stagingArea.remove(fileName);
                        this.rmCommand(fileName);
                    }
                }
            }
        }

        String commitMessage = "Merged " + branchName + " into " + activeBranch + ".";
        this.mergeCommit(commitMessage, branchHeadId);

        if (mergeConflictEncountered) { Utils.message("Encountered a merge conflict."); }
    }

    private void mergeCommit(String message, String parentId2) {
        if (stagingArea.isEmpty() && removalArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit currCommit = Helpers.getCommit(COMMIT_DIR, headCommit);
        HashMap<String, String> blobs = currCommit.getBlobs();

        Set<String> stagingKeys = stagingArea.keySet();

        for (String fileName: stagingKeys) {
            String value;
            if (blobs.containsKey(fileName)) {
                value = stagingArea.get(fileName);
                blobs.replace(fileName, value);
            } else {
                value = stagingArea.get(fileName);
                blobs.put(fileName, value);
            }
        }

        for (String fileName: removalArea) {
            blobs.remove(fileName);
        }

        stagingArea.clear();
        removalArea.clear();

        Commit newCommit = Helpers.createCommit(headCommit, parentId2, message, blobs);
        saveCommit(newCommit);
    }

    /**
     * saves commit into a file in the .gitlet/commits/ directory. adds reference to
     * said commit to commitHistory and assigns headCommit to the commit's id
     * */
    private void saveCommit(Commit commit) {
        FileData fileData = Helpers.getObjectAndId(commit);

        String commitId = fileData.id;
        byte[] serializedCommit = fileData.serialized;

        File commitFile = Utils.join(COMMIT_DIR, commitId);
        writeContents(commitFile, serializedCommit);

        CommitData commitData = new CommitData(
                commit.getParentId(),
                commit.getParentId2(),
                commit.getTimestamp(),
                commit.getMessage()
        );
        commitHistory.put(commitId, commitData);
        headCommit = commitId;
        branches.replace(activeBranch, headCommit);
    }
}

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

class FileData {
    String id;
    byte[] serialized;
    FileData(String objectId, byte[] serializedObject) {
        this.id = objectId;
        this.serialized = serializedObject;
    }
}

