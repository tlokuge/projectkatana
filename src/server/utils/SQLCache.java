package server.utils;

import java.util.ArrayList;
import java.util.HashMap;
import server.communication.KatanaServer;
import server.handlers.GameHandler;
import server.handlers.SQLHandler;
import server.limbo.Lobby;
import server.templates.CreatureTemplate;
import server.templates.MapTemplate;
import server.templates.PlayerClassTemplate;
import server.templates.SpellTemplate;

public abstract class SQLCache
{
    private static HashMap<Integer, PlayerClassTemplate> class_map = new HashMap<Integer, PlayerClassTemplate>();
    private static HashMap<Integer, SpellTemplate> spell_map = new HashMap<Integer, SpellTemplate>();
    private static HashMap<Integer, String> model_map = new HashMap<Integer, String>();
    private static HashMap<Integer, CreatureTemplate> creature_map = new HashMap<Integer, CreatureTemplate>();
    private static HashMap<Integer, MapTemplate> map_map = new HashMap<Integer, MapTemplate>();
    
    public SQLCache() {}
    public static void createCache()
    {
        cacheLocations();
        cacheSpells();
        cacheModels();
        cacheClasses();
        cacheCreatures();
        cacheMaps();
    }
    
    public static int getTotalClasses()    { return class_map.size(); }
    public static int getTotalSpells()     { return spell_map.size(); }
    public static int getTotalModels()     { return model_map.size(); }
    public static int getTotalCreatures()  { return creature_map.size(); }
    public static int getTotalMaps()       { return map_map.size(); }
    public static PlayerClassTemplate getClass(int id) { return class_map.get(id); }
    public static SpellTemplate getSpell(int id)       { return spell_map.get(id); }
    public static String getModel(int id)              { return model_map.get(id); }
    public static CreatureTemplate getCreature(int id) { return creature_map.get(id); }
    public static MapTemplate getMap(int id)           { return map_map.get(id);   }
    
    public static ArrayList<MapTemplate> getMapsByLocation(int location)
    {
        ArrayList<MapTemplate> templates = new ArrayList<MapTemplate>();
        
        for(int i : map_map.keySet())
        {
            MapTemplate temp = getMap(i);
            if(temp.getLocation() == location)
                templates.add(temp);
        }
        
        return templates;
    }
    
    private static void cacheLocations()
    {
        System.out.println("Loading Locations....");
        ProgressBar bar = new ProgressBar();
        
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().runLocationQuery();
        if(results == null || results.isEmpty())
        {
            System.err.println("ERROR: NO LOCATIONS LOADED");
            KatanaServer.exit(KatanaError.ERR_SQLCACHE_NO_LOCATIONS);
        }
        
        for(int i = 0; i < results.size(); ++i)
        {
            HashMap<String, Object> map = results.get(i);
            int id = (Integer)map.get("location_id");
            String name = (String)map.get("location_name");
            double latitude = (Double)map.get("latitude");
            double longitude = (Double)map.get("longitude");
            double radius = (Double)map.get("radius");
            
            GameHandler.instance().addLobby(id, new Lobby(id, name,latitude, longitude, radius));
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + results.size() + " locations");
    }
    
    private static void cacheSpells()
    {
        System.out.println("Loading spells....");
        spell_map.clear();
        
        ProgressBar bar = new ProgressBar();
        
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().runSpellQuery();
        if(results == null || results.isEmpty())
        {
            System.err.println("ERROR: NO SPELLS LOADED");
            KatanaServer.exit(KatanaError.ERR_SQLCACHE_NO_SPELLS);
        }
        
        for(int i = 0; i < results.size(); ++i)
        {
            HashMap<String, Object> map = results.get(i);
            int id = (Integer)map.get("spell_id");
            String name = (String)map.get("spell_name");
            int damage = (Integer)map.get("damage");
            int cooldown = (Integer)map.get("cooldown");
            
            spell_map.put(id, new SpellTemplate(id, name, damage, cooldown));
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + spell_map.size() + " spells");
    }
    
    private static void cacheModels()
    {
        System.out.println("Loading models....");
        model_map.clear();
        
        ProgressBar bar = new ProgressBar();
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().runModelQuery();
        if(results == null || results.isEmpty())
        {
            System.err.println("ERROR: NO MODELS LOADED");
            KatanaServer.exit(KatanaError.ERR_SQLCACHE_NO_MODELS);
        }
        
        for(int i = 0; i < results.size(); ++i)
        {
            HashMap<String, Object> row = results.get(i);
            int model_id = (Integer)row.get("model_id");
            String file = (String)row.get("file");
            
            model_map.put(model_id, file);
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + results.size() + " models");
    }
    
    private static void cacheClasses()
    {
        System.out.println("Loading classes....");
        class_map.clear();
        
        ProgressBar bar = new ProgressBar();
        
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().runClassQuery();
        if(results == null || results.isEmpty())
        {
            System.err.println("ERROR: NO CLASSES LOADED");
            KatanaServer.exit(KatanaError.ERR_SQLCACHE_NO_CLASSES);
        }
        
        for(int i = 0; i < results.size(); ++i)
        {
            HashMap<String, Object> map = results.get(i);
            int id = (Integer)map.get("class_id");
            String name = (String)map.get("name");
            int spell_1 = (Integer)map.get("spell_1");
            int spell_2 = (Integer)map.get("spell_2");
            int spell_3 = (Integer)map.get("spell_3");
            int spell_4 = (Integer)map.get("spell_4");
            int model   = (Integer)map.get("model_id");
            
            class_map.put(id, new PlayerClassTemplate(id, name, spell_1, spell_2, spell_3, spell_4, model));
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + class_map.size() + " classes");
    }
    
    private static void cacheCreatures()
    {
        System.out.println("Loading creatures....");
        creature_map.clear();
        
        ProgressBar bar = new ProgressBar();
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().runCreatureQuery();
        for(int i = 0; i < results.size(); ++i)
        {
            HashMap<String, Object> map = results.get(i);
            int id        = (Integer)map.get("creature_id");
            String name   = (String)map.get("creature_name");
            int health    = (Integer)map.get("health");
            int level     = (Integer)map.get("level");
            int atk_speed = (Integer)map.get("attack_speed");
            int atk_dmg   = (Integer)map.get("attack_damage");
            float speed   = (Float)map.get("move_speed");
            int model_id  = (Integer)map.get("model_id");
            String script = (String)map.get("script");
            
            creature_map.put(id, new CreatureTemplate(id, name, health, level, atk_speed, atk_dmg, speed, model_id, script));
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + results.size() + " creatures");
    }
    
    private static void cacheMaps()
    {
        System.out.println("Loading maps....");
        
        ProgressBar bar = new ProgressBar();
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().runMapTemplateQuery();
        if(results == null || results.isEmpty())
        {
            System.err.println("ERROR: NO MAPS LOADED");
            KatanaServer.exit(KatanaError.ERR_SQLCACHE_NO_MAPS);
        }
        for(int i = 0; i < results.size(); ++i)
        {
            HashMap<String, Object> row = results.get(i);
            int id = (Integer)row.get("map_id");
            int location = (Integer)row.get("location_id");
            String name = (String)row.get("map_name");
            String background = (String)row.get("background");
            
            MapTemplate template = new MapTemplate(id, location, name, background);
            ArrayList<HashMap<String, Object>> creature_instances = SQLHandler.instance().runCreatureInstanceQuery(id);
            if(creature_instances != null && !creature_instances.isEmpty())
                for(int j = 0; j != creature_instances.size(); ++j)
                    template.addCreature((Integer)creature_instances.get(j).get("creature_id"));
            map_map.put(id, template);
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + results.size() + " maps");
    }
}