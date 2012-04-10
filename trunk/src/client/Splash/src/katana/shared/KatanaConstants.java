package katana.shared;

public abstract class KatanaConstants
{
	/** MISC **/
	public static final String GAMENAME = "Splash";
	
	/** Class Descriptions **/
	public static final String DEF_DESC = "Press twice on a class to select it!";
	public static final String DESC_CLASS1 = "This is internet explorer. He sucks. Pick Firefox instead.";
	public static final String DESC_CLASS2 = "You made the right choice.";
	
	/** Server Info **/
	public static final String SERVER_IP = "projectkatana.no-ip.org";
	public static final int SERVER_PORT = 7777;
	
	/** Application Preferences **/
	// Login Preferences
	public static final String PREFS_LOGIN = "UserPreferences";
	public static final String LOGIN_USER = "username";
	public static final String LOGIN_PASS = "password";
	public static final String PLAYER_ID = "PlayerId";
	
	// Game Preferences
	public static final String PREFS_GAME = "GamePreferences";
	public static final String GAME_NAME = "roomname";
	public static final String GAME_DIFF = "difficulty";
	public static final String GAME_MAXP = "maxplayers";
	public static final String GAME_CLASS = "class";
	
	// GPS
	public static final int GPS_MIN_REFRESHTIME = 10000;
	public static final int GPS_MIN_REFRESHDIST = 15;
	
	// LoginActivity Constants
    public static final int PASSMIN = 1;
    public static final int PASSMAX = 16;
    public static final int USERMIN = 1;
    public static final int USERMAX = 16;
    
    // LobbyActivity Constants
    public static final int ROOMNAMEMIN = 1;
    public static final String DEF_ROOMNAME = "Lets Fun!";
    public static final int DEF_ROOMDIFF = 2;
    public static final int DEF_ROOMMAXP = 4;
    public static final String DIFF_EASY = "Easy";
    public static final String DIFF_STD = "Standard";
    public static final String DIFF_HARD = "Hard";
	
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
                                                + "%s";
    
    public static final int MAX_PACKET_BUF = 1024;
    public static final int PING_INTERVAL_MS = 10000;
    
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