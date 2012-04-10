package katana.game;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.sprite.AnimatedSprite;

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
	public HPBar(final float pX, final float pY, final float pWidth, final float pHeight, AnimatedSprite sprite) {
		super();
		
		this.mBackgroundRectangle = new Rectangle(pX - 1, pY - 1, pWidth + 2,
				pHeight + 2);

		this.mHPRectangle = new Rectangle(pX, pY, pWidth, pHeight);

		sprite.attachChild(this.mBackgroundRectangle); // This one is drawn first.

		sprite.attachChild(this.mHPRectangle); // The progress is drawn afterwards.

		this.mPixelsPerPercentRatio = pWidth / 5000;
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
	 *            is <b> BETWEEN </b> 0 - 5000.
	 */
	public void setHP(final float pHP) {
		if (pHP < 0)
			this.mHPRectangle.setWidth(0);
		this.mHPRectangle.setWidth(this.mPixelsPerPercentRatio * pHP);
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
