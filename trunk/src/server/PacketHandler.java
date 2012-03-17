
package server;

import java.util.ArrayList;
import java.util.HashMap;
import shared.Constants;
import shared.KatanaPacket;

public abstract class PacketHandler
{
    public static void handlePacket(KatanaPacket packet)
    {
        if(packet == null)
            return;
        
        switch(packet.getOpcode())
        {
            case C_REGISTER:        handleRegisterPacket(packet);   break;
            case C_LOGIN:           handleLoginPacket(packet);      break;
            case C_LOGOUT:          handleLogoutPacket(packet);     break;
            case C_PONG:            handlePongPacket(packet);       break;
            case C_ROOM_CREATE:     handleRoomCreatePacket(packet); break;
            case C_ROOM_DESTROY:    handleRoomDestroyPacket(packet);break;
            case C_ROOM_JOIN:       handleRoomJoinPacket(packet);   break;
            case C_ROOM_LIST:
                break;
                
            case C_CLASS_CHANGE:
                break;
                
            case C_LEADERBOARD:
                break;
                
            case C_GAME_START:
                break;
                
            case C_MOVE:
                break;
                
            case C_SPELL:
                break;
        }
    }
    
    // Packet data format expected to be username / password / location
    private static void handleRegisterPacket(KatanaPacket packet)
    {
        System.out.println("handleRegisterPacket: INCOMPLETE");
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 3)
        {
            System.err.println("handleRegisterPacket: Invalid data in packet (" + packet.getData() + ")");
            // TODO: Send response to client
            return;
        }
        
        String username = data[0].trim();
        String password = data[1];
        String location = data[2]; // TODO
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList results = sql.execute("SELECT `username` FROM `users` WHERE `username` LIKE '" + username + "';");
        if(results != null && !results.isEmpty())
        {
            System.err.println("handleRegisterPacket: Username [" + username + "] already exists in database. Prevent registration");
            // Send response to client
            return;
        }
        
        sql.executeQuery("INSERT INTO `users` (`username`,`password`) VALUES ('" + username + "', SHA1('" + password + "'));");
        
    }
    
    // Data format: username, password, location
    private static void handleLoginPacket(KatanaPacket packet)
    {
        System.out.println("handleLoginPacket: INCOMPLETE");
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 3)
        {
            System.err.println("handleLoginPacket: Invalid data in packet (" + packet.getData() + ")");
            // TODO: Send response to client
            return;
        }
        
        String username = data[0];
        String password = data[1];
        String location = data[2];
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList results = sql.execute("SELECT * FROM `users` WHERE `username` LIKE '" + username + "' AND `password` LIKE SHA1('" + password + "');");
        if(results == null || results.size() != 1) // Bad login
        {
            System.out.println("handleLoginPacket: Bad login - " + username + " / " + password);
            // TODO: Client response
            return;
        }
        
        // TODO: Login success!
        System.out.println("handleLoginPacket: User [" + username + "] logged in");
    }
    
    // No data
    private static void handleLogoutPacket(KatanaPacket packet)
    {
        System.out.println("handleLogoutPacket: INCOMPLETE");
    }
    
    // No data
    private static void handlePongPacket(KatanaPacket packet)
    {
        System.out.println("handlePongPacket: INCOMPLETE");
    }
    
    // Data format: Location ID, difficulty, max_players
    private static void handleRoomCreatePacket(KatanaPacket packet)
    {
        System.out.println("handleRoomCreatePacket: INCOMPLETE");
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 3)
        {
            System.err.println("handleRoomCreatePacket: Invalid data in packet (" + packet.getData() + ")");
            // TODO: Send response to client
            return;
        }
        
        int location, difficulty, max_players;
        
        try
        {
            location = Integer.parseInt(data[0].trim());
            difficulty = Integer.parseInt(data[1].trim());
            max_players = Integer.parseInt(data[2].trim());
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleRoomCreatePacket: NumberFormatException in packet data (" + packet.getData() + ")");
            // TODO: Send response to client
            return;
        }
        
        // TODO: Check difficulty level is within range
        if(max_players > 4 || max_players < 1) // is this possible?
        {
            System.err.println("handleRoomCreatePacket: max_players not within 1 and 4 (" + max_players + ")");
            // TODO: Send response to client - necessary?
            return;
        }
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList results = sql.execute("SELECT * FROM `locations` WHERE `id` = " + location + ";");
        if(results == null || results.size() != 1) // Do we even need to check?
        {
            System.err.println("handleRoomCreatePacket: location id (" + location + ") is invalid");
            // TODO: Response
            return;
        }
        
        sql.executeQuery("INSERT INTO `rooms` (`location_id`, `difficulty`, `max_players`) VALUES (" + location + "," + difficulty + "," + max_players + ");");
        
    }
    
    // Room ID? Is it necessary? Room ID should be stored inside player.
    public static void handleRoomDestroyPacket(KatanaPacket packet)
    {
        System.out.println("handleRoomDestroyPacket: INCOMPLETE");
    }
    
    // Room ID
    public static void handleRoomJoinPacket(KatanaPacket packet)
    {
        System.out.println("handleRoomJoinPacket: INCOMPLETE");
        
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        int room_id;
        try
        {
            room_id = Integer.parseInt(data[0].trim());
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleRoomJoinPacket: NumberFormatException in packet data (" + packet.getData() + ")");
            // TODO: send response to client
            return;
        }
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList<HashMap<String,Object>> results = sql.execute("SELECT r.`max_players`, COUNT(ur.`room_id`) AS `current` FROM `rooms` r INNER JOIN `user_rooms` ur ON r.`id` = ur.`room_id` WHERE r.`id` = " + room_id + ";");
        if(results == null || results.size() != 1) // No room found or too many rooms found (should not be possible)
        {
            System.err.println("handleRoomJoinPacket: room id (" + room_id + ") is invalid");
            // TODO: Response
            return;
        }
        
        // Check if the room has space for the player - this might be unnecessary if the server has Room objects containing the necessary data
        int cur = (int)results.get(0).get("current");
        int max = (int)results.get(0).get("max_players");
        if((max - cur) == 0)
        {
            System.err.println("handleRoomJoinPacket: room id (" + room_id + ") is full");
            // TODO: Response
            return;
        }
        
        int class_id = 0; // Player.getClassId();
        sql.executeQuery("INSERT INTO `user_rooms` VALUES (" + packet.getPlayerId() + ", " + room_id + "," + class_id + ")");
        // Response
        
    }
}
