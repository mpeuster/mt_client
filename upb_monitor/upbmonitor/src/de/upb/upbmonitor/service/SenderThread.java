package de.upb.upbmonitor.service;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import de.upb.upbmonitor.model.UeContext;
import de.upb.upbmonitor.network.NetworkManager;
import de.upb.upbmonitor.rest.UeEndpoint;

/**
 * Represents the sender thread of the service.
 * 
 * @author manuel
 * 
 */
public class SenderThread implements Runnable
{
	private static final String LTAG = "SenderThread";
	private Handler myHandler;
	private int mInterval;
	private UeEndpoint restUeEndpoint = null;
	private int mRegisterTries = 0;

	public SenderThread(Handler myHandler, int interval, String backendHost,
			int backendPort)
	{
		// arguments
		this.myHandler = myHandler;
		this.mInterval = interval;

		// initializations
		// API end point
		this.restUeEndpoint = new UeEndpoint(backendHost, backendPort);

		// kick off periodic run
		this.getHandler().postDelayed(this, 0);
	}

	public void run()
	{
		Log.v(LTAG, "Awake with interval: " + this.mInterval);
		// access model
		UeContext c = UeContext.getInstance();

		if (c.getURI() == null)
		{
			// register UE in backend
			this.restUeEndpoint.register();
			// show toast message every n-th retry
			if ((this.mRegisterTries - 1) % 5 == 0)
			{
				Toast.makeText(UeContext.getInstance().getApplicationContext(),
						"Backend connection error.", Toast.LENGTH_SHORT).show();
			}
			this.mRegisterTries++;
		} else
		{
			// periodically send update if UE is registered
			this.sendUpdate();
		}

		// re-schedule
		myHandler.postDelayed(this, this.mInterval);
	}

	private void sendUpdate()
	{
		// access model
		UeContext c = UeContext.getInstance();

		// send context update if new data is available (context has changed)
		if (c.hasChanged())
		{
			// detailed log output:
			Log.v(LTAG, c.toString());

			// send to backend
			this.restUeEndpoint.update();

			// reset changed flag in all models
			c.resetDataChangedFlag();
		}
	}

	public void removeUe()
	{
		// access model
		UeContext c = UeContext.getInstance();
		// remove UE from backend
		this.restUeEndpoint.remove();
	}

	public synchronized Handler getHandler()
	{
		return this.myHandler;
	}
}
