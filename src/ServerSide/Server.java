package ServerSide;

import Messages.*;
import modules.Game;
import modules.GameViewers;
import modules.User;
import sqlite.DatabaseManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Observable;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

public class Server extends Observable implements Runnable
{

    // DATA MEMBERS

    //lazy instantiation of singleton class
    private static Server singletonRef = new Server();

    //the port this ServerManager is on
    private final int serverPort = 8888;

    //an ArrayList of the different ClientManagers connected to this server
    private HashMap<UUID, ClientConnection> clientMap = new HashMap<>();

    //a blocking queue for messages to be processed
    private ArrayBlockingQueue<Message> msgQueue = new ArrayBlockingQueue<>(1000);


    // METHODS

    private Server()
    {
        System.out.println("Server turned on\n");

        new Thread(this).start();
    }

    static Server getInstance()
    {
        return singletonRef;
    }

    @Override
    public void run()
    {
        try
        {
            //start the Publisher
            new Thread(new Publisher()).start();

            //establish server on a port
            ServerSocket ss = new ServerSocket(serverPort);

            //ServerManager open for connections to ClientManagers
            while(true)
            {
                Socket socket = ss.accept(); //connections to cMs

                if(socket.isConnected())
                {
                    System.out.println("Server connected to a client");
                    System.out.println(socket.getInetAddress().getHostAddress() + " (port " + socket.getPort() + ")\n");

                    //create connection id and store connection
                    UUID connectID = UUID.randomUUID();
                    clientMap.put(connectID, new ClientConnection(socket, connectID));
                }
                else
                    System.out.println("Server didn't connect !!!\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ClientConnection implements Runnable
    {
        //DATA MEMBERS
        private Socket socket;
        private UUID connectionID; //used to identify this ClientConnection
        private String clientsUserName;
        private Thread clientThread;

        //this connection's socket streams
        private ObjectInputStream objectInputFromClient;
        private ObjectOutputStream objectOutputToClient;

        //METHODS
        ClientConnection(Socket socket, UUID id)
        {
            this.socket = socket;
            connectionID = id;

            wrapSocketStreams();

            //start this object's thread
            clientThread = new Thread(this);
            clientThread.start();
        }

        @Override
        public void run()
        {
            try
            {
                //receive incoming messages
                while(true)
                {
                    Message incomingMsg = (Message) objectInputFromClient.readObject();
                    incomingMsg.setConnectionID(connectionID);
                    msgQueue.add(incomingMsg);
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
                if(e instanceof IOException) //client program disconnected
                {
                    //logoff the client
                    User searchUser = new User(clientsUserName);
                    User returnedUser = (User) DatabaseManager.getInstance().get(searchUser);
                    if(returnedUser.getStatus().equals("ONLINE"))
                    {
                        returnedUser.setStatus("OFFLINE");
                        DatabaseManager.getInstance().update(returnedUser);
                        //send to server GUI
                        sendToServerGUI(new LogoutMsg(returnedUser));
                    }

                    //remove their connection from map
                    clientMap.remove(connectionID);
                }
                System.out.println("exception caught in one of server's ClientConnection object's run()");
                e.printStackTrace();
            }
        }

        void sendMessageFromPublisher(Message msg)
        {
            try
            {
                objectOutputToClient.writeObject(msg);
            }
            catch (IOException e) {
                //System.out.println("exception caught in sendMessageFromPub() of " + memberName + "'s MemberConnection object");
                e.printStackTrace();
            }
        }

        private void wrapSocketStreams()
        {
            try {
                objectOutputToClient = new ObjectOutputStream(socket.getOutputStream());
                objectInputFromClient = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.out.println("exception caught in wrapSocketStream()\n\n");
                e.printStackTrace();
            }
        }
    }


    /*********************************************************************************/


    private class Publisher implements Runnable
    {
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    //pull message off queue
                    Message nextMsg = msgQueue.take();

                    ClientConnection client = clientMap.get(nextMsg.getConnectionID());

                    //process the message
                    if(nextMsg instanceof RegistrationMsg)
                    {
                        RegistrationMsg regMsg = (RegistrationMsg) nextMsg;

                        User returnedUser = new User(regMsg.getUser().getUsername());
                        returnedUser = (User) DatabaseManager.getInstance().get(returnedUser);

                        if(returnedUser == null) //account does not exist
                        {
                            client.objectOutputToClient.writeBoolean(false); //notify account doesnt exist

                            Object successfulInsert = DatabaseManager.getInstance().insert(regMsg.getUser());

                            if(successfulInsert != null) {
                                client.objectOutputToClient.writeBoolean(true);
                                client.clientsUserName = regMsg.getUser().getUsername(); //update ClientConnection username attribute
                                sendToServerGUI(regMsg); //send to server GUI
                            }
                            else
                                client.objectOutputToClient.writeBoolean(false);

                            client.objectOutputToClient.flush();
                        }
                        else //account already exists
                        {
                            client.objectOutputToClient.writeBoolean(true); //notify account exists

                            //notify of INACTIVE status
                            if(returnedUser.getStatus().equals("INACTIVE"))
                                client.objectOutputToClient.writeBoolean(true);
                            else
                                client.objectOutputToClient.writeBoolean(false);

                            client.objectOutputToClient.flush();
                        }
                    }
                    else if(nextMsg instanceof LoginMsg)
                    {
                        LoginMsg loginMsg = (LoginMsg) nextMsg;

                        User returnedUser = (User) DatabaseManager.getInstance().authenticate(loginMsg.getUsername(), loginMsg.getPassword());

                        if(returnedUser != null)
                        {
                            //notify authentication status
                            client.objectOutputToClient.writeBoolean(true);

                            if(returnedUser.getStatus().equals("ONLINE"))
                            {
                                //notify that user's already online
                                client.objectOutputToClient.writeBoolean(true);
                            }
                            else
                            {
                                //notify user is not already online
                                client.objectOutputToClient.writeBoolean(false);

                                //set user to ONLINE status and send the user info
                                User user = new User(loginMsg.getUsername());
                                user = (User) DatabaseManager.getInstance().get(user);
                                user.setStatus("ONLINE");
                                DatabaseManager.getInstance().update(user);
                                client.objectOutputToClient.writeObject(user);

                                //update ClientConnection username attribute
                                client.clientsUserName = user.getUsername();

                                //send message to server GUI
                                sendToServerGUI(loginMsg);
                            }
                        }
                        else
                            client.objectOutputToClient.writeBoolean(false); //not authenticated

                        client.objectOutputToClient.flush();
                    }
                    else if(nextMsg instanceof DeleteUserMsg)
                    {
                        DeleteUserMsg deleteMsg = (DeleteUserMsg) nextMsg;

                        Object returnStatus = DatabaseManager.getInstance().delete(deleteMsg.getUser());

                        if(returnStatus != null)
                            sendToServerGUI(deleteMsg);
                    }
                    else if(nextMsg instanceof LogoutMsg)
                    {
                        LogoutMsg logoutMsg = (LogoutMsg) nextMsg;
                        logoutMsg.getUser().setStatus("OFFLINE");
                        Object returnStatus = DatabaseManager.getInstance().update(logoutMsg.getUser());

                        if(returnStatus != null)
                            sendToServerGUI(logoutMsg);
                    }
                    else if(nextMsg instanceof ReactivateUserMsg)
                    {
                        ReactivateUserMsg reactivateMsg = (ReactivateUserMsg) nextMsg;
                        User returnedUser = new User(reactivateMsg.getUser().getUsername());
                        returnedUser = (User) DatabaseManager.getInstance().get(returnedUser);

                        if(returnedUser != null) //account does exists
                        {
                            if(returnedUser.getStatus().equals("INACTIVE")) //account is inactive
                            {
                                //update and reactivate account
                                returnedUser.setStatus("OFFLINE");
                                returnedUser.setFirstName(reactivateMsg.getUser().getFirstName());
                                returnedUser.setLastName(reactivateMsg.getUser().getLastName());
                                returnedUser.setPassword(reactivateMsg.getUser().getPassword());
                                DatabaseManager.getInstance().update(returnedUser);

                                //update ClientConnection username attribute
                                client.clientsUserName = returnedUser.getUsername();

                                //send to server GUI
                                sendToServerGUI(reactivateMsg);
                            }
                        }
                        else //account didn't exist
                        {
                            //not sure what to do here... probably no no need for any action
                        }
                    }
                    else if(nextMsg instanceof UpdateUserMsg)
                    {
                        UpdateUserMsg updateUserMsg = (UpdateUserMsg) nextMsg;
                        User returnedUser = (User) DatabaseManager.getInstance().update(updateUserMsg.getUser());
                        if(returnedUser != null)
                        {
                            client.objectOutputToClient.writeBoolean(true);
                            client.clientsUserName = returnedUser.getUsername(); //update ClientConnection username attribute
                            sendToServerGUI(updateUserMsg);
                        }
                        else
                            client.objectOutputToClient.writeBoolean(false);
                        client.objectOutputToClient.flush();
                    }
                    else if(nextMsg instanceof NewGameMsg)
                    {
                        NewGameMsg newGameMsg = (NewGameMsg) nextMsg;
                        Game newGame;
                        if(newGameMsg.getGameType().equals("COMPUTER"))
                        {
                            newGame = new Game(newGameMsg.getCreator(), "computer");
                            System.out.println("Created new Game w/ Computer");
                        }
                        else if (newGameMsg.getGameType().equals("LOCAL"))
                        {
                            newGame = new Game(newGameMsg.getCreator(), "local player");
                            System.out.println("Created new Game w/ local");
                        }
                        else
                        {
                            newGame = new Game(newGameMsg.getCreator());
                            System.out.println("Created new Game w/ Online");
                        }

                        //check if player has a game open
                        User player1 = (User) DatabaseManager.getInstance().get(new User(client.clientsUserName));
                        Object playerHasGameRunning = DatabaseManager.getInstance().query(newGame,
                                                            "WHERE p1Id = \"" + player1.getUserID() + "\""
                                                                + "OR p2Id == \"" + player1.getUserID()
                                                                + "\" AND gameStatus = \"RUNNING\" ");
                        //if not make game
                        if(playerHasGameRunning == null)
                        {
                            Object successfulInsert = DatabaseManager.getInstance().insert(newGame);
                            if(successfulInsert != null)
                            {
                                GameCreatedMsg gameCreatedMsg = new GameCreatedMsg(newGame, client.clientsUserName);
                                client.objectOutputToClient.writeObject(gameCreatedMsg);
                            }
                        }
                        else //indicate a game already exists
                            client.objectOutputToClient.writeObject(new UserHasGameOpenMsg());
                            client.objectOutputToClient.flush();
                    }
                    else if(nextMsg instanceof GameEndedMsg)
                    {
                        GameEndedMsg gameEndedMsg = (GameEndedMsg) nextMsg;
                        // Notify DB needs updating
                        DatabaseManager.getInstance().update(gameEndedMsg.getGameOver());

                        // Notify Observers that Game is Over

                    }
                    else if(nextMsg instanceof ViewGameMsg)
                    {
                        ViewGameMsg viewGameMsg = (ViewGameMsg) nextMsg;
                        Game findGame = new Game(viewGameMsg.getNewViewer().getId());
                        Object liveGame = DatabaseManager.getInstance().query(findGame, "WHERE gameId = \'" + findGame.getGameId()
                                                                                  + "\' "
                                                                                  + "AND status != 'ENDED' " );

                        // Game is still running
                        if(liveGame != null)
                        {
                            // Gets the Game Viewer Class w/ gameId + viewerId
                            GameViewers newViewer = viewGameMsg.getNewViewer();
                            // Adds Game Viewer Id into DB
                            DatabaseManager.getInstance().insert(newViewer);

                            // Add Viewer to a subscribed list (make observable)
                        }
                        else
                        {
                            // Notify that game has ended
                        }



                    }
                }
                catch (InterruptedException | IOException e) {
                    System.out.println("exception caught in server Publisher's run()");
                    e.printStackTrace();
                }
            }
        }
    }


    void sendToServerGUI(Object obj)
    {
        setChanged();
        notifyObservers(obj);
    }


    void processMessage(Message msg)
    {
        msgQueue.add(msg);
    }
}
