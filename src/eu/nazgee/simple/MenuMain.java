package eu.nazgee.simple;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import android.content.Context;
import eu.nazgee.game.utils.scene.SceneTextmenu;

public class MenuMain extends SceneTextmenu {
	private final MyResources mResources = new MyResources();
	public static final int MENU_GO = 0;
	public static final int MENU_QUIT = 1;

	public MenuMain(float W, float H, Camera pCamera, Font pFont,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(W, H, pCamera, pFont, pVertexBufferObjectManager);
		getLoader().install(mResources);
		addMenuEntry("Go!", MENU_GO, Color.BLUE, Color.CYAN, getVertexBufferObjectManager());
		addMenuEntry("Quit", MENU_QUIT, Color.BLUE, Color.CYAN, getVertexBufferObjectManager());
		buildAnimations();
	}

	@Override
	public void onLoadResources(Engine e, Context c) {
	}

	@Override
	public void onLoad(Engine e, Context c) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		reset();
	}

	@Override
	public void onUnload() {
	}

	private static class MyResources extends Resources {
		public Font FONT;

		@Override
		public void onLoadResources(Engine e, Context c) {
			final ITexture textureFontHud = new BitmapTextureAtlas(e.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
			this.FONT = FontFactory.createFromAsset(e.getFontManager(), textureFontHud, c.getAssets(), "F-Rotten.ttf", 100, true, Color.BLACK_ARGB_PACKED_INT);
		}

		@Override
		public void onLoad(Engine e, Context c) {
			this.FONT.load();
		}

		@Override
		public void onUnload() {
			this.FONT.unload();
		}
	}
}
