package Server;

import Util.NetworkUtil;

import java.io.*;
import java.net.Socket;

public class FileSender implements Runnable{

    private NetworkUtil fileSocket;
    private String filepath;
    private ObjectOutputStream fout;

    public FileSender(NetworkUtil fileSocket, String filepath){
        this.fileSocket = fileSocket;
        this.filepath = filepath;
        fout = fileSocket.getOOS();
    }


    @Override
    public void run() {
        try {
            sendFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile() throws IOException {
        System.out.println("Trying Filepath: " + filepath);

        int bytes = 0;
        File file = new File(filepath);
        FileInputStream fileInputStream = new FileInputStream(file);

        // send file size
        fout.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4*1024];

        while ((bytes=fileInputStream.read(buffer))!=-1){
            fout.write(buffer,0,bytes);
            fout.flush();
        }
        fileInputStream.close();
    }

}
