package gitlet;

import java.io.Serializable;

import static gitlet.Utils.serialize;

public class Helpers {
    /** Takes an object and serializes it. Once serialized, creates a
     * SHA-1 hash. Returns a FileData instance that contains both the SHA-1
     * hash and the serialized object. */
    public static FileData getObjectAndId(Object object) {
        byte[] serializedObject = serialize((Serializable) object);
        String objectId = Utils.sha1(serializedObject);
        return new FileData(objectId, serializedObject);
    }
}
