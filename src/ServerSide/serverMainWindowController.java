package ServerSide;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
        import javafx.scene.control.TextArea;
import modules.BaseModel;
import modules.User;
import sqlite.DatabaseManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class serverMainWindowController implements Initializable {

    @FXML
    private TextArea messageBox;

    @FXML
    private TextArea onlineUserBox;

    @FXML
    private Button addUserButton;

    @FXML
    private Button modifyUserButton;

    @FXML
    private Button refreshButton;

    @Override
    public void initialize(URL x, ResourceBundle y)
    {
        refreshButtonClicked(new ActionEvent());
    }

    @FXML
    void addUserClicked(ActionEvent event) {

    }

    @FXML
    void modifyUserButtonClicked(ActionEvent event) {

    }

    @FXML
    void refreshButtonClicked(ActionEvent event)
    {
        onlineUserBox.clear();

        List<BaseModel> list = DatabaseManager.getInstance().query(new User(), "WHERE status = 'ONLINE'");
        for (BaseModel u : list)
        {
            onlineUserBox.appendText(((User) u).getUsername() + "\n");
        }
    }

}
