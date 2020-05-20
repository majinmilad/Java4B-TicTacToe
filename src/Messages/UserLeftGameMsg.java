package Messages;

import modules.User;

public class UserLeftGameMsg extends Message
{
    private final User user;
    private final String gameId;

    public UserLeftGameMsg(User user, String gameId)
    {
        super("UserLeftGameMsg", "");
        this.user = user;
        this.gameId = gameId;
    }

    public User getUser() { return user; }
    public String getGameId() { return gameId; }
}
