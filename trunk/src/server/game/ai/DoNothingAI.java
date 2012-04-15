package server.game.ai;

import server.game.Creature;
import server.game.CreatureAI;

public class DoNothingAI extends CreatureAI
{
    public DoNothingAI(Creature creature)
    {
        super(creature);
    }
    public void updateAI(int diff) 
    {
    }

    @Override
    public CreatureAI getAI(Creature creature) 
    {
        return new DoNothingAI(creature);
    }
    
}
