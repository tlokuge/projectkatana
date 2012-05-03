 package server.game.ai;

import java.util.ArrayList;
import java.util.Iterator;
import server.game.*;
import server.handlers.GameHandler;
import server.shared.Constants;

public class GenericAI extends CreatureAI
{
    private int moveTimer;
    
    private int spawnTimer;
    private int spawnInterval;
    private int checkTimer;
    
    public ArrayList<Integer> creature_spawns;
    
    private final static int MIN_DISTANCE = 50;
    
    public GenericAI(Creature creature)
    {
        super(creature);
        creature_spawns = new ArrayList<Integer>();
        if(m_creature == null)
        {
            System.err.println("GenericAI: m_creature null");
            return;
        }
        
        moveTimer = 5000;
        spawnTimer = 100;
        checkTimer = 500;
        
        Map map = GameHandler.instance().getMap(m_creature.getMap());
        if(map == null)
        {
            System.err.println("GenericAI: null map");
            return;
        }
        
        switch(map.getDifficulty())
        {
            case Constants.DIFFICULTY_HARD:     spawnInterval = 1000; break;
            case Constants.DIFFICULTY_MEDIUM:   spawnInterval = 2000; break;
            default:                            spawnInterval = 3000; break;
        }
    }
    
    public void checkMovement(Player move)
    {
        if(move == null)
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
            
            if(isWithinRangeOf(move, spawn, MIN_DISTANCE))
            {
                move.addPoints(1);
                System.out.println("PLAYER " + move + " : " + move.getPoints());
                System.out.println("Removing " + cguid);
                itr.remove();
                despawnCreature(cguid);
                break;
            }
        }
    }
    
    private boolean isWithinRangeOf(Unit unit, Unit target, float range)
    {
        if(unit == null || target == null)
            return false;
        
        float ux = unit.getX();
        float uy = unit.getY();
        float tx = target.getX();
        float ty = target.getY();
        
        if(Math.abs(ux - tx) < range && Math.abs(uy - ty) < range)
            return true;
        
        return false;
    }
    
    private int calculateTravelTime(float start_x, float start_y, float dest_x, float dest_y)
    {
        float dx = Math.abs(start_x - dest_x);
        float dy = Math.abs(start_y - dest_y);
        float hyp = (float)Math.sqrt((dx * dx) + (dy * dy));
        int time =(int) hyp * 5;
        System.out.println("[" + start_x + "," + start_y + "] -> [" + dest_x + "," + dest_y + "] = " + time);
        return time;
    }
    
    private int moveToTarget(Unit target)
    {
        if(target == null)
            return -1;
        
        System.out.println("Mastoras moving to " + target);
        float mx = m_creature.getX();
        float my = m_creature.getY();
        float dest_x = target.getX();
        float dest_y = target.getY();
        m_creature.moveTo(dest_x, dest_y);
        return calculateTravelTime(mx, my, dest_x, dest_y);
    }
    
    private int moveToRandomLocation()
    {
        System.out.println("Mastoras random move");
        Map map = GameHandler.instance().getMap(m_creature.getMap());
        if(map == null)
        {
            System.err.println("omg map nul moveToRandmLoc");
            return -1;
        }
        
        float mx = m_creature.getX();
        float my = m_creature.getY();
        float dest_x = map.getRandX();
        float dest_y = map.getRandY();
        m_creature.moveTo(dest_x, dest_y);
        
        return calculateTravelTime(mx, my, dest_x, dest_y);
    }
    
    public void updateAI(int diff)
    {        
        if(moveTimer < diff)
        {
            if(!creature_spawns.isEmpty())
            {
                Map m = GameHandler.instance().getMap(m_creature.getMap());
                int tar_guid = creature_spawns.get(GameHandler.instance().getRandInt(creature_spawns.size()));
                if(m != null)
                    moveTimer = moveToTarget(m.getCreature(tar_guid));
                else
                    moveTimer = moveToRandomLocation();
            }
            else
                moveTimer = moveToRandomLocation();
            if(moveTimer < 0)
                moveTimer = moveToRandomLocation();
            
            moveTimer += 200;
            checkTimer = moveTimer - 100;
            
            System.out.println("moveTimer: " + moveTimer + " checkTimer: " + checkTimer);
        }else moveTimer -= diff;
        
        if(checkTimer < diff)
        {
            System.out.println("Checking...");
            Map m = GameHandler.instance().getMap(m_creature.getMap());
            if(m != null)
            {
                Iterator<Integer> itr = creature_spawns.iterator();
                while(itr.hasNext())
                {
                    int cguid = itr.next();
                    if(isWithinRangeOf(m.getCreature(cguid), m_creature, MIN_DISTANCE))
                    {
                        itr.remove();
                        despawnCreature(cguid);
                        System.err.println("Mastoras eating " + cguid);
                        break;
                    }
                }
            }
            System.out.println(creature_spawns);
            System.out.println(m.getCreatures().keySet());
            
            moveTimer = 500;
            checkTimer = 500;
        }else checkTimer -= diff;
        
        if(creature_spawns.size() >= 10)
            return;
        
        if(spawnTimer < diff)
        {
            Map map = GameHandler.instance().getMap(m_creature.getMap());
            if(map == null)
                return;
            Creature spawn = summonCreature(Constants.CREATURE_WATERMELON_ENTRY, map.getRandX(), map.getRandY());
            if(spawn != null)
                creature_spawns.add(spawn.getId());
            spawnTimer = spawnInterval;
        }else spawnTimer -= diff;
    }
    
    public CreatureAI getAI(Creature creature)
    {
        return new GenericAI(creature);
    }
}
