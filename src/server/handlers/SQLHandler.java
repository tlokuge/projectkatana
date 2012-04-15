package server.handlers;

import com.mysql.jdbc.ResultSetMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import server.communication.KatanaServer;
import server.shared.Constants;
import server.utils.KatanaError;

public class SQLHandler
{
    private String hostname;
    private String database;
    private String username;
    private String password;
    
    private Connection connection;
    private static SQLHandler instance = null;
    
    private SQLHandler(String hostname, String database, String username, String password)
    {
        this.hostname = hostname;
        this.database = database;
        this.username = username;
        this.password = password;
        
        connection = null;
    }
    
    private void checkConnection()
    {
        try
        {
            if(connection == null || connection.isClosed())
            {
                System.err.println("MySQL: Lost Connection, attempting to reconnect...");
                initConnection();
                if(connection == null || connection.isClosed())
                {
                    System.err.println("MySQL: Terminating");
                    KatanaServer.exit(KatanaError.ERR_SQL_CONN_LOST);
                }
            }
        }
        catch(Exception ex)
        {
            System.err.println("MySQL: Exception in checkConnection");
            ex.printStackTrace();
        }
    }
    
    public void finalize()
    {
        System.out.println("MySQL: Closing Connection...");
        try
        {
            if(connection != null)
                connection.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Unable to clone a singleton!");
    }
    
    public static void initSingleton(String hostname, String database, String username, String password)
    {
        if(instance == null)
        {
            instance = new SQLHandler(hostname, database, username, password);
            instance.initConnection();
        }
    }
    
    public synchronized static SQLHandler instance() 
    {
        if(instance == null)
        {
            System.err.println("MySQL: Singleton lost");
            KatanaServer.exit(KatanaError.ERR_SQL_SINGLETON_LOST);
        }
        
        return instance;
    }
    
    private static ArrayList<HashMap<String, Object>> convertResultSetToArrayList(ResultSet results)
    {
        if(results == null)
            return null;
        
        try
        {
            ArrayList<HashMap<String, Object>> results_list = new ArrayList<HashMap<String, Object>>();
            ResultSetMetaData meta = (ResultSetMetaData) results.getMetaData();
            //for(int i = 1; i < meta.getColumnCount()+1; ++i)
            //    System.out.print(meta.getColumnName(i) + "\t");
            System.out.println();
            while(results.next())
            {
                HashMap<String, Object> row = new HashMap<String, Object>();
                for(int i = 1; i < meta.getColumnCount()+1; ++i)
                {
                  //  System.out.print(results.getObject(i) + "\t");
                    row.put(meta.getColumnName(i), results.getObject(i));
                }
                //System.out.println();
                results_list.add(row);
            }
            
            return results_list;
        }
        catch(Exception ex)
        {
            System.err.println("MySQL: Error converting ResultSet to ArrayList");
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public void initConnection()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String address = "jdbc:mysql://" + hostname + "/" + database;
            connection = DriverManager.getConnection(address, username, password);
            if(connection.isClosed())
            {
                System.err.println("MySQL: Connection Failed");
                KatanaServer.exit(KatanaError.ERR_SQL_CONN_FAIL);
            }
            
            System.out.println("MySQL: Connection Established");
        }
        catch(Exception ex)
        {
            System.err.println("MySQL: Connection Failed");
            ex.printStackTrace();
            KatanaServer.exit(KatanaError.ERR_SQL_CONN_FAIL);
        }
    }
    
    public synchronized void runPingQuery()
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.PING_QUERY);
            query.executeQuery();
            query.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runLoginQuery(String username, String password)
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.LOGIN_QUERY);
            query.setString(1, username);
            query.setString(2, password);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runUsernameQuery(String username)
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.USERNAME_QUERY);
            query.setString(1, username);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized void runRegisterUserQuery(String username, String password)
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.REGUSER_QUERY);
            query.setString(1, username);
            query.setString(2, password);
            query.execute();
            query.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    public synchronized ArrayList<HashMap<String, Object>> runLeaderboardQuery(int location_id)
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.LEADERBOARD_QUERY);
            query.setInt(1, location_id);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runLocationQuery()
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.LOCATION_QUERY);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runSpellQuery()
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.SPELL_QUERY);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runClassQuery()
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.CLASS_QUERY);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runCreatureQuery()
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.CREATURE_QUERY);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runModelQuery()
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.MODEL_QUERY);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runMapTemplateQuery()
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.MAPTEMPLATE_QUERY);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized ArrayList<HashMap<String, Object>> runCreatureInstanceQuery(int map_id)
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.CREATUREINSTANCE_QUERY);
            query.setInt(1, map_id);
            ArrayList<HashMap<String, Object>> results = convertResultSetToArrayList(query.executeQuery());
            query.close();
            
            return results;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public synchronized void runInsertUpdateLeaderboardQuery(int player_id, int location_id, int score)
    {
        checkConnection();
        
        try
        {
            PreparedStatement query = connection.prepareStatement(Constants.LEADERBOARDCHECK_QUERY);
            query.setInt(1, player_id);
            query.setInt(2, location_id);
            ResultSet results = query.executeQuery();
            // Check if a row exists, if so just update the row
            if(results.first())
            {
                query.close();
                query = connection.prepareStatement(Constants.LEADERBOARDUPDATE_QUERY);
                query.setInt(1, score);
                query.setInt(2, player_id);
                query.setInt(3, location_id);
                query.execute();
            }
            else // Row does not exist, so insert it
            {
                query.close();
                query = connection.prepareStatement(Constants.LEADERBOARDINSERT_QUERY);
                query.setInt(1, player_id);
                query.setInt(2, location_id);
                query.setInt(3, score);
                query.execute();
            }
            
            query.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /*
    public ArrayList<HashMap<String, Object>> execute(String query)
    {   
        checkConnection();
        
        try
        {
            System.out.println("MySQL: " + query);
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query);
            ArrayList<HashMap<String, Object>> results_list = convertResultSetToArrayList(results);
            statement.close();
            return results_list;
        }
        catch(Exception ex)
        {
            System.err.println("MySQL: Error executing resultset query '" + query + "'");
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public void executeQuery(String query)
    {
        checkConnection();
        
        try
        {
            System.out.println("MySQL: " + query);
            Statement statement = connection.createStatement();
            statement.execute(query);
            statement.close();
        }
        catch(Exception ex)
        {
            System.err.println("MySQL: Error executing query '" + query + "'");
            ex.printStackTrace();
        }
    }*/
}