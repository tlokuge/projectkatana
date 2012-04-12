package server.game;

import server.utils.SQLCache;
import server.templates.MapTemplate;
import server.communication.KatanaServer;
import java.util.ArrayList;
import java.util.HashMap;

public class Map
{
    private int id;
    private int guid;
    private int location_id;
    private int difficulty;
    
    private String name;
    private String background;
    
    private ArrayList<Integer> player_list;
    private HashMap<Integer, Creature> creature_map;
    
    private static int NEXT_MAP_GUID = 0;
    
    public Map(int id, int difficulty)
    {
        MapTemplate template = SQLCache.getMap(id);
        
        this.id          = template.getId();
        this.guid        = getNextMapGUID();
        this.location_id = template.getLocation();
        this.difficulty  = difficulty;
        this.name        = template.getName();
        this.background  = template.getBackground();
        
        this.player_list  = new ArrayList<>();
        this.creature_map = new HashMap<>();
    }
    
    public static int getNextMapGUID() { return NEXT_MAP_GUID++; }
    
    public int getMapId()           { return id; }
    public int getGUID()            { return guid; }
    public int getLocationId()      { return location_id; }
    public int getDifficulty()      { return difficulty; }
    public String getName()         { return name; }
    public String getBackground()   { return background; }
    
    public void addPlayer(Player pl) { player_list.add(pl.getId()); }
    
    public Creature spawnCreature(int id)
    {
        Creature creature = new Creature(id);
        creature_map.put(creature.getGUID(), creature);
        return creature;
    }
    
    public void update(int diff)
    {
        for(int id : player_list)
            KatanaServer.instance().getPlayer(id).update(diff);
        
        for(int id : creature_map.keySet())
            creature_map.get(id).update(diff);
    }
}
