package de.upb.upbmonitor.service;

import org.json.JSONException;
import org.json.JSONObject;

import de.upb.upbmonitor.model.UeContext;
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
			// TODO implement assignment logic
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
			// no ap assigned
			if (backend_data.get("assigned_accesspoint") == null)
				return "none";
			// ap assigned, return URI as string
			return backend_data.get("assigned_accesspoint").toString();
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
		
		// null as string case (strange!?)
		if (ap_backend.equals("null") || ap_current.equals("null"))
			return false;
		
		// string case
		return !ap_backend.equals(ap_current);
	}
}
