package shared;

public abstract class Constants
{
    public final static String SERVER_CONFIG_FILE = "Server.conf";
    
    public final static String PACKET_FORMATTER = "%d\n%d\n%d\n%s";
    
    public static final int MAX_PACKET_BUF = 1024;
    public static final int SERVER_LISTEN_PORT = 59140;
    public static final int PING_INTERVAL_MS = 10000;
    
    /** Error Codes **/
    // SQL Errors
    public static final int ERR_SQL_CONN_LOST = 1;
    public static final int ERR_SQL_CONN_FAIL = 2;
    
    public static final int ERR_CONFIG_LOAD_FAIL = 3;
}