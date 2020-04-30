package Messages;

import modules.User;

public class NewGameMsg extends Message
{
    private final User player1; //game creator
    private final String gameType;


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
