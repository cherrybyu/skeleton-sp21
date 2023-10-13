package gitlet;

import java.io.Serializable;

/** Represents a gitlet blob object.
 *
 *  @author Hannah Nguyen
 */
public class Blob implements Serializable {
    /** File name */
    private String fileName;
    /** File contents */
    private byte[] fileContents;

    /** Class constructor */
    public Blob(String fileName, byte[] fileContents) {
        this.fileName = fileName;
        this.fileContents = fileContents;
    }

    /** Gets file contents */
    public byte[] getFileContents() {
        return fileContents;
    }
}
