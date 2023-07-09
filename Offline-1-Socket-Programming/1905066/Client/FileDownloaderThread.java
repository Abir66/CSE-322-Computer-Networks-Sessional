package Client;

import Server.ENV;
import Util.NetworkUtil;

import java.io.*;


public class FileDownloaderThread implements Runnable {

    private NetworkUtil fileSocket;
    private String savePath;

    public FileDownloaderThread(NetworkUtil fileSocket, String savePath) {
        this.fileSocket = fileSocket;
        this.savePath = savePath;
    }

    @Override
    public void run() {

        try {

            String confirmation = (String) fileSocket.read();
            if (!confirmation.equalsIgnoreCase("FILE_FOUND")) {
                System.out.println(confirmation);
                System.out.print("> ");
                return;
            }

            String filename = (String) fileSocket.read();
            System.out.println("Downloading: " + filename);

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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
