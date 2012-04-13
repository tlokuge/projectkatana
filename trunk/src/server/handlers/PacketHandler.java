
package server.handlers;

import server.utils.SQLCache;
import server.communication.KatanaClient;
import server.communication.KatanaServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import server.game.Map;
import server.limbo.GameRoom;
import server.limbo.Lobby;
import server.game.Player;
import server.shared.Constants;
import server.shared.KatanaPacket;
import server.shared.Opcode;
import server.templates.MapTemplate;

public abstract class PacketHandler
{
    public static void handlePacket(KatanaClient client, KatanaPacket packet)
    {
        if(packet == null)
            return;
        
        switch(packet.getOpcode())
        {
            case C_LOGOUT:      handleLogoutPacket(client, packet);     break;
            case C_REGISTER:    handleRegisterPacket(client, packet);   break;
            case C_LOGIN:       handleLoginPacket(client, packet);      break;
            case C_PONG:        handlePongPacket(client, packet);       break;
            case C_ROOM_CREATE: handleRoomCreatePacket(client, packet); break;
            case C_ROOM_DESTROY:handleRoomDestroyPacket(client, packet);break;
            case C_ROOM_JOIN:   handleRoomJoinPacket(client, packet);   break;
            case C_ROOM_LEAVE:  handleRoomLeavePacket(client);          break;
            case C_ROOM_LIST:   handleRoomListPacket(client, packet);   break;
            case C_CLASS_CHANGE:handleClassChangePacket(client, packet);break;
            case C_LEADERBOARD: handleLeaderboardPacket(client, packet);break;
                
            case C_GAME_START:  handleGameStartPacket(client, packet);  break;
            case C_GAME_READY:  handleGameReadyPacket(client, packet);  break;
                
            case C_MOVE:
                break;
                
            case C_SPELL:
                break;
        }
    }
    
    private static Player loginClient(String username, String password, KatanaClient client)
    {
        if(username == null || username.isEmpty() || password == null || password.isEmpty() || client == null)
            return null;
        
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().runLoginQuery(username, password);
        if(results == null || results.size() != 1) // Bad login
            return null;
        
        int id = (Integer)results.get(0).get("user_id");
        // Check if player is already logged in
        if(KatanaServer.instance().getPlayer(id) != null)
        {
            System.err.println("Player " + id + " attempted to log in but is already authenticated!");
            return null;
        }
        
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
        Player pl = client.getPlayer();
        // Destroy any rooms that the player is in
        if(pl.isRoomLeader())
        {
            Lobby lobby = KatanaServer.instance().getLobby(pl.getLocation());
            GameRoom room = lobby.getRoom(pl.getRoom());
            destroyRoom(lobby, room);
        }
        
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
            KatanaPacket response = new KatanaPacket(Opcode.S_REG_NO);
            client.sendPacket(response);
            return;
        }
        
        String username = data[0].trim();
        String password = data[1];
        
        SQLHandler sql = SQLHandler.instance();
        ArrayList results = sql.runUsernameQuery(username);
        if(results != null && !results.isEmpty())
        {
            // Handle case where the user reinstalled the app (if password matches database,log her in)
            Player player = loginClient(username, password, client);
            if(player == null)
            {
                System.err.println("handleRegisterPacket: Username [" + username + "] already exists in database. Prevent registration");

                KatanaPacket response = new KatanaPacket(Opcode.S_REG_NO);
                client.sendPacket(response);
                return;
            }
            else
            {
                KatanaPacket response = new KatanaPacket(Opcode.S_AUTH_OK);
                response.addData(player.getId() + "");;
                player.sendPacket(response);
                
                return;
            }
        }
        
        // New user, create her account then log her in
        sql.runRegisterUserQuery(username, password);
        Player player = loginClient(username, password, client);
        if(player == null)
        {
            KatanaPacket response = new KatanaPacket(Opcode.S_REG_NO); // Somehow something went wrong....
            client.sendPacket(response);
            return;
        }
        
        // Yay! Success!
        KatanaPacket response = new KatanaPacket(Opcode.S_REG_OK);
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
            KatanaPacket response = new KatanaPacket(Opcode.S_AUTH_NO);
            client.sendPacket(response);
            return;
        }
        
        String username = data[0].trim();
        String password = data[1];
        
        System.out.println("handleLoginPacket: User [" + username + "] logged in");
        Player player = loginClient(username, password, client);
        if(player == null)
        {
            KatanaPacket response = new KatanaPacket(Opcode.S_AUTH_NO);
            client.sendPacket(response);
            return;
        }
        
        // Login success!
        KatanaPacket response = new KatanaPacket(Opcode.S_AUTH_OK);
        response.addData(player.getId() + "");
        player.sendPacket(response);
    }
    
    // No data - unnecessary
    private static void handlePongPacket(KatanaClient client, KatanaPacket packet)
    {
        // what should we do in here
        client.pongReceived();
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
            KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        String[] data = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
        if(data.length < 3)
        {
            System.err.println("handleRoomCreatePacket: Invalid data in packet (" + packet.getData() + ")");
            // Response
            KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        String name;
        int difficulty, max_players, class_id;
        
        try
        {
            name = data[0].trim();
            difficulty = Integer.parseInt(data[1].trim());
            max_players = Integer.parseInt(data[2].trim());
            class_id = Integer.parseInt(data[3].trim());
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleRoomCreatePacket: NumberFormatException (" + ex.getLocalizedMessage() + ") in packet data (" + packet.getData() + ")");
            
            // Response
            KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        if(max_players > 4 || max_players < 1) // is this possible?
        {
            System.err.println("handleRoomCreatePacket: max_players not within 1 and 4 (" + max_players + ")");
            // Response - necessary?
            KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        Lobby lobby = KatanaServer.instance().getLobby(pl.getLocation());
        if(lobby == null) // Do we even need to check?
        {
            System.err.println("handleRoomCreatePacket: location id (" + pl.getLocation() + ") is invalid");
            // Response
            KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_CREATE_NO);
            client.sendPacket(response);
            return;
        }
        
        int room_id = lobby.getNextRoomId();
        pl.addToRoom(room_id);
        pl.setRoomLeader(true);
        pl.setClass(class_id);
        
        lobby.addRoom(new GameRoom(room_id, name, difficulty, max_players, pl));
        
        System.err.println("Created new room : " + room_id + " - " + name + " - " + difficulty + " - max:" + max_players);
        KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_CREATE_OK);
        response.addData(room_id + "");
        client.sendPacket(response);
        
        KatanaPacket notify = createRoomListPacket(lobby);
        for(Integer i : lobby.getPlayers())
            KatanaServer.instance().getPlayer(i).sendPacket(notify);
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
        
        destroyRoom(lobby, room);
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
            KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_JOIN_NO);
            client.sendPacket(response);
            return;
        }
        
        Player pl = client.getPlayer();
        Lobby lobby = KatanaServer.instance().getLobby(pl.getLocation());
        GameRoom room = lobby.getRoom(room_id);
        
        System.err.println("Player " + pl + " attempting to join room " + room);
        if(room == null)
        {
            System.err.println("handleRoomJoinPacket: room id (" + room_id + ") is invalid");
            // Response
            KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_JOIN_NO);
            pl.sendPacket(response);
            return;
        }
        
        if((room.getMaxPlayers() - room.getNumPlayers()) == 0)
        {
            System.err.println("handleRoomJoinPacket: room id (" + room_id + ") is full");
            // Response
            KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_JOIN_NO);
            client.sendPacket(response);
            return;
        }
        
        // Response
        KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_JOIN_OK);
        KatanaPacket notify = new KatanaPacket(Opcode.S_ROOM_PLAYER_JOIN);
        notify.addData(pl.getId() + ";" + pl.getName() + ";" + class_id);
        for(Integer i : room.getPlayers())
        {
            Player p = KatanaServer.instance().getPlayer(i);
            response.addData(p.getId() + ";" + p.getName() + ";" + p.getClassId() + ";");
            p.sendPacket(notify);
        }
        lobby.removePlayer(pl);
        room.addPlayer(pl);
        pl.addToRoom(room_id);
        pl.setClass(class_id);
        
        client.sendPacket(response);
    }
    
    public static void destroyRoom(Lobby lobby, GameRoom room)
    {
        if(lobby == null || room == null)
        {
            System.err.println("Attempted to destroy room but room or lobby is null!");
            return;
        }
        
        Player leader = KatanaServer.instance().getPlayer(room.getLeader());
        KatanaPacket response = new KatanaPacket(Opcode.S_ROOM_DESTROY);
        for(Integer i : room.getPlayers())
        {
            Player p = KatanaServer.instance().getPlayer(i);
            p.removeFromRoom();
            p.setRoomLeader(false);
            p.setLocation(lobby.getLocationId());
            lobby.addPlayer(p);
            p.sendPacket(response);
        }
        
        lobby.removeRoom(room.getId());
        leader.setRoomLeader(false);
    }
    
    public static void handleRoomLeavePacket(KatanaClient client)
    {
        System.out.println("handleRoomLeavePacket: INCOMPLETE");
        
        Player pl = KatanaServer.instance().getPlayer(client.getId());
        int room_id = pl.getRoom();
        GameRoom room = KatanaServer.instance().getLobby(pl.getLocation()).getRoom(room_id);
        if(room == null)
        {
            System.err.println("handleRoomLeave: Player " + pl.getName() + " attempted to laeve null room!");
            return;
        }
        
        if(pl.isRoomLeader())
        {
            destroyRoom(KatanaServer.instance().getLobby(pl.getLocation()), room);
            return;
        }
        
        room.removePlayer(pl);
        pl.removeFromRoom();

        KatanaPacket notify = new KatanaPacket(Opcode.S_ROOM_PLAYER_LEAVE);
        notify.addData(pl.getId() + "");
        for(Integer i : room.getPlayers())
            KatanaServer.instance().getPlayer(i).sendPacket(notify);
    }
    
    private static KatanaPacket createRoomListPacket(Lobby lobby)
    {
        if(lobby == null)
        {
            System.err.println("Cannot create room list packet for null lobby");
            return null;
        }
        
        KatanaPacket packet = new KatanaPacket(Opcode.S_ROOM_LIST);
        packet.addData(lobby.getName());
        for(int room_id : lobby.getRoomIds())
        {
            GameRoom room = lobby.getRoom(room_id);
            packet.addData(room.getId() + ";" + room.getName() + ";" + room.getDifficulty() + ";" + room.getMaxPlayers() + ";");
        }
        
        return packet;
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
                KatanaPacket response = new KatanaPacket(Opcode.S_BAD_LOCATION);
                client.sendPacket(response);
                return;
            }
            
            Player pl = KatanaServer.instance().getPlayer(client.getId());
            Lobby old = KatanaServer.instance().getLobby(pl.getLocation());
            if(old != null)
                old.removePlayer(pl);
            pl.setLocation(loc_id);
            lobby.addPlayer(pl);
            
            KatanaPacket response = createRoomListPacket(lobby);
            client.sendPacket(response);
        }
        catch(NumberFormatException ex)
        {
            System.err.println("handleRoomListPacket: Received bad data");
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
        GameRoom room = KatanaServer.instance().getLobby(pl.getLocation()).getRoom(room_id);
        
        if(room == null)
        {
            System.err.println("handleClassChange: Player " + pl.getName() + " attempted to change class but not in a room!");
            return;
        }
        KatanaPacket notify = new KatanaPacket(Opcode.S_PLAYER_UPDATE_CLASS);
        notify.addData(pl.getId() + "");
        notify.addData(class_id + "");
        for(Integer i : room.getPlayers())
        {
            Player p = KatanaServer.instance().getPlayer(i);
            if(p != pl)
                p.sendPacket(notify);
        }
        
        KatanaPacket response = new KatanaPacket(Opcode.S_CLASS_CHANGE_OK);
        client.sendPacket(response);
    }
    
    // Location already stored in Player
    public static void handleLeaderboardPacket(KatanaClient client, KatanaPacket packet)
    { 
        int location = client.getPlayer().getLocation();
        if(location == -1)
        {
            System.err.println("Player " + client.getPlayer() + " attempted to get leaderboard but is not in a lobby!");
            return;
        }
        KatanaPacket response = new KatanaPacket(Opcode.S_LEADERBOARD);
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().runLeaderboardQuery(location);
        if(results != null && !results.isEmpty())
            for(HashMap map : results)
                response.addData(map.get("username") + ";" + map.get("points") + ";");
        client.sendPacket(response);
    }
    
    public static void handleGameStartPacket(KatanaClient client, KatanaPacket packet)
    {
        Player pl = client.getPlayer();
        
        Lobby lobby = KatanaServer.instance().getLobby(pl.getLocation());
        GameRoom room = lobby.getRoom(pl.getRoom());
        
        
        ArrayList<MapTemplate> templates = SQLCache.getMapsByLocation(pl.getLocation());
        // Pick a random template
        MapTemplate template = templates.get(new Random(System.currentTimeMillis()).nextInt(templates.size()));
        Map instance = new Map(template.getId(), room.getDifficulty());
        
        KatanaPacket response = new KatanaPacket(Opcode.S_GAME_START);
        for(int i : room.getPlayers())
        {
            Player p = KatanaServer.instance().getPlayer(i);
            instance.addPlayer(p);
            p.removeFromRoom();
            p.setRoomLeader(false);
            p.addToMap(instance.getGUID());
            p.sendPacket(response);
        }

        KatanaServer.instance().addMap(instance.getGUID(), instance);
       
        room.clearPlayers();
        lobby.removeRoom(room.getId());
    }
    
    
    public static void handleGameReadyPacket(KatanaClient client, KatanaPacket packet)
    {
        Player pl = client.getPlayer();
        
        if(pl.getMap() == -1)
        {
            System.err.println("Player " + client.getPlayer() + " sent game ready packet but is not in map!");
            return;
        }
        
        // map set width/height from camera size
        Map instance = KatanaServer.instance().getMap(pl.getMap());
        if(instance == null)
        {
            System.err.println("INVALID MAP");
            return;
        }
        
        KatanaPacket response = new KatanaPacket(Opcode.S_GAME_POPULATE);
        response.addData(instance.getBackground());
        for(int i : instance.getPlayers())
        {
            Player p = KatanaServer.instance().getPlayer(i);
            response.addData(p.getId() + ";" + p.getMaxHealth() + ";" + SQLCache.getModel(p.getModelId()) + ";");
        }
        
        pl.sendPacket(response);
    }
    
}
