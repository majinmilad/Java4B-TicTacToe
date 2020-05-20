package Messages;

import modules.User;

public class GameTiedMsg extends Message
{
    private final String gameId;
    private final User msgSender; //game creator

    public GameTiedMsg(String gameId, User msgSender)
    {
        super("GameTiedMsg", "");

        this.gameId = gameId;
        this.msgSender = msgSender;
    }

    public String getGameId() {
        return gameId;
    }

    public User getMsgSender() {
        return msgSender;
    }
}
