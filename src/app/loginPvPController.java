package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class loginPvPController {

    @FXML
    private Button backButton;

    @FXML
    private ListView<?> gameList;

    @FXML
    private Button joinButton;

    @FXML
    private Button createGameButton;

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException {
        Parent mainWindow = FXMLLoader.load(getClass().getResource("mainMenuWindow.fxml"));
        Scene mainScene = new Scene(mainWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }

    @FXML
    void createButtonClicked(ActionEvent event) {

    }

    @FXML
    void joinButtonClicked(ActionEvent event) {

    }

}