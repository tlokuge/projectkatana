package server.templates;

import java.util.ArrayList;

public class MapTemplate 
{
    private int id;
    private int location;
    private String name;
    private String background;
    
    private ArrayList<Integer> creature_entries;
    
    public MapTemplate(int id, int location, String name, String background)
    {
        this.id         = id;
        this.location   = location;
        this.name       = name;
        this.background = background;
        
        creature_entries = new ArrayList<Integer>();
    }
    
    public int getId()            { return id; }
    public int getLocation()      { return location; }
    public String getName()       { return name; }
    public String getBackground() { return background; }
    
    public void addCreature(int entry) { creature_entries.add(entry); }
    public ArrayList<Integer> getCreatureEntries() { return creature_entries; }
}
