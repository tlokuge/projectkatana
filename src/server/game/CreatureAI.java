package server.game;

import server.game.ai.DoNothingAI;
import server.game.ai.GenericAI;
import server.handlers.GameHandler;

/**
 * This is an abstract AI class. Every unique AI that is created is a subclass of this AI.
 */
public abstract class CreatureAI 
{
    protected Creature m_creature; // The creature that this AI belongs to
    protected Map m_instance; // The map that the creature is in
    
    /**
     * Constructs a new CreatureAI for the creature
     * @param creature The creature for which this AI belongs to
     */
    public CreatureAI(Creature creature)
    {
        System.out.println("Creating CreatureAI - MapID: " + creature.getMap());
        this.m_creature = creature;
        this.m_instance = GameHandler.instance().getMap(m_creature.getMap());
    }
    
    /**
     * Simple static function to properly load an AI for a creature
     * @param name The name of the AI to be loaded
     * @param creature The creature that the AI is loaded for
     * @return A new instance of the specified AI
     */
    public static CreatureAI getAIByName(String name, Creature creature)
    {
        // Just do a simple string comparision chain until we find the proper AI name
        if(name.equalsIgnoreCase("GenericAI"))
            return new GenericAI(creature);
        else if(name.equalsIgnoreCase("DoNothingAI"))
            return new DoNothingAI(creature);
        
        // By default, return the idle AI
        return new DoNothingAI(creature);
    }
    
    /**
     * Helper function to spawn a new creature within this map on a random location
     * @param creature_entry The entry of the creature to be spawned
     * @return The spawned creature
     */
    public Creature summonCreature(int creature_entry)
    {
        float px = m_instance.getRandX();
        float py = m_instance.getRandY();
        return summonCreature(creature_entry, px, py);
    }
    
    /**
     * Helper function to spawn a new creature on the specified coordinates of this map
     * @param creature_entry The entry of the creature to be spawned
     * @param tx x-coordinate to spawn the creature on
     * @param ty y-coordinate to spawn the creature on
     * @return The spawned creature
     */
    public Creature summonCreature(int creature_entry, float tx, float ty)
    {
        // Creatures (and their AI) cannot actually spawn a new creature directly, only maps can so we make our map instance spawn it
        Creature cr = m_instance.spawnCreature(creature_entry, tx, ty);
        this.onSummon(cr); // Call the AI's onSummon function (since we just spawned a creature) - allows more scripting power
        
        return cr;
    }
    
    /**
     * Finds a specific creature instance within the map using the creature's GUID
     * @param cguid The GUID of the desired creature
     * @return The creature that the GUID corresponds to (or NULL if not found)
     */
    public Creature getCreatureFromMap(int cguid)
    {
        return m_instance.getCreature(cguid);
    }
    
    /**
     * Despawns a specific creature from the map using the creature's GUID
     * @param cguid The GUID of the creature to be despawned
     */
    public void despawnCreature(int cguid)
    {
        System.err.println("Despawning " + cguid);
        
        // Similar to summoning, only the map can despawn creatures 
        m_instance.despawnCreature(cguid);
    }
    
    // These functions are called by identical functions from the Creature class, and can be overrided within individual AI scripts for various reasons.
    public int onHealReceived(int heal, Unit healer)    { return heal;   }
    public int onDamageTaken(int damage, Unit attacker) { return damage; }
    public int onDamageDeal(int damage, Unit target, Spell spell, boolean auto) { return damage; }
    public void onDeath(Unit killer) {}
    public void onSummon(Creature summoned) {}
    public void onSpellHit(Spell spell, Unit caster) {}
    public void onSpellCast(Spell spell, Unit target) {}
    
    /**
     * Casts a spell towards a target
     * @param spell The spell being cast
     * @param target The target of the spell
     */
    public void castSpell(Spell spell, Unit target) 
    { 
        // The AI simply calls the unit's cast spell function - this method is just convenience (even if unused)
        m_creature.castSpell(spell, target); 
    }
    
    /**
     * Called whenever a player completes a movement
     * @param pl The player who completed the movement
     */
    public void onPlayerMoveComplete(Player pl) {}
    
    /**
     * The most important function of the AI (and so MUST be overrided), is called by the creature every diff.
     * This function allows the AI to make use of custom timers and so will be the meat of most AI scripts
     * @param diff Time (in ms) since this function was last called (default around 100 ms)
     */
    public abstract void updateAI(int diff);
}
