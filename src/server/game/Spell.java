package server.game;

import server.templates.SpellTemplate;

public class Spell
{
    private int id;
    private int current_cooldown;
    private int cooldown_timer;
    private int damage;
    
    public Spell(SpellTemplate template)
    {
        this.id = template.getId();
        this.current_cooldown = 0;
        this.cooldown_timer = template.getCooldown();
        this.damage = template.getDamage();
    }
    
    public Spell(int id, int cooldown, int damage)
    {
        this.id = id;
        this.current_cooldown = 0;
        this.cooldown_timer = cooldown;
        this.damage = damage;
    }
    
    public int getId()       { return id; }
    
    public void setCooldown(int cd) { this.current_cooldown = cd; }
    public int getCooldown()        { return current_cooldown; }
    public int getCooldownTimer()   { return cooldown_timer; }
    
    public int getDamage()   { return damage; }
}
