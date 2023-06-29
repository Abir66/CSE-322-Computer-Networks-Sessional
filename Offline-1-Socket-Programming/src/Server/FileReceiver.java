package Server;

import Database.Database;
import Util.NetworkUtil;

import java.io.File;
import java.io.FileOutputStream;

public class FileReceiver implements Runnable {

    private NetworkUtil fileUploadSocket;
    private int fileID;
    private int chunkSize;
    private String saveAs;
    private String username;
    private String privacy;
    private long filesize;
    private Database database = Database.getInstance();

    public FileReceiver(NetworkUtil fileUploadSocket, int fileID, String privacy, String filename, long filesize, int chunkSize, String username) {
        this.fileUploadSocket = fileUploadSocket;
        this.fileID = fileID;
        this.saveAs = filename;
        this.username = username;
        this.privacy = privacy;
        this.filesize = filesize;
        this.chunkSize = chunkSize;
    }

    @Override
    public void run() {

        System.out.println("FileReceiver started");

        System.out.println("Downloading file: " + saveAs + " from " + username);

        FileOutputStream fileOutputStream = null;
        File file = new File("Files/" + username + "/" + privacy + "/" + saveAs);
        try {

            System.out.println("here...");
            fileUploadSocket.write(chunkSize + " " + fileID);

            if (!file.exists()) file.createNewFile();

            // receive bytes in chunks of and send acknowledgement after each chunk
            int bytes = 0;
            fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[chunkSize];
            int cnt = 0;

            while (filesize > 0 && (bytes = fileUploadSocket.getOIS().read(buffer, 0, (int) Math.min(buffer.length, filesize))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                filesize -= bytes;
                database.removeChunk(bytes);

                // send acknowledgement
                fileUploadSocket.write("ACK received Chunk#" + cnt++ + " " + bytes + " bytes");
            }



            if(filesize > 0) {
                System.out.println("File not completely received. Deleting file.");
                fileUploadSocket.write("ERROR_UPLOADING_FILE");
            }

            else {
                System.out.println("File received successfully.");
                fileUploadSocket.write("FILE_RECEIVED");
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
                if(filesize > 0){
                    file.delete();
                    database.removeChunk(filesize);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
