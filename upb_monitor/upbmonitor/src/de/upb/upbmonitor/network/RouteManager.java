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

	public synchronized ArrayList<Route> getRouteList()
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
	
	/**
	 * Get first matching route.
	 * Use "null" as wildcard.
	 * @param prefix
	 * @param via
	 * @param dev
	 * @return
	 */
	public synchronized Route getRoute(String prefix, String via, String dev)
	{
		for(Route r : this.getRouteList())
		{
			boolean prefix_ok = true;
			boolean via_ok  = true;
			boolean dev_ok = true;
			
			if(prefix != null)
				if(!prefix.equals(r.getPrefix()))
					prefix_ok=false;
			
			if(via != null)
				if(!via.equals(r.getVia()))
					via_ok=false;
			
			if(dev != null)
				if(!dev.equals(r.getDev()))
					dev_ok=false;
			
			if(prefix_ok && via_ok && dev_ok)
				return r;
		}
		return null;
	}

	public synchronized void addRoute(Route r)
	{
		Shell.execute("ip route add " + r.toString());
	}

	public synchronized void removeRoute(Route r)
	{
		Shell.execute("ip route del " + r.toString());
	}
	
	public synchronized boolean routeExists(String prefix, String via, String dev)
	{
		Route r = new Route(prefix, via, dev);
		return routeExists(r);
	}
	
	public synchronized boolean routeExists(Route r)
	{
		for(Route r2 : this.getRouteList())
		{
			if(r2 != null)
				if (r2.equals(r))
				return true;
		}
		return false;
	}

}
