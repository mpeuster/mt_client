package de.upb.upbmonitor.service;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import de.upb.upbmonitor.model.UeContext;

/**
 * Represents the monitoring thread of the service.
 * 
 * Attention: This Runnable/Handler implementation is the way to do periodic
 * tasks in Android. Do not use Java timers. However its a bit ugly.
 * 
 * @author manuel
 * 
 */
public class MonitoringThread implements Runnable
{
	private static final String LTAG = "MonitoringThread";
	private Handler myHandler;
	private int mInterval;

	private SystemMonitor mSystemMonitor;
	private NetworkMonitor mNetworkMonitor;

	public MonitoringThread(Handler myHandler, int monitoringInterval)
	{ // arguments
		this.myHandler = myHandler;
		this.mInterval = monitoringInterval;

		// initializations
		this.mSystemMonitor = new SystemMonitor(UeContext.getInstance()
				.getApplicationContext());
		this.mNetworkMonitor = new NetworkMonitor();

		// kick off periodic run
		this.getHandler().postDelayed(this, 0);
	}

	public void run()
	{
		Log.v(LTAG, "Awake with interval: " + this.mInterval);
		// periodically monitor
		this.monitor();
		myHandler.postDelayed(this, this.mInterval);
	}

	private void monitor()
	{
		Log.d(LTAG, "Monitoring @ " + SystemClock.elapsedRealtime());
		// call monitoring components
		this.mSystemMonitor.monitor();
		this.mNetworkMonitor.monitor();
		// update model
		UeContext c = UeContext.getInstance();
		c.incrementUpdateCount();	
	}

	public synchronized Handler getHandler()
	{
		return this.myHandler;
	}
}
