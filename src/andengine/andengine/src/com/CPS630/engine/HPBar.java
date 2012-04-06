package com.CPS630.engine;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.primitive.Rectangle;

public class HPBar extends HUD {

	// ===========================================================
	// Fields
	// ===========================================================

	private final Rectangle mBackgroundRectangle;
	private final Rectangle mHPRectangle;

	private final float mPixelsPerPercentRatio;

	// ===========================================================
	// Constructors
	// ===========================================================
	public HPBar(final Camera pCamera, final float pX, final float pY, final float pWidth, final float pHeight) {
		super();
		super.setCamera(pCamera);

		this.mBackgroundRectangle = new Rectangle(pX - 2, pY - 2, pWidth + 4,
				pHeight + 4);

		this.mHPRectangle = new Rectangle(pX, pY, pWidth, pHeight);

		super.attachChild(this.mBackgroundRectangle); // This one is drawn first.

		super.attachChild(this.mHPRectangle); // The progress is drawn afterwards.

		this.mPixelsPerPercentRatio = pWidth / 100;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public void setBackColor(final float pRed, final float pGreen,
			final float pBlue, final float pAlpha) {
		this.mBackgroundRectangle.setColor(pRed, pGreen, pBlue, pAlpha);
	}

	public void setHPColor(final float pRed, final float pGreen,
			final float pBlue, final float pAlpha) {
		this.mHPRectangle.setColor(pRed, pGreen, pBlue, pAlpha);
	}

	/**
	 * Set the current hp of this hp bar.
	 * 
	 * @param hp
	 *            is <b> BETWEEN </b> 0 - 100.
	 */
	public void setHP(final float pHP) {
		if (pHP < 0)
			this.mHPRectangle.setWidth(0);
		this.mHPRectangle.setWidth(this.mPixelsPerPercentRatio * pHP);
	}

	public void move(final float dest_X, final float dest_Y,
			float realMoveDuration) {
		MoveModifier mod = new MoveModifier(realMoveDuration,
				this.mBackgroundRectangle.getX(), dest_X - 2,
				this.mBackgroundRectangle.getY(), dest_Y - 2);
		this.mBackgroundRectangle.registerEntityModifier(mod.deepCopy());
		MoveModifier mod2 = new MoveModifier(realMoveDuration,
				this.mHPRectangle.getX(), dest_X, this.mHPRectangle.getY(),
				dest_Y);
		this.mHPRectangle.registerEntityModifier(mod2.deepCopy());

	}
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
