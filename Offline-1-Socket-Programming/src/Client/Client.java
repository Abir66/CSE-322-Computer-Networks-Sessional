package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Socket socket = new Socket("localhost", 6666);
        System.out.println("Connection established");

        // buffers
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        // scan username
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username;
        username = scanner.nextLine();

        // send username
        out.writeObject(username);
        out.flush();

        // wait for response
        String response = (String) in.readObject();
        System.out.println(response);

        if (response.equals("DUPLICATE_USER_ERROR")){
            System.out.println("You are already logged in another client.");
            System.out.println("Closing connection...");
            socket.close();
            return;
        }

        Thread writeThread = new Thread(new ClientWriteThread(socket, out, username));
        Thread readThread = new Thread(new ClientReadThread(socket, in));

        writeThread.start();
        readThread.start();
    }
}