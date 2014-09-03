package de.upb.upbmonitor.service;

import org.json.JSONException;
import org.json.JSONObject;

import de.upb.upbmonitor.model.ApModel;
import de.upb.upbmonitor.model.UeContext;
import de.upb.upbmonitor.network.NetworkManager;
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

	public AssignmentThread(Handler myHandler, int interval)
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

		// get latest backend assignment and current assignment
		String assignedApBackend = this.getAssignedApUriFromBackend();
		String assignedApCurrent = UeContext.getInstance().getAssignedApURI();

		if (isConnectionChangeNeeded(assignedApBackend, assignedApCurrent))
		{
			Log.i(LTAG, "Connection change needed!" + " Backend: "
					+ assignedApBackend + " / Current: " + assignedApCurrent);
			UeContext.getInstance().setAssignedApURI(assignedApBackend);

			NetworkManager nm = NetworkManager.getInstance();
			
			if(assignedApBackend == null || assignedApBackend.equals("none"))
			{
				// No AP assigned by backend: disconnect from Wi-Fi
				
				// switch DNS
				nm.setDnsServer("8.8.8.8", "8.8.4.4", NetworkManager.MOBILE_INTERFACE);
			}
			else
			{
				// fetch information about assigned Wi-Fi
				String SSID = ApModel.getInstance().getSsid(assignedApBackend);
				String PSK = ApModel.getInstance().getPsk(assignedApBackend);
				if(SSID == null)
					Log.e(LTAG, "AP without SSID assigned!");
								
				// connect new assigned Wi-Fi
				Log.d(LTAG, "Trying to connect to Wi-Fi: " + SSID);
				
				// switch DNS
				nm.setDnsServer("8.8.8.8", "8.8.4.4", NetworkManager.WIFI_INTERFACE);
			}
			
			// set current Wi-Fi to backend Wi-Fi
			// TODO check for success?
			UeContext.getInstance().setAssignedApURI(assignedApBackend);
		}

		// re-schedule
		myHandler.postDelayed(this, this.mInterval);
	}

	public synchronized Handler getHandler()
	{
		return this.myHandler;
	}

	private String getAssignedApUriFromBackend()
	{
		// get latest backend data from model (produced by receiver thread)
		JSONObject backend_data = UeContext.getInstance().getBackendContext();
		// no backend data available yet
		if (backend_data == null)
			return null;
		try
		{
			String backend_ap_uri = backend_data.getString("assigned_accesspoint");
			// no ap assigned
			if (backend_ap_uri == null)
				return "none";
			if (backend_ap_uri.equals("null"))
				return "none";
			// ap assigned, return URI as string
			return backend_ap_uri;
		} catch (Exception e)
		{
			Log.e(LTAG, "Could not fetch assignment from backend data.");
		}
		return null;
	}

	private boolean isConnectionChangeNeeded(String ap_backend,
			String ap_current)
	{
		// null case
		if (ap_backend == null || ap_current == null)
			return false;
		
		// string case
		return !ap_backend.equals(ap_current);
	}
}
