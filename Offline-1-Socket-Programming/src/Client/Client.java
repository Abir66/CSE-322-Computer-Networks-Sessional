package Client;

import Server.ENV;
import Util.NetworkUtil;

import java.io.IOException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        NetworkUtil textSocket = new NetworkUtil(ENV.SERVER_IP, ENV.SERVER_PORT);
        NetworkUtil fileSocket = new NetworkUtil(ENV.SERVER_IP, ENV.SERVER_FILE_PORT);
        NetworkUtil fileUploadSocket = new NetworkUtil(ENV.SERVER_IP, ENV.FILE_UPLOAD_PORT);

        System.out.println("Connection established");

        // scan username
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username;
        username = scanner.nextLine();

        // send username
        textSocket.write(username);

        // wait for response
        String response = (String) textSocket.read();
        System.out.println(response);

        if (response.equals("DUPLICATE_USER_ERROR")){
            System.out.println("> You are already logged in another client.");
            System.out.println("> Closing connection...");
            textSocket.closeConnection();
            return;
        }

        Thread writeThread = new Thread(new ClientWriteThread(textSocket, fileSocket, fileUploadSocket, username));
        Thread readThread = new Thread(new ClientReadThread(textSocket));

        writeThread.start();
        readThread.start();
    }
}