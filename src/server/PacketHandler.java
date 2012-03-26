
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
            case C_CLASS_CHANGE:handleClassChangePacket(client, packet);break;
            case C_LEADERBOARD: handleLeaderboardPacket(client, packet);break;
                
            case C_GAME_START:
                break;
                
            case C_MOVE:
                break;
                
            case C_SPELL:
                break;
        }
    }
    
    private static int loginClient(String username, String password)
    {
        if(username == null || username.isEmpty() || password == null || password.isEmpty())
            return -1;
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList<HashMap<String, Object>> results = sql.execute("SELECT `user_id` FROM `users` WHERE `username` LIKE '" + username + "' AND `password` LIKE SHA1('" + password + "');");
        if(results == null || results.size() != 1) // Bad login
            return -1;
        
        return (Integer)results.get(0).get("user_id");
    }
    
    // Packet data format expected to be username / password / location
    private static void handleRegisterPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRegisterPacket: INCOMPLETE");
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 2)
        {
            System.err.println("handleRegisterPacket: Invalid data in packet (" + packet.getData() + ")");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_REG_NO);
            client.sendPacket(response);
            return;
        }
        
        String username = data[0].trim();
        String password = data[1];
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList results = sql.execute("SELECT `username` FROM `users` WHERE `username` LIKE '" + username + "';");
        if(results != null && !results.isEmpty())
        {
            // Handle case where the user reinstalled the app (if password matches database,log her in)
            int id = loginClient(username, password);
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
        
        // New user, create her account then log her in
        sql.executeQuery("INSERT INTO `users` (`username`,`password`) VALUES ('" + username + "', SHA1('" + password + "'));");
        int id = loginClient(username, password);
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
        if(data.length < 2)
        {
            System.err.println("handleLoginPacket: Invalid data in packet (" + packet.getData() + ")");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_AUTH_NO);
            client.sendPacket(response);
            return;
        }
        
        String username = data[0];
        String password = data[1];
        
        System.out.println("handleLoginPacket: User [" + username + "] logged in");
        int id = loginClient(username, password);
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
        client.remove();
    }
    
    // No data - unnecessary
    private static void handlePongPacket(KatanaClient client, KatanaPacket packet)
    {
        // what should we do in here
    }
    
    // Data format: Location ID, difficulty, max_players
    private static void handleRoomCreatePacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRoomCreatePacket: INCOMPLETE");
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 3)
        {
            System.err.println("handleRoomCreatePacket: Invalid data in packet (" + packet.getData() + ")");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        String name;
        int location, difficulty, max_players;
        
        try
        {
            name = data[0].trim();
            location = Integer.parseInt(data[1].trim());
            difficulty = Integer.parseInt(data[2].trim());
            max_players = Integer.parseInt(data[3].trim());
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleRoomCreatePacket: NumberFormatException (" + ex.getLocalizedMessage() + ") in packet data (" + packet.getData() + ")");
            
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        if(max_players > 4 || max_players < 1) // is this possible?
        {
            System.err.println("handleRoomCreatePacket: max_players not within 1 and 4 (" + max_players + ")");
            // Response - necessary?
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList results = sql.execute("SELECT * FROM `locations` WHERE `location_id` = " + location + ";");
        if(results == null || results.size() != 1) // Do we even need to check?
        {
            System.err.println("handleRoomCreatePacket: location id (" + location + ") is invalid");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        sql.executeQuery("INSERT INTO `rooms` (`name`, `location_id`, `difficulty`, `max_players`) VALUES (" + name + "," + location + "," + difficulty + "," + max_players + ");");
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_OK);
        client.sendPacket(response);
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
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_JOIN_NO);
            client.sendPacket(response);
            return;
        }
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList<HashMap<String,Object>> results = sql.execute("SELECT r.`max_players`, COUNT(ur.`room_id`) AS `current` FROM `rooms` r INNER JOIN `user_rooms` ur ON r.`room_id` = ur.`room_id` WHERE r.`room_id` = " + room_id + ";");
        if(results == null || results.size() != 1) // No room found or too many rooms found (should not be possible)
        {
            System.err.println("handleRoomJoinPacket: room id (" + room_id + ") is invalid");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_JOIN_NO);
            client.sendPacket(response);
            return;
        }
        
        // Check if the room has space for the player - this might be unnecessary if the server has Room objects containing the necessary data
        int cur = (Integer)results.get(0).get("current");
        int max = (Integer)results.get(0).get("max_players");
        if((max - cur) == 0)
        {
            System.err.println("handleRoomJoinPacket: room id (" + room_id + ") is full");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_JOIN_NO);
            client.sendPacket(response);
            return;
        }
        
        int class_id = 0; // Player.getClassId();
        sql.executeQuery("INSERT INTO `user_rooms` VALUES (" + packet.getPlayerId() + ", " + room_id + "," + class_id + ")");
        // Response
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_JOIN_OK);
        results = sql.execute("SELECT `u`.`user_id`, `u`.`username`,`ur`.`class_id` FROM `user_rooms` ur INNER JOIN `users` u ON `ur`.`user_id` = `u`.`user_id` WHERE `ur`.`room_id` = " + room_id + ";");
        if(results != null && !results.isEmpty())
            for(HashMap map : results)
                response.addData(map.get("user_id") + ";" + map.get("username") + ";" + map.get("class_id") + ";");
        client.sendPacket(response);
    }
    
    public static void handleRoomListPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRoomListPacket: INCOMPLETE");
        
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 2) // lat, long
        {
            System.err.println("handleRoomListPacket: Received invalid data [" + packet.getData() + "]");
            // Send response
            return;
        }
        
        try
        {
            double lat = Double.parseDouble(data[0].trim());
            double lng = Double.parseDouble(data[1].trim());
            int location = -1;
            String name = "";

            SQLHandler sql = SQLHandler.instance();
            ArrayList<HashMap<String, Object>> results = sql.execute("SELECT `location_id`,`name`,`latitude`,`longitude`,`radius` FROM `locations`;");
            if(results == null || results.isEmpty())
            {
                System.err.println("handleRoomListPacket: Database has no locations!");
                //Response?
                return;
            }
            
            for(HashMap map : results)
            {
                double latitude = (Double)map.get("latitude");
                double longitude = (Double)map.get("longitude");
                double radius = (Double)map.get("radius");
                
                System.out.println(lat + " - " + latitude + " = " + Math.abs(lat - latitude));
                System.out.println(lng + " - " + longitude + " = " + Math.abs(lng - longitude));
                
                if((Math.abs(lat - latitude) < radius) && (Math.abs(lng - longitude) < radius))
                {
                    location = (Integer)map.get("location_id");
                    name = (String)map.get("name");
                    break;
                }
            }
            
            if(location == -1 || name.isEmpty())
            {
                KatanaPacket response = new KatanaPacket(-1, Opcode.S_BAD_LOCATION);
                client.sendPacket(response);
                return;
            }
            
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_LIST);
            response.addData(name);
            
            results = sql.execute("SELECT `location_id`,`name`,`difficulty`,`max_players` FROM `rooms` WHERE `location_id` = " + location + ";");
            if(results != null && !results.isEmpty())
            {
                for(HashMap map : results)
                {
                    String room = map.get("location_id") + ";" + map.get("name") + ";" + map.get("difficulty") + ";" + map.get("max_players")+ ";";
                    response.addData(room);
                }
            }
            client.sendPacket(response);
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleRoomListPacket: Received bad data");
            ex.printStackTrace();
        }
    }
    
    // Class ID, Room ID
    public static void handleClassChangePacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleClassChangePacket: INCOMPLETE");
        
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 2)
        {
            System.err.println("handleClassChangePacket: Invalid data [ " + packet.getData() + "]");
            // Response?
            return;
        }
        
        int class_id;
        int room_id;
        try
        {
            class_id = Integer.parseInt(data[0].trim()); 
            room_id  = Integer.parseInt(data[1].trim());
        }
        catch(NumberFormatException ex) 
        {
            System.err.println("handleClassChangePacket: " + ex.getLocalizedMessage());
            // Response?
            return; 
        }
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList<HashMap<String, Object>> results = sql.execute("SELECT `class_id` FROM `classes` WHERE `class_id` = " + class_id + ";");
        if(results.size() < 1)
        {
            System.err.println("handleClassChangePacket: Class not found");
            // Response?
            return;
        }
        
        sql.executeQuery("UPDATE `user_rooms` SET `class_id` = " + class_id + "WHERE `user_id` = " + client.getId() + " AND `room_id` = " + room_id + ";");
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_CLASS_CHANGE_OK);
        client.sendPacket(response);
    }
    
    // For now, assume that location is sent with packet. 
    public static void handleLeaderboardPacket(KatanaClient client, KatanaPacket packet)
    {
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length != 1)
        {
            System.err.println("handleLeaderboardPacket: Invalid data in packet: " + packet.getData());
            // Response?
            return;
        }
        
        int location;
        try
        {
            location = Integer.parseInt(data[0].trim());
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleLeaderboard: Bad data (" + ex.getLocalizedMessage() + ") in " + packet.getData());
            // Response?
            return;
        }
        
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_LEADERBOARD);
        SQLHandler sql = SQLHandler.instance();
        ArrayList<HashMap<String, Object>> results = sql.execute("SELECT `u`.`username`,`lb`.`points` FROM `leaderboard` lb INNER JOIN `users` u ON `lb`.`user_id` = `u`.`user_id` WHERE `location_id` = " + location + " ORDER BY `points` DESC;");
        if(results != null && !results.isEmpty())
            for(HashMap map : results)
                response.addData(map.get("username") + ";" + map.get("points") + ";");
        client.sendPacket(response);
    }
}
