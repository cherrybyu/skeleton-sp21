# Gitlet Design Document

**Name**: Hannah Nguyen

## Classes and Data Structures

### Commit

#### Fields


1. parentId - SHA-1 hash of parent commit 
2. and (for merges) a second parent reference 
3. timestamp
4. message
5. a mapping of file names to blob references 




### Blob
#### Fields

1. fileName - name of file in the staging area (wug.txt)
2. fileContents - data from file in cwd written into a new file


### Repository

#### Fields

1. commits - directory to store serialized commits
2. blobs - directory to store blobs
3. 
4. branches?!?!?!?! EUGH


## Algorithms

## Persistence


