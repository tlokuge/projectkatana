package com.CPS630.engine;

import java.util.ArrayList;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontManager;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public class AndengineActivity extends BaseGameActivity {

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TiledTextureRegion uSpellTextureRegion;
    private TiledTextureRegion dSpellTextureRegion;
    private TiledTextureRegion lSpellTextureRegion;
    private TiledTextureRegion rSpellTextureRegion;
 
    private TiledTextureRegion mDevilTextureRegion;

    private TiledTextureRegion mPlayerTextureRegion;
    
    //for custom background
    private BitmapTextureAtlas mBgRegion;
    private TextureRegion mBackground;
    
    float realMoveDuration;
    //sprite files
    String uSpellfile = "face_box.png";
    String dSpellfile = "face_box.png";
    String lSpellfile = "face_box.png";
    String rSpellfile = "face_box.png";
    String background = "Background.png";
    
    AnimatedSprite face;
    
    AnimatedSprite boss;
    AnimatedSprite lspell,rspell,uspell,dspell;
    Sprite bg;
    Sprite projectile;
    Scene scene = new Scene();
	HPBar playerHP;
    
    private BitmapTextureAtlas mFontTexture;
    private Font mFont;
   
    int cameraWidth;
    int cameraHeight;
    boolean bossTouched=false;
    ArrayList <CharacterEntity> EntityList= new ArrayList <CharacterEntity>();
    
	GestureDetector mGestureDetector;
	ChangeableText healthText;
	
	AnimatedSprite user;
	int userID;
	boolean playerSelected;
	private int health=5000;
        
	public Engine onLoadEngine() {
		final Display display = getWindowManager().getDefaultDisplay();
		cameraWidth = display.getWidth();
		cameraHeight = display.getHeight();
		mCamera = new Camera(0, 0, cameraWidth, cameraHeight);

		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
				new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera));

	}

    public void onLoadResources() {
    	
    	/* The font */
        this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.WHITE);
        this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
        this.getFontManager().loadFont(this.mFont);
        
        loadBitmaps();
        
        this.mEngine.getTextureManager().loadTexture(this.mBgRegion);
        this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }
    
    public void loadBitmaps(){
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        
    	this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA );
    	
    	//create background bitmap texture
    	this.mBgRegion = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.mBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBgRegion, this,"Background.png", 0, 0);
        
        this.uSpellTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, uSpellfile, 400, 0, 1, 1);
        this.dSpellTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, dSpellfile, 400, 50, 1, 1);
        this.lSpellTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, lSpellfile, 400, 100, 1, 1);
        this.rSpellTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, rSpellfile, 400, 150, 1, 1);
        
        this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "player.png",170 , 0, 3, 4);

        this.mDevilTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "download.jpeg", 0, 0, 1, 1);
    }
    
    public Scene onLoadScene() {

    	this.mEngine.registerUpdateHandler(new FPSLogger());        
        scene.setBackgroundEnabled(true);     	    
        mGestureDetector = new GestureDetector(this, new myGestureListener());
        scene.setTouchAreaBindingEnabled(true); 
        return scene;
    }
    
    //setup background of the game base on location
    public void loadBackground(int location){
    	if(location==1)
    		bg = new Sprite(0, 0, this.mBackground);
    	else
    		bg = new Sprite(0, 0, this.mBackground);
    	
        scene.setBackground(new SpriteBackground(bg));
    }
    
    //creating player himself
    public void createUserChar(int playerID, int playerClass, int playerHP, float pX, float pY){
    	
    	final PlayerEntity pEntity = new PlayerEntity(playerID, playerClass, playerHP);
    	userID=playerID;
    	
		if (playerClass==1)
    		user = new AnimatedSprite(pX, pY, this.mPlayerTextureRegion){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX,
					final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					pEntity.setSelected(true);
					return true;
				}
				return false;
			}
		};
    	else
    		user = new AnimatedSprite(pX, pY, this.mPlayerTextureRegion){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX,
					final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					pEntity.setSelected(true);
					return true;
				}
				return false;
			}
		};
		pEntity.setSelected(playerSelected);
		pEntity.setCharSprite(user);
    	HPBar pHP = new HPBar(0, 0, user.getWidth(), 2, user);
    	pHP.setBackColor(0, 0, 0, 1f);
        pHP.setHPColor(0, 1f, 0, 1f);
        pHP.setHP(playerHP);
        pEntity.setHPBar(pHP);
        EntityList.add(pEntity);
        user.setScale(2);
        scene.attachChild(user);
        scene.registerTouchArea(user);
    }
    //create teammate
    public void createTeammate(int playerID, int playerClass, int playerHP, float pX, float pY){
    	
    	AnimatedSprite players;
    	final PlayerEntity pEntity = new PlayerEntity(playerID, playerClass, playerHP);
    	
		if (playerClass==1)
    		players = new AnimatedSprite(pX, pY, this.mPlayerTextureRegion){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX,
					final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					pEntity.setSelected(true);
					return true;
				}
				return false;
			}
		};
    	else
    		players = new AnimatedSprite(pX, pY, this.mPlayerTextureRegion){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX,
					final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					pEntity.setSelected(true);
					return true;
				}
				return false;
			}
		};
		
    	pEntity.setCharSprite(players);
    	HPBar pHP = new HPBar(0, 0, players.getWidth(), 2, players);
    	pHP.setBackColor(0, 0, 0, 1f);
        pHP.setHPColor(0, 1f, 0, 1f);
        pHP.setHP(playerHP);
        pEntity.setHPBar(pHP);
        EntityList.add(pEntity);
        scene.attachChild(players);
        scene.registerTouchArea(players);
    }
    //creating monster
    public void createMonster(int monsterID, int monsterType, int monsterHP, float mX, float mY){
    	AnimatedSprite monster;
    	final MonsterEntity mEntity = new MonsterEntity(monsterID, monsterType, monsterHP);
    	
		if (monsterType == 1) {
			monster = new AnimatedSprite(mX, mY, this.mDevilTextureRegion) {
				@Override
				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
						final float pTouchAreaLocalX,
						final float pTouchAreaLocalY) {
					if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
						bossTouched = true;
						mEntity.setSelected(true);
						return true;
					}
					return false;
				}
			};
		} else {
			monster = new AnimatedSprite(mX, mY, this.mDevilTextureRegion) {
				@Override
				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
						final float pTouchAreaLocalX,
						final float pTouchAreaLocalY) {
					if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
						bossTouched = true;
						mEntity.setSelected(true);
						return true;
					}
					return false;
				}
			};
    	}
        
    	mEntity.setCharSprite(monster);
    	EntityList.add(mEntity);
    	scene.attachChild(monster);
    	scene.registerTouchArea(monster);
    }
    //move the characters on screen for both monster and player
    public void move_Entity(int ID, float pX, float pY){
    	for(int i=0;i<EntityList.size();i++){
        	if(EntityList.get(i).getCharID()==ID){
        		move(EntityList.get(i).getCharSprite(),pX,pY);
        		break;
        	}		
        }
    }
    //update HP 
    public void update_pHP(int ID, int hp){
 
    	for(int i=0;i<EntityList.size();i++){
        	if(EntityList.get(i).getCharID()==ID){	
        		EntityList.get(i).setCharHP(hp); //may not be needed
        		((PlayerEntity) EntityList.get(i)).getHPBar().setHP(hp);
        		break;
        	}		
        }
    }

     //method to send spell
    public void send_spell(int caster, int targetID, int spellID){
    	
    }
    
    //destroy character
    public void remove_char(int ID){
    	
    	for(int i=0;i<EntityList.size();i++){
        	if(EntityList.get(i).getCharID()==ID){
        		removeSprite(EntityList.get(i).getCharSprite());
        		EntityList.remove(i);
        		break;
        	}		
        }
    	
    }
    //casting spell
    public void spellCasting(int casterID, int targetID, int spellID){	
    	for(int i=0;i<EntityList.size();i++){
        	if(EntityList.get(i).getCharID()==casterID){
        		if(EntityList.get(i).getType()=="monster"){
	        		if(spellID==1){
	        			//do monster spell1 atk animation
	        		}	
	        	    if(spellID==2){
	        	    	//do monster spell2 atk animation
	        	    }
	        	    if(spellID==3){
	        			//do monster spell3 atk animation
	        		}	
	        	    if(spellID==4){
	        	    	//do monster spell4 atk animation
	        	    }
        		}
        		else{
        			if(spellID==1){
	        			//do player spell1 atk animation
	        		}	
	        	    if(spellID==2){
	        	    	//do player spell2 atk animation
	        	    }
	        	    if(spellID==3){
	        			//do player spell3 atk animation
	        		}	
	        	    if(spellID==4){
	        	    	//do player spell4 atk animation
	        	    }
        		}
        	}
        	if(EntityList.get(i).getCharID()==targetID){
        		if(EntityList.get(i).getType()=="monster"){
	        		if(spellID==1){
	        			//do monster spell1 dmg animation
	        		}	
	        	    if(spellID==2){
	        	    	//do monster spell2 dmg animation
	        	    }
	        	    if(spellID==3){
	        			//do monster spell3 dmg animation
	        		}	
	        	    if(spellID==4){
	        	    	//do monster spell4 dmg animation
	        	    }
        		}
        		else{
					if (spellID == 1) {
						// do player spell1 dmg animation
					}
					if (spellID == 2) {
						// do player spell2 dmg animation
					}
					if (spellID == 3) {
						// do player spell3 dmg animation
					}
					if (spellID == 4) {
						// do player spell4 dmg animation
					}
        		}
        	}	
        }
    }
    //load up spell display
    public void loadSpellDisplay(){
    	for(int i=0;i<EntityList.size();i++){
        	if(EntityList.get(i).getCharID()==userID){
        		if(((PlayerEntity) EntityList.get(i)).getPlayerClass()==1){
        			lspell = new AnimatedSprite(cameraWidth-50, 100, this.lSpellTextureRegion);
        	        dspell = new AnimatedSprite(cameraWidth-50, 175, this.dSpellTextureRegion);
        	        uspell = new AnimatedSprite(cameraWidth-50, 250, this.uSpellTextureRegion);
        	        rspell = new AnimatedSprite(cameraWidth-50, 325, this.rSpellTextureRegion);
        		}
        		else{
        			lspell = new AnimatedSprite(cameraWidth-50, 100, this.lSpellTextureRegion);
        	        dspell = new AnimatedSprite(cameraWidth-50, 175, this.dSpellTextureRegion);
        	        uspell = new AnimatedSprite(cameraWidth-50, 250, this.uSpellTextureRegion);
        	        rspell = new AnimatedSprite(cameraWidth-50, 325, this.rSpellTextureRegion);
        		}
        		scene.attachChild(lspell);
                scene.attachChild(rspell);
                scene.attachChild(uspell);
                scene.attachChild(dspell);
                break;
        			
        	}		
        }
    	
    }
    //load up HP display
    public void load_HPdisplay(){	
    	healthText = new ChangeableText(cameraWidth-75, 30, this.mFont, "5000", "XXXX".length());
        scene.attachChild(healthText);
    	lspell.setScale(2);
        rspell.setScale(2);
        dspell.setScale(2);
        uspell.setScale(2);
    }
    
    public void onLoadComplete() {

    }
    
    public FontManager getFontManager() {
        	return this.mEngine.getFontManager();
    }
    
    //This move class is for andengine itselfs, not for server uses
    private void move(AnimatedSprite sprite, float dest_spriteX, float dest_spriteY ) {
    	
    	float curr_spriteX= sprite.getX();
    	float curr_spriteY= sprite.getY();
    	
        dest_spriteX-= sprite.getWidth()/2;
        dest_spriteY-= sprite.getHeight()/2;
        
        int offX = (int) (dest_spriteX - curr_spriteX);
        int offY = (int) (dest_spriteY - curr_spriteY);
        
        float length = (float) Math.sqrt((offX * offX)
            + (offY * offY));
        float velocity = 200.0f / 1.0f; // 480 pixels / 1 sec
        realMoveDuration = length / velocity;

        if(!(offX==0 && offY==0)){
	        MoveModifier mod = new MoveModifier(realMoveDuration, curr_spriteX, dest_spriteX, curr_spriteY, dest_spriteY);
	        sprite.registerEntityModifier(mod.deepCopy());
	    }
  
    }
    //gesture detection
    public boolean onTouchEvent(MotionEvent event) {
    	if (mGestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
    }
    //Placeholder for animation class
    public void attackAnimate(AnimatedSprite sprite) {

    	sprite.animate(new long[] { 10, 20, 50, 20 }, new int[] { 1, 3, 6 , 5 },  10);
	}
  //Placeholder for animation class
	public void runAnimate(AnimatedSprite sprite, float destX, float destY) {

		destX -= sprite.getWidth() / 2;
		destY -= sprite.getHeight() / 2;

		while (sprite.getX() != destX && sprite.getY() != destY) {
			sprite.animate(new long[] {100, 100, 100}, new int[] { 3, 5, 4 }, 100);
		}
		sprite.stopAnimation();
	}
	//remove method for andengine uses
	public void removeSprite(final AnimatedSprite _sprite) {
		runOnUpdateThread(new Runnable() {
			public void run() {
				scene.detachChild(_sprite);
			}
		});
	}
	//remove method for andengine uses
	public void removeSprite(final Sprite _sprite) {
		runOnUpdateThread(new Runnable() {
			public void run() {
				scene.detachChild(_sprite);
			}
		});
	}
	//Gesture class
	class myGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent ev) {
			
			float dest_X=ev.getX();
			float dest_Y=ev.getY();
			
			if(!bossTouched)
			{
				user.clearEntityModifiers();
				move(user,dest_X, dest_Y);
				runAnimate(user, dest_X, dest_Y);
			}
			else{

				bossTouched=false;		
			}
		
			return true;
		}
		
		@Override
		public boolean onDoubleTap(MotionEvent ev){
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent ev) {
		}

		@Override
		public void onLongPress(MotionEvent ev) {
			//Toast.makeText(AndengineActivity.this, "Long press.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			//Toast.makeText(AndengineActivity.this, "Scroll.", Toast.LENGTH_SHORT).show();
			return false;
		}

		@Override
		public boolean onDown(MotionEvent ev) {
			//Toast.makeText(AndengineActivity.this, "Down.", Toast.LENGTH_SHORT).show();
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			final float swipeMinDistance = 80;

            final boolean isHorizontalFling = Math.abs(velocityX) > Math.abs(velocityY);

            if(isHorizontalFling) {
                    if(e1.getX() - e2.getX() > swipeMinDistance) {
                            return AndengineActivity.this.onSwipeLeft();
                    } else if(e2.getX() - e1.getX() > swipeMinDistance) {
                            return AndengineActivity.this.onSwipeRight();
                    }
            } else {
                    if(e1.getY() - e2.getY() > swipeMinDistance) {
                            return AndengineActivity.this.onSwipeUp();
                    } else if(e2.getY() - e1.getY() > swipeMinDistance) {
                            return AndengineActivity.this.onSwipeDown();
                    }
            }

            return false;

		}
	}
   
	boolean onSwipeUp() {
		if(bossTouched){
			//Toast.makeText(AndengineActivity.this, "onSwipeUp", Toast.LENGTH_SHORT).show();
			for(int i=0;i<EntityList.size();i++){
	        	if(EntityList.get(i).getSelected()==true){	
	    			send_spell(userID, EntityList.get(i).getCharID(), 1);
	        	}		
	        }
			return true;
		}
		else
			return false;
	}

	boolean onSwipeRight() {
		Toast.makeText(AndengineActivity.this, "onSwipeRight", Toast.LENGTH_SHORT).show();	
		if(bossTouched){
			for(int i=0;i<EntityList.size();i++){
	        	if(EntityList.get(i).getSelected()==true){	
	    			send_spell(userID, EntityList.get(i).getCharID(), 2);
	        	}		
	        }	
		}
		return true;
	}

	boolean onSwipeLeft() {
		Toast.makeText(AndengineActivity.this, "onSwipeLeft", Toast.LENGTH_SHORT).show();
		if(bossTouched){
			for(int i=0;i<EntityList.size();i++){
	        	if(EntityList.get(i).getSelected()==true){	
	    			send_spell(userID, EntityList.get(i).getCharID(), 3);
	        	}		
	        }
		}
		return true;
	}

	boolean onSwipeDown() {
		Toast.makeText(AndengineActivity.this, "onSwipeDown", Toast.LENGTH_SHORT).show();
		if(bossTouched){
			for(int i=0;i<EntityList.size();i++){
	        	if(EntityList.get(i).getSelected()==true){	
	    			send_spell(userID, EntityList.get(i).getCharID(), 4);
	        	}		
	        }
		}
		return true;
	}
}

