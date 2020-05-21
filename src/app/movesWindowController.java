package app;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import modules.BaseModel;
import modules.Game;
import modules.Move;
import modules.User;
import sqlite.DatabaseManager;

import java.awt.event.ActionEvent;
import java.util.List;

public class movesWindowController{

    @FXML
    private TextFlow movesScreen;

    @FXML
    private Button closeButton;

    @FXML
    public void closeButtonClicked() {
        movesScreen.getChildren().clear();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void initWindow(String gameId, String p1Id, String p2Id) {
        List<BaseModel> moves = DatabaseManager.getInstance().queryList(new Move(),"WHERE gameId = \'" + gameId + "\' "
                        +  "ORDER BY time ASC");

        User p1 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + p1Id + "\' ");
        User p2 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + p2Id + "\' ");
        String currentPlayer;

        for (BaseModel m : moves)
        {
            if(((Move)m).getPlayerId().equals(p1Id))
            {
                currentPlayer = p1.getUsername();
            }
            else
            {
                currentPlayer = p2.getUsername();
            }

            movesScreen.getChildren().add(new Text("[Time]:\t" + ((Move)m).getTime() + "\t\t[Player]:\t" + currentPlayer + "\t\t[Move(X,Y)]:\t(" + ((Move)m).getXcoord() + ',' + ((Move)m).getYcoord() + ")\n\n"));
        }

        Game game = (Game) DatabaseManager.getInstance().query(new Game(), "WHERE UUID == \'" + gameId + "\' ");

        Text t = new Text();

        if (game.getWinnerId().equals(p1.getUserID())) {
            t.setText("Player: " + p1.getUsername() + "\twon the game!");
            t.setFill(Color.GREEN);
        } else if (game.getWinnerId().equals(p2.getUserID())) {
            t.setText("Player: " + p2.getUsername() + "\twon the game!");
            t.setFill(Color.GREEN);
        }
        else
        {
            t.setText("Game Ended in Tie");
            t.setFill(Color.BLUE);
        }

        movesScreen.getChildren().add(t);
    }


}
