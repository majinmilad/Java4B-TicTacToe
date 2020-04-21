package Messages;

import modules.User;

public class LogoutMsg extends Message
{
    User user;

    public LogoutMsg(User user)
    {
        super("LogoutMsg", user.getUsername());
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}