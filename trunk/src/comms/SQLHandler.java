package comms;


import java.sql.Connection;
import java.sql.DriverManager;


public class SQLHandler
{
    private String hostname;
    private String database;
    private String username;
    private String password;
    
    public SQLHandler(String hostname, String database, String username, String password)
    {
        this.hostname = hostname;
        this.database = database;
        this.username = username;
        this.password = password;
    }
    
    public void initConnection()
    {
        try
        {
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            String address = "jdbc:mysql://" + hostname + "/" + database;
            Connection con = DriverManager.getConnection(address, username, password);
            if(con.isClosed())
            {
                System.err.println("ERROR: UNABLE TO INIT SQL CONNECTION");
                System.exit(1);
            }
            
            System.out.println("yay sql");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}