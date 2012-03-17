package shared;

public abstract class Constants
{
    /** Config **/
    public final static String CONFIG_FILE  = "Server.conf";
    public final static String CONFIG_LINE_SEPERATOR = ":";
    public final static String CONFIG_SQL_HOSTNAME = "SQL_HOSTNAME";
    public final static String CONFIG_SQL_DATABASE = "SQL_DATABASE";
    public final static String CONFIG_SQL_USERNAME = "SQL_USERNAME";
    public final static String CONFIG_SQL_PASSWORD = "SQL_PASSWORD";
    public final static String CONFIG_PING_INTERVAL= "PING_INTERVAL";
    
    public final static String PACKET_FORMATTER = "%d\n%d\n%d\n%s";
    
    public static final int MAX_PACKET_BUF = 1024;
    public static final int SERVER_LISTEN_PORT = 59140;
    public static final int PING_INTERVAL_MS = 10000;
    
    /** Error Codes **/
    // SQL
    public static final int ERR_SQL_CONN_LOST = 1;
    public static final int ERR_SQL_CONN_FAIL = 2;
    public static final int ERR_SQL_SINGLETON_LOST = 3;
    
    // Config
    public static final int ERR_CONFIG_LOAD_FAIL = 4;
}