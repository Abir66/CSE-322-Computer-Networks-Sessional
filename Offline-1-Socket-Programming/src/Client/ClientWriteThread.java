package Client;

import Util.NetworkUtil;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientWriteThread implements Runnable {

    NetworkUtil textSocket;
    NetworkUtil fileSocket;
    NetworkUtil fileUploadSocket;
    String username;
    String fileViewUsername;

    public ClientWriteThread(NetworkUtil textSocket, NetworkUtil fileSocket, NetworkUtil fileUploadSocket, String username) {
        this.textSocket = textSocket;
        this.fileSocket = fileSocket;
        this.fileUploadSocket = fileUploadSocket;
        this.username = username;
        this.fileViewUsername = username;
    }

    @Override
    public void run() {

        String input, command;
        Scanner scanner = new Scanner(System.in);

        while (!textSocket.isClosed()) {

            System.out.print("> ");
            input = scanner.nextLine();

            // split the input into command and argument
            String[] inputArray = input.split(" ");
            command = inputArray[0];
            try {
                if (command.equalsIgnoreCase("logout")) {
                    textSocket.write(input);
                    return;
                }

                else if (command.equalsIgnoreCase("userlist")) {
                    textSocket.write(input);
                }

                else if(command.equalsIgnoreCase("showFiles")){
                    textSocket.write(input);
                }

                else if(command.equalsIgnoreCase("upload")){

                    // upload private/public [save_as] [req_id=n]
                    System.out.println("Choose a file to upload");
                    // a file picker ui
                    JFileChooser fileChooser = new JFileChooser();
                    int returnValue = fileChooser.showOpenDialog(null);

                    if(returnValue == JFileChooser.APPROVE_OPTION){
                        String filePath = fileChooser.getSelectedFile().getAbsolutePath();

                        // file size
                        File file = new File(filePath);
                        long fileSize = file.length();
                        String fileName = file.getName();
                        System.out.println(fileName);
                        System.out.println(filePath);

                        String message = "upload " + inputArray[1] + " " + fileName + " " + fileSize;
                        if(inputArray.length > 2)
                            message += " " + inputArray[2].split("=")[1];;

                        textSocket.write(message);

                        Thread fileUploaderThread = new Thread(new FileUploaderThread(fileUploadSocket, file));
                        fileUploaderThread.start();
                    }
                }

                else if(command.equalsIgnoreCase("download")) {
                    // download [username] filename
                    String s = input;
                    String filename = inputArray[2];

                    if(inputArray.length == 2) {
                        s = "download " + fileViewUsername + " " + inputArray[1];
                        filename = inputArray[1];
                    }

                    // select a folder to save with JFileChooser
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnValue = fileChooser.showSaveDialog(null);

                    String savePath = null;

                    if(returnValue == JFileChooser.APPROVE_OPTION){
                        savePath = fileChooser.getSelectedFile().getAbsolutePath();
                    }

                    if(savePath == null) savePath = "Downloads/" + username;

                    Thread fileDownloaderThread = new Thread(new FileDownloaderThread(textSocket, fileSocket, filename, savePath));
                    fileDownloaderThread.start();

                    textSocket.write(s);
                }


                if(command.equalsIgnoreCase("showFiles") && inputArray.length == 2) fileViewUsername = inputArray[1];
                else if(!command.equalsIgnoreCase("download")) fileViewUsername = username;


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
