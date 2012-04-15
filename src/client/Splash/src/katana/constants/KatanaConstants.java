package katana.constants;

public abstract class KatanaConstants
{
	/** MISC **/
	public static final String KATANA_NAME = "Splash";
	
	/** Class Descriptions **/
	public static final String CLASS_DESC_DEFAULT = "Select your icon by pressing on it twice!";
	public static final String CLASS_DESC_1 = "This is a mage!";
	public static final String CLASS_DESC_2 = "This is a warrior! ";
	
	/** Server Info **/
	public static final String SERVER_IP = "projectkatana.no-ip.org";
	public static final int SERVER_PORT = 7777;
	
	/** Application Preferences **/
	// Login Preferences
	public static final String PREFS_LOGIN = "UserPreferences";
	public static final String PREFS_LOGIN_USER = "username";
	public static final String PREFS_LOGIN_PASS = "password";
	public static final String PREFS_LOGIN_PLAYERID = "PlayerId";
	
	// Game Preferences
	public static final String PREFS_GAME = "GamePreferences";
	public static final String PREFS_GAME_NAME = "roomname";
	public static final String PREFS_GAME_DIFF = "difficulty";
	public static final String PREFS_GAME_MAXP = "maxplayers";
	public static final String PREFS_GAME_CLASS = "class";
	
	// GPS
	public static final int GPS_MIN_REFRESHTIME = 10000;
	public static final int GPS_MIN_REFRESHDIST = 15;
	
	// LoginActivity Constants
    public static final int LOGIN_PASSMIN = 1;
    public static final int LOGIN_PASSMAX = 16;
    public static final int LOGIN_USERMIN = 1;
    public static final int LOGIN_USERMAX = 16;
    
    // LobbyActivity Constants
    public static final int LOBBY_MIN_ROOMNAME = 1;
    
    public static final String 	ROOM_NAME_DEFAULT = "Let's Fun!";
    public static final int 	ROOM_DIFF_DEFAULT = 2;
    public static final int 	ROOM_MAXP_DEFAULT = 4;
    
    public static final String DIFF_EASY = "Easy";
    public static final String DIFF_STD = "Standard";
    public static final String DIFF_HARD = "Hard";
	
    /** KatanaPacket **/
    public final static String PACKET_DATA_SEPERATOR = "\n";
    public final static String PACKET_FORMATTER = "%d" + PACKET_DATA_SEPERATOR 
                                                + "%d" + PACKET_DATA_SEPERATOR 
                                                + "%s";
    
    public static final int MAX_PACKET_BUF = 1024;
    public static final int PING_INTERVAL_MS = 10000;
    
    /** GameActivity **/
    public static final String GAME_BG_DEFAULT = "Background.jpg";
}