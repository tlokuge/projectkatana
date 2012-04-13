package katana.game;

import org.anddev.andengine.entity.sprite.AnimatedSprite;

public class Unit 
{
	private int id;
	private int cur_health;
	private int max_health;

	private AnimatedSprite model;
	private HPBar bar;
	
	public Unit(int id, int health)
	{
		this.id = id;
		this.cur_health = health;
		this.max_health = health;
	}
	
	public Unit(int id, int health, AnimatedSprite model)
	{
		this.id = id;
		this.cur_health = health;
		this.max_health = health;
		
		this.model = model;
		this.bar = HPBar.createDefaultHPBar(model, health);
	}
	
	public int getId() { return id; }
	
	public void setHealth(int health) { this.cur_health = health; }
	public int getHealth() 			  { return cur_health; }
	
	public int getMaxHealth() { return max_health; }
	
	public void setModel(AnimatedSprite model) 
	{ 
		this.model = model;
		this.bar = HPBar.createDefaultHPBar(model, cur_health);
	}
	public AnimatedSprite getSprite() { return model; }
	
	public HPBar setHPBar() { return bar; }

	public String toString()
	{
		return id + ": " + cur_health + "/" + max_health;
	}
}
