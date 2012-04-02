package server;

import com.mysql.jdbc.ResultSetMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import shared.Constants;

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
                    System.exit(Constants.ERR_SQL_CONN_LOST);
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
            System.exit(Constants.ERR_SQL_SINGLETON_LOST);
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
    }
}