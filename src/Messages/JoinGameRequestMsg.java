package Messages;

import modules.GameInfo;
import modules.User;

public class JoinGameRequestMsg extends Message
{
    private final User requestingUser;
    private final GameInfo gameInfo;

    public JoinGameRequestMsg(GameInfo g, User from)
    {
        super("JoinGameRequestMsg", "");
        requestingUser = from;
        gameInfo = g;
    }

    public User getRequestingUser() {
        return requestingUser;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }
}
