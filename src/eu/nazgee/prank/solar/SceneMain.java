package eu.nazgee.prank.solar;

import java.util.Random;

import org.andengine.engine.Engine;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.ColorModifier;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteGroup;
import org.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.IModifier.IModifierListener;

import android.content.Context;
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

		SpriteGroup group = new SpriteGroup(mResources.TEXS_PANELS.getTexture(), cols * rows, getVertexBufferObjectManager());
		Random rand = new Random();
		for (int i = 0; i < mPanels.length; i++) {
//			mPanels[i] = new Sprite(0, 0, Consts.CELL_SIZE_W, Consts.CELL_SIZE_H, mResources.TEXS_PANELS.getTextureRegion(rand.nextInt(mResources.TEXS_PANELS.getTileCount())), getVertexBufferObjectManager());
			mCells[i] = new Cell(0, 0, Consts.CELL_SIZE_W, Consts.CELL_SIZE_H, mResources.TEXS_PANELS.getTextureRegion(rand.nextInt(mResources.TEXS_PANELS.getTileCount())), Consts.CELL_SIZE_W*0.9f, Consts.CELL_SIZE_H*0.5f, mPanels[i], getVertexBufferObjectManager());
			group.attachChild(mCells[i]);
		}

		final float offsetH = Consts.CAMERA_HEIGHT - Consts.PANEL_SIZE_HEIGHT;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				Positioner.setCentered(mCells[col + row*cols], Consts.CELL_SIZE_W * col + Consts.CELL_SIZE_W/2, offsetH + Consts.CELL_SIZE_H * row + Consts.CELL_SIZE_H/2);
			}
		}
		
		// attach panels
		for (Cell cell : mCells) {
//			attachChild(cell);
			this.registerTouchArea(cell);
		}
		attachChild(group);

		this.setOnAreaTouchListener(new IOnAreaTouchListener() {
			Cell lastCell;
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					ITouchArea pTouchArea, float pTouchAreaLocalX,
					float pTouchAreaLocalY) {
				for (Cell cell : mCells) {
					if (pTouchArea == cell && cell != lastCell) {
						lastCell = cell;
						postRunnable(new ColorizeRunnable(cell));
						return true;
					}
				}
				
				if (pSceneTouchEvent.isActionUp()) {
					lastCell = null;
				}
				return false;
			}
		});

//		this.setTouchAreaBindingOnActionDownEnabled(true);
	}
	
	public class ColorizeRunnable implements Runnable {
		private Cell mCell;

		ColorizeRunnable(Cell pCell) {
			this.mCell = pCell;
		}

		@Override
		public void run() {
			final float time = 1;
			Color col = mCell.getColor();
			IEntityModifier mod = new SequenceEntityModifier(
					new ColorModifier(time, col, Color.RED)
					);
			mod.setAutoUnregisterWhenFinished(true);
			mod.addModifierListener(new IModifierListener<IEntity>() {
				@Override
				public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
				}
				@Override
				public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
					mCell.forceRefresh();
				}
			});
			mCell.registerEntityModifier(mod);
		}
	}

	@Override
	public void onUnload() {
		detachChildren();
		clearEntityModifiers();
		clearUpdateHandlers();
	}

	private static class MyResources extends SimpleLoadableResource {
		public ITiledTextureRegion TEXS_PANELS;
		private BuildableBitmapTextureAtlas mAtlases[] = new BuildableBitmapTextureAtlas[1];

		@Override
		public void onLoadResources(Engine e, Context c) {
			mAtlases[0] = new BuildableBitmapTextureAtlas(e.getTextureManager(), 1024, 1024);
			BuildableBitmapTextureAtlas atlasPanels = mAtlases[0];

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
	
	private float mTimePassed = 0;
	private float mTimePassedLong = 0;
	@Override
	public void setLightLevel(final LightConverter mLightConverter, final float pTimePassed) {
		mTimePassed += pTimePassed;
		mTimePassedLong += pTimePassed;

		if (mTimePassed >= 1) {	
			mTimePassed = 0;
	
			final float avg = mLightConverter.getLightValue(5);
//			Log.d(getClass().getSimpleName(), "avg=" + avg);
			
			for (int i = 0; i < mCells.length; i++) {
				Cell cell = mCells[i];
	
				if (((float)i)/(float)mCells.length < avg) {
					cell.setIsActive(true);
				} else {
					cell.setIsActive(false);
				}
			}
		}

		if (mTimePassedLong >= 6) {
			for (int i = 0; i < mCells.length; i++) {
				Cell cell = mCells[i];
				
				final float timeOffset = (float)i/(float)mCells.length * mTimePassedLong/6 + 0.1f;
				final float timeShiverFactor = 0.2f;
				IEntityModifier mod = new ParallelEntityModifier(
						new SequenceEntityModifier(
								new DelayModifier(timeOffset),
								new ScaleModifier(mTimePassedLong/3 * timeShiverFactor/2, 1f, 1.1f, 1, 1),
								new ScaleModifier(mTimePassedLong/3 * timeShiverFactor/2, 1.1f, 1f, 1, 1)
							),
						new SequenceEntityModifier(
							new DelayModifier(timeOffset),
							new AlphaModifier(mTimePassedLong/3 * timeShiverFactor, 1, 0.75f),
							new AlphaModifier(mTimePassedLong/3 * timeShiverFactor, 0.75f, 1)
						));
				mod.setAutoUnregisterWhenFinished(true);
				cell.registerEntityModifier(mod);
			}
			mTimePassedLong = 0;
		}
	}
}
