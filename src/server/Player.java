package server;

import shared.KatanaPacket;

public class Player extends Unit
{
    private KatanaClient client;
    private int class_id;
    
    public Player(int id, String name, int max_health, int atk_speed, int atk_damage, float move_speed, int model_id, KatanaClient client)
    {
        super(id, name, max_health, atk_speed, atk_damage, move_speed, model_id);
        
        this.client = client;
    }
    
    public void sendPacket(KatanaPacket packet)
    {
        client.sendPacket(packet);
    }
    
    public void setClass(int class_id) { this.class_id = class_id; }
    public int getClassId()            { return class_id; }
}