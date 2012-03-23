
package server;

import java.util.ArrayList;
import java.util.HashMap;
import shared.Constants;
import shared.KatanaPacket;
import shared.Opcode;

public abstract class PacketHandler
{
    public static void handlePacket(KatanaClient client, KatanaPacket packet)
    {
        if(packet == null)
            return;
        
        switch(packet.getOpcode())
        {
            case C_REGISTER:    handleRegisterPacket(client, packet);   break;
            case C_LOGIN:       handleLoginPacket(client, packet);      break;
            case C_LOGOUT:      handleLogoutPacket(client, packet);     break;
            case C_PONG:        handlePongPacket(client, packet);       break;
            case C_ROOM_CREATE: handleRoomCreatePacket(client, packet); break;
            case C_ROOM_DESTROY:handleRoomDestroyPacket(client, packet);break;
            case C_ROOM_JOIN:   handleRoomJoinPacket(client, packet);   break;
            case C_ROOM_LIST:   handleRoomListPacket(client, packet);   break;
                
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
    
    private static long loginClient(String username, String password)
    {
        if(username == null || username.isEmpty() || password == null || password.isEmpty())
            return -1;
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList<HashMap<String, Object>> results = sql.execute("SELECT `id` FROM `users` WHERE `username` LIKE '" + username + "' AND `password` LIKE SHA1('" + password + "');");
        if(results == null || results.size() != 1) // Bad login
            return -1;
        
        return (Long)results.get(0).get("id");
    }
    
    // Packet data format expected to be username / password / location
    private static void handleRegisterPacket(KatanaClient client, KatanaPacket packet)
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
            // Handle case where the user reinstalled the app (if password matches database,log her in)
            long id = loginClient(username, password);
            if(id == -1)
            {
                System.err.println("handleRegisterPacket: Username [" + username + "] already exists in database. Prevent registration");

                KatanaPacket response = new KatanaPacket(-1, Opcode.S_REG_NO);
                client.sendPacket(response);
                return;
            }
            else
            {
                client.setId(id);
                KatanaPacket response = new KatanaPacket(-1, Opcode.S_AUTH_OK);
                client.sendPacket(response);
                
                KatanaServer.instance().addClient(id, client);
                return;
            }
        }
        
        sql.executeQuery("INSERT INTO `users` (`username`,`password`) VALUES ('" + username + "', SHA1('" + password + "'));");
        long id = loginClient(username, password);
        if(id == -1)
        {
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_REG_NO); // Somehow something went wrong....
            client.sendPacket(response);
            return;
        }
        
        client.setId(id);
        // Yay! Success!
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_REG_OK);
        response.addData(id + "");
        client.sendPacket(response);
        
        KatanaServer.instance().addClient(id, client);
    }
    
    // Data format: username, password, location
    private static void handleLoginPacket(KatanaClient client, KatanaPacket packet)
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
        
        System.out.println("handleLoginPacket: User [" + username + "] logged in");
        long id = loginClient(username, password);
        if(id == -1)
        {
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_AUTH_NO);
            client.sendPacket(response);
            return;
        }
        
        client.setId(id);
        // Login success!
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_AUTH_OK);
        response.addData(id + "");
        client.sendPacket(response);
        
        KatanaServer.instance().addClient(id, client);
    }
    
    // No data
    private static void handleLogoutPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleLogoutPacket: INCOMPLETE");
        // Database stuff?
        KatanaServer.instance().removeClient(client.getId());
    }
    
    // No data
    private static void handlePongPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handlePongPacket: INCOMPLETE");
    }
    
    // Data format: Location ID, difficulty, max_players
    private static void handleRoomCreatePacket(KatanaClient client, KatanaPacket packet)
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
    public static void handleRoomDestroyPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRoomDestroyPacket: INCOMPLETE");
    }
    
    // Room ID
    public static void handleRoomJoinPacket(KatanaClient client, KatanaPacket packet)
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
        int cur = (Integer)results.get(0).get("current");
        int max = (Integer)results.get(0).get("max_players");
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
    
    public static void handleRoomListPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRoomListPacket: INCOMPLETE");
        
        int location = 1; // Where do we get the location from? The packet or the client?
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList<HashMap<String,Object>> results = sql.execute("SELECT `id`,`name`,`difficulty`,`max_players` FROM `rooms` WHERE `location_id` = " + location + ";");
        
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_LIST);
        if(results != null && !results.isEmpty())
        {
            for(HashMap map : results)
            {
                String room = map.get("id") + ";" + map.get("name") + ";" + map.get("difficulty") + map.get("max_players");
                response.addData(room);
            }
        }
        client.sendPacket(response);
    }
}
