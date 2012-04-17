package eu.nazgee.simple;

import java.util.Random;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;
import eu.nazgee.game.utils.helpers.AtlasLoader;
import eu.nazgee.game.utils.helpers.TiledTextureRegionFactory;
import eu.nazgee.game.utils.scene.SceneLoadable;

public class SceneMain extends SceneLoadable {

	MyResources mResources = new MyResources();
	private final GameHandler mGameHandler;

	private SceneMain(VertexBufferObjectManager pVertexBufferObjectManager, GameHandler pGameHandler) {
		super(pVertexBufferObjectManager);
		mGameHandler = pGameHandler;
		getLoader().install(mResources);
	}

	public SceneMain(float W, float H,
			VertexBufferObjectManager pVertexBufferObjectManager, GameHandler pGameHandler) {
		super(W, H, pVertexBufferObjectManager);
		this.mGameHandler = pGameHandler;
		getLoader().install(mResources);
	}

	@Override
	public void onLoadResources(Engine e, Context c) {
	}

	@Override
	public void onLoad(Engine e, Context c) {
		// create panels
		Sprite mPanels[] = new Sprite[mResources.TEXS_PANELS.getTileCount()];
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

		Random r = new Random();
		for (int i = 0; i < 10; i++) {
			Sprite s = new Sprite(getW() * r.nextFloat(),
					getH() * r.nextFloat(), mResources.TEX_SHOCKWAVE, getVertexBufferObjectManager());
			s.registerEntityModifier(new ScaleModifier(1 + 10 * r.nextFloat(), 2 * r.nextFloat() + 0.5f, 2 * r.nextFloat()));
			attachChild(s);
		}

		TimerHandler timer = new TimerHandler(10, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				mGameHandler.onFinished();
			}
		});
		registerUpdateHandler(timer);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onUnload() {
		detachChildren();
		clearEntityModifiers();
		clearUpdateHandlers();
	}

	private static class MyResources extends Resources {
		public ITextureRegion TEX_SHOCKWAVE;
		public TiledTextureRegion TEXS_PANELS;
		private BuildableBitmapTextureAtlas mAtlases[] = new BuildableBitmapTextureAtlas[2];

		@Override
		public void onLoadResources(Engine e, Context c) {
			mAtlases[0] = new BuildableBitmapTextureAtlas(e.getTextureManager(), 512, 512);
			mAtlases[1] = new BuildableBitmapTextureAtlas(e.getTextureManager(), 1024, 1024);
			BuildableBitmapTextureAtlas atlasRest = mAtlases[0];
			BuildableBitmapTextureAtlas atlasPanels = mAtlases[1];

			TEX_SHOCKWAVE = BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlasRest, c, "shockwave.png");
			TEXS_PANELS = TiledTextureRegionFactory.loadTiles(c, "gfx/", "panels", atlasPanels);
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

	public interface GameHandler {
		void onFinished();
	}
}
