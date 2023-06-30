package Server;

import Database.*;
import Util.NetworkUtil;

import java.io.File;
import java.io.FileOutputStream;

public class FileReceiver implements Runnable {

    private NetworkUtil fileUploadSocket;

    private int chunkSize;
    private User user;
    private long filesize;
    private Database database = Database.getInstance();
    private UserFile userFile;


    public FileReceiver(NetworkUtil fileUploadSocket, UserFile userFile, long filesize, int randomChunkSize) {
        this.fileUploadSocket = fileUploadSocket;
        this.user = userFile.getOwner();
        this.filesize = filesize;
        this.chunkSize = randomChunkSize;
        this.userFile = userFile;
    }

    @Override
    public void run() {

        System.out.println("FileReceiver started");

        System.out.println("Downloading file: " + userFile.getFileName() + " from " + user.getUsername());

        FileOutputStream fileOutputStream = null;
        File file = new File("Files/" + user.getUsername() + "/" + userFile.getAccessType() + "/" + userFile.getFileName());
        try {

            fileUploadSocket.write(chunkSize + " " + userFile.getFileID());

            if (!file.exists()) file.createNewFile();

            System.out.println("File size: " + filesize
                    + " bytes. Chunk size: " + chunkSize + " bytes.");

            int bytes = 0;
            fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[10];
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
                user.addFile(userFile);
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
