# Gitlet Design Document

**Name**: Hannah Nguyen

## Classes and Data Structures

### Repository
#### Fields

1. File COMMIT_DIR - directory to store serialized commits
2. File BLOB_DIR - directory to store blobs
3. HashMap<String, String> remotes - Mapping of remote directories' names to their file paths
4. HashMap<String, String> branches - Mapping of branch names to their branch's head commit's ID
5. HashMap<String, CommitData> - Mapping of commits' IDs to their CommitData object
6. HashMap<String, String> stagingArea - Mapping of file names staged for addition to their ID
7. List<String> removalArea - List of file names staged for removal
8. String activeBranch - ID of current branch
9. String headCommit - ID of the most recent commit in the current branch

### Commit
#### Fields

1. String parentId - SHA-1 hash of parent commit 
2. String parentId2 - SHA-1 hash of second parent commit
3. String timestamp = Time of commit creation
4. String message - Commit message
5. HashMap<String, String> - a mapping of file names to blob references

### Blob
#### Fields

1. String fileName - name of file in the staging area (wug.txt)
2. byte[] fileContents - data from file

## Persistence
Every time something is to be modified, read object from file, modify that object, write it back into a file.
