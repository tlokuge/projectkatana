package com.katana.splash;

import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public class MonsterEntity extends CharacterEntity{
	
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
	private int monsterHP;
	
	//private String monsterSpriteFile;	
	private String[] spells;
	private AnimatedSprite monsterSprite;
	
	public MonsterEntity(int monsterID, int monsterType, int monsterHP){
		super(monsterID, monsterHP);
		this.monsterType=monsterType;
		this.setType("monster");
		/*
		if(monsterType == BOS){
			monsterSpriteFile = BOS_SPRITE;
			//spells = ATK_SPELLS;
		}
		else{
			monsterSpriteFile = MIN_SPRITE;
			//spells = HLR_SPELLS;
		}
		*/
	}
	
	public int getMonsterType(){
		return monsterType;
	}
	/*
	public String getSpell(int monsterClass, int spell){
		if(monsterClass == BOS)
			return ATK_SPELLS[spell];
		else
			return HLR_SPELLS[spell];
	}*/
	
	
}
