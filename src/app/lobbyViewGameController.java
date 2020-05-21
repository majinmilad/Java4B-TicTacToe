package app;

import Messages.*;
import TicTacToe.viewingGameWindowController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import modules.GameInfo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class lobbyViewGameController implements Initializable {

    @FXML
    private Button backButton;

    @FXML
    private ListView<GameInList> gameList;

    @FXML
    private Button viewButton;

    @FXML
    private Button refreshButton;

    ListeningClass listener;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        //populate lobby gameList with waiting games
        RequestForGamesMsg requestGamesMsg = new RequestForGamesMsg("\'RUNNING\'");

        try {
            Global.toServer.writeObject(requestGamesMsg);
            Global.toServer.flush();

            GameListMsg gameListMsg = (GameListMsg) Global.fromServer.readObject();

            if(gameListMsg != null)
            {
                for (GameInfo gameInfo : gameListMsg.getGameList())
                {
                    GameInList g = new GameInList();
                    g.setGameInfo(gameInfo);
                    gameList.getItems().add(g);
                }
            }

            listener = new ListeningClass();
            listener.start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    class GameInList
    {
        //class used to store games in ListView while printing a specific string via toString()
        private GameInfo gameInfo;
        @Override
        public String toString()
        {
            String secondPlayerName;
            if(gameInfo.getGame().getP2Id().equals("1"))
                secondPlayerName = "The Computer";
            else
                secondPlayerName = gameInfo.getPlayer2Username();

            return gameInfo.getPlayer1Username() + " VS " + secondPlayerName;
        }
        GameInfo getGameInfo() {
            return gameInfo;
        }
        void setGameInfo(GameInfo gameInfo) {
            this.gameInfo = gameInfo;
        }
    }


    /*******************************************************************************************************/


    // LISTENING THREAD
    class ListeningClass implements Runnable
    {
        Thread thread;
        boolean keepRunning;

        public void start()
        {
            keepRunning = true;
            thread = new Thread(this);
            thread.start();
        }

        public void setStopSignal()
        {
            keepRunning = false;
        }

        @Override
        public void run() //this thread's run()
        {
            while(keepRunning)
            {
                try
                {
                    //receive msg from server
                    Object serverMsg = Global.fromServer.readObject();

                    if(serverMsg instanceof KillListenerMsg)
                    {
                        setStopSignal();
                    }
                    else
                    {
                        //perform certain actions on FX application thread

                        if(serverMsg instanceof BeginViewingGameMsg)
                        {
                            BeginViewingGameMsg beginViewingMsg = (BeginViewingGameMsg) serverMsg;

                            if(beginViewingMsg.getPermissionToView())
                            {
                                setStopSignal();

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        //open game
                                        try {
                                            FXMLLoader loader = new FXMLLoader(viewingGameWindowController.class.getResource("viewingGameWindow.fxml"));
                                            Parent boardParent = loader.load();

                                            //set values
                                            viewingGameWindowController setController = loader.getController();
                                            setController.initializeName(beginViewingMsg.getGameInfo().getPlayer1Username(), beginViewingMsg.getGameInfo().getPlayer2Username());
                                            setController.setThisGameID(beginViewingMsg.getGameInfo().getGame().getGameId());

//                                            if(msg.getCreatedGamePlayer().getUserID().equals(Global.CurrentAccount.getCurrentUser().getUserID()))
//                                                setController.setItsYourTurn(true);
//                                            else
//                                                setController.setItsYourTurn(false);

                                            Scene boardScene = new Scene(boardParent, 800, 600);
                                            boardScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());

                                            Stage boardWindow = (Stage) backButton.getScene().getWindow();
                                            boardWindow.setScene(boardScene);
                                            boardWindow.show();

                                            //shutdown Listener thread in game controller upon exiting the window
                                            //and notify server that user has stopped viewing
                                            boardWindow.setOnCloseRequest(anonymF ->
                                            {
                                                try {
                                                    //stop the controller's listener
                                                    Global.toServer.writeObject(new KillListenerMsg("for the view game window controller exit"));
                                                    Global.toServer.flush();

                                                    //notify server user has left the game   //MAKE THIS STOPPEDVIEWINGMSG
//                                                    Global.toServer.writeObject(new UserLeftGameMsg(Global.CurrentAccount.getCurrentUser(),
//                                                            msg.getGameInfo().getGame().getGameId()));
//                                                    Global.toServer.flush();
                                                } catch (IOException e) { e.printStackTrace(); }
                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });



                                System.out.println("permission!");
                            }

                        }
                        else if(serverMsg instanceof GameListMsg) //received because of refresh button action
                        {
                            GameListMsg gameListMsg = (GameListMsg) serverMsg;

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    //repopulate list of waiting games
                                    gameList.getItems().clear();

                                    for (GameInfo gameInfo : gameListMsg.getGameList())
                                    {
                                        GameInList g = new GameInList();
                                        g.setGameInfo(gameInfo);
                                        gameList.getItems().add(g);
                                    }
                                }
                            });
                        }

//
//                        else if(serverMsg instanceof UserHasGameOpenMsg)
//                        {
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    //display a message
//                                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "You have a game in progress. Please finish before playing another one.");
//                                    alert.setTitle("Game in Progress");
//                                    alert.setHeaderText("User: " + Global.CurrentAccount.getCurrentUser().getUsername());
//                                    Optional<ButtonType> buttonResult = alert.showAndWait();
//                                }
//                            });
//                        }
//                        else if(serverMsg instanceof GameStartingMsg)
//                        {
//                            setStopSignal();
//
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    //open game
//                                    try {
//                                        GameStartingMsg msg = (GameStartingMsg) serverMsg;
//
//                                        FXMLLoader loader = new FXMLLoader(gameWindowController.class.getResource("gameWindow.fxml"));
//                                        Parent boardParent = loader.load();
//
//                                        //set values
//                                        gameWindowController setController = loader.getController();
//                                        setController.initializeName(msg.getCreatedGamePlayer().getUsername(), msg.getJoiningGamePlayer().getUsername());
//                                        setController.setThisGameID(msg.getGameInfo().getGame().getGameId());
//                                        if(msg.getCreatedGamePlayer().getUserID().equals(Global.CurrentAccount.getCurrentUser().getUserID()))
//                                            setController.setItsYourTurn(true);
//                                        else
//                                            setController.setItsYourTurn(false);
//
//                                        Scene boardScene = new Scene(boardParent, 800, 600);
//                                        boardScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());
//
//                                        Stage boardWindow = (Stage) backButton.getScene().getWindow();
//                                        boardWindow.setScene(boardScene);
//                                        boardWindow.show();
//
//                                        //shutdown Listener thread in game controller upon exiting the window
//                                        //and notify server that user has left the game
//                                        boardWindow.setOnCloseRequest(anonymF ->
//                                        {
//                                            try {
//                                                //stop the controller's listener
//                                                Global.toServer.writeObject(new KillListenerMsg("for the game window controller exit"));
//                                                Global.toServer.flush();
//
//                                                //notify server user has left the game
//                                                Global.toServer.writeObject(new UserLeftGameMsg(Global.CurrentAccount.getCurrentUser(),
//                                                        msg.getGameInfo().getGame().getGameId()));
//                                                Global.toServer.flush();
//                                            } catch (IOException e) { e.printStackTrace(); }
//                                        });
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//                        }

                        System.out.println("message processed in listener");
                    }
                }
                catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("thread is stopped.");
        }
    }


    /******************************************************************************************************/


    @FXML
    void viewButtonClicked(ActionEvent event) throws IOException {
        if(gameList.getSelectionModel().getSelectedItem() != null)
        {
            GameInfo gameSelected = gameList.getSelectionModel().getSelectedItem().getGameInfo();

            //send join a game request to server
            Global.toServer.writeObject(new ViewGameRequestMsg(gameSelected, Global.CurrentAccount.getCurrentUser()));
            Global.toServer.flush();
        }
    }

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException {
        //shutdown listener
        Global.toServer.writeObject(new KillListenerMsg("from lobbyPvPController"));

        Parent mainWindow = FXMLLoader.load(getClass().getResource("mainMenuWindow.fxml"));
        Scene mainScene = new Scene(mainWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }

    @FXML
    void refreshButtonClicked(ActionEvent event) {
        //request for gameList with running games
        RequestForGamesMsg requestGamesMsg = new RequestForGamesMsg("\'RUNNING\'");

        try {
            Global.toServer.writeObject(requestGamesMsg);
            Global.toServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
