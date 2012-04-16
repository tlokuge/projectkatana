package com.katana.splash;

import java.util.ArrayList;
import java.util.HashMap;

import katana.constants.KatanaConstants;
import katana.constants.Opcode;
import katana.dialogs.ScoresDialog;
import katana.game.Unit;
import katana.objects.KatanaPacket;
import katana.receivers.KatanaReceiver;
import katana.services.KatanaService;
import katana.services.KatanaService.KatanaSBinder;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontManager;
import org.anddev.andengine.opengl.texture.TextureManager;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.IModifier.IModifierListener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class GameActivity extends BaseGameActivity implements IOnSceneTouchListener {

	// ===========================================================
	// Fields
	// ===========================================================

	private GestureDetector katanaGestureDetector;
	
	// BitmapTextureAtlas
	private BitmapTextureAtlas spriteTextureAtlas;
	private BitmapTextureAtlas bgTextureAtlas;
	private BitmapTextureAtlas fontTextureAtlas;
	
	// TextureRegion
	private TextureRegion bgTextureRegion;

	private String background_name = KatanaConstants.GAME_BG_DEFAULT;
	private ArrayList<String> temp_unit_string_list;
	private Scene katanaScene = new Scene();

	private Font gameFont;

	private final String BASE_PATH = "gfx/";

	private final int BITMAP_SQUARE = 2048;
	private final int BITMAP_STEP = 512;
	private final int NUM_ANIMS = 5;

	private HashMap<Integer, Unit> unit_map = new HashMap<Integer, Unit>();
	private HashMap<String, TiledTextureRegion> texture_map = new HashMap<String, TiledTextureRegion>();
	private int bitmap_x = 0;
	private int bitmap_y = 0;

	private int userID;
	
	private MoveModifier mod;

	private boolean gameEnd = false;
	/** ANDROID ACTIVITY LIFECYCLE **/
	/** DO NOT REMOVE **/
	protected void onStart() {
		super.onStart();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		System.out.println(bundle);
		background_name = bundle.getString(KatanaService.EXTRAS_GAMEBG);
		temp_unit_string_list = bundle.getStringArrayList(KatanaService.EXTRAS_GAMESTART);
	}

	public void onBackPressed() {
		super.onBackPressed();
		Log.d("CDA", "onBackPressed");
		System.err.println("onBackPressed");
		if(!gameEnd) {
			katanaService.sendPacket(new KatanaPacket(Opcode.C_LOGOUT));
			doKillService();
			this.finish();
		}
		// TODO: Add logic for "do you want to quit"
	}

	public void onPause() {
		super.onPause();
		Log.d("CDA", "onPause");
		System.err.println("onPause");
		doUnbindService();
		this.finish();
	}

	public Engine onLoadEngine() {
		doBindService();
		System.out.println("onLoadEngine: " + katanaService);

		final Display display = getWindowManager().getDefaultDisplay();
		int cameraWidth = display.getWidth();
		int cameraHeight = display.getHeight();
		Camera katanaCamera = new Camera(0, 0, cameraWidth, cameraHeight);

		return new Engine(new EngineOptions(
							  true, 
							  ScreenOrientation.LANDSCAPE,
							  new RatioResolutionPolicy(cameraWidth, cameraHeight), 
						      katanaCamera)
		);

	}

	public void onLoadResources() {
		FontManager fontManager = mEngine.getFontManager();
		TextureManager textureManager = mEngine.getTextureManager();
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(BASE_PATH);
		
		spriteTextureAtlas = new BitmapTextureAtlas(BITMAP_SQUARE,
				BITMAP_SQUARE, 
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		bgTextureAtlas = new BitmapTextureAtlas(BITMAP_SQUARE, 
				BITMAP_SQUARE,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		
		fontTextureAtlas = new BitmapTextureAtlas(
				256, 
				256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		gameFont = new Font(
				fontTextureAtlas, 
				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 
				32, 
				true, 
				Color.WHITE);
		
		fontManager.loadFont(gameFont);

		textureManager.loadTexture(fontTextureAtlas);
		textureManager.loadTexture(bgTextureAtlas);
		textureManager.loadTexture(spriteTextureAtlas);

		katanaService.sendPacket(new KatanaPacket(Opcode.C_GAME_READY));
	}

	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		katanaScene.setOnSceneTouchListener(this);
		katanaScene.setBackgroundEnabled(true);
	
		// testing method ends
	
		// initialize gesture detector
		katanaGestureDetector = new GestureDetector(this, new KatanaGestureListener());
	
		userID = getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE)
				.getInt(KatanaService.EXTRAS_PLAYERID, 0);
		if (userID == 0)
			userID = KatanaService.player_id;
	
		setBackground(background_name);
		if (temp_unit_string_list != null)
			updateUnits(temp_unit_string_list);
	
		return katanaScene;
	}

	public void onLoadComplete() {}
	private TiledTextureRegion createTexture(final String file, int animations) {
		
		TiledTextureRegion texture = texture_map.get(file);
		if(texture != null)
			return texture;
		
		int bx = bitmap_x;
		int by = bitmap_y;
		stepBitmapCoordinates();

		if (file.equalsIgnoreCase("attack.png") || 
			file.equalsIgnoreCase("healer.png") || 
			file.equalsIgnoreCase("eggy.png"))
			animations = NUM_ANIMS;
		else
			animations = 1;
		
		if (spriteTextureAtlas == null)
			spriteTextureAtlas = new BitmapTextureAtlas(
					BITMAP_SQUARE, 
					BITMAP_SQUARE, 
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		texture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				spriteTextureAtlas, 
				this, 
				file, 
				bx, 
				by, 
				animations, 
				1);
		
		texture_map.put(file, texture);
		
		return texture;
	}

	private void stepBitmapCoordinates() {
		if ((bitmap_x) >= (BITMAP_SQUARE - BITMAP_STEP)) {
			bitmap_x = 0;
			bitmap_y += BITMAP_STEP;
		} else
			bitmap_x += BITMAP_STEP;
	}

	private AnimatedSprite createSprite(float pos_x, float pos_y, TiledTextureRegion texture, final Unit unit) {
		return new AnimatedSprite(pos_x, pos_y, texture);
	}

	private void move(AnimatedSprite sprite, float dest_spriteX, float dest_spriteY) {
		float curr_spriteX = sprite.getX();
		float curr_spriteY = sprite.getY();

		sprite.setCurrentTileIndex(0);
		sprite.clearEntityModifiers();

		dest_spriteX -= sprite.getWidth() / 2;
		dest_spriteY -= sprite.getHeight() / 2;

		int offX = (int) (dest_spriteX - curr_spriteX);
		int offY = (int) (dest_spriteY - curr_spriteY);

		float length = (float) Math.sqrt((offX * offX) + (offY * offY));
		float velocity = 200.0f / 1.0f; // 480 pixels / 1 sec
		float realMoveDuration = length / velocity;

		if (!(offX == 0 && offY == 0)) {
			mod = new MoveModifier(realMoveDuration, 
					curr_spriteX,
					dest_spriteX, 
					curr_spriteY, 
					dest_spriteY);
			
			mod.addModifierListener(new IModifierListener<IEntity>()
				{
					@Override
					public void onModifierStarted(IModifier<IEntity> pModifier,	IEntity pItem) {}
					@Override
					public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) 
					{
						Log.d("MOVE", "COMPLETE");
						katanaService.sendPacket(new KatanaPacket(Opcode.C_MOVE_COMPLETE));
					}
			
				});

			sprite.registerEntityModifier(mod);
		}
	}
	
	
	// ------------------------------------- //
	//    Methods called by KatanaReceiver   //
	// ------------------------------------- //
	public void updateUnits(ArrayList<String> unitList) {
		// Prepare to update any existing units
		for(int id : unit_map.keySet())
		{
			Unit u = unit_map.get(id);
			if(u != null)
				u.update(false);
		}
		
		// id, health, model
		for (String unit_str : unitList) 
		{
			String[] data = unit_str.split(";");
			if (data.length < 6)
				continue;
			
			try 
			{
				Integer id = Integer.parseInt(data[0].trim());
				int cur_health = Integer.parseInt(data[1].trim());
				int max_health = Integer.parseInt(data[2].trim());
				float x = Float.parseFloat(data[3].trim());
				float y = Float.parseFloat(data[4].trim());
				String model_name = data[5].trim();
				Unit u = unit_map.remove(id);
				if (u != null)
				{
					u.setHealth(cur_health);
					u.setMaxHealth(max_health);
					if (!u.getModelName().equalsIgnoreCase(model_name)) 
					{
						TiledTextureRegion texture = createTexture(model_name, NUM_ANIMS);
						AnimatedSprite model = createSprite(x, y, texture, u);
						u.setModel(model, model_name);
					}
					float oldx = u.getX();
					float oldy = u.getY();
					if (oldx != x && oldy != y) 
					{
						u.moveTo(x, y);
						u.getSprite().setPosition(x, y);
					}
					unit_map.put(id, u);
				} else
				{
					u = spawnUnit(id, max_health, model_name, x, y);

				}
				u.update(true);
			} catch (NumberFormatException ex)
			{
				System.err.println("GameActivity: " + ex.getLocalizedMessage());
			}
		}
		
		ArrayList<Integer> removeList = new ArrayList<Integer>();
		// Prepare to remove any unupdated units
		for(int id : unit_map.keySet())
		{
			Unit u = unit_map.get(id);
			if(u == null || !u.isUpdated())
				removeList.add(id);
		}
		
		// Remove unupdated units
		for(int id : removeList)
			despawnUnit(id);
	}

	public Unit spawnUnit(int id, int health, String model_name, float pos_x, float pos_y)
	{
		final Unit unit = new Unit(id, health, pos_x, pos_y);
		TiledTextureRegion texture = createTexture(model_name, NUM_ANIMS);
		AnimatedSprite model = createSprite(pos_x, pos_y, texture, unit);
		unit.setModel(model, model_name);
		model.setScale(1);
		katanaScene.attachChild(model);
		katanaScene.registerTouchArea(model);
		unit_map.put(id, unit);
		return unit;
	}

	public void despawnUnit(int unit_id) {
		Unit removedUnit = unit_map.get(unit_id);
		if(removedUnit == null)
			return; 
		unit_map.remove(unit_id);
		removeSprite(removedUnit.getSprite());
	}
	
	private void removeSprite(final AnimatedSprite sprite)
	{
		mEngine.runOnUpdateThread(new Runnable()
		{
			@Override
			public void run()
			{
				katanaScene.detachChild(sprite);
			}
		});
	}
	
	public void moveUnit(int unit_id, float x, float y) {
		if (unit_id <= 0) {
			return;
		}
	
		Unit unit = unit_map.get(unit_id);
		if (unit == null) {
			return;
		}
	
		unit.moveTo(x, y);
		move(unit.getSprite(), x, y);
	}

	public void setBackground(String background) {
		if (bgTextureAtlas == null)
			bgTextureAtlas = new BitmapTextureAtlas(
					BITMAP_SQUARE, 
					BITMAP_SQUARE,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		bgTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				bgTextureAtlas, 
				this, 
				background, 
				0, 
				0);
		
		Sprite bg = new Sprite(0, 0, bgTextureRegion);
		katanaScene.setBackground(new SpriteBackground(bg));
	}

	// ----------------------------------------- //
	//    END Methods called by KatanaReceiver   //
	// ----------------------------------------- //
	
	class KatanaGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent ev) {
			float x = ev.getX();
			float y = ev.getY();
			Unit user = unit_map.get(userID);
			if(user == null)
				return false;
			AnimatedSprite sprite = user.getSprite();
			move(sprite, ev.getX(), ev.getY());

			KatanaPacket packet = new KatanaPacket(Opcode.C_MOVE);
			packet.addData(x + "");
			packet.addData(y + "");
			katanaService.sendPacket(packet);

			return true;
		}
	}
	
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		katanaGestureDetector.onTouchEvent(pSceneTouchEvent.getMotionEvent());
		return true;
	}

	// --------------------------------------------------- //
	// REQUIRED FOR KATANA SERVICE DO NOT CHANGE OR REMOVE //
	// --------------------------------------------------- //
	private KatanaService katanaService;
	public KatanaReceiver katanaReceiver;
	private boolean serviceBound;

	private void doBindService() {
		System.err.println("doBindService");
		katanaReceiver = new KatanaReceiver(KatanaReceiver.MODE_GAME);
		Intent intent = new Intent(this, KatanaService.class);
		bindService(intent, katanaConnection, Context.BIND_AUTO_CREATE);
		registerReceiver(katanaReceiver, new IntentFilter(
				KatanaService.BROADCAST_ACTION));
	}

	private void doUnbindService() {
		if (serviceBound) {
			// Detach our existing connection and broadcast receiver
			unbindService(katanaConnection);
			unregisterReceiver(katanaReceiver);
			serviceBound = false;
		}
	}

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

	public void showScoresDialog(ArrayList<String> al) {
		gameEnd = true;
		ScoresDialog dialog = new ScoresDialog(this, al, R.style.DialogTheme);
    	dialog.show(); 
	}
}
