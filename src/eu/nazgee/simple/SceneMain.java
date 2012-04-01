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
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;
import eu.nazgee.game.utils.helpers.AtlasLoader;
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
		Random r = new Random();
		for (int i = 0; i < 100; i++) {
			Sprite s = new Sprite(getW() * r.nextFloat(),
					getH() * r.nextFloat(), mResources.TEX_SHOCKWAVE, getVertexBufferObjectManager());
			s.registerEntityModifier(new ScaleModifier(1 + 10 * r.nextFloat(), 2 * r.nextFloat() + 0.5f, 2 * r.nextFloat()));
			attachChild(s);
		}

		TimerHandler timer = new TimerHandler(3, new ITimerCallback() {
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
		private BuildableBitmapTextureAtlas mAtlas;

		@Override
		public void onLoadResources(Engine e, Context c) {
			mAtlas = new BuildableBitmapTextureAtlas(e.getTextureManager(), 512, 512);
			TEX_SHOCKWAVE = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAtlas, c, "shockwave.png");
		}

		@Override
		public void onLoad(Engine e, Context c) {
			AtlasLoader.buildAndLoad(mAtlas);
		}

		@Override
		public void onUnload() {
			mAtlas.unload();
		}
	}

	public interface GameHandler {
		void onFinished();
	}
}
