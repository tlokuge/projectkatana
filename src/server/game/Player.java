package server.game;

import server.communication.KatanaClient;
import server.handlers.GameHandler;
import server.handlers.PacketHandler;
import server.limbo.GameRoom;
import server.limbo.Lobby;
import server.shared.KatanaPacket;
import server.templates.PlayerClassTemplate;
import server.utils.SQLCache;

public class Player extends Unit
{
    private KatanaClient client;
    private PlayerClass m_class;
    
    private int class_id;
    
    private int location;
    
    private int room_id;
    
    private boolean is_room_leader;
    
    private int points;
    
    public Player(int id, String name, int max_health, int atk_speed, int atk_damage, float move_speed, int model_id, KatanaClient client)
    {
        super(id, name, max_health, atk_speed, atk_damage, move_speed, model_id);
        
        this.client = client;
        m_class = null;
        
        class_id = -1;
        
        location = -1;
    
        room_id = -1;
        
        points = 0;
        
        is_room_leader = false;
    }
    
    public KatanaClient getClient() { return client; }
    
    public void sendPacket(KatanaPacket packet)
    {
        client.sendPacket(packet);
    }
    
    public void logout()
    {
        if(room_id > 0)
        {
            Lobby lobby = GameHandler.instance().getLobby(getLocation());
            GameRoom room = lobby == null ? null : lobby.getRoom(room_id);
            if(room != null)
            {
                if(is_room_leader)
                    PacketHandler.destroyRoom(lobby, room);
                else
                    PacketHandler.handleRoomLeavePacket(client);
            }
        }
        
        Map map = GameHandler.instance().getMap(getMap());
        if(map != null)
            map.removePlayer(getId());
        
        GameHandler.instance().removePlayer(getId());
    }
    
    public void setClass(int class_id) 
    {
        this.class_id = class_id;
        PlayerClassTemplate template = SQLCache.getClass(class_id);
        m_class = new PlayerClass(template);
        setModelId(template.getModelId());
    }
    public int getClassId()                { return class_id; }
    public int getSpellCooldown(int spell) { return m_class.getSpellById(spell).getCooldown(); }
    public boolean isSpellReady(int spell) { return m_class.getSpellById(spell).getCooldown() == 0; }
    
    public void setLocation(int id)     { this.location = id; }
    public int getLocation()            { return location; }
    
    public void addToRoom(int room)    { this.room_id = room; }
    public void removeFromRoom()       { this.room_id = -1; }
    public int getRoom()               { return room_id; }
    public void setRoomLeader(boolean leader) { this.is_room_leader = leader; }
    public boolean isRoomLeader()             { return is_room_leader; }
    
    public void setPoints(int points) { this.points = points; }
    public void addPoints(int add)    { this.points += add; }
    public int getPoints()            { return points; }
    
    @Override
    public void moveTo(float mx, float my)
    {
        super.moveTo(mx, my);
        
        Map map = GameHandler.instance().getMap(getMap());
        if(map == null)
            return;
        
        map.notifyMovement(this);
    }
    
    @Override
    public void update(int diff)
    {
        super.update(diff);
        if(m_class == null)
            return;
        
        m_class.update(diff);
    }

    @Override
    public int onHealReceived(int heal, Unit healer) 
    {
//        KatanaPacket packet = new KatanaPacket(-1, Opcode.S_UPDATE_DAMAGE_TAKEN);
//        packet.addData(getId() + "");
//        packet.addData(heal + "");
        // broadcastToMap(packet);
        return heal;
    }

    @Override
    public int onDamageTaken(int damage, Unit attacker) 
    {
//        KatanaPacket packet = new KatanaPacket(-1, Opcode.S_UPDATE_DAMAGE_TAKEN);
//        packet.addData(getId() + "");
//        packet.addData(damage + "");
        // broadcastToMap(packet);
        return damage;
    }

    @Override
    public void onSpellHit(Spell spell, Unit caster) 
    {
        // Anything necessary?
    }
    
    @Override
    public void onSpellCast(Spell spell, Unit target)
    {
        spell.setCooldown(spell.getCooldown());
    }
    
    @Override
    public void onDeath(Unit killer)
    {
        // TODO
    }

    @Override
    public int onDamageDeal(int damage, Unit target, Spell spell, boolean is_auto_attack) 
    {
        if(spell == null || is_auto_attack)
        {
            /* This was supposed to be for auto attacks - not dealing with that for now
            KatanaPacket packet = new KatanaPacket(-1, Opcode.S_UPDATE_DAMAGE_DONE);
            packet.addData(target.getId() + "");
            packet.addData(damage + "");
            // broadcastToMap(packet);*/
        }
        
        return damage;
    }
}
