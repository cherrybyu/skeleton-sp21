package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import static gitlet.Utils.*;

public class Helpers {
    /** Takes an object and serializes it. Once serialized, creates a
     * SHA-1 hash. Returns a FileData instance that contains both the SHA-1
     * hash and the serialized object. */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");

    public static FileData getObjectAndId(Object object) {
        byte[] serializedObject = serialize((Serializable) object);
        String objectId = Utils.sha1(serializedObject);
        return new FileData(objectId, serializedObject);
    }

    /** reads Commit object from a file named the given id and returns it */
    public static Commit getCommit(File dir, String commitId) {
        File file = Utils.join(dir, commitId);
        return readObject(file, Commit.class);
    }

    /** reads Blob object from a file named the given id and returns it */
    public static Blob getBlob(File dir, String blobId) {
        File file = Utils.join(dir, blobId);
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
            File cwd,
            File blobDir) {
        Blob fileObject = Helpers.getBlob(blobDir, fileId);
        byte[] fileContents = fileObject.getFileContents();
        File originalFile = Utils.join(cwd, fileName);
        Utils.writeContents(originalFile, fileContents);
    }

    public static HashMap<String, byte[]> getFilesFromCommit(Commit commit, File blobDir) {
        HashMap<String, String> blobs = commit.getBlobs();
        Set<String> blobKeys = blobs.keySet();

        HashMap<String, byte[]> fileContents = new HashMap<>();
        for (String fileName: blobKeys) {
            String id = blobs.get(fileName);
            Blob blob = Helpers.getBlob(blobDir, id);
            fileContents.put(fileName, blob.getFileContents());
        }

        return fileContents;
    }

    public static HashMap<String, byte[]> setUpFileList(
            HashMap<String, byte[]> activeHeadFiles,
            HashMap<String, byte[]> branchHeadFiles,
            HashMap<String, byte[]> splitPointFiles,
            List<String> workingFiles) {

        HashMap <String, byte[]> fileList = new HashMap<>();

        for (String fileName: activeHeadFiles.keySet()) {
            if (!fileList.containsKey(fileName)) {
                fileList.put(fileName, null);
            }
        }

        for (String fileName: branchHeadFiles.keySet()) {
            if (!fileList.containsKey(fileName)) {
                fileList.put(fileName, null);
            }
        }

        for (String fileName: splitPointFiles.keySet()) {
            if (!fileList.containsKey(fileName)) {
                fileList.put(fileName, null);
            }
        }

        for (String fileName: workingFiles) {
            if (!fileList.containsKey(fileName)) {
                fileList.put(fileName, null);
            }
        }

        return fileList;
    }

    public static void overwriteConflictedFile(
            File file,
            byte[] activeFileContents,
            byte[] branchFileContents) {

        Utils.writeContents(
                file,
                "<<<<<<< HEAD\n"
                        + new String(activeFileContents)
                        + "=======\n"
                        + new String(branchFileContents)
                        + ">>>>>>>");
    }

    public static Commit createCommit(
            String parentId,
            String parentId2,
            String message,
            HashMap<String, String> blobs) {

        Date timeStamp = Date.from(Instant.now());
        String formattedDate = sdf.format(timeStamp);
        return new Commit(parentId, parentId2, message, blobs, formattedDate);
    }

}

