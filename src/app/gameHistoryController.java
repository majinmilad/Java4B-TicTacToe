package app;

import TicTacToe.gameWindowController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import modules.*;
import sqlite.DatabaseManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class gameHistoryController implements Initializable {

    @FXML
    private Button backButton;

    @FXML
    private ListView<GameInHistory> gameList;

    @FXML
    private TextFlow gameDetails;

    @FXML
    private Button movesButton;

    @FXML
    private Label winScore;

    @FXML
    private Label lossScore;

    @FXML
    private Label tieScore;


    private ArrayList<BaseModel> gameHistory;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
     getGames();
     getWLT();
     movesButton.setDisable(true);
    }

    @FXML
    void backButtonClicked(ActionEvent event) throws IOException {
        Parent mainWindow = FXMLLoader.load(getClass().getResource("mainMenuWindow.fxml"));
        Scene mainScene = new Scene(mainWindow);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.show();
    }

    @FXML
    void movesButtonClicked(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(gameHistoryController.class.getResource("movesWindow.fxml"));
        Parent movesParent = loader.load();

        //set players involved in the controller
        movesWindowController setController = loader.getController();
        Game selectedGame = gameList.getSelectionModel().getSelectedItem().getGameInfo().getGame();
        setController.initWindow(selectedGame.getGameId(),selectedGame.getP1Id(),selectedGame.getP2Id());

        Scene movesScene = new Scene(movesParent);
        Stage movesStage = new Stage();
        movesStage.setScene(movesScene);
        movesStage.show();
    }

    class GameInHistory
    {
        //class used to store games in ListView while printing a specific string via toString()
        private GameInfo gameInfo;
        @Override
        public String toString()
        {
            String result;
           if(gameInfo.getGame().getWinnerId().equals(Global.CurrentAccount.getCurrentUser().getUserID()))
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
        gameHistory = (ArrayList<BaseModel>) DatabaseManager.getInstance().queryList(new Game(), "WHERE p2Id != 1 AND (p1Id == \'" +  Global.CurrentAccount.getCurrentUser().getUserID()  + "\' "
                + "OR p2Id == \'" +  Global.CurrentAccount.getCurrentUser().getUserID()  + "\') AND gameStatus == 'ENDED' ");

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
        List<BaseModel> countList = DatabaseManager.getInstance().queryList(new Game(), "WHERE p2Id != 1 AND gameStatus == 'ENDED' AND winnerId == \'" + Global.CurrentAccount.getCurrentUser().getUserID() + "\' ");

        winScore.setText(Integer.toString(countList.size()));

        countList = DatabaseManager.getInstance().queryList(new Game(), "WHERE p2Id != 1 AND (p1Id == \'" +  Global.CurrentAccount.getCurrentUser().getUserID()  + "\' "
                + "OR p2Id == \'" +  Global.CurrentAccount.getCurrentUser().getUserID()  + "\') AND gameStatus == 'ENDED' AND winnerId == '0' ");

        tieScore.setText(Integer.toString(countList.size()));

        countList = DatabaseManager.getInstance().queryList(new Game(), "WHERE p2Id != 1 AND (p1Id = \'" +  Global.CurrentAccount.getCurrentUser().getUserID()  + "\' "
                + "OR p2Id = \'" +  Global.CurrentAccount.getCurrentUser().getUserID()  + "\') AND gameStatus == 'ENDED' AND winnerId != '0' AND winnerId != \'" + Global.CurrentAccount.getCurrentUser().getUserID() + "\' ");

        lossScore.setText(Integer.toString(countList.size()));
    }

    @FXML
    private void getGameDetails()
    {

        try{
            gameDetails.getChildren().clear();
            Game selectedGame = gameList.getSelectionModel().getSelectedItem().getGameInfo().getGame();
            movesButton.setDisable(false);
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
            t2.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 12));
            Text t3 = new Text("\n[Game Mode]: ");
            t3.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t4 = new Text(mode);
            t4.setFill(Color.BLUE);
            t4.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 12));

            Text t5 = new Text("\n\n[Start Time]: ");
            t5.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t6 = new Text(selectedGame.getStartTime());
            t6.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 12));
            Text t7 = new Text("\n[End Time]: ");
            t7.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t8 = new Text(selectedGame.getEndTime());
            t8.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 12));

            Text t9 = new Text("\n\n[Creator & Player 1]: ");
            t9.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t10 = new Text(p1.getUsername());
            t10.setFill(Color.BLUE);
            t10.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 12));
            Text t11 = new Text("\n\t\t  [Player 2]: ");
            t11.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t12 = new Text(p2.getUsername());
            t12.setFill(Color.BLUE);
            t12.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 12));

            Text t13 = new Text("\n\n[Winner]:  ");
            t13.setFont(Font.font("System", FontWeight.BOLD , 15));
            Text t14 = new Text(winner.getUsername());
            t14.setFill(Color.GREEN);
            t14.setFont(Font.font("Aldhabi", FontWeight.SEMI_BOLD , 12));
            Text t15 = new Text("\n\n[Viewers]: ");
            t15.setFont(Font.font("System", FontWeight.BOLD , 15));

            gameDetails.getChildren().addAll(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11,t12, t13, t14, t15);

//            gameDetails.setText("Game ID: " + selectedGame.getGameId() + "\t\tGame Mode: " + mode
//                    + "\n\nStart Time: " + selectedGame.getStartTime() + "\tEnd Time: " + selectedGame.getEndTime()
//                    + "\n\nCreator (Player 1): " + p1.getUsername() + " " + p1.getUserID()
//                    + "\nPlayer 2: " + p2.getUsername() + " " + p2.getUserID()
//                    + "\nWinner: " + winner.getUsername() + " " + winner.getUserID() + "\n\nViewers:\n");

            viewers = (ArrayList<BaseModel>) DatabaseManager.getInstance().queryList(new GameViewers(selectedGame.getGameId(), "", ""), "");
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
            movesButton.setDisable(true);
            System.out.println("User clicked on Empty Cell");
        }

    }

}
