package server.game;

import server.handlers.GameHandler;
import server.shared.KatanaPacket;
import server.shared.Opcode;

public abstract class Unit
{
    private int id;
    private String name;
    
    private int cur_health;
    private int max_health;
    
    private int atk_speed;
    private int atk_damage;
    private int attack_timer;
    
    private float move_speed;
    
    private int map_id;
    private float pos_x;
    private float pos_y;
    
    private int model_id;
    
    private Unit current_target;
    
    private final int TEMP_BASIC_DAMAGE = 100;
    
    public Unit(int id, String name, int max_health, int atk_speed, int atk_damage, float move_speed, int model_id)
    {
        this.id = id;
        this.name = name;
        
        this.cur_health = max_health;
        this.max_health = max_health;
        this.atk_speed  = atk_speed;
        this.atk_damage = atk_damage;
        this.move_speed = move_speed;
        this.model_id   = model_id;
        
        this.attack_timer = atk_speed;
        
        this.current_target = null;
        
        this.map_id = -1;
        this.pos_x  = 0.0f;
        this.pos_y  = 0.0f;
        
        //System.out.println("UNIT [" + id + ", " + name + "]");
    }
    
    public void setId(int id)        { this.id = id; }
    public int getId()               { return id; }
    public void setName(String name) { this.name = name; }
    public String getName()          { return name; }
    
    public void setHealth(int health) { this.cur_health = health; }
    public int getHealth()            { return cur_health; }
    public void setMaxhealth(int max) { this.max_health = max; }
    public int getMaxHealth()         { return max_health; }
    
    public void setAttackSpeed(int speed)   { this.atk_speed = speed; }
    public int getAttackSpeed()             { return atk_speed;  }
    public void setAttackDamage(int damage) { this.atk_damage = damage; }
    public int getAttackDamage()            { return atk_damage; }
    
    public void setSpeed(float speed) { this.move_speed = speed; }
    public float getMoveSpeed()       { return move_speed; }
    
    public void moveTo(float x, float y) 
    {
        this.pos_x = x; 
        this.pos_y = y;
        
        KatanaPacket packet = new KatanaPacket(Opcode.S_GAME_UPDATE_MOVE);
        packet.addData(id + "");
        packet.addData(pos_x + "");
        packet.addData(pos_y + "");
        Map map = GameHandler.instance().getMap(map_id);
        if(map != null)
            map.broadcastPacketToAll(packet, id);
    }
    public void setPosition(float x, float y) { this.pos_x = x; this.pos_y = y; }
    public float getX()                  { return pos_x; }
    public float getY()                  { return pos_y; }
    public void addToMap(int map)        { this.map_id = map; }
    public int getMap()                  { return map_id; }
    
    public void setModelId(int model) { this.model_id = model; }
    public int getModelId()           { return model_id;   }
    
    public void setCurrentTarget(Unit target) { this.current_target = target; }
    public Unit getCurrentTarget()            { return current_target; }
    
    @Override
    public String toString()
    {
        return id + " - " + name;
    }
    
    public int onHealReceived(int heal, Unit healer)    { return heal; }
    public int onDamageTaken(int damage, Unit attacker) { return damage; }
    public abstract int onDamageDeal(int damage, Unit target, Spell spell, boolean is_auto_attack);
    public abstract void onSpellHit(Spell spell, Unit caster);
    public abstract void onSpellCast(Spell spell, Unit target);
    public abstract void onDeath(Unit killer);
    
    public boolean castSpell(Spell spell, Unit target)
    {
        if(spell == null || target == null)
            return false;
        
        onSpellCast(spell, target);
        target.onSpellHit(spell, this);
        int damage = spell.getDamage();
        if(damage < 0) // it's a heal!
        {
            damage = onHealReceived(damage * -1, this);
            int health = target.getHealth() + damage;
            if(health > target.getMaxHealth())
                target.setHealth(target.getMaxHealth());
            else
                target.setHealth(health);
        }
        else
        {
            damage = onDamageDeal(damage, target, spell, false);
            dealDamage(damage, target);
        }
        
        KatanaPacket packet = new KatanaPacket(Opcode.S_GAME_SPELL_CAST);
        packet.addData(id + "");
        packet.addData(target.getId() + "");
        packet.addData(spell.getId() + "");
        packet.addData(damage + "");
        // Map->sendPacket(packet);
        
        return true;
    }
    
    public void dealDamage(int damage, Unit target)
    {
        target.onDamageTaken(damage, this);
        int health = target.getHealth() - damage;
        if(health <= 0)
        {
            target.setHealth(0);
            target.onDeath(this);
            // TODO: Implement death
        }
        target.setHealth(health);
    }
    
   /* public void doAutoAttackIfReady(int diff)
    {
        if(current_target == null)
            return;
        
        if(attack_timer <= diff)
        {
            int damage = TEMP_BASIC_DAMAGE;
            onDamageDeal(damage, current_target, null, true);
            dealDamage(damage, current_target);
            attack_timer = atk_speed;
        }else attack_timer -= diff;
    }*/
    
    public void update(int diff)
    {
        //doAutoAttackIfReady(diff);
    }
}
