package modules;

import java.util.ArrayList;
import java.util.List;

public class GameViewers extends BaseModel {
    private int gameId;
    private List<User> viewers = new ArrayList<>();

    GameViewers(List<User> viewers)
    {
        this.viewers = List.copyOf(viewers);
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
