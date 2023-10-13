package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


import static gitlet.Main.CWD;
import static gitlet.Main.GITLET_DIR;
import static gitlet.Utils.*;

public class Helpers {
    /** Takes an object and serializes it. Once serialized, creates a
     * SHA-1 hash. Returns a FileData instance that contains both the SHA-1
     * hash and the serialized object. */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
    private static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    private static File BLOB_DIR = Utils.join(GITLET_DIR, "blobs");

    public static FileData getObjectAndId(Object object) {
        byte[] serializedObject = serialize((Serializable) object);
        String objectId = Utils.sha1(serializedObject);
        return new FileData(objectId, serializedObject);
    }

    /** reads Commit object from a file named the given id and returns it */
    public static Commit getCommit(String commitId) {
        File file = Utils.join(COMMIT_DIR, commitId);
        return readObject(file, Commit.class);
    }

    /** reads Blob object from a file named the given id and returns it */
    public static Blob getBlob(String blobId) {
        File file = Utils.join(BLOB_DIR, blobId);
        return readObject(file, Blob.class);
    }

    /** returns true if the given file is in the given directory, false otherwise */
    public static Boolean isFileInDir(File dir, String file) {
        List<String> filesInDir = plainFilenamesIn(dir);
        return filesInDir != null && filesInDir.contains(file);
    }

    /** takes the given fileName,
     * creates a new File object in the given dir, turns the file's contents to a byte[],
     * creates a new Blob with the given fileName and the byte[] and returns it
     *
     * @param dir File
     * @param fileName String
     * @return Blob
     */
    public static Blob fileToBlob(File dir, String fileName) {
        File originalFile = Utils.join(dir, fileName);
        byte[] fileContents = readContents(originalFile);
        return new Blob(fileName, fileContents);
    }

    /**
     *
     *
     * @param fileId String
     * @param fileName String
     *
     */
    public static void overwriteWorkingFile(
            String fileId,
            String fileName,
            File blobDir) {
        Blob fileObject = Helpers.getBlob(fileId);
        byte[] fileContents = fileObject.getFileContents();
        File originalFile = Utils.join(CWD, fileName);
        Utils.writeContents(originalFile, fileContents);
    }

    public static HashMap<String, byte[]> getFilesFromCommit(Commit commit) {
        HashMap<String, String> blobs = commit.getBlobs();
        Set<String> blobKeys = blobs.keySet();
        HashMap<String, byte[]> fileContents = new HashMap<>();

        for (String fileName: blobKeys) {
            String id = blobs.get(fileName);
            Blob blob = Helpers.getBlob(id);
            fileContents.put(fileName, blob.getFileContents());
        }

        return fileContents;
    }

    public static HashSet<String> setUpFileSet(
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

    public static void overwriteConflictedFile(
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

    public static Commit createCommit(
            String parentId,
            String parentId2,
            String message,
            HashMap<String, String> blobs) {

        Date timeStamp = Date.from(Instant.now());
        String formattedDate = SDF.format(timeStamp);
        return new Commit(parentId, parentId2, message, blobs, formattedDate);
    }

    public static boolean isShortenedId(String shortenedId, String fullId) {
        return fullId.startsWith(shortenedId);
    }

    public static String getFullCommitId(String shortenedId) {
        List<String> commitIdList = plainFilenamesIn(COMMIT_DIR);

        assert commitIdList != null;
        for (String fullId: commitIdList) {
            if (Helpers.isShortenedId(shortenedId, fullId)) {
                return fullId;
            }
        }

        return null;
    }

    public static boolean isStringNotNull(String string) {
        return string != null;
    }

    public static HashSet<String> getCommitHistoryIds(
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

    public static String findSplitPoint(String branchHeadId,
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

    public static String getSplitPoint(String headCommitId,
                                       String branchHeadId,
                                       HashMap<String, CommitData> commitHistory,
                                       Boolean forSecondParent) {
        HashSet<String> activeHistory = Helpers.getCommitHistoryIds(
                headCommitId,
                commitHistory,
                forSecondParent);
        return Helpers.findSplitPoint(branchHeadId, activeHistory, commitHistory);
    }

    public static void findUntrackedFiles() {
        List<String> workingFiles = plainFilenamesIn(CWD);

        if (workingFiles != null) {
            for (String fileName : workingFiles) {
                Blob blob = Helpers.fileToBlob(CWD, fileName);
                FileData blobData = Helpers.getObjectAndId(blob);
                if (!Helpers.isFileInDir(BLOB_DIR, blobData.id)) {
                    Messages.untrackedFiles();
                    return;
                }
            }
        }
    }

    public static void deleteFilesNotInCommit(Set<String> blobKeys) {
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
    public static void deleteFilesNotInBranch(Set<String> currBlobKeys,
                                              Set<String> branchHeadBlobKeys) {
        for (String fileName: currBlobKeys) {
            if (!branchHeadBlobKeys.contains(fileName)) {
                Utils.restrictedDelete(Utils.join(CWD, fileName));
            }
        }
    }

    public static void verifyCheckoutArgs(String[] args) {
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

    public static Set<String> getBlobKeys(String commitId) {
        Commit commit = Helpers.getCommit(commitId);
        HashMap<String, String> blobs = commit.getBlobs();
        return blobs.keySet();
    }

    public static boolean checkMergeErrorCases(HashMap<String, String> stagingArea,
                                            ArrayList<String> removalArea,
                                            HashMap<String, String> branches,
                                            String branchName,
                                            String activeBranch) {

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

    public static boolean checkMergeSpecialCases(String splitPointId,
                                              String splitPointId2,
                                              String branchHeadId,
                                              String headCommitId,
                                              String branchName,
                                              Repository repo) throws IOException {

        if (Objects.equals(splitPointId, branchHeadId)
                || Objects.equals(splitPointId2, branchHeadId)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            return false;
        }

        if (Objects.equals(splitPointId, headCommitId)
                || Objects.equals(splitPointId2, headCommitId)) {
            repo.checkoutCommand(new String[]{"checkout", branchName});
            Utils.message("Current branch fast-forwarded.");
            return false;
        }

        return true;
    }

    public static boolean compareFilesForMerge(
            byte[] activeHeadFileContent,
            byte[] branchHeadFileContent,
            byte[] splitPointFileContent,
            byte[] splitPointFileContent2,
            String fileName,
            String branchHeadId,
            boolean hasSecondParent,
            Repository repo) throws IOException {
        File newFile = Utils.join(CWD, fileName);

        if (branchHeadFileContent != null) {
            if (Arrays.equals(splitPointFileContent, activeHeadFileContent)
                    && !Arrays.equals(splitPointFileContent, branchHeadFileContent)) {
                repo.checkoutCommand(new String[]{"checkout", branchHeadId, "--", fileName});
                repo.addCommand(fileName);
            }
            if (hasSecondParent) {
                if (Arrays.equals(splitPointFileContent2, activeHeadFileContent)
                        && !Arrays.equals(splitPointFileContent2, branchHeadFileContent)) {
                    String[] args = new String[]{"checkout", branchHeadId, "--", fileName};
                    repo.checkoutCommand(args);
                    repo.addCommand(fileName);
                }
            }

            if (activeHeadFileContent != null) {
                if (!Arrays.equals(splitPointFileContent, branchHeadFileContent)
                        && !Arrays.equals(splitPointFileContent, activeHeadFileContent)) {
                    if (activeHeadFileContent != branchHeadFileContent) {
                        Helpers.overwriteConflictedFile(
                                newFile,
                                activeHeadFileContent,
                                branchHeadFileContent);
                        repo.addCommand(fileName);
                        return true;
                    }
                }
                if (hasSecondParent) {
                    if (!Arrays.equals(splitPointFileContent2, branchHeadFileContent)
                            && !Arrays.equals(splitPointFileContent2, activeHeadFileContent)) {
                        if (activeHeadFileContent != branchHeadFileContent) {
                            Helpers.overwriteConflictedFile(
                                    newFile,
                                    activeHeadFileContent,
                                    branchHeadFileContent);
                            repo.addCommand(fileName);
                            return true;
                        }
                    }
                }
            }

            if (splitPointFileContent == null && activeHeadFileContent == null) {
                repo.checkoutCommand(new String[]{"checkout", branchHeadId, "--", fileName});
                repo.addCommand(fileName);
            }
            if (hasSecondParent) {
                if (splitPointFileContent2 == null && activeHeadFileContent == null) {
                    repo.checkoutCommand(new String[]{"checkout", branchHeadId, "--", fileName});
                    repo.addCommand(fileName);
                }
            }
        } else {
            if (activeHeadFileContent != null && splitPointFileContent != null) {
                if (!Arrays.equals(activeHeadFileContent, splitPointFileContent)) {
                    Helpers.overwriteConflictedFile(newFile, activeHeadFileContent, null);
                    repo.addCommand(fileName);
                    return true;
                }

                if (Arrays.equals(activeHeadFileContent, splitPointFileContent)) {
                    repo.rmCommand(fileName);
                }
            }
            if (hasSecondParent) {
                if (activeHeadFileContent != null && splitPointFileContent2 != null) {
                    if (!Arrays.equals(activeHeadFileContent, splitPointFileContent2)) {
                        Helpers.overwriteConflictedFile(newFile, activeHeadFileContent, null);
                        repo.addCommand(fileName);
                        return true;
                    }

                    if (Arrays.equals(activeHeadFileContent, splitPointFileContent2)) {
                        repo.rmCommand(fileName);
                    }
                }
            }
        }
        return false;
    }

    public static String convertSlashes(String remoteLocation) {
        String[] arrOfStr = remoteLocation.split("");
        String result = "";
        for (int i = 0; i < arrOfStr.length; i++ ) {
            if (Objects.equals(arrOfStr[i], "/")) {
                arrOfStr[i] = java.io.File.separator;
            }
            result += arrOfStr[i];
        }
        return result;
    }

    public static File getRemoteGitletDir(String remoteLocation) {
        return new File(remoteLocation);
    }

    public static String getRemoteBranchHead(
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

    public static ArrayList<String> getCommitsForPush(HashMap<String, CommitData> commitHistory,
                                                 String toHeadId,
                                                 String fromHeadId) {
        ArrayList<String> commits = new ArrayList<>();
        String currId = fromHeadId;
        while (currId != null) {
            commits.add(0, currId);
            CommitData commitData = commitHistory.get(currId);
            currId = commitData.getCommitParentId();
        }

        return commits;
    }

    public static ArrayList<String> fetchCommits(HashMap<String, CommitData> toCommitHistory,
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

    public static void copyRemoteBranch(HashMap<String, CommitData> commitHistory,
                                        HashMap<String, CommitData> remoteHistory,
                                        String toHeadId,
                                        String fromHeadId,
                                        File remoteGitletDir) {
        ArrayList<String> commits = Helpers.fetchCommits(commitHistory, remoteHistory, fromHeadId);
        File remoteCommitDir = Utils.join(remoteGitletDir, "commits");
        File remoteBlobDir = Utils.join(remoteGitletDir, "blobs");

        for (String commitId: commits) {
            commitHistory.put(commitId, remoteHistory.get(commitId));
            Helpers.copyCommitToDir(commitId, remoteCommitDir, COMMIT_DIR);
            Helpers.copyFilesToDir(commitId, remoteCommitDir, remoteBlobDir, BLOB_DIR);
        }
    }

    public static ArrayList<String> commitsToPush(HashMap<String, CommitData> commitHistory,
                                                  String toHeadId,
                                                  String fromHeadId) {
        ArrayList<String> commits = Helpers.getCommitsForPush(commitHistory, toHeadId, fromHeadId);
        commits.add(0, toHeadId);
        return commits;
    }

    public static void addCommitsToRemote(
            HashMap<String, CommitData> commitHistory,
            String remoteHeadId,
            String headCommit,
            Repository remoteRepo,
            HashMap<String, CommitData> remoteHistory,
            File remoteGitletDir) {
        File remoteCommitDir = Utils.join(remoteGitletDir, "commits");
        File remoteBlobDir = Utils.join(remoteGitletDir, "blobs");
        ArrayList<String> commits = Helpers.commitsToPush(commitHistory, remoteHeadId, headCommit);

        for (String commitId: commits) {
            remoteHistory.put(commitId, commitHistory.get(commitId));
            Helpers.copyCommitToDir(commitId, COMMIT_DIR, remoteCommitDir);
            Helpers.copyFilesToDir(commitId, COMMIT_DIR, BLOB_DIR, remoteBlobDir);
        }
    }

    public static void copyCommitToDir(String commitId, File fromDir, File toDir) {
        File commitFile = Utils.join(fromDir, commitId);
        byte[] commitFileContents = Utils.readContents(commitFile);
        File newFile = Utils.join(toDir, commitId);
        Utils.writeContents(newFile, commitFileContents);
    }

    public static void copyFilesToDir(String commitId, File commitDir, File fromDir, File toDir) {
//        Commit commit = Helpers.getCommit(commitId);
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
}









