package com.CPS630.engine;

import org.anddev.andengine.entity.sprite.AnimatedSprite;

public class CharacterEntity {
	private int charID;
	private int charHP;
	private AnimatedSprite charSprite;
	private boolean isSelected;
	
	public CharacterEntity(int charID, int charHP){
		this.charID = charID;
		this.charHP = charHP;
		this.isSelected = false;
	}
	
	public int getCharID(){
		return charID;
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
	
	
}
