package gitlet;

public class Messages {

    public static void gitletAlreadyExists() {
        Utils.message(
                "A Gitlet version-control system already exists in the current directory."
        );
    }
    public static void untrackedFiles() {
        Utils.message(
                "There is an untracked file in the way; delete it, or add and commit it first."); //TODO SHORTEN

    }

    public static String mergeCommitMessage(String branchName, String activeBranch) {
        return "Merged " + branchName + " into " + activeBranch + ".";
    }
}
