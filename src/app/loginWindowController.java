package app;

import Messages.LoginMsg;
import Messages.LogoutMsg;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import modules.User;
import sqlite.DatabaseManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class loginWindowController {

    @FXML
    private TextField username;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField password;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    @FXML
    void loginButtonClicked(ActionEvent event) throws IOException, ClassNotFoundException
    {
        //request login from server
        Global.toServer.writeObject(new LoginMsg(username.getText(), password.getText()));
        boolean authenticated = Global.fromServer.readBoolean();

        if(authenticated)
        {
            boolean userOnline = Global.fromServer.readBoolean();

            if(!userOnline)
            {
                //update the current account object
                User user = (User) Global.fromServer.readObject();
                Global.CurrentAccount.update(user);

                //proceed to the menu window
                Parent menuWindow = FXMLLoader.load(getClass().getResource("menuWindow.fxml"));
                Scene menuScene = new Scene(menuWindow);
                Stage menuWindowStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                menuWindowStage.setScene(menuScene);
                menuWindowStage.show();

                //set logout upon exiting the menu window
                menuWindowStage.setOnCloseRequest(anonymF ->
                {
                    try {
                        Global.toServer.writeObject(new LogoutMsg(Global.CurrentAccount.getCurrentUser()));
                        Global.toServer.flush();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            else
            {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("User is already online");
            }
        }
        else
        {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Wrong Username and Password");
        }
    }

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException {
        Parent mainWindow = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        Scene mainScene = new Scene(mainWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }
}
