package de.upb.upbmonitor.service;

import de.upb.upbmonitor.model.UeContext;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SystemMonitor
{
	private static final String LTAG = "SystemMonitor";
	private Context myContext;

	public SystemMonitor(Context myContext)
	{
		this.myContext = myContext;
	}

	public void monitor()
	{
		this.monitorActiveApplication();
		this.monitorScreenState();
		this.monitorLocation();
	}

	/**
	 * screen state monitoring
	 */
	public void monitorScreenState()
	{
		// get screen state from power model
		PowerManager pm = (PowerManager) this.myContext
				.getSystemService(Context.POWER_SERVICE);
		boolean screen_state = pm.isScreenOn();
		// write results to model
		UeContext c = UeContext.getInstance();
		c.setDisplayState(screen_state);
	}

	/**
	 * active application monitoring
	 */
	public void monitorActiveApplication()
	{
		// get activity manager and receive info
		ActivityManager am = (ActivityManager) this.myContext
				.getSystemService(Context.ACTIVITY_SERVICE);

		// get the info from the currently running task
		try
		{
			// receives task list with max_tasks = 1 and fetches the first
			// element of the list
			RunningTaskInfo taskInfo = am.getRunningTasks(1).get(0);

			// write results to model
			UeContext c = UeContext.getInstance();
			c.setActiveApplicationPackage(taskInfo.topActivity.getPackageName());
			c.setActiveApplicationActivity(taskInfo.topActivity.getClassName());
		} catch (Exception e)
		{
			Log.w(LTAG, "Not able to get running task info: " + e.getMessage());
		}
	}

	/**
	 * location monitoring. looks in the shared preferences if manual location
	 * definition is enabled.
	 * 
	 * reads location info and sets it to the model. other solutions for finding
	 * a location directly on the UE should be implemented here.
	 */
	public void monitorLocation()
	{
		boolean manual_enabled;
		boolean volume_enabled;
		int px = 0;
		int py = 0;
		// try to get manually set location from shared preferences
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this.myContext);
		try
		{
			// location preferences
			manual_enabled = preferences.getBoolean(
					"pref_enable_manual_location", false);
			volume_enabled = preferences.getBoolean(
					"pref_enable_volume_location", false);
		} catch (Exception e)
		{
			Log.e(LTAG, "Error reading preferences. Using default values.");
			manual_enabled = false;
			volume_enabled = false;
		}
		// only use values if manual location is enabled
		if (manual_enabled)
		{
			this.setManualLocationInModel(preferences);
			return;
		}
		// only use values if volume location is enabled
		if (volume_enabled)
		{
			this.setVolumeLocationInModel(preferences);
			return;
		}
		// use default values instead
		UeContext.getInstance().setPositionX(0);
		UeContext.getInstance().setPositionY(0);
	}

	private void setManualLocationInModel(SharedPreferences p)
	{
		UeContext.getInstance().setPositionX(
				p.getInt("pref_manual_location_x", 0));
		UeContext.getInstance().setPositionY(
				p.getInt("pref_manual_location_y", 0));
	}

	private void setVolumeLocationInModel(SharedPreferences p)
	{
		// OPTIONAL make predefined volume locations configurable
		float[] PREDEFINED_X = { 0, 800, 0, 800 };
		float[] PREDEFINED_Y = { 0, 0, 800, 800 };

		AudioManager am = (AudioManager) myContext
				.getSystemService(Context.AUDIO_SERVICE);
		int lvl1 = am.getStreamVolume(AudioManager.STREAM_RING);
		int lvl2 = am.getStreamVolume(AudioManager.STREAM_MUSIC);

		// compute predefined location index as modulo of volume levels
		int idx = (lvl1 + lvl2)
				% (Math.min(PREDEFINED_X.length, PREDEFINED_Y.length) - 1);
	
		UeContext c = UeContext.getInstance();

		// if something changed, show a toast
		if (c.getPositionX() != PREDEFINED_X[idx]
				|| c.getPositionY() != PREDEFINED_Y[idx])
		{
			Toast.makeText(
					myContext,
					"Changed location to: " + PREDEFINED_X[idx] + "/"
							+ PREDEFINED_Y[idx], Toast.LENGTH_SHORT).show();
			Log.i(LTAG, "Volume location change to: " + PREDEFINED_X[idx] + "/"
					+ PREDEFINED_Y[idx]);
		}

		c.setPositionX(PREDEFINED_X[idx]);
		c.setPositionY(PREDEFINED_Y[idx]);
	}

}
