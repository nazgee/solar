package eu.nazgee.prank.solar;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import eu.nazgee.game.utils.scene.SceneLoader;
import eu.nazgee.game.utils.scene.SceneLoader.ISceneLoaderListener;
import eu.nazgee.game.utils.scene.SceneLoader.eLoadingSceneHandling;
import eu.nazgee.game.utils.scene.SceneLoading;

public class SimpleActivity extends SimpleBaseGameActivity{
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private Font mFont;
	private SceneMain mSceneMain;
	private SceneLoader mLoader;
	private LightConverter mLightConverter;

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

//		BuildableBitmapTextureAtlas atlas = new BuildableBitmapTextureAtlas(textureManager, 512, 512);
//		this.mAETextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, SimpleActivity.this, "ae.png");
//		this.mNazgeeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, SimpleActivity.this, "nazgee.png");
//		AtlasLoader.buildAndLoad(atlas);

		final ITexture textureFontHud = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);
		this.mFont = FontFactory.createFromAsset(fontManager, textureFontHud, getAssets(), "LCD.ttf", CAMERA_HEIGHT*0.15f, true, Color.WHITE.getARGBPackedInt());
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
		
		mSceneMain = new SceneMain(CAMERA_WIDTH, CAMERA_HEIGHT, getVertexBufferObjectManager());

		// Start loading the first scene
		mLoader.loadScene(mSceneMain, getEngine(), this, new MainSceneLoadedListener());

		// Show splash screen
		return loadingScene;
	}

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		if (!enableLightSensor()) {
			Log.e(getClass().getSimpleName(), "light sensor is NOT supported!");
		} else {
			Log.i(getClass().getSimpleName(), "light sensor is supported");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		disableLightSensor();
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public boolean enableLightSensor() {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if(this.isSensorSupported(sensorManager, Sensor.TYPE_LIGHT)) {
			this.registerSelfAsSensorListener(sensorManager, Sensor.TYPE_LIGHT, SensorManager.SENSOR_DELAY_FASTEST);
			return true;
		} else {
			return false;
		}
	}

	public boolean disableLightSensor() {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if(this.isSensorSupported(sensorManager, Sensor.TYPE_LIGHT)) {
			this.unregisterSelfAsSensorListener(sensorManager, Sensor.TYPE_LIGHT);
			return true;
		} else {
			return false;
		}
	}

	private boolean isSensorSupported(final SensorManager pSensorManager, final int pType) {
		return pSensorManager.getSensorList(pType).size() > 0;
	}

	private void registerSelfAsSensorListener(final SensorManager pSensorManager, final int pType, final int pSensorDelay) {
		final Sensor sensor = pSensorManager.getSensorList(pType).get(0);
		mLightConverter = new LightConverter(mSceneMain, sensor.getMaximumRange());
		pSensorManager.registerListener(mLightConverter, sensor, pSensorDelay);

		getEngine().registerUpdateHandler(new TimerHandler(1f, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				mSceneMain.setLightLevel(mLightConverter.getLightValue());
				pTimerHandler.reset();
			}
		}));
	}

	private void unregisterSelfAsSensorListener(final SensorManager pSensorManager, final int pType) {
		final Sensor sensor = pSensorManager.getSensorList(pType).get(0);
		pSensorManager.unregisterListener(mLightConverter, sensor);
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	class MainSceneLoadedListener implements ISceneLoaderListener {
		@Override
		public void onSceneLoaded(Scene pScene) {

		}
	}
}