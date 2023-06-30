package Database;

import Util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class User {
    String username;
    NetworkUtil textSocket;
    boolean isLoggedIn = false;

    ArrayList<UserFile> publicFiles = new ArrayList<>();
    ArrayList<UserFile> privateFiles = new ArrayList<>();
    List<String> messages = new ArrayList<>();
    int lastSeenMessage = 0;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public NetworkUtil getSocket() {
        return textSocket;
    }

    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(Boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public void setSocket(NetworkUtil socket) {
        this.textSocket = socket;
    }

    public ArrayList<UserFile> getPublicFiles() {
        return publicFiles;
    }

    public ArrayList<UserFile> getPrivateFiles() {
        return privateFiles;
    }

    public void addFile(UserFile file) {
        if(file.getAccessType().equalsIgnoreCase("public")){
            publicFiles.add(file);
        }else{
            privateFiles.add(file);
        }
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages(boolean unseenOnly) {
        int temp = lastSeenMessage;
        lastSeenMessage = messages.size();

        if(!unseenOnly) return messages;
        return messages.subList(temp, messages.size());
    }

    @Override
    public String toString() {
        return username;
    }

}
