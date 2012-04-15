package server.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import server.game.ai.GenericAI;
import server.handlers.GameHandler;
import server.handlers.SQLHandler;
import server.shared.Constants;
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
    private ArrayList<Creature> temp_creature_holder;
    
    private boolean ready;
    
    private int interval;
    private int gameEndTimer;
    
    private int boss_guid;
    
    private int max_x;
    private int max_y;
    
    private final int UPDATE_INTERVAL = 2500;
    private final int DEFAULT_MAX_X = 800;
    private final int DEFAULT_MAX_Y = 480;
    
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
        this.temp_creature_holder = new ArrayList<Creature>();
        
        this.ready = false;
        
        interval        = UPDATE_INTERVAL*2;
        gameEndTimer    = Constants.GAME_END_TIMER;
        
        this.max_x = DEFAULT_MAX_X;
        this.max_y = DEFAULT_MAX_Y;
        
        boss_guid = -1;
        
        spawnRandomCreatureFromTemplate(template);
    }
    
    public void spawnRandomCreatureFromTemplate(MapTemplate template)
    {
        if(template == null)
            return;
        
        ArrayList<Integer> entries = template.getCreatureEntries();
        if(entries == null || entries.isEmpty())
        {
            System.err.println("No creature to spawn on map creation");
            return;
        }
        
        int creature_id = entries.get(GameHandler.instance().getRandInt(entries.size()));
        
        Creature creature = spawnCreature(creature_id, getRandX(), getRandY());
        if(creature != null)
        {
            System.out.println("Spawned creature: " + creature);
            boss_guid = creature.getId();
        }
    }
    
    public static int getNextMapGUID() { return GameHandler.instance().getNextGUID(); }
    
    public int getMapId()           { return id; }
    public int getGUID()            { return guid; }
    public int getLocationId()      { return location_id; }
    public int getDifficulty()      { return difficulty; }
    public String getName()         { return name; }
    public String getBackground()   { return background; }
    public void ready()             { this.ready = true; }
    
    public void setMaxCoords(int mx, int my) { this.max_x = mx; this.max_y = my; }
    public float getMaxX()                   { return max_x; }
    public float getMaxY()                   { return max_y; }
    public float getRandX()                  { return GameHandler.instance().getRandInt(max_x); }
    public float getRandY()                  { return GameHandler.instance().getRandInt(max_y); }
    
    public void addPlayer(Player pl) { player_list.add(pl.getId()); }
    public void removePlayer(int pid)
    {
        player_list.remove((Integer)pid);
        if(player_list.isEmpty())
        {
            creature_map.clear();
            GameHandler.instance().removeMap(guid);
        }
    }
    
    public ArrayList<Integer> getPlayers() { return player_list; }
    
    public Creature spawnCreature(int cid, float pos_x, float pos_y)
    {
        Creature creature = new Creature(cid, guid);
        temp_creature_holder.add(creature);
        creature.setPosition(pos_x, pos_y);
        return creature;
    }
    
    public void despawnCreature(int cguid)
    {
        creature_map.remove((Integer)cguid);
        
        KatanaPacket packet = new KatanaPacket(Opcode.S_GAME_DESPAWN_UNIT);
        packet.addData(cguid + "");
        broadcastPacketToAll(packet, -1);
    }
    
    public Creature getCreature(int cguid) { return creature_map.get(cguid); }
    
    public void notifyMovement(Player pl)
    {
        Creature boss = creature_map.get(boss_guid);
        if(boss == null || pl == null)
            return;
        
        ((GenericAI)boss.getAI()).checkMovement(pl);
    }
    
    private void mergeTempHolder()
    {
        if(temp_creature_holder.isEmpty())
            return;
        
        for(Creature cr : temp_creature_holder)
            creature_map.put(cr.getId(), cr);
        
        temp_creature_holder.clear();
    }
    
    private void clearCreatures()
    {
        temp_creature_holder.clear();
        creature_map.clear();
    }
    
    public void update(int diff)
    {
        if(!ready)
            return;
        
        if(gameEndTimer < diff)
        {
            System.out.println("ENDING GAME");
            KatanaPacket packet = new KatanaPacket(Opcode.S_GAME_END);
            ArrayList<Integer> removeList = new ArrayList<Integer>();
            for(int pid : getPlayers())
            {
                Player pl = GameHandler.instance().getPlayer(pid);
                int points = pl.getPoints();
                if(pl != null)
                    packet.addData(pl.getName() + ";" + points + ";");
                pl.setPoints(0);
                removeList.add(pid);
                // Update the leaderboard in the database
                SQLHandler.instance().runInsertUpdateLeaderboardQuery(pid, location_id, points);
            }
            broadcastPacketToAll(packet, -1);
            for(int pid : removeList)
                removePlayer(pid);
            clearCreatures();
            ready = false;
            GameHandler.instance().removeMap(guid);
        }else gameEndTimer -= diff;
        
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
        
        Iterator<Integer> itr = creature_map.keySet().iterator();
        while(itr.hasNext())
        {
            if(itr == null)
            {
                System.err.println("null itr");
                break;
            }
            
            Creature c = creature_map.get(itr.next());
            if(c != null)
                c.update(diff);
        }
    }
    
    public synchronized void broadcastPacketToAll(KatanaPacket packet, int ignore_player_id)
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
    
    public String getPopulateData()
    {
        String data = "";
        for(int pid : player_list)
        {
            Player p = GameHandler.instance().getPlayer(pid);
            if(p != null)
                data += (p.getId() + ";" + 
                        p.getHealth() + ";" + p.getMaxHealth() + ";" + 
                        p.getX() + ";" + p.getY() + ";" + 
                        SQLCache.getModel(p.getModelId()) + ";\n");
        }
        
        for(int c_guid : creature_map.keySet())
        {
            Creature c = creature_map.get(c_guid);
            if(c != null)
                data += (c.getId() + ";" + 
                        c.getHealth() + ";" + c.getMaxHealth() + ";" +
                        c.getX() + ";" + c.getY() + ";" +
                        SQLCache.getModel(c.getModelId()) + ";\n");
        }
        
        mergeTempHolder();
        
        return data;
    }
    
    private void sendSyncPacket()
    {
        KatanaPacket packet = new KatanaPacket(Opcode.S_GAME_UPDATE_SYNC);
        packet.addData(getPopulateData());
        broadcastPacketToAll(packet, -1);
    }
}
