package com.CPS630.gEngine;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.view.MotionEvent;
import android.widget.Toast;

public class KatanaActivity extends BaseGameActivity {
    // ===========================================================
    // Constants
    // ===========================================================

    private static final int CAMERA_WIDTH = 720;
    private static final int CAMERA_HEIGHT = 480;

    // ===========================================================
    // Fields
    // ===========================================================

    private Camera mCamera;
    private BitmapTextureAtlas mBitmapTextureAtlas;
    private TextureRegion mFaceTextureRegion;
    
    Sprite face;
    Scene scene = new Scene();

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public Engine onLoadEngine() {
    	Toast.makeText(this, "Touch & Drag the face!", Toast.LENGTH_LONG).show();
            this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
            return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
    }

    @Override
    public void onLoadResources() {
    	this.mBitmapTextureAtlas = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0);
       // this.mTargetTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "download.jpeg",128, 0);
        
        this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
    }

    @Override
    public Scene onLoadScene() {
    	

        scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

        final int centerX = (CAMERA_WIDTH - this.mFaceTextureRegion.getWidth()) / 2;
        final int centerY = (CAMERA_HEIGHT - this.mFaceTextureRegion.getHeight()) / 2;
        face = new Sprite(centerX, centerY, this.mFaceTextureRegion) {
                @Override
                public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                        this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                        return true;
                }
        };
        face.setScale(1);
        scene.attachChild(face);
        scene.registerTouchArea(face);
        scene.setTouchAreaBindingEnabled(true);
     //   createSpriteSpawnTimeHandler();
        return scene;
    }

    @Override
    public void onLoadComplete() {

    }
    
    private void move(final float pX, final float pY) {

        int offX = (int) (pX - face.getX());
        int offY = (int) (pY - face.getY());
        
        float length = (float) Math.sqrt((offX * offX)
            + (offY * offY));
        float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
        float realMoveDuration = length / velocity;

        MoveModifier mod = new MoveModifier(realMoveDuration,face.getX(), pX, face.getY(), pY);
        face.registerEntityModifier(mod.deepCopy());
  
    }
    public boolean onTouchEvent(MotionEvent event) {
        int myEventAction = event.getAction(); 

        float X = event.getX();
        float Y = event.getY();

        switch (myEventAction) {
           case MotionEvent.ACTION_DOWN:
        	break;
           case MotionEvent.ACTION_MOVE: {
            	//face.setPosition(X, Y);
            	break;}
           case MotionEvent.ACTION_UP:{
        	    //face.setPosition(X  - face.getWidth() - face.getWidth()/2 - face.getWidth()/4, Y - face.getHeight() / 2);
        	    move(X  - face.getWidth() - face.getWidth()/2 - face.getWidth()/4, Y - face.getHeight() / 2);
                break;}
        }
        return true;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}

