package de.upb.upbmonitor.service;

import android.os.Handler;
import android.util.Log;
import de.upb.upbmonitor.network.NetworkManager;
import de.upb.upbmonitor.rest.UeEndpoint;

/**
 * Represents the receiver thread of the service.
 * 
 * @author manuel
 * 
 */
public class ReceiverThread implements Runnable
{
	private static final String LTAG = "ReceiverThread";
	private Handler myHandler;
	private int mInterval;
	private UeEndpoint restUeEndpoint = null;

	public ReceiverThread(Handler myHandler, int interval, String backendHost,
			int backendPort)
	{
		// arguments
		this.myHandler = myHandler;
		this.mInterval = interval;

		// initializations
		// API end point
		this.restUeEndpoint = new UeEndpoint(NetworkManager.getInstance()
				.getIpByHostname(backendHost), backendPort);

		// kick off periodic run
		this.getHandler().postDelayed(this, 0);
	}

	public void run()
	{
		Log.v(LTAG, "Awake with interval: " + this.mInterval);

		// receive latest UE status (method writes result into model)
		this.restUeEndpoint.get();

		// re-schedule
		myHandler.postDelayed(this, this.mInterval);
	}

	public synchronized Handler getHandler()
	{
		return this.myHandler;
	}
}
