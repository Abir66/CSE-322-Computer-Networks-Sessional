package Database;

import java.util.ArrayList;
import java.util.List;

public class FileRequest {
    int requestID;
    String description;
    User requester;
    List<UserFile> files = new ArrayList<>();

    public FileRequest(int requestID, User requester, String description) {
        this.requestID = requestID;
        this.description = description;
        this.requester = requester;
    }

    public int getRequestID() {
        return requestID;
    }

    public String getDescription() {
        return description;
    }

    public User getRequester() {
        return requester;
    }

    public List<UserFile> getFiles() {
        return files;
    }

    public void addFile(UserFile file){
        files.add(file);
    }

    public String getDetails(){
        StringBuilder sb = new StringBuilder();
        sb.append("Request ID: ").append(requestID).append("\n");
        sb.append("Requester: ").append(requester.getUsername()).append("\n");
        sb.append("Description: ").append(description).append("\n");
        if (files.isEmpty()) {
            sb.append("No files uploaded yet");
            return sb.toString();
        }

        sb.append("Files: ").append("\n");
        for(UserFile file : files){
            sb.append("\tFrom : ").append(file.getOwner().getUsername()).append(" , ");
            sb.append("File Name : ").append(file.getFileName()).append("\n");
        }
        return sb.toString();
    }
}
