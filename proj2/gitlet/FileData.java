package gitlet;
class FileData {
    String id;
    byte[] serialized;
    FileData(String objectId, byte[] serializedObject) {
        this.id = objectId;
        this.serialized = serializedObject;
    }
}
