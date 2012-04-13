package katana.game;

import org.anddev.andengine.entity.sprite.AnimatedSprite;

public class Unit 
{
	private int id;
	private int cur_health;
	private int max_health;

	private float x;
	private float y;
	
	private AnimatedSprite model;
	private String model_name;
	private HPBar bar;
	
	public Unit(int id, int health, float x, float y)
	{
		this.id = id;
		this.cur_health = health;
		this.max_health = health;
		
		this.x = x;
		this.y = y;
	}
	
	public Unit(int id, int health, float x, float y, AnimatedSprite model)
	{
		this.id = id;
		this.cur_health = health;
		this.max_health = health;
		
		this.x = x;
		this.y = y;
		
		this.model = model;
		this.bar = HPBar.createDefaultHPBar(model, health);
	}
	
	public int getId() { return id; }
	
	public void setHealth(int health) 
	{ 
		this.cur_health = health;
		if(bar != null)
			bar.setHP(cur_health);
	}
	public int getHealth() 			  { return cur_health; }
	
	public void setMaxHealth(int max) { this.max_health = max; }
	public int getMaxHealth() 		  { return max_health; }
	
	public void moveTo(float x, float y) { this.x = x; this.y = y; }
	public float getX()					 { return x; }
	public float getY()					 { return y; }
	
	public void setModel(AnimatedSprite model, String model_name) 
	{ 
		this.model = model;
		this.model_name = model_name;
		this.bar = HPBar.createDefaultHPBar(model, cur_health);
	}
	public AnimatedSprite getSprite() { return model; }
	public String getModelName() 	  { return model_name; }
	
	public HPBar setHPBar() { return bar; }

	public String toString()
	{
		return id + ": " + cur_health + "/" + max_health;
	}
}
