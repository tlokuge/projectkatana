package com.CPS630.engine;

import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;

public class Players {
	
	private static final int CLASS_ATK = 0;
	private static final int CLASS_HLR = 1;
	
	private int playerID;
	private int playerClass;
	
	String spriteFile;
	AnimatedSprite playerSprite;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	
	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	
	public int getID()
	{
		return playerID;
	}
	
	public void setID(int id)
	{
		playerID = id;
	}
	
	public int getPlayerClass()
	{
		return playerClass;
	}
	
	public void setPlayerClass(int playerClass)
	{
		this.playerClass=playerClass;
		if(playerClass == CLASS_ATK){
			spriteFile = "face_box.png"
		}
	}	
	
	public void loadSprite(){
		playerSprite = 
	}
}
