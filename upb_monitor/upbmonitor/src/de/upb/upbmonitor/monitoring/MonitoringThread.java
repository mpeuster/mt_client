package de.upb.upbmonitor.monitoring;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import de.upb.upbmonitor.monitoring.model.UeContext;

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
	private Context myContext;
	private Handler myHandler;
	private int mMonitoringInterval;
	
	private SystemMonitor mSystemMonitor;
	private NetworkMonitor mNetworkMonitor;

	public MonitoringThread(Context myContext, Handler myHandler,
			int monitoringInterval)
	{	// arguments
		this.myContext = myContext;
		this.myHandler = myHandler;
		this.mMonitoringInterval = monitoringInterval;
		
		// initializations
		this.mSystemMonitor = new SystemMonitor(this.myContext);	
		this.mNetworkMonitor = new NetworkMonitor(this.myContext);
	}

	public void run()
	{
		Log.v(LTAG, "Awake with interval: "
				+ this.mMonitoringInterval);
		// periodically monitor
		this.monitor();
		myHandler.postDelayed(this, this.mMonitoringInterval);
	}

	private void monitor()
	{
		// call monitoring components
		this.mSystemMonitor.monitor();
		this.mNetworkMonitor.monitor();
		// update model
		UeContext c = UeContext.getInstance();
		c.incrementUpdateCount();
	}
}
