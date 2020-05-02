package Messages;

import modules.GameInfo;

import java.util.List;

public class GameListMsg extends Message
{
    private List<GameInfo> gameInfoList;

    public GameListMsg(List<GameInfo> gameInfoList)
    {
        super("GameListMsg", "");
        this.gameInfoList = gameInfoList;
    }

    public List<GameInfo> getGameList() { return gameInfoList; }
}
