package Messages;

import modules.GameInfo;
import modules.User;

public class GameStartingMsg extends Message
{
    private final User createdGamePlayer;
    private final User joiningGamePlayer;
    private final GameInfo gameInfo;

    public GameStartingMsg(GameInfo g, User createdGame, User joiningGame)
    {
        super("GameStartingMsg", "");
        createdGamePlayer = createdGame;
        joiningGamePlayer = joiningGame;
        gameInfo = g;
    }

    public User getCreatedGamePlayer() {
        return createdGamePlayer;
    }

    public User getJoiningGamePlayer() {
        return joiningGamePlayer;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }
}
