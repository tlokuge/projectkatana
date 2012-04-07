package com.CPS630.engine;

import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public class MonsterEntity {
	
	private static final int BOS = 2;
	private static final int MIN = 1;
	/*
	Change the file name in the following lines to the corresponding sprite
	*/
	private static final String BOS_SPRITE = "face_box.png";
	private static final String MIN_SPRITE = "face_box.png";
	//
	
	//private static final String[] ATK_SPELLS = {"Simple Attack", "Poison", "Rapid Fire", "Kamikaze"};
	//private static final String[] HLR_SPELLS = {"Small Heal", "Big Heal", "Simple Attack", "Shield"};
	
	private int monsterID;
	private int monsterType;
	private int monsterHP
	private int maxHP;
	private String monsterSpriteFile;	
	private String[] spells;
	private AnimatedSprite monsterSprite;
	
	public monsters(int monsterID, int monsterType, int monsterHP){
		this.monsterID = monsterID;
		this.monsterHP = monsterHP;
		maxHP = monsterHP;
		this.monsterClass=monsterClass;
		if(monsterClass == BOS){
			monsterSpriteFile = BOS_SPRITE;
			//spells = ATK_SPELLS;
		}
		else{
			monsterSpriteFile = MIN_SPRITE;
			//spells = HLR_SPELLS;
		}
	}
	public int getMonsterID(){
		return monsterID;
	}
	
	public int getMonsterClass(){
		return monsterClass;
	}
	/*
	public String getSpell(int monsterClass, int spell){
		if(monsterClass == BOS)
			return ATK_SPELLS[spell];
		else
			return HLR_SPELLS[spell];
	}*/
	public int getmonsterHP(){
		return monsterHP;
	}

	public void setMonsterHP(int monsterHP){
		this.monsterHP = monsterHP;
	}
	
	public AnimatedSprite getMonsterSprite(){
		return monsterSprite;
	}
	
	public void setMonsterSprite(AnimatedSprite monsterSprite){
		this.monsterSprite = monsterSprite;
	}
	
}
