package de.upb.upbmonitor.service;

import android.os.Handler;
import android.util.Log;


/**
 * Represents the assignment thread of the service.
 * 
 * @author manuel
 * 
 */
public class AssignmentThread implements Runnable
{
	private static final String LTAG = "AssignmentThread";
	private Handler myHandler;
	private int mInterval;


	public AssignmentThread(Handler myHandler,
			int interval)
	{ 
		// arguments
		this.myHandler = myHandler;
		this.mInterval = interval;
		
		// kick off periodic run
		this.getHandler().postDelayed(this, 0);
	}

	public void run()
	{
		Log.v(LTAG, "Awake with interval: " + this.mInterval);
		
		// TODO implement assignment logic
	
		// re-schedule
		myHandler.postDelayed(this, this.mInterval);
	}

	public synchronized Handler getHandler()
	{
		return this.myHandler;
	}
}
