package app;

import Messages.DeleteUserMsg;
import Messages.LogoutMsg;
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

public class accountSettingsWindowController {

    @FXML
    private Button updateAccountButton;

    @FXML
    private Button deleteAccountButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button backButton;

    @FXML
    void updateAccountClicked(ActionEvent event) throws IOException
    {
        Parent updateWindow = FXMLLoader.load(getClass().getResource("updateUserWindow.fxml"));
        Scene updateScene = new Scene(updateWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(updateScene);
        window.show();
    }

    @FXML
    void deleteAccountClicked(ActionEvent event) throws IOException
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this account?");
        alert.setTitle("Delete Account");
        alert.setHeaderText("Username: " + Global.CurrentAccount.getCurrentUser().getUsername());
        Optional<ButtonType> buttonResult = alert.showAndWait();

        if(buttonResult.get() == ButtonType.OK)
        {
            Global.toServer.writeObject(new DeleteUserMsg(Global.CurrentAccount.getCurrentUser()));
            Global.CurrentAccount.reset();

            //return to main window
            Parent mainWindow = FXMLLoader.load(getClass().getResource("primaryWindow.fxml"));
            Scene mainScene = new Scene(mainWindow);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(mainScene);
            window.show();
        }
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
    void backButtonClicked(ActionEvent event) throws IOException
    {
        Parent mainMenuWindow = FXMLLoader.load(getClass().getResource("mainMenuWindow.fxml"));
        Scene mainMenuScene = new Scene(mainMenuWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainMenuScene);
        window.show();
    }
}
