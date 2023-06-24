package Database;

import java.net.Socket;
import java.util.ArrayList;

public class User {
    String username;
    Socket socket;
    boolean isLoggedIn = false;

    ArrayList<String> publicFiles = new ArrayList<>();
    ArrayList<String> privateFiles = new ArrayList<>();

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(Boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void addPublicFile(String filename) {
        publicFiles.add(filename);
    }

    public void addPrivateFile(String filename) {
        privateFiles.add(filename);
    }

    public ArrayList<String> getPublicFiles() {
        return publicFiles;
    }

    public ArrayList<String> getPrivateFiles() {
        return privateFiles;
    }

    public void addFile(String filename, String fileType) {
        if (fileType.equalsIgnoreCase("public")) {
            addPublicFile(filename);
        } else {
            addPrivateFile(filename);
        }
    }


    @Override
    public String toString() {
        return username;
    }

}
