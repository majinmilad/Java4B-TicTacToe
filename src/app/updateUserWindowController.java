package app;

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
import modules.User;

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

    @Override
    public void initialize(URL x, ResourceBundle y)
    {
        username.setText(Global.CurrentAccount.getCurrentUser().getUsername());
        firstName.setText(Global.CurrentAccount.getCurrentUser().getFirstName());
        lastName.setText(Global.CurrentAccount.getCurrentUser().getLastName());
        password.setText(Global.CurrentAccount.getCurrentUser().getPassword());

        update.setFocusTraversable(true);
        update.requestFocus();
    }

    @FXML
    void updateButtonClicked(ActionEvent event)
    {
        if(!username.getText().isBlank() && !password.getText().isBlank()
                && !firstName.getText().isBlank() && !lastName.getText().isBlank())
        {
            //prep update message
            User newUser = new User(username.getText(), password.getText(), firstName.getText(), lastName.getText(), Global.CurrentAccount.getCurrentUser().getStatus(), Global.CurrentAccount.getCurrentUser().getUserID(), Global.CurrentAccount.getCurrentUser().getCreation());
            UpdateUserMsg updateUserMsg = new UpdateUserMsg(newUser);

            try
            {
                //send registration msg
                Global.toServer.writeObject(updateUserMsg);
                Global.toServer.flush();

                //receive update status
                boolean successfulUpdate = Global.fromServer.readBoolean();

                if(successfulUpdate)
                {
                    //update CurrentAccount
                    Global.CurrentAccount.update(newUser);

                    errorLabel.setTextFill(Color.LIMEGREEN);
                    errorLabel.setText("User successfully updated");
                }
                else
                {
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("Unable to update user");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException {
        Parent menuWindow = FXMLLoader.load(getClass().getResource("accountSettingsWindow.fxml"));
        Scene menuScene = new Scene(menuWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(menuScene);
        window.show();
    }

}
