package Server;

import Database.Database;
import Database.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ServerReadThread implements Runnable{

    Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;
    String username;
    Database database;


    public ServerReadThread(Socket socket, ObjectInputStream in, ObjectOutputStream out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;
        database = Database.getInstance();
    }

    @Override
    public void run() {

        while(socket.isConnected()){

            try{
                String input = (String) in.readObject();
                String[] inputArray = input.split(" ", 2);
                String command = inputArray[0];

                if(command.equalsIgnoreCase("logout")){
                    database.removeLoggedInUser(username);
                    socket.close();
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
                    out.writeObject(userListString.toString());
                    out.flush();
                }

                else if(command.equalsIgnoreCase("showFiles")){

                    String targetUsername = null;

                    if(inputArray.length > 1) targetUsername = inputArray[1];
                    else targetUsername = username;

                    User targetUser = database.getUser(targetUsername);

                    StringBuilder fileListString = new StringBuilder();

                    if(targetUser == null){
                        fileListString.append("User " + targetUsername + " does not exist\n");
                        out.writeObject(fileListString.toString());
                        out.flush();
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
                    out.writeObject(fileListString.toString());
                    out.flush();
                }




            }catch (Exception e){
                e.printStackTrace();
                break;
            }
        }

        database.removeLoggedInUser(username);

    }
}
