package eu.nazgee.prank.solar;

import java.util.Random;

import org.andengine.engine.Engine;
import org.andengine.entity.modifier.ScaleModifier;
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
	private Text mText;

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

		mText = new Text(0, 0, mResources.FONT, "", 50, new TextOptions(AutoWrap.WORDS, getW(), Text.LEADING_DEFAULT, HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mText.setAlpha(0.5f);
		attachChild(mText);

//		Random r = new Random();
//		for (int i = 0; i < 10; i++) {
//			Sprite s = new Sprite(getW() * r.nextFloat(),
//					getH() * r.nextFloat(), mResources.TEX_SHOCKWAVE, getVertexBufferObjectManager());
//			s.registerEntityModifier(new ScaleModifier(1 + 10 * r.nextFloat(), 2 * r.nextFloat() + 0.5f, 2 * r.nextFloat()));
//			attachChild(s);
//		}
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

	@Override
	public void setLightLevel(float pValue) {
		Log.e(getClass().getSimpleName(), "pLevel=" + pValue);
		for (Sprite panel : mPanels) {
			panel.setAlpha(pValue);
		}
		mText.setText((int)(pValue * 100) + "%");
	}

	public interface GameHandler {
		void onFinished();
	}

}
