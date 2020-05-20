package TicTacToe;

import java.util.ArrayList;
import java.util.Scanner;

class MoveCoord {
    int row;
    int col;
}

public class MinimaxMove
{

    private static int[][] gameBoard = new int[3][3];

    private static final int X = 1;
    private static final int O = -1;

    public static MoveCoord createComputerMove(int[][] inputBoard) throws Exception
    {
        return makeComputerMove(inputBoard);
    }



    /******************************************************************************/



    private static MoveCoord makeComputerMove(int[][] gameBoard) throws Exception
    {
        if(!noMoreMoves(gameBoard))
        {
            int minVal = 100000;
            int moveVal;

            int rowMoveToMake = -1;
            int colMoveToMake = -1;

            for(int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++)
                {
                    if(gameBoard[row][col] == 0) //empty
                    {
                        gameBoard[row][col] = O; //set

                        moveVal = minimax(gameBoard, true); //evaluate move

                        gameBoard[row][col] = 0; //unset

                        if(moveVal < minVal) //update best move
                        {
                            rowMoveToMake = row;
                            colMoveToMake = col;
                            minVal = moveVal;
                        }
                    }
                }
            }

            //set and return official move
            MoveCoord officialMove = new MoveCoord();
            officialMove.row = rowMoveToMake;
            officialMove.col = colMoveToMake;

            return officialMove;
        }
        else
            throw new Exception("No room on board to make move");
    }


    private static int minimax(int[][] gameBoard, boolean maxPlayer)
    {
        //checking for base case
        int gameOverState;

        gameOverState = whoIsWinner(gameBoard); //1 if X won, -1 if O won, 0 means possible draw

        if(gameOverState == X)
            return 10;
        else if(gameOverState == O)
            return -10;
        else if(noMoreMoves(gameBoard))
            return 0; //draw

        //continue exploring children
        if(maxPlayer)
        {
            int maxEval = -100000;

            //GENERATE CHILDREN ARRAY
            ArrayList<int[][]> children = generateChildren(gameBoard, true);

            for(int[][] child : children)
            {
                int eval = minimax(child, false);
                maxEval = maximum(eval, maxEval);
            }

            return maxEval;
        }
        else
        {
            int minEval = +100000;

            //GENERATE CHILDREN ARRAY
            ArrayList<int[][]> children = generateChildren(gameBoard, false);

            for(int[][] child : children)
            {
                int eval = minimax(child, true);
                minEval = minimum(eval, minEval);
            }

            return minEval;
        }
    }



    /******************************************************************************/



    private static int whoIsWinner(int[][] gameBoard)
    {
        //this is a static evaluation function. it determines if someone
        //has definitively won yet. returns 1 or -1 as indicator of who
        //has won and 0 if there is no winner or a possible draw

        //check rows
        for(int row = 0; row < 3; row++)
        {
            if(gameBoard[row][0] == gameBoard[row][1] && gameBoard[row][1] == gameBoard[row][2])
            {
                if(gameBoard[row][0] == X)
                    return X;
                else if(gameBoard[row][0] == O)
                    return O;
            }
        }

        //check col
        for(int col = 0; col < 3; col++)
        {
            if(gameBoard[0][col] == gameBoard[1][col] && gameBoard[1][col] == gameBoard[2][col])
            {
                if(gameBoard[0][col] == X)
                    return X;
                else if(gameBoard[0][col] == O)
                    return O;
            }
        }

        //check diagonal
        if(gameBoard[0][0] == gameBoard[1][1]
                && gameBoard[1][1] == gameBoard[2][2])
        {
            if(gameBoard[0][0] == X)
                return X;
            else if(gameBoard[0][0] == O)
                return O;
        }

        if(gameBoard[0][2] == gameBoard[1][1]
                && gameBoard[1][1] == gameBoard[2][0])
        {
            if(gameBoard[0][2] == X)
                return X;
            else if(gameBoard[0][2] == O)
                return O;
        }

        return 0; //no definitive winner
    }


    private static boolean noMoreMoves(int[][] gameBoard)
    {
        for (int row = 0; row < 3; row++) {
            if(gameBoard[row][0] == 0 || gameBoard[row][1] == 0 || gameBoard[row][2] == 0)
                return false;
        }

        return true;
    }


    private static ArrayList<int[][]> generateChildren(int[][] gameBoard, boolean maxPlayer)
    {
        int[][] altBoard; //used as a copy of board versions

        int symbol;

        if(maxPlayer)
            symbol = X;
        else
            symbol = O;

        ArrayList<int[][]> children = new ArrayList<>(); //array of all children to be returned

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if(gameBoard[row][col] == 0) //available position
                {
                    altBoard = clone2D(gameBoard); //clone original
                    altBoard[row][col] = symbol; //set move
                    children.add(altBoard); //push child to array
                }
            }
        }

        return children;
    }


    private static int maximum(int num1, int num2) {
        if(num1 > num2)
            return num1;
        else
            return num2;
    }


    private static int minimum(int num1, int num2) {
        if(num1 < num2)
            return num1;
        else
            return num2;
    }


    private static int[][] clone2D(int[][] twoDimArray)
    {
        if(twoDimArray == null)
            return null;

        int[][] copy = twoDimArray.clone();

        for (int row = 0; row < twoDimArray.length; row++) {
            copy[row] = twoDimArray[row].clone();
        }

        return copy;
    }


    private static void printBoard(int[][] gameBoard)
    {
        for(int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if(gameBoard[row][col] == 0)
                    System.out.print("-" + "   ");
                else if(gameBoard[row][col] == 1)
                    System.out.print("X" + "   ");
                else if(gameBoard[row][col] == -1)
                    System.out.print("O" + "   ");
            }
            System.out.println();
        }
    }


    private static void playGameInConsole(int[][] gameBoard) throws Exception
    {
        Scanner input = new Scanner(System.in);

        int playerRow;
        int playerCol;

        printBoard(gameBoard);

        while (whoIsWinner(gameBoard) == 0 && !noMoreMoves(gameBoard))
        {
            System.out.print("Make move player... ");
            playerRow = input.nextInt();
            playerCol = input.nextInt();

            gameBoard[playerRow][playerCol] = X;

            //computer move
            if(whoIsWinner(gameBoard) == 0)
                makeComputerMove(gameBoard);

            System.out.println();
            printBoard(gameBoard);
        }

        System.out.println();

        if(whoIsWinner(gameBoard) == 1)
            System.out.println("Player X wins!!!");
        else if(whoIsWinner(gameBoard) == -1)
            System.out.println("Player O wins!!!");
        else
            System.out.println("It's a draw \"/");
    }


    private static void testPrinterOfAllChildren(int[][] gameBoard, boolean maxPlayer)
    {
        //FOR TEST PRINTING ALL CURRENT CHILDREN

        ArrayList<int[][]> children;

        System.out.println("ORIGINAL");
        printBoard(gameBoard);
        System.out.println("\n");

        System.out.println("CHILDREN ARRAYS:");

        children = generateChildren(gameBoard, maxPlayer);

        for (int[][] child : children) {
            printBoard(child);
            System.out.println("\n");
        }

        System.out.println("ORIGINAL");
        printBoard(gameBoard);
    }

    //end of class
}
