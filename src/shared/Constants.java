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
    public final static String CONFIG_UPDATE_DIFF  = "UPDATE_DIFF";
    public final static String CONFIG_SERVER_PORT  = "SERVER_PORT";
    
    /** KatanaPacket **/
    public final static String PACKET_DATA_SEPERATOR = "\n";
    public final static String PACKET_FORMATTER = "%d" + PACKET_DATA_SEPERATOR 
                                                + "%d" + PACKET_DATA_SEPERATOR 
                                                + "%d" + PACKET_DATA_SEPERATOR 
                                                + "%s";
    
    public static final int MAX_PACKET_BUF = 1024;
    public static final int PING_INTERVAL_MS = 10000;
    
    /** SQL Queries **/
    public static final String PING_QUERY = "SELECT 1 FROM `users` LIMIT 1";
    public static final String LOGIN_QUERY = "SELECT `user_id` FROM `users` WHERE `username` LIKE ? AND `password` LIKE SHA1(?);";
    public static final String USERNAME_QUERY = "SELECT `username` FROM `users` WHERE `username` LIKE ?;";
    public static final String REGUSER_QUERY = "INSERT INTO `users` (`username`,`password`) VALUES (?, SHA1(?));";
    public static final String ROOMCREATE_QUERY = "INSERT INTO `rooms` (`room_name`, `location_id`, `difficulty`, `max_players`, `leader`) VALUES (?,?,?,?,?);";
    public static final String ROOMLEADER_QUERY = "SELECT `room_id` FROM `rooms` WHERE `leader` = ?;";
    public static final String CLEARROOM_QUERY = "DELETE FROM `user_rooms` WHERE `room_id` = ?;";
    public static final String DELROOM_QUERY = "DELETE FROM `rooms` WHERE `room_id` = ?;";
    public static final String ROOMJOIN_QUERY = "INSERT INTO `user_rooms` VALUES (?,?,?);";
    public static final String ROOMPLAYERS_QUERY = "SELECT `u`.`user_id`, `u`.`username`,`ur`.`class_id` FROM `user_rooms` ur INNER JOIN `users` u ON `ur`.`user_id` = `u`.`user_id` WHERE `ur`.`room_id` = ?;";
    public static final String ROOMLEAVE_QUERY = "DELETE FROM `user_rooms` WHERE `user_id` = ?;";
    public static final String CLASSCHANGE_QUERY = "UPDATE `user_rooms` SET `class_id` = ? WHERE `user_id` = ?;";
    public static final String LEADERBOARD_QUERY = "SELECT `u`.`username`,`lb`.`points` FROM `leaderboard` lb INNER JOIN `users` u ON `lb`.`user_id` = `u`.`user_id` WHERE `location_id` = ? ORDER BY `points` DESC;";
    public static final String LOCATION_QUERY = "SELECT `location_id`, `location_name`, `latitude`, `longitude`, `radius` FROM `locations`;";
    public static final String SPELL_QUERY = "SELECT `spell_id`, `spell_name`,`damage`,`cooldown` FROM `spells`;";
    public static final String CLASS_QUERY = "SELECT `class_id`, `class_name`, `spell_1`, `spell_2`, `spell_3`, `spell_4`, `model_id` FROM `classes`;";
    
    
    // Default player stats
    public static final int DEFAULT_PLAYER_HEALTH  = 5000;
    public static final int DEFAULT_PLAYER_ATKSPD  = 1000;
    public static final int DEFAULT_PLAYER_ATKDMG  = 10;
    public static final float DEFAULT_PLAYER_MOVE  = 1.0f;
    
    // Waiting Room
    public static final int ROOM_MAX_PLAYERS    = 4;
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