package Client;

import Util.NetworkUtil;

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
                System.out.println(response);
                System.out.print("> ");
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
