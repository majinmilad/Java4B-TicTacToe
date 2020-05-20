package Messages;

import modules.User;

public class OpponentLeftGameMsg extends Message
{
    private final User userWhoLeft;
    private final String gameId;

    public OpponentLeftGameMsg()
    {
        super("OpponentLeftGameMsg", "");
        this.userWhoLeft = null;
        this.gameId = null;
    }

    public OpponentLeftGameMsg(User user, String gameId)
    {
        super("OpponentLeftGameMsg", "");
        this.userWhoLeft = user;
        this.gameId = gameId;
    }

    public User getUserWhoLeft() { return userWhoLeft; }
    public String getGameId() { return gameId; }
}
