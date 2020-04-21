package Messages;

import modules.User;

public class RegistrationMsg extends Message
{
    User user;

    public RegistrationMsg(User user)
    {
        super("RegistrationMsg", user.getUsername());
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
