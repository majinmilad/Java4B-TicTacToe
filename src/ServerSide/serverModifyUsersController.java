package ServerSide;

import Messages.DeleteUserMsg;
import TicTacToe.gameWindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import modules.BaseModel;
import modules.User;
import sqlite.DatabaseManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class serverModifyUsersController implements Initializable {

    @FXML
    ListView<String> usersList;

    @FXML
    private Button viewButton;

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
    void viewButtonClicked(ActionEvent event)
    {

    }

    @FXML
    void updateButtonClicked(ActionEvent event)
    {
        User user = new User(usersList.getSelectionModel().getSelectedItem());
        user = (User) DatabaseManager.getInstance().get(user);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServerSide/updateUserWindow.fxml"));
            Parent updateParent = loader.load();

            //set user involved in the controller
            updateUserWindowController setController = loader.getController();
            setController.setUserToUpdate(user);

            //show update window
            Scene updateScene = new Scene(updateParent);
            Stage updateStage = (Stage) ((Node) event.getSource()).getScene().getWindow();;
            updateStage.setScene(updateScene);
            updateStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteButtonClicked(ActionEvent event) throws InterruptedException
    {
        User user = new User(usersList.getSelectionModel().getSelectedItem());
        user = (User) DatabaseManager.getInstance().get(user);
        if(user != null && !user.getStatus().equals("INACTIVE"))
        {
            DeleteUserMsg deleteUserMsg = new DeleteUserMsg(user);
            Server.getInstance().processMessage(deleteUserMsg);
            TimeUnit.SECONDS.sleep(2);
            populateUserList();
        }
    }

    private void populateUserList()
    {
        usersList.getItems().clear();

        List<BaseModel> list = DatabaseManager.getInstance().queryList(new User(), "AND status != 'INACTIVE'");

        for (BaseModel u : list)
        {
            usersList.getItems().add(((User) u).getUsername());
        }
    }
}
