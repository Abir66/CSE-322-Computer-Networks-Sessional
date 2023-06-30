package Client;

import Util.NetworkUtil;

import javax.swing.*;
import java.io.File;
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
        System.out.print("> ");

        while (!textSocket.isClosed()) {
            input = scanner.nextLine();

            // split the input into command and argument
            String[] inputArray = input.split(" ");
            command = inputArray[0];
            try {
                if(command.equalsIgnoreCase("help")){
                    System.out.println("Available commands:---------------------");
                    System.out.println("users                           -   shows all users");
                    System.out.println("files [username]                -   shows files of a user. If no username is given, shows own files");
                    System.out.println("make_req description            -   makes a file request to all users");
                    System.out.println("request [req_id=n]              -   shows own requests. If req_id is given, shows details of that request");
                    System.out.println("inbox [unseen]                  -   shows inbox. If unseen is given, shows only unseen messages");
                    System.out.println("upload private/public [req_id=n]-   uploads a file. If req_id is given, uploads to that request");
                    System.out.println("download [username] filename    -   downloads a file. If no username is given, downloads from own files");
                    System.out.println("logout                          -   logs out from the server");
                    System.out.println("---------------------------------------");
                }


                else if (command.equalsIgnoreCase("logout")) {
                    textSocket.write(input);
                    return;
                }

                else if (command.equalsIgnoreCase("users")) {
                    textSocket.write(input);
                }

                else if(command.equalsIgnoreCase("files")){
                    textSocket.write(input);
                }

                else if(command.equalsIgnoreCase("upload")){

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

                        String req_id = "req_id=-1";
                        String message = "upload " + inputArray[1] + " " + fileSize;
                        if(inputArray.length > 2){
                            if(inputArray[2].startsWith("req_id="))
                                req_id = inputArray[2];

                            else{
                                System.out.println("> Invalid command");
                                continue;
                            }
                        }

                        message += " " + req_id;
                        message += " " + fileName;
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

                else if(command.equalsIgnoreCase("make_req")){
                    textSocket.write(input);
                }

                else if(command.equalsIgnoreCase("request")){
                    textSocket.write(input);
                }


                else if(command.equalsIgnoreCase("inbox")){
                    textSocket.write(input);
                }

                else System.out.println("Invalid command");

                if(command.equalsIgnoreCase("showFiles") && inputArray.length == 2) fileViewUsername = inputArray[1];
                else if(!command.equalsIgnoreCase("download")) fileViewUsername = username;

                System.out.print("> ");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
