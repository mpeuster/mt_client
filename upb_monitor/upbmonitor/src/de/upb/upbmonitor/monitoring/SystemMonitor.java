package de.upb.upbmonitor.monitoring;

import de.upb.upbmonitor.monitoring.model.UeContext;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

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
		boolean enabled;
		int px = 0;
		int py = 0;
		// try to get manually set location from shared preferences
		try
		{
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this.myContext);
			// location preferences
			enabled = preferences.getBoolean("pref_enable_manual_location",
					false);
			px = preferences.getInt("pref_manual_location_x", 0);
			py = preferences.getInt("pref_manual_location_y", 0);
		} catch (Exception e)
		{
			Log.e(LTAG, "Error reading preferences. Using default values.");
			enabled = false;
		}
		// only use values if manual location is enabled
		if (!enabled)
		{
			px = 0;
			py = 0;
		}
		// set values in model
		UeContext c = UeContext.getInstance();
		c.setPositionX(px);
		c.setPositionY(py);
	}

}
