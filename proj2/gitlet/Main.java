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
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            return;
        }
        try {
            String firstArg = args[0];
            String fileName;
            String message;
            String branchName;
            String commitId;
            Repository currRepo;
            switch(firstArg) {
                case "init":
                    // `init` command
                    Repository newRepo = new Repository();
                    newRepo.initCommand();
                    repository.createNewFile();
                    Utils.writeObject(repository, newRepo);
                    break;
                case "add":
                    // `add [filename]` command
                    fileName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.addCommand(fileName);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "commit":
                    // `commit [log message]` command
                    message = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.commitCommand(message);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "rm":
                    // `rm [fileName]` command
                    fileName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.rmCommand(fileName);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "log":
                    // `log` command
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.logCommand();
                    Utils.writeObject(repository, currRepo);
                    break;
                case "global-log":
                    // `global-log` command
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.globalLogCommand();
                    Utils.writeObject(repository, currRepo);
                    break;
                case "find":
                    // `find [commit message]` command
                    message = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.findCommand(message);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "status":
                    // `status` command
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.statusCommand();
                    Utils.writeObject(repository, currRepo);
                    break;
                case "checkout":
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.checkoutCommand(args);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "branch":
                    // `branch [branchName]` command
                    branchName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.branchCommand(branchName);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "rm-branch":
                    // TODO: handle the `rm-branch [branchName]` command
                    branchName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.rmBranchCommand(branchName);
                    Utils.writeObject(repository, currRepo);
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
