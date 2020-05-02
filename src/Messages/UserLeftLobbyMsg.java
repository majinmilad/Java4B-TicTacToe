package Messages;

import modules.User;

public class UserLeftLobbyMsg extends Message
{
    private final User user;

    public UserLeftLobbyMsg(User user)
    {
        super("UserLeftLobbyMsg", "");
        this.user = user;
    }

    public User getUser() { return user; }
}
