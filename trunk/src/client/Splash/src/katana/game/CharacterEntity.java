package katana.game;

import org.anddev.andengine.entity.sprite.AnimatedSprite;

public class CharacterEntity {
	private int charID;
	private int charHP;
	private AnimatedSprite charSprite;
	private boolean isSelected;
	private String type;
	
	public CharacterEntity(int charID, int charHP){
		this.charID = charID;
		this.charHP = charHP;
		this.isSelected = false;
	}
	
	public int getCharID(){
		return charID;
	}
	
	public void setType(String type){
		this.type=type;
	}
	
	public String getType(){
		return type;
	}
	
	public AnimatedSprite getCharSprite(){
		return charSprite;
	}
	
	public void setCharSprite(AnimatedSprite charSprite){
		this.charSprite = charSprite;
	}
	
	
	public int getCharHP(){
		return charHP;
	}

	public void setCharHP(int charHP){
		this.charHP = charHP;
	}
	
	public boolean getSelected(){
		return isSelected;
	}
	
	public void setSelected(boolean selected){
		isSelected = selected;
	}
}
