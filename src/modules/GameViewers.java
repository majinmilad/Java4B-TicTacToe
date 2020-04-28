package modules;

import java.util.ArrayList;
import java.util.List;

public class GameViewers extends BaseModel {
    private String playerId;

    public GameViewers(String gameId, String playerId)
    {
        setId(gameId);
        setPlayerId(playerId);
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
