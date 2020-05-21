package Messages;

import javafx.scene.control.Button;
import modules.User;

import java.util.ArrayList;

public class GameWonMsg extends Message
{
    private final String gameId;
    private final User gameWinner; //game creator

    public GameWonMsg(String gameId, User gameWinner)
    {
        super("GameWonMsg", "");

        this.gameId = gameId;
        this.gameWinner = gameWinner;
    }

    public String getGameId() {
        return gameId;
    }

    public User getGameWinner() {
        return gameWinner;
    }


}
