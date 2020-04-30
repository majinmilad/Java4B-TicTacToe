package modules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Game extends BaseModel {
//    private int  gameId;
    private String startTime;
    private String endTime;
    private String  p1Id;
    private String p2Id;
    private String starterId;
    private String winnerId;

    Game(String p1) {
        startTime = "";
        endTime   = "";
        setP1Id(p1);
        setStarterId(p1);

    }

    public Game(String gameId, String sTime, String eTime, String p1, String p2, String starter, String winner)
    {
        setId(gameId);
        recallTimes(sTime,eTime);
        setP1Id(p1);
        setP2Id(p2);
        setStarterId(starter);
        setWinnerId(winner);
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime() {
        Date ended = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        endTime = dateFormat.format(ended);
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime() {
        Date started = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startTime = dateFormat.format(started);
    }

    public String getP1Id() {
        return p1Id;
    }

    public void setP1Id(String p1Id) {
        this.p1Id = p1Id;
    }

    public String getP2Id() {
        return p2Id;
    }

    public void setP2Id(String p2Id) {
        this.p2Id = p2Id;
    }

    public String getStarterId() {
        return starterId;
    }

    public void setStarterId(String starterId) {
        this.starterId = starterId;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public void recallTimes(String sTime, String eTime)
    {
        startTime = sTime;
        endTime   = eTime;
    }


}
