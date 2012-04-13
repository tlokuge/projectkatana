package com.katana.splash;

import java.util.ArrayList;
import java.util.HashMap;

import katana.game.CharacterEntity;
import katana.game.HPBar;
import katana.game.MonsterEntity;
import katana.game.PlayerEntity;
import katana.game.Unit;
import katana.receivers.KatanaReceiver;
import katana.receivers.LocationReceiver;
import katana.services.KatanaService;
import katana.services.KatanaService.KatanaSBinder;
import katana.shared.KatanaConstants;
import katana.shared.KatanaPacket;
import katana.shared.Opcode;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.IBinder;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public class GameActivity extends BaseGameActivity implements IOnSceneTouchListener {

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TiledTextureRegion uSpellTextureRegion;
    private TiledTextureRegion dSpellTextureRegion;
    private TiledTextureRegion lSpellTextureRegion;
    private TiledTextureRegion rSpellTextureRegion;
 
    private TiledTextureRegion mMonster1TextureRegion;
    private TiledTextureRegion mMonster2TextureRegion;
    private TiledTextureRegion mMonster3TextureRegion;

    private TiledTextureRegion mPlayerTextureRegion;
    private TiledTextureRegion mPlayer2TextureRegion;
    
    //for custom background
    private BitmapTextureAtlas mBgRegion;
    private TextureRegion mBackground;
    
    float realMoveDuration;
    //sprite files
    String uSpellfile = "uspell.png";
    String dSpellfile = "dspell.png";
    String lSpellfile = "lspell.png";
    String rSpellfile = "rspell.png";
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
	
	private final String BASE_PATH = "gfx/";
	private final String USPELL_FILE = "uspell.png";
	private final String DSPELL_FILE = "dspell.png";
	private final String LSPELL_FILE = "lspell.png";
	private final String RSPELL_FILE = "rspell.png";
	
	private final int BITMAP_SQUARE = 2048;
	private final int BITMAP_STEP = 350;
	private final int NUM_ANIMS = 5;

	private Unit player;
	private Unit selected;
	private HashMap<Integer, Unit> unit_map = new HashMap<Integer, Unit>();
	private int bitmap_x = 0;
	private int bitmap_y = 0;
	
	AnimatedSprite user;
	int userID;
	boolean playerSelected;
	private int health=5000;
       
	/** ANDROID ACTIVITY LIFECYCLE **/
	/**       DO NOT REMOVE        **/
	protected void onStart() {
		super.onStart();
	}
	
	public Engine onLoadEngine() {
		
		doBindService();
		System.out.println("onLoadEngine: " + katanaService);
		
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
        
        loadStaticBitmaps();
        
        this.mEngine.getTextureManager().loadTexture(this.mBgRegion);
        this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }
    
    public void loadStaticBitmaps()
    {
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(BASE_PATH);
    	
    	mBitmapTextureAtlas = new BitmapTextureAtlas(BITMAP_SQUARE, BITMAP_SQUARE, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    	mBgRegion = new BitmapTextureAtlas(BITMAP_SQUARE, BITMAP_SQUARE, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    	
    	uSpellTextureRegion = createTexture(USPELL_FILE, 1);
    	dSpellTextureRegion = createTexture(DSPELL_FILE, 1);
    	lSpellTextureRegion = createTexture(LSPELL_FILE, 1);
    	rSpellTextureRegion = createTexture(RSPELL_FILE, 1);
    }
    
  /*  public void loadBitmaps(){
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        
    	this.mBitmapTextureAtlas = new BitmapTextureAtlas(2048, 2048, TextureOptions.BILINEAR_PREMULTIPLYALPHA );
    	
    	//create background bitmap texture
    	this.mBgRegion = new BitmapTextureAtlas(2048, 2048, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.mBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBgRegion, this,"background3.png", 0, 0);
        
        this.uSpellTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, uSpellfile, 400, 0, 1, 1);
        this.dSpellTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, dSpellfile, 400, 50, 1, 1);
        this.lSpellTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, lSpellfile, 400, 100, 1, 1);
        this.rSpellTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, rSpellfile, 400, 150, 1, 1);
        
        this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "attack.png",600 , 0, 5, 1);
        this.mPlayer2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "healer.png", 1000, 0, 5, 1);

        this.mMonster1TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "monster1.png", 0, 0, 1, 1);
        this.mMonster2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "monster2.png", 0, 600, 1, 1);
        this.mMonster3TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "monster3.png", 0, 1000, 1, 1);
    }*/
    
    public Scene onLoadScene() {
        //setting up screen here
    	
    	this.mEngine.registerUpdateHandler(new FPSLogger());
    	scene.setOnSceneTouchListener(this);
        scene.setBackgroundEnabled(true);
        
        //testing method, will be erase when linked with server
        /*loadBackground(1);
        createUserChar(1, 1, 5000, 100, 100);
        createTeammate(2, 2, 5000, 100, 400);
    
        createMonster(3, 1, 9000, 600, 200);
        createMonster(4, 2, 2000, 500, 200);*/
        loadSpellDisplay();
        load_HPdisplay();
        
        //testing method ends
        
        //initialize gesture detector
        mGestureDetector = new GestureDetector(this, new myGestureListener());
        //scene.setTouchAreaBindingEnabled(true); 

		
		userID = getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE).getInt(KatanaService.EXTRAS_PLAYERID, 0);
		if(userID == 0)
		{
			userID = KatanaService.player_id;
			System.err.println("YAY HACKY");
		}
		System.err.println(userID + " USERID");
		katanaService.sendPacket(new KatanaPacket(Opcode.C_GAME_READY));
        return scene;
    }
    
    public void setBackground(String background)
    {
    	mBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBgRegion, this, background, 0, 0);
    	bg = new Sprite(0, 0, mBackground);
    	scene.setBackground(new SpriteBackground(bg));
    	
    	System.out.println("Setting background to " + background);
    }
    
    //setup background of the game base on location
    public void loadBackground(int location){
    	if(location==1)
    		bg = new Sprite(0, 0, this.mBackground);
    	else
    		bg = new Sprite(0, 0, this.mBackground);
    	
        scene.setBackground(new SpriteBackground(bg));
    }
    
    private TiledTextureRegion createTexture(final String file, int animations)
    {
    	int bx = bitmap_x;
    	int by = bitmap_y;
    	System.out.println("file: " + file + " - bx: " + bx + " by: " + by);
    	stepBitmapCoordinates();
    	System.out.println("bitx: " + bitmap_x + " bity: " + bitmap_y);
    	
    	return BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, file, bx, by, animations, 1);
    }
    
    private void stepBitmapCoordinates()
    {
    	if((bitmap_x + BITMAP_STEP) > BITMAP_SQUARE)
    	{
    		bitmap_x = 0;
    		bitmap_y += BITMAP_STEP;
    	}
    	else
    		bitmap_x += BITMAP_STEP;
    }
    
    public void createUnits(ArrayList<String> unitList)
    {
    	// id, health, model
    	for(String unit_str : unitList)
    	{
    		String[] data = unit_str.split(";");
    		if(data.length < 3)
    		{
    			System.err.println("GameActivity: Received invalid unit data string: '" + unit_str + "'");
    			continue;
    		}
    		
    		try
    		{
    			Unit u = spawnUnit(Integer.parseInt(data[0].trim()), Integer.parseInt(data[1].trim()), data[2], 0, 0);
    			
    			System.out.println("GameActivity: Created unit[" + u + "]");
    		}
    		catch(NumberFormatException ex)
    		{
    			System.err.println("GameActivity: " + ex.getLocalizedMessage());
    		}
    	}
    }
    
    public Unit spawnUnit(int id, int health, String model_name, int pos_x, int pos_y)
    {
    	TiledTextureRegion texture = createTexture(model_name, NUM_ANIMS);
    	final Unit unit = new Unit(id, health);
    	AnimatedSprite model = new AnimatedSprite((float)pos_x, (float)pos_y, texture)
    	{
    		@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,	final float local_x, final float local_y) 
    		{
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN)
				{
					selected = unit;
					return true;
				}
				return false;
			}
		};
		
		unit.setModel(model);
		model.setScale(1);
		scene.attachChild(model);
		scene.registerTouchArea(model);
		
		unit_map.put(id, unit);
		
    	return unit;
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
					if(bossTouched){
						bossTouched = false;
					}
					else{
						bossTouched = true;
					}
					return true;
				}
				return false;
			}
		};
    	else
    		user = new AnimatedSprite(pX, pY, this.mPlayer2TextureRegion){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX,
					final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					pEntity.setSelected(true);
					if(bossTouched){
						bossTouched = false;
					}
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
        user.setScale(1);
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
					if(bossTouched){
						bossTouched = false;
					}
					else{
						bossTouched = true;
					}
					return true;
				}
				return false;
			}
		};
    	else
    		players = new AnimatedSprite(pX, pY, this.mPlayer2TextureRegion){
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX,
					final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					pEntity.setSelected(true);
					if(bossTouched){
						bossTouched = false;
					}
					
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
			monster = new AnimatedSprite(mX, mY, this.mMonster1TextureRegion) {
				@Override
				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
						final float pTouchAreaLocalX,
						final float pTouchAreaLocalY) {
					if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
						if(bossTouched){
							bossTouched = false;
						}
						else{
							bossTouched = true;
						}
						
						mEntity.setSelected(true);				
					}
					return true;
				}
			};
		} else {
			monster = new AnimatedSprite(mX, mY, this.mMonster2TextureRegion) {
				@Override
				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
						final float pTouchAreaLocalX,
						final float pTouchAreaLocalY) {
					if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
						if(bossTouched){
							bossTouched = false;
						}
						else{
							bossTouched = true;
						}
						
						mEntity.setSelected(true);
					}
					return true;
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
    public void send_spell(int casterID, int targetID, int spellID){
    	
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
        				spell1_Animate(EntityList.get(i).getCharSprite());
	        		}	
	        	    if(spellID==2){
	        	    	//do player spell2 atk animation
	        	    	spell2_Animate(EntityList.get(i).getCharSprite());
	        	    }
	        	    if(spellID==3){
	        			//do player spell3 atk animation
	        	    	spell3_Animate(EntityList.get(i).getCharSprite());
	        		}	
	        	    if(spellID==4){
	        	    	//do player spell4 atk animation
	        	    	spell4_Animate(EntityList.get(i).getCharSprite());
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
    public void loadSpellDisplay()
	{
    	uspell = new AnimatedSprite(cameraWidth - 50, 250, uSpellTextureRegion);
    	dspell = new AnimatedSprite(cameraWidth - 50, 175, dSpellTextureRegion);
    	lspell = new AnimatedSprite(cameraWidth - 50, 100, lSpellTextureRegion);
    	rspell = new AnimatedSprite(cameraWidth - 50, 325, rSpellTextureRegion);
    	
		scene.attachChild(lspell);
	    scene.attachChild(rspell);
	    scene.attachChild(uspell);
	    scene.attachChild(dspell);    	
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
    	
    	sprite.setCurrentTileIndex(0);
    	sprite.clearEntityModifiers();
    	
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
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
    		
    	mGestureDetector.onTouchEvent(pSceneTouchEvent.getMotionEvent());
		return true;
    }

    //Placeholder for animation class
    public void spell1_Animate(AnimatedSprite sprite) {
    	sprite.animate(new long[] {100,100,100}, new int[] {0,1,0} ,10);
    	//sprite.setRotation(90);
	}
    
    public void spell2_Animate(AnimatedSprite sprite) {
    	sprite.animate(new long[] {100,100,100}, new int[] {0,2,0} ,10);
	}
    
    public void spell3_Animate(AnimatedSprite sprite) {
    	sprite.animate(new long[] {100,100,100}, new int[] {0,3,0} ,10);
	}
    
    public void spell4_Animate(AnimatedSprite sprite) {
    	sprite.animate(new long[] {100,100,100}, new int[] {0,4,0} ,10);
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
		public boolean onSingleTapUp(MotionEvent ev) 
		{
			Unit user = unit_map.get(userID);
			System.out.println("userID: " + userID + " - " + user);
			AnimatedSprite sprite = user.getSprite();
			System.out.println("Sprite: " + sprite);
			move(sprite, ev.getX(), ev.getY());
		
			return true;
		}
		
		@Override
		public boolean onDoubleTap(MotionEvent ev){
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent ev) {
			//Toast.makeText(AndengineActivity.this, "show press.", Toast.LENGTH_SHORT).show();
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
                            return GameActivity.this.onSwipeLeft();
                    } else if(e2.getX() - e1.getX() > swipeMinDistance) {
                            return GameActivity.this.onSwipeRight();
                    }
            } else {
                    if(e1.getY() - e2.getY() > swipeMinDistance) {
                            return GameActivity.this.onSwipeUp();
                    } else if(e2.getY() - e1.getY() > swipeMinDistance) {
                            return GameActivity.this.onSwipeDown();
                    }
            }

            return false;

		}
	}
   
	boolean onSwipeUp() {
		Toast.makeText(GameActivity.this, "onSwipeUp", Toast.LENGTH_SHORT).show();
		if(bossTouched){
			spell1_Animate(user);		
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
		Toast.makeText(GameActivity.this, "onSwipeRight", Toast.LENGTH_SHORT).show();	
		if(bossTouched){
			spell2_Animate(user);
			for(int i=0;i<EntityList.size();i++){
	        	if(EntityList.get(i).getSelected()==true){	
	    			send_spell(userID, EntityList.get(i).getCharID(), 2);
	        	}		
	        }	
		}
		return true;
	}

	boolean onSwipeLeft() {
		Toast.makeText(GameActivity.this, "onSwipeLeft", Toast.LENGTH_SHORT).show();
		if(bossTouched){
			spell3_Animate(user);
			for(int i=0;i<EntityList.size();i++){
	        	if(EntityList.get(i).getSelected()==true){	
	    			send_spell(userID, EntityList.get(i).getCharID(), 3);
	        	}		
	        }
		}
		return true;
	}

	boolean onSwipeDown() {
		Toast.makeText(GameActivity.this, "onSwipeDown", Toast.LENGTH_SHORT).show();
		if(bossTouched){
			spell4_Animate(user);
			for(int i=0;i<EntityList.size();i++){
	        	if(EntityList.get(i).getSelected()==true){	
	    			send_spell(userID, EntityList.get(i).getCharID(), 4);
	        	}		
	        }
		}
		return true;
	}
    
    // --------------------------------------------------- //
    // REQUIRED FOR KATANA SERVICE DO NOT CHANGE OR REMOVE //
    // --------------------------------------------------- //
    private KatanaService katanaService;
    private KatanaReceiver katanaReceiver;
    private boolean serviceBound;
    
	private void doBindService() {
		System.err.println("doBindService");
	    katanaReceiver = new KatanaReceiver(3);
	    Intent intent = new Intent(this,KatanaService.class);
	    bindService(intent, katanaConnection, Context.BIND_AUTO_CREATE);
	    registerReceiver(katanaReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
	}
	
	private void doUnbindService() {
	    if (serviceBound) {
	        // Detach our existing connection and broadcast receiver
	        unbindService(katanaConnection);
	        unregisterReceiver(katanaReceiver);
	        serviceBound = false;
	    }
	}
	
	    @SuppressWarnings("unused")
	    private void doKillService() {
	    unbindService(katanaConnection);
	    unregisterReceiver(katanaReceiver);
	    stopService(new Intent(this, KatanaService.class));
	    serviceBound = false;
	}
	    
	private ServiceConnection katanaConnection = new ServiceConnection() {
	    @Override
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        KatanaSBinder binder = (KatanaSBinder) service;
	        katanaService = binder.getService();
	        serviceBound = true;
	        System.err.println("onServiceConnected: " + katanaService);
	    }
	
	    @Override
	    public void onServiceDisconnected(ComponentName arg0) {
	        serviceBound = false;
	    }
	};
	// --------------------------------------------------- //
    // ---------------- END KATANASERVICE ---------------- //
    // --------------------------------------------------- //
}

