package TicTacToe;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class gameWindowController implements Initializable {

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
    GridPane gameBoard;

    @FXML
    MenuBar boardMenuBar;

    @FXML
    Label scoreBoardP1, scoreBoardP2;

    @FXML
    Label turnPrompt;

    @FXML
    Label scoreP1, scoreP2, scoreTie;

    @FXML
    Text player1Name, player2Name;

    int turn = 1;
    static String p1Turn;
    static String p2Turn;

    static int player1Score = 0;
    static int player2Score = 0;
    static int tieScore     = 0;

    public void initializeName(String p1, String p2)
    {
        scoreP1.setText(Integer.toString(player1Score));
        scoreP2.setText(Integer.toString(player2Score));
        scoreTie.setText(Integer.toString(tieScore));

        scoreBoardP1.setText(p1);
        scoreBoardP2.setText(p2);
        player1Name.setText(p1);
        player2Name.setText(p2);

        p1Turn = p1 + "'s Turn!";
        p2Turn = p2 + "'s Turn!";
    }

    public void resetScore()
    {
        player1Score = 0;
        player2Score = 0;
        tieScore     = 0;

        scoreP1.setText(Integer.toString(player1Score));
        scoreP2.setText(Integer.toString(player2Score));
        scoreTie.setText(Integer.toString(tieScore));
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

        //Row 1
        if ("" != b1.getText() && b1.getText() == b2.getText()
                && b2.getText() == b3.getText()) {
            highlightWinningCombo(b1, b2, b3);
            return true;
        }
        //Row 2
        if ("" != b4.getText() && b4.getText() == b5.getText()
                && b5.getText() == b6.getText()) {
            highlightWinningCombo(b4, b5, b6);
            return true;
        }
        //Row 3
        if ("" != b7.getText() && b7.getText() == b8.getText()
                && b8.getText() == b9.getText()) {
            highlightWinningCombo(b7, b8, b9);
            return true;
        }
        //Column 1
        if ("" != b1.getText() && b1.getText() == b4.getText()
                && b4.getText() == b7.getText()) {
            highlightWinningCombo(b1, b4, b7);
            return true;
        }
        //Column 2
        if ("" != b2.getText() && b2.getText() == b5.getText()
                && b5.getText() == b8.getText()) {
            highlightWinningCombo(b2, b5, b8);
            return true;
        }
        //Column 3
        if ("" != b3.getText() && b3.getText() == b6.getText()
                && b6.getText() == b9.getText()) {
            highlightWinningCombo(b3, b6, b9);
            return true;
        }
        //Diagonal 1
        if ("" != b1.getText() && b1.getText() == b5.getText()
                && b5.getText() == b9.getText()) {
            highlightWinningCombo(b1, b5, b9);
            return true;
        }
        //Diagonal 2
        if ("" != b3.getText() && b3.getText() == b5.getText()
                && b5.getText() == b7.getText()) {
            highlightWinningCombo(b3, b5, b7);
            return true;
        }
        return false;
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

    public void buttonClickHandler(ActionEvent evt)
    {
        String winner;

        Button clickedButton = (Button) evt.getTarget();
        String buttonLabel = clickedButton.getText();

        if ("".equals(buttonLabel) && isFirstPlayer) {
            turnPrompt.setText(p1Turn);
            clickedButton.setText("O");
            clickedButton.setTextFill(Color.DODGERBLUE);
            isFirstPlayer = false;
            turnPrompt.setText(p2Turn);
            turn++;
        } else if ("".equals(buttonLabel) && !isFirstPlayer && !player2Name.getText().equals("Computer")) {
            turnPrompt.setText(p2Turn);
            clickedButton.setText("X");
            clickedButton.setTextFill(Color.DARKKHAKI);
            isFirstPlayer = true;
            turnPrompt.setText(p1Turn);
            turn++;
        }

        if("".equals(buttonLabel) && turn % 2 == 0 && player2Name.getText().equals("Computer")) //computer playing
        {
            turnPrompt.setText(p2Turn);

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

            try{
                Move newMove = MinimaxMove.createComputerMove(currentBoard);

                if(newMove.row == 0 && newMove.col == 0)
                {
                    b1.setTextFill(Color.DARKKHAKI);
                    b1.setText("X");
                }
                else if(newMove.row == 0 && newMove.col == 1)
                {
                    b2.setTextFill(Color.DARKKHAKI);
                    b2.setText("X");
                }
                else if(newMove.row == 0 && newMove.col == 2)
                {
                    b3.setTextFill(Color.DARKKHAKI);
                    b3.setText("X");
                }
                else if(newMove.row == 1 && newMove.col == 0)
                {
                    b4.setTextFill(Color.DARKKHAKI);
                    b4.setText("X");
                }
                else if(newMove.row == 1 && newMove.col == 1)
                {
                    b5.setTextFill(Color.DARKKHAKI);
                    b5.setText("X");
                }
                else if(newMove.row == 1 && newMove.col == 2)
                {
                    b6.setTextFill(Color.DARKKHAKI);
                    b6.setText("X");
                }
                else if(newMove.row == 2 && newMove.col == 0)
                {
                    b7.setTextFill(Color.DARKKHAKI);
                    b7.setText("X");
                }
                else if(newMove.row == 2 && newMove.col == 1)
                {
                    b8.setTextFill(Color.DARKKHAKI);
                    b8.setText("X");
                }
                else if(newMove.row == 2 && newMove.col == 2)
                {
                    b9.setTextFill(Color.DARKKHAKI);
                    b9.setText("X");
                }

                isFirstPlayer = true;
                turnPrompt.setText(p1Turn);
                turn++;
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }

        //check for win
        boolean result;
        result = find3InARow();

        if (result == true) {
            if(!isFirstPlayer)
            {
                winner = player1Name.getText() + ' ';
                player1Score++;
                scoreP1.setText(Integer.toString(player1Score));
            }
            else
            {
                winner = player2Name.getText() + ' ';
                player2Score++;
                scoreP2.setText(Integer.toString(player2Score));
            }
            turnPrompt.setText(winner + " WON!");
            gameBoard.setDisable(true);

        }
        else if(gameBoard.isDisable() == false && turn > 9)
        {
            turnPrompt.setText("Tie Game!");
            tieScore++;
            scoreTie.setText(Integer.toString(tieScore));
            gameBoard.setDisable(true);
            turn = 1;
            isFirstPlayer = true;
        }
    }


    public void menuClickHandler(ActionEvent evt) {

        MenuItem clickedMenu = (MenuItem) evt.getTarget();
        String menuLabel = clickedMenu.getText();

        if ("Reset".equals(menuLabel)) {
            gameBoard.setDisable(false);
            ObservableList<Node> buttons =
                    gameBoard.getChildren();

            buttons.forEach(btn -> {
                ((Button) btn).setText("");
                stopFadeTransition((Button) btn);
                btn.getStyleClass().remove("winning-square");
                turnPrompt.setText(p1Turn);
                turn = 1;
                isFirstPlayer = true;
            });

        }

    }


    public void returnMainMenu(ActionEvent event) throws IOException {
        Parent menuParent = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene menuScene = new Scene(menuParent);

        Stage menuWindow = (Stage) boardMenuBar.getScene().getWindow();
        menuWindow.setResizable(false);
        menuWindow.setScene(menuScene);
        menuWindow.show();

        resetScore();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}










