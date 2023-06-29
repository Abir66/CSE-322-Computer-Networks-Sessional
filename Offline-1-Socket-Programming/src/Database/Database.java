package Database;

import Util.NetworkUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Singleton
public class Database {

    private static Database instance = null;

    // list of users
    private List<User> users = new ArrayList<>();
    private HashMap<String, User> loggedInUsers = new HashMap<>();
    private long totalChunkSize = 0;
    private int fileID = 0;

    private Database() {
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
            instance.init();
        }
        return instance;
    }


    public void init(){

        String[] usernames = {"Abir", "Lara", "Saad"};

        for (String username : usernames) {
            var user = addUser(username);

            for(int i = 1; i <= 8; i++) {
                user.addFile("File_" + username + "_public" + i + ".txt", "public");
                user.addFile("File_" + username + "_private" + i + ".txt", "private");
            }
        }


        for(User user : users){
            System.out.println("User : " + user.username);

        }

    }

    public User addUser(String username) {

        // create a new user with the username
        User user = new User(username);
        users.add(user);

        System.out.println("Added user : " + user.username);
        // create a folder for the user with the username
        new File("Files/" + username + "/private").mkdirs();
        new File("Files/" + username + "/public").mkdirs();

        return user;
    }

    public User getUser(String username) {
        System.out.println("Finding user : " + username);
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void addLoggedInUser(String username, NetworkUtil socket) {
        User user = getUser(username);
        addLoggedInUser(user, socket);
    }

    public void addLoggedInUser(User user, NetworkUtil socket) {
        loggedInUsers.put(user.getUsername(), user);
        user.setIsLoggedIn(true);
        user.setSocket(socket);
    }


    public void removeLoggedInUser(String username) {
        User user = loggedInUsers.get(username);
        user.setIsLoggedIn(false);
        user.setSocket(null);
        loggedInUsers.remove(username);
    }


    public boolean userLoggedIn(String username) {
        return loggedInUsers.containsKey(username);
    }

    public List<User> getUserList(){
        return users;
    }

    public String getFilePath(String filename, String owner, String requester){

        User user = getUser(owner);

        if(user == null) return "USER_NOT_FOUND";

        if(user.getPublicFiles().contains(filename))
            return "Files/" + owner + "/public/" + filename;

        if(user.getPrivateFiles().contains(filename)){
            if(requester.equals(owner))
                return "Files/" + owner + "/private/" + filename;
            else
                return "ACCESS_DENIED";
        }

        return "FILE_NOT_FOUND";
    }

    public synchronized long getTotalChunkSize() {
        return totalChunkSize;
    }

    public synchronized void setTotalChunkSize(long totalChunkSize) {
        this.totalChunkSize = totalChunkSize;
    }

    public synchronized void addChunk(long chunkSize) {
        this.totalChunkSize += chunkSize;
    }

    public synchronized void removeChunk(long chunkSize) {
        this.totalChunkSize -= chunkSize;
    }

    public synchronized int getNewFileID() {
        return ++fileID;
    }
}
