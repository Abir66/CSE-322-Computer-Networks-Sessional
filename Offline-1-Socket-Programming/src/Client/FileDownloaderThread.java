package Client;

import Server.ENV;
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

        System.out.println("Downloading: " + filename);
        // create a new file in Downloads folder
        try {

//            System.out.println(savePath);
            // if the savepath does not exist, create it
            File savePathFile = new File(savePath);
            if(!savePathFile.exists()) savePathFile.mkdirs();

            File file = new File(savePath + "/" + filename);
            if(!file.exists()) file.createNewFile();

            int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            long size = fileSocket.readLong();     // read file size
            byte[] buffer = new byte[ENV.MAX_CHUNK_SIZE];
            while (size > 0 && (bytes = fileSocket.getOIS().read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;
            }
            fileOutputStream.close();

            System.out.println("Download complete: " + filename);
            System.out.print("> ");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
