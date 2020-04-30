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
            this.connection = DriverManager.getConnection("jdbc:sqlite:Database/TicTacToeDB.db");
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
    public BaseModel insert(BaseModel obj) {
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

            qryBuilder.append("Game (id,p1Id,starterId) " +
                    "VALUES (" + g.getId() + ", " + g.getP1Id() + ", " + g.getStarterId() + ')');
        }
        else if(obj instanceof Moves)
        {
            Moves m = (Moves) obj;

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
            System.out.println("Insertion worked\n\n");
            return obj;
        } catch (SQLException e) {
            System.out.println("error executing insert\n\n");
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public BaseModel delete(BaseModel obj) {
        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("UPDATE ");

        if(obj instanceof User)
        {
            User u = (User) obj;

            qryBuilder.append("User " +
                    "SET status = 'INACTIVE' " +
                    "WHERE UUID = \'" + u.getUserID() + "\'");
        }

        try
        {
            executeDelete(qryBuilder.toString());
            System.out.println("DELETED SUCCESS\n\n");
            return obj;
        } catch (SQLException e) {
            System.out.println("DELETED FAIL\n\n");
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public BaseModel update(BaseModel obj) {
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

        try {
            executeUpdate(qryBuilder.toString());
            System.out.println("UPDATE QUERY Successful\n\n");
            return obj;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("UNABLE to update QUERY");
            return null;
        }
    }

    @Override
    public BaseModel get(BaseModel obj) {
        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("SELECT *");

        if (obj instanceof User)
        {
            User u = (User) obj;
            qryBuilder.append("FROM User " + "WHERE userName = \'" + u.getUsername() + "\' ");

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                rs.next();
                User user = new User(rs.getString("userName"), rs.getString("password"), rs.getString("fName"),
                        rs.getString("lName"), rs.getString("status"), rs.getString("UUID"), rs.getString("dateCreated"));

                return user;
            } catch (SQLException e) {
                System.out.println("exception in getUser()");
                e.printStackTrace();
            }

        }
        else if (obj instanceof Game)
        {
            Game g = (Game) obj;
            qryBuilder.append("FROM Game " + "WHERE id = \'" + g.getId() + "\'" );

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                rs.next();
                Game game = new Game((rs.getString("gameId")), rs.getString("startTime"),
                        rs.getString("endTime"), (rs.getString("p1Id")),
                        (rs.getString("p2Id")), (rs.getString("starterId")),
                        (rs.getString("winnerId")));

                return game;
            } catch (SQLException e) {
                System.out.println("exception in getUser()");
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public BaseModel authenticate(String username, String password) {

        String query = "SELECT * "
                +  "FROM User "
                +  "WHERE userName = \'" + username + "\' "
                +  "AND   password = \'" + password + "\'"
                +  "AND   status   != 'INACTIVE'";

        try {
            ResultSet rs = executeQuery(query);
            User u = new User(rs.getString("userName"), rs.getString("password"), rs.getString("fName"), rs.getString("lName"), rs.getString("status"), rs.getString("UUID"), rs.getString("dateCreated"));
            System.out.println("User Authenticated\n\n");
            return u;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.printf("User Not Authenticated\n\n");
            return null;
        }

    }

    @Override
    public List <BaseModel> list(BaseModel obj) {
        List <BaseModel> list = new ArrayList<>();

        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("SELECT * ");

        if(obj instanceof User)
        {


            qryBuilder.append("FROM User "
                    +  "WHERE userID > 1 "
                    +  "AND  status != 'INACTIVE' ");

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                while(rs.next())
                {

                    User user = new User(rs.getString("userName"), rs.getString("password"), rs.getString("fName"),
                            rs.getString("lName"), rs.getString("status"), rs.getString("UUID"), rs.getString("dateCreated"));
                    list.add(user);

                }

                System.out.println("Got all QUERY \n\n");

            } catch (SQLException e) {
                System.out.println("Error Get All Query");
                e.printStackTrace();
            }
        }
//        else if (obj instanceof Game)
//        {
//            Game game = (Game) obj;
//
//            qryBuilder.append("FROM Game "
//                            +  "WHERE playerId = \'" + game.get() + "\' "
//                            +  "AND  status != 'INACTIVE' ");
//        }
        else if (obj instanceof Moves)
        {
            Moves moves = (Moves) obj;
            qryBuilder.append("FROM Moves "
                    +  "WHERE gameId = \'" + moves.getGameId() + "\' "
                    +  "ORDER BY time ASC;");

            try {
                ResultSet   rs = executeQuery(qryBuilder.toString());
                while(rs.next())
                {
                    Moves m = new Moves(rs.getString("gameId"), rs.getString("playerId"), Integer.parseInt(rs.getString("X_coord")),
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

            qryBuilder.append("FROM GameViewers " +
                    "WHERE viewerId = \'" + gv.getId() + "\' " );

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                while(rs.next())
                {
                    GameViewers gameViewers = new GameViewers(rs.getString("gameId"), rs.getString("viewerId"));
                    list.add(gameViewers);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("Able to get all GameViewers!!\n\n");

        }

        return list;
    }


    @Override
    public List<BaseModel> query(BaseModel obj, String filter) {

        List <BaseModel> list = new ArrayList<>();
        StringBuilder qryBuilder = new StringBuilder();
        qryBuilder.append("SELECT ");

        if(obj instanceof User)
        {
            qryBuilder.append("* " +
                    "FROM User " +
                    filter );

            try {
                ResultSet rs = executeQuery(qryBuilder.toString());

                while(rs.next())
                {
                    User u = new User(rs.getString("userName"), rs.getString("password"), rs.getString("fName"), rs.getString("lName"), rs.getString("status"), rs.getString("UUID"), rs.getString("dateCreated"));
                    list.add(u);
                }

                System.out.println("Got all QUERY \n\n");

            } catch (SQLException e) {
                System.out.println("Error Get All Query");
                e.printStackTrace();
                return null;
            }

        }
        else if (obj instanceof Game)
        {

            qryBuilder.append("* " +
                    "FROM Game " +
                    filter);

            /*
                    @ To get a plyer's game history
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

                while(rs.next())
                {
                    Game game = new Game((rs.getString("gameId")), rs.getString("startTime"),
                            rs.getString("endTime"), (rs.getString("p1Id")),
                            (rs.getString("p2Id")), (rs.getString("starterId")),
                            (rs.getString("winnerId")));
                    list.add(game);
                }

                System.out.println("Got all QUERY \n\n");

            } catch (SQLException e) {
                System.out.println("Error Get All Query");
                e.printStackTrace();
                return null;
            }

        }

        return list;
    }

//
//    @Override
//    public BaseModel getUser(String userName)
//    {
//        StringBuilder qryBuilder = new StringBuilder();
//        qryBuilder.append("SELECT ");
//
//            qryBuilder.append("* " + "FROM User " + "WHERE userName = \"" + userName + "\"");
//
//            try
//            {
//                ResultSet rs = executeQuery(qryBuilder.toString());
//
//                rs.next();
//                User user = new User(rs.getString("userName"), rs.getString("password"), rs.getString("fName"), rs.getString("lName"), rs.getString("status"), rs.getString("UUID"), rs.getString("dateCreated"));
//
//                return user;
//            }
//            catch (SQLException e) {
//                System.out.println("exception in getUser()");
//                e.printStackTrace();
//                return null;
//            }
//    }



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
