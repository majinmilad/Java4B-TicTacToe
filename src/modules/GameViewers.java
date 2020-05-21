package modules;

import java.util.ArrayList;
import java.util.List;

public class GameViewers extends BaseModel
{
    private String gameId;
    private String playerId;
    private String viewingStatus;

    public GameViewers(String gameId, String playerId, String viewingStatus)
    {
        this.gameId = gameId;
        this.playerId = playerId;
        this.viewingStatus = viewingStatus;
    }

    // Use for querying
    public GameViewers() {

    }

    public GameViewers(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setViewingStatus(String status)
    {
        viewingStatus = status;
    }

    public String getStatus()
    {
        return viewingStatus;
    }

}
