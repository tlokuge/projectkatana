package server.game.ai;

import java.util.ArrayList;
import server.game.Creature;
import server.game.CreatureAI;
import server.game.Map;
import server.game.Player;
import server.handlers.GameHandler;
import server.shared.Constants;
import server.shared.KatanaPacket;
import server.shared.Opcode;

public class GenericAI extends CreatureAI
{
    private int moveTimer;
    
    private int spawnTimer;
    private int spawnInterval;
    private int gameTimer;
    
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
        gameTimer = Constants.GAME_END_TIMER;
        
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
        
        float move_x = move.getX();
        float move_y = move.getY();
        
        //System.out.println("CHECKING MOVEMENT FOR PLAYER " + move + " - " + move_x + " , " + move_y);
        ArrayList<Integer> removeList = new ArrayList<Integer>();
        for(Integer cguid : creature_spawns)
        {
            Creature spawn = getCreatureFromMap(cguid);
            if(spawn == null)
            {
                removeList.add(cguid);
                continue;
            }
            
            float cx = spawn.getX();
            float cy = spawn.getY();
//            System.out.println("===========");
//            System.out.println(cx + " - " + move_x + " = " + Math.abs(cx - move_x));
//            System.out.println(cy + " - " + move_y + " = " + Math.abs(cy - move_y));
            
            if(Math.abs(cx - move_x) < MIN_DISTANCE && Math.abs(cy - move_y) < MIN_DISTANCE)
            {
                move.addPoints(1);
                System.out.println("PLAYER " + move + " : " + move.getPoints());
                creature_spawns.remove(cguid);
                despawnCreature(cguid);
                return;
            }
        }
    }
    
    public void updateAI(int diff)
    {        
        if(moveTimer < diff)
        {
            Map map = GameHandler.instance().getMap(m_creature.getMap());
            m_creature.moveTo(map.getRandX(), map.getRandY());
            moveTimer = 500 + GameHandler.instance().getRandInt(1000);
        }else moveTimer -= diff;
        
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
