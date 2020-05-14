package Messages;

import modules.GameInfo;
import modules.Move;
import modules.User;

public class MoveMadeMsg extends Message
{
    private final User moveMadeByUser;
    private final Move move;

    public MoveMadeMsg(Move m, User from)
    {
        super("MoveMadeMsg", "");
        moveMadeByUser = from;
        move = m;
    }

    public User getMoveMadeByUser() {
        return moveMadeByUser;
    }

    public Move getMove() {
        return move;
    }
}
