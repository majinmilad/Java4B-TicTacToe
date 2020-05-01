package Messages;

import modules.Game;

public class GameEndedMsg extends Message {

    private Game thisGame;

    public GameEndedMsg(Game gameOver)
    {
        super("GameEndedMsg", "");
        this.thisGame = gameOver;
    }

    public Game getGameOver()
    {
        return thisGame;
    }

}
