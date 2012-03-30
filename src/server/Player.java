package server;

import shared.KatanaPacket;

public class Player extends Unit
{
    private KatanaClient client;
    
    private int class_id;
    
    private int room_id;
    private boolean is_room_leader;
    
    public Player(int id, String name, int max_health, int atk_speed, int atk_damage, float move_speed, int model_id, KatanaClient client)
    {
        super(id, name, max_health, atk_speed, atk_damage, move_speed, model_id);
        
        this.client = client;
        
        room_id = -1;
        is_room_leader = false;
    }
    
    public void sendPacket(KatanaPacket packet)
    {
        client.sendPacket(packet);
    }
    
    public void setClass(int class_id) { this.class_id = class_id; }
    public int getClassId()            { return class_id; }
    
    public void addToRoom(int room)    { this.room_id = room; }
    public void removeFromRoom()       { this.room_id = -1; }
    public int getRoom()               { return room_id; }
    public void setRoomLeader(boolean leader) { this.is_room_leader = leader; }
    public boolean isRoomLeader()             { return is_room_leader; }
    
}