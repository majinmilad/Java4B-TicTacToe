package ServerSide;

import Messages.DeleteUserMsg;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import modules.BaseModel;
import modules.User;
import sqlite.DatabaseManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class serverModifyUsersController implements Initializable {

    @FXML
    ListView<String> usersList;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        populateUserList();
    }

    @FXML
    void updateButtonClicked(ActionEvent event) {

    }

    @FXML
    void deleteButtonClicked(ActionEvent event)
    {
        User user = (User) DatabaseManager.getInstance().getUser(usersList.getSelectionModel().getSelectedItem());
        if(user != null)
        {
            DeleteUserMsg deleteUserMsg = new DeleteUserMsg(user);
            Server.getInstance().processMessage(deleteUserMsg);
            //still need to remove the deleted account from the userList at moment of deletion
        }
    }

    void populateUserList()
    {
        usersList.getItems().clear();

        List<BaseModel> list = DatabaseManager.getInstance().list(new User());

        for (BaseModel u : list)
        {
            usersList.getItems().add(((User) u).getUsername());
        }
    }
}
