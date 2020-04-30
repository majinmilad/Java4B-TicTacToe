package ServerSide;

import Messages.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import modules.BaseModel;
import modules.User;
import sqlite.DatabaseManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

public class serverMainWindowController implements Observer, Initializable {

    @FXML
    private TextArea messageBox;

    @FXML
    private TextArea onlineUserBox;

    @FXML
    private Button modifyUserButton;

    @Override
    public void initialize(URL x, ResourceBundle y)
    {
        Server server = Server.getInstance();
        server.addObserver(this);
        refreshButtonClicked(new ActionEvent());
    }

    @FXML
    void addUserClicked(ActionEvent event) {

    }

    @FXML
    void modifyUserButtonClicked(ActionEvent event) throws IOException
    {
        // open modify users in new window
        Parent modifyUsersRoot = FXMLLoader.load(getClass().getResource("serverModifyUsers.fxml"));
        Scene modifyUsersScene = new Scene(modifyUsersRoot);
        Stage window = new Stage();
        window.setScene(modifyUsersScene);
        window.show();
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

    @Override
    public void update(Observable o, Object arg)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                if(arg instanceof RegistrationMsg)
                {
                    messageBox.appendText("\"" + ((RegistrationMsg) arg).getUser().getUsername() + "\" has registered as a new user\n\n");
                }
                else if(arg instanceof LoginMsg)
                {
                    messageBox.appendText("user \"" + ((LoginMsg) arg).getUsername() + "\" has just logged on\n\n");
                    refreshButtonClicked(new ActionEvent());
                }
                else if(arg instanceof DeleteUserMsg)
                {
                    messageBox.appendText("user \"" + ((DeleteUserMsg) arg).getUser().getUsername() + "\" deleted their account (account deactivated)\n\n");
                    refreshButtonClicked(new ActionEvent());
                }
                else if(arg instanceof LogoutMsg)
                {
                    messageBox.appendText("user \"" + ((LogoutMsg) arg).getUser().getUsername() + "\" has logged out\n\n");
                    refreshButtonClicked(new ActionEvent());
                }
                else if(arg instanceof ReactivateUserMsg)
                {
                    messageBox.appendText("user \"" + ((ReactivateUserMsg) arg).getUser().getUsername() + "\" has reactivated their deleted account\n\n");
                }
                else if(arg instanceof UpdateUserMsg)
                {
                    messageBox.appendText("user \"" + ((UpdateUserMsg) arg).getUser().getUsername() + "\" has updated their account\n\n");
					refreshButtonClicked(new ActionEvent());
                }
            }
        });
    }
}
