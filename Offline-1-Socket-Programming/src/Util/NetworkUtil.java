package Util;

import Database.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class NetworkUtil {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private User user;

    public NetworkUtil(String s, int port) throws IOException {
        this.socket = new Socket(s, port);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }

    public NetworkUtil(Socket s) throws IOException {
        this.socket = s;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }

    public Object read() throws IOException, ClassNotFoundException {
        return ois.readUnshared();
    }

    public void write(Object o) throws IOException {
        oos.writeUnshared(o);
        oos.reset();
        oos.flush();
    }

    public void closeConnection() throws IOException {
        ois.close();
        oos.close();
        socket.close();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public long readLong(){
        try {
            return ois.readLong();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ObjectInputStream getOIS() {
        return ois;
    }

    public ObjectOutputStream getOOS() {
        return oos;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setTimeout(int timeout){
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}