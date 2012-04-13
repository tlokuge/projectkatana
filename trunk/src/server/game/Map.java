package server.game;

import server.utils.SQLCache;
import server.templates.MapTemplate;
import server.communication.KatanaServer;
import java.util.ArrayList;
import java.util.HashMap;
import server.shared.KatanaPacket;
import server.shared.Opcode;

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
    
    private int interval;
    private final int UPDATE_INTERVAL = 2500;
    
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
        
        interval = UPDATE_INTERVAL;
    }
    
    public static int getNextMapGUID() { return NEXT_MAP_GUID++; }
    
    public int getMapId()           { return id; }
    public int getGUID()            { return guid; }
    public int getLocationId()      { return location_id; }
    public int getDifficulty()      { return difficulty; }
    public String getName()         { return name; }
    public String getBackground()   { return background; }
    
    public void addPlayer(Player pl) { player_list.add(pl.getId()); }
    public ArrayList<Integer> getPlayers() { return player_list; }
    
    public Creature spawnCreature(int id)
    {
        Creature creature = new Creature(id);
        creature_map.put(creature.getGUID(), creature);
        return creature;
    }
    
    public void update(int diff)
    {
        for(int id : player_list)
        {
            Player p = KatanaServer.instance().getPlayer(id);
            if(p != null)
                p.update(diff);
        }
        
        if(interval < UPDATE_INTERVAL)
        {
            sendSyncPacket();
            interval = UPDATE_INTERVAL;
        }else interval -= diff;
        
        for(int id : creature_map.keySet())
            creature_map.get(id).update(diff);
    }
    
    public void broadcastPacketToAll(KatanaPacket packet, int ignore_player_id)
    {
        if(packet == null)
        {
            System.err.println("Attempted to broadcast NULL packet");
            return;
        }
        
        for(int pid : player_list)
        {
            Player p = KatanaServer.instance().getPlayer(pid);
            if(p != null && pid != ignore_player_id)
                p.sendPacket(packet);
        }
    }
    
    private void sendSyncPacket()
    {
        KatanaPacket packet = new KatanaPacket(Opcode.S_GAME_UPDATE_SYNC);
        
        for(int pid : player_list)
        {
            Player p = KatanaServer.instance().getPlayer(pid);
            if(p != null)
                packet.addData(p.getId() + ";" + 
                        p.getHealth() + ";" + p.getMaxHealth() + ";" + 
                        p.getX() + ";" + p.getY() + ";" + 
                        SQLCache.getModel(p.getModelId()) + ";");
        }
        
        for(int c_guid : creature_map.keySet())
        {
            Creature c = creature_map.get(c_guid);
            if(c != null)
                packet.addData(c.getId() + ";" + 
                        c.getHealth() + ";" + c.getMaxHealth() + ";" +
                        c.getX() + ";" + c.getY() + ";" +
                        SQLCache.getModel(c.getModelId()) + ";");
        }
        
        broadcastPacketToAll(packet, -1);
    }
}
