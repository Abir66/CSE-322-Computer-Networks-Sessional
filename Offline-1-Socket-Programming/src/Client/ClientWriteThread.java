package Client;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientWriteThread implements Runnable {

    Socket socket;
    ObjectOutputStream out;
    String username;

    ClientWriteThread(Socket socket, ObjectOutputStream out, String username) {
        this.socket = socket;
        this.out = out;
        this.username = username;
    }

    @Override
    public void run() {

        String input, command;
        Scanner scanner = new Scanner(System.in);

        while (!socket.isClosed()) {

            System.out.print("> ");
            input = scanner.nextLine();

            // split the input into command and argument
            String[] inputArray = input.split(" ", 2);
            command = inputArray[0];
            try {
                if (command.equalsIgnoreCase("logout")) {
                    out.writeObject(input);
                    out.flush();
                    socket.close();
                    return;
                }

                else if (command.equalsIgnoreCase("userlist")) {
                    out.writeObject(input);
                    out.flush();
                }

                else if(command.equalsIgnoreCase("showFiles")){
                    out.writeObject(input);
                    out.flush();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
