package de.upb.upbmonitor.assignment;

import android.util.Log;
import de.upb.upbmonitor.network.NetworkManager;
import de.upb.upbmonitor.rest.UeEndpoint;

public class AssignmentController
{
	
	private static final String LTAG = "AssignmentController";
	private static AssignmentController INSTANCE;
	private String backendhost;
	private int backendport;
	

	/**
	 * Use as singleton class.
	 * 
	 * @return class instance
	 */
	public synchronized static AssignmentController getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new AssignmentController();
		return INSTANCE;
	}
	
	public synchronized void updateConfiguration(String backendhost, int backendport)
	{
		this.backendhost = backendhost;
		this.backendport = backendport;
	}
		
	public synchronized void updateAssignment()
	{
		// fetch assignment from backend
		String assignment_url = this.fetchUeAssignment();
		Log.i(LTAG, "Current assignment is: " + assignment_url);
	}
	
	private String fetchUeAssignment()
	{
		UeEndpoint ue = new UeEndpoint(this.backendhost, this.backendport);
		ue.get();
		// TODO implement!!!
		return null;
	}
	
	

}
