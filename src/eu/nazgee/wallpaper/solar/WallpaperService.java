package eu.nazgee.wallpaper.solar;

import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.util.color.Color;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import eu.nazgee.game.utils.misc.AppRater;
import eu.nazgee.game.utils.scene.SceneLoader;
import eu.nazgee.game.utils.scene.SceneLoader.eLoadingSceneHandling;
import eu.nazgee.game.utils.scene.SceneLoading;
import eu.nazgee.wallpaper.solar.HUD.eChargeStatus;

public class WallpaperService extends BaseLiveWallpaperService{
	// ===========================================================
	// Constants
	// ===========================================================



	// ===========================================================
	// Fields
	// ===========================================================

	private Font mFont;
	private SceneMain mSceneMain;
	private SceneLoader mLoader;
	private LightConverter mLightConverter;
	private HUD mHud;
	private UpdateTimerHandler mUpdateTimerHandler;

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
		final Camera camera = new Camera(0, 0, Consts.CAMERA_WIDTH, Consts.CAMERA_HEIGHT);

		EngineOptions opts = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(Consts.CAMERA_WIDTH, Consts.CAMERA_HEIGHT), camera);
		return opts;
	}

	@Override
	public org.andengine.engine.Engine onCreateEngine(
			EngineOptions pEngineOptions) {
		return new LimitedFPSEngine(pEngineOptions, 20);
	}


	@Override
	protected void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		SVGBitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		FontFactory.setAssetBasePath("font/");
		final TextureManager textureManager = getTextureManager();
		final FontManager fontManager = getFontManager();

		final ITexture textureFontHud = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);
		this.mFont = FontFactory.createFromAsset(fontManager, textureFontHud, getAssets(), Consts.FONT, Consts.CAMERA_WIDTH*0.1f, true, Color.WHITE.getARGBPackedInt());
		this.mFont.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		mSceneMain = new SceneMain(Consts.CAMERA_WIDTH, Consts.CAMERA_HEIGHT, getVertexBufferObjectManager());

		mHud = new HUD(mSceneMain.getW(), mSceneMain.getH(), getVertexBufferObjectManager());
		mSceneMain.getLoader().install(mHud);
		
		mSceneMain.getLoader().loadResources(getEngine(), this);
		mSceneMain.getLoader().load(getEngine(), this);

		getEngine().getCamera().setHUD(mHud);

		// Show splash screen
		return mSceneMain;
	}

	@Override
	public synchronized void onResumeGame() {
		super.onResumeGame();
		if (!enableLightSensor()) {
			Log.e(getClass().getSimpleName(), "light sensor is NOT supported!");
		} else {
			Log.i(getClass().getSimpleName(), "light sensor is supported");
		}
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		disableLightSensor();
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public boolean enableSensor(int pSensor) {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if(this.isSensorSupported(sensorManager, pSensor)) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String rateString = prefs.getString(getResources().getString(R.string.wallpaper_settings_read_rate_key), "0");
	
			
			int rate = Integer.parseInt(rateString);
			Log.w(getClass().getSimpleName(), "sensor read rate=" + rateString + "/" + rate);
			this.registerSelfAsSensorListener(sensorManager, pSensor, rate);
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
		Log.w(getClass().getSimpleName(), "registerSelfAsSensorListener");

		final Sensor sensor = pSensorManager.getSensorList(pType).get(0);
		
		if (mLightConverter == null) {
			SharedPreferences prefs = getSharedPreferences(Consts.PREFS_NAME, 0);
			float min = prefs.getFloat(Consts.PREFS_KEY_LIGHTMIN, Float.MAX_VALUE);
			float max = prefs.getFloat(Consts.PREFS_KEY_LIGHTMAX, Float.MIN_VALUE);
			mLightConverter = new LightConverter(mSceneMain, min, max, sensor.getMaximumRange());
		}
		
		pSensorManager.registerListener(mLightConverter, sensor, pSensorDelay);

		getEngine().unregisterUpdateHandler(mUpdateTimerHandler);
		mUpdateTimerHandler = new UpdateTimerHandler(0.1f);
		getEngine().registerUpdateHandler(mUpdateTimerHandler);
	}

	private void unregisterSelfAsSensorListener(final SensorManager pSensorManager, final int pType) {
		Log.w(getClass().getSimpleName(), "unregisterSelfAsSensorListener");
		
		getEngine().unregisterUpdateHandler(mUpdateTimerHandler);
		final Sensor sensor = pSensorManager.getSensorList(pType).get(0);
		pSensorManager.unregisterListener(mLightConverter, sensor);
		
		SharedPreferences prefs = getSharedPreferences(Consts.PREFS_NAME, 0);
		Editor e = prefs.edit();
		e.putFloat(Consts.PREFS_KEY_LIGHTMIN, mLightConverter.getLightValueMin());
		e.putFloat(Consts.PREFS_KEY_LIGHTMAX, mLightConverter.getLightValueMax());
		e.commit();
	}
	
	public void updateMiliAmps(final float pTimePassed) {
		float mAhsPerSec = 1;
		mHud.incMiliAmps(mLightConverter.getLightValue(pTimePassed) * pTimePassed * mAhsPerSec);
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	class UpdateTimerHandler extends TimerHandler {
		public UpdateTimerHandler(float pTimerSeconds) {
			super(pTimerSeconds, new UpdateTimerCallback());
		}
	}

	class UpdateTimerCallback implements ITimerCallback {
		@Override
		public void onTimePassed(TimerHandler pTimerHandler) {
			
			if (mSceneMain.isLoaded()) {
				mSceneMain.setLightLevel(mLightConverter, 0.1f);
				
				final float avg = mLightConverter.getLightValue(5);
				if (avg < 0) {
					mHud.setProgressBar(0.01f);
				} else {
					mHud.setProgressBar(avg);
					if (avg < Consts.CHARGE_THRESHOLD) {
						mHud.setChargeStatus(eChargeStatus.SUSPEND);
					} else {
						updateMiliAmps(0.1f);
						mHud.setChargeStatus(eChargeStatus.CHARGE);
					}
				}
				
			}
			pTimerHandler.reset();
		}
	}

	@Override
	public final void onCreateResources(final OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
		this.onCreateResources();

		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public final void onCreateScene(final OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
		final Scene scene = this.onCreateScene();

		pOnCreateSceneCallback.onCreateSceneFinished(scene);
	}

	@Override
	public final void onPopulateScene(final Scene pScene, final OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
}