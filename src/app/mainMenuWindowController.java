package app;

import Messages.GameCreatedMsg;
import Messages.LogoutMsg;
import Messages.NewGameMsg;
import Messages.UserHasGameOpenMsg;
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
    }

    @FXML
    void pvcButtonClicked(ActionEvent event) throws IOException, ClassNotFoundException {
        // Create a game request for computer
        NewGameMsg newGameMsg = new NewGameMsg("COMPUTER", Global.CurrentAccount.getCurrentUser());
        System.out.println("newGameMsg: " + newGameMsg.getGameType());

        //send game request to server
        Global.toServer.writeObject(newGameMsg);
        Global.toServer.flush();

        //receive response from server
        Object response = Global.fromServer.readObject();

        if(response instanceof GameCreatedMsg)
        {
            FXMLLoader loader = new FXMLLoader(gameWindowController.class.getResource("gameWindow.fxml"));
            Parent boardParent = loader.load();

            gameWindowController setController = loader.getController();
            setController.initializeName("Player 1", "Computer");

            Scene boardScene = new Scene(boardParent, 950, 775);
            boardScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());

            Stage boardWindow = new Stage();
            boardWindow.setScene(boardScene);
            boardWindow.show();
        }
        else if(response instanceof UserHasGameOpenMsg)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "You have a game in progress. Please finish before playing another one.");
            alert.setTitle("Game in Progress");
            alert.setHeaderText("User: " + Global.CurrentAccount.getCurrentUser().getUsername());
            Optional<ButtonType> buttonResult = alert.showAndWait();
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

}
