package ServerSide;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.SQLException;

public class ServerMain extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Server server = Server.getInstance();

        Parent root = FXMLLoader.load(getClass().getResource("../ServerSide/serverMainWindow.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) throws SQLException
    {
        launch(args);
    }
}
