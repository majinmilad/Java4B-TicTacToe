package Messages;

import modules.GameInfo;
import modules.User;

public class BeginViewingGameMsg extends Message
{
    boolean permissionToView;
//    private final User requestingUser;
    private final GameInfo gameInfo;

    public BeginViewingGameMsg(boolean permissionToView, GameInfo g)
    {
        super("BeginViewingGameMsg", "");
        this.permissionToView = permissionToView;
//        requestingUser = from;
        gameInfo = g;
    }

    public boolean getPermissionToView()
    {
        return permissionToView;
    }

//    public User getRequestingUser() {
//        return requestingUser;
//    }
//
    public GameInfo getGameInfo() {
        return gameInfo;
    }
}
