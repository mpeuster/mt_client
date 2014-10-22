package de.upb.upbmonitor.service;

import de.upb.upbmonitor.model.UeContext;
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
	
	private HandlerThread assignmentThread;
	private AssignmentThread assignmentTask = null;

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

		if(monitorTask.getHandler() != null)
			monitorTask.getHandler().removeCallbacks(monitorTask);
		monitorThread.quit();

		if(senderTask.getHandler() != null)
			senderTask.getHandler().removeCallbacks(senderTask);
		senderThread.quit();
		
		if(receiverTask.getHandler() != null)
			receiverTask.getHandler().removeCallbacks(receiverTask);
		receiverThread.quit();
		
		if(assignmentTask.getHandler() != null)
			assignmentTask.getHandler().removeCallbacks(receiverTask);
		assignmentThread.quit();

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
			// nslookup
			if(BACKEND_HOST != null)
			{
				BACKEND_HOST = NetworkManager.getInstance().getIpByHostname(BACKEND_HOST);
			}
			BACKEND_PORT = Integer.valueOf(preferences.getString(
					"pref_backend_api_port", "6680"));
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
		
		// ensure that backend route is set
		NetworkManager.getInstance().setBackendRoute(BACKEND_HOST);

		// start monitoring task (independent looper thread)
		this.monitorThread = new HandlerThread("MonitorThread");
		this.monitorThread.start();
		this.monitorTask = new MonitoringThread(new Handler(
				this.monitorThread.getLooper()), MONITORING_INTERVAL);
		
		// start sender task (independent looper thread)
		// OPTIONAL: Change sending to be event based and not interval based
		this.senderThread = new HandlerThread("SenderThread");
		this.senderThread.start();
		this.senderTask = new SenderThread(new Handler(
				this.senderThread.getLooper()), SENDING_INTERVAL,
				BACKEND_HOST, BACKEND_PORT);
				
		// start receiver task (independent looper thread)
		this.receiverThread = new HandlerThread("ReceiverThread");
		this.receiverThread.start();
		this.receiverTask = new ReceiverThread(new Handler(
				this.receiverThread.getLooper()), 2000,
				BACKEND_HOST, BACKEND_PORT);
				
		// start assignment task (independent looper thread)
		this.assignmentThread = new HandlerThread("AssignmentThread");
		this.assignmentThread.start();
		this.assignmentTask = new AssignmentThread(new Handler(
				this.assignmentThread.getLooper()), 1000);
		
		
		Log.i(LTAG, "Management service and its worker thrads successfully started.");
		
		// start sticky, so service will be restarted if it is killed
		return Service.START_STICKY;
	}

	private void initializeContext()
	{
		UeContext c = UeContext.getInstance();
		// pass application context to model
		UeContext.getInstance().setApplicationContext(this);
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
