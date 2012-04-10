package server.game;

public abstract class CreatureAI 
{
    protected Creature m_creature;
    
    public CreatureAI(Creature creature)
    {
        this.m_creature = creature;
    }
    
    public void onDamageTaken(Unit attacker, int damage) {}
    public void onDeath(Unit killer) {}
    public void onSummon(Creature summoned) {}
    public void onSpellHit(Unit caster, Spell spell) {}
    public void onSpellCast(Unit target, Spell spell) {}
    
    public void castSpell(Spell spell, Unit target) {}
    public void moveTo(float x, float y) {}
    
    public abstract void updateAI(int diff);
    public abstract CreatureAI getAI(Creature creature);
}
