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
    private ObjectInputStream fin;



    public FileUploaderThread(NetworkUtil fileUploadSocket, File file) {
        this.fileUploadSocket = fileUploadSocket;
        this.file = file;
        fout = fileUploadSocket.getOOS();
        fin = fileUploadSocket.getOIS();
        fileUploadSocket.setTimeout(ENV.SOCKET_TIMEOUT);
    }

    @Override
    public void run() {
        try {
            System.out.println("FileUploaderThread started");
            if (fileUploadSocket.isClosed()) return;

            // read for confirmation from server
            String confirmation = (String) fileUploadSocket.read();
            System.out.println(confirmation);

            if (confirmation.equalsIgnoreCase("FILE_SIZE_EXCEEDED")) {
                System.out.println("File size exceeded");
                return;
            }

            // split the confirmation to get the chunk size
            String[] confirmationArray = confirmation.split(" ");
            int chunkSize = Integer.parseInt(confirmationArray[0]);
            int fileID = Integer.parseInt(confirmationArray[1]);

            System.out.println("Chunk size: " + chunkSize);

            // send file size
            System.out.println("uploading file: " + file.getName());
            int bytes = 0;
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[10];

            System.out.println("starting upload");
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                fout.write(buffer, 0, bytes);
                fout.flush();

                // read acknowledgement
                String ack = (String) fin.readObject();
                System.out.println(ack);
            }

            fileInputStream.close();


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
