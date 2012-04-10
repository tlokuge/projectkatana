package com.katana.splash;

import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public class PlayerEntity extends CharacterEntity{
	
	private static final int CLASS_ATK = 2;
	private static final int CLASS_HLR = 1;
	/*
	Change the file name in the following lines to the corresponding sprite
	*/
	private static final String ATK_SPRITE = "face_box.png";
	private static final String HLR_SPRITE = "face_box.png";
	//
	
	private static final String[] ATK_SPELLS = {"Simple Attack", "Poison", "Rapid Fire", "Kamikaze"};
	private static final String[] HLR_SPELLS = {"Small Heal", "Big Heal", "Simple Attack", "Shield"};
	
	private int playerClass;
	//private String playerSpriteFile;	
	private String[] spells;
	private AnimatedSprite playerSprite;
	private HPBar hpbar;
	
	public PlayerEntity(int playerID, int playerClass, int playerHP){
		super(playerID,playerHP);
		this.setType("player");
		this.playerClass=playerClass;
		if(playerClass == CLASS_ATK){
			//playerSpriteFile = ATK_SPRITE;
			spells = ATK_SPELLS;
		}
		else{
			//playerSpriteFile = HLR_SPRITE;
			spells = HLR_SPELLS;
		}
	}
	
	public int getPlayerClass(){
		return playerClass;
	}
	
	public String getSpell(int playerClass, int spell){
		if(playerClass == CLASS_ATK)
			return ATK_SPELLS[spell];
		else
			return HLR_SPELLS[spell];
	}
	
	
	public void setHPBar(HPBar hpbar){
		this.hpbar = hpbar;
	}
	
	public HPBar getHPBar(){
		return this.hpbar;
	}
}
