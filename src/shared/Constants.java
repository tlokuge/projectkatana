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
    public final static String CONFIG_SERVER_PORT  = "SERVER_PORT";
    
    /** KatanaPacket **/
    public final static String PACKET_DATA_SEPERATOR = "\n";
    public final static String PACKET_FORMATTER = "%d" + PACKET_DATA_SEPERATOR 
                                                + "%d" + PACKET_DATA_SEPERATOR 
                                                + "%d" + PACKET_DATA_SEPERATOR 
                                                + "%s";
    
    public static final int MAX_PACKET_BUF = 1024;
    public static final int PING_INTERVAL_MS = 10000;
    
    // Default player stats
    public static final int DEFAULT_PLAYER_HEALTH  = 5000;
    public static final int DEFAULT_PLAYER_ATKSPD  = 1000;
    public static final int DEFAULT_PLAYER_ATKDMG  = 10;
    public static final float DEFAULT_PLAYER_MOVE  = 1.0f;
    
    /** Error Codes **/
    // SQL
    public static final int ERR_SQL_CONN_LOST = 1;
    public static final int ERR_SQL_CONN_FAIL = 2;
    public static final int ERR_SQL_SINGLETON_LOST = 3;
    
    // Config
    public static final int ERR_CONFIG_LOAD_FAIL = 4;
    
    // Server
    public static final int ERR_SERVER_LISTEN_FAIL = 5;
    public static final int ERR_SERVER_SINGLETON_LOST = 6;
}