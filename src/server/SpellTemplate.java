package server;

public class SpellTemplate
{
    private int spell_id;
    private String spell_name;
    private int damage;
    private int cooldown;

    public SpellTemplate(int spell_id, String spell_name, int damage, int cooldown)
    {
        this.spell_id = spell_id;
        this.spell_name = spell_name;
        this.damage = damage;
        this.cooldown = cooldown;
    }

    public int getId()      { return spell_id; }
    public String getName() { return spell_name; }
    public int getDamage()  { return damage; }
    public int getCooldown(){ return cooldown; }
}
