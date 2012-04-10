package server;

public class MapTemplate 
{
    private int id;
    private int location;
    private String name;
    private String background;
    
    public MapTemplate(int id, int location, String name, String background)
    {
        this.id         = id;
        this.location   = location;
        this.name       = name;
        this.background = background;
    }
    
    public int getId()            { return id; }
    public int getLocation()      { return location; }
    public String getName()       { return name; }
    public String getBackground() { return background; }
}
