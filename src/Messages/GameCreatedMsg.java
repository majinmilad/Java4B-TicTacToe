package Messages;

import modules.Game;
import modules.User;

public class GameCreatedMsg extends Message
{
    private final Game theGame;
    private final String creatorUsername;
//    private final User player1; //game creator
//    private final String gameType;

    //game type options
    // "LOCAL" - player v. player locally
    // "REMOTE" - player v. player remotely
    // "COMPUTER" - player v. computer

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
