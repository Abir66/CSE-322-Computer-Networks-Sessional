package Server;

import Database.*;
import Util.NetworkUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileReceiver implements Runnable {

    private NetworkUtil fileUploadSocket;

    private int chunkSize;
    private User user;
    private long filesize;
    private Database database = Database.getInstance();
    private UserFile userFile;
    private int requestID;


    public FileReceiver(NetworkUtil fileUploadSocket, UserFile userFile, long filesize, int randomChunkSize, int requestID) {
        this.fileUploadSocket = fileUploadSocket;
        this.user = userFile.getOwner();
        this.filesize = filesize;
        this.chunkSize = randomChunkSize;
        this.userFile = userFile;
        this.requestID = requestID;
    }

    @Override
    public void run() {

        System.out.println("Receiving file: " + userFile.getFileName() + " from " + user.getUsername());
        database.addChunk(chunkSize);

        FileOutputStream fileOutputStream = null;
        File file = new File("Files/" + user.getUsername() + "/" + userFile.getAccessType() + "/" + userFile.getFileName());
        try {

            fileUploadSocket.write(chunkSize + " " + userFile.getFileID());

            file.createNewFile();

            System.out.println("File size: " + filesize + " bytes. Chunk size: " + chunkSize + " bytes.");

            int bytes = 0;
            fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[chunkSize];
            int cnt = 0;

            var is = fileUploadSocket.getOIS();


            while (filesize > 0) {

                int bytesRead = 0;
                int bytesToRead = 0;

                try {
                    bytesToRead = (int) fileUploadSocket.read();
                } catch (Exception e) {
                    return;
                }

                // System.out.println("bytes to read: " + bytesToRead);

                while (bytesToRead > 0 && fileUploadSocket.getOIS().available() > 0 && (bytes = fileUploadSocket.getOIS().read(buffer, 0, (int) Math.min(chunkSize, filesize))) > 0) {
                    fileOutputStream.write(buffer, 0, bytes);
                    bytesToRead -= bytes;
                    bytesRead += bytes;
                }

                filesize -= bytesRead;
                database.removeChunk(bytesToRead);
                System.out.println("received Chunk#" + cnt + " " + bytesRead + " bytes");
                // send acknowledgement
                fileUploadSocket.write("ACK received Chunk#" + cnt++ + " " + bytesRead + " bytes");
            }

            // read
            fileUploadSocket.read();
            if (filesize == 0) {
                System.out.println("File received successfully.");
                fileUploadSocket.write("FILE_RECEIVED");
                user.addFile(userFile);
                database.addFile(userFile);

                // if requestID is not -1
                if (requestID != -1) {
                    var request = database.getFileRequest(requestID);
                    var requester = request.getRequester();
                    String message = user.getUsername() + " has added a new file in response of your request.\n"
                            + "Request ID: " + requestID + "\n"
                            + "File Name: " + userFile.getFileName() + "\n"
                            + "Description: " + request.getDescription();

                    request.addFile(userFile);
                    requester.addMessage(message);
                    requester.getSocket().write("You have a new message.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (filesize > 0) cancel(file, filesize);
        }
    }

    void cancel(File file, long filesize) {
        try {
            System.out.println("File not completely received. Deleting file.");
            fileUploadSocket.write("ERROR_UPLOADING_FILE");
            file.delete();
            database.removeChunk(filesize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
