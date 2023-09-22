package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        String fileName;
        String message;
        String branchName;
        String commitId;
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                fileName = args[1];

                break;
            // TODO: FILL THE REST IN
            case "commit":
                // TODO: handle the `commit [log message]` command
                message = args[1];

                break;
            case "rm":
                // TODO: handle the `rm [fileName]` command
                fileName = args[1];

                break;
            case "log":
                // TODO: handle the `log` command
                break;
            case "global-log":
                // TODO: handle the `global-log` command
                break;
            case "find":
                // TODO: handle the `find [commit message]` command
                message = args[1];

                break;
            case "status":
                // TODO: handle the `status` command
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
        }
    }
}
