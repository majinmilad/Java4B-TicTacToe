package ServerSide;

import Messages.LogoutMsg;
import Messages.UpdateUserMsg;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import modules.Game;
import modules.GameViewers;
import modules.User;
import sqlite.DatabaseManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class updateUserWindowController implements Initializable {

    @FXML
    private TextField username;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField password;

    @FXML
    private Button update;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Button kickButton;

    public User user;

    void setUserToUpdate(User user)
    {
        this.user = user;

        username.setText(user.getUsername());
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        password.setText(user.getPassword());
    }

    @Override
    public void initialize(URL x, ResourceBundle y)
    {
        update.setFocusTraversable(true);
        update.requestFocus();
    }

    @FXML
    void updateButtonClicked(ActionEvent event)
    {
        if(!username.getText().isBlank() && !password.getText().isBlank()
                && !firstName.getText().isBlank() && !lastName.getText().isBlank())
        {
            //prep update
            user.setUsername(username.getText());
            user.setFirstName(firstName.getText());
            user.setLastName(lastName.getText());
            user.setPassword(password.getText());

            User successfulUpdate = (User) DatabaseManager.getInstance().update(user);

            if(successfulUpdate != null)
            {
                errorLabel.setTextFill(Color.LIMEGREEN);
                errorLabel.setText("User successfully updated");
            }
            else
            {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Unable to update user");
            }
        }
    }

    @FXML
    void kickButtonClicked()
    {
        Server.getInstance().processMessage(new LogoutMsg(user));
        Game s = (Game) DatabaseManager.getInstance().get(user);

        if(s.getStatus().equalsIgnoreCase("OFFLINE"))
        {
            errorLabel.setTextFill(Color.LIMEGREEN);
            errorLabel.setText("User has been kicked");
        }
        else
        {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Unable to kick user");
        }

    }

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException {
        // open modify users window
        Parent modifyUsersRoot = FXMLLoader.load(getClass().getResource("serverModifyUsers.fxml"));
        Scene modifyUsersScene = new Scene(modifyUsersRoot);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(modifyUsersScene);
        window.show();
    }

}
