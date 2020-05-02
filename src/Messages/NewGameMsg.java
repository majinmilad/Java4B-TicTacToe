package Messages;

import modules.User;

public class NewGameMsg extends Message
{
    private final User player1; //game creator
    private final String player2Id; //null for PvP or 1 for PvC

    public NewGameMsg(User gameCreator, String player2Id)
    {
        super("NewGameMsg", "");

        player1 = gameCreator;
        this.player2Id = player2Id;
    }

    public User getCreator() {
        return player1;
    }
    public String getPlayer2Id() { return player2Id; }
}
