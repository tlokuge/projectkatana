package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SQLCache
{
    class Location
    {
        private int loc_id;
        private String name;
        private double latitude;
        private double longitude;
        private double radius;
        
        public Location(int loc_id, String name, double latitude, double longitude, double radius)
        {
            this.loc_id = loc_id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
        }
        
        public int getId() { return loc_id; }
        public String getName() { return name; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public double getRadius() { return radius; }
    }
    
    class Class
    {
        private int class_id;
        private String class_name;
        private int spell[] = new int[4];
        private int model_id = 0;
        
        public Class(int class_id, String class_name, int spell1, int spell2, int spell3, int spell4, int model_id)
        {
            this.class_id = class_id;
            this.class_name = class_name;
            this.spell[0] = spell1;
            this.spell[1] = spell2;
            this.spell[2] = spell3;
            this.spell[3] = spell4;
            this.model_id = model_id;
        }
        
        public int getId() { return class_id; }
        public String getName() { return class_name; }
        public int getSpell1() { return spell[0]; }
        public int getSpell2() { return spell[1]; }
        public int getSpell3() { return spell[2]; }
        public int getSpell4() { return spell[3]; }
        public int getModelId(){ return model_id; }
        
    }
    
    class Spell
    {
        private int spell_id;
        private String spell_name;
        private int damage;
        private int cooldown;
        
        public Spell(int spell_id, String spell_name, int damage, int cooldown)
        {
            this.spell_id = spell_id;
            this.spell_name = spell_name;
            this.damage = damage;
            this.cooldown = cooldown;
        }
        
        public int getId()      { return spell_id; }
        public String getName() { return spell_name; }
        public int getDamage()  { return damage; }
        public int getCooldown(){ return cooldown; }
    }
    
    private HashMap<Integer, Location> location_map;
    private HashMap<Integer, Class> class_map;
    private HashMap<Integer, Spell> spell_map;
    
    public SQLCache()
    {
        location_map = new HashMap<Integer, Location>();
        class_map    = new HashMap<Integer, Class>();
        spell_map    = new HashMap<Integer, Spell>();
    }
    
    public void createCache()
    {
        cacheLocations();
        cacheSpells();
        cacheClasses();
        cacheCreatures();
    }
    
    public int getTotalLocations()  { return location_map.size(); }
    public int getTotalClasses()    { return class_map.size(); }
    public int getTotalSpells()     { return spell_map.size(); }
    
    public Set<Integer> getLocationIds() { return location_map.keySet(); }
    
    public Location getLocation(int id) { return location_map.get(id); }
    public Class getClass(int id)       { return class_map.get(id); }
    public Spell getSpell(int id)       { return spell_map.get(id); }
    
    
    private void cacheLocations()
    {
        location_map.clear();
        
        ProgressBar bar = new ProgressBar();
        
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().execute("SELECT `location_id`, `location_name`, `latitude`, `longitude`, `radius` FROM `locations`;");
        if(results == null || results.isEmpty())
        {
            System.err.println("ERROR: NO LOCATIONS LOADED");
            return;
            //System.exit(Constants.NO_LOCATIONS);
        }
        
        for(int i = 0; i < results.size(); ++i)
        {
            HashMap<String, Object> map = results.get(i);
            int id = (Integer)map.get("location_id");
            String name = (String)map.get("location_name");
            double latitude = (Double)map.get("latitude");
            double longitude = (Double)map.get("longitude");
            double radius = (Double)map.get("radius");
            
            location_map.put(id, new Location(id, name,latitude, longitude, radius));
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + location_map.size() + " locations");
    }
    
    private void cacheSpells()
    {
        spell_map.clear();
        
        ProgressBar bar = new ProgressBar();
        
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().execute("SELECT `spell_id`, `spell_name`,`damage`,`cooldown` FROM `spells`");
        if(results == null || results.isEmpty())
        {
            System.err.println("ERROR: NO SPELLS LOADED");
            return;
            //System.exit(Constants.NO_SPELLS);
        }
        
        for(int i = 0; i < results.size(); ++i)
        {
            HashMap<String, Object> map = results.get(i);
            int id = (Integer)map.get("spell_id");
            String name = (String)map.get("spell_name");
            int damage = (Integer)map.get("damage");
            int cooldown = (Integer)map.get("cooldown");
            
            spell_map.put(id, new Spell(id, name, damage, cooldown));
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + spell_map.size() + " spells");
    }
    
    private void cacheClasses()
    {
        class_map.clear();
        
        ProgressBar bar = new ProgressBar();
        
        ArrayList<HashMap<String, Object>> results = SQLHandler.instance().execute("SELECT `class_id`, `class_name`, `spell_1`, `spell_2`, `spell_3`, `spell_4`, `model_id` FROM `classes`;");
        if(results == null || results.isEmpty())
        {
            System.err.println("ERROR: NO CLASSES LOADED");
            return;
            // System.exit(Constants.NO_CLASSES);
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
            
            class_map.put(id, new Class(id, name, spell_1, spell_2, spell_3, spell_4, model));
            bar.update(i, results.size());
        }
        
        System.out.println("Loaded " + class_map.size() + " classes");
    }
    
    private void cacheCreatures()
    {
        System.out.println("Loaded 0 creatures");
    }
}