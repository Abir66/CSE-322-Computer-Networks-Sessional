package Client;

import Util.NetworkUtil;

import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientReadThread implements Runnable {

    NetworkUtil textSocket;

    public ClientReadThread(NetworkUtil textSocket) {
        this.textSocket = textSocket;
    }

    @Override
    public void run() {
        while (!textSocket.isClosed()){
            try {
                String response = (String) textSocket.read();
                System.out.println("here : " + response);
                System.out.print("> ");
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
