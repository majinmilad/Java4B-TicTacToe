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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import modules.BaseModel;
import modules.Game;
import modules.GameInfo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class lobbyPvPController implements Initializable {

    @FXML
    private ListView<String> gameList;

    @FXML
    private Button joinGameButton;

    @FXML
    private Button createGameButton;

    @FXML
    private Button playLocallyButton;

    @FXML
    private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        //populate lobby gameList with waiting games
        RequestForGamesMsg requestGamesMsg = new RequestForGamesMsg("WAITING");

        try {
            Global.toServer.writeObject(requestGamesMsg);
            Global.toServer.flush();

            GameListMsg gameListMsg = (GameListMsg) Global.fromServer.readObject();

            for (GameInfo gameInfo : gameListMsg.getGameList())
            {
                gameList.getItems().add("Host: " + gameInfo.getPlayer1Username() + " (waiting for opponent)...");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

//        new Thread(new ListeningClass()).start();
    }

    @FXML
    void joinGameButtonClicked(ActionEvent event) {

    }

    @FXML
    void createGameButtonClicked(ActionEvent event) throws IOException, ClassNotFoundException
    {
        NewGameMsg newGameMsg = new NewGameMsg(Global.CurrentAccount.getCurrentUser(), null);

        //send game request to server
        Global.toServer.writeObject(newGameMsg);
        Global.toServer.flush();

        //receive response from server
        Object response = Global.fromServer.readObject();

        if(response instanceof GameCreatedMsg)
        {
            //add game to lobby
            GameCreatedMsg gameCreatedMsg = (GameCreatedMsg) response;
            gameList.getItems().add("Host: " + gameCreatedMsg.getCreatorUsername() + " (waiting for opponent)...");
        }
        else if(response instanceof UserHasGameOpenMsg)
        {
            //display message
        }
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
        Scene gameScene = new Scene(gameParent, 950, 775);
        gameScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());
        Stage gameStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        gameStage.setScene(gameScene);
        gameStage.show();
    }

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException
    {
        Global.toServer.writeObject(new UserLeftLobbyMsg(Global.CurrentAccount.getCurrentUser()));
        Global.toServer.flush();

        Parent mainMenuWindow = FXMLLoader.load(getClass().getResource("mainMenuWindow.fxml"));
        Scene mainMenuScene = new Scene(mainMenuWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainMenuScene);
        window.show();
    }

    // LISTENING THREAD
    class ListeningClass implements Runnable
    {
        @Override
        public void run() //this thread's run()
        {
            while(true)
            {
                try
                {
                    //receive msg from server
                    Object serverMsg = Global.fromServer.readObject();

                    //perform action on FX application thread
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(serverMsg instanceof GameCreatedMsg)
                            {
                                //add game to lobby
                                GameCreatedMsg gameCreatedMsg = (GameCreatedMsg) serverMsg;
                                gameList.getItems().add("Host: " + gameCreatedMsg.getCreatorUsername() + " (waiting for opponent)...");
                            }
                            else if(serverMsg instanceof UserHasGameOpenMsg)
                            {
                                //display message
                            }
                        }
                    });
                }
                catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}