package eu.nazgee.prank.solar;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.Letter;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import android.content.Context;
import eu.nazgee.game.utils.loadable.SimpleLoadableResource;
import eu.nazgee.game.utils.scene.HUDLoadable;

public class HUD extends HUDLoadable {
	
	public enum eChargeStatus {
		CALIBRATION,
		SUSPEND,
		CHARGE,
	};
	MyResources mResources = new MyResources();
	private Text mTextStatus;
	private Text mTextPercent;
	private Text mTextBar;
	private volatile eChargeStatus mChargeStatus = eChargeStatus.CALIBRATION;
	private volatile float mMiliAmps = 0;

	public HUD(float W, float H,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(W, H, pVertexBufferObjectManager);
		
		getLoader().install(mResources);
	}

	@Override
	public void onLoadResources(Engine e, Context c) {
		final float alpha = 0.8f;

		float offsetY = 0;
		mTextStatus = new Text(0, offsetY, mResources.FONT, "CALIBRATING...", 50, new TextOptions(AutoWrap.NONE, getW(), Text.LEADING_DEFAULT, HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mTextStatus.setAlpha(alpha);
		attachChild(mTextStatus);

		mTextPercent = new Text(0, offsetY + mTextStatus.getHeight(), mResources.FONT, "100%", 50, new TextOptions(AutoWrap.NONE, getW(), Text.LEADING_DEFAULT, HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mTextPercent.setAlpha(alpha);
		attachChild(mTextPercent);

		mTextBar = new Text(mTextPercent.getWidth(), offsetY + mTextStatus.getHeight(), mResources.FONT, "", 50, new TextOptions(AutoWrap.NONE, getW() - mTextPercent.getWidth(), Text.LEADING_DEFAULT, HorizontalAlign.LEFT), getVertexBufferObjectManager());
		mTextBar.setAlpha(alpha);
		attachChild(mTextBar);
	}

	@Override
	public void onLoad(Engine e, Context c) {
		this.registerUpdateHandler(new TimerHandler(1, new ITimerCallback() {
			private int i = 0;
			String txt_cal[] = {"CALIBRATING","CALIBRATING.","CALIBRATING..","CALIBRATING..."};
			String txt_sus[] = {"STANDBY","STANDBY...","TOO DARK HERE!","TOO DARK HERE!"};
			String txt_chg[] = {"CHARGING   ","CHARGING.  ","CHARGING.. ","CHARGING..."};

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				switch (mChargeStatus) {
				case CALIBRATION:
					setNextText(txt_cal);
					break;
				case SUSPEND:
					setNextText(txt_sus);
					break;
				case CHARGE:
					setNextText(txt_chg);
					mTextStatus.setText(mTextStatus.getText() + " " + (int)mMiliAmps + "mAh");
					break;
				default:
					break;
				}

				pTimerHandler.setTimerSeconds(1);
				pTimerHandler.reset();
			}
			
			private void setNextText(String[] text) {
				setStatus(text[i++ % text.length]);
			}
		}));
	}

	@Override
	public void onUnload() {
		detachChildren();
		clearEntityModifiers();
		clearUpdateHandlers();
	}

	public void setProgressBar(final float pValue) {
		mTextPercent.setText((int)(pValue * 100) + "%");
		
		Letter l = mResources.FONT.getLetter('^');
		final float w = (l.mWidth + l.mOffsetX) / (mTextBar.getAutoWrapWidth());
		final int count = (int) (pValue / w);
		String s = "";
		for(int i = 0; i < (count); i++) {
			s += "^";
		}
		mTextBar.setText(s);
	}

	public void setChargeStatus(eChargeStatus pChargeStatus) {
		mChargeStatus  = pChargeStatus;
	}

	public void setMiliAmps(float pMiliAmps) {
		mMiliAmps = pMiliAmps;
		if (mMiliAmps > 9999) {
			mMiliAmps = 0;
		}
	}

	public void incMiliAmps(float pMiliAmps) {
		setMiliAmps(getMiliAmps() + pMiliAmps);
	}

	public float getMiliAmps() {
		return mMiliAmps;
	}

	public void setStatus(String pStatus) {
		mTextStatus.setText(pStatus);
	}

	private static class MyResources extends SimpleLoadableResource {
		public Font FONT;

		@Override
		public void onLoadResources(Engine e, Context c) {
			final ITexture textureFontHud = new BitmapTextureAtlas(e.getTextureManager(), 256, 512, TextureOptions.BILINEAR);
			FONT = FontFactory.createFromAsset(e.getFontManager(), textureFontHud, c.getAssets(), Consts.FONT, 50, true, Color.WHITE.getARGBPackedInt());
			
		}

		@Override
		public void onLoad(Engine e, Context c) {
			FONT.load();
		}

		@Override
		public void onUnload() {
			FONT.unload();
		}
	}
}
