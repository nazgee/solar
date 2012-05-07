package eu.nazgee.prank.solar;

import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import eu.nazgee.game.utils.helpers.Positioner;

public class Cell extends Sprite {

	private final Sprite mCellSprite;
	private boolean mIsActive = true;
	private IEntityModifier mActiveMod;
	private Rectangle mFillerRect;

	public Cell(float pX, float pY, float pWidth, float pHeight,
			ITextureRegion pTexture,
			float pWidthFiller, float pHeightFiller,
			Sprite pCellSprite,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pWidth, pHeight, pTexture, pVertexBufferObjectManager);
		mCellSprite = pCellSprite;

		mFillerRect = new Rectangle(pX, pY, pWidthFiller, pHeightFiller, pVertexBufferObjectManager);
		mFillerRect.setZIndex(-1);
		attachChild(mFillerRect);
//		Positioner.setCentered(getCellSprite(), getWidth()/2, getHeight()/2);
		Positioner.setCentered(mFillerRect, getWidth()/2, getHeight()/2);
		sortChildren();
	}

	public Rectangle getFiller() {
		return mFillerRect;
	}

	public boolean isActive() {
		return mIsActive;
	}

	public void setIsActive(boolean mIsActive) {
		if (isActive() != mIsActive) {
			this.mIsActive = mIsActive;
			forceRefresh();
		}
	}

	public void forceRefresh() {
		unregisterEntityModifier(mActiveMod);
		if (isActive()) {
			mActiveMod = new ColorModifier(0.8f, getFiller().getColor(), Color.WHITE);
			getFiller().registerEntityModifier(mActiveMod);
		} else {
			mActiveMod = new ColorModifier(0.8f, getFiller().getColor(), Color.BLACK);
			getFiller().registerEntityModifier(mActiveMod);
		}
	}
}
