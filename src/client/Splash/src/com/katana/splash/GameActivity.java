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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
    
    //for custom background
    private BitmapTextureAtlas mBgRegion;
    private TextureRegion mBackground;
    
    private String background = "Background.png";
    private ArrayList<String> temp_unit_string_list;

    Sprite bg;
    Sprite projectile;
    Scene scene = new Scene();
	HPBar playerHP;
    
    private BitmapTextureAtlas mFontTexture;
    private Font mFont;
   
    int cameraWidth;
    int cameraHeight;
    
	GestureDetector mGestureDetector;
	ChangeableText healthText;
	
	private final String BASE_PATH = "gfx/";
	
	private final int BITMAP_SQUARE = 2048;
	private final int BITMAP_STEP = 512;
	private final int NUM_ANIMS = 5;

	private HashMap<Integer, Unit> unit_map = new HashMap<Integer, Unit>();
	private int bitmap_x = 0;
	private int bitmap_y = 0;
	
	int userID;
       
	/** ANDROID ACTIVITY LIFECYCLE **/
	/**       DO NOT REMOVE        **/
	protected void onStart() {
		super.onStart();
	}
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		System.out.println(bundle);
		background = bundle.getString(KatanaService.EXTRAS_GAMEBG);
		temp_unit_string_list = bundle.getStringArrayList(KatanaService.EXTRAS_GAMESTART);
	}
	
	public void onBackPressed()
	{
		super.onBackPressed();
		Log.d("CDA", "onBackPressed");
		System.err.println("onBackPressed");
		katanaService.sendPacket(new KatanaPacket(Opcode.C_LOGOUT));
		doKillService();
		finish();
	}
	
	public void onPause()
	{
		super.onPause();
		Log.d("CDA", "onPause");
		System.err.println("onPause");
		katanaService.sendPacket(new KatanaPacket(Opcode.C_LOGOUT));
		doUnbindService();
		finish();
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

		katanaService.sendPacket(new KatanaPacket(Opcode.C_GAME_READY));
    }
    
    public void loadStaticBitmaps()
    {
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(BASE_PATH);
    	
    	mBitmapTextureAtlas = new BitmapTextureAtlas(BITMAP_SQUARE, BITMAP_SQUARE, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    	mBgRegion = new BitmapTextureAtlas(BITMAP_SQUARE, BITMAP_SQUARE, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    }
    
    public Scene onLoadScene() {
        //setting up screen here
    	
    	this.mEngine.registerUpdateHandler(new FPSLogger());
    	scene.setOnSceneTouchListener(this);
        scene.setBackgroundEnabled(true);
        
        load_HPdisplay();
        
        //testing method ends
        
        //initialize gesture detector
        mGestureDetector = new GestureDetector(this, new myGestureListener());
        //scene.setTouchAreaBindingEnabled(true); 

		
		userID = getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE).getInt(KatanaService.EXTRAS_PLAYERID, 0);
		if(userID == 0)
			userID = KatanaService.player_id;
		
		setBackground(background);
		if(temp_unit_string_list != null)
			updateUnits(temp_unit_string_list);
		
        return scene;
    }
    
    public void setBackground(String background)
    {
    	System.out.println("mBgRegion: " + mBgRegion + " - background: " + background);
    	if(mBgRegion == null)
        	mBgRegion = new BitmapTextureAtlas(BITMAP_SQUARE, BITMAP_SQUARE, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    	mBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBgRegion, this, background, 0, 0);
    	bg = new Sprite(0, 0, mBackground);
    	scene.setBackground(new SpriteBackground(bg));
    	
    	System.out.println("Setting background to " + background);
    }
    
    private TiledTextureRegion createTexture(final String file, int animations)
    {
    	int bx = bitmap_x;
    	int by = bitmap_y;
    	Log.d("TEXTURE: ", "file: " + file + " - bx: " + bx + " by: " + by);
    	stepBitmapCoordinates();
    	Log.d("TEXTURE:", "bitx: " + bitmap_x + " bity: " + bitmap_y);
    	
    	System.err.println("CREATING TEXTURE: " + file);
    	
    	if(file.equalsIgnoreCase("attack.png") || file.equalsIgnoreCase("healer.png") || file.equalsIgnoreCase("eggy.png"))
    		animations = NUM_ANIMS;
    	else
    		animations = 1;
    	if(mBitmapTextureAtlas == null)
    		new BitmapTextureAtlas(BITMAP_SQUARE, BITMAP_SQUARE, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    	
    	return BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, file, bx, by, animations, 1);
    }
    
    private void stepBitmapCoordinates()
    {
    	if((bitmap_x) >= (BITMAP_SQUARE - BITMAP_STEP))
    	{
    		bitmap_x = 0;
    		bitmap_y += BITMAP_STEP;
    	}
    	else
    		bitmap_x += BITMAP_STEP;
    }
    
    public void updateUnits(ArrayList<String> unitList)
    {
    	// id, health, model
    	for(String unit_str : unitList)
    	{
    		String[] data = unit_str.split(";");
    		if(data.length < 6)
    		{
    			System.err.println("GameActivity: Received invalid unit data string: '" + unit_str + "'");
    			continue;
    		}
    		
    		try
    		{
    			Integer id = Integer.parseInt(data[0].trim());
    			int cur_health = Integer.parseInt(data[1].trim());
    			int max_health = Integer.parseInt(data[2].trim());
    			float x = Float.parseFloat(data[3].trim());
    			float y = Float.parseFloat(data[4].trim());
    			String model_name = data[5].trim();
    			Unit u = unit_map.remove(id);
    			if(u != null)
    			{
    				u.setHealth(cur_health);
    				u.setMaxHealth(max_health);
    				if(!u.getModelName().equalsIgnoreCase(model_name))
    				{
    					TiledTextureRegion texture = createTexture(model_name, NUM_ANIMS);
    					AnimatedSprite model = createSprite(x, y, texture, u);
    					u.setModel(model, model_name);
    				}
    				float oldx = u.getX();
    				float oldy = u.getY();
    				if(oldx != x && oldy != y)
    				{
    					u.moveTo(x, y);
    					u.getSprite().setPosition(x,  y);
    				}
    				unit_map.put(id, u);
    			}
    			else
    			{
	    			System.err.println("Spawning Unit: " + id + " - " + max_health + " - " + model_name + " - [" + x + "," + y + "]");
	    			u = spawnUnit(id, max_health, model_name, x, y);
    			
	    			System.out.println("GameActivity: Created unit[" + u + "]");
    			}
    		}
    		catch(NumberFormatException ex)
    		{
    			System.err.println("GameActivity: " + ex.getLocalizedMessage());
    		}
    	}
    }
    
    public Unit spawnUnit(int id, int health, String model_name, float pos_x, float pos_y)
    {
    	TiledTextureRegion texture = createTexture(model_name, NUM_ANIMS);
    	final Unit unit = new Unit(id, health, pos_x, pos_y);
		AnimatedSprite model = createSprite(pos_x, pos_y, texture, unit);
		unit.setModel(model, model_name);
		model.setScale(1);
		scene.attachChild(model);
		scene.registerTouchArea(model);
		
		unit_map.put(id, unit);
		
    	return unit;
    }
    
    private AnimatedSprite createSprite(float pos_x, float pos_y, TiledTextureRegion texture, final Unit unit)
    {
    	return new AnimatedSprite(pos_x, pos_y, texture)
    	{
    		@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,	final float local_x, final float local_y) 
    		{
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN)
				{
					//selected = unit;
					return true;
				}
				return false;
			}
		};
    }
    
    //load up HP display
    public void load_HPdisplay()
    {	
    	healthText = new ChangeableText(cameraWidth-75, 30, this.mFont, "5000", "XXXX".length());
        scene.attachChild(healthText);
    }
       
    public void onLoadComplete() {

    }
    
    public FontManager getFontManager() {
        	return this.mEngine.getFontManager();
    }
    
    public void moveUnit(int unit_id, float x, float y)
    {
    	if(unit_id <= 0)
    	{
    		System.err.println("INVALID ID");
    		return;
    	}
    	
    	Unit unit = unit_map.get(unit_id);
    	if(unit == null)
    	{
    		System.err.println("INVALID UNIT");
    		return;
    	}
    	
    	unit.moveTo(x, y);
    	move(unit.getSprite(), x, y);
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
        float realMoveDuration = length / velocity;

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

	//remove method for andengine uses
	public void removeSprite(final AnimatedSprite _sprite) {
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
			float x = ev.getX();
			float y = ev.getY();
			Unit user = unit_map.get(userID);
			AnimatedSprite sprite = user.getSprite();
			move(sprite, ev.getX(), ev.getY());
		
			KatanaPacket packet = new KatanaPacket(Opcode.C_MOVE);
			packet.addData(x + "");
			packet.addData(y + "");
			katanaService.sendPacket(packet);
			
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
		
			return true;
	}

	boolean onSwipeRight() {
		Toast.makeText(GameActivity.this, "onSwipeRight", Toast.LENGTH_SHORT).show();	
		
		return true;
	}

	boolean onSwipeLeft() {
		Toast.makeText(GameActivity.this, "onSwipeLeft", Toast.LENGTH_SHORT).show();
		
		return true;
	}

	boolean onSwipeDown() {
		Toast.makeText(GameActivity.this, "onSwipeDown", Toast.LENGTH_SHORT).show();
		
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

