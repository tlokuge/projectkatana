package server;

public abstract class Unit
{
    private int id;
    private String name;
    
    private int cur_health;
    private int max_health;
    
    private int atk_speed;
    private int atk_damage;
    
    private float move_speed;
    
    private int model_id;
    
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
    }
    
    public void setId(int id)        { this.id = id; }
    public int getId()               { return id; }
    public void setName(String name) { this.name = name; }
    public String getName()          { return name; }
    
    public void setHealth(int health) { this.cur_health = health; }
    public int getCurrentHealth()     { return cur_health; }
    public int getMaxHealth()         { return max_health; }
    
    public void setAttackSpeed(int speed)   { this.atk_speed = speed; }
    public int getAttackSpeed()             { return atk_speed;  }
    public void setAttackDamage(int damage) { this.atk_damage = damage; }
    public int getAttackDamage()            { return atk_damage; }
    
    public void setSpeed(float speed) { this.move_speed = speed; }
    public float getMoveSpeed()       { return move_speed; }
    
    public void setModelId(int model) { this.model_id = model; }
    public int getModelId()           { return model_id;   }
}
