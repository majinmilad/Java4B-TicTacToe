package Messages;

public class RequestForGamesMsg extends Message
{
    private final String gameStatusFilter;

    public RequestForGamesMsg()
    {
        super("RequestForGamesMsg", "");
        gameStatusFilter = null;
    }

    public RequestForGamesMsg(String filter)
    {
        super("RequestForGamesMsg", "");
        gameStatusFilter = filter;
    }

    public String getGameStatusFilter() { return gameStatusFilter; }
}
