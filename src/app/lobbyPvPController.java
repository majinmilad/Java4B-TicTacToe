package app;

import Messages.GameCreatedMsg;
import Messages.NewGameMsg;
import Messages.UserHasGameOpenMsg;
import TicTacToe.gameWindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import modules.Game;

import java.io.IOException;

public class lobbyPvPController {

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

    @FXML
    void joinGameButtonClicked(ActionEvent event) {

    }

    @FXML
    void createGameButtonClicked(ActionEvent event) throws IOException, ClassNotFoundException
    {
        NewGameMsg newGameMsg = new NewGameMsg("REMOTE", Global.CurrentAccount.getCurrentUser());

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

        //set players involved in the controller
        gameWindowController setController = loader.getController();
        setController.initializeName("Player 1", "Player 2");

        //show the game window
        Parent gameParent = loader.load();
        Scene gameScene = new Scene(gameParent, 950, 775);
        gameScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());
        Stage gameStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        gameStage.setScene(gameScene);
        gameStage.show();
    }

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException {
        Parent mainMenuWindow = FXMLLoader.load(getClass().getResource("mainMenuWindow.fxml"));
        Scene mainMenuScene = new Scene(mainMenuWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainMenuScene);
        window.show();
    }



}