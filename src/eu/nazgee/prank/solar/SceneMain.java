package eu.nazgee.prank.solar;

import org.andengine.engine.Engine;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.DurationEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.ease.EaseBounceOut;

import android.content.Context;
import android.util.Log;
import eu.nazgee.game.utils.helpers.AtlasLoader;
import eu.nazgee.game.utils.helpers.Positioner;
import eu.nazgee.game.utils.loadable.SimpleLoadableResource;
import eu.nazgee.game.utils.scene.SceneLoadable;
import eu.nazgee.prank.solar.LightConverter.LightFeedback;

public class SceneMain extends SceneLoadable implements LightFeedback {

	MyResources mResources = new MyResources();
	private Sprite mPanels[];
	private Cell mCells[];

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
		mCells = new Cell[cols * rows];
		for (int i = 0; i < mPanels.length; i++) {
			mPanels[i] = new Sprite(0, 0, Consts.CELL_SIZE_W, Consts.CELL_SIZE_H, mResources.TEXS_PANELS.getTextureRegion(i % mResources.TEXS_PANELS.getTileCount()), getVertexBufferObjectManager());
			mCells[i] = new Cell(0, 0, Consts.CELL_SIZE_W*0.9f, Consts.CELL_SIZE_H*0.5f, mPanels[i], getVertexBufferObjectManager());
		}

		final float offsetH = Consts.CAMERA_HEIGHT - Consts.PANEL_SIZE_HEIGHT;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
//				mPanels[col + row*cols].setPosition(Consts.CELL_SIZE_W * col + Consts.CELL_SIZE_W/2, offsetH + Consts.CELL_SIZE_H * row + Consts.CELL_SIZE_H/2);
//				Positioner.setCentered(mPanels[col + row*cols], Consts.PANEL_SIZE_W * col + Consts.PANEL_SIZE_W/2, offsetH + Consts.PANEL_SIZE_H * row + Consts.PANEL_SIZE_H/2);
				Positioner.setCentered(mCells[col + row*cols], Consts.CELL_SIZE_W * col + Consts.CELL_SIZE_W/2, offsetH + Consts.CELL_SIZE_H * row + Consts.CELL_SIZE_H/2);
			}
		}
		
		// attach panels
//		for (Sprite panel : mPanels) {
//			attachChild(panel);
//		}
		for (Cell cell : mCells) {
			attachChild(cell);
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
		private BuildableBitmapTextureAtlas mAtlases[] = new BuildableBitmapTextureAtlas[1];

		@Override
		public void onLoadResources(Engine e, Context c) {
			mAtlases[0] = new BuildableBitmapTextureAtlas(e.getTextureManager(), 1024, 1024);
			BuildableBitmapTextureAtlas atlasPanels = mAtlases[0];

//			TEX_SHOCKWAVE = BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlasRest, c, "shockwave.png");
//			TEXS_PANELS = TiledTextureRegionFactory.loadTiles(c, "gfx/", "panels", atlasPanels);
			TEXS_PANELS = SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(atlasPanels, c, "solar-tiles.svg", Consts.CELL_SIZE_W * 2, Consts.CELL_SIZE_H * 4, 2, 4);

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

	private void setLightCells(final float pValue, final float pTime) {
		for (int i = 0; i < mCells.length; i++) {
			Cell cell = mCells[i];
			cell.getCellSprite().clearEntityModifiers();

			if (((float)i)/(float)mCells.length < pValue) {
				cell.getCellSprite().registerEntityModifier(new AlphaModifier(pTime, cell.getAlpha(), 1f));
			} else {
				cell.getCellSprite().registerEntityModifier(new AlphaModifier(pTime, cell.getAlpha(), 0.5f));
			}
		}
	}
	
	private float mTimePassed = 0;
	private float mTimePassedLong = 0;
	@Override
	public void setLightLevel(final LightConverter mLightConverter, final float pTimePassed) {
		mTimePassed += pTimePassed;
		mTimePassedLong += pTimePassed;

		if (mTimePassed >= 1) {	
			mTimePassed = 0;
	
			final float avg = mLightConverter.getLightValue(5);
			Log.d(getClass().getSimpleName(), "avg=" + avg);
			
			for (int i = 0; i < mCells.length; i++) {
				Cell cell = mCells[i];
				cell.clearEntityModifiers();
	
				if (((float)i)/(float)mCells.length < avg) {
					cell.registerEntityModifier(new ColorModifier(0.8f, cell.getColor(), Color.WHITE));
				} else {
					cell.registerEntityModifier(new ColorModifier(0.8f, cell.getColor(), Color.BLACK));
				}
			}
		}

		if (mTimePassedLong >= 6) {
			for (int i = 0; i < mCells.length; i++) {
				Cell cell = mCells[i];
				cell.getCellSprite().clearEntityModifiers();
				
				final float timeOffset = (float)i/(float)mCells.length * mTimePassedLong/6 + 0.1f;
				final float timeShiverFactor = 0.2f;
				cell.getCellSprite().registerEntityModifier(new SequenceEntityModifier(
						new DelayModifier(timeOffset),
//						new ScaleModifier(mTimePassedLong/3 * timeShiverFactor, 1f, 1.1f, 1, 1),
//						new ScaleModifier(mTimePassedLong/3 * timeShiverFactor, 1.1f, 1f, 1, 1)
						new AlphaModifier(mTimePassedLong/3 * timeShiverFactor, 1, 0.75f),
						new AlphaModifier(mTimePassedLong/3 * timeShiverFactor, 0.75f, 1)
						));
			}
			mTimePassedLong = 0;
		}
	}
}
