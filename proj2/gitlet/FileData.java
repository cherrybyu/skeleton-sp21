package gitlet;

class FileData {
    /** SHA-1 hash of file */
    String id;
    /** File contents */
    byte[] contents;

    /** Class constructor */
    FileData(String objectId, byte[] serializedObject) {
        this.id = objectId;
        this.contents = serializedObject;
    }
}
