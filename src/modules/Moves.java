package modules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public class Moves  extends BaseModel{
//    private int moveId;
    private int gameId;
    private int playerId;
    private int Xcoord;
    private int Ycoord;
    private String time;


    public void  setTIme()
    {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time  = dateFormat.format(date);
    }

    public String getTime()
    {
        return time;
    }

    public void setYcoord(int ycoord) {
        Ycoord = ycoord;
    }

    public void setXcoord(int xcoord) {
        Xcoord = xcoord;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getXcoord() {
        return Xcoord;
    }

    public int getYcoord() {
        return Ycoord;
    }

    public int getGameId() {
        return gameId;
    }
}
