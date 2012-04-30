package server.game.ai;

import java.util.ArrayList;
import java.util.Iterator;
import server.game.Creature;
import server.game.CreatureAI;
import server.game.Player;
import server.handlers.GameHandler;
import server.shared.Constants;

public class GenericAI extends CreatureAI
{
    private int moveTimer;
    
    private int spawnTimer;
    private int spawnInterval;
    private int checkTimer;
    
    public ArrayList<Integer> creature_spawns;
    
    private final static int MIN_DISTANCE = 75;
    
    public GenericAI(Creature creature)
    {
        super(creature);
        creature_spawns = new ArrayList<Integer>();
        moveTimer = 5000;
        spawnTimer = 100;
        checkTimer = 500;
        
        switch(m_instance.getDifficulty())
        {
            case Constants.DIFFICULTY_HARD:     spawnInterval = 1000; break;
            case Constants.DIFFICULTY_MEDIUM:   spawnInterval = 2000; break;
            default:                            spawnInterval = 3000; break;
        }
    }
    
    public void onPlayerMoveComplete(Player pl)
    {
        if(pl == null)
            return;
        
        //System.out.println("CHECKING MOVEMENT FOR PLAYER " + move + " - " + move_x + " , " + move_y);
        Iterator<Integer> itr = creature_spawns.iterator();
        while(itr.hasNext())
        {
            int cguid = itr.next();
            Creature spawn = getCreatureFromMap(cguid);
            if(spawn == null)
            {
                itr.remove();
                continue;
            }
            
            //if(isWithinRangeOf(move, spawn, MIN_DISTANCE))
            if(pl.isWithinRangeOf(spawn, MIN_DISTANCE))
            {
                pl.addPoints(1);
                System.out.println("PLAYER " + pl + " : " + pl.getPoints());
                System.out.println("Removing " + cguid);
                itr.remove();
                despawnCreature(cguid);
                break;
            }
        }
    }
    
    public void updateAI(int diff)
    {        
        if(moveTimer < diff)
        {
            if(!creature_spawns.isEmpty())
            {
                int tar_guid = creature_spawns.get(GameHandler.instance().getRandInt(creature_spawns.size()));
                moveTimer = m_creature.moveTo(getCreatureFromMap(tar_guid));
            }
            else
                moveTimer = m_creature.moveRandom();
            if(moveTimer < 0)
                moveTimer = m_creature.moveRandom();
            
            moveTimer += 200;
            checkTimer = moveTimer - 100;
            
            System.out.println("moveTimer: " + moveTimer + " checkTimer: " + checkTimer);
        }else moveTimer -= diff;
        
        if(checkTimer < diff)
        {
            System.out.println("Checking...");
            Iterator<Integer> itr = creature_spawns.iterator();
            while(itr.hasNext())
            {
                int cguid = itr.next();
                //if(isWithinRangeOf(m.getCreature(cguid), m_creature, MIN_DISTANCE))
                if(m_creature.isWithinRangeOf(getCreatureFromMap(cguid), MIN_DISTANCE))
                {
                    itr.remove();
                    despawnCreature(cguid);
                    System.err.println("Professor eating " + cguid);
                    break;
                }
            }
            System.out.println(creature_spawns);
            System.out.println(m_instance.getCreatures().keySet());
            
            moveTimer = 500;
            checkTimer = 500;
        }else checkTimer -= diff;
        
        if(creature_spawns.size() >= 10)
            return;
        
        if(spawnTimer < diff)
        {
            Creature spawn = summonCreature(Constants.CREATURE_WATERMELON_ENTRY);
            if(spawn != null)
                creature_spawns.add(spawn.getId());
            spawnTimer = spawnInterval;
        }else spawnTimer -= diff;
    }
}
