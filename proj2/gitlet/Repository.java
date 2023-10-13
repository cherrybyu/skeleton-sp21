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
    private final File COMMIT_DIR = join(GITLET_DIR, "commits");
    /** The blob directory */
    private final File BLOB_DIR = join(GITLET_DIR, "blobs");
    /** Stores branches mapped to their head commit */
    private final HashMap<String, String> branches = new HashMap<>();
    private String activeBranch;
    /** stores commit ids mapped to that commit's data*/
    private final HashMap<String, CommitData> commitHistory = new HashMap<>();
    /** The staging area. stores filenames mapped to their blob's id */
    private final HashMap<String, String> stagingArea = new HashMap<>();
    private final ArrayList<String> removalArea = new ArrayList<>();
    private String headCommit;
    private final SimpleDateFormat SDF = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
    private HashMap<String, String> remotes = new HashMap<>();

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message `initial commit` and time
     * stamp.
     */
    public void initCommand() throws IOException {
        if (GITLET_DIR.exists()) {
            Messages.gitletAlreadyExists();
        }

        CWD.mkdir();
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();

        Date timeStamp = Date.from(Instant.EPOCH);
        String formattedDate = SDF.format(timeStamp);
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
                throw Utils.error("File does not exist");
            }

            if (removalArea.contains(fileName)) {
                removalArea.remove(fileName);
                return;
            }

            Blob newBlob = Helpers.fileToBlob(CWD, fileName);
            FileData newBlobData = Helpers.getObjectAndId(newBlob);

            Commit currCommit = Helpers.getCommit(headCommit);
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            String currFileId = currBlobs.get(fileName);
            List<String> blobFileNames = plainFilenamesIn(BLOB_DIR);

            if (blobFileNames != null && !Objects.equals(currFileId, newBlobData.id)) {
                stagingArea.put(fileName, newBlobData.id);
                File blobFile = Utils.join(BLOB_DIR, newBlobData.id);
                writeContents(blobFile, newBlobData.serialized);
            }

        } catch (GitletException e) {
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

        Commit currCommit = Helpers.getCommit(headCommit);
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
                Commit currCommit = Helpers.getCommit(headCommit);
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
                    throw Utils.error("No reason to remove the file.");
                }
            }
        } catch (GitletException e) {
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
            Commit currCommit = Helpers.getCommit(commitId);
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
        List<String> modifiedFiles = Helpers.listModifiedFiles(
                headCommit,
                stagingArea,
                removalArea);
        for (String file: modifiedFiles) {
            Utils.message(file);
        }
        System.out.println();

        Utils.message("=== Untracked Files ===");
        List<String> untrackedFiles = Helpers.listUntrackedFiles(modifiedFiles);
        for (String file: untrackedFiles) {
            Utils.message(file);
        }
        System.out.println();
    }

    public void checkoutCommand(String[] args) throws IOException {
        Helpers.verifyCheckoutArgs(args);
        if (args.length == 3) { // `checkout -- [fileName]` command
            String fileName = args[2];
            Commit currCommit = Helpers.getCommit(headCommit);
            HashMap<String, String> currBlobs = currCommit.getBlobs();

            if (currBlobs.containsKey(fileName)) {
                String fileId = currBlobs.get(fileName);
                Helpers.overwriteWorkingFile(fileId, fileName, BLOB_DIR);
                return;
            }

            Utils.message("File does not exist in that commit.");
        } else if (args.length == 4) { // `checkout [commit id] -- [fileName]` command
            String commitId = args[1];
            String fileName = args[3];

            if (commitId.length() < headCommit.length()) {
                String shortId = commitId;
                commitId = Helpers.getFullCommitId(shortId);
            }

            if (commitHistory.containsKey(commitId)) {
                Commit currCommit = Helpers.getCommit(commitId);
                HashMap<String, String> currBlobs = currCommit.getBlobs();

                if (currBlobs.containsKey(fileName)) {
                    String currFileId = currBlobs.get(fileName);
                    Helpers.overwriteWorkingFile(currFileId, fileName, BLOB_DIR);
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

            if (Helpers.findUntrackedFiles()) {
                return;
            }

            Commit currCommit = Helpers.getCommit(headCommit);
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            Set<String> currBlobKeys = currBlobs.keySet();

            String branchHeadId = branches.get(branchName);
            Commit branchHeadCommit = Helpers.getCommit(branchHeadId);
            HashMap<String, String> branchHeadBlobs = branchHeadCommit.getBlobs();
            Set<String> branchHeadBlobKeys = branchHeadBlobs.keySet();

            Helpers.deleteFilesNotInBranch(currBlobKeys, branchHeadBlobKeys);

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

        Set<String> blobKeys = Helpers.getBlobKeys(commitId);
        if (Helpers.findUntrackedFiles()) {
            return;
        }
        Helpers.deleteFilesNotInCommit(blobKeys);

        for (String fileName: blobKeys) {
            String[] args = new String[]{"checkout", commitId, "--", fileName};
            this.checkoutCommand(args);
        }

        stagingArea.clear();
        headCommit = commitId;
        branches.replace(activeBranch, commitId);
    }

    public void mergeCommand(String branchName) throws IOException {
        if (Helpers.findUntrackedFiles()) {
            return;
        }
        if (!Helpers.checkMergeErrorCases(
                stagingArea,
                removalArea,
                branches,
                branchName,
                activeBranch)) {
            return;
        }

        CommitData headCommitData = commitHistory.get(headCommit);
        boolean hasSecondParent = Helpers.isStringNotNull(headCommitData.getCommitParentId2());
        String branchHeadId = branches.get(branchName);
        String splitPointId = Helpers.getSplitPoint(headCommit, branchHeadId, commitHistory, false);
        String splitPointId2 = "";
        if (hasSecondParent) {
            splitPointId2 = Helpers.getSplitPoint(headCommit, branchHeadId, commitHistory, true);
        }
        if (!Helpers.checkMergeSpecialCases(splitPointId, splitPointId2,
                                            branchHeadId, headCommit,
                                            branchName, this)) {
            return;
        }

        Commit activeHeadCommit = Helpers.getCommit(headCommit);
        Commit branchHeadCommit = Helpers.getCommit(branchHeadId);
        Commit splitPointCommit = Helpers.getCommit(splitPointId);
        Commit splitPointCommit2 = null;
        if (hasSecondParent) {
            splitPointCommit2 = Helpers.getCommit(splitPointId2);
        }

        HashMap<String, byte[]> activeHeadFiles =
                Helpers.getFilesFromCommit(activeHeadCommit);
        HashMap<String, byte[]> branchHeadFiles =
                Helpers.getFilesFromCommit(branchHeadCommit);
        HashMap<String, byte[]> splitPointFiles =
                Helpers.getFilesFromCommit(splitPointCommit);
        HashMap<String, byte[]> splitPointFiles2 = new HashMap<>();
        if (hasSecondParent) {
            splitPointFiles2 =
                    Helpers.getFilesFromCommit(splitPointCommit2);
        }

        boolean mergeConflictEncountered = false;
        HashSet<String> fileSet =
                Helpers.setUpFileSet(activeHeadFiles, branchHeadFiles,
                                        splitPointFiles, splitPointFiles2);
        for (String fileName: fileSet) {
            byte[] activeHeadFileContent = activeHeadFiles.get(fileName);
            byte[] branchHeadFileContent = branchHeadFiles.get(fileName);
            byte[] splitPointFileContent = splitPointFiles.get(fileName);
            byte[] splitPointFileContent2 = splitPointFiles2.get(fileName);

            if (Helpers.compareFilesForMerge(
                    activeHeadFileContent,
                    branchHeadFileContent,
                    splitPointFileContent,
                    splitPointFileContent2,
                    fileName,
                    branchHeadId,
                    hasSecondParent,
                    this)) {
                mergeConflictEncountered = true;
            }
        }

        this.mergeCommit(Messages.mergeCommitMessage(branchName, activeBranch), branchHeadId);
        if (mergeConflictEncountered) {
            Utils.message("Encountered a merge conflict.");
        }
    }

    public void addRemoteCommand(String remoteName, String remoteLocation) {
        if (remotes.containsKey(remoteName)) {
            message("A remote with that name already exists.");
            return;
        }
        remoteLocation = Helpers.convertSlashes(remoteLocation);
        remotes.put(remoteName, remoteLocation);
    }

    public void rmRemoteCommand(String remoteName) {
        if (!remotes.containsKey(remoteName)) {
            Utils.message("A remote with that name does not exist.");
            return;

        } else {
            remotes.remove(remoteName);
        }
    }

    public void pushCommand(String remoteName, String remoteBranchName) throws IOException {
        File remoteGitletDir = Helpers.getRemoteGitletDir(remotes.get(remoteName));
        File remoteRepoFile = join(remoteGitletDir, "repository");
        Repository remoteRepo;

        if (remoteGitletDir.exists()) {
            remoteRepo = readObject(remoteRepoFile, Repository.class);
            String remoteBranchHead = Helpers.getRemoteBranchHead(
                    remoteRepo.branches,
                    remoteRepo.headCommit,
                    remoteBranchName);

            if (commitHistory.containsKey(remoteBranchHead)) {
                Helpers.addCommitsToRemote(
                        commitHistory,
                        remoteBranchHead,
                        headCommit,
                        remoteRepo,
                        remoteRepo.commitHistory,
                        remoteGitletDir);
                remoteRepo.resetCommand(headCommit);
            } else {
                message("Please pull down remote changes before pushing.");
                return;
            }
        } else {
            message("Remote directory not found.");
            return;
        }

        Utils.writeObject(remoteRepoFile, remoteRepo);
    }

    public void fetchCommand(String remoteName, String remoteBranchName) throws IOException {
        File remoteGitletDir = new File(remotes.get(remoteName));
        File remoteRepoFile;
        Repository remoteRepo;
        if (remoteGitletDir.exists()) {
            remoteRepoFile = join(remoteGitletDir, "repository");
            remoteRepo = readObject(remoteRepoFile, Repository.class);
            HashMap<String, String> remoteBranches = remoteRepo.branches;

            if (remoteBranches.containsKey(remoteBranchName)) {
                String localBranchName = remoteName + "/" + remoteBranchName;
                if (!branches.containsKey(localBranchName)) {
                    branches.put(localBranchName, headCommit);
                }
                String remoteBranchHead = remoteBranches.get(remoteBranchName);
                Helpers.copyRemoteBranch(
                        commitHistory,
                        remoteRepo.commitHistory,
                        headCommit,
                        remoteBranchHead,
                        remoteGitletDir
                );
                branches.replace(localBranchName, remoteBranchHead);

            } else {
                message("That remote does not have that branch.");
                return;
            }
        } else {
            message("Remote directory not found.");
            return;
        }

        if (remoteRepo != null) {
            Utils.writeObject(remoteRepoFile, remoteRepo);
        }
    }

    public void pullCommand(String remoteName, String remoteBranchName) throws IOException {
        String localBranchName = remoteName + "/" + remoteBranchName;
        this.fetchCommand(remoteName, remoteBranchName);
        this.mergeCommand(localBranchName);
    }

    private void mergeCommit(String message, String parentId2) {
        if (stagingArea.isEmpty() && removalArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit currCommit = Helpers.getCommit(headCommit);
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

