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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import eu.nazgee.game.utils.misc.AppRater;
import eu.nazgee.game.utils.scene.SceneLoader;
import eu.nazgee.game.utils.scene.SceneLoader.eLoadingSceneHandling;
import eu.nazgee.game.utils.scene.SceneLoading;

public class ActivityMain extends SimpleBaseGameActivity{
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 720;

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
		final Camera camera = new Camera(0, 0, ActivityMain.CAMERA_WIDTH, ActivityMain.CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(ActivityMain.CAMERA_WIDTH, ActivityMain.CAMERA_HEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		FontFactory.setAssetBasePath("font/");
		final TextureManager textureManager = getTextureManager();
		final FontManager fontManager = getFontManager();

		final ITexture textureFontHud = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);
		this.mFont = FontFactory.createFromAsset(fontManager, textureFontHud, getAssets(), "LCD.ttf", CAMERA_WIDTH*0.1f, true, Color.WHITE.getARGBPackedInt());
		this.mFont.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		// Create "Loading..." scene that will be used for all loading-related activities
		SceneLoading loadingScene = new SceneLoading(CAMERA_WIDTH, CAMERA_HEIGHT, mFont, "BOOTING...", getVertexBufferObjectManager());

		// Prepare loader, that will be used for all loading-related activities (besides splash-screen)
		mLoader = new SceneLoader(loadingScene, getEngine(), this);
		mLoader.setLoadingSceneHandling(eLoadingSceneHandling.SCENE_SET_ACTIVE).setLoadingSceneUnload(false);
		
		mSceneMain = new SceneMain(CAMERA_WIDTH, CAMERA_HEIGHT, getVertexBufferObjectManager());

		// Start loading the first scene
		mLoader.loadScene(mSceneMain, getEngine(), this, new MainSceneLoadedListener());

		mHud = new HUD(mSceneMain.getW(), mSceneMain.getH(), getVertexBufferObjectManager());
		mSceneMain.getLoader().install(mHud);
		
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
	public boolean enableSensor(int pSensor) {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if(this.isSensorSupported(sensorManager, pSensor)) {
			this.registerSelfAsSensorListener(sensorManager, pSensor, SensorManager.SENSOR_DELAY_FASTEST);
			return true;
		} else {
			return false;
		}
	}

	public boolean disableSensor(int pSensor) {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if(this.isSensorSupported(sensorManager, pSensor)) {
			this.unregisterSelfAsSensorListener(sensorManager, pSensor);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean enableLightSensor() {
		if (enableSensor(Sensor.TYPE_LIGHT)) {
			return true;
		} else {
			if (enableSensor(Sensor.TYPE_PROXIMITY)) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean disableLightSensor() {
		if (disableSensor(Sensor.TYPE_LIGHT)) {
			return true;
		} else {
			if (disableSensor(Sensor.TYPE_PROXIMITY)) {
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean isSensorSupported(final SensorManager pSensorManager, final int pType) {
		return pSensorManager.getSensorList(pType).size() > 0;
	}

	private void registerSelfAsSensorListener(final SensorManager pSensorManager, final int pType, final int pSensorDelay) {
		final Sensor sensor = pSensorManager.getSensorList(pType).get(0);
		
		SharedPreferences prefs = getSharedPreferences(Consts.PREFS_NAME, 0);
		float min = prefs.getFloat(Consts.PREFS_KEY_LIGHTMIN, Float.MAX_VALUE);
		float max = prefs.getFloat(Consts.PREFS_KEY_LIGHTMAX, Float.MIN_VALUE);
		mLightConverter = new LightConverter(mSceneMain, min, max, sensor.getMaximumRange());
		
		pSensorManager.registerListener(mLightConverter, sensor, pSensorDelay);

		getEngine().registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
			private int skipper = 0;
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				skipper = skipper++ % 10;
				
				if (mSceneMain.isLoaded()) {
					final float avg = mLightConverter.getLightValue(5);
					if (skipper == 0) {
						mSceneMain.setLightLevel(avg);
					}
					mHud.setProgressBar(avg);
				}
				pTimerHandler.reset();
			}
		}));
	}

	private void unregisterSelfAsSensorListener(final SensorManager pSensorManager, final int pType) {
		final Sensor sensor = pSensorManager.getSensorList(pType).get(0);
		pSensorManager.unregisterListener(mLightConverter, sensor);
		
		SharedPreferences prefs = getSharedPreferences(Consts.PREFS_NAME, 0);
		Editor e = prefs.edit();
		e.putFloat(Consts.PREFS_KEY_LIGHTMIN, mLightConverter.getLightValueMin());
		e.putFloat(Consts.PREFS_KEY_LIGHTMAX, mLightConverter.getLightValueMax());
		e.commit();
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	class MainSceneLoadedListener implements SceneLoader.ISceneLoaderListener {
		@Override
		public void onSceneLoaded(Scene pScene) {
			// HUD should be also loaded by now
			getEngine().getCamera().setHUD(mHud);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Drawable icon = getResources().getDrawable(
							R.drawable.ic_launcher);
					AppRater.app_launched(
							ActivityMain.this,
							"I hope you love \"charging\" your phone by this app. Can you rate it, please?",
							getPackageName(), icon, 3, 5);
				}
			});
		}
	}

	public static final String PREFS_KEY_TOTAL_BUBBLES_POPPED = "lightmin";
	private HUD mHud;
}