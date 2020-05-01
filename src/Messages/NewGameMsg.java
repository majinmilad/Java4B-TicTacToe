package Messages;

import modules.User;

public class NewGameMsg extends Message
{
    private final User player1; //game creator
    private final String gameType;

    //game type options
    // "LOCAL" - player v. player locally
    // "REMOTE" - player v. player remotely
    // "COMPUTER" - player v. computer

    public NewGameMsg(String gameType, User gameCreator)
    {
        super("NewGameMsg", "");

        this.gameType = gameType;
        player1 = gameCreator;
    }

    public User getCreator() {
        return player1;
    }
}
