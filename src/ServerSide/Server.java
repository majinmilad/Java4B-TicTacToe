package ServerSide;

import Messages.*;
import modules.*;
import sqlite.DatabaseManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class Server extends Observable implements Runnable
{

    // DATA MEMBERS

    //lazy instantiation of singleton class
    private static Server singletonRef = new Server();

    //the port this ServerManager is on
    private final int serverPort = 7777;

    //a map of the different ClientConnections to this server
    private HashMap<UUID, ClientConnection> clientMapConnectId = new HashMap<>();
    private HashMap<String, ClientConnection> clientMapPlayerId = new HashMap<>(); //used in situations where player id is used to get socket connection

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
                    clientMapConnectId.put(connectID, new ClientConnection(socket, connectID));
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
                    incomingMsg.setConnectionID(connectionID); //to id which socket stream this msg came from
                    msgQueue.add(incomingMsg);
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
                if(e instanceof IOException) //client program disconnected
                {
                    //perform disconnection actions
                    ClientHasDisconnected();
                }
                System.out.println("exception caught in one of server's ClientConnection object's run()");
                e.printStackTrace();
            }
        }

        private void ClientHasDisconnected()
        {
            User searchUser = new User(clientsUserName);
            User thisUser = (User) DatabaseManager.getInstance().get(searchUser);

            //delete if user has WAITING game
            Object game = DatabaseManager.getInstance().query(new Game(), "WHERE p1Id = \'" + thisUser.getUserID() + "\' AND gameStatus = \'WAITING\'");
            if(game != null)
                DatabaseManager.getInstance().delete((Game) game);

            //logoff the client
            if(thisUser.getStatus().equals("ONLINE"))
            {
                thisUser.setStatus("OFFLINE");
                DatabaseManager.getInstance().update(thisUser);
                //send to server GUI
                sendToServerGUI(new LogoutMsg(thisUser));
            }

            //remove their connection from map
            clientMapConnectId.remove(connectionID);
            clientMapPlayerId.remove(thisUser.getUserID());
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

                    ClientConnection client = clientMapConnectId.get(nextMsg.getConnectionID());

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
                                client.clientsUserName = regMsg.getUser().getUsername(); //update ClientConnection username attribute
                                sendToServerGUI(regMsg); //send to server GUI
                                client.objectOutputToClient.writeBoolean(true); //indicate successful registration
                            }
                            else
                                client.objectOutputToClient.writeBoolean(false); //indicate unsuccessful registration

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

                                //update parallel connection map with player id
                                clientMapPlayerId.put(returnedUser.getUserID(), client);

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
                        {
                            clientMapPlayerId.remove(logoutMsg.getUser().getUserID(), client);
                            sendToServerGUI(logoutMsg);
                        }
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
                        Game newGame = new Game(newGameMsg.getCreator(), newGameMsg.getPlayer2Id());

                        //check if player has a game open
                        User player1 = (User) DatabaseManager.getInstance().get(new User(client.clientsUserName));
                        Object playerHasGameAlready = DatabaseManager.getInstance().query(newGame, "WHERE p1Id = \""
                                                                + player1.getUserID() + "\" AND (gameStatus = \"RUNNING\" OR gameStatus = \"WAITING\")");

                        if(playerHasGameAlready == null) //player is involved in no games
                        {
                            if(newGameMsg.getPlayer2Id() == null) // new PvP game
                            {
                                newGame.setStatus("WAITING");
                                //create the requested game
                                Object successfulInsert = DatabaseManager.getInstance().insert(newGame);
                                if(successfulInsert != null)
                                {
                                    GameCreatedMsg gameCreatedMsg = new GameCreatedMsg(newGame, client.clientsUserName);
                                    client.objectOutputToClient.writeObject(gameCreatedMsg);
                                }
                            }
                            else if(newGameMsg.getPlayer2Id().equals("1")) // new PvC game
                            {
                                //set up player vs computer game
                                newGame.setStatus("RUNNING");
                                newGame.setStartTime();
                                Object successfulInsert = DatabaseManager.getInstance().insert(newGame);
                                if(successfulInsert != null)
                                {
                                    newGame.displayAll();
                                    GameCreatedMsg compGame = new GameCreatedMsg(newGame, client.clientsUserName);
                                    client.objectOutputToClient.writeObject(compGame);
                                }
                            }
                        }
                        else //indicate game not made
                            client.objectOutputToClient.writeObject(new UserHasGameOpenMsg());

                        client.objectOutputToClient.flush();
                    }
                    else if(nextMsg instanceof RequestForGamesMsg)
                    {
                        RequestForGamesMsg requestGamesMsg = (RequestForGamesMsg) nextMsg;

                        List<BaseModel> gameList;

                        if(requestGamesMsg.getGameStatusFilter() == null)
                        {
                            gameList = DatabaseManager.getInstance().queryList(new Game(), "");
                        }
                        else
                        {
                            gameList = DatabaseManager.getInstance().queryList(new Game(), "WHERE gameStatus = " + requestGamesMsg.getGameStatusFilter());
                        }

                        //convert list of games into list of gameInfo objects
                        List<GameInfo> gameInfoList = new ArrayList<>();

                        for(BaseModel g : gameList)
                        {
                            Game game = (Game) g;
                            GameInfo gameInfo = new GameInfo();
                            gameInfo.setGame(game);

                            //set usernames involved in game
                            String userName = ((User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + game.getP1Id() +"\'")).getUsername();
                            gameInfo.setPlayer1Username(userName);
                            User player2 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + game.getP2Id() +"\'");
                            if(player2 != null)
                                gameInfo.setPlayer2Username(player2.getUsername());

                            gameInfoList.add(gameInfo);

                            System.out.println("game collected");
                        }

                        //send gameListMsg back to client
                        GameListMsg gameListMsg = new GameListMsg(gameInfoList);
                        client.objectOutputToClient.writeObject(gameListMsg);
                        client.objectOutputToClient.flush();
                        System.out.println("game list flushed");
                    }
                    else if(nextMsg instanceof UserLeftLobbyMsg)
                    {
                        User userThatLeft = ((UserLeftLobbyMsg) nextMsg).getUser();
                        Object game = DatabaseManager.getInstance().query(new Game(), "WHERE p1Id = \'" + userThatLeft.getUserID() + "\' AND gameStatus = \'WAITING\'");
                        if(game != null)
                            DatabaseManager.getInstance().delete((Game) game);
                    }
                    else if(nextMsg instanceof JoinGameRequestMsg)
                    {
                        JoinGameRequestMsg joinGameMsg = (JoinGameRequestMsg) nextMsg;

                        //check if game still in WAITING mode
                        Game game = (Game) DatabaseManager.getInstance().query(new Game(),
                                "WHERE p1Id = \'" + joinGameMsg.getGameInfo().getGame().getP1Id()
                                        + "\' AND UUID = \'" + joinGameMsg.getGameInfo().getGame().getGameId()
                                        + "\' AND gameStatus = \'WAITING\'");

                        if(game != null)
                        {
                            User gameCreator = (User) DatabaseManager.getInstance().get(new User(joinGameMsg.getGameInfo().getPlayer1Username()));

                            if(!joinGameMsg.getRequestingUser().getUserID().equals(gameCreator.getUserID()))
                            {
                                //start the game
                                GameStartingMsg gameStartingMsg = new GameStartingMsg(joinGameMsg.getGameInfo(), gameCreator, joinGameMsg.getRequestingUser());

                                //send to game creator
                                clientMapPlayerId.get(gameCreator.getUserID()).objectOutputToClient.writeObject(gameStartingMsg);
                                clientMapPlayerId.get(gameCreator.getUserID()).objectOutputToClient.flush();

                                //send to second player
                                client.objectOutputToClient.writeObject(gameStartingMsg);
                                client.objectOutputToClient.flush();

                                //delete if joining player had a WAITING game
                                Object waitingGame = DatabaseManager.getInstance().query(new Game(), "WHERE p1Id = \'" + gameStartingMsg.getJoiningGamePlayer().getUserID() + "\' AND gameStatus = \'WAITING\'");
                                if(waitingGame != null)
                                    DatabaseManager.getInstance().delete((Game) waitingGame);

                                //set game's player2Id, startTime, and its status to RUNNING
                                game.setP2Id(joinGameMsg.getRequestingUser().getUserID());
                                game.setStartTime();
                                game.setStatus("RUNNING");
                                game.displayAll();
                                DatabaseManager.getInstance().update(game);
                            }
                        }
                    }
                    else if(nextMsg instanceof KillListenerMsg)
                    {
                        client.objectOutputToClient.writeObject(nextMsg);
                        client.objectOutputToClient.flush();
                    }
                    else if(nextMsg instanceof MoveMadeMsg)
                    {
                        MoveMadeMsg moveMsg = (MoveMadeMsg) nextMsg;

                        //write to db
                        Object successfulMoveInsert = DatabaseManager.getInstance().insert(moveMsg.getMove());

                        //propagate move
                        if(successfulMoveInsert != null)
                        {
                            Game game = (Game) DatabaseManager.getInstance().query(new Game(), "WHERE UUID = \'" + moveMsg.getMove().getGameId() + "\' AND gameStatus = \'RUNNING\'");

                            //send move to other player
                            if(!game.getP2Id().equals("1")) // not a PvC game
                            {
                                if (moveMsg.getMoveMadeByUser().getUserID().equals(game.getP1Id())) {
                                    clientMapPlayerId.get(game.getP2Id()).objectOutputToClient.writeObject(moveMsg);
                                    clientMapPlayerId.get(game.getP2Id()).objectOutputToClient.flush();
                                } else {
                                    clientMapPlayerId.get(game.getP1Id()).objectOutputToClient.writeObject(moveMsg);
                                    clientMapPlayerId.get(game.getP1Id()).objectOutputToClient.flush();
                                }
                            }

//                            //send to viewers
//                            List<BaseModel> viewerList;
//
//                            viewerList = DatabaseManager.getInstance().queryList(new GameViewers(game.getGameId()), " AND viewingStatus = \'WATCHING\'");
//
//                            if(viewerList != null)
//                            {
//                                System.out.println(viewerList.size());
//                            }
//                            for(BaseModel g : viewerList)
//                            {
//                                GameViewers gameViewer = (GameViewers) g;
//
//                                clientMapPlayerId.get(gameViewer.getPlayerId()).objectOutputToClient.writeObject(moveMsg);
//                                clientMapPlayerId.get(gameViewer.getPlayerId()).objectOutputToClient.flush();
//                            }
                        }
                    }
                    else if(nextMsg instanceof GameWonMsg)
                    {
                        GameWonMsg gameWonMsg = (GameWonMsg) nextMsg;
                        Game game = (Game) DatabaseManager.getInstance().query(new Game(), "WHERE UUID = \'" + gameWonMsg.getGameId() + "\' AND gameStatus = \'RUNNING\'");

                        //update game record
                        game.setWinnerId(gameWonMsg.getGameWinner().getUserID());
                        game.setEndTime();
                        game.setStatus("ENDED");
                        DatabaseManager.getInstance().update(game);

                        //send win msg to other player
                        if(!game.getP2Id().equals("1")) // not a PvC game
                        {
                            if (gameWonMsg.getGameWinner().getUserID().equals(game.getP1Id())) {
                                clientMapPlayerId.get(game.getP2Id()).objectOutputToClient.writeObject(gameWonMsg);
                                clientMapPlayerId.get(game.getP2Id()).objectOutputToClient.flush();
                            } else {
                                clientMapPlayerId.get(game.getP1Id()).objectOutputToClient.writeObject(gameWonMsg);
                                clientMapPlayerId.get(game.getP1Id()).objectOutputToClient.flush();
                            }
                        }

                        //send to viewers
                    }
                    else if(nextMsg instanceof GameTiedMsg)
                    {
                        GameTiedMsg gameTiedMsg = (GameTiedMsg) nextMsg;
                        Game game = (Game) DatabaseManager.getInstance().query(new Game(), "WHERE UUID = \'" + gameTiedMsg.getGameId() + "\' AND gameStatus = \'RUNNING\'");

                        //update game record
                        game.setWinnerId("0");
                        game.setEndTime();
                        game.setStatus("ENDED");
                        DatabaseManager.getInstance().update(game);

                        //send tie msg to other player
                        if(!game.getP2Id().equals("1")) // not a PvC game
                        {
                            if (gameTiedMsg.getMsgSender().getUserID().equals(game.getP1Id())) {
                                clientMapPlayerId.get(game.getP2Id()).objectOutputToClient.writeObject(gameTiedMsg);
                                clientMapPlayerId.get(game.getP2Id()).objectOutputToClient.flush();
                            } else {
                                clientMapPlayerId.get(game.getP1Id()).objectOutputToClient.writeObject(gameTiedMsg);
                                clientMapPlayerId.get(game.getP1Id()).objectOutputToClient.flush();
                            }
                        }

                        //send to viewers
                    }
                    else if(nextMsg instanceof UserLeftGameMsg)
                    {
                        UserLeftGameMsg userLeftGameMsg = (UserLeftGameMsg) nextMsg;

                        Game game = (Game) DatabaseManager.getInstance().query(new Game(), "WHERE UUID = \'" + userLeftGameMsg.getGameId() + "\' AND gameStatus = \'RUNNING\'");

                        if(game != null)
                        {
                            if(!game.getP2Id().equals("1")) //PvP game
                            {
                                String leavingPlayerId = userLeftGameMsg.getUser().getUserID();

                                //award leaving player a loss
                                //and notify other player of their win
                                if (leavingPlayerId.equals(game.getP1Id())) {
                                    game.setWinnerId(game.getP2Id());
                                    clientMapPlayerId.get(game.getP2Id()).objectOutputToClient.writeObject(new OpponentLeftGameMsg());
                                    clientMapPlayerId.get(game.getP2Id()).objectOutputToClient.flush();

                                } else {
                                    game.setWinnerId(game.getP1Id());
                                    clientMapPlayerId.get(game.getP1Id()).objectOutputToClient.writeObject(new OpponentLeftGameMsg());
                                    clientMapPlayerId.get(game.getP1Id()).objectOutputToClient.flush();
                                }
                            }
                            else
                                game.setWinnerId("1");

                            game.setEndTime();
                            game.setStatus("ENDED");
                            DatabaseManager.getInstance().update(game);
                        }
                    }
                    else if(nextMsg instanceof ViewGameRequestMsg)
                    {

                        ViewGameRequestMsg viewGameMsg = (ViewGameRequestMsg) nextMsg;

                        //check if game still in RUNNING mode
                        Game game = (Game) DatabaseManager.getInstance().query(new Game(),
                                "WHERE UUID = \'" + viewGameMsg.getGameInfo().getGame().getGameId()
                                        + "\' AND gameStatus = \'RUNNING\'");

//                        //check if already a viewer of game
//                        Object alreadyViewer = DatabaseManager.getInstance().query(new GameViewers()) TRYING TO NOT LET VIEWER BE LISTED TWICE

                        if(game != null)
                        {
                            //add user as a viewer
                            GameViewers viewer = new GameViewers(game.getGameId(), viewGameMsg.getRequestingUser().getUserID(), "WATCHING");
                            DatabaseManager.getInstance().insert(viewer);

                            //send begin viewing message
                            BeginViewingGameMsg beginViewingMsg = new BeginViewingGameMsg(true, viewGameMsg.getGameInfo());
                            client.objectOutputToClient.writeObject(beginViewingMsg);
                            client.objectOutputToClient.flush();
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


    private void sendToServerGUI(Object obj)
    {
        setChanged();
        notifyObservers(obj);
    }


    void processMessage(Message msg)
    {
        msgQueue.add(msg);
    }
}
