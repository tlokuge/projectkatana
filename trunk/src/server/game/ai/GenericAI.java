package server.game.ai;

import server.game.Creature;
import server.game.CreatureAI;

public class GenericAI extends CreatureAI
{
    public GenericAI(Creature creature)
    {
        super(creature);
    }
    
    public void updateAI(int diff)
    {
        
    }
    
    public CreatureAI getAI(Creature creature)
    {
        return new GenericAI(creature);
    }
}
