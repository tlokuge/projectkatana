package server.limbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import server.game.Player;
import server.handlers.GameHandler;
import server.shared.KatanaPacket;

public class Lobby 
{
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private double radius;
    
    private ArrayList<Integer> players;
    private HashMap<Integer, GameRoom> rooms;
    
    private int nextRoomId;
    
    public Lobby(int id, String name, double latitude, double longitude, double radius)
    {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        
        this.nextRoomId = 0;
        
        players = new ArrayList<Integer>();
        rooms   = new HashMap<Integer, GameRoom>();
    }
    
    public int getLocationId()    { return id; }
    
    public synchronized void addPlayer(Player pl)      { players.add(pl.getId()); }
    public synchronized void removePlayer(Player pl)   { players.remove((Object)pl.getId()); }
    public synchronized ArrayList<Integer> getPlayers() { return players; }
    
    public synchronized void addRoom(GameRoom room)    
    {
        if(room == null)
        {
            System.err.println("Lobby " + getLocationId() + " attempted to add null room");
            return;
        }
        rooms.put(room.getId(), room);
        System.out.println("Lobby: " + name + " (" + id + ") added room: " + room.getId() + " - " + room.getName());
    }
    public synchronized void removeRoom(int room) { rooms.remove(room); }
    public synchronized GameRoom getRoom(int room){ return rooms.get(room);  }
    public synchronized Set<Integer> getRoomIds() { return rooms.keySet(); }
    
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }
    public double getRadius()    { return radius; }
    public String getName()      { return name; }
    public int getNextRoomId()   { return ++nextRoomId; }
    
    public synchronized void broadcastToLobby(KatanaPacket packet)
    {
        if(packet == null)
            return;
        
        synchronized(players)
        {
            for(int pid : players)
            {
                Player p = GameHandler.instance().getPlayer(pid);
                if(p != null)
                    p.sendPacket(packet);
            }
        }
    }
    
    public String toString()
    {
        return id + " - " + name + " [" + latitude + "," + longitude + "] <>" + radius;
    }
}
