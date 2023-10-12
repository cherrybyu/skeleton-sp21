package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static gitlet.Utils.join;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Hannah Nguyen
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
            String remoteName;
            String remoteBranchName;
            Repository currRepo;

            if (!Objects.equals(firstArg, "init") && !GITLET_DIR.exists()) {
                Utils.message("Not in an initialized Gitlet directory.");
                return;
            }

            switch(firstArg) {
                case "init":
                    // `init` command
                    if (args.length > 1) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    Repository newRepo = new Repository();
                    newRepo.initCommand();
                    repository.createNewFile();
                    Utils.writeObject(repository, newRepo);

                    break;
                case "add":
                    // `add [filename]` command
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    fileName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.addCommand(fileName);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "commit":
                    // `commit [log message]` command
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    message = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.commitCommand(message);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "rm":
                    // `rm [fileName]` command
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    fileName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.rmCommand(fileName);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "log":
                    // `log` command
                    if (args.length > 1) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.logCommand();
                    Utils.writeObject(repository, currRepo);

                    break;
                case "global-log":
                    // `global-log` command
                    if (args.length > 1) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.globalLogCommand();
                    Utils.writeObject(repository, currRepo);

                    break;
                case "find":
                    // `find [commit message]` command
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    message = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.findCommand(message);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "status":
                    // `status` command
                    if (args.length > 1) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.statusCommand();
                    Utils.writeObject(repository, currRepo);
                    break;
                case "checkout":
                    if (args.length > 4) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.checkoutCommand(args);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "branch":
                    // `branch [branchName]` command
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    branchName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.branchCommand(branchName);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "rm-branch":
                    // `rm-branch [branchName]` command
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    branchName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.rmBranchCommand(branchName);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "reset":
                    // TODO: handle the `reset [commitId]` command
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    commitId = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.resetCommand(commitId);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "merge":
                    // TODO: handle the `merge [branchName]` command
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }

                    branchName = args[1];
                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.mergeCommand(branchName);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "add-remote":
                    if (args.length > 3) {
                        Utils.message("Incorrect operands.");
                        break;
                    }
                    remoteName = args[1];
                    String remoteLocation = args[2];

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.addRemoteCommand(remoteName, remoteLocation);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "rm-remote":
                    if (args.length > 2) {
                        Utils.message("Incorrect operands.");
                        break;
                    }
                    remoteName = args[1];

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.rmRemoteCommand(remoteName);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "push":
                    if (args.length > 3) {
                        Utils.message("Incorrect operands.");
                        break;
                    }
                    remoteName = args[1];
                    remoteBranchName = args[2];

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.pushCommand(remoteName, remoteBranchName);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "fetch":
                    if (args.length > 3) {
                        Utils.message("Incorrect operands.");
                        break;
                    }
                    remoteName = args[1];
                    remoteBranchName = args[2];

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.fetchCommand(remoteName, remoteBranchName);
                    Utils.writeObject(repository, currRepo);
                    break;
                case "pull":
                    if (args.length > 3) {
                        Utils.message("Incorrect operands.");
                        break;
                    }
                    remoteName = args[1];
                    remoteBranchName = args[2];

                    currRepo = Utils.readObject(repository, Repository.class);
                    currRepo.pullCommand(remoteName, remoteBranchName);
                    Utils.writeObject(repository, currRepo);

                    break;
                case "":
                    Utils.message("Please enter a command.");
                    break;
                default:
                    Utils.message("No command with that name exists.");
                    break;
            }
        } catch (IOException e) {
            // Handle the exception here (e.g., print an error message)
            e.printStackTrace();
        }
    }
}

