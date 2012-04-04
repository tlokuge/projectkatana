package com.CPS630.engine;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.SurfaceGestureDetector;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;

public class AndengineActivity extends BaseGameActivity implements OnGestureListener{

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TiledTextureRegion mFaceTextureRegion;
 
    private TiledTextureRegion mDevilTextureRegion;
    private TiledTextureRegion mProjectileTextureRegion;
    private TiledTextureRegion mHeliTextureRegion;
    private boolean bossSelected=false;
    float realMoveDuration;
    
    AnimatedSprite face;
    AnimatedSprite heli;
    AnimatedSprite boss;
    AnimatedSprite face2;
    Sprite projectile;
    private SurfaceGestureDetector spells;
    Scene scene = new Scene();
   
    int cameraWidth;
    int cameraHeight;
    
    /*protected int getLayoutID() {
            return R.layout.main;
    }

    protected int getRenderSurfaceViewID() {
            return R.id.rendersurfaceview;
    }*/
    
	public Engine onLoadEngine() {
		final Display display = getWindowManager().getDefaultDisplay();
		cameraWidth = display.getWidth();
		cameraHeight = display.getHeight();
		mCamera = new Camera(0, 0, cameraWidth, cameraHeight);

		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
				new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera));

	}

    public void onLoadResources() {
    	this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA );
    	
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_box_tiled.png", 132, 180, 2, 1);
        this.mHeliTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "player.png",170 , 0, 3, 4);
        this.mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this,"projectile.png", 110, 64,1,1);
        
        this.mDevilTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "download.jpeg", 0, 0, 1, 1);
              
        this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }
   
    public Scene onLoadScene() {
    	
    	//GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		/*View inflate = getLayoutInflater().inflate(R.layout.main, null);
		gestureOverlayView.addView(inflate);*/
    	/*
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!gestureLib.load()) {
			finish();
		}
        setContentView(gestureOverlayView);
        */
    	//spells= new SpellDetector();
    	this.mEngine.registerUpdateHandler(new FPSLogger());
    	
        scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
        
        final int centerX = (cameraWidth - this.mFaceTextureRegion.getWidth()) / 2;
        final int centerY = (cameraHeight - this.mFaceTextureRegion.getHeight()) / 2;
        
        heli = new AnimatedSprite(100,100, this.mHeliTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                   // this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);                   
            	if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            			return true;
            		}
            		return false;
            }
        };
        
        heli.setScale(2);

        /* Quickly twinkling face. */
        face = new AnimatedSprite(50, 50, this.mFaceTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                   // this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
                    	
            			return true;
            		}
            		return false;
            }
        };
        
        
        boss = new AnimatedSprite(centerX, centerY, this.mDevilTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            			final float touchX = pSceneTouchEvent.getX();
            			final float touchY = pSceneTouchEvent.getY();
            			bossSelected=true;
            			 attackAnimate(heli);
            			//shootProjectile(touchX, touchY);
            			return true;
            		}
            		return false;
            }
        };
        
        face.setScale(1);
        
        //spells = new GestureDetector(this);        
        scene.registerTouchArea(boss);
        scene.registerTouchArea(face);
        scene.registerTouchArea(heli);
        scene.setTouchAreaBindingEnabled(true);  
     //   createSpriteSpawnTimeHandler();
        scene.attachChild(heli);
        scene.attachChild(face);
        scene.attachChild(boss);
       // scene.registerUpdateHandler(detect);
        return scene;
    }

    
    public void onLoadComplete() {

    }
    
    
    IUpdateHandler detect = new IUpdateHandler() {
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
	};
    
    private void move(float dest_spriteX, float dest_spriteY, AnimatedSprite sprite) {
    	
    	float curr_spriteX= sprite.getX();
    	float curr_spriteY= sprite.getY();
    	
        dest_spriteX-= sprite.getWidth()/2;
        dest_spriteY-= sprite.getHeight()/2;
        
        int offX = (int) (dest_spriteX - curr_spriteX);
        int offY = (int) (dest_spriteY - curr_spriteY);
        
        float length = (float) Math.sqrt((offX * offX)
            + (offY * offY));
        float velocity = 100.0f / 1.0f; // 480 pixels / 1 sec
        realMoveDuration = length / velocity;

        MoveModifier mod = new MoveModifier(realMoveDuration, curr_spriteX, dest_spriteX, curr_spriteY, dest_spriteY);
        sprite.registerEntityModifier(mod.deepCopy());
  
    }
    public boolean onTouchEvent(MotionEvent event) {
    	System.out.println("");
        int myEventAction = event.getAction(); 
        
        float dest_X = event.getX();
        float dest_Y = event.getY();
        
        System.out.println("Move to: " + dest_X + ", " + dest_Y);
        
        switch (myEventAction) {
           case MotionEvent.ACTION_DOWN:
        	   	break;
           case MotionEvent.ACTION_MOVE: {
        	   	onScroll(event,event, event.getX()-dest_X, event.getY()-dest_Y);
            	break;
           }
           case MotionEvent.ACTION_UP:{
        	   if (!heli.isAnimationRunning()){
        		   move(dest_X, dest_Y, heli);
        		   runAnimate(heli, dest_X, dest_Y);
        	   }
                break;
           }
        }
        return true;
    }
    
    public void attackAnimate(AnimatedSprite sprite) {

    	sprite.animate(new long[] { 10, 20, 50, 20 }, new int[] { 1, 3, 6 , 5 },  10);
	}

	public void runAnimate(AnimatedSprite sprite, float destX, float destY) {

		destX -= sprite.getWidth() / 2;
		destY -= sprite.getHeight() / 2;

		while (sprite.getX() != destX && sprite.getY() != destY) {
			sprite.animate(new long[] {100, 100, 100}, new int[] { 3, 4, 5 }, 10);
		}
		sprite.stopAnimation();
	}
	/*private void shootProjectile(final float pX, final float pY) {

		    int offX = (int) (pX - face.getX());
		    int offY = (int) (pY - face.getY());
		    int realY;
		    projectile = new Sprite(face.getX(), face.getY(), mProjectileTextureRegion.deepCopy());
		    scene.attachChild(projectile, 1);

		    int realX = (int) (mCamera.getWidth() + projectile.getWidth() / 2.0f);
		    float ratio = (float) offY / (float) offX;
		    
		    if(pX < face.getX()){
		    	realY = (int) ((realX * ratio) - projectile.getY());
		    }
		    else{
		    	realY = (int) ((realX * ratio) + projectile.getY());
		    }

		    int offRealX = (int) (realX - projectile.getX());
		    int offRealY = (int) (realY - projectile.getY());
		    
		    float length = (float) Math.sqrt((offRealX * offRealX)
		        + (offRealY * offRealY));
		    float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
		    float realMoveDuration = length / velocity;

		    MoveModifier mod = new MoveModifier(realMoveDuration,
		    projectile.getX(), realX, projectile.getY(), realY);
		    projectile.registerEntityModifier(mod.deepCopy());

	}*/

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

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if(distanceX>distanceY){
			if(distanceX>0){
				System.out.println("Left");
				Toast.makeText(this,"Left",Toast.LENGTH_SHORT).show();
			}
			else{
				System.out.println("Down");
				Toast.makeText(this,"Down",Toast.LENGTH_SHORT).show();
			}
		}
		else{
			if(distanceY>0){
				System.out.println("Up");
				Toast.makeText(this,"Up",Toast.LENGTH_SHORT).show();
			}
			else{
				System.out.println("Right");
				Toast.makeText(this,"Left",Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//inner class to extend SurfaceGestureDetector
	
	
}

