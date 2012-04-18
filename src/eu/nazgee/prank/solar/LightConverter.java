package eu.nazgee.prank.solar;

import org.andengine.util.adt.queue.CircularQueue;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class LightConverter implements SensorEventListener{
	
	private static final float MACHEPS = 0.001f;
	final private LightFeedback mLightFeedback;
	private float mAdvertisedSensorMax = 1;
	private Statistics mStats;

	public LightConverter(LightFeedback mLightFeedback,
			final float mKnownMin,
			final float mKnownMax,
			float mAdvertisedSensorMax) {
		super();
		mStats = new Statistics(mKnownMin, mKnownMax);
		this.mLightFeedback = mLightFeedback;
		this.mAdvertisedSensorMax = mAdvertisedSensorMax;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
//		sensor.get
	}

	@Override
	public void onSensorChanged(SensorEvent pEvent) {
		mStats.registerValue(pEvent.values[0]);
		
//		mLightFeedback.setLightLevel(val);
	}

	public float getLightValue(final float pTimeToAvg) {
		final float avg = mStats.getAverage(pTimeToAvg);
		if (avg < 0) {
			return 0.5f;
		}
		return (avg - mStats.getMin() + MACHEPS) / (mStats.getMax() - mStats.getMin() + MACHEPS);
	}
	
	public float getLightValueMax() {
		return mStats.getMax();
	}

	public float getLightValueMin() {
		return mStats.getMin();
	}

	public interface LightFeedback {
		void setLightLevel(final float pValue);
	}
	
	private class StatEntry {
		private final float mValue;
		private final Long mTimestamp;
		public StatEntry(final float pValue) {
			mValue = pValue;
			mTimestamp = System.currentTimeMillis();
		}

		private final float getAge(Long pTime) {
			if (pTime < mTimestamp) {
				throw new IllegalArgumentException(getClass().getSimpleName() + "younger than this one was given!");
			}	
			return (pTime - mTimestamp)/1000f;
		}
		
		public final float getAge() {
			return getAge(System.currentTimeMillis());
		}

		public final float getAge(StatEntry other) {
			if (other == null) {
				return getAge(System.currentTimeMillis());
			} else {
				return getAge(other.mTimestamp);
			}
		}

		public float getValue() {
			return mValue;
		}
	}
	private class Statistics {
		private float mMax;
		private float mMin;
		CircularQueue<StatEntry> mSamples = new CircularQueue<StatEntry>(20);
		
		public Statistics(float mKnownMin, float mKnownMax) {
			mMin = mKnownMin;
			mMax = mKnownMax;
		}

		synchronized public void registerValue(final float pValue) {
			mMax = Math.max(pValue, mMax);
			mMin = Math.min(pValue, mMin);
			mSamples.enter(new StatEntry(pValue));
			Log.d(getClass().getSimpleName(), "min=" + mMin + "; max=" + mMax + "; val=" + pValue);
		}
		
		synchronized public float getMax() {
			return mMax;
		}
		synchronized public float getMin() {
			return mMin;
		}
		synchronized public float getAverage(final float pTime) {
			float timeSoFar = 0;
			float valSoFar = 0;
			boolean bailEarly = false;
			StatEntry prevStat = null;
			for (int i = mSamples.size() - 1; i >=0 ; i--) {
				StatEntry stat = mSamples.get(i);
				float time = stat.getAge(prevStat);
				
				if ((timeSoFar + time) >= pTime) {
					time -= (timeSoFar + time - pTime);
					bailEarly = true;
				}
				
				timeSoFar += time;
				valSoFar += stat.getValue() * time;
				prevStat = stat;

				Log.d(getClass().getSimpleName(), "i=" + i + "; timeSoFar=" + timeSoFar + "; valSoFar=" + valSoFar);
				if (bailEarly) {
					break;
				}
			}

			if (mSamples.size() == 0) {
				return -1;
			} else {
				return valSoFar / timeSoFar;
			}
		}
	}
}
