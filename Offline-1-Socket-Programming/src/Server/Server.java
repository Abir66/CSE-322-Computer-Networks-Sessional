package Server;

import Database.Database;
import Util.NetworkUtil;

import java.io.IOException;
import java.net.ServerSocket;


public class Server {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(ENV.SERVER_PORT);
        ServerSocket fileServerSocket = new ServerSocket(ENV.SERVER_FILE_PORT);
        ServerSocket fileUploadServerSocket = new ServerSocket(ENV.FILE_UPLOAD_PORT);

        Database.getInstance();
        System.out.println("Server started............");

        while (true) {
            NetworkUtil textSocket = new NetworkUtil(serverSocket.accept());
            NetworkUtil fileSocket = new NetworkUtil(fileServerSocket.accept());
            NetworkUtil fileUploadSocket = new NetworkUtil(fileUploadServerSocket.accept());
            System.out.println("New client connected");

            // create a new thread for each client
            Thread t = new Thread(new ClientHandler(textSocket, fileSocket, fileUploadSocket));
            t.start();
        }

    }
}