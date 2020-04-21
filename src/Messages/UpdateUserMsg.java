package Messages;

import modules.User;

public class UpdateUserMsg extends Message
{
    User user;

    public UpdateUserMsg(User user)
    {
        super("UpdateUserMsg", user.getUsername());
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
