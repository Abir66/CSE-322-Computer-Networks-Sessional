package Database;

public class UserFile {

    private int fileID;
    private String fileName;
    private String accessType;
    private User owner;
    private String path;

    public UserFile(int fileID, String fileName, String accessType, User owner) {
        this.fileID = fileID;
        this.fileName = fileName;
        this.accessType = accessType;
        this.owner = owner;
        this.path = "Files/" + owner.getUsername() + "/" + accessType + "/" + fileName;
    }

    public int getFileID() {
        return fileID;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAccessType() {
        return accessType;
    }

    public User getOwner() {
        return owner;
    }

    public String getPath() {
        return path;
    }


}
