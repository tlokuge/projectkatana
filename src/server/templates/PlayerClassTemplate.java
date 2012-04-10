package server.templates;

public class PlayerClassTemplate
{
    private int id;
    private String name;
    private int model_id;
    private int spell[] = new int[4];
    
    public PlayerClassTemplate(int id, String name, int spell1, int spell2, int spell3, int spell4, int model_id)
    {
        this.id       = id;
        this.name     = name;
        this.spell[0] = spell1;
        this.spell[1] = spell2;
        this.spell[2] = spell3;
        this.spell[3] = spell4;
        this.model_id = model_id;
    }
    
    public int getId()     { return id; }
    public String getName(){ return name; }
    public int getSpell1() { return spell[0]; }
    public int getSpell2() { return spell[1]; }
    public int getSpell3() { return spell[2]; }
    public int getSpell4() { return spell[3]; }
    public int getModelId(){ return model_id; }
}
