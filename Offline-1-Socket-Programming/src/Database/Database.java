package Database;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Singleton
public class Database {

    private static Database instance = null;

    // list of users
    private List<User> users = new ArrayList<>();
    private List<User> loggedInUsers = new ArrayList<>();
    private HashMap<String, User> userMap = new HashMap<>();
    private HashMap<Integer, FileRequest> fileRequests = new HashMap<>();
    private HashMap<Integer, UserFile> fileMap = new HashMap<>();
    private long totalChunkSize = 0;
    private int fileID = 0;
    private int requestID = 0;

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

            for(int i = 1; i <= 5; i++) {
                String filename = "File_" + username + "_public" + i + ".txt";
                var file = new UserFile(getNewFileID(), filename, "public", user);
                user.addFile(file);
                fileMap.put(file.getFileID(), file);

                filename = "File_" + username + "_private" + i + ".txt";
                file = new UserFile(getNewFileID(), filename, "private", user);
                user.addFile(file);
                fileMap.put(file.getFileID(), file);
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
        userMap.put(username, user);

        System.out.println("Added user : " + user.username);
        // create a folder for the user with the username
        new File("Files/" + username + "/private").mkdirs();
        new File("Files/" + username + "/public").mkdirs();

        return user;
    }

    public User getUser(String username) {
        return userMap.get(username);
    }

    public void addLoggedInUser(User user) {
        loggedInUsers.add(user);
        user.setIsLoggedIn(true);
    }

    public void removeLoggedInUser(User user) {
        user.setIsLoggedIn(false);
        user.setSocket(null);
        loggedInUsers.remove(user);
    }

    public boolean userLoggedIn(String username) {
        return loggedInUsers.contains(getUser(username));
    }

    public List<User> getUserList(){
        return users;
    }

    public String getFilePath(String filename, String owner, String requester){

        User user = getUser(owner);

        if(user == null) return "USER_NOT_FOUND";

        for(var file : user.getPublicFiles()){
            if (file.getFileName().equals(filename))
                return "Files/" + owner + "/public/" + filename;
        }

        for(var file : user.getPrivateFiles()){
            if (file.getFileName().equals(filename)){
                if(requester.equals(owner)) return "Files/" + owner + "/private/" + filename;
                else return "ACCESS_DENIED";
            }
        }
        return "FILE_NOT_FOUND";
    }

    public String getFilePath(int fileID, User requester){
        UserFile file = fileMap.get(fileID);

        if(file == null) return "FILE_NOT_FOUND";

        // if file is public
        if(file.getAccessType().equals("public")) return file.getPath();

        else{
            if (file.getOwner() == requester) return file.getPath();
            else return "ACCESS_DENIED";
        }
    }

    public UserFile getFile(int FileID){
        return fileMap.get(FileID);
    }

    public synchronized long getTotalChunkSize() {
        return totalChunkSize;
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

    public synchronized int getNewRequestID() {
        return ++requestID;
    }

    public void addRequestMessage(User sender, String message){
        for(User user : users){
            if(user != sender){
                user.addMessage(message);
            }
        }
    }

    public List<User> getLoggedInUsers() {
        return  loggedInUsers;
    }

    public void addFileRequest(FileRequest fileRequest){
        fileRequests.put(fileRequest.getRequestID(), fileRequest);
        fileRequest.getRequester().addRequest(fileRequest);
    }

    public FileRequest getFileRequest(int requestID){
        return fileRequests.get(requestID);
    }

    public void addFile(UserFile file) {
        fileMap.put(file.getFileID(), file);
    }

}
