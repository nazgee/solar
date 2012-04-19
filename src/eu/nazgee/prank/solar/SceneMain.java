package eu.nazgee.prank.solar;

import org.andengine.engine.Engine;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.modifier.ease.EaseBounceOut;

import android.content.Context;
import android.util.Log;
import eu.nazgee.game.utils.helpers.AtlasLoader;
import eu.nazgee.game.utils.loadable.SimpleLoadableResource;
import eu.nazgee.game.utils.scene.SceneLoadable;
import eu.nazgee.prank.solar.LightConverter.LightFeedback;

public class SceneMain extends SceneLoadable implements LightFeedback {

	MyResources mResources = new MyResources();
	private Sprite mPanels[];

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
		int cols = Consts.PANEL_COLS;
		int rows = Consts.PANEL_ROWS;
		mPanels = new Sprite[cols * rows];
		for (int i = 0; i < mPanels.length; i++) {
			mPanels[i] = new Sprite(0, 0, Consts.PANEL_SIZE_W, Consts.PANEL_SIZE_H, mResources.TEXS_PANELS.getTextureRegion(i % mResources.TEXS_PANELS.getTileCount()), getVertexBufferObjectManager());
		}

		final float offsetH = Consts.CAMERA_HEIGHT - Consts.PANELS_HEIGHT;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				mPanels[col + row*cols].setPosition(Consts.PANEL_SIZE_W * col, offsetH + Consts.PANEL_SIZE_H * row);
			}
		}
		
		// attach panels
		for (Sprite panel : mPanels) {
			attachChild(panel);
		}
	}

	@Override
	public void onUnload() {
		detachChildren();
		clearEntityModifiers();
		clearUpdateHandlers();
	}

	private static class MyResources extends SimpleLoadableResource {
//		public ITextureRegion TEX_SHOCKWAVE;
		public ITiledTextureRegion TEXS_PANELS;
		private BuildableBitmapTextureAtlas mAtlases[] = new BuildableBitmapTextureAtlas[2];

		@Override
		public void onLoadResources(Engine e, Context c) {
			mAtlases[0] = new BuildableBitmapTextureAtlas(e.getTextureManager(), 512, 512);
			mAtlases[1] = new BuildableBitmapTextureAtlas(e.getTextureManager(), 1024, 1024);
			BuildableBitmapTextureAtlas atlasRest = mAtlases[0];
			BuildableBitmapTextureAtlas atlasPanels = mAtlases[1];

//			TEX_SHOCKWAVE = BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlasRest, c, "shockwave.png");
//			TEXS_PANELS = TiledTextureRegionFactory.loadTiles(c, "gfx/", "panels", atlasPanels);
			TEXS_PANELS = SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(atlasPanels, c, "solar-tiles.svg", Consts.PANEL_SIZE_W * 2, Consts.PANEL_SIZE_H * 4, 2, 4);

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

	private void setLightPanels(final float pValue, final float pTime) {
		for (int i = 0; i < mPanels.length; i++) {
			Sprite panel = mPanels[i];
			panel.clearEntityModifiers();

			if (((float)i)/(float)mPanels.length < pValue) {
//				panel.registerEntityModifier(new AlphaModifier(pTime/2, panel.getAlpha(), 1, EaseBounceOut.getInstance()));
				panel.registerEntityModifier(new AlphaModifier(pTime, panel.getAlpha(), 0.9f));
			} else {
				panel.registerEntityModifier(new AlphaModifier(pTime, panel.getAlpha(), 0.5f));
			}
		}
	}
	
	@Override
	public void setLightLevel(float pValue) {
		Log.d(getClass().getSimpleName(), "pLevel=" + pValue);
		setLightPanels(pValue, 0.6f);
	}
}
