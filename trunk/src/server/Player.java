package server;

import shared.KatanaPacket;
import shared.Opcode;

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
    
    public KatanaClient getClient() { return client; }
    
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
    
    @Override
    public void update(int diff)
    {
        super.update(diff);
        if(m_class == null)
            return;
        
        m_class.update(diff);
    }

    @Override
    public void onHealReceived(int heal, Unit healer) 
    {
        KatanaPacket packet = new KatanaPacket(-1, Opcode.S_UPDATE_DAMAGE_TAKEN);
        packet.addData(getId() + "");
        packet.addData(heal + "");
        // broadcastToMap(packet);
    }

    @Override
    public void onDamageTaken(int damage, Unit attacker) 
    {
        KatanaPacket packet = new KatanaPacket(-1, Opcode.S_UPDATE_DAMAGE_TAKEN);
        packet.addData(getId() + "");
        packet.addData(damage + "");
        // broadcastToMap(packet);
    }

    @Override
    public void onSpellHit(Spell spell, Unit caster) 
    {
        // Anything necessary?
    }
    
    @Override
    public void onSpellCast(Spell spell, Unit target)
    {
        KatanaPacket packet = new KatanaPacket(-1, Opcode.S_UPDATE_SPELL);
        packet.addData(getId() + "");
        packet.addData(spell.getId() + "");
        // broadcastToMap(packet);
    }
    
    @Override
    public void onDeath(Unit killer)
    {
        // TODO
    }

    @Override
    public void onDamageDeal(int damage, Unit target, Spell spell, boolean is_auto_attack) 
    {
        if(spell == null)
        {
            KatanaPacket packet = new KatanaPacket(-1, Opcode.S_UPDATE_DAMAGE_DONE);
            packet.addData(target.getId() + "");
            packet.addData(damage + "");
            // broadcastToMap(packet);
        }
    }
}