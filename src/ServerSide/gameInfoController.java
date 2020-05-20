package ServerSide;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import modules.*;
import sqlite.DatabaseManager;

public class gameInfoController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private TextFlow gameDetailBox;

    @FXML
    private Tab movesTab;

    @FXML
    private TextFlow movesBox;

    private String mode;
    private Game   currentGame;
    private User    p1;
    private User    p2;

    private enum gameStat{ENDED, RUNNING, WAITING }

    public void initWindows(Game currentGame, User p1, User p2)
    {
        gameDetailBox.getChildren().clear();
        movesBox.getChildren().clear();
        this.currentGame = currentGame;
        this.p1 = p1;
        this.p2 = p2;

           try{
               if (currentGame.getP2Id().equals("1")) {
                   mode = "Player vs Computer";
               } else if (currentGame.getP2Id().equals(currentGame.getP1Id())) {
                   mode = "Player vs Player Local";
               } else {
                   mode = "Player vs Player Online";
               }
           }
           catch (NullPointerException e)
           {
               mode = "Plater vs Player Online";
           }

            if(currentGame.getStatus().equals("ENDED"))
            {
                populateCompletedGame();
            }
            else if(currentGame.getStatus().equals("RUNNING"))
            {
                populateRunningGame();
            }
            else
            {
                populateWaitingGame();
            }

    }

    private void populateRunningGame() {
        movesTab.setDisable(false);
        getGameDetials(gameStat.RUNNING);
        getViewers();
        getMoves();

    }

    private void populateCompletedGame() {
        movesTab.setDisable(false);
        getGameDetials(gameStat.ENDED);
        getViewers();
        getMoves();

    }

    private void populateWaitingGame()
    {
        movesTab.setDisable(true);
        getGameDetials(gameStat.WAITING);

    }

    private void getMoves() {
        List<BaseModel> moves = DatabaseManager.getInstance().queryList(new Move(),"WHERE gameId = \'" + currentGame.getGameId() + "\' "
                +  "ORDER BY time ASC");

        String currentPlayer;

        for (BaseModel m : moves)
        {
            if(((Move)m).getId() == p1.getUserID())
            {
                currentPlayer = p1.getUsername();
            }
            else
            {
                currentPlayer = p2.getUsername();
            }

            movesBox.getChildren().add(new Text("[Time]:\t" + ((Move)m).getTime() + "\t\t[Player]:\t" + currentPlayer + "\t\t[Move(X,Y)]:\t(" + ((Move)m).getXcoord() + ',' + ((Move)m).getYcoord() + ")\n\n"));
        }

        Text t = new Text();

        if(currentGame.getWinnerId().equals('0'))
        {
            t.setText("Game Ended in Tie");
        }
        else if (currentGame.getWinnerId().equals(p1.getUserID()))
        {
            t.setText("Player: " + p1.getUsername() + "\twon the game!");
            t.setFill(Color.GREEN);
        }
        else if(currentGame.getWinnerId().equals(p2.getUserID()))
        {
            t.setText("Player: " +  p2.getUsername() + "\twon the game!");
            t.setFill(Color.GREEN);
        }

        movesBox.getChildren().add(t);
    }

    private void getViewers() {
        ArrayList<BaseModel> viewers = (ArrayList<BaseModel>) DatabaseManager.getInstance().queryList(new GameViewers(currentGame.getGameId(), ""), "");
        if (viewers.size() > 0) {
            User temp = new User();
            for (BaseModel element : viewers) {
                temp = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + ((GameViewers) element).getPlayerId() + "\' ");

                if(viewers.indexOf(element) == 0)
                {
                    gameDetailBox.getChildren().add(new Text(temp.getUsername()+ "\n"));
                }
                else
                {
                    gameDetailBox.getChildren().add(new Text("\t\t  " + temp.getUsername()+ "\n"));
                }

            }
        } else
        {
            gameDetailBox.getChildren().add(new Text("none"));
        }
    }

    public void getGameDetials(gameStat stat)
    {
        User winner = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + currentGame.getWinnerId() + "\' ");

        Text t1 = new Text("[Game ID]: ");
        t1.setFont(Font.font("System", FontWeight.EXTRA_BOLD , 15));
        Text t2 = new Text(currentGame.getGameId());
        t2.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));
        Text t3 = new Text("\n[Game Mode]: ");
        t3.setFont(Font.font("System", FontWeight.BOLD , 15));
        Text t4 = new Text(mode);
        t4.setFill(Color.BLUE);
        t4.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));

        Text t5 = new Text("\n\n[Start Time]: ");
        t5.setFont(Font.font("System", FontWeight.BOLD , 15));
        Text t6 = new Text();
        if(stat.equals(gameStat.ENDED) || stat.equals(gameStat.RUNNING))
        {
            t6.setText(currentGame.getStartTime());
            t6.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));
        }
        else
        {
            t6.setText("TBD");
        }


        Text t7 = new Text("\n[End Time]: ");
        t7.setFont(Font.font("System", FontWeight.BOLD , 15));
        Text t8 = new Text();
        if(stat.equals(gameStat.ENDED))
        {
            t8.setText(currentGame.getEndTime());
            t8.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));
        }
        else if (stat.equals(gameStat.RUNNING))
        {
            t8.setText("TBD");
        }

        Text t9 = new Text("\n\n[Creator & Player 1]: ");
        t9.setFont(Font.font("System", FontWeight.BOLD , 15));
        Text t10 = new Text(p1.getUsername());
        t10.setFill(Color.BLUE);
        t10.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));

        Text t11 = new Text();
        Text t12 = new Text();
        Text t13 = new Text();
        Text t14 = new Text();
        Text t15 = new Text();
        if(stat.equals(gameStat.WAITING))
        {
            t11.setText("\nWaiting For Player 2...");
        }
        else
        {
            t11.setText("\n\t\t  [Player 2]: ");
            t11.setFont(Font.font("System", FontWeight.BOLD , 15));
            t12.setText(p2.getUsername());
            t12.setFill(Color.BLUE);
            t12.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));

            t13.setText("\n\n[Winner]:  ");
            t13.setFont(Font.font("System", FontWeight.BOLD , 15));

            if(stat.equals(gameStat.RUNNING))
            {
                t14.setText("TBD");
            }
            else
            {
                t14.setText(winner.getUsername());
                t14.setFill(Color.GREEN);
            }
            t14.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));

            t15.setText("\n\n[Viewers]: ");
            t15.setFont(Font.font("System", FontWeight.BOLD , 15));

        }

        if(stat.equals(gameStat.WAITING))
        {
            gameDetailBox.getChildren().addAll(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
        }
        else
        {
            gameDetailBox.getChildren().addAll(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11,t12, t13, t14, t15);
        }

    }

}
