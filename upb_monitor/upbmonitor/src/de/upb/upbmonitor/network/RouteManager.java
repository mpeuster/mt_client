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
	
	public synchronized ArrayList<Rule> getRuleList()
	{
		// get ip route show output
		ArrayList<String> out = Shell.executeBlocking("ip rule show");
		// if output is less than one line, something went wrong
		if (out.size() < 1)
			return null;
		// create rule objects from each line
		ArrayList<Rule> result = new ArrayList<Rule>();
		for (String l : out)
		{
			Rule r = Rule.parse(l);
			if (r != null)
				result.add(r);
		}
		return result;
	}

	/**
	 * Get first matching route. Use "null" as wildcard.
	 * 
	 * @param prefix
	 * @param via
	 * @param dev
	 * @return
	 */
	public synchronized Route getRoute(String prefix, String via, String dev,
			String scope, String table)
	{
		for (Route r : this.getRouteList())
		{
			boolean prefix_ok = true;
			boolean via_ok = true;
			boolean dev_ok = true;
			boolean scope_ok = true;
			boolean table_ok = true;

			if (prefix != null)
				if (!prefix.equals(r.getPrefix()))
					prefix_ok = false;

			if (via != null)
				if (!via.equals(r.getVia()))
					via_ok = false;

			if (dev != null)
				if (!dev.equals(r.getDev()))
					dev_ok = false;

			if (scope != null)
				if (!scope.equals(r.getScope()))
					scope_ok = false;

			if (table != null)
				if (!table.equals(r.getTable()))
					table_ok = false;

			if (prefix_ok && via_ok && dev_ok && scope_ok && table_ok)
				return r;
		}
		return null;
	}

	public synchronized void addRoute(Route r)
	{
		if (r != null)
			Shell.execute("ip route add " + r.toString());
	}
	
	public synchronized void addRule(Rule r)
	{
		if (r != null)
			Shell.execute("ip rule add " + r.toString());
	}

	public synchronized void removeRoute(Route r)
	{
		if (r != null)
			Shell.execute("ip route del " + r.toString());
	}
	
	public synchronized void removeRule(Rule r)
	{
		if (r != null)
			Shell.execute("ip rule del " + r.toString());
	}

	public synchronized boolean routeExists(String prefix, String via,
			String dev, String scope, String table)
	{
		Route r = new Route(prefix, via, dev, scope, table);
		return routeExists(r);
	}

	public synchronized boolean routeExists(Route r)
	{
		for (Route r2 : this.getRouteList())
		{
			if (r2 != null)
				if (r2.equals(r))
					return true;
		}
		return false;
	}
	
	public synchronized boolean ruleExists(String from, String lookup)
	{
		Rule r = new Rule(from, lookup);
		return ruleExists(r);
	}

	public synchronized boolean ruleExists(Rule r)
	{
		for (Rule r2 : this.getRuleList())
		{
			if (r2 != null)
				if (r2.equals(r))
					return true;
		}
		return false;
	}

	public synchronized void setDefaultRouteToWiFi()
	{
		Log.i(LTAG, "Setting default route to: "
				+ NetworkManager.WIFI_INTERFACE);
		// remove existing default routes
		this.removeDefaultRoutes();
		// add new default route
		Route r = new Route("default", NetworkManager.getInstance()
				.getWifiGateway(), NetworkManager.WIFI_INTERFACE);
		this.addRoute(r);
	}

	public synchronized void setDefaultRouteToMobile()
	{
		Log.i(LTAG, "Setting default route to: "
				+ NetworkManager.MOBILE_INTERFACE);
		// remove existing default routes
		this.removeDefaultRoutes();
		// add new default route
		Route r = new Route("default", null, NetworkManager.MOBILE_INTERFACE);
		this.addRoute(r);
	}

	private synchronized void removeDefaultRoutes()
	{
		Route r;
		// remove default rmnet route (if present)
		r = this.getRoute("default", null, NetworkManager.MOBILE_INTERFACE, null, null);
		if (r != null)
			this.removeRoute(r);
		// remove default rmnet route (if present)
		r = this.getRoute("default", null, NetworkManager.WIFI_INTERFACE, null, null);
		if (r != null)
			this.removeRoute(r);
	}

}
