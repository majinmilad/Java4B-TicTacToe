package modules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public class Move extends BaseModel
{
    private String gameId;
    private String playerId;
    private int Xcoord;
    private int Ycoord;
    private String time;

    Move()
    {

    }

    public Move(String gameId, String playerId, int X, int Y)
    {
        setId(playerId);
        setGameId(gameId);
        setXcoord(X);
        setYcoord(Y);
        setInitialTime();
    }

    public Move(String gameId, String playerId, int X, int Y, String time)
    {
        setId(playerId);
        setGameId(gameId);
        setXcoord(X);
        setYcoord(Y);
        setRetrievedTime(time);
    }


    public void setInitialTime()
    {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time  = dateFormat.format(date);
    }


    public String getGameId() {
        return gameId;
    }

    public String getPlayerId() { return playerId; }

    public int getXcoord() {
        return Xcoord;
    }

    public int getYcoord() {
        return Ycoord;
    }

    public String getTime()
    {
        return time;
    }

    public void setRetrievedTime(String time)
    {
        this.time = time;
    }

    public void setYcoord(int ycoord) {
        Ycoord = ycoord;
    }

    public void setXcoord(int xcoord) {
        Xcoord = xcoord;
    }

    public void setGameId(String id) {this.gameId = id;}
}
