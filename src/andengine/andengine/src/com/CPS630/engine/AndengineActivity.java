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
    AnimatedSprite player;
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
    ArrayList <PlayerEntity> pEntityList= new ArrayList <PlayerEntity>();
    ArrayList <MonsterEntity> mEntityList= new ArrayList <MonsterEntity>();
    
	GestureDetector mGestureDetector;
	ChangeableText healthText;
	
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
        loadBackground(1);
        //load players sprite
        for(int i=0;i<pEntityList.size();i++){
        	scene.attachChild(pEntityList.get(i).getPlayerSprite());
        }
        
        //load monster sprite
        for(int i=0;i<mEntityList.size();i++){
        	scene.attachChild(mEntityList.get(i).getMonsterSprite());
        }
        player = new AnimatedSprite(100,100, this.mPlayerTextureRegion);
        
        player.setScale(2);
          
        loadSpellSign();
        loadSidebar();
		loadHPbar(player);
        mGestureDetector = new GestureDetector(this, new myGestureListener());

        scene.registerTouchArea(boss);

        scene.registerTouchArea(player);
        scene.setTouchAreaBindingEnabled(true); 
   
        scene.attachChild(healthText);

        scene.attachChild(player);
        scene.attachChild(lspell);
        scene.attachChild(rspell);
        scene.attachChild(uspell);
        scene.attachChild(dspell);
        scene.attachChild(boss);
        
       // scene.registerUpdateHandler(detect);
        
      /*  scene.registerUpdateHandler(new TimerHandler(1 / 20.0f, true, new ITimerCallback() {
            public void onTimePassed(final TimerHandler pTimerHandler) {
                    elapsedText.setText("Seconds elapsed: " + AndengineActivity.this.mEngine.getSecondsElapsedTotal());
                  
            }
    })); */
        return scene;
    }
    
    public void loadBackground(int location){
    	if(location==1)
    		bg = new Sprite(0, 0, this.mBackground);
    	else
    		bg = new Sprite(0, 0, this.mBackground);
    	
        scene.setBackground(new SpriteBackground(bg));
    }
    
    public void createPlayer(int playerID, int playerClass, int playerHP){
    	
    	AnimatedSprite player1;
    	PlayerEntity pEntity = new PlayerEntity(playerID, playerClass, playerHP);
    	
		if (playerClass==1)
    		player1 = new AnimatedSprite(50, 50, this.mPlayerTextureRegion);
    	else
    		player1 = new AnimatedSprite(50, 50, this.mPlayerTextureRegion);
		
    	pEntity.setPlayerSprite(player1);
    	HPBar pHP = new HPBar(0, 0, player1.getWidth(), 2, player1);
    	pHP.setBackColor(0, 0, 0, 1f);
        pHP.setHPColor(0, 1f, 0, 1f);
        pHP.setHP(playerHP);
        pEntity.setHPBar(pHP);
        pEntityList.add(pEntity);
        scene.attachChild(player1);
    }
    
    public void createMonster(int monsterID, int monsterType, int monsterHP, float mX, float mY){
    	AnimatedSprite monster;
    	MonsterEntity mEntity = new MonsterEntity(monsterID, monsterType, monsterHP);
    	
		if (monsterType == 1) {
			monster = new AnimatedSprite(mX, mY, this.mDevilTextureRegion) {
				@Override
				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
						final float pTouchAreaLocalX,
						final float pTouchAreaLocalY) {
					if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
						bossTouched = true;
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
						return true;
					}
					return false;
				}
			};
    	}
        
    	mEntity.setMonsterSprite(monster);
    	mEntityList.add(mEntity);
    	scene.attachChild(monster);
    	scene.registerTouchArea(monster);
    }
    
    public void move_pEntity(int playerID, float pX, float pY){
    	for(int i=0;i<pEntityList.size();i++){
        	if(pEntityList.get(i).getPlayerID()==playerID){
        		move(pEntityList.get(i).getPlayerSprite(),pX,pY);
        	}		
        }
    }
    
    public void move_mEntity(int monsterID, float pX, float pY){
    	for(int i=0;i<mEntityList.size();i++){
        	if(mEntityList.get(i).getMonsterID()==monsterID){
        		move(mEntityList.get(i).getMonsterSprite(),pX,pY);
        	}
        		
        }
    }
    
    public void on_pDamage(int playerID, int dValue){
    	int pHealth;
    	for(int i=0;i<pEntityList.size();i++){
        	if(pEntityList.get(i).getPlayerID()==playerID){
        		pHealth=pEntityList.get(i).getPlayerHP()-dValue;
        		pEntityList.get(i).setPlayerHP(pHealth);
        		pEntityList.get(i).getHPBar().setHP(pHealth);
        	}		
        }
    }
    
    public void on_mDamage(int monsterID, int dValue){
    	int mHealth;
    	for(int i=0;i<mEntityList.size();i++){
        	if(mEntityList.get(i).getMonsterID()==monsterID){
        		mHealth=mEntityList.get(i).getmonsterHP()-dValue;
        		mEntityList.get(i).setMonsterHP(mHealth);
        	}
        		
        }
    }
    
    public void loadSpellSign(){
    	final int centerX = (cameraWidth - this.dSpellTextureRegion.getWidth()) / 2;
        final int centerY = (cameraHeight - this.dSpellTextureRegion.getHeight()) / 2;
    	boss = new AnimatedSprite(centerX, centerY, this.mDevilTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {               	
            			bossTouched=true;  			
            			return true;
            		}
            		return false;
            }
        };
    	lspell = new AnimatedSprite(cameraWidth-50, 100, this.lSpellTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            			return true;
            		}
            		return false;
            }
        };
        dspell = new AnimatedSprite(cameraWidth-50, 175, this.dSpellTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
            	if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            		return true;
            	}
            	return false;
            }
        };
        
        
        uspell = new AnimatedSprite(cameraWidth-50, 250, this.uSpellTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
            	if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            		return true;
            	}
            	return false;
            }
        };
        rspell = new AnimatedSprite(cameraWidth-50, 325, this.rSpellTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
            	if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            		return true;
            	}
            	return false;
            }
        };
    }
    
	public void loadHPbar(AnimatedSprite sprite){
    	 playerHP = new HPBar(0, 0, sprite.getWidth(), 2, sprite);
         playerHP.setBackColor(0, 0, 0, 1f);
         playerHP.setHPColor(0, 1f, 0, 1f);
         playerHP.setHP(health);
         //sprite.attachChild(playerHP);
    }
	
    public void loadSidebar(){	
    	healthText = new ChangeableText(cameraWidth-75, 30, this.mFont, "5000", "XXXX".length());
        
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
    
   /* IUpdateHandler detect = new IUpdateHandler() {
		public void reset() {
		}

		public void onUpdate(float pSecondsElapsed) {
			boolean hit = false;
			if (projectile != null) {
				if (projectile.getX() >= mCamera.getWidth()
						|| projectile.getY() >= mCamera.getHeight()
								+ projectile.getHeight()
						|| projectile.getY() <= -projectile.getHeight()) {
					removeSprite(projectile);
				}

				if (boss.collidesWith(projectile)) {
					removeSprite(projectile);
					hit = true;
				}
				if (hit) {
					removeSprite(boss);
					hit = false;
				}
			}
		}
	};*/
    
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
    
    public boolean onTouchEvent(MotionEvent event) {
    	if (mGestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
    }
    
    public void attackAnimate(AnimatedSprite sprite) {

    	sprite.animate(new long[] { 10, 20, 50, 20 }, new int[] { 1, 3, 6 , 5 },  10);
	}

	public void runAnimate(AnimatedSprite sprite, float destX, float destY) {

		destX -= sprite.getWidth() / 2;
		destY -= sprite.getHeight() / 2;

		while (sprite.getX() != destX && sprite.getY() != destY) {
			sprite.animate(new long[] {100, 100, 100}, new int[] { 3, 5, 4 }, 100);
		}
		sprite.stopAnimation();
	}
	
	public void removeSprite(final AnimatedSprite _sprite) {
		runOnUpdateThread(new Runnable() {
			public void run() {
				scene.detachChild(_sprite);
			}
		});
	}
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
				player.clearEntityModifiers();
				move(player,dest_X, dest_Y);
				runAnimate(player, dest_X, dest_Y);
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
			Toast.makeText(AndengineActivity.this, "onSwipeUp", Toast.LENGTH_SHORT).show();
			//elapsedText.setText("Water Atk");
			return true;
		}
		else
			return false;
	}

	boolean onSwipeRight() {
		Toast.makeText(AndengineActivity.this, "onSwipeRight", Toast.LENGTH_SHORT).show();	
		if(bossTouched){
			//elapsedText.setText("Fire Atk");	
		}
		return true;
	}

	boolean onSwipeLeft() {
		Toast.makeText(AndengineActivity.this, "onSwipeLeft", Toast.LENGTH_SHORT).show();
		if(bossTouched){
			//elapsedText.setText("Wind Atk");	
			attackAnimate(player);	
		}
		return true;
	}

	boolean onSwipeDown() {
		Toast.makeText(AndengineActivity.this, "onSwipeDown", Toast.LENGTH_SHORT).show();
		if(bossTouched){
			//elapsedText.setText("Earth Atk");	
		}
		return true;
	}
}

