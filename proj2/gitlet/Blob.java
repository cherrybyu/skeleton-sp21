package gitlet;
//import static gitlet.Utils.*;
//import java.io.File;
import java.io.Serializable;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.TreeMap;
public class Blob implements Serializable {
    public String fileName;
    private byte[] fileContents;

    public Blob(String fileName, byte[] fileContents) {
        this.fileName = fileName;
        this.fileContents = fileContents;
    }

    public byte[] getFileContents() {
        return fileContents;
    }
}
