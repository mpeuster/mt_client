package de.upb.upbmonitor.service;

import de.upb.upbmonitor.assignment.AssignmentController;
import de.upb.upbmonitor.monitoring.model.UeContext;
import de.upb.upbmonitor.network.NetworkManager;
import de.upb.upbmonitor.rest.UeEndpoint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ManagementService extends Service
{
	private static final String LTAG = "ManagementService";
	public static boolean SERVICE_EXISTS = false;
	private static int MONITORING_INTERVAL = Integer.MAX_VALUE;
	private static int SENDING_INTERVAL = Integer.MAX_VALUE;
	private static String BACKEND_HOST;
	private static int BACKEND_PORT;

	private HandlerThread monitorThread;
	private MonitoringThread monitorTask = null;

	private HandlerThread senderThread;
	private SenderThread senderTask = null;
	
	private HandlerThread receiverThread;
	private ReceiverThread receiverTask = null;

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

		monitorTask.getHandler().removeCallbacks(monitorTask);
		monitorThread.quit();

		senderTask.getHandler().removeCallbacks(senderTask);
		senderThread.quit();
		
		receiverTask.getHandler().removeCallbacks(receiverTask);
		receiverThread.quit();

		senderTask.removeUe(); // attention not async!
		SERVICE_EXISTS = false;
		
		Log.i(LTAG, "Management service and its worker thrads successfully stopped.");
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
			BACKEND_HOST = preferences.getString("pref_backend_api_address",
					null);
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

		// configure assignment controller
		AssignmentController.getInstance().updateConfiguration(BACKEND_HOST,
				BACKEND_PORT);

		// start monitoring task (independent looper thread)
		this.monitorThread = new HandlerThread("MonitorThread");
		this.monitorThread.start();
		this.monitorTask = new MonitoringThread(this, new Handler(
				this.monitorThread.getLooper()), MONITORING_INTERVAL);
		// kick off monitoring
		this.monitorTask.getHandler().postDelayed(monitorTask, 0);

		// start sender task (independent looper thread)
		this.senderThread = new HandlerThread("SenderThread");
		this.senderThread.start();
		this.senderTask = new SenderThread(this, new Handler(
				this.senderThread.getLooper()), SENDING_INTERVAL,
				BACKEND_HOST, BACKEND_PORT);
		// kick off sending
		this.senderTask.getHandler().postDelayed(senderTask, 0);
				
		// start receiver task (independent looper thread)
		// TODO: use push notification to trigger receiver task, instead of active polling
		this.receiverThread = new HandlerThread("ReceiverThread");
		this.receiverThread.start();
		this.receiverTask = new ReceiverThread(new Handler(
				this.receiverThread.getLooper()), 2000,
				BACKEND_HOST, BACKEND_PORT);
		// kick off receiving
		this.receiverTask.getHandler().postDelayed(receiverTask, 0);
		
		Log.i(LTAG, "Management service and its worker thrads successfully started.");
		
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