package server.shared;

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
    public final static String CONFIG_MAX_PINGS    = "MAX_PINGS";
    
    /** KatanaPacket **/
    public final static String PACKET_DATA_SEPERATOR = "\n";
    public final static String PACKET_FORMATTER = "%d" + PACKET_DATA_SEPERATOR  // Packet ID
                                                + "%d" + PACKET_DATA_SEPERATOR  // Opcode
                                                + "%s";                         // Data
    
    public static final int MAX_PACKET_BUF = 1024;
    
    /** SQL Queries **/
    public static final String PING_QUERY = "SELECT 1 FROM `users` LIMIT 1";
    public static final String LOGIN_QUERY = "SELECT `user_id` FROM `users` WHERE `username` LIKE ? AND `password` LIKE SHA1(?);";
    public static final String USERNAME_QUERY = "SELECT `username` FROM `users` WHERE `username` LIKE ?;";
    public static final String REGUSER_QUERY = "INSERT INTO `users` (`username`,`password`) VALUES (?, SHA1(?));";
    public static final String LEADERBOARD_QUERY = "SELECT `u`.`username`,`lb`.`points` FROM `leaderboard` lb INNER JOIN `users` u ON `lb`.`user_id` = `u`.`user_id` WHERE `location_id` = ? ORDER BY `points` DESC;";
    public static final String LOCATION_QUERY = "SELECT `location_id`, `location_name`, `latitude`, `longitude`, `radius` FROM `locations`;";
    public static final String SPELL_QUERY = "SELECT `spell_id`, `spell_name`,`damage`,`cooldown` FROM `spells`;";
    public static final String CLASS_QUERY = "SELECT `class_id`, `class_name`, `spell_1`, `spell_2`, `spell_3`, `spell_4`, `model_id` FROM `classes`;";
    public static final String MODEL_QUERY = "SELECT `model_id`, `file` FROM `models`;";
    public static final String CREATURE_QUERY = "SELECT `creature_id`, `creature_name`, `health`, `level`, `attack_speed`, `attack_damage`, `move_speed`, `model_id`, `script` FROM `creatures`;";
    public static final String MAPTEMPLATE_QUERY = "SELECT `map_id`, `location_id`, `map_name`, `background` FROM `map_template`;";
    public static final String CREATUREINSTANCE_QUERY = "SELECT `creature_id` FROM `creature_instance` WHERE `map_id` = ?;";
    
    // Default player stats
    public static final int DEFAULT_PLAYER_HEALTH  = 5000;
    public static final int DEFAULT_PLAYER_ATKSPD  = 1000;
    public static final int DEFAULT_PLAYER_ATKDMG  = 10;
    public static final float DEFAULT_PLAYER_MOVE  = 1.0f;
    
    // Waiting Room
    public static final int ROOM_MAX_PLAYERS    = 4;
    
    // Game
    public static final int DIFFICULTY_HARD   = 3;
    public static final int DIFFICULTY_MEDIUM = 2;
    public static final int DIFFICULTY_EASY   = 1;
    
    public static final int GAME_END_TIMER = 30000;
    
    public static final int CREATURE_WATERMELON_ENTRY = 2;
}