package server.game;

import server.handlers.GameHandler;

public abstract class CreatureAI 
{
    protected Creature m_creature;
    
    public CreatureAI(Creature creature)
    {
        this.m_creature = creature;
    }
    
    public Creature summonCreature(int creature_entry, float pos_x, float pos_y)
    {
        Map map = GameHandler.instance().getMap(m_creature.getMap());
        if(map == null)
            return null;
        
        Creature cr = map.spawnCreature(creature_entry, pos_x, pos_y);
        this.onSummon(cr);
        
        return cr;
    }
    
    public Creature getCreatureFromMap(int cguid)
    {
        Map map = GameHandler.instance().getMap(m_creature.getMap());
        if(map == null)
            return null;
        return map.getCreature(cguid);
    }
    
    public void despawnCreature(int cguid)
    {
        Map map = GameHandler.instance().getMap(m_creature.getMap());
        if(map == null)
            return;
        
        map.despawnCreature(cguid);
    }
    
    public void onDamageTaken(Unit attacker, int damage) {}
    public void onDeath(Unit killer) {}
    public void onSummon(Creature summoned) {}
    public void onSpellHit(Unit caster, Spell spell) {}
    public void onSpellCast(Unit target, Spell spell) {}
    
    public void castSpell(Spell spell, Unit target) {}
    
    public abstract void updateAI(int diff);
    public abstract CreatureAI getAI(Creature creature);
}
