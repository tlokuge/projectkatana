package server.limbo;

import java.util.ArrayList;
import server.game.Player;
import server.shared.Constants;

public class GameRoom 
{
    private int id;
    private String name;
    private int difficulty;
    private int max_players;
    private int leader_id;
    
    private ArrayList<Integer> players;
    
    public GameRoom(int id, String name, int difficulty, int max_players, Player leader)
    {
        this.id         = id;
        this.name       = name;
        this.difficulty = difficulty;
        this.max_players= max_players;
        this.leader_id  = leader.getId();
        
        players = new ArrayList<Integer>();
        addPlayer(leader);
        
        System.out.println(this);
    }
    
    public int getId() { return id; }
    
    public void setName(String name) { this.name = name; }
    public String getName()          { return name; }
    
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public int getDifficulty()                { return difficulty; }
    
    public boolean setMaxPlayers(int max)
    {
        if(max == max_players)
            return true;
        else if(max < 1)
            return false;
        else if(max < players.size())
            return false;
        else if(max > Constants.ROOM_MAX_PLAYERS)
            return false;
        
        this.max_players = max;
        
        return true;
    }
    public int getNumPlayers() { return players.size(); }
    public int getMaxPlayers() { return max_players; }
    
    public void setLeader(int id)  { this.leader_id = id; }
    public int getLeader()         { return leader_id; }
    
    public boolean addPlayer(Player pl)
    {
        if(players.size() == max_players)
            return false;
        players.add(pl.getId());
        return true;
    }
    public void removePlayer(Player pl) { players.remove((Object)pl.getId()); }
    
    public ArrayList<Integer> getPlayers() { return players; }
    
    public String toString()
    {
        return id + " - " + name + " - " + difficulty + "/3 - " + players.size() + "/" + max_players;
    }
}
