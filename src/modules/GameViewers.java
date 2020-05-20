package modules;

import java.util.ArrayList;
import java.util.List;

public class GameViewers extends BaseModel {
    private String playerId;
    private String viewingStatus;

    public GameViewers(String gameId, String playerId, String viewingStatus)
    {
        setId(gameId);
        setPlayerId(playerId);
        this.viewingStatus = viewingStatus;
    }

    // Use for querying
    public GameViewers() {

    }

    public String getPlayerId() {
        return playerId;
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
