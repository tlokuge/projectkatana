package com.CPS630.engine;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
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
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
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
    private TiledTextureRegion mFaceTextureRegion;
 
    private TiledTextureRegion mDevilTextureRegion;
    private TiledTextureRegion mProjectileTextureRegion;
    private TiledTextureRegion mHeliTextureRegion;
    
    //for custom background
    private BitmapTextureAtlas mBgRegion;
    private TextureRegion mBackground;
    
    float realMoveDuration;
    
    AnimatedSprite face;
    AnimatedSprite heli;
    AnimatedSprite boss;
    AnimatedSprite lspell,rspell,uspell,dspell;
    Sprite bg;
    Sprite projectile;
    Scene scene = new Scene();
    
    private BitmapTextureAtlas mFontTexture;
    private Font mFont;
   
    int cameraWidth;
    int cameraHeight;
    boolean bossTouched=false;
    
	GestureDetector mGestureDetector;
	ChangeableText healthText;
    
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
    	
    	/* The font */
        this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.WHITE);
        this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
        this.getFontManager().loadFont(this.mFont);
        
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        
    	this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA );
    	
    	//create background bitmap texture
    	this.mBgRegion = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        this.mBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBgRegion, this,"Background.png", 0, 0);
        
        this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_box_tiled.png", 132, 180, 2, 1);
        this.mHeliTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "player.png",170 , 0, 3, 4);
        this.mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this,"projectile.png", 110, 64,1,1);
        
        this.mDevilTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "download.jpeg", 0, 0, 1, 1);
        
        this.mEngine.getTextureManager().loadTexture(this.mBgRegion);
        this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }
   
    public Scene onLoadScene() {

    	this.mEngine.registerUpdateHandler(new FPSLogger());
    	
        //scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
        final int centerX = (cameraWidth - this.mFaceTextureRegion.getWidth()) / 2;
        final int centerY = (cameraHeight - this.mFaceTextureRegion.getHeight()) / 2;
       
        scene.setBackgroundEnabled(true); 
        bg = new Sprite(0, 0, this.mBackground);
        scene.setBackground(new SpriteBackground(bg));
        
        heli = new AnimatedSprite(100,100, this.mHeliTextureRegion);
        
        heli.setScale(2);

        /* Quickly twinkling face. */
        lspell = new AnimatedSprite(cameraWidth-50, 100, this.mFaceTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                   // this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            			return true;
            		}
            		return false;
            }
        };
        rspell = new AnimatedSprite(cameraWidth-50, 175, this.mFaceTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                   // this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            			return true;
            		}
            		return false;
            }
        };
        
        rspell.setCurrentTileIndex(1);
        
        uspell = new AnimatedSprite(cameraWidth-50, 250, this.mFaceTextureRegion){
            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                   // this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            			return true;
            		}
            		return false;
            }
        };
        dspell = new AnimatedSprite(cameraWidth-50, 325, this.mFaceTextureRegion){
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
            			bossTouched=true;  			
            			return true;
            		}
            		return false;
            }
        };
        
        healthText = new ChangeableText(cameraWidth-75, 30, this.mFont, "5000", "XXXX".length());
        
        mGestureDetector = new GestureDetector(this, new myGestureListener());
        
        lspell.setScale(2);
        rspell.setScale(2);
        dspell.setScale(2);
        uspell.setScale(2);
        scene.registerTouchArea(boss);
        //scene.registerTouchArea(face);
        scene.registerTouchArea(heli);
        
        scene.setTouchAreaBindingEnabled(true); 
   
        scene.attachChild(healthText);

     //   createSpriteSpawnTimeHandler();
        scene.attachChild(heli);
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

    
    public void onLoadComplete() {

    }
    
    public FontManager getFontManager() {
        	return this.mEngine.getFontManager();
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
        float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
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
	
	class myGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent ev) {
			
			float dest_X=ev.getX();
			float dest_Y=ev.getY();
			
			if(!bossTouched)
			{
				heli.clearEntityModifiers();
				move(dest_X, dest_Y, heli);
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
			attackAnimate(heli);	
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

