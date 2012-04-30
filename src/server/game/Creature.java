package server.game;

import server.handlers.GameHandler;
import server.templates.CreatureTemplate;
import server.utils.SQLCache;

/**
 * Every non player character (NPC) is a creature (which is a child of unit), containing some base stats (which are currently not being used) and its own AI
 */
public class Creature extends Unit
{
    private int entry; // Creature ID in the creatures datanase table
    private int level; // Creature level (UNUSED)
    
    private CreatureAI ai; // The AI instance for this creature
    
    /**
     * Instantiates a new Creature object
     * @param entry The ID of the creature from the creatures database table
     * @param map_id The map that the creature is in
     */
    public Creature(int entry, int map_id)
    {
        // Calls the Unit constructor (the creature's ID is a GUID taken from the GameHandler). 
        // Every other value passed (besides the map_id) is default to 0 and will be set via the CreatureTemplate below.
        super(GameHandler.instance().getNextGUID(), "", map_id, 0, 0, 0, 0f, 0);
        
        this.entry = entry;
        // Load the template for the creature entry (Cached on server startup)
        CreatureTemplate template = SQLCache.getCreature(entry);
        if(template == null)
        {
            System.err.println("Attempted to spawn invalid creature (entry: " + entry + ")");
            return; // What should we do if this happens? Spawn a default?
        }
        // Set all stats
        this.setName(template.getCreatureName());
        this.setMaxhealth(template.getHealth());
        this.setLevel(template.getLevel());
        this.setAttackSpeed(template.getAttackSpeed());
        this.setAttackDamage(template.getAttackDamage());
        this.setSpeed(template.getMoveSpeed());
        this.setModelId(template.getModelId());
        this.addToMap(map_id);
        // Load the AI by the AI name, and pass this to the AI so it has access to the actual creature
        this.ai = CreatureAI.getAIByName(template.getScript(), Creature.this);
    }
    
    // Simple accessors and mutators
    public int getEntry()           { return entry; }
    public void setLevel(int level) { this.level = level; }
    public int getLevel()           { return level; }
    public CreatureAI getAI()       { return ai; }
    
    /**
     * Update is called every diff (default: 100 ms) and can be used for a variety of things. For now, it is only used for AI
     * @param diff Time since update was last called (generally around 100 ms by default)
     */
    @Override
    public void update(int diff)
    {
        super.update(diff);
        // Updates the AI
        ai.updateAI(diff);
    }
    
    // The functions below are currently unused - they were going to be used during battles and allowed more AI power and flexibility
    /**
     * Called everytime the creature is healed
     * @param heal The amount the creature is healed by
     * @param healer The unit healing the creature
     * @return The (possibly) modified heal amount
     */
    @Override
    public int onHealReceived(int heal, Unit healer) 
    {
        // Call the AI's onHealReceived, allowing the AI to control what happens if this unit is healed
        return ai.onHealReceived(heal, healer);
    }

    /**
     * Called everytime the creature takes damage
     * @param damage The amount of damage taken
     * @param attacker The unit attacking the creature
     * @return The (possibly) modified damage amount
     */
    @Override
    public int onDamageTaken(int damage, Unit attacker) 
    {
        // Call the AI's onDamageTaken, allowing it to control what happens
        return ai.onDamageTaken(damage, attacker);
    }

    /**
     * Called everytime the creature deals damage
     * @param damage The amount of damage dealt
     * @param target The target to damage
     * @param spell The spell being cast (possibly)
     * @param is_auto_attack A flag indicating whether or not this attack is an auto attack
     * @return The amount of damage being dealt (after any possibly modifiers)
     */
    @Override
    public int onDamageDeal(int damage, Unit target, Spell spell, boolean is_auto_attack) 
    {
        // Call the AI's onDamageDeal, allowing it control
        return ai.onDamageDeal(damage, target, spell, is_auto_attack);
    }

    /**
     * Called everytime the creature is hit by a spell
     * @param spell The spell the creature is hit by
     * @param caster The unit casting the spell
     */
    @Override
    public void onSpellHit(Spell spell, Unit caster) 
    {
        // Call the AI's onSpellHit, giving it control
        ai.onSpellHit(spell, caster);
    }
    
    /**
     * Called everytime the creature casts a spell
     * @param spell The spell being cast
     * @param target The target of the spell
     */
    @Override
    public void onSpellCast(Spell spell, Unit target) 
    {
        // Give the AI control
        ai.onSpellCast(spell, target);
    }
    
    /**
     * Called when the creature dies (health reaches 0)
     * @param killer The unit that dealt the last bit of damage, killing the creature
     */
    @Override
    public void onDeath(Unit killer)  
    {
        // AI has control
        ai.onDeath(killer);
    }
}
