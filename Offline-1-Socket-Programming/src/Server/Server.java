package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket scoket = new ServerSocket(6666);

        while (true) {
            Socket socket = scoket.accept();
            System.out.println("New client connected");

            // create a new thread for each client
            Thread t = new Thread(new ClientHandler(socket));
            t.start();
        }

    }
}