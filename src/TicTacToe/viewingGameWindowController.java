package TicTacToe;

import Messages.*;
import app.Global;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import modules.Move;
import modules.User;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class viewingGameWindowController implements Initializable
{
    private boolean isFirstPlayer = true;

    @FXML
    Button b1;
    @FXML
    Button b2;
    @FXML
    Button b3;
    @FXML
    Button b4;
    @FXML
    Button b5;
    @FXML
    Button b6;
    @FXML
    Button b7;
    @FXML
    Button b8;
    @FXML
    Button b9;

    @FXML
    Button backButton;

    @FXML
    Button resetButton;

    @FXML
    GridPane gameBoard;

    @FXML
    Label scoreBoardP1, scoreBoardP2;

    @FXML
    Label turnPrompt;

    @FXML
    Label scoreP1, scoreP2, scoreTie;

    @FXML
    Text player1Name, player2Name;

    static String p1TurnPrompt;
    static String p2TurnPrompt;
    int turnNumber = 1;

    static int player1Score = 0;
    static int player2Score = 0;
    static int tieScore     = 0;


    /********************************/

    ListeningClass listener;
    boolean itsYourTurn;
    String yourSymbol;
    String yourUsername;
    String yourTurnPrompt;
    String opponentSymbol;
    String opponentsUsername;
    String opponentTurnPrompt;

    String thisGameID;


    @Override
    public void initialize(URL x, ResourceBundle y)
    {
        backButton.setVisible(false);
        resetButton.setVisible(false);

        setBoard();

        itsYourTurn = false;

        listener = new ListeningClass();
        listener.start();
    }

    public void setItsYourTurn(boolean value)
    {
        //this function determines if you're first player or second
        itsYourTurn = value;

        if(value) {
            yourSymbol = "O";
            yourUsername = player1Name.getText();
            yourTurnPrompt = p1TurnPrompt;

            opponentSymbol = "X";
            opponentsUsername = player2Name.getText();
            opponentTurnPrompt = p2TurnPrompt;
        }
        else {
            yourSymbol = "X";
            yourUsername = player2Name.getText();
            yourTurnPrompt = p2TurnPrompt;

            opponentSymbol = "O";
            opponentsUsername = player1Name.getText();
            opponentTurnPrompt = p1TurnPrompt;
        }

        //initially display "p1Turn"
        turnPrompt.setText(p1TurnPrompt);
    }

    public void setThisGameID(String thisGameID) {
        this.thisGameID = thisGameID;
    }


    public void buttonClickHandler(ActionEvent evt) throws IOException
    {
        Button clickedButton = (Button) evt.getTarget();
        String buttonLabel = clickedButton.getText();

        if ("".equals(buttonLabel) && itsYourTurn) //make your move
        {
            clickedButton.setText(yourSymbol);
            Color symbolColor = checkSymbol(yourSymbol);
            clickedButton.setTextFill(symbolColor);

           // clickedButton.setTextFill(Color.DODGERBLUE);
            itsYourTurn = false;
            turnPrompt.setText(opponentTurnPrompt);
            turnNumber++;

            //send move to server
            Pair<Integer, Integer> pair = getCoord(clickedButton.getId());
            Move move = new Move(thisGameID, Global.CurrentAccount.getCurrentUser().getUserID(), pair.getKey(), pair.getValue());
            MoveMadeMsg moveMadeMsg = new MoveMadeMsg(move, Global.CurrentAccount.getCurrentUser());
            Global.toServer.writeObject(moveMadeMsg);
            Global.toServer.flush();
        }

        if("".equals(buttonLabel) && !itsYourTurn && player2Name.getText().equals("Computer") && turnNumber % 2 == 0) //computer will make move after you
        {
            turnPrompt.setText(p2TurnPrompt);

            MoveCoord computerMove = executeComputerMove();

            //send move to server
            Move move = new Move(thisGameID, "1", computerMove.row, computerMove.col);
            MoveMadeMsg moveMadeMsg = new MoveMadeMsg(move, new User("Computer"));
            Global.toServer.writeObject(moveMadeMsg);
            Global.toServer.flush();

            itsYourTurn = true;
            turnPrompt.setText(p1TurnPrompt);
            turnNumber++;
        }

        //check if move caused a win
        boolean result = find3InARow();

        if(result && !"Computer".equals(opponentsUsername))
        {
            String winner = yourUsername;

            if(yourUsername.equals(player1Name.getText()))
            {
                player1Score++;
                scoreP1.setText(Integer.toString(player1Score));
            }
            else
            {
                player2Score++;
                scoreP2.setText(Integer.toString(player2Score));
            }

            turnPrompt.setText(winner + " WON!");
            gameBoard.setDisable(true);
            backButton.setVisible(true);

            GameWonMsg gameWonMsg = new GameWonMsg(thisGameID, Global.CurrentAccount.getCurrentUser());
            Global.toServer.writeObject(gameWonMsg);
            Global.toServer.flush();
        }
        else if(result) //PvC game won (by minimax computer)
        {
            player2Score++;
            scoreP2.setText(Integer.toString(player2Score));

            turnPrompt.setText("Computer WON!");
            gameBoard.setDisable(true);
            backButton.setVisible(true);

            User computer = new User("Computer");

            GameWonMsg gameWonMsg = new GameWonMsg(thisGameID, computer);
            Global.toServer.writeObject(gameWonMsg);
            Global.toServer.flush();
        }
        else if(!gameBoard.isDisable() && turnNumber > 9) //tie game
        {
            tieScore++;
            scoreTie.setText(Integer.toString(tieScore));
            disableBoard();
            isFirstPlayer = true;
            turnPrompt.setText("Tie Game!");
            backButton.setVisible(true);

            GameTiedMsg gameTiedMsg = new GameTiedMsg(thisGameID, Global.CurrentAccount.getCurrentUser());
            Global.toServer.writeObject(gameTiedMsg);
            Global.toServer.flush();
        }
    }


    // LISTENING THREAD
    class ListeningClass implements Runnable
    {
        Thread thread;
        boolean keepRunning;

        void start()
        {
            keepRunning = true;
            thread = new Thread(this);
            thread.start();
        }

        void setStopSignal()
        {
            keepRunning = false;
        }

        @Override
        public void run() //this thread's run()
        {
            while(keepRunning)
            {
                try
                {
                    //receive msg from server
                    Object serverMsg = Global.fromServer.readObject();

                    if(serverMsg instanceof KillListenerMsg)
                    {
                        setStopSignal();
                    }
                    else
                    {
                        //perform certain actions on FX application thread
                        if(serverMsg instanceof MoveMadeMsg)
                        {
                            MoveMadeMsg moveMadeMsg = (MoveMadeMsg) serverMsg;

                            //get button
                            Pair<Integer, Integer> pair = new Pair<>(moveMadeMsg.getMove().getXcoord(), moveMadeMsg.getMove().getYcoord());
                            String button = getButton(pair);

                            //register the move
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    paintButton(button, opponentSymbol);

                                    turnPrompt.setText(yourTurnPrompt);
                                    turnNumber++;
                                }
                            });
                        }
                        else if(serverMsg instanceof GameWonMsg)
                        {
                            GameWonMsg gameWonMsg = (GameWonMsg) serverMsg;

                            String winner = gameWonMsg.getGameWinner().getUsername();

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if(winner.equals(player1Name.getText()))
                                    {
                                        player1Score++;
                                        scoreP1.setText(Integer.toString(player1Score));
                                    }
                                    else
                                    {
                                        player2Score++;
                                        scoreP2.setText(Integer.toString(player2Score));
                                    }

                                    turnPrompt.setText(winner + " WON!");
                                    gameBoard.setDisable(true);
                                    backButton.setVisible(true);
                                }
                            });
                        }
                        else if(serverMsg instanceof GameTiedMsg)
                        {
                            GameTiedMsg gameTiedMsg = (GameTiedMsg) serverMsg;

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    tieScore++;
                                    scoreTie.setText(Integer.toString(tieScore));
                                    disableBoard();
                                    turnPrompt.setText("Tie Game!");
                                    backButton.setVisible(true);
                                }
                            });
                        }
                        else if(serverMsg instanceof OpponentLeftGameMsg)
                        {
                            OpponentLeftGameMsg leftMsg = (OpponentLeftGameMsg) serverMsg;

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if(leftMsg.getUserWhoLeft().getUsername().equals(player2Name.getText()))
                                    {
                                        player1Score++;
                                        scoreP1.setText(Integer.toString(player1Score));
                                        turnPrompt.setText(player1Name.getText() + " WON!");
                                    }
                                    else
                                    {
                                        player2Score++;
                                        scoreP2.setText(Integer.toString(player2Score));
                                        turnPrompt.setText(player2Name.getText() + " WON!");
                                    }

                                    gameBoard.setDisable(true);
                                    backButton.setVisible(true);

                                    //display a message
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "The opponent has left the game.");
                                    alert.setTitle("Opponent Left");
                                    alert.setHeaderText("Oops");
                                    Optional<ButtonType> buttonResult = alert.showAndWait();
                                }
                            });

                        }

                        System.out.println("message processed in game controller listener");
                    }
                }
                catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("thread in game controller is stopped.");
        }
    }


    private Pair<Integer, Integer> getCoord(String button)
    {
        if(button.equals("b1"))
            return new Pair<>(0,0);
        else if(button.equals("b2"))
            return new Pair<>(1,0);
        else if(button.equals("b3"))
            return new Pair<>(2,0);
        else if(button.equals("b4"))
            return new Pair<>(0,1);
        else if(button.equals("b5"))
            return new Pair<>(1,1);
        else if(button.equals("b6"))
            return new Pair<>(2,1);
        else if(button.equals("b7"))
            return new Pair<>(0,2);
        else if(button.equals("b8"))
            return new Pair<>(1,2);
        else if(button.equals("b9"))
            return new Pair<>(2, 2);
        return null;
    }

    private String getButton(Pair<Integer, Integer> pair) {
        Pair<Integer,Integer> p1 = new Pair<>(0,0);
        Pair<Integer,Integer> p2 = new Pair<>(1,0);
        Pair<Integer,Integer> p3 = new Pair<>(2,0);
        Pair<Integer,Integer> p4 = new Pair<>(0,1);
        Pair<Integer,Integer> p5 = new Pair<>(1,1);
        Pair<Integer,Integer> p6 = new Pair<>(2,1);
        Pair<Integer,Integer> p7 = new Pair<>(0,2);
        Pair<Integer,Integer> p8 = new Pair<>(1,2);
        Pair<Integer,Integer> p9 = new Pair<>(2,2);

        if(pair.equals(p1)) {
            return "b1";
        }
        else if (pair.equals(p2)) {
            return "b2";
        }
        else if (pair.equals(p3)) {
            return "b3";
        }
        else if (pair.equals(p4)) {
            return "b4";
        }
        else if (pair.equals(p5)) {
            return "b5";
        }
        else if (pair.equals(p6)) {
            return "b6";
        }
        else if (pair.equals(p7)) {
            return "b7";
        }
        else if (pair.equals(p8)) {
            return "b8";
        }
        else if (pair.equals(p9)) {
            return "b9";
        }
        return "ERROR";
    }

    private void paintButton(String buttonLabel, String symbol)
    {
        if(b1.getId().equals(buttonLabel))
        {
            b1.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b1.setTextFill(symbolColor);
        }
        else if(b2.getId().equals(buttonLabel))
        {
            b2.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b2.setTextFill(symbolColor);
        }
        else if(b3.getId().equals(buttonLabel))
        {
            b3.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b3.setTextFill(symbolColor);
        }
        else if(b4.getId().equals(buttonLabel))
        {
            b4.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b4.setTextFill(symbolColor);
        }
        else if(b5.getId().equals(buttonLabel))
        {
            b5.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b5.setTextFill(symbolColor);
        }
        else if(b6.getId().equals(buttonLabel))
        {
            b6.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b6.setTextFill(symbolColor);
        }
        else if(b7.getId().equals(buttonLabel))
        {
            b7.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b7.setTextFill(symbolColor);
        }
        else if(b8.getId().equals(buttonLabel))
        {
            b8.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b8.setTextFill(symbolColor);
        }
        else if(b9.getId().equals(buttonLabel))
        {
            b9.setText(symbol);
            Color symbolColor = checkSymbol(symbol);
            b9.setTextFill(symbolColor);
        }
    }

    private Color checkSymbol(String symbol) {
        if(symbol.equalsIgnoreCase("O"))
        {
            return Color.DODGERBLUE;
        }
        else
        {
            return Color.DARKKHAKI;
        }
    }


    public static int stringToInt(String symbol)
    {
        //for translating GUI Grid to a minimax gameboard
        if(symbol == "O")
            return 1;
        else if(symbol == "X")
            return -1;
        else
            return 0;
    }


    public void initializeName(String p1, String p2)
    {
        //scoreboard
        scoreP1.setText(Integer.toString(player1Score));
        scoreP2.setText(Integer.toString(player2Score));
        scoreTie.setText(Integer.toString(tieScore));

        scoreBoardP1.setText(p1);
        scoreBoardP2.setText(p2);
        player1Name.setText(p1);
        player2Name.setText(p2);

        backButton.getStyleClass().removeAll();
        resetButton.getStyleClass().removeAll();

        p1TurnPrompt = p1 + "'s Turn!";
        p2TurnPrompt = p2 + "'s Turn!";
    }


    @FXML
    void backButtonClicked(ActionEvent event) throws IOException
    {
        //stop the controller's listener
        Global.toServer.writeObject(new KillListenerMsg("from game controller"));
        Global.toServer.flush();

        while(listener.thread.isAlive()) //wait for listener thread to shutdown
        {
            Parent mainMenuWindow = FXMLLoader.load(getClass().getResource("/app/mainMenuWindow.fxml"));
            Scene mainMenuScene = new Scene(mainMenuWindow);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(mainMenuScene);
            window.show();
            resetScore();
        }
    }

    @FXML
    void resetButtonClicked(ActionEvent event) throws IOException {
        gameBoard.setDisable(false);
        ObservableList<Node> buttons =
                gameBoard.getChildren();

        buttons.forEach(btn -> {
            ((Button) btn).setText("");
            stopFadeTransition((Button) btn);
            btn.getStyleClass().remove("winning-square");
            btn.getStyleClass().remove("tie");
            turnPrompt.setText(p1TurnPrompt);
            turnNumber = 1;
            isFirstPlayer = true;
        });
    }


    /*********************************************************************************************/


    public void resetScore()
    {
        player1Score = 0;
        player2Score = 0;
        tieScore     = 0;

        scoreP1.setText(Integer.toString(player1Score));
        scoreP2.setText(Integer.toString(player2Score));
        scoreTie.setText(Integer.toString(tieScore));
    }


    public void setBoard()
    {
        ObservableList<Node> buttons = gameBoard.getChildren();

        buttons.forEach(btn -> {
            btn.getStyleClass().add("boardButton");
        });
    }

    public void disableBoard()
    {
        gameBoard.setDisable(true);
        ObservableList<Node> buttons = gameBoard.getChildren();

        buttons.forEach(btn -> {
            btn.getStyleClass().add("tie");
            turnPrompt.setText(p1TurnPrompt);
            isFirstPlayer = true;
        });
    }

    private void applyFadeTransition(Button winningButton) {

        FadeTransition ft = new FadeTransition(Duration.millis(150), winningButton);

        ft.setFromValue(1.0);
        ft.setToValue(0.1);
        ft.setCycleCount(10);
        ft.setAutoReverse(true);
        ft.play();
    }

    private void stopFadeTransition(Button winningButton) {

        FadeTransition ft = new FadeTransition(Duration.millis(0));

        ft.setFromValue(0);
        ft.setToValue(0);
        ft.setCycleCount(0);
        ft.setAutoReverse(false);
        ft.play();
    }

    private void highlightWinningCombo(Button first, Button second, Button third) {
        first.getStyleClass().add("winning-square");
        second.getStyleClass().add("winning-square");
        third.getStyleClass().add("winning-square");

        applyFadeTransition(first);
        applyFadeTransition(second);
        applyFadeTransition(third);
    }

    private boolean find3InARow() {
        ArrayList<Button> winners = new ArrayList<>();

        //Row 1
        if ("" != b1.getText() && b1.getText() == b2.getText()
                && b2.getText() == b3.getText()) {
            disableBoard();
            highlightWinningCombo(b1, b2, b3);
//            winners.add(b1);
//            winners.add(b2);
//            winners.add(b3);
            return true;

        }
        //Row 2
        if ("" != b4.getText() && b4.getText() == b5.getText()
                && b5.getText() == b6.getText()) {
            disableBoard();
            highlightWinningCombo(b4, b5, b6);
//            winners.add(b4);
//            winners.add(b5);
//            winners.add(b6);
            return true;
        }
        //Row 3
        if ("" != b7.getText() && b7.getText() == b8.getText()
                && b8.getText() == b9.getText()) {
            disableBoard();
            highlightWinningCombo(b7, b8, b9);
//            winners.add(b7);
//            winners.add(b8);
//            winners.add(b9);
            return true;
        }
        //Column 1
        if ("" != b1.getText() && b1.getText() == b4.getText()
                && b4.getText() == b7.getText()) {
            disableBoard();
            highlightWinningCombo(b1, b4, b7);
//            winners.add(b1);
//            winners.add(b4);
//            winners.add(b7);
            return true;
        }
        //Column 2
        if ("" != b2.getText() && b2.getText() == b5.getText()
                && b5.getText() == b8.getText()) {
            disableBoard();
            highlightWinningCombo(b2, b5, b8);
//            winners.add(b2);
//            winners.add(b5);
//            winners.add(b8);
            return true;
        }
        //Column 3
        if ("" != b3.getText() && b3.getText() == b6.getText()
                && b6.getText() == b9.getText()) {
            disableBoard();
            highlightWinningCombo(b3, b6, b9);
//            winners.add(b3);
//            winners.add(b6);
//            winners.add(b9);
            return true;
        }
        //Diagonal 1
        if ("" != b1.getText() && b1.getText() == b5.getText()
                && b5.getText() == b9.getText()) {
            disableBoard();
            highlightWinningCombo(b1, b5, b9);
//            winners.add(b1);
//            winners.add(b5);
//            winners.add(b9);
            return true;
        }
        //Diagonal 2
        if ("" != b3.getText() && b3.getText() == b5.getText()
                && b5.getText() == b7.getText()) {
            disableBoard();
            highlightWinningCombo(b3, b5, b7);
//            winners.add(b3);
//            winners.add(b5);
//            winners.add(b7);
            return true;
        }
        return false;
    }

    private MoveCoord executeComputerMove()
    {
        //translate into 2d board
        int[][] currentBoard = new int[3][3];

        currentBoard[0][0] = stringToInt(b1.getText());
        currentBoard[0][1] = stringToInt(b2.getText());
        currentBoard[0][2] = stringToInt(b3.getText());
        currentBoard[1][0] = stringToInt(b4.getText());
        currentBoard[1][1] = stringToInt(b5.getText());
        currentBoard[1][2] = stringToInt(b6.getText());
        currentBoard[2][0] = stringToInt(b7.getText());
        currentBoard[2][1] = stringToInt(b8.getText());
        currentBoard[2][2] = stringToInt(b9.getText());

        try
        {
            MoveCoord computerMove = MinimaxMove.createComputerMove(currentBoard);

            if(computerMove.row == 0 && computerMove.col == 0)
            {
                b1.setTextFill(Color.DARKKHAKI);
                b1.setText("X");
            }
            else if(computerMove.row == 0 && computerMove.col == 1)
            {
                b2.setTextFill(Color.DARKKHAKI);
                b2.setText("X");
            }
            else if(computerMove.row == 0 && computerMove.col == 2)
            {
                b3.setTextFill(Color.DARKKHAKI);
                b3.setText("X");
            }
            else if(computerMove.row == 1 && computerMove.col == 0)
            {
                b4.setTextFill(Color.DARKKHAKI);
                b4.setText("X");
            }
            else if(computerMove.row == 1 && computerMove.col == 1)
            {
                b5.setTextFill(Color.DARKKHAKI);
                b5.setText("X");
            }
            else if(computerMove.row == 1 && computerMove.col == 2)
            {
                b6.setTextFill(Color.DARKKHAKI);
                b6.setText("X");
            }
            else if(computerMove.row == 2 && computerMove.col == 0)
            {
                b7.setTextFill(Color.DARKKHAKI);
                b7.setText("X");
            }
            else if(computerMove.row == 2 && computerMove.col == 1)
            {
                b8.setTextFill(Color.DARKKHAKI);
                b8.setText("X");
            }
            else if(computerMove.row == 2 && computerMove.col == 2)
            {
                b9.setTextFill(Color.DARKKHAKI);
                b9.setText("X");
            }

            return computerMove;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

}