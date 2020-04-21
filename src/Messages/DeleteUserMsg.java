package Messages;

import modules.User;

public class DeleteUserMsg extends Message
{
    private final User user;

    public DeleteUserMsg(User user)
    {
        super("DeleteUserMsg", "");
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
