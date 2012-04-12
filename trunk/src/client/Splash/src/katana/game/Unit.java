package katana.game;

import org.anddev.andengine.entity.sprite.AnimatedSprite;

public class Unit 
{
	private int id;
	private int cur_health;
	private int max_health;
	
	private boolean selected;
	
	private AnimatedSprite model;
	private HPBar bar;
	
	public Unit(int id, int health, AnimatedSprite model, HPBar bar)
	{
		this.id = id;
		this.cur_health = health;
		this.max_health = health;
		
		this.selected = false;
		
		this.model = model;
		this.bar = bar;
	}
	
	public int getId() { return id; }
	
	public void setHealth(int health) { this.cur_health = health; }
	public int getHealth() { return cur_health; }
	
	public int getMaxHealth() { return max_health; }
	
	public void setSelected(boolean selected) { this.selected = selected; }
	public boolean isSelected() { return selected; }
	
	public void setModel(AnimatedSprite model) { this.model = model; }
	public AnimatedSprite getSprite() { return model; }
	
	public HPBar setHPBar() { return bar; }

}
