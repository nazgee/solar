package eu.nazgee.simple;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;

import eu.nazgee.game.utils.helpers.AtlasLoader;
import eu.nazgee.game.utils.scene.SceneLoader;
import eu.nazgee.game.utils.scene.SceneLoader.ISceneLoaderListener;
import eu.nazgee.game.utils.scene.SceneLoader.eLoadingSceneHandling;
import eu.nazgee.game.utils.scene.SceneLoading;
import eu.nazgee.game.utils.scene.SceneSplash;

public class SimpleActivity extends SimpleBaseGameActivity implements IOnMenuItemClickListener, SceneMain.GameHandler{
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private ITextureRegion mAETextureRegion;
	private ITextureRegion mNazgeeTextureRegion;
	private Font mFont;
	private MenuMain mMainMenu;
	private SceneMain mSceneMain;
	private SceneLoader mLoader;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, SimpleActivity.CAMERA_WIDTH, SimpleActivity.CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(SimpleActivity.CAMERA_WIDTH, SimpleActivity.CAMERA_HEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		FontFactory.setAssetBasePath("font/");
		final TextureManager textureManager = getTextureManager();
		final FontManager fontManager = getFontManager();

		BuildableBitmapTextureAtlas atlas = new BuildableBitmapTextureAtlas(textureManager, 512, 512);
		this.mAETextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, SimpleActivity.this, "ae.png");
		this.mNazgeeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, SimpleActivity.this, "nazgee.png");
		AtlasLoader.buildAndLoad(atlas);

		final ITexture textureFontHud = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);
		this.mFont = FontFactory.createFromAsset(fontManager, textureFontHud, getAssets(), "F-Rotten.ttf", CAMERA_HEIGHT*0.15f, true, Color.WHITE.getARGBPackedInt());
		this.mFont.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		// Create "Loading..." scene that will be used for all loading-related activities
		SceneLoading loadingScene = new SceneLoading(CAMERA_WIDTH, CAMERA_HEIGHT, mFont, "Loading...", getVertexBufferObjectManager());

		// Prepare loader, that will be used for all loading-related activities (besides splash-screen)
		mLoader = new SceneLoader(loadingScene, getEngine(), this);
		mLoader.setLoadingSceneHandling(eLoadingSceneHandling.SCENE_SET_ACTIVE).setLoadingSceneUnload(false);

		// Prepare a splash screen- first scene even shown
		final SceneSplash splash = new SceneSplash(CAMERA_WIDTH, CAMERA_HEIGHT, Color.BLACK, 2, mFont, getVertexBufferObjectManager());
		splash.addSplashScreen(new Sprite(0, 0, mAETextureRegion, getVertexBufferObjectManager()));
		splash.addSplashScreen(new Sprite(0, 0, mNazgeeTextureRegion, getVertexBufferObjectManager()));

		// Prepare loader, that will load the first scene, while showing splash-screen
		SceneLoader loader = new SceneLoader(splash, getEngine(), this);
		loader.setLoadingSceneHandling(eLoadingSceneHandling.SCENE_DONT_TOUCH).setLoadingSceneUnload(true);

		// Start loading the first scene
		mMainMenu = new MenuMain(CAMERA_WIDTH, CAMERA_HEIGHT, getEngine().getCamera(), mFont, getVertexBufferObjectManager());
		mMainMenu.setOnMenuItemClickListener(this);
		loader.loadScene(mMainMenu, getEngine(), this, null);

		// Show splash screen
		return splash;
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		if (pMenuScene == mMainMenu) {
			switch (pMenuItem.getID()) {
			case MenuMain.MENU_QUIT:
				this.finish();
				break;
			case MenuMain.MENU_GO:
				mSceneMain = new SceneMain(CAMERA_WIDTH, CAMERA_HEIGHT, getVertexBufferObjectManager(), this);
				mLoader.loadScene(mSceneMain, getEngine(), SimpleActivity.this, null);
				break;
			default:
				break;
			}
		}
		return false;
	}

	@Override
	public void onFinished() {
		// Go back to main menu, when game is finished
		mLoader.loadScene(mMainMenu, getEngine(), SimpleActivity.this, new ISceneLoaderListener() {
			@Override
			public void onSceneLoaded(Scene pScene) {
				TimerHandler timer = new TimerHandler(3, new ITimerCallback() {
					@Override
					public void onTimePassed(TimerHandler pTimerHandler) {
						mSceneMain = new SceneMain(CAMERA_WIDTH, CAMERA_HEIGHT, getVertexBufferObjectManager(), SimpleActivity.this);
						mLoader.loadScene(mSceneMain, getEngine(), SimpleActivity.this, null);
					}
				});
				pScene.registerUpdateHandler(timer);
			}
		});
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}