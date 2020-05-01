package modules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Game extends BaseModel
{
    private String p1Id;
    private String p2Id;
    private  String startTime;
    private String endTime;
    private String creatorId;
    private String winnerId;
    private final String gameId;
    private String status;

    //create a brand new game
    public Game(User player1)
    {
        p1Id = player1.getUserID();
        creatorId = player1.getUserID();
        startTime = null;
        gameId = UUID.randomUUID().toString();
        status = "WAITING";
    }

    // Create a new game w/ Computer or Local
    public Game(User player1, String player2)
    {
        p1Id = player1.getUserID();
        p2Id = player2;
        creatorId = player1.getUserID();
        startTime = generateTime();
        gameId = UUID.randomUUID().toString();
        status = "RUNNING";
    }

    //create a game and set all attributes
    public Game(String p1Id, String p2Id, String sTime, String eTime, String gameCreatorId, String winnerId, String gameId)
    {
        this.p1Id = p1Id;
        this.p2Id = p2Id;
        startTime = sTime;
        endTime = eTime;
        creatorId = gameCreatorId;
        this.winnerId = winnerId;
        this.gameId = gameId;
    }

    // getters
    public String getP1Id() {
        return p1Id;
    }

    public String getP2Id() {
        return p2Id;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getStatus(){ return status;}

    // setters
    public void setP1Id(String p1Id) {
        this.p1Id = p1Id;
    }

    public void setP2Id(String p2Id) {
        this.p2Id = p2Id;
    }

    private void setEndTime() { endTime = generateTime(); }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    // helper methods
    public String generateTime() {
        Date ended = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(ended);
    }

    public void displayAll()
    {
        System.out.println(p1Id);
        System.out.println(p2Id);
        System.out.println(startTime);
        System.out.println(endTime);
        System.out.println(creatorId);
        System.out.println(winnerId);
        System.out.println(gameId);
    }
}
