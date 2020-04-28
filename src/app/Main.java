package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.Socket;
import java.sql.SQLException;


public class Main extends Application {

    // ../app/primaryWindow.fxml
        // ../app/createAccountWindow.fxml
        // ../app/loginWindow.fxml
            // ../app/mainMenuWindow.fxml
                // ../app/accountSettingsWindow.fxml
                    // ../app/updateUserWindow.fxml

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        //establish connection to the Server
        Socket socket = new Socket("localhost", 7777);

        if(socket.isConnected()) {
            System.out.println("Connected to Server...\n");
            Global.setSocketWithServer(socket);
        }
        else
            System.out.println("Connection to Server failed...\n");

        Parent root = FXMLLoader.load(getClass().getResource("primaryWindow.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) throws SQLException
    {
        launch(args);
    }
}
