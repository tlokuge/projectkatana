
package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import shared.Constants;
import shared.KatanaPacket;
import shared.Opcode;

public abstract class PacketHandler
{
    public static boolean handlePacket(KatanaClient client, KatanaPacket packet)
    {
        if(packet == null)
            return false;
        
        switch(packet.getOpcode())
        {
            case C_LOGOUT:      handleLogoutPacket(client, packet);     return true;
            case C_REGISTER:    handleRegisterPacket(client, packet);   break;
            case C_LOGIN:       handleLoginPacket(client, packet);      break;
            case C_PONG:        handlePongPacket(client, packet);       break;
            case C_ROOM_CREATE: handleRoomCreatePacket(client, packet); break;
            case C_ROOM_DESTROY:handleRoomDestroyPacket(client, packet);break;
            case C_ROOM_JOIN:   handleRoomJoinPacket(client, packet);   break;
            case C_ROOM_LEAVE:  handleRoomLeavePacket(client, packet);  break;
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
        
        return false;
    }
    
    private static Player loginClient(String username, String password, KatanaClient client)
    {
        if(username == null || username.isEmpty() || password == null || password.isEmpty() || client == null)
            return null;
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList<HashMap<String, Object>> results = sql.execute("SELECT `user_id` FROM `users` WHERE `username` LIKE '" + username + "' AND `password` LIKE SHA1('" + password + "');");
        if(results == null || results.size() != 1) // Bad login
            return null;
        
        int id = (Integer)results.get(0).get("user_id");
        Player player = new Player(id, 
                                   username,
                                   Constants.DEFAULT_PLAYER_HEALTH,
                                   Constants.DEFAULT_PLAYER_ATKSPD, 
                                   Constants.DEFAULT_PLAYER_ATKDMG, 
                                   Constants.DEFAULT_PLAYER_MOVE, 
                                   0,
                                   client);
        
        client.setPlayer(player);
        KatanaServer.instance().removeWaitingClient(client);
        KatanaServer.instance().addPlayer(id, player);
        
        return player;
    }
    
    // No data
    private static void handleLogoutPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleLogoutPacket: INCOMPLETE");
        // Database stuff?
        client.remove(true);
    }
    
    // Packet data format expected to be username / password
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
            Player player = loginClient(username, password, client);
            if(player == null)
            {
                System.err.println("handleRegisterPacket: Username [" + username + "] already exists in database. Prevent registration");

                KatanaPacket response = new KatanaPacket(-1, Opcode.S_REG_NO);
                client.sendPacket(response);
                return;
            }
            else
            {
                KatanaPacket response = new KatanaPacket(-1, Opcode.S_AUTH_OK);
                response.addData(player.getId() + "");;
                player.sendPacket(response);
                
                return;
            }
        }
        
        // New user, create her account then log her in
        sql.executeQuery("INSERT INTO `users` (`username`,`password`) VALUES ('" + username + "', SHA1('" + password + "'));");
        Player player = loginClient(username, password, client);
        if(player == null)
        {
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_REG_NO); // Somehow something went wrong....
            client.sendPacket(response);
            return;
        }
        
        // Yay! Success!
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_REG_OK);
        response.addData(player.getId() + "");
        player.sendPacket(response);
    }
    
    // Data format: username, password
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
        
        String username = data[0].trim();
        String password = data[1];
        
        System.out.println("handleLoginPacket: User [" + username + "] logged in");
        Player player = loginClient(username, password, client);
        if(player == null)
        {
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_AUTH_NO);
            client.sendPacket(response);
            return;
        }
        
        // Login success!
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_AUTH_OK);
        response.addData(player.getId() + "");
        player.sendPacket(response);
    }
    
    // No data - unnecessary
    private static void handlePongPacket(KatanaClient client, KatanaPacket packet)
    {
        // what should we do in here
    }
    
    // Data format: name, difficulty, max_players
    private static void handleRoomCreatePacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRoomCreatePacket: INCOMPLETE");
        // Make sure player is in a valid lobby
        Player pl = client.getPlayer();
        if(pl.getLocation() < 0)
        {
            System.err.println("handleRoomCreatePacket: " + pl + " not in a lobby");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
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
        int difficulty, max_players;
        
        try
        {
            name = data[0].trim();
            difficulty = Integer.parseInt(data[1].trim());
            max_players = Integer.parseInt(data[2].trim());
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
        Lobby lobby = KatanaServer.instance().getLobby(pl.getLocation());
        if(lobby == null) // Do we even need to check?
        {
            System.err.println("handleRoomCreatePacket: location id (" + pl.getLocation() + ") is invalid");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        sql.executeQuery("INSERT INTO `rooms` (`room_name`, `location_id`, `difficulty`, `max_players`, `leader`) VALUES ('" + name + "'," + pl.getLocation() + "," + difficulty + "," + max_players + "," + client.getId() + ");");
        
        ArrayList<HashMap<String, Object>> results = sql.execute("SELECT `room_id` FROM `rooms` WHERE `leader` = " + client.getId() + ";");
        if(results == null || results.isEmpty())
        {
            // Error occurred
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        int room_id = (Integer)results.get(0).get("room_id");
        pl.addToRoom(room_id);
        pl.setRoomLeader(true);
        
        lobby.addRoom(new GameRoom(room_id, name, difficulty, max_players, pl));
        
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_CREATE_OK);
        response.addData(room_id + "");
        client.sendPacket(response);
    }
    
    // Room ID? Is it necessary? Room ID should be stored inside player.
    public static void handleRoomDestroyPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRoomDestroyPacket: INCOMPLETE");
        
        Player pl = client.getPlayer();
        if(pl == null || !pl.isRoomLeader())
        {
            System.err.println("Invalid player or player is not room leader - cannot delete room");
            return;
        }
        
        Lobby lobby = KatanaServer.instance().getLobby(pl.getLocation());
        GameRoom room = lobby.getRoom(pl.getRoom());
        if(room == null)
        {
            System.err.println("Unable to delete room - room not found");
            return;
        }
        
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_DESTROY);
        for(Player p : room.getPlayers())
        {
            p.removeFromRoom();
            p.setLocation(lobby.getLocationId());
            lobby.addPlayer(p);
            p.sendPacket(response);
        }
        
        SQLHandler sql = SQLHandler.instance();
        sql.executeQuery("DELETE FROM `user_rooms` WHERE `room_id` = " + room.getId() + ";");
        sql.executeQuery("DELETE FROM `rooms` WHERE `room_id` = " + room.getId() + ";");
        
        lobby.removeRoom(room.getId());
        pl.setRoomLeader(false);
    }
    
    // Room ID, Class ID
    public static void handleRoomJoinPacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRoomJoinPacket: INCOMPLETE");
        
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        int room_id;
        int class_id;
        try
        {
            room_id = Integer.parseInt(data[0].trim());
            class_id= Integer.parseInt(data[1].trim());
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleRoomJoinPacket: NumberFormatException in packet data (" + packet.getData() + ")");
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_JOIN_NO);
            client.sendPacket(response);
            return;
        }
        
        Player pl = client.getPlayer();
        Lobby lobby = KatanaServer.instance().getLobby(pl.getLocation());
        GameRoom room = lobby.getRoom(room_id);
        if(room == null)
        {
            System.err.println("handleRoomJoinPacket: room id (" + room_id + ") is invalid");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_JOIN_NO);
            pl.sendPacket(response);
            return;
        }
        
        if((room.getMaxPlayers() - room.getNumPlayers()) == 0)
        {
            System.err.println("handleRoomJoinPacket: room id (" + room_id + ") is full");
            // Response
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_JOIN_NO);
            client.sendPacket(response);
            return;
        }
        
        room.addPlayer(pl);
        SQLHandler sql = SQLHandler.instance();
        sql.executeQuery("INSERT INTO `user_rooms` VALUES (" + client.getId() + ", " + room_id + "," + class_id + ")");
        // Response
        KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_JOIN_OK);
        ArrayList<HashMap<String, Object>> results = sql.execute("SELECT `u`.`user_id`, `u`.`username`,`ur`.`class_id` FROM `user_rooms` ur INNER JOIN `users` u ON `ur`.`user_id` = `u`.`user_id` WHERE `ur`.`room_id` = " + room_id + ";");
        if(results != null && !results.isEmpty())
        {
            pl.addToRoom(room_id);
            pl.setClass(class_id);
            
            KatanaPacket notify = new KatanaPacket(-1, Opcode.S_ROOM_PLAYER_JOIN);
            notify.addData(pl.getId() + ";" + pl.getName() + ";" + class_id);
            for(HashMap map : results)
            {
                int id = (Integer)map.get("user_id");
                if(id == pl.getId())
                    continue;
                
                response.addData(id + ";" + map.get("username") + ";" + map.get("class_id") + ";");
                KatanaServer.instance().getPlayer(id).sendPacket(notify);
            }
        }
        
        client.sendPacket(response);
    }
    
    public static void handleRoomLeavePacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleRoomLeavePacket: INCOMPLETE");
        
        Player pl = KatanaServer.instance().getPlayer(client.getId());
        int room_id = pl.getRoom();
        pl.removeFromRoom();
        
        SQLHandler sql = SQLHandler.instance();
        sql.executeQuery("DELETE FROM `user_rooms` WHERE `user_id` = " + client.getId() + ";");
        
        GameRoom room = KatanaServer.instance().getLobby(pl.getLocation()).getRoom(room_id);
        if(room == null)
            return;
        room.removePlayer(pl);

        KatanaPacket notify = new KatanaPacket(-1, Opcode.S_ROOM_PLAYER_LEAVE);
        notify.addData(pl.getId() + "");
        for(Player p : room.getPlayers())
            p.sendPacket(notify);
    }
    
    // latitude, longitude
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
            int loc_id = -1;
            String name = "";

            Set<Integer> locs = KatanaServer.instance().getLocationIDs();
            
            Lobby lobby = null;
            for(int i : locs)
            {
                lobby = KatanaServer.instance().getLobby(i);
                double latitude = lobby.getLatitude();
                double longitude = lobby.getLongitude();
                double radius = lobby.getRadius();
                
                System.out.println(lat + " - " + latitude + " = " + Math.abs(lat - latitude));
                System.out.println(lng + " - " + longitude + " = " + Math.abs(lng - longitude));
                System.out.println("radius: " + radius);
                if((Math.abs(lat - latitude) < radius) && (Math.abs(lng - longitude) < radius))
                {
                    loc_id = lobby.getLocationId();
                    name = lobby.getName();
                    break;
                }
            }
            
            if(loc_id == -1 || name.isEmpty())
            {
                KatanaPacket response = new KatanaPacket(-1, Opcode.S_BAD_LOCATION);
                client.sendPacket(response);
                return;
            }
            
            Player pl = KatanaServer.instance().getPlayer(client.getId());
            Lobby old = KatanaServer.instance().getLobby(pl.getLocation());
            if(old != null)
                old.removePlayer(pl);
            pl.setLocation(loc_id);
            lobby.addPlayer(pl);
            
            KatanaPacket response = new KatanaPacket(-1, Opcode.S_ROOM_LIST);
            response.addData(name);
            
            Set<Integer> room_ids = lobby.getRoomIds();
            if(room_ids == null)
            {
                client.sendPacket(response);
                return;
            }
            
            for(int id : room_ids)
            {
                GameRoom room = lobby.getRoom(id);
                if(room == null)
                {
                    System.err.println("Room ID: " + id + " is null");
                    continue;
                }
                System.out.println(room);
                String r = room.getId() + ";" + room.getName() + ";" + room.getDifficulty() + ";" + room.getMaxPlayers() + ";";
                response.addData(r);
            }
            client.sendPacket(response);
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleRoomListPacket: Received bad data");
            ex.printStackTrace();
        }
    }
    
    // Class ID
    public static void handleClassChangePacket(KatanaClient client, KatanaPacket packet)
    {
        System.out.println("handleClassChangePacket: INCOMPLETE");
        
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 1)
        {
            System.err.println("handleClassChangePacket: Invalid data [ " + packet.getData() + "]");
            // Response?
            return;
        }
        
        int class_id;
        try
        {
            class_id = Integer.parseInt(data[0].trim()); 
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleClassChangePacket: " + ex.getLocalizedMessage());
            // Response?
            return; 
        }
        
        if(SQLCache.getClass(class_id) == null)
        {
            System.err.println("handleClassChangePacket: Class not found");
            // Response?
            return;
        }
        
        Player pl = client.getPlayer();
        pl.setClass(class_id);
        
        int room_id = pl.getRoom();
        GameRoom room = KatanaServer.instance().getLobby(pl.getLocation()).getRoom(pl.getRoom());
        
        KatanaPacket notify = new KatanaPacket(-1, Opcode.S_PLAYER_UPDATE_CLASS);
        notify.addData(pl.getId() + "");
        notify.addData(class_id + "");
        for(Player p : room.getPlayers())
            p.sendPacket(notify);
        
        SQLHandler.instance().executeQuery("UPDATE `user_rooms` SET `class_id` = " + class_id + "WHERE `user_id` = " + client.getId() + " AND `room_id` = " + room_id + ";");
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
