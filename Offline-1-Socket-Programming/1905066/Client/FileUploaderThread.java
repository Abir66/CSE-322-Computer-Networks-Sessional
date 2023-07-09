package Client;

import Database.User;
import Database.UserFile;
import Server.ENV;
import Util.NetworkUtil;

import java.io.*;

public class FileUploaderThread implements Runnable{
    private NetworkUtil fileUploadSocket;
    private File file;
    private ObjectOutputStream fout;


    public FileUploaderThread(NetworkUtil fileUploadSocket, File file) {
        this.fileUploadSocket = fileUploadSocket;
        this.file = file;
        fout = fileUploadSocket.getOOS();
        fileUploadSocket.setTimeout(ENV.SOCKET_TIMEOUT);
    }

    @Override
    public void run() {
        try {
            // read for confirmation from server
            String confirmation = (String) fileUploadSocket.read();

            if (confirmation.equalsIgnoreCase("FILE_SIZE_EXCEEDED") || confirmation.equalsIgnoreCase("INVALID REQUEST ID")) {
                return;
            }

            // split the confirmation to get the chunk size
            String[] confirmationArray = confirmation.split(" ");
            int chunkSize = Integer.parseInt(confirmationArray[0]);
            int fileID = Integer.parseInt(confirmationArray[1]);

            System.out.println("Chunk size: " + chunkSize + ", File ID : " + fileID);

            // send file size
            System.out.println("uploading file: " + file.getName());
            int bytes = 0;
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[chunkSize];

            int cnt = 0;
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                //if(cnt == 2) break;

                fileUploadSocket.write(bytes);
                fout.write(buffer, 0, bytes);
                fout.flush();
                fout.reset();

                // read acknowledgement
                String ack = (String) fileUploadSocket.read();
                System.out.println(ack);
            }

            fileInputStream.close();
            fileUploadSocket.write("Last Chunk");

            confirmation = (String) fileUploadSocket.read();
            System.out.println("> " + confirmation);
            System.out.print("> ");


        }catch (java.net.SocketTimeoutException e){
            System.out.println("Socket timeout");
            try {
                fileUploadSocket.write("SOCKET_TIMEOUT");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
