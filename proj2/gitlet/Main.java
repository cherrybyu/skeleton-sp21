package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.join;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static File repository = join(GITLET_DIR, "repository");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        try {
            String firstArg = args[0];
            String fileName;
            String message;
            String branchName;
            String commitId;
            Repository currRepo;
            switch(firstArg) {
                case "init":
                    // TODO: handle the `init` command
                    Repository newRepo = new Repository();
                    newRepo.initCommand();
                    repository.createNewFile();
                    Utils.writeObject(repository, newRepo);
                    break;
                case "add":
                    // TODO: handle the `add [filename]` command
                    fileName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.addCommand(fileName);
                    Utils.writeObject(repository, currRepo);
    //                Repository repo = Utils.readObject(repository, Repository.class);
    //                System.out.println(repo.stagingArea);
    //                System.out.println(repo.commitHistory.get("473619c1cc5b19a74354c9ccc97abd9f59ac4b4f").commitTimestamp);
                    break;
                // TODO: FILL THE REST IN
                case "commit":
                    // TODO: handle the `commit [log message]` command
                    message = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.commitCommand(message);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "rm":
                    // TODO: handle the `rm [fileName]` command
                    fileName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.rmCommand(fileName);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "log":
                    // TODO: handle the `log` command
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.logCommand();
                    Utils.writeObject(repository, currRepo);
                    break;
                case "global-log":
                    // TODO: handle the `global-log` command
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.globalLogCommand();
                    Utils.writeObject(repository, currRepo);
                    break;
                case "find":
                    // TODO: handle the `find [commit message]` command
                    message = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.findCommand(message);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "status":
                    // TODO: handle the `status` command
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.statusCommand();
                    Utils.writeObject(repository, currRepo);
                    break;
                case "checkout":
                    // TODO: handle the `checkout -- [fileName]` command
                    // TODO: handle the `checkout [commit id] -- [fileName]` command
                    // TODO: handle the `checkout [branchName]` command

                    break;
                case "branch":
                    // TODO: handle the `branch [branchName]` command
                    branchName = args[1];
                    break;
                case "rm-branch":
                    // TODO: handle the `rm-branch [branchName]` command
                    branchName = args[1];
                    break;
                case "reset":
                    // TODO: handle the `reset [commitId]` command
                    commitId = args[1];
                    break;
                case "merge":
                    // TODO: handle the `merge [branchName]` command
                    branchName = args[1];
                    break;
                case "":
                    Utils.message("Please enter a command.");
            }
        } catch (IOException e) {
            // Handle the exception here (e.g., print an error message)
            e.printStackTrace();
        }
    }
}
