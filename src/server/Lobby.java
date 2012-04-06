package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Lobby 
{
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private double radius;
    
    private ArrayList<Player> players;
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
    }
    
    public int getLocationId()    { return id; }
    
    public void addPlayer(Player pl)      
    {
        if(players == null)
            players = new ArrayList<Player>();
        players.add(pl); 
    }
    public void removePlayer(Player pl)   { players.remove(pl); }
    public ArrayList<Player> getPlayers() { return players; }
    
    public void addRoom(GameRoom room)    
    {
        if(rooms == null)
            rooms = new HashMap<Integer, GameRoom>();
        rooms.put(room.getId(), room);
        System.out.println("Lobby: " + name + " (" + id + ") added room: " + room.getId() + " - " + room.getName());
    }
    public void removeRoom(int room) { rooms.remove(room); }
    public GameRoom getRoom(int room){ return rooms.get(id);  }
    public Set<Integer> getRoomIds() 
    { 
        if(rooms == null)
            return null;
        return rooms.keySet(); 
    }
    
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }
    public double getRadius()    { return radius; }
    public String getName()      { return name; }
    public int getNextRoomId()   { return nextRoomId++; }
    
    public String toString()
    {
        return id + " - " + name + " [" + latitude + "," + longitude + "] <>" + radius;
    }
}
