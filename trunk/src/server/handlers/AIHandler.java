package server.handlers;

import java.util.HashMap;
import server.game.Creature;
import server.game.CreatureAI;
import server.game.ai.GenericAI;

public class AIHandler 
{
    private HashMap<String, CreatureAI> ai_map;
    
    private static AIHandler instance;
    
    private AIHandler()
    {
        ai_map = new HashMap<>();
        loadAI();
    }
    
    public CreatureAI getAIByName(String name, Creature creature)
    {
        if(ai_map.get(name) == null)
            return new GenericAI(creature);
        
        return ai_map.get(name).getAI(creature);
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Unable to clone a singleton!");
    }
    
    public static AIHandler instance()
    {
        if(instance == null)
            instance = new AIHandler();
        
        return instance;
    }
    
    // Null creatures will be passed into this as they will simply be used as a template
    private void loadAI()
    {
        System.out.println("Loading AI Scripts....");
        ai_map.put("GenericAI", new GenericAI(null));
        
        System.out.println("Loaded " + ai_map.size() + " AI Scripts!");
    }
}
