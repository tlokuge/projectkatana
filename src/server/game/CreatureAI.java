package server.game;

import server.game.ai.DoNothingAI;
import server.game.ai.GenericAI;
import server.handlers.GameHandler;

public abstract class CreatureAI 
{
    protected Creature m_creature;
    protected Map m_instance;
    
    public CreatureAI(Creature creature)
    {
        System.out.println("Creating CreatureAI - MapID: " + creature.getMap());
        this.m_creature = creature;
        this.m_instance = GameHandler.instance().getMap(m_creature.getMap());
    }
    
    public static CreatureAI getAIByName(String name, Creature creature)
    {
        if(name.equalsIgnoreCase("GenericAI"))
            return new GenericAI(creature);
        if(name.equalsIgnoreCase("DoNothingAI"))
            return new DoNothingAI(creature);
        
        // By default, return the idle AI
        return new DoNothingAI(creature);
    }
    
    public Creature summonCreature(int creature_entry)
    {
        float px = m_instance.getRandX();
        float py = m_instance.getRandY();
        return summonCreature(creature_entry, px, py);
    }
    
    public Creature summonCreature(int creature_entry, float tx, float ty)
    {
        Creature cr = m_instance.spawnCreature(creature_entry, tx, ty);
        this.onSummon(cr);
        
        return cr;
    }
    
    public Creature getCreatureFromMap(int cguid)
    {
        return m_instance.getCreature(cguid);
    }
    
    public void despawnCreature(int cguid)
    {
        System.err.println("Despawning " + cguid);
        
        m_instance.despawnCreature(cguid);
    }
    
    public void onDamageTaken(Unit attacker, int damage) {}
    public void onDeath(Unit killer) {}
    public void onSummon(Creature summoned) {}
    public void onSpellHit(Unit caster, Spell spell) {}
    public void onSpellCast(Unit target, Spell spell) {}
    
    public void castSpell(Spell spell, Unit target) {}
    public void onPlayerMoveComplete(Player pl) {}
    
    public abstract void updateAI(int diff);
}
