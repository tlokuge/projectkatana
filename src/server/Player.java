package server;

import shared.KatanaPacket;

public class Player extends Unit
{
    private KatanaClient client;
    private PlayerClass m_class;
    
    private int class_id;
    
    private int location;
    
    private int room_id;
    private boolean is_room_leader;
    
    public Player(int id, String name, int max_health, int atk_speed, int atk_damage, float move_speed, int model_id, KatanaClient client)
    {
        super(id, name, max_health, atk_speed, atk_damage, move_speed, model_id);
        
        this.client = client;
        m_class = null;
        
        class_id = -1;
        
        location = -1;
    
        room_id = -1;
        is_room_leader = false;
    }
    
    public void sendPacket(KatanaPacket packet)
    {
        client.sendPacket(packet);
    }
    
    public void setClass(int class_id) 
    {
        this.class_id = class_id;
        m_class = new PlayerClass(SQLCache.getClass(class_id));
    }
    public int getClassId()            { return class_id; }
    public boolean isSpellReady(int spell) { return m_class.getSpellById(spell).getCooldown() == 0; }
    
    public void setLocation(int id)     { this.location = id; }
    public int getLocation()            { return location; }
    
    public void addToRoom(int room)    { this.room_id = room; }
    public void removeFromRoom()       { this.room_id = -1; }
    public int getRoom()               { return room_id; }
    public void setRoomLeader(boolean leader) { this.is_room_leader = leader; }
    public boolean isRoomLeader()             { return is_room_leader; }
    
    public void update(int diff)
    {
        if(m_class == null)
            return;
        
        m_class.update(diff);
    }
}
