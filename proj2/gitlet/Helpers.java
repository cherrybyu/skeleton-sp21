package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static gitlet.Utils.*;

public class Helpers {
    /** Takes an object and serializes it. Once serialized, creates a
     * SHA-1 hash. Returns a FileData instance that contains both the SHA-1
     * hash and the serialized object. */
    public static FileData getObjectAndId(Object object) {
        byte[] serializedObject = serialize((Serializable) object);
        String objectId = Utils.sha1(serializedObject);
        return new FileData(objectId, serializedObject);
    }

    public static Commit getCommit(File dir, String commitId) {
        File file = Utils.join(dir, commitId);
        return readObject(file, Commit.class);
    }

    public static Blob getBlob(File dir, String blobId) {
        File file = Utils.join(dir, blobId);
        return readObject(file, Blob.class);
    }

    public static Boolean isFileInDir(File dir, String sha1) {
        List<String> filesInDir = plainFilenamesIn(dir);
        return filesInDir != null && filesInDir.contains(sha1);
    }
}

