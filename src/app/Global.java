package app;

import modules.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Stack;

// a static class used to provide shared resources among the controllers

class Global
{
    // SOCKET WITH THE SERVER

    static Socket socketWithServer;
    static ObjectOutputStream toServer;
    static ObjectInputStream fromServer;

    static void setSocketWithServer(Socket s) throws IOException {
        socketWithServer = s;
        toServer = new ObjectOutputStream(socketWithServer.getOutputStream());
        fromServer = new ObjectInputStream(socketWithServer.getInputStream());
    }


    // CURRENT USER ACCOUNT

    public static class CurrentAccount
    {
        private static User currentUser;

        static void update(User user)
        {
            currentUser = user;
        }

        static void reset()
        {
            currentUser = null;
        }

        static User getCurrentUser()
        {
            return currentUser;
        }

        static void display()
        {
            if(currentUser != null)
                currentUser.getAll();
        }
    }












    static String myUserName;

    //socket with the ClientManager

    static Socket socketWithClientManager;
    static ObjectOutputStream toClientManager;
    static ObjectInputStream fromClientManager;

    static void connectWithClientManager(int port)
    {
        try
        {
            socketWithClientManager = new Socket("localhost", port);
            if(socketWithClientManager.isConnected())
            {
                toClientManager = new ObjectOutputStream(socketWithClientManager.getOutputStream());
                fromClientManager = new ObjectInputStream(socketWithClientManager.getInputStream());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    //sockets with chat room objects

    static HashMap<String, connectionPackage> socketMap = new HashMap<>(); //hash map of <chat room name, socket> pairs

    static class connectionPackage
    {
        Socket socketToUse;
        ObjectOutputStream out;
        ObjectInputStream in;
    }

    static void pushConnectionPackage(String roomName, int portToConnect)
    {
        connectionPackage x = new connectionPackage();
        try
        {
            x.socketToUse = new Socket("localhost", portToConnect);
            x.out = new ObjectOutputStream(x.socketToUse.getOutputStream());
            x.in = new ObjectInputStream(x.socketToUse.getInputStream());
            Global.socketMap.put(roomName, x);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    //a container for retrieving the name of the just-requested room

    static Stack<String> roomNames = new Stack<>();
}
