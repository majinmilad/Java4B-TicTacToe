package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class primaryWindowController {

    @FXML
    private Button createAccountButton;

    @FXML
    private Button loginButton;

    @FXML
    void createAccountClicked(ActionEvent event) throws IOException
    {
        Parent addUserWindow = FXMLLoader.load(getClass().getResource("createAccountWindow.fxml"));
        Scene addUserScene = new Scene(addUserWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(addUserScene);
        window.show();
    }

    @FXML
    void loginButtonClicked(ActionEvent event) throws IOException
    {
        Parent loginWindow = FXMLLoader.load(getClass().getResource("loginWindow.fxml"));
        Scene loginScene = new Scene(loginWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.show();
    }

}

