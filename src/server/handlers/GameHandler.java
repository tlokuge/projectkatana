package server.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import server.game.Map;
import server.game.Player;
import server.limbo.Lobby;

public class GameHandler
{
    private HashMap<Integer, Player> players;
    private HashMap<Integer, Lobby> lobbies;
    private HashMap<Integer, Map> maps;
    
    private ArrayList<Integer> removePlayerList;
    private ArrayList<Integer> removeMapList;
    
    private Random randGenerator;
    
    private static GameHandler instance;
    
    private int NEXT_GUID = 500000;
    
    private GameHandler()
    {
        players        = new HashMap<Integer, Player>();
        lobbies        = new HashMap<Integer, Lobby>();
        maps           = new HashMap<Integer, Map>();
        
        removePlayerList = new ArrayList<Integer>();
        removeMapList    = new ArrayList<Integer>();
        
        randGenerator  = new Random(System.currentTimeMillis());
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("DON'T CLONE SINGLETONS D<");
    }
    
    public synchronized static GameHandler instance()
    {
        if(instance == null)
            instance = new GameHandler();
        
        return instance;
    }
    
    public void addPlayer(int id, Player player){ if(!containsPlayer(id)) players.put(id, player); }
    public Player getPlayer(int id)             { return players.get(id); }
    public boolean containsPlayer(Player p)     { return containsPlayer(p.getId()); }
    public boolean containsPlayer(int id)       { return players.containsKey(id); }
    public void removePlayer(int id)            { removePlayerList.add(id); }
    public HashMap<Integer, Player> getPlayers(){ return players; }
    
    public void safelyRemovePlayers()
    {
        if(removePlayerList.isEmpty())
            return;
        
        for(Integer i : removePlayerList)
            players.remove(i);
    }
    
    public void addLobby(int id, Lobby lobby)   { lobbies.put(id, lobby); }
    public Lobby getLobby(int id)               { return lobbies.get(id); }
    public HashMap<Integer, Lobby> getLobbies() { return lobbies; }
    public Set<Integer> getLocationIDs()        { return lobbies.keySet(); }
    
    public void addMap(int id, Map map)         { maps.put(id, map); }
    public void removeMap(int id)               { removeMapList.add(id); }
    public Map getMap(int id)                   { return maps.get(id); }
    public HashMap<Integer, Map> getMaps()      { return maps; }
    
    public void safelyRemoveMaps()
    {
        if(removeMapList.isEmpty())
            return;
        
        for(Integer i : removeMapList)
            maps.remove(i);
    }
    
    public int getRandInt(int maxInt)  { return randGenerator.nextInt(maxInt); }
    public int getRandInt()            { return randGenerator.nextInt(); }
    public float getRandFloat()        { return randGenerator.nextFloat(); }
    
    public int getNextGUID() { return NEXT_GUID++; }
}
