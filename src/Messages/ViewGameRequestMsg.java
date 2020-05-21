package Messages;

import modules.GameInfo;
import modules.User;

public class ViewGameRequestMsg extends Message
{
    private final User requestingUser;
    private final GameInfo gameInfo;

    public ViewGameRequestMsg(GameInfo g, User from)
    {
        super("ViewGameRequestMsg", "");
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
