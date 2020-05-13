package app;

import Messages.*;
import TicTacToe.gameWindowController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import modules.GameInfo;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class lobbyPvPController implements Initializable {

    @FXML
    private ListView<GameInList> gameList;

    @FXML
    private Button joinGameButton;

    @FXML
    private Button createGameButton;

    @FXML
    private Button playLocallyButton;

    @FXML
    private Button backButton;

    ListeningClass listener;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        //populate lobby gameList with waiting games
        RequestForGamesMsg requestGamesMsg = new RequestForGamesMsg("WAITING");

        try {
            Global.toServer.writeObject(requestGamesMsg);
            Global.toServer.flush();

            GameListMsg gameListMsg = (GameListMsg) Global.fromServer.readObject();

            System.out.println("read back from server");
            if(gameListMsg == null)
                System.out.println("is null");
            else
                System.out.println("not null");
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

    @FXML
    void joinGameButtonClicked(ActionEvent event) throws IOException
    {
        GameInfo gameSelected = gameList.getSelectionModel().getSelectedItem().getGameInfo();

        //send join a game request to server
        Global.toServer.writeObject(new JoinGameRequestMsg(gameSelected, Global.CurrentAccount.getCurrentUser()));
        Global.toServer.flush();
    }

    @FXML
    void createGameButtonClicked(ActionEvent event) throws IOException
    {
        NewGameMsg newGameMsg = new NewGameMsg(Global.CurrentAccount.getCurrentUser(), null);

        //send game creation request to server
        Global.toServer.writeObject(newGameMsg);
        Global.toServer.flush();
    }

    @FXML
    void playLocallyButtonClicked(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TicTacToe/gameWindow.fxml"));
        Parent gameParent = loader.load();

        //set players involved in the controller
        gameWindowController setController = loader.getController();
        setController.initializeName("Player 1", "Player 2");

        //show the game window
        Scene gameScene = new Scene(gameParent, 800, 600);
        gameScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());
        Stage gameStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        gameStage.setScene(gameScene);
        gameStage.show();

        listener.setStopSignal();
    }

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException
    {
        //shutdown listener
        Global.toServer.writeObject(new KillListenerMsg("from lobbyPvPController"));

        //notify that user left lobby
        Global.toServer.writeObject(new UserLeftLobbyMsg(Global.CurrentAccount.getCurrentUser()));
        Global.toServer.flush();

        Parent mainMenuWindow = FXMLLoader.load(getClass().getResource("mainMenuWindow.fxml"));
        Scene mainMenuScene = new Scene(mainMenuWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainMenuScene);
        window.show();
    }

    /**************************************************************************************************/

    class GameInList
    {
        //class used to store games in ListView while printing a specific string via toString()
        private GameInfo gameInfo;
        @Override
        public String toString()
        {
            return "Host: " + gameInfo.getPlayer1Username() + " (waiting for opponent)...";
        }
        GameInfo getGameInfo() {
            return gameInfo;
        }
        void setGameInfo(GameInfo gameInfo) {
            this.gameInfo = gameInfo;
        }
    }

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
                        //perform action on FX application thread
                                if(serverMsg instanceof GameCreatedMsg)
                                {
                                    GameCreatedMsg gameCreatedMsg = (GameCreatedMsg) serverMsg;

                                    //add game to lobby
                                    GameInfo gameInfo = new GameInfo();
                                    gameInfo.setGame(gameCreatedMsg.getGame());
                                    gameInfo.setPlayer1Username(Global.CurrentAccount.getCurrentUser().getUsername());

                                    GameInList g = new GameInList();
                                    g.setGameInfo(gameInfo);

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            gameList.getItems().add(g);
                                        }
                                    });
                                }
                                else if(serverMsg instanceof UserHasGameOpenMsg)
                                {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            //display a message
                                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "You have a game in progress. Please finish before playing another one.");
                                            alert.setTitle("Game in Progress");
                                            alert.setHeaderText("User: " + Global.CurrentAccount.getCurrentUser().getUsername());
                                            Optional<ButtonType> buttonResult = alert.showAndWait();
                                        }
                                    });
                                }
                                else if(serverMsg instanceof GameStartingMsg)
                                {
                                    System.out.println("before " + keepRunning);
                                    setStopSignal();
                                    System.out.println(keepRunning);

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            //open game
                                            try {
                                                FXMLLoader loader = new FXMLLoader(gameWindowController.class.getResource("gameWindow.fxml"));
                                                Parent boardParent = loader.load();

                                                gameWindowController setController = loader.getController();
                                                setController.initializeName("Player 1", "Player 2");

                                                Scene boardScene = new Scene(boardParent, 800, 600);
                                                boardScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());

                                                Stage boardWindow = (Stage) backButton.getScene().getWindow();;
                                                boardWindow.setScene(boardScene);
                                                boardWindow.show();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    //System.out.println("game has been opened for " + Global.CurrentAccount.getCurrentUser().getUsername()+"!!!\n");
                                }

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
}