package de.upb.upbmonitor.network;

import java.util.ArrayList;

import android.util.Log;
import de.upb.upbmonitor.commandline.Shell;

public class RouteManager
{
	private static final String LTAG = "RouteManager";
	
	private static RouteManager INSTANCE;

	/**
	 * Use as singleton class.
	 * 
	 * @return class instance
	 */
	public synchronized static RouteManager getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new RouteManager();
		return INSTANCE;
	}

	public synchronized ArrayList<Route> getRoutes()
	{
		// get ip route show output
		ArrayList<String> out = Shell.executeBlocking("ip route show");
		// if output is less than one line, something went wrong
		if (out.size() < 1)
			return null;
		// create route objects from each line
		ArrayList<Route> result = new ArrayList<Route>();
		for (String l : out)
		{
			Route r = Route.parse(l);
			if (r != null)
				result.add(r);
		}	
		return result;
	}

	public synchronized void addRoute(Route r)
	{
		Shell.execute("ip route add " + r.toString());
	}

	public synchronized void removeRoute(Route r)
	{
		Shell.execute("ip route del " + r.toString());
	}

}
