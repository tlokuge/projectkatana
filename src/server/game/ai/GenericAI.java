package server.game.ai;

import java.util.Random;
import server.game.Creature;
import server.game.CreatureAI;

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
            int x = rand.nextInt(480);
            int y = rand.nextInt(800);
            moveTo(x, y);
            moveTimer = 500 + rand.nextInt(1000);
        }else moveTimer -= diff;
    }
    
    public CreatureAI getAI(Creature creature)
    {
        return new GenericAI(creature);
    }
}
