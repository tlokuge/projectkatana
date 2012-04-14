package server.templates;

public class CreatureTemplate 
{
    private int creature_id;
    private String creature_name;
    private int health;
    private int level;
    private int attack_speed;
    private int attack_damage;
    private float move_speed;
    private int model_id;
    private String script;
    
    public CreatureTemplate(int creature_id, String creature_name, int health, int level, int attack_speed, int attack_damage, float move_speed, int model_id, String script)
    {
        this.creature_id    = creature_id;
        this.creature_name  = creature_name;
        this.health         = health;
        this.level          = level;
        this.attack_speed   = attack_speed;
        this.attack_damage  = attack_damage;
        this.move_speed     = move_speed;
        this.model_id       = model_id;
        this.script         = script;
    }
    
    public int getCreatureId()      { return creature_id; }
    public String getCreatureName() { return creature_name; }
    public int getHealth()          { return health; }
    public int getLevel()           { return level; }
    public int getAttackSpeed()     { return attack_speed; }
    public int getAttackDamage()    { return attack_damage; }
    public float getMoveSpeed()     { return move_speed; }
    public int getModelId()         { return model_id; }
    public String getScript()       { return script; }
}
