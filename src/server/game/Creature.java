package server.game;

import server.handlers.AIHandler;
import server.templates.CreatureTemplate;
import server.utils.SQLCache;

public class Creature extends Unit
{
    private int guid;
    private int level;
    
    private CreatureAI ai;
    
    private static int NEXT_GUID = 0;
    
    public Creature(int id)
    {
        super(id, "", 0, 0, 0, 0f, 0);
        
        this.guid = getNextGUID();
        
        CreatureTemplate template = SQLCache.getCreature(id);
        this.setName(template.getCreatureName());
        this.setMaxhealth(template.getHealth());
        this.setLevel(template.getLevel());
        this.setAttackSpeed(template.getAttackSpeed());
        this.setAttackDamage(template.getAttackDamage());
        this.setSpeed(template.getMoveSpeed());
        this.setModelId(template.getModelId());
        this.ai = loadAI(template.getScript());
    }
    
    private CreatureAI loadAI(String script_name)
    {
        return AIHandler.instance().getAIByName(script_name, this);
    }
    
    public static int getNextGUID() { return NEXT_GUID++; }
    
    public int getGUID() { return guid; }
    
    public void setLevel(int level) { this.level = level; }
    public int getLevel()           { return level; }

    @Override
    public void update(int diff)
    {
        super.update(diff);
        ai.updateAI(diff);
    }
    
    @Override
    public int onHealReceived(int heal, Unit healer) 
    {
        return heal;
    }

    @Override
    public int onDamageTaken(int damage, Unit attacker) 
    {
        return damage;
    }

    @Override
    public int onDamageDeal(int damage, Unit target, Spell spell, boolean is_auto_attack) 
    {
        return damage;
    }

    @Override
    public void onSpellHit(Spell spell, Unit caster) {}
    @Override
    public void onSpellCast(Spell spell, Unit target) {}
    @Override
    public void onDeath(Unit killer)  {}
}
