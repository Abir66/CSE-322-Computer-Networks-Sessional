package Client;

import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientReadThread implements Runnable {

    Socket socket;
    ObjectInputStream in;

    public ClientReadThread(Socket socket, ObjectInputStream in) {
        this.socket = socket;
        this.in = in;
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                String response = (String) in.readObject();
                System.out.println(response);
                System.out.print("> ");
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
