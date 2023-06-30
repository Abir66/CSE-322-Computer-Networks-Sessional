package Server;

import Database.*;
import Database.User;
import Util.NetworkUtil;

import java.io.EOFException;
import java.io.IOException;


public class ServerReadThread implements Runnable{

    NetworkUtil textSocket;
    NetworkUtil fileSocket;
    NetworkUtil fileUploadSocket;
    User user;
    Database database;

    public ServerReadThread(NetworkUtil textSocket, NetworkUtil fileSocket, NetworkUtil fileUploadSocket, User user) {
        this.textSocket = textSocket;
        this.fileSocket = fileSocket;
        this.fileUploadSocket = fileUploadSocket;
        this.user = user;
        database = Database.getInstance();
    }

    @Override
    public void run() {

        while(!textSocket.isClosed()){

            try{
                String input = (String) textSocket.read();
                System.out.println("> " + user.getUsername() + "\t: " + input);
                String[] inputArray = input.split(" ");
                String command = inputArray[0];

                if(command.equalsIgnoreCase("logout")){
                    database.removeLoggedInUser(user);
                    textSocket.closeConnection();
                    fileSocket.closeConnection();
                    fileUploadSocket.closeConnection();
                    System.out.println("User " + user.getUsername() + " logged out");
                    return;
                }

                else if(command.equalsIgnoreCase("users")){

                    if(inputArray.length > 1) {
                        textSocket.write("Invalid Argument");
                        continue;
                    }

                    var userList = database.getUserList();
                    StringBuilder userListString = new StringBuilder();
                    userListString.append("User List ---------------------------\n");
                    for (User user : userList) {
                        userListString.append(user.getUsername());
                        if (user.getIsLoggedIn()) userListString.append(" (online)");
                        userListString.append("\n");
                    }
                    userListString.append("------------------------------------");
                    textSocket.write(userListString.toString());
                }

                else if(command.equalsIgnoreCase("files")){

                    if(inputArray.length > 2) {
                        textSocket.write("Invalid Arguments");
                        continue;
                    }

                    User targetUser = null;
                    if(inputArray.length == 1) targetUser = user;
                    else  targetUser = database.getUser(inputArray[1]);

                    if(targetUser == null){
                        textSocket.write("User " + inputArray[1] + " does not exist");
                        continue;
                    }

                    StringBuilder fileListString = new StringBuilder();
                    fileListString.append("Showing Files of User : " + targetUser.getUsername() + "\n");
                    fileListString.append("------------------------------------\n");

                    var fileList = targetUser.getPublicFiles();
                    fileListString.append("Public Files : \n");
                    for (var file : fileList) {
                        fileListString.append("\t" + file.getFileName() + "\n");
                    }

                    if (fileList.size() == 0) fileListString.append("\tUser " + targetUser.getUsername() + " has no Public Files\n");

                    if(targetUser == user){
                        fileList = targetUser.getPrivateFiles();
                        fileListString.append("Private Files : \n");
                        for (var file : fileList) {
                            fileListString.append("\t" + file.getFileName() + "\n");
                        }
                    }

                    if (fileList.size() == 0) fileListString.append("\tUser " + targetUser.getUsername() + " has no Private Files\n");
                    fileListString.append("------------------------------------");
                    textSocket.write(fileListString.toString());
                }

                else if(command.equalsIgnoreCase("download")){

                    if(inputArray.length < 3) {
                        textSocket.write("Invalid no of arguments");
                        continue;
                    }

                    String filepath = database.getFilePath(inputArray[2], inputArray[1], user.getUsername());

                    if(!filepath.startsWith("Files/")){
                        textSocket.write(filepath);
                        continue;
                    }

                    Thread fileThread = new Thread(new FileSender(fileSocket, filepath));
                    fileThread.start();
                }


                else if(command.equalsIgnoreCase("upload")){

                    // upload private/public filesize req_id=n filename
                    System.out.println(input);

                    long filesize = Long.parseLong(inputArray[2]);
                    String filename = input.split(" ", 5)[4];
                    int requestID = -1;
                    try {
                        requestID = Integer.parseInt(inputArray[3].split("=")[1]);
                    } catch(Exception e) {
                        fileUploadSocket.write("INVALID REQUEST ID");
                        continue;
                    }

                    if(filesize + database.getTotalChunkSize() > ENV.MAX_BUFFER_SIZE){
                        System.out.println("file size : " + filesize + " total chunk size : " + database.getTotalChunkSize());
                        fileUploadSocket.write("FILE_SIZE_EXCEEDED");
                        continue;
                    }

                    int randomChunkSize = (int) (Math.random() * (ENV.MAX_CHUNK_SIZE - ENV.MIN_CHUNK_SIZE + 1) + ENV.MIN_CHUNK_SIZE);
                    int fileID = database.getNewFileID();

                    UserFile file = new UserFile(fileID, filename, inputArray[1], textSocket.getUser());

                    Thread fileThread = new Thread(new FileReceiver(fileUploadSocket, file, filesize, randomChunkSize, requestID));
                    fileThread.start();
                }

                else if(command.equalsIgnoreCase("make_req")){
                    if(inputArray.length < 2) {
                        textSocket.write("ERR : No description provided");
                        continue;
                    }

                    int requestID = database.getNewRequestID();
                    String description = input.substring(8);
                    User user = textSocket.getUser();

                    String message = "Request ID : " + requestID + "\n" +
                            "Sender : " + user.getUsername() + "\n" +
                            "Description : " + description;

                    new Thread(() -> {
                        database.addRequestMessage(user, message);

                        for(var activeUser : database.getLoggedInUsers()){
                            try{
                                if(user != activeUser)
                                    activeUser.getSocket().write("You have a new message");
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        try{
                            database.addFileRequest(new FileRequest(requestID, user, description));
                            textSocket.write("Request added successfully");
                            textSocket.write(message);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

                else if (command.equalsIgnoreCase("inbox")){

                    if(inputArray.length > 2 || (inputArray.length == 2 && !inputArray[1].equalsIgnoreCase("unseen"))){
                        textSocket.write("Invalid Argument");
                        continue;
                    }

                    boolean unseenOnly = inputArray.length > 1 && inputArray[1].equalsIgnoreCase("unseen");
                    var messages = user.getMessages(unseenOnly);

                    if (messages.size() == 0) {
                        textSocket.write("No messages found"); continue;
                    }

                    StringBuilder messageString = new StringBuilder();
                    messageString.append("Inbox ---------------------------\n");
                    for (var message : messages) {
                        messageString.append(message);
                        messageString.append("\n\n");
                    }
                    messageString.append("------------------------------------");
                    textSocket.write(messageString.toString());
                }

                else if(command.equalsIgnoreCase("request")){

                    if (inputArray.length == 1){
                        var requests = user.getRequests();
                        if (requests.size() == 0) {
                            textSocket.write("No requests found"); continue;
                        }

                        StringBuilder requestString = new StringBuilder();
                        requestString.append("Requests ---------------------------\n");
                        for (var request : requests) {
                            requestString.append(request.getDetails());
                            requestString.append("\n\n");
                        }
                        requestString.append("------------------------------------");
                        textSocket.write(requestString.toString());
                    }

                    else if(inputArray.length > 2 || !inputArray[1].matches("\\d+")){
                        textSocket.write("Invalid Argument");
                    }

                    else{
                        int requestID = Integer.parseInt(inputArray[1]);
                        var request = database.getFileRequest(requestID);

                        if(request == null){
                            textSocket.write("Request not found");
                            continue;
                        }
                        textSocket.write(request.getDetails());
                    }
                }

            }catch (EOFException e){
                System.out.println("Client " + user.getUsername() + " disconnected");
                database.removeLoggedInUser(user);

                try {
                    fileSocket.closeConnection();
                    fileUploadSocket.closeConnection();
                } catch (Exception e1) {
                    break;
                }

                break;
            }
            catch (Exception e){
                e.printStackTrace();
                break;
            }
        }

        database.removeLoggedInUser(user);

    }
}
