package server.game;

import server.templates.PlayerClassTemplate;
import server.utils.SQLCache;

public class PlayerClass
{
    private int id;
    private Spell spell1;
    private Spell spell2;
    private Spell spell3;
    private Spell spell4;
    
    public PlayerClass(PlayerClassTemplate template)
    {
        this.id = template.getId();
        this.spell1 = new Spell(SQLCache.getSpell(template.getSpell1()));
        this.spell2 = new Spell(SQLCache.getSpell(template.getSpell2()));
        this.spell3 = new Spell(SQLCache.getSpell(template.getSpell3()));
        this.spell4 = new Spell(SQLCache.getSpell(template.getSpell4()));
    }
    
    public int getId()       { return id; }
    public Spell getSpell1() { return spell1; }
    public Spell getSpell2() { return spell2; }
    public Spell getSpell3() { return spell3; }
    public Spell getSpell4() { return spell4; }
    
    public Spell getSpellById(int spell)
    {
        if(spell1.getId() == spell)
            return spell1;
        else if(spell2.getId() == spell)
            return spell2;
        else if(spell3.getId() == spell)
            return spell3;
        else if(spell4.getId() == spell)
            return spell4;
        
        return null;
    }
    
    public void cooldown_spell(Spell spell, int diff)
    {
        if(spell != null && spell.getCooldown() > 0)
        {
            spell.setCooldown(spell.getCooldown() - diff);
            if(spell.getCooldown() <= diff)
                spell1.setCooldown(0);
        }
    }
    
    public void update(int diff)
    {
        cooldown_spell(spell1, diff);
        cooldown_spell(spell2, diff);
        cooldown_spell(spell3, diff);
        cooldown_spell(spell4, diff);
    }
}
