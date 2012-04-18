package eu.nazgee.prank.solar;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.ease.EaseBounceOut;

import android.content.Context;
import android.util.Log;
import eu.nazgee.game.utils.helpers.AtlasLoader;
import eu.nazgee.game.utils.helpers.TiledTextureRegionFactory;
import eu.nazgee.game.utils.loadable.SimpleLoadableResource;
import eu.nazgee.game.utils.scene.SceneLoadable;
import eu.nazgee.prank.solar.LightConverter.LightFeedback;

public class SceneMain extends SceneLoadable implements LightFeedback {

	MyResources mResources = new MyResources();
	private Sprite mPanels[];
	private Text mTextStatus;
	private Text mTextPercent;
	private Text mTextBar;

	private SceneMain(VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pVertexBufferObjectManager);
		getLoader().install(mResources);
	}

	public SceneMain(float W, float H,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(W, H, pVertexBufferObjectManager);
		getLoader().install(mResources);
	}

	@Override
	public void onLoadResources(Engine e, Context c) {
	}

	@Override
	public void onLoad(Engine e, Context c) {
		mPanels = new Sprite[mResources.TEXS_PANELS.getTileCount()];
		for (int i = 0; i < mPanels.length; i++) {
			mPanels[i] = new Sprite(0, 0, getW()/2, getH()/2, mResources.TEXS_PANELS.getTextureRegion(i), getVertexBufferObjectManager());
		}
		
		// position panels
		mPanels[0].setPosition(0, 0);
		mPanels[1].setPosition(getW()/2, 0);
		mPanels[2].setPosition(0, getH()/2);
		mPanels[3].setPosition(getW()/2, getH()/2);
		
		// attach panels
		for (Sprite panel : mPanels) {
			attachChild(panel);
		}

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

//		Random r = new Random();
//		for (int i = 0; i < 10; i++) {
//			Sprite s = new Sprite(getW() * r.nextFloat(),
//					getH() * r.nextFloat(), mResources.TEX_SHOCKWAVE, getVertexBufferObjectManager());
//			s.registerEntityModifier(new ScaleModifier(1 + 10 * r.nextFloat(), 2 * r.nextFloat() + 0.5f, 2 * r.nextFloat()));
//			attachChild(s);
//		}
		
		this.registerUpdateHandler(new TimerHandler(5, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				setStatus("CHARGING...");
			}
		}));
	}

	@Override
	public void onUnload() {
		detachChildren();
		clearEntityModifiers();
		clearUpdateHandlers();
	}

	private static class MyResources extends SimpleLoadableResource {
		public ITextureRegion TEX_SHOCKWAVE;
		public TiledTextureRegion TEXS_PANELS;
		public Font FONT;
		private BuildableBitmapTextureAtlas mAtlases[] = new BuildableBitmapTextureAtlas[2];

		@Override
		public void onLoadResources(Engine e, Context c) {
			mAtlases[0] = new BuildableBitmapTextureAtlas(e.getTextureManager(), 512, 512);
			mAtlases[1] = new BuildableBitmapTextureAtlas(e.getTextureManager(), 1024, 1024);
			BuildableBitmapTextureAtlas atlasRest = mAtlases[0];
			BuildableBitmapTextureAtlas atlasPanels = mAtlases[1];

			TEX_SHOCKWAVE = BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlasRest, c, "shockwave.png");
			TEXS_PANELS = TiledTextureRegionFactory.loadTiles(c, "gfx/", "panels", atlasPanels);
			
			final ITexture textureFontHud = new BitmapTextureAtlas(e.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
			FONT = FontFactory.createFromAsset(e.getFontManager(), textureFontHud, c.getAssets(), "LCD.ttf", 50, true, Color.WHITE.getARGBPackedInt());
			FONT.load();
		}

		@Override
		public void onLoad(Engine e, Context c) {
			AtlasLoader.buildAndLoad(mAtlases);
		}

		@Override
		public void onUnload() {
			for (BuildableBitmapTextureAtlas atlas : mAtlases) {
				atlas.unload();
			}
		}
	}

	private void setProgressBar(final float pValue) {
		final float w = mResources.FONT.getLetter('-').mWidth / (mTextBar.getAutoWrapWidth());
		final int count = (int) (pValue / w);
		String s = "";
		for(int i = 0; i < (count-1); i++) {
			s += "-";
		}
		s += "+";
		mTextBar.setText(s);
	}

	private void setStatus(String pStatus) {
		mTextStatus.setText(pStatus);
	}

	private void setLightPanels(final float pValue, final float pTime) {
		for (int i = 0; i < mPanels.length; i++) {
			Sprite panel = mPanels[i];
			panel.clearEntityModifiers();

			if (((float)i)/(float)mPanels.length < pValue) {
				panel.registerEntityModifier(new AlphaModifier(pTime/2, panel.getAlpha(), 1, EaseBounceOut.getInstance()));
			} else {
				panel.registerEntityModifier(new AlphaModifier(pTime, panel.getAlpha(), 0.5f));
			}
		}
	}
	
	@Override
	public void setLightLevel(float pValue) {
		Log.d(getClass().getSimpleName(), "pLevel=" + pValue);
		mTextPercent.setText((int)(pValue * 100) + "%");
		setLightPanels(pValue, 0.6f);
		setProgressBar(pValue);
	}

	public interface GameHandler {
		void onFinished();
	}

}
