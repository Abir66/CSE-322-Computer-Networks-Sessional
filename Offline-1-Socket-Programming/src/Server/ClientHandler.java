package Server;

import Database.*;
import Util.NetworkUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    NetworkUtil textSocket;
    NetworkUtil fileSocket;
    NetworkUtil fileUploadSocket;
    Database database;

    public ClientHandler(NetworkUtil socket, NetworkUtil fileSocket, NetworkUtil fileUploadSocket) {
        this.textSocket = socket;
        this.fileSocket = fileSocket;
        this.fileUploadSocket = fileUploadSocket;
        database = Database.getInstance();
    }

    @Override
    public void run() {

        String username = null;
        try {
            username = (String) textSocket.read();

            // if logged in already send error message and close connection
            if (database.userLoggedIn(username)) {
                textSocket.write("DUPLICATE_USER_ERROR");
                textSocket.closeConnection();
                fileSocket.closeConnection();
                fileUploadSocket.closeConnection();
                return;
            }

            User user = database.getUser(username);

            // if user does not exist, add it to the database
            if (user == null) user = database.addUser(username);

            // add user to logged in users
            database.addLoggedInUser(user);

            // send success message
            textSocket.write("SUCCESS");

            System.out.println("User - " + username + " logged in successfully");

            // set user in the sockets
            textSocket.setUser(user);
            fileSocket.setUser(user);
            fileUploadSocket.setUser(user);

            user.setSocket(textSocket);

            // create a new thread for the client
            Thread readThread = new Thread(new ServerReadThread(textSocket, fileSocket, fileUploadSocket, user));
            readThread.start();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
