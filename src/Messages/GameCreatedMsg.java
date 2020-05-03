package Messages;

import modules.Game;
import modules.User;

public class GameCreatedMsg extends Message
{
    private final Game theGame;
    private final String creatorUsername;

    public GameCreatedMsg(Game createdGame, String creatorUsername)
    {
        super("GameCreatedMsg", "");

        theGame = createdGame;
        this.creatorUsername = creatorUsername;
    }

    public Game getGame() {
        return theGame;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }
}
