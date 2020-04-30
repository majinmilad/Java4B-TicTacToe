package app;

import Messages.ReactivateUserMsg;
import Messages.RegistrationMsg;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import modules.User;

import java.io.IOException;
import java.util.Optional;

public class createAccountWindowController {

    @FXML
    private Button createUser;

    @FXML
    private TextField username;

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField password;

    @FXML
    private Label errorLabel;

    @FXML
    private Button backButton;

    @FXML
    void createUserClicked(ActionEvent event) {

        if(!username.getText().isBlank() && !password.getText().isBlank()
            && !firstName.getText().isBlank() && !lastName.getText().isBlank())
        {
            //prep registration message
            User newUser = new User(username.getText(), password.getText(), firstName.getText(), lastName.getText());
            RegistrationMsg regMsg = new RegistrationMsg(newUser);

            try {
                //send registration msg
                Global.toServer.writeObject(regMsg);
                Global.toServer.flush();

                //receive account-already-exists status
                boolean accountAlreadyExists = Global.fromServer.readBoolean();

                if(!accountAlreadyExists)
                {
                    //receive registration status
                    boolean successfulInsert = Global.fromServer.readBoolean();

                    if(successfulInsert)
                    {
                        errorLabel.setTextFill(Color.LIMEGREEN);
                        errorLabel.setText("User successfully added");
                    }
                    else
                    {
                        errorLabel.setTextFill(Color.RED);
                        errorLabel.setText("Unable to add user");
                    }
                }
                else
                {
                    //receive inactive status
                    boolean accountInactive = Global.fromServer.readBoolean();

                    if(accountInactive)
                    {
                        String message = "This account is inactive, do you want to reactivate it?"
                                + "\nThe information entered will replace any old account details.";
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
                        alert.setTitle("Reactivate Account");
                        alert.setHeaderText("Username: " + regMsg.getUser().getUsername());
                        Optional<ButtonType> buttonResult = alert.showAndWait();

                        if(buttonResult.get() == ButtonType.OK)
                        {
                            //send reactivate message
                            ReactivateUserMsg reactivateMsg = new ReactivateUserMsg(newUser);
                            Global.toServer.writeObject(reactivateMsg);
                            Global.toServer.flush();

                            errorLabel.setTextFill(Color.LIMEGREEN);
                            errorLabel.setText("User successfully reactivated");
                        }
                    }
                    else //account exists and is active
                    {
                        errorLabel.setTextFill(Color.RED);
                        errorLabel.setText("An account with this username already exists");
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Please fill out all fields to register");
        }
    }


    @FXML
    void backButtonClicked(ActionEvent event) throws IOException {
        Parent mainWindow = FXMLLoader.load(getClass().getResource("primaryWindow.fxml"));
        Scene mainScene = new Scene(mainWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }

}
