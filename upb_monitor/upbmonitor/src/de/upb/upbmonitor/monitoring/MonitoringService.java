package de.upb.upbmonitor.monitoring;

import de.upb.upbmonitor.monitoring.model.UeContext;
import de.upb.upbmonitor.network.NetworkManager;
import de.upb.upbmonitor.rest.UeEndpoint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MonitoringService extends Service
{
	private static final String LTAG = "MonitoringService";
	public static boolean SERVICE_EXISTS = false;
	private static int MONITORING_INTERVAL = Integer.MAX_VALUE;
	private static int SENDING_INTERVAL = Integer.MAX_VALUE;
	private static String BACKEND_HOST;
	private static int BACKEND_PORT;

	private Handler threadHandler = new Handler();
	private MonitoringThread monitoringTask = null;
	private SenderThread sendingTask = null;

	@Override
	public void onCreate()
	{
		super.onCreate();
		SERVICE_EXISTS = true;
		Log.d(LTAG, "onCreate()");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(LTAG, "onDestroy()");
		threadHandler.removeCallbacks(monitoringTask);
		threadHandler.removeCallbacks(sendingTask);
		sendingTask.removeUe(); // attention not async!
		SERVICE_EXISTS = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(LTAG, "onStartCommand()");
		// load preferences
		try
		{
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			// monitoring preferences
			MONITORING_INTERVAL = Integer.valueOf(preferences.getString(
					"pref_monitoring_interval", "0"));
			SENDING_INTERVAL = Integer.valueOf(preferences.getString(
					"pref_sending_interval", "0"));
			// backend API destination preferences
			BACKEND_HOST = preferences.getString("pref_backend_api_address", null);
			BACKEND_PORT = Integer.valueOf(preferences.getString(
					"pref_backend_api_port", "5000"));
		} catch (Exception e)
		{
			// if preferences could not be read, use a fixed interval
			Log.e(LTAG, "Error reading preferences. Using fallback.");
			Toast.makeText(getApplicationContext(),
					"Error reading preferences. Check your inputs.",
					Toast.LENGTH_LONG).show();
			MONITORING_INTERVAL = 1000;
			SENDING_INTERVAL = 5000;
		}
		
		// initialize context model
		this.initializeContext();

		// run service's tasks
		if (!threadHandler.hasMessages(0))
		{
			// start monitoring task
			this.monitoringTask = new MonitoringThread(this,
					this.threadHandler, MONITORING_INTERVAL);
			threadHandler.postDelayed(monitoringTask, 0);
			Log.d(LTAG, "Monitoring task started");

			// start monitoring task
			this.sendingTask = new SenderThread(this, this.threadHandler,
					SENDING_INTERVAL, BACKEND_HOST, BACKEND_PORT);
			threadHandler.postDelayed(sendingTask, 0);
			Log.d(LTAG, "Sender task started");
		}
		// start sticky, so service will be restarted if it is killed
		return Service.START_STICKY;
	}

	private void initializeContext()
	{
		UeContext c = UeContext.getInstance();
		// values from preferences
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		c.setDeviceID(preferences
				.getString("prof_backend_device_id", "device0"));
		c.setLocationServiceID(preferences.getString(
				"prof_backend_locationservice_id", "node0"));
		// device values
		c.setWifiMac(NetworkManager.getInstance().getWiFiInterfaceMac());
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// for communication return IBinder implementation
		return null;
	}
}
