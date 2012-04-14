package server.game.ai;

import java.util.Random;
import server.game.Creature;
import server.game.CreatureAI;
import server.game.Map;
import server.handlers.GameHandler;

public class GenericAI extends CreatureAI
{
    private int moveTimer;
    
    public GenericAI(Creature creature)
    {
        super(creature);
        moveTimer = 5000;
    }
    
    public void updateAI(int diff)
    {
        if(moveTimer < diff)
        {
            Random rand = new Random(System.currentTimeMillis());
            Map map = GameHandler.instance().getMap(m_creature.getMap());
            m_creature.moveTo(map.getRandX(), map.getRandY());
            moveTimer = 500 + rand.nextInt(1000);
        }else moveTimer -= diff;
    }
    
    public CreatureAI getAI(Creature creature)
    {
        return new GenericAI(creature);
    }
}
