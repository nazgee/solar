package eu.nazgee.prank.solar;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
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
	
	MyResources mResources = new MyResources();
	private Text mTextStatus;
	private Text mTextPercent;
	private Text mTextBar;
	private volatile boolean mCalibrationFinished = false;

	public HUD(float W, float H,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(W, H, pVertexBufferObjectManager);
		
		getLoader().install(mResources);
	}

	@Override
	public void onLoadResources(Engine e, Context c) {
		final float alpha = 0.8f;

		mTextStatus = new Text(0, 0, mResources.FONT, "CALIBRATING...", 50, new TextOptions(AutoWrap.NONE, getW(), Text.LEADING_DEFAULT, HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mTextStatus.setAlpha(alpha);
		attachChild(mTextStatus);

		mTextPercent = new Text(0, mTextStatus.getHeight(), mResources.FONT, "100%", 50, new TextOptions(AutoWrap.NONE, getW(), Text.LEADING_DEFAULT, HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mTextPercent.setAlpha(alpha);
		attachChild(mTextPercent);

		mTextBar = new Text(mTextPercent.getWidth(), mTextStatus.getHeight(), mResources.FONT, "", 50, new TextOptions(AutoWrap.NONE, getW() - mTextPercent.getWidth(), Text.LEADING_DEFAULT, HorizontalAlign.LEFT), getVertexBufferObjectManager());
		mTextBar.setAlpha(alpha);
		attachChild(mTextBar);
	}

	@Override
	public void onLoad(Engine e, Context c) {
		this.registerUpdateHandler(new TimerHandler(1, new ITimerCallback() {
			private int i = 0;
			String txt[] = {"CHARGING","CHARGING.","CHARGING..","CHARGING..."};
			String txt_cal[] = {"CALIBRATING","CALIBRATING.","CALIBRATING..","CALIBRATING..."};
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				if (mCalibrationFinished) {
					setStatus(txt[i++ % txt.length]);
				}else{
					setStatus(txt_cal[i++ % txt.length]);
				}
				pTimerHandler.setTimerSeconds(1);
				pTimerHandler.reset();
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
		
		final float w = mResources.FONT.getLetter('-').mWidth / (mTextBar.getAutoWrapWidth());
		final int count = (int) (pValue / w);
		String s = "";
		for(int i = 0; i < (count-2); i++) {
			s += "-";
		}
		s += "+";
		mTextBar.setText(s);
	}

	public void finishCalibration() {
		mCalibrationFinished  = true;
	}
	
	public boolean isFinishCalibration() {
		return mCalibrationFinished;
	}

	public void setStatus(String pStatus) {
		mTextStatus.setText(pStatus);
	}

	private static class MyResources extends SimpleLoadableResource {
		public Font FONT;

		@Override
		public void onLoadResources(Engine e, Context c) {
			final ITexture textureFontHud = new BitmapTextureAtlas(e.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
			FONT = FontFactory.createFromAsset(e.getFontManager(), textureFontHud, c.getAssets(), "LCD.ttf", 50, true, Color.WHITE.getARGBPackedInt());
			
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
