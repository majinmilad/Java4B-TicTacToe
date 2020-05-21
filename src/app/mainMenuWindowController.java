package app;

import Messages.*;
import TicTacToe.gameWindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import modules.Game;

import java.io.IOException;
import java.util.Optional;

public class mainMenuWindowController {

    @FXML
    private Button pvpButton;

    @FXML
    private Button pvcButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button watchGamesButton;

    @FXML
    private Button accountSettingsButton;

    @FXML
    void pvpButtonClicked(ActionEvent event) throws IOException
    {
        //go to player v. player lobby window
        Parent playerLobbyWindow = FXMLLoader.load(getClass().getResource("lobbyPvPWindow.fxml"));
        Scene playerLobbyScene = new Scene(playerLobbyWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(playerLobbyScene);
        window.show();

        //shutdown Listener thread in pvpLobby upon exiting the window
        window.setOnCloseRequest(anonymF ->
        {
            try {
                //stop the controller's listener
                Global.toServer.writeObject(new KillListenerMsg("for the PvP controller exit"));
                Global.toServer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void pvcButtonClicked(ActionEvent event) throws IOException
    {
        //send game creation request to server
        NewGameMsg newGameMsg = new NewGameMsg(Global.CurrentAccount.getCurrentUser(), "1");
        Global.toServer.writeObject(newGameMsg);
        Global.toServer.flush();

        //open game
        try {
            //receive game confirmation from server
            Object serverMsg = Global.fromServer.readObject();

            if(serverMsg instanceof GameCreatedMsg)
            {
                GameCreatedMsg msg = (GameCreatedMsg) serverMsg;

                FXMLLoader loader = new FXMLLoader(gameWindowController.class.getResource("gameWindow.fxml"));
                Parent boardParent = loader.load();

                //set values
                gameWindowController setController = loader.getController();
                setController.initializeName(Global.CurrentAccount.getCurrentUser().getUsername(), "Computer");
                setController.setThisGameID(msg.getGame().getGameId());
                setController.setItsYourTurn(true); //always human player's turn first

                Scene boardScene = new Scene(boardParent, 800, 600);
                boardScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());

                Stage boardWindow = (Stage) pvcButton.getScene().getWindow();
                boardWindow.setScene(boardScene);
                boardWindow.show();

                //shutdown Listener thread in game controller upon exiting the window
                boardWindow.setOnCloseRequest(anonymF ->
                {
                    try {
                        //stop the controller's listener
                        Global.toServer.writeObject(new KillListenerMsg("for the game window controller exit"));
                        Global.toServer.flush();

                        //notify server user has left the game
                        Global.toServer.writeObject(new UserLeftGameMsg(Global.CurrentAccount.getCurrentUser(),
                                msg.getGame().getGameId()));
                        Global.toServer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            else if(serverMsg instanceof UserHasGameOpenMsg)
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "You have a game in progress. Please finish before playing another one.");
                alert.setTitle("Game in Progress");
                alert.setHeaderText("User: " + Global.CurrentAccount.getCurrentUser().getUsername());
                Optional<ButtonType> buttonResult = alert.showAndWait();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void watchGamesButtonClicked(ActionEvent event) throws IOException
    {
        Parent mainWindow = FXMLLoader.load(getClass().getResource("lobbyViewGameWindow.fxml"));
        Scene mainScene = new Scene(mainWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();

        //shutdown Listener thread in watchGameLobby upon exiting the window
        window.setOnCloseRequest(anonymF ->
        {
            try {
                //stop the controller's listener
                Global.toServer.writeObject(new KillListenerMsg("for the View Game Lobby controller exit"));
                Global.toServer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void accountSettingsButtonClicked(ActionEvent event) throws IOException
    {
        Parent menuWindow = FXMLLoader.load(getClass().getResource("accountSettingsWindow.fxml"));
        Scene menuScene = new Scene(menuWindow);
        Stage menuWindowStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        menuWindowStage.setScene(menuScene);
        menuWindowStage.show();
    }

    @FXML
    void logoutButtonClicked(ActionEvent event) throws IOException
    {
        LogoutMsg logoutMsg = new LogoutMsg(Global.CurrentAccount.getCurrentUser());
        Global.toServer.writeObject(logoutMsg);
        Global.toServer.flush();

        //return to main window
        Parent mainWindow = FXMLLoader.load(getClass().getResource("primaryWindow.fxml"));
        Scene mainScene = new Scene(mainWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }

    @FXML
    void gameHistoryButtonClicked(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameHistoryWindow.fxml"));
        Parent gHistoryParent = loader.load();
        Scene gHistoryScene = new Scene(gHistoryParent);
        Stage gHistoryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        gHistoryStage.setScene(gHistoryScene);
        gHistoryStage.show();
    }

}
