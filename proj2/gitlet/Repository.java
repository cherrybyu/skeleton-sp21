package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.Main.CWD;
import static gitlet.Main.GITLET_DIR;

/** Represents a gitlet repository.
 *
 *  @author Hannah Nguyen
 */
public class Repository implements Serializable {
    /** The commit directory */
    private final File COMMIT_DIR = join(GITLET_DIR, "commits");
    /** The blob directory */
    private final File BLOB_DIR = join(GITLET_DIR, "blobs");
    /** Mapping of branches to their head commit's ID */
    private final HashMap<String, String> branches = new HashMap<>();
    /** Current branch */
    private String activeBranch;
    /** Mapping of commits' IDs to their CommitData object */
    private final HashMap<String, CommitData> commitHistory = new HashMap<>();
    /** SHA-1 hash of latest commit */
    private String headCommit;
    /** Mapping of file names staged for addition to their ID */
    private final HashMap<String, String> stagingArea = new HashMap<>();
    /** List of files staged for removal */
    private final List<String> removalArea = new ArrayList<>();
    /** Mapping of remote directories' names to their file paths */
    private final HashMap<String, String> remotes = new HashMap<>();

    /** Creates a new Gitlet version-control system in the current directory. */
    public void initCommand() throws IOException {
        if (GITLET_DIR.exists()) {
            gitletAlreadyExists();
            return;
        }

        makeDirectories();
        Commit initCommit = createInitCommit();
        saveCommit(initCommit);

        branches.put("master", headCommit);
        activeBranch = "master";
    }

    /** Stages file for addition */
    public void addCommand(String fileName) throws IOException {
        if (!fileExists(fileName)) {
            return;
        }

        if (fileStagedForRemoval(fileName)) {
            return;
        }

        Blob newBlob = fileToBlob(CWD, fileName);
        FileData newBlobData = getObjectAndId(newBlob);

        Commit currCommit = getCommit(headCommit);
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        String currFileId = currBlobs.get(fileName);
        List<String> blobFileNames = plainFilenamesIn(BLOB_DIR);

        if (blobFileNames != null && !Objects.equals(currFileId, newBlobData.id)) {
            stagingArea.put(fileName, newBlobData.id);
            File blobFile = Utils.join(BLOB_DIR, newBlobData.id);
            writeContents(blobFile, newBlobData.contents);
        }
    }

    /** Saves changes to a Commit */
    public void commitCommand(String message) throws IOException {
        if (message == null || message.isEmpty()) {
            Utils.message("Please enter a commit message.");
            return;
        }

        if (stagingArea.isEmpty() && removalArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit currCommit = getCommit(headCommit);
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

        Commit newCommit = createCommit(headCommit, null, message, blobs);
        saveCommit(newCommit);
    }

    /** Stages file for removal */
    public void rmCommand(String fileName) {
        try {
            if (stagingArea.containsKey(fileName)) {
                stagingArea.remove(fileName);
            } else {
                Commit currCommit = getCommit(headCommit);
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

    /** Prints out all commits of the active branch */
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

    /** Prints out all commits */
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

    /** Finds all commits that contain the message that was passed in
     * and prints their IDs */
    public void findCommand(String message) {
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        List<String> messagesToPrint = new ArrayList<>();

        assert commitList != null;
        for (String commitId: commitList) {
            Commit currCommit = getCommit(commitId);
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

    /** Prints out the status of the repository */
    public void statusCommand() {
        statusBranches();
        statusStaged();
        statusRemoved();
        List<String> modifiedFiles = statusModified();
        statusUntracked(modifiedFiles);
    }

    /** Checks out file from commit or checks out commit from another branch*/
    public void checkoutCommand(String[] args) throws IOException {
        verifyCheckoutArgs(args);
        if (args.length == 3) { // `checkout -- [fileName]` command
            String fileName = args[2];
            Commit currCommit = getCommit(headCommit);
            HashMap<String, String> currBlobs = currCommit.getBlobs();

            if (currBlobs.containsKey(fileName)) {
                String fileId = currBlobs.get(fileName);
                overwriteWorkingFile(fileId, fileName);
                return;
            }

            Utils.message("File does not exist in that commit.");
        } else if (args.length == 4) { // `checkout [commit id] -- [fileName]` command
            String commitId = args[1];
            String fileName = args[3];

            if (commitId.length() < headCommit.length()) {
                String shortId = commitId;
                commitId = getFullCommitId(shortId);
            }

            if (commitHistory.containsKey(commitId)) {
                Commit currCommit = getCommit(commitId);
                HashMap<String, String> currBlobs = currCommit.getBlobs();

                if (currBlobs.containsKey(fileName)) {
                    String currFileId = currBlobs.get(fileName);
                    overwriteWorkingFile(currFileId, fileName);
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

            if (findUntrackedFiles()) {
                return;
            }

            Set<String> currBlobKeys = getBlobKeys(headCommit);

            String branchHeadId = branches.get(branchName);
            Set<String> branchHeadBlobKeys = getBlobKeys(branchHeadId);

            deleteFilesNotInBranch(currBlobKeys, branchHeadBlobKeys);

            for (String fileName: branchHeadBlobKeys) {
                String[] newArgs = new String[] {"checkout", branchHeadId, "--", fileName};
                this.checkoutCommand(newArgs);
            }

            stagingArea.clear();
            activeBranch = branchName;
            headCommit = branchHeadId;
        }
    }

    /** Creates a branch */
    public void branchCommand(String branchName) {
        if (branches.containsKey(branchName)) {
            Utils.message("A branch with that name already exists.");
            return;
        }

        branches.put(branchName, headCommit);
    }

    /** Removes a branch */
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

    /** Resets the branch to a specific commit */
    public void resetCommand(String commitId) throws IOException {
        if (!isFileInDir(COMMIT_DIR, commitId)) {
            Utils.message("No commit with that id exists.");
            return;
        }

        Set<String> blobKeys = getBlobKeys(commitId);
        if (findUntrackedFiles()) {
            return;
        }
        deleteFilesNotInCommit(blobKeys);

        for (String fileName: blobKeys) {
            String[] args = new String[]{"checkout", commitId, "--", fileName};
            this.checkoutCommand(args);
        }

        stagingArea.clear();
        headCommit = commitId;
        branches.replace(activeBranch, commitId);
    }

    /** Merges a branch into the active branch */
    public void mergeCommand(String branchName) throws IOException {
        if (findUntrackedFiles()) {
            return;
        }
        if (!checkMergeErrorCases(branchName)) {
            return;
        }

        CommitData headCommitData = commitHistory.get(headCommit);
        boolean hasSecondParent = isStringNotNull(headCommitData.getCommitParentId2());
        String branchHeadId = branches.get(branchName);
        String splitPointId = getSplitPoint(headCommit, branchHeadId, commitHistory, false);
        String splitPointId2 = "";
        if (hasSecondParent) {
            splitPointId2 = getSplitPoint(headCommit, branchHeadId, commitHistory, true);
        }
        if (!checkMergeSpecialCases(
                splitPointId,
                splitPointId2,
                branchHeadId,
                branchName)) {
            return;
        }

        Commit activeHeadCommit = getCommit(headCommit);
        Commit branchHeadCommit = getCommit(branchHeadId);
        Commit splitPointCommit = getCommit(splitPointId);
        Commit splitPointCommit2 = null;
        if (hasSecondParent) {
            splitPointCommit2 = getCommit(splitPointId2);
        }

        HashMap<String, byte[]> activeHeadFiles =
                getFilesFromCommit(activeHeadCommit);
        HashMap<String, byte[]> branchHeadFiles =
                getFilesFromCommit(branchHeadCommit);
        HashMap<String, byte[]> splitPointFiles =
                getFilesFromCommit(splitPointCommit);
        HashMap<String, byte[]> splitPointFiles2 = new HashMap<>();
        if (hasSecondParent) {
            splitPointFiles2 =
                    getFilesFromCommit(splitPointCommit2);
        }

        boolean mergeConflictEncountered = false;
        HashSet<String> fileSet =
                setUpFileSet(activeHeadFiles, branchHeadFiles,
                                        splitPointFiles, splitPointFiles2);
        for (String fileName: fileSet) {
            byte[] activeHeadFileContent = activeHeadFiles.get(fileName);
            byte[] branchHeadFileContent = branchHeadFiles.get(fileName);
            byte[] splitPointFileContent = splitPointFiles.get(fileName);
            byte[] splitPointFileContent2 = splitPointFiles2.get(fileName);

            if (compareFilesForMerge(
                    activeHeadFileContent,
                    branchHeadFileContent,
                    splitPointFileContent,
                    splitPointFileContent2,
                    fileName,
                    branchHeadId,
                    hasSecondParent)) {
                mergeConflictEncountered = true;
            }
        }

        this.mergeCommit(mergeCommitMessage(branchName, activeBranch), branchHeadId);
        if (mergeConflictEncountered) {
            Utils.message("Encountered a merge conflict.");
        }
    }

    /** Add a remote */
    public void addRemoteCommand(String remoteName, String remoteLocation) {
        if (remotes.containsKey(remoteName)) {
            message("A remote with that name already exists.");
            return;
        }
        remoteLocation = convertSlashes(remoteLocation);
        remotes.put(remoteName, remoteLocation);
    }

    /** Removes remote */
    public void rmRemoteCommand(String remoteName) {
        if (!remotes.containsKey(remoteName)) {
            Utils.message("A remote with that name does not exist.");
            return;

        } else {
            remotes.remove(remoteName);
        }
    }

    /** Copies commits from local repository to a remote repository */
    public void pushCommand(String remoteName, String remoteBranchName) throws IOException {
        File remoteGitletDir = getRemoteGitletDir(remotes.get(remoteName));
        File remoteRepoFile = join(remoteGitletDir, "repository");
        Repository remoteRepo;

        if (remoteGitletDir.exists()) {
            remoteRepo = readObject(remoteRepoFile, Repository.class);
            String remoteBranchHead = getRemoteBranchHead(
                    remoteRepo.branches,
                    remoteRepo.headCommit,
                    remoteBranchName);

            if (commitHistory.containsKey(remoteBranchHead)) {
                addCommitsToRemote(
                        remoteBranchHead,
                        headCommit,
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

    /** Copies commits from a branch in a remote repository to a branch
     *  in the local repository */
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
                copyRemoteBranch(
                        remoteRepo.commitHistory,
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

    /** Copies commits from a branch in a remote repository to a branch
     * in the local repository and merges that branch */
    public void pullCommand(String remoteName, String remoteBranchName) throws IOException {
        String localBranchName = remoteName + "/" + remoteBranchName;
        this.fetchCommand(remoteName, remoteBranchName);
        this.mergeCommand(localBranchName);
    }

    /** saves commit into a file in the .gitlet/commits/ directory. adds reference to
     * said commit to commitHistory and assigns headCommit to the commit's id */
    private void saveCommit(Commit commit) {
        FileData fileData = getObjectAndId(commit);

        String commitId = fileData.id;
        byte[] serializedCommit = fileData.contents;

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

    /** Creates initial commit */
    private Commit createInitCommit() {
        SimpleDateFormat SDF = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");

        Date timeStamp = Date.from(Instant.EPOCH);
        String formattedDate = SDF.format(timeStamp);
        Commit initCommit = new Commit(
                null,
                null,
                "initial commit", new HashMap<>(), formattedDate
        );
        return initCommit;
    }

    /** Creates a commit */
    private Commit createCommit(
            String parentId,
            String parentId2,
            String message,
            HashMap<String, String> blobs) {
        SimpleDateFormat SDF = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        Date timeStamp = Date.from(Instant.now());
        String formattedDate = SDF.format(timeStamp);
        return new Commit(parentId, parentId2, message, blobs, formattedDate);
    }

    /** Creates a commit or merge */
    private void mergeCommit(String message, String parentId2) {
        if (stagingArea.isEmpty() && removalArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit currCommit = getCommit(headCommit);
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

        Commit newCommit = createCommit(headCommit, parentId2, message, blobs);
        saveCommit(newCommit);
    }

    /** Makes directories for initial commit */
    private void makeDirectories() {
        CWD.mkdir();
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
    }

    /** Gets all file names from a commit */
    private Set<String> getBlobKeys(String commitId) {
        Commit commit = getCommit(commitId);
        HashMap<String, String> blobs = commit.getBlobs();
        return blobs.keySet();
    }

    /** Check if file exists */
    private boolean fileExists(String fileName) {
        if (plainFilenamesIn(CWD).contains(fileName)) {
            return true;
        } else {
            message("File does not exist");
            return false;
        }
    }

    /** Check if file is staged for removal */
    private boolean fileStagedForRemoval(String fileName) {
        if (removalArea.contains(fileName)) {
            removalArea.remove(fileName);
            return true;
        } else {
            return false;
        }
    }

    /** Check if string is an abbreviated ID */
    private boolean isShortenedId(String shortenedId, String fullId) {
        return fullId.startsWith(shortenedId);
    }

    /** Checks if string is not null */
    private boolean isStringNotNull(String string) {
        return string != null;
    }

    /** Gets a full ID from an abbreviated ID */
    private String getFullCommitId(String shortenedId) {
        List<String> commitIdList = plainFilenamesIn(COMMIT_DIR);

        assert commitIdList != null;
        for (String fullId: commitIdList) {
            if (isShortenedId(shortenedId, fullId)) {
                return fullId;
            }
        }

        return null;
    }

    /** Checks if there is untracked files in the working directory */
    private boolean findUntrackedFiles() {
        List<String> workingFiles = plainFilenamesIn(CWD);

        if (workingFiles != null) {
            for (String fileName : workingFiles) {
                Blob blob = fileToBlob(CWD, fileName);
                FileData blobData = getObjectAndId(blob);
                if (!isFileInDir(BLOB_DIR, blobData.id)) {
                    untrackedFiles();
                    return true;
                }
            }
        }
        return false;
    }

    /** Displays branch status */
    private void statusBranches() {
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
    }

    /** Displays status of staged files */
    private void statusStaged() {
        Utils.message("=== Staged Files ===");
        String[] stagingKeys = stagingArea.keySet().toArray(new String[0]);
        Arrays.sort(stagingKeys);
        for (String key : stagingKeys) {
            Utils.message(key);
        }
        System.out.println();
    }

    /** Displays status of removed files */
    private void statusRemoved() {
        Utils.message("=== Removed Files ===");
        Collections.sort(removalArea);
        for (String file: removalArea) {
            Utils.message(file);
        }
        System.out.println();
    }

    /** Displays status of modified files */
    private List<String> statusModified() {
        Utils.message("=== Modifications Not Staged For Commit ===");
        List<String> modifiedFiles = listModifiedFiles();
        for (String file: modifiedFiles) {
            Utils.message(file);
        }
        System.out.println();
        return modifiedFiles;
    }

    /** Displaus status of untracked files */
    private void statusUntracked(List<String> modifiedFiles) {
        Utils.message("=== Untracked Files ===");
        List<String> untrackedFiles = listUntrackedFiles(modifiedFiles);
        for (String file: untrackedFiles) {
            Utils.message(file);
        }
        System.out.println();
    }

    /** Creates a list of all untracked files */
    private ArrayList<String> listUntrackedFiles(List<String> modifiedFiles) {
        List<String> workingFiles = plainFilenamesIn(CWD);
        ArrayList<String> untrackedFiles = new ArrayList<>();
        if (workingFiles != null) {
            for (String fileName : workingFiles) {
                if (!modifiedFiles.isEmpty()) {
                    for (String file : modifiedFiles) {
                        if (!file.startsWith(fileName)) {
                            Blob blob = fileToBlob(CWD, fileName);
                            FileData blobData = getObjectAndId(blob);
                            if (!isFileInDir(BLOB_DIR, blobData.id)) {
                                untrackedFiles.add(fileName);
                            }
                        }
                    }
                } else {
                    Blob blob = fileToBlob(CWD, fileName);
                    FileData blobData = getObjectAndId(blob);
                    if (!isFileInDir(BLOB_DIR, blobData.id)) {
                        untrackedFiles.add(fileName);
                    }
                }
            }
            untrackedFiles.sort(String::compareToIgnoreCase);
        }
        return untrackedFiles;
    }

    /** Creates a list of all modified files */
    private ArrayList<String> listModifiedFiles() {
        List<String> workingFiles = plainFilenamesIn(CWD);
        Commit currCommit = getCommit(headCommit);
        HashMap<String, String> currBlobs = currCommit.getBlobs();

        ArrayList<String> modifiedFiles = new ArrayList<>();

        if (workingFiles != null) {
            for (String fileName : currBlobs.keySet()) {
                if (!removalArea.contains(fileName) && !workingFiles.contains(fileName)) {
                    modifiedFiles.add(fileName + " (deleted)");
                }
            }

            for (String fileName : stagingArea.keySet()) {
                if (!workingFiles.contains(fileName)) {
                    modifiedFiles.add(fileName + " (deleted)");
                }
            }

            for (String fileName : workingFiles) {
                if (currBlobs.containsKey(fileName)) {
                    Blob blob = getBlob(currBlobs.get(fileName));
                    Blob newBlob = fileToBlob(CWD, fileName);
                    if (!Arrays.equals(newBlob.getFileContents(), blob.getFileContents())
                            && !stagingArea.containsKey(fileName)) {
                        modifiedFiles.add(fileName + " (modified)");
                    }

                }

                if (stagingArea.containsKey(fileName)) {
                    if (currBlobs.containsKey(fileName)) {
                        Blob blob = getBlob(currBlobs.get(fileName));
                        Blob stagedBlob = getBlob(stagingArea.get(fileName));
                        if (!Arrays.equals(stagedBlob.getFileContents(), blob.getFileContents())) {
                            modifiedFiles.add(fileName + " (modified)");
                        }
                    }
                }
            }
        }
        modifiedFiles.sort(String::compareToIgnoreCase);
        return modifiedFiles;
    }

    /** Takes an object and serializes it. Once serialized, creates a SHA-1 hash.
     * Returns a FileData instance that contains both the SHA-1 hash and the serialized object. */
    private FileData getObjectAndId(Object object) {
        byte[] serializedObject = serialize((Serializable) object);
        String objectId = Utils.sha1(serializedObject);
        return new FileData(objectId, serializedObject);
    }

    /** reads Commit object from a file named the given id and returns it */
    public Commit getCommit(String commitId) {
        File file = Utils.join(COMMIT_DIR, commitId);
        return readObject(file, Commit.class);
    }

    /** reads Blob object from a file named the given id and returns it */
    public Blob getBlob(String blobId) {
        File file = Utils.join(BLOB_DIR, blobId);
        return readObject(file, Blob.class);
    }

    /** returns true if the given file is in the given directory, false otherwise */
    public Boolean isFileInDir(File dir, String file) {
        List<String> filesInDir = plainFilenamesIn(dir);
        return filesInDir != null && filesInDir.contains(file);
    }

    /** Gets blob from file */
    public Blob fileToBlob(File dir, String fileName) {
        File originalFile = Utils.join(dir, fileName);
        byte[] fileContents = readContents(originalFile);
        return new Blob(fileName, fileContents);
    }

    /** Overwrites file in CWD */
    public void overwriteWorkingFile(
            String fileId,
            String fileName) {
        Blob fileObject = getBlob(fileId);
        byte[] fileContents = fileObject.getFileContents();
        File originalFile = Utils.join(CWD, fileName);
        Utils.writeContents(originalFile, fileContents);
    }

    /** Get all files from a commit */
    public HashMap<String, byte[]> getFilesFromCommit(Commit commit) {
        HashMap<String, String> blobs = commit.getBlobs();
        Set<String> blobKeys = blobs.keySet();
        HashMap<String, byte[]> fileContents = new HashMap<>();

        for (String fileName: blobKeys) {
            String id = blobs.get(fileName);
            Blob blob = getBlob(id);
            fileContents.put(fileName, blob.getFileContents());
        }

        return fileContents;
    }

    /** Verifies the arguments passed into checkout */
    private void verifyCheckoutArgs(String[] args) {
        try {
            if (args.length == 3) {
                if (!Objects.equals(args[1], "--")) {
                    throw Utils.error("Incorrect operands.");
                }
            }

            if (args.length == 4) {
                if (!Objects.equals(args[2], "--")) {
                    throw Utils.error("Incorrect operands.");
                }
            }
        } catch (GitletException e) {
            Utils.message(e.getMessage());
        }
    }

    /** Delete files not tracked in commit */
    private void deleteFilesNotInCommit(Set<String> blobKeys) {
        List<String> workingFiles = plainFilenamesIn(CWD);

        if (workingFiles != null) {
            for (String file: workingFiles) {
                if (!blobKeys.contains(file)) {
                    File toDelete = Utils.join(CWD, file);
                    restrictedDelete(toDelete);
                }
            }
        }
    }

    /** Delete files that are not present in a branch */
    private void deleteFilesNotInBranch(Set<String> currBlobKeys,
                                        Set<String> branchHeadBlobKeys) {
        Set<String> toRemove = new HashSet<>();
        for (String fileName: currBlobKeys) {
            if (!branchHeadBlobKeys.contains(fileName)) {
                Utils.restrictedDelete(Utils.join(CWD, fileName));
                toRemove.add(fileName);
            }
        }

        for (String fileName: toRemove) {
            currBlobKeys.remove(fileName);
        }
    }

    /** Set up files for comparison */
    private HashSet<String> setUpFileSet(
            HashMap<String, byte[]> activeHeadFiles,
            HashMap<String, byte[]> branchHeadFiles,
            HashMap<String, byte[]> splitPointFiles,
            HashMap<String, byte[]> splitPointFiles2) {

        List<String> workingFiles = plainFilenamesIn(CWD);
        HashSet<String> fileSet = new HashSet<>();

        fileSet.addAll(activeHeadFiles.keySet());
        fileSet.addAll(branchHeadFiles.keySet());
        fileSet.addAll(splitPointFiles.keySet());

        if (workingFiles != null) {
            fileSet.addAll(workingFiles);
        }

        if (splitPointFiles2 != null) {
            fileSet.addAll(splitPointFiles2.keySet());
        }

        return fileSet;
    }

    /** Overwrites a file that caused a merge conflict */
    private void overwriteConflictedFile(
            File file,
            byte[] activeFileContents,
            byte[] branchFileContents) {

        if (branchFileContents != null) {
            Utils.writeContents(
                    file,
                    "<<<<<<< HEAD\n"
                            + new String(activeFileContents, StandardCharsets.UTF_8)
                            + "=======\n"
                            + new String(branchFileContents, StandardCharsets.UTF_8)
                            + ">>>>>>>\n");
        } else {
            Utils.writeContents(
                    file,
                    "<<<<<<< HEAD\n"
                            + new String(activeFileContents, StandardCharsets.UTF_8)
                            + "=======\n"
                            + ">>>>>>>\n");
        }
    }

    /** Get all IDs from a branch's commit history */
    private HashSet<String> getCommitHistoryIds(
            String headCommitId,
            HashMap<String, CommitData> commitHistory,
            boolean forSecondParent) {

        HashSet<String> commitHistoryIds = new HashSet<>();
        String currCommitId = headCommitId;

        if (!forSecondParent) {
            while (currCommitId != null) {
                commitHistoryIds.add(currCommitId);
                CommitData currCommitData = commitHistory.get(currCommitId);
                currCommitId = currCommitData.getCommitParentId();
            }
        } else {
            while (currCommitId != null) {
                commitHistoryIds.add(currCommitId);
                CommitData currCommitData = commitHistory.get(currCommitId);
                currCommitId = currCommitData.getCommitParentId2();
            }
        }
        return commitHistoryIds;
    }

    /** Finds the split point between two branches */
    private String findSplitPoint(String branchHeadId,
                                        HashSet<String> commitHistoryIds,
                                        HashMap<String, CommitData> commitHistory) {
        String currBranchCommitId = branchHeadId;

        while (currBranchCommitId != null) {
            if (commitHistoryIds.contains(currBranchCommitId)) {
                return currBranchCommitId;
            } else {
                CommitData currBranchCommitData = commitHistory.get(currBranchCommitId);
                currBranchCommitId = currBranchCommitData.getCommitParentId();
            }
        }
        return "";
    }

    /** Gets the split point between two branches */
    private String getSplitPoint(String headCommitId,
                                       String branchHeadId,
                                       HashMap<String, CommitData> commitHistory,
                                       Boolean forSecondParent) {
        HashSet<String> activeHistory = getCommitHistoryIds(
                headCommitId,
                commitHistory,
                forSecondParent);
        return findSplitPoint(branchHeadId, activeHistory, commitHistory);
    }

    private boolean checkMergeErrorCases(String branchName) {
        if (!stagingArea.isEmpty() || !removalArea.isEmpty()) {
            Utils.message("You have uncommitted changes.");
            return false;
        } else if (!branches.containsKey(branchName)) {
            Utils.message("A branch with that name does not exist.");
            return false;
        } else if (Objects.equals(branchName, activeBranch)) {
            Utils.message("Cannot merge a branch with itself.");
            return false;
        }

        return true;
    }

    /** Check for special cases in merge */
    private boolean checkMergeSpecialCases(String splitPointId,
                                           String splitPointId2,
                                           String branchHeadId,
                                           String branchName) throws IOException {
        if (Objects.equals(splitPointId, branchHeadId)
                || Objects.equals(splitPointId2, branchHeadId)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            return false;
        }

        if (Objects.equals(splitPointId, headCommit)
                || Objects.equals(splitPointId2, headCommit)) {
            this.checkoutCommand(new String[]{"checkout", branchName});
            Utils.message("Current branch fast-forwarded.");
            return false;
        }

        return true;
    }

    /** Compares files for merge */
    private boolean compareFilesForMerge(
            byte[] activeHeadFileContent,
            byte[] branchHeadFileContent,
            byte[] splitPointFileContent,
            byte[] splitPointFileContent2,
            String fileName,
            String branchHeadId,
            boolean hasSecondParent) throws IOException {
        File newFile = Utils.join(CWD, fileName);
        if (branchHeadFileContent != null) {
            if (Arrays.equals(splitPointFileContent, activeHeadFileContent)
                    && !Arrays.equals(splitPointFileContent, branchHeadFileContent)) {
                this.checkoutCommand(new String[]{"checkout", branchHeadId, "--", fileName});
                this.addCommand(fileName);
            }
            if (hasSecondParent) {
                if (Arrays.equals(splitPointFileContent2, activeHeadFileContent)
                        && !Arrays.equals(splitPointFileContent2, branchHeadFileContent)) {
                    String[] args = new String[]{"checkout", branchHeadId, "--", fileName};
                    this.checkoutCommand(args);
                    this.addCommand(fileName);
                }
            }
            if (activeHeadFileContent != null) {
                if (!Arrays.equals(splitPointFileContent, branchHeadFileContent)
                        && !Arrays.equals(splitPointFileContent, activeHeadFileContent)) {
                    if (activeHeadFileContent != branchHeadFileContent) {
                        overwriteConflictedFile(
                                newFile,
                                activeHeadFileContent,
                                branchHeadFileContent);
                        this.addCommand(fileName);
                        return true;
                    }
                }
                if (hasSecondParent) {
                    if (!Arrays.equals(splitPointFileContent2, branchHeadFileContent)
                            && !Arrays.equals(splitPointFileContent2, activeHeadFileContent)) {
                        if (activeHeadFileContent != branchHeadFileContent) {
                            overwriteConflictedFile(
                                    newFile,
                                    activeHeadFileContent,
                                    branchHeadFileContent);
                            this.addCommand(fileName);
                            return true;
                        }
                    }
                }
            }
            if (splitPointFileContent == null && activeHeadFileContent == null) {
                this.checkoutCommand(new String[]{"checkout", branchHeadId, "--", fileName});
                this.addCommand(fileName);
            }
            if (hasSecondParent) {
                if (splitPointFileContent2 == null && activeHeadFileContent == null) {
                    this.checkoutCommand(new String[]{"checkout", branchHeadId, "--", fileName});
                    this.addCommand(fileName);
                }
            }
        } else {
            if (activeHeadFileContent != null && splitPointFileContent != null) {
                if (!Arrays.equals(activeHeadFileContent, splitPointFileContent)) {
                    overwriteConflictedFile(newFile, activeHeadFileContent, null);
                    this.addCommand(fileName);
                    return true;
                }
                if (Arrays.equals(activeHeadFileContent, splitPointFileContent)) {
                    this.rmCommand(fileName);
                }
            }
            if (hasSecondParent) {
                if (activeHeadFileContent != null && splitPointFileContent2 != null) {
                    if (!Arrays.equals(activeHeadFileContent, splitPointFileContent2)) {
                        overwriteConflictedFile(newFile, activeHeadFileContent, null);
                        this.addCommand(fileName);
                        return true;
                    }
                    if (Arrays.equals(activeHeadFileContent, splitPointFileContent2)) {
                        this.rmCommand(fileName);
                    }
                }
            }
        }
        return false;
    }

    /** Converts forward slashes to java.io.File.separator */
    private String convertSlashes(String remoteLocation) {
        String[] arrOfStr = remoteLocation.split("");
        String result = "";
        for (int i = 0; i < arrOfStr.length; i++) {
            if (Objects.equals(arrOfStr[i], "/")) {
                arrOfStr[i] = java.io.File.separator;
            }
            result += arrOfStr[i];
        }
        return result;
    }

    /** Gets the remote's File object */
    private File getRemoteGitletDir(String remoteLocation) {
        return new File(remoteLocation);
    }

    /** Gets the head commit ID from a remote branch */
    private String getRemoteBranchHead(
            HashMap<String, String> remoteBranches,
            String remoteHead,
            String remoteBranchName) {
        String remoteBranchHead;

        if (!remoteBranches.containsKey(remoteBranchName)) {
            remoteBranches.put(remoteBranchName, remoteHead);
            remoteBranchHead = remoteHead;
        } else {
            remoteBranchHead = remoteBranches.get(remoteBranchName);
        }

        return remoteBranchHead;
    }

    /** Gets commits for push */
    private ArrayList<String> getCommitsForPush(String fromHeadId) {
        ArrayList<String> commits = new ArrayList<>();
        String currId = fromHeadId;
        while (currId != null) {
            commits.add(0, currId);
            CommitData commitData = commitHistory.get(currId);
            currId = commitData.getCommitParentId();
        }

        return commits;
    }

    /** Gets commits for push */
    private ArrayList<String> commitsToPush(String toHeadId, String fromHeadId) {
        ArrayList<String> commits = getCommitsForPush(fromHeadId);
        commits.add(0, toHeadId);
        return commits;
    }

    /** Copies local commits to remote branch */
    private void addCommitsToRemote(
            String remoteHeadId,
            String headCommit,
            HashMap<String, CommitData> remoteHistory,
            File remoteGitletDir) {
        File remoteCommitDir = Utils.join(remoteGitletDir, "commits");
        File remoteBlobDir = Utils.join(remoteGitletDir, "blobs");
        ArrayList<String> commits = commitsToPush(remoteHeadId, headCommit);

        for (String commitId: commits) {
            remoteHistory.put(commitId, commitHistory.get(commitId));
            copyCommitToDir(commitId, COMMIT_DIR, remoteCommitDir);
            copyFilesToDir(commitId, COMMIT_DIR, BLOB_DIR, remoteBlobDir);
        }
    }

    /** Get commits from remote branch */
    private ArrayList<String> fetchCommits(HashMap<String, CommitData> toCommitHistory,
                                                 HashMap<String, CommitData> fromCommitHistory,
                                                 String fromHeadId) {
        ArrayList<String> commits = new ArrayList<>();
        String currId = fromHeadId;

        while (currId != null && !toCommitHistory.containsKey(currId)) {
            commits.add(0, currId);
            CommitData commitData = fromCommitHistory.get(currId);
            currId = commitData.getCommitParentId();
        }

        return commits;
    }

    /** Copies commits from remote branch */
    private void copyRemoteBranch(HashMap<String, CommitData> remoteHistory,
                                  String fromHeadId,
                                  File remoteGitletDir) {
        ArrayList<String> commits = fetchCommits(commitHistory, remoteHistory, fromHeadId);
        File remoteCommitDir = Utils.join(remoteGitletDir, "commits");
        File remoteBlobDir = Utils.join(remoteGitletDir, "blobs");

        for (String commitId: commits) {
            commitHistory.put(commitId, remoteHistory.get(commitId));
            copyCommitToDir(commitId, remoteCommitDir, COMMIT_DIR);
            copyFilesToDir(commitId, remoteCommitDir, remoteBlobDir, BLOB_DIR);
        }
    }

    /** Copies a commit from one directory to another */
    private void copyCommitToDir(String commitId, File fromDir, File toDir) {
        File commitFile = Utils.join(fromDir, commitId);
        byte[] commitFileContents = Utils.readContents(commitFile);
        File newFile = Utils.join(toDir, commitId);
        Utils.writeContents(newFile, commitFileContents);
    }

    /** Copies a file from one directory to another */
    private void copyFilesToDir(String commitId, File commitDir, File fromDir, File toDir) {
        File cfile = Utils.join(commitDir, commitId);

        Commit commit = readObject(cfile, Commit.class);
        HashMap<String, String> blobs = commit.getBlobs();

        for (String id: blobs.values()) {
            File file = Utils.join(fromDir, id);
            byte[] fileContents = Utils.readContents(file);
            File newFile = Utils.join(toDir, id);
            Utils.writeContents(newFile, fileContents);
        }
    }

    /** Prints message */
    private void gitletAlreadyExists() {
        Utils.message(
                "A Gitlet version-control system already exists in the current directory."
        );
    }

    /** Prints message */
    private void untrackedFiles() {
        Utils.message(
                "There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
    }

    /** Prints message */
    private String mergeCommitMessage(String branchName, String activeBranch) {
        return "Merged " + branchName + " into " + activeBranch + ".";
    }
}

