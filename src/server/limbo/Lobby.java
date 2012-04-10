package server.limbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import server.game.Player;

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
        
        players = new ArrayList<>();
        rooms   = new HashMap<>();
    }
    
    public int getLocationId()    { return id; }
    
    public void addPlayer(Player pl)      { players.add(pl.getId()); }
    public void removePlayer(Player pl)   { players.remove((Object)pl.getId()); }
    public ArrayList<Integer> getPlayers() { return players; }
    
    public void addRoom(GameRoom room)    
    {
        if(room == null)
        {
            System.err.println("Lobby " + getLocationId() + " attempted to add null room");
            return;
        }
        rooms.put(room.getId(), room);
        System.out.println("Lobby: " + name + " (" + id + ") added room: " + room.getId() + " - " + room.getName());
    }
    public void removeRoom(int room) { rooms.remove(room); }
    public GameRoom getRoom(int room){ return rooms.get(room);  }
    public Set<Integer> getRoomIds() { return rooms.keySet(); }
    
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }
    public double getRadius()    { return radius; }
    public String getName()      { return name; }
    public int getNextRoomId()   { return ++nextRoomId; }
    
    public String toString()
    {
        return id + " - " + name + " [" + latitude + "," + longitude + "] <>" + radius;
    }
}
