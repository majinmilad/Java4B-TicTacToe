package sqlite;

import modules.*;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class DatabaseManager implements DataSource {
    private static DatabaseManager instance = null;
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }


    private DatabaseManager() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Phillip\\Documents\\GitHub\\Java4B-TicTacToe\\Database\\TicTacToeDB.db");
            // Milad: C:\Users\chabo\Documents\GitHub\Java 4B Repos\Tic-Tac-Toe repos\Java4B-TicTacToe\Database\TicTacToeDB.db
            // Phill: Database/TicTacToeDB.db
            // Kenny:

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    public static DatabaseManager getInstance()
    {
        if (instance == null)
        {
            synchronized (DatabaseManager.class)
            {
                if (instance == null)
                {
                    try {
                        instance = new DatabaseManager();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Database Connection SUCCESSFUL\n");

                    return instance;

                }
            }
        }

        return instance;
    }


    @Override
    public BaseModel insert(BaseModel obj)
    {
        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("INSERT INTO ");

        if(obj instanceof User)
        {
            User u = (User) obj;

            qryBuilder.append("User (fName,lName,password,dateCreated,userName,status,UUID) " +
                    "VALUES (\'" +  u.getFirstName() + "\', \'" + u.getLastName() + "\', \'" + u.getPassword() + "\', \'" +
                    u.getCreation() + "\', \'" + u.getUsername() + "\', \'" + u.getStatus() + "\', \'" + u.getUserID() + "\')" );
        }
        else if(obj instanceof Game)
        {
            Game g = (Game) obj;

            qryBuilder.append("Game (p1Id, p2Id, creatorId, gameStatus, UUID) " +
                    "VALUES (\'" + g.getP1Id() + "\', \'" + g.getP2Id() + "\', \'" + g.getCreatorId() + "\', \'WAITING\', \'" + g.getGameId() + "\')");
        }
        else if(obj instanceof Move)
        {
            Move m = (Move) obj;

            qryBuilder.append("Moves (gameId,playerId,X_coord,Y_coord,time) " +
                    "VALUES ( " + m.getGameId() +  ", " + m.getId() + ", " + m.getXcoord()
                    + ", " + m.getYcoord() + ", \'" + m.getTime() + "\')" );
        }
        else if(obj instanceof GameViewers)
        {
            GameViewers gv = (GameViewers) obj;

            qryBuilder.append("GameViewers (gameId,viewerId) " +
                    "VALUES (" + gv.getId() + ", " + gv.getPlayerId() + ')');
        }

        try {
            executeInsert(qryBuilder.toString());
            System.out.println("Successful insertion into db\n");
            return obj;
        } catch (SQLException e) {
            System.out.println("Unsuccessful insertion into db");
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public BaseModel update(BaseModel obj)
    {
        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("UPDATE ");

        if(obj instanceof User)
        {
            User u = (User) obj;

            qryBuilder.append("User " +
                    "SET userName = \'" + u.getUsername()  + "\', password = \'" + u.getPassword() + "\', fName = \'"
                    + u.getFirstName() + "\', lName = \'"    + u.getLastName() + "\', status = \'"
                    + u.getStatus()    + "\' "               +
                    "WHERE UUID = \'" + u.getUserID() + "\'");
        }
        else if (obj instanceof Game)
        {
            Game g = (Game) obj;

            qryBuilder.append("Game "+
                    "SET startTime = \'"+ g.getStartTime() + "\',  p2Id = \'" + g.getP2Id() +"\', winnerId = \'" + g.getWinnerId() + "\', " +
                    "endTime = \'" + g.getEndTime() +"\', gameStatus = \'" + g.getStatus() + "\' " + "WHERE UUID = \'" + g.getGameId() + "\'");
        }
        else if(obj instanceof GameViewers)
        {
            GameViewers gv = (GameViewers) obj;

            qryBuilder.append("GameViewers" +
                              "SET viewingStatus = \'" + gv.getStatus() + "\' " +
                              "WHERE gameId == \'" + gv.getId() + "\'");
        }


        try {
            executeUpdate(qryBuilder.toString());
            System.out.println("Successful update to db\n");
            return obj;
        } catch (SQLException e) {
            System.out.println("Unsuccessful update to db");
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public BaseModel delete(BaseModel obj)
    {
        StringBuilder qryBuilder = new StringBuilder();

        if(obj instanceof User)
        {
            User u = (User) obj;

            qryBuilder.append("UPDATE User " +
                    "SET status = 'INACTIVE' " +
                    "WHERE UUID = \'" + u.getUserID() + "\'");
        }
        else if(obj instanceof Game)
        {
            Game g = (Game) obj;

            qryBuilder.append("DELETE FROM Game WHERE UUID = \'" + g.getGameId() + "\'");
        }

        try {
            executeDelete(qryBuilder.toString());
            System.out.println("Successful delete in db\n");
            return obj;
        } catch (SQLException e) {
            System.out.println("Unsuccessful delete in db");
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public BaseModel authenticate(String username, String password)
    {
        String query = "SELECT * "
                +  "FROM User "
                +  "WHERE userName = \'" + username + "\' "
                +  "AND   password = \'" + password + "\'"
                +  "AND   status   != 'INACTIVE'";

        try {
            ResultSet rs = executeQuery(query);

            User u = new User(rs.getString("userName"), rs.getString("password"),
                    rs.getString("fName"), rs.getString("lName"),
                    rs.getString("status"), rs.getString("UUID"),
                    rs.getString("dateCreated"));

            System.out.println("User login authenticated\n");
            return u;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.printf("User login not authenticated");
            return null;
        }
    }


    @Override
    public BaseModel get(BaseModel obj)
    {
        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("SELECT *");

        if (obj instanceof User)
        {
            User u = (User) obj;
            qryBuilder.append("FROM User " + "WHERE userName = \'" + u.getUsername() + "\'");

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());
                rs.next();

                User user = new User(rs.getString("userName"), rs.getString("password"),
                        rs.getString("fName"), rs.getString("lName"),
                        rs.getString("status"), rs.getString("UUID"),
                        rs.getString("dateCreated"));

                return user;
            } catch (SQLException e) {
                System.out.println("exception in get()");
                e.printStackTrace();
            }
        }
        else if (obj instanceof Game)
        {
//            Game g = (Game) obj;
//            qryBuilder.append("FROM Game " + "WHERE id = \'" + g.getId() + "\'" );
//
//            try {
//                ResultSet rs = executeQuery(qryBuilder.toString());
//
//                rs.next();
//                Game game = new Game((rs.getString("gameId")), rs.getString("startTime"),
//                        rs.getString("endTime"), (rs.getString("p1Id")),
//                        (rs.getString("p2Id")), (rs.getString("starterId")),
//                        (rs.getString("winnerId")));
//
//                return game;
//            } catch (SQLException e) {
//                System.out.println("exception in getUser()");
//                e.printStackTrace();
//            }
        }

        return null;
    }


    @Override
    public BaseModel query(BaseModel obj, String filter)
    {
        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("SELECT ");

        if(obj instanceof User)
        {
            qryBuilder.append("* FROM User " + filter + " LIMIT 1");

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());
                rs.next();

                User user = new User(rs.getString("userName"), rs.getString("password"),
                        rs.getString("fName"), rs.getString("lName"),
                        rs.getString("status"), rs.getString("UUID"),
                        rs.getString("dateCreated"));

                System.out.println("Successful User query");
                return user;
            } catch (SQLException e) {
                System.out.println("Unsuccessful User query");
                e.printStackTrace();
                return null;
            }
        }
        else if (obj instanceof Game)
        {
            qryBuilder.append("* FROM Game " + filter + " LIMIT 1");

            /*
                    @ To get game history
                    "WHERE p1Id = \'" +  playerID  + "\' "
                   + OR p2Id = \'" +  playerID + "\' "

                    @ To Get Active Games
                    "WHERE endTime != NULL "
                  + "ORDER BY id ASC "

                    @ To Get Completed Games
                    "WHERE endTime = NULL "
                  + "ORDER BY id ASC "

                    @ To See if Player is playing
                     "WHERE p1Id = \'" +  playerID  + "\' "
                   + "OR p2Id = \'" +  playerID + "\' "
                   + "AND endTime != NULL "
             */

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                Game game = new Game(rs.getString("p1Id"), rs.getString("p2Id"),
                        rs.getString("startTime"), rs.getString("endTime"),
                        rs.getString("creatorId"), rs.getString("winnerId"),
                        rs.getString("gameStatus"), rs.getString("UUID"));


                System.out.println("Successful Game query\n");
                return game;
            } catch (SQLException e)
            {
                System.out.println("Unsuccessful Game query");
                e.printStackTrace();
                return null;
            }

        }
        else if(obj instanceof GameViewers)
        {
            GameViewers gv = (GameViewers) obj;
            qryBuilder.append("* From GameViewers " + filter + " LIMIT 1");

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());
                rs.next();

                GameViewers gameviewer = new GameViewers(rs.getString("gameId"), rs.getString("viewerId"),
                        rs.getString("viewingStatus"));

                System.out.println("Successful GameViewer query");

                return gameviewer;

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Unsuccessful User query");
                return null;
            }

        }

        return null; //temp
    }


    @Override
    public List <BaseModel> queryList(BaseModel obj, String filter)
    {
        List <BaseModel> list = new ArrayList<>();

        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("SELECT ");

        if(obj instanceof User)
        {
            qryBuilder.append("* FROM User WHERE userID > 1 " +  filter);

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                while(rs.next())
                {
                    User u = new User(rs.getString("userName"), rs.getString("password"),
                            rs.getString("fName"), rs.getString("lName"),
                            rs.getString("status"), rs.getString("UUID"),
                            rs.getString("dateCreated"));
                    list.add(u);
                }

                System.out.println("Successful User list query\n");
            } catch (SQLException e) {
                System.out.println("Unsuccessful User list query");
                e.printStackTrace();
            }
        }
        else if (obj instanceof Game)
        {
            qryBuilder.append("* FROM Game " + filter);

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                while(rs.next())
                {
                    Game g = new Game(rs.getString("p1Id"), rs.getString("p2Id"),
                            rs.getString("startTime"), rs.getString("endTime"),
                            rs.getString("creatorId"), rs.getString("winnerId"),
                            rs.getString("gameStatus"), rs.getString("UUID"));

                    list.add(g);
                }

                System.out.println("Successful Game list query\n");
            } catch (SQLException e) {
                System.out.println("Unsuccessful Game list query");
                e.printStackTrace();
            }
        }
        else if (obj instanceof Move)
        {
            Move moves = (Move) obj;
            qryBuilder.append("* FROM Moves " + filter );

            try {
                ResultSet   rs = executeQuery(qryBuilder.toString());
                while(rs.next())
                {
                    Move m = new Move(rs.getString("gameId"), rs.getString("playerId"), Integer.parseInt(rs.getString("X_coord")),
                            Integer.parseInt(rs.getString("Y_coord")), rs.getString("time"));
                    list.add(m);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("Got All Moves Queries");
        }
        else if (obj instanceof GameViewers)
        {
            GameViewers gv = (GameViewers) obj;

            qryBuilder.append("DISTINCT * FROM GameViewers " +
                    "WHERE gameId = \'" + gv.getId() + "\' " );

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                while(rs.next())
                {
                    GameViewers gameViewers = new GameViewers(rs.getString("gameId"), rs.getString("viewerId"), rs.getString("gameStatus"));
                    list.add(gameViewers);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("Able to get all GameViewers!!\n\n");

        }

        return list;
    }


    private void executeInsert(String query) throws SQLException {

        PreparedStatement pst = connection.prepareStatement(query);
        pst.execute();
        pst.close();
    }

    private void executeDelete(String query) throws SQLException {

        PreparedStatement pst = connection.prepareStatement(query);
        pst.executeUpdate();

    }

    private void executeUpdate(String query) throws SQLException {

        PreparedStatement pst =  connection.prepareStatement(query);
        pst.executeUpdate();

    }

    private ResultSet executeQuery(String query) throws SQLException {

        PreparedStatement pstmt  =  connection.prepareStatement(query);
        ResultSet rs  = pstmt.executeQuery();
        return rs;

    }
}
