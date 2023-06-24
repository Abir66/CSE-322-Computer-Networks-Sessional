package Server;

import Database.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    Database database;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        database = Database.getInstance();

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

        String username = null;
        try {
            username = (String) in.readObject();

            // if logged in already send error message and close connection
            if (database.userLoggedIn(username)) {
                out.writeObject("DUPLICATE_USER_ERROR");
                out.flush();
                socket.close();
                return;
            }

            User user = database.getUser(username);

            // if user does not exist, add it to the database
            if (user == null) user = database.addUser(username);

            // add user to logged in users
            database.addLoggedInUser(user, socket);

            // send success message
            out.writeObject("SUCCESS");
            out.flush();

            // create a new thread for the client
            Thread readThread = new Thread(new ServerReadThread(socket, in, out, username));
            readThread.start();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
