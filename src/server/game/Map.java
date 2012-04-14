package server.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import server.handlers.GameHandler;
import server.shared.KatanaPacket;
import server.shared.Opcode;
import server.templates.MapTemplate;
import server.utils.SQLCache;

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
    
    private boolean ready;
    
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
        
        this.player_list  = new ArrayList<Integer>();
        this.creature_map = new HashMap<Integer, Creature>();
        
        this.ready = false;
        
        interval = UPDATE_INTERVAL*2;
        
        spawnRandomCreatureFromTemplate(template);
    }
    
    public void spawnRandomCreatureFromTemplate(MapTemplate template)
    {
        if(template == null)
            return;
        
        ArrayList<Integer> entries = template.getCreatureEntries();
        if(entries == null || entries.isEmpty())
            return;
        
        int creature_id = entries.get(new Random(System.currentTimeMillis()).nextInt(entries.size()));
        
        Creature creature = new Creature(creature_id);
        if(creature != null)
        {
            System.err.println("Spawned creature: " + creature);
            creature_map.put(creature.getGUID(), creature);
            creature.moveTo(50, 50);
            creature.addToMap(guid);
        }
        
        System.err.println("dshjadsk");
    }
    
    public static int getNextMapGUID() { return NEXT_MAP_GUID++; }
    
    public int getMapId()           { return id; }
    public int getGUID()            { return guid; }
    public int getLocationId()      { return location_id; }
    public int getDifficulty()      { return difficulty; }
    public String getName()         { return name; }
    public String getBackground()   { return background; }
    public void ready()             { this.ready = true; }
    
    public void addPlayer(Player pl) { player_list.add(pl.getId()); }
    public void removePlayer(int pid)
    {
        player_list.remove(pid);
        if(player_list.isEmpty())
        {
            creature_map.clear();
            GameHandler.instance().removeMap(guid);
        }
    }
    
    public ArrayList<Integer> getPlayers() { return player_list; }
    
    public Creature spawnCreature(int cid)
    {
        Creature creature = new Creature(cid);
        creature_map.put(creature.getGUID(), creature);
        creature.addToMap(guid);
        return creature;
    }
    
    public void update(int diff)
    {
        if(!ready)
            return;
        
        for(int pid : player_list)
        {
            Player p = GameHandler.instance().getPlayer(pid);
            if(p != null)
                p.update(diff);
        }
        
        if(interval < diff)
        {
            sendSyncPacket();
            interval = UPDATE_INTERVAL;
        }else interval -= diff;
        
        for(int cid : creature_map.keySet())
            creature_map.get(cid).update(diff);
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
            Player p = GameHandler.instance().getPlayer(pid);
            if(p != null && pid != ignore_player_id)
                p.sendPacket(packet);
        }
    }
    
    private void sendSyncPacket()
    {
        KatanaPacket packet = new KatanaPacket(Opcode.S_GAME_UPDATE_SYNC);
        
        for(int pid : player_list)
        {
            Player p = GameHandler.instance().getPlayer(pid);
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
