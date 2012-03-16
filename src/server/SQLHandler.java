package server;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.sql.Statement;
import shared.Constants;


public class SQLHandler implements Runnable
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
    
    private void checkConnection() throws SQLException
    {
        if(connection == null || connection.isClosed())
        {
            System.err.println("MySQL: Lost Connection, attempting to reconnect...");
            initConnection();
            if(connection == null || connection.isClosed())
            {
                System.err.println("MySQL: Terminating");
                System.exit(Constants.ERR_SQL_CONN_LOST);
            }
        }
    }
    
    public void run()
    {
        initConnection();
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
            new Thread(instance, "SQL-Thread").start();
        }
    }
    
    public static SQLHandler instance() { return instance; }
    
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
                System.exit(Constants.ERR_SQL_CONN_FAIL);
            }
            
            System.out.println("MySQL: Connection Established");
        }
        catch(Exception ex)
        {
            System.err.println("MySQL: Connection Failed");
            ex.printStackTrace();
            System.exit(Constants.ERR_SQL_CONN_FAIL);
        }
    }
    
    public void executeQuery(String query)
    {
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
    }
}