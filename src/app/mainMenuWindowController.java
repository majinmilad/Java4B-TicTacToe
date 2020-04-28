package app;

import Messages.LogoutMsg;
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
    void pvcButtonClicked(ActionEvent event) {

    }

    @FXML
    void pvpButtonClicked(ActionEvent event) {

    }

    @FXML
    void watchGamesButtonClicked(ActionEvent event) {

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
