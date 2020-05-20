package ServerSide;

import app.gameHistoryController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import modules.*;
import sqlite.DatabaseManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class viewPlayerController{

    @FXML
    private Label nameHistory;

    @FXML
    private ListView<GameInHistory> gameList;

    @FXML
    private Tab generaltab;

    @FXML
    private TextFlow gameDetails;

    @FXML
    private Tab moveTab;

    @FXML
    private TextFlow moveBox;

    @FXML
    private Label currentStat;

    @FXML
    private Label wScore;

    @FXML
    private Label lScore;

    @FXML
    private Label tScore;

    @FXML
    private Label pUsername;

    @FXML
    private Label pId;

    @FXML
    private TabPane tPane;

    private ArrayList<BaseModel> gameHistory;

    private User currentUser;

    private Game selectedGame;


    void getMoves() {
        try {
            selectedGame = gameList.getSelectionModel().getSelectedItem().getGameInfo().getGame();
            List<BaseModel> moves = DatabaseManager.getInstance().queryList(new Move(), "WHERE gameId = \'" + selectedGame.getGameId() + "\' "
                    + "ORDER BY time ASC");
            User p1 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + selectedGame.getP1Id() + "\' ");
            User p2 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + selectedGame.getP2Id() + "\' ");
            String currentPlayer;

            for (BaseModel m : moves) {
                if (((Move) m).getId() == p1.getUserID()) {
                    currentPlayer = p1.getUsername();
                } else {
                    currentPlayer = p2.getUsername();
                }

                moveBox.getChildren().add(new Text("[Time]:\t" + ((Move) m).getTime() + "\n[Player]:\t" + currentPlayer + "\t\t[Move(X,Y)]:\t(" + ((Move) m).getXcoord() + ',' + ((Move) m).getYcoord() + ")\n\n"));
            }

            Game game = (Game) DatabaseManager.getInstance().query(new Game(), "WHERE UUID == \'" + selectedGame.getGameId() + "\' ");

            Text t = new Text();

            if (game.getWinnerId().equals('0')) {
                t.setText("Game Ended in Tie");
            } else if (game.getWinnerId().equals(p1.getUserID())) {
                t.setText("Player: " + p1.getUsername() + "\twon the game!");
                t.setFill(Color.GREEN);
            } else {
                t.setText("Player: " + p2.getUsername() + "\twon the game!");
                t.setFill(Color.GREEN);
            }

            moveBox.getChildren().add(t);
        } catch (NullPointerException e) {

        }
    }

    private void setLabels() {
        nameHistory.setText(currentUser.getUsername() + "\'s History");
        currentStat.setText(currentUser.getStatus());
        pUsername.setText(currentUser.getUsername());
        pId.setText(currentUser.getUserID());
    }

    public void initUser(User user)
    {
        currentUser = user;
        getGames();
        getWLT();
        setLabels();
        moveBox.setPrefWidth(Double.MAX_VALUE);
    }

    class GameInHistory
    {
        //class used to store games in ListView while printing a specific string via toString()
        private GameInfo gameInfo;
        @Override
        public String toString()
        {
            String result;
            if(gameInfo.getGame().getWinnerId().equals(currentUser.getUserID()))
            {
                result = "WON";
            }
            else if (gameInfo.getGame().getWinnerId().equals("0"))
            {
                result = "TIE";
            }
            else
            {
                result = "LOSS";
            }
            String t = "Date: " + gameInfo.getGame().getStartTime() + "\tResult: " + result;
            return t;
        }

        GameInfo getGameInfo() {
            return gameInfo;
        }
        void setGameInfo(GameInfo gameInfo) {
            this.gameInfo = gameInfo;
        }
    }

    public void getGames()
    {
            gameHistory = (ArrayList<BaseModel>) DatabaseManager.getInstance().queryList(new Game(), "WHERE (p1Id == \'" +  currentUser.getUserID()  + "\' "
                    + "OR p2Id == \'" +  currentUser.getUserID() + "\') AND gameStatus == 'ENDED' ");

        System.out.println("gameHistory size: " + gameHistory.size());

            for(BaseModel b: gameHistory)
            {
                GameInfo gameInfo = new GameInfo();
                gameInfo.setGame((Game)b);
                GameInHistory g = new GameInHistory();
                g.setGameInfo(gameInfo);
                gameList.getItems().add(g);
            }
    }


    private void getWLT()
    {
        List<BaseModel> countList = DatabaseManager.getInstance().queryList(new Game(), "WHERE p2Id != 1 AND gameStatus == 'ENDED' AND winnerId == \'" + currentUser.getUserID() + "\' ");

        wScore.setText(Integer.toString(countList.size()));

        countList = DatabaseManager.getInstance().queryList(new Game(), "WHERE p2Id != 1 AND (p1Id == \'" +  currentUser.getUserID()  + "\' "
                + "OR p2Id == \'" +  currentUser.getUserID()  + "\') AND gameStatus == 'ENDED' AND winnerId == '0' ");

        tScore.setText(Integer.toString(countList.size()));

        countList = DatabaseManager.getInstance().queryList(new Game(), "WHERE p2Id != 1 AND (p1Id = \'" +  currentUser.getUserID()  + "\' "
                + "OR p2Id = \'" +  currentUser.getUserID()  + "\') AND gameStatus == 'ENDED' AND winnerId != '0' AND winnerId != \'" + currentUser.getUserID() + "\' ");

        lScore.setText(Integer.toString(countList.size()));
    }

    @FXML
    private void getGameDetails()
    {

        try{
            selectedGame = gameList.getSelectionModel().getSelectedItem().getGameInfo().getGame();
            gameDetails.getChildren().clear();
            moveBox.getChildren().clear();
            tPane.getSelectionModel().select(0);
            ArrayList<BaseModel> viewers;

            User p1 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + selectedGame.getP1Id() + "\' ");
            User p2 = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + selectedGame.getP2Id() + "\' ");
            User winner = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + selectedGame.getWinnerId() + "\' ");

            String mode;
            if (selectedGame.getP2Id().equals("1")) {
                mode = "Player vs Computer";
            } else if (selectedGame.getP2Id().equals(selectedGame.getP1Id())) {
                mode = "Player vs Player Local";
            } else {
                mode = "Player vs Player Online";
            }

            Text t1 = new Text("[Game ID]: ");
            t1.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t2 = new Text(selectedGame.getGameId());
            t2.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));
            Text t3 = new Text("\n[Game Mode]: ");
            t3.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t4 = new Text(mode);
            t4.setFill(Color.BLUE);
            t4.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));

            Text t5 = new Text("\n\n[Start Time]: ");
            t5.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t6 = new Text(selectedGame.getStartTime());
            t6.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));
            Text t7 = new Text("\n[End Time]: ");
            t7.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t8 = new Text(selectedGame.getEndTime());
            t8.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));

            Text t9 = new Text("\n\n[Creator & Player 1]: ");
            t9.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t10 = new Text(p1.getUsername());
            t10.setFill(Color.BLUE);
            t10.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));
            Text t11 = new Text("\n\t\t  [Player 2]: ");
            t11.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t12 = new Text(p2.getUsername());
            t12.setFill(Color.BLUE);
            t12.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));

            Text t13 = new Text("\n\n[Winner]:  ");
            t13.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t14 = new Text(winner.getUsername());
            t14.setFill(Color.GREEN);
            t14.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 13));
            Text t15 = new Text("\n\n[Viewers]: ");
            t15.setFont(Font.font("System", FontWeight.BOLD , 15));

            gameDetails.getChildren().addAll(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11,t12, t13, t14, t15);

//            gameDetails.setText("Game ID: " + selectedGame.getGameId() + "\t\tGame Mode: " + mode
//                    + "\n\nStart Time: " + selectedGame.getStartTime() + "\tEnd Time: " + selectedGame.getEndTime()
//                    + "\n\nCreator (Player 1): " + p1.getUsername() + " " + p1.getUserID()
//                    + "\nPlayer 2: " + p2.getUsername() + " " + p2.getUserID()
//                    + "\nWinner: " + winner.getUsername() + " " + winner.getUserID() + "\n\nViewers:\n");

            viewers = (ArrayList<BaseModel>) DatabaseManager.getInstance().queryList(new GameViewers(selectedGame.getGameId(), ""), "");
            if (viewers.size() > 0) {
                User temp = new User();
                for (BaseModel element : viewers) {
                    temp = (User) DatabaseManager.getInstance().query(new User(), "WHERE UUID = \'" + ((GameViewers) element).getPlayerId() + "\' ");

                    if(viewers.indexOf(element) == 0)
                    {
                        gameDetails.getChildren().add(new Text(temp.getUsername()+ "\n"));
                    }
                    else
                    {
                        gameDetails.getChildren().add(new Text("\t\t  " + temp.getUsername()+ "\n"));
                    }

                }
            } else
            {
                gameDetails.getChildren().add(new Text("none"));
            }
        }
        catch (NullPointerException e){
            System.out.println("User clicked on Empty Cell");
        }

        getMoves();

    }

}
