package Client;

import Util.NetworkUtil;

import java.io.*;
import java.net.Socket;

public class FileDownloaderThread implements Runnable {

    private NetworkUtil textSocket;
    private NetworkUtil fileSocket;
    private String filename;
    private String savePath;

    public FileDownloaderThread(NetworkUtil textSocket, NetworkUtil fileSocket, String filename, String savePath) {
        this.textSocket = textSocket;
        this.fileSocket = fileSocket;
        this.filename = filename;
        this.savePath = savePath;
    }

    @Override
    public void run() {

        System.out.println("Trying to downlaod: " + filename);
        // create a new file in Downloads folder
        try {

            File file = new File("Downloads/" + filename);
            if(!file.exists()) file.createNewFile();

            int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            long size = fileSocket.readLong();     // read file size
            byte[] buffer = new byte[10];
            while (size > 0 && (bytes = fileSocket.getOIS().read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;
            }
            fileOutputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
