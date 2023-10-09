package gitlet;

import java.io.Serializable;
public class Blob implements Serializable {
    private String fileName;
    private byte[] fileContents;

    public Blob(String fileName, byte[] fileContents) {
        this.fileName = fileName;
        this.fileContents = fileContents;
    }

    public byte[] getFileContents() {
        return fileContents;
    }

    public String getFileName() {
        return fileName;
    }
}
