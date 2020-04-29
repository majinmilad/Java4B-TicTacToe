package app;

import Messages.LogoutMsg;
import TicTacToe.gameWindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

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
        //give option for local or remote
        //add windows in between

        FXMLLoader loader = new FXMLLoader(gameWindowController.class.getResource("gameWindow.fxml"));
        Parent boardParent = loader.load();

        gameWindowController setController = loader.getController();
        setController.initializeName("Player 1", "Player 2");

        Scene boardScene = new Scene(boardParent, 950, 775);
        boardScene.getStylesheets().add(getClass().getResource("/TicTacToe/gameWindow.css").toExternalForm());

        Stage boardWindow = new Stage();
        boardWindow.setScene(boardScene);
        boardWindow.show();
    }

    @FXML
    void pvcButtonClicked(ActionEvent event) throws IOException
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

    @FXML
    void watchGamesButtonClicked(ActionEvent event)
    {

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
