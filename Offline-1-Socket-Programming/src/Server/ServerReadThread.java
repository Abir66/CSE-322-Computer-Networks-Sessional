package Server;

import Database.Database;
import Database.User;
import Util.NetworkUtil;


public class ServerReadThread implements Runnable{

    NetworkUtil textSocket;
    NetworkUtil fileSocket;
    NetworkUtil fileUploadSocket;
    String username;
    Database database;


    public ServerReadThread(NetworkUtil textSocket, NetworkUtil fileSocket, NetworkUtil fileUploadSocket, String username) {
        this.textSocket = textSocket;
        this.fileSocket = fileSocket;
        this.fileUploadSocket = fileUploadSocket;
        this.username = username;
        database = Database.getInstance();
    }

    @Override
    public void run() {

        while(!textSocket.isClosed()){

            try{
                String input = (String) textSocket.read();
                String[] inputArray = input.split(" ");
                String command = inputArray[0];

                if(command.equalsIgnoreCase("logout")){
                    database.removeLoggedInUser(username);
                    textSocket.closeConnection();
                    return;
                }

                else if(command.equalsIgnoreCase("userlist")){
                    var userList = database.getUserList();
                    StringBuilder userListString = new StringBuilder();
                    userListString.append("User List ---------------------------\n");
                    System.out.println(userList.size());
                    for (User user : userList) {
                        userListString.append(user.getUsername());
                        System.out.println(user.getUsername());
                        if (user.getIsLoggedIn()) userListString.append(" (online)");
                        userListString.append("\n");
                    }
                    userListString.append("------------------------------------");
                    textSocket.write(userListString.toString());
                }

                else if(command.equalsIgnoreCase("showFiles")){

                    String targetUsername = null;

                    if(inputArray.length > 1) targetUsername = inputArray[1];
                    else targetUsername = username;

                    User targetUser = database.getUser(targetUsername);

                    StringBuilder fileListString = new StringBuilder();

                    if(targetUser == null){
                        fileListString.append("User " + targetUsername + " does not exist\n");
                        textSocket.write(fileListString.toString());
                        continue;
                    }

                    fileListString.append("Showing Files of User : " + targetUsername + "\n");
                    fileListString.append("------------------------------------\n");

                    var fileList = targetUser.getPublicFiles();
                    fileListString.append("Public Files : \n");
                    for (String file : fileList) {
                        fileListString.append(file + "\n");
                    }

                    if (fileList.size() == 0) fileListString.append("User " + targetUsername + " has no Public Files\n");

                    if(targetUsername.equals(username)){
                        fileList = targetUser.getPrivateFiles();
                        fileListString.append("Private Files : \n");
                        for (String file : fileList) {
                            fileListString.append(file + "\n");
                        }
                    }

                    if (fileList.size() == 0) fileListString.append("User " + targetUsername + " has no Private Files\n");

                    fileListString.append("------------------------------------");
                    textSocket.write(fileListString.toString());
                }

                else if(command.equalsIgnoreCase("download")){

                    if(inputArray.length < 3) {
                        textSocket.write("Invalid no of arguments");
                        continue;
                    }

                    String filepath = database.getFilePath(inputArray[2], inputArray[1], username);

                    if(!filepath.startsWith("Files/")){
                        textSocket.write(filepath);
                        continue;
                    }

                    Thread fileThread = new Thread(new FileSender(fileSocket, filepath));
                    fileThread.start();
                }


                else if(command.equalsIgnoreCase("upload")){
                    System.out.println(input);

                    long filesize = Long.parseLong(inputArray[3]);

                    // if file size is greater than 1GB
                    if(filesize + database.getTotalChunkSize() > ENV.MAX_BUFFER_SIZE){
                        System.out.println("file size : " + filesize + " total chunk size : " + database.getTotalChunkSize());
                        fileUploadSocket.write("FILE_SIZE_EXCEEDED");
                        continue;
                    }

                    int randomChunkSize = (int) (Math.random() * (ENV.MAX_CHUNK_SIZE - ENV.MIN_CHUNK_SIZE + 1) + ENV.MIN_CHUNK_SIZE);
                    int fileID = database.getNewFileID();

                    Thread fileThread = new Thread(new FileReceiver(fileUploadSocket, fileID, inputArray[1], inputArray[2], filesize, randomChunkSize, username));
                    fileThread.start();
                }


            }catch (Exception e){
                e.printStackTrace();
                break;
            }
        }

        database.removeLoggedInUser(username);

    }
}
