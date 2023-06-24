package Database;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Singleton
public class Database {

    private static Database instance = null;

    // list of users
    private static List<User> users = new ArrayList<>();
    private static HashMap<String, User> loggedInUsers = new HashMap<>();
    //private static HashMap<String, Socket> userSocketMap = new HashMap<>();

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


        // print the files
        for (User user : users) {
            System.out.println("User : " + user.username);
            System.out.println("Public Files : ");
            for (String file : user.getPublicFiles()) {
                System.out.println(file);
            }
            System.out.println("Private Files : ");
            for (String file : user.getPrivateFiles()) {
                System.out.println(file);
            }
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
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void addLoggedInUser(String username, Socket socket) {
        User user = getUser(username);
        addLoggedInUser(user, socket);
    }

    public void addLoggedInUser(User user, Socket socket) {
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

}
