package ServerSide;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import modules.BaseModel;
import modules.User;
import sqlite.DatabaseManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class serverUsersController implements Initializable {

    @FXML
    ListView<String> usersList;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        List<BaseModel> list = DatabaseManager.getInstance().list(new User());

        for (BaseModel u : list)
        {
            usersList.getItems().add(((User) u).getUsername() + "\n");
        }
    }

    @FXML
    void updateButtonClicked(ActionEvent event) {

    }

    @FXML
    void deleteButtonClicked(ActionEvent event) {

    }
}
