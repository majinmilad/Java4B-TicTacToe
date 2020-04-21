package Messages;

import modules.User;

public class ReactivateUserMsg extends Message
{
    User user;

    public ReactivateUserMsg(User user)
    {
        super("ReactivateUserMsg", user.getUsername());
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
