package de.upb.upbmonitor.network;

import android.util.Log;

public class Route
{
	private static final String LTAG = "Route";
	private String prefix;
	private String via;
	private String dev;
	private String scope;
	private String table;

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public String getVia()
	{
		return via;
	}

	public void setVia(String via)
	{
		this.via = via;
	}

	public String getDev()
	{
		return dev;
	}

	public void setDev(String dev)
	{
		this.dev = dev;
	}
	
	public String getScope()
	{
		return scope;
	}

	public void setScope(String scope)
	{
		this.scope = scope;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}

	public static Route parse(String input)
	{
		// parse command line output
		String prefix = null;
		String via = null;
		String dev = null;
		String scope = null;
		String table = null;

		// at least prefix + via or dev
		String[] parts = input.split("\\s+");
		if (parts.length < 3)
			return null;

		// parse for options
		prefix = parts[0];
		via = getRouteValueByKey(parts, "via");
		dev = getRouteValueByKey(parts, "dev");
		scope = getRouteValueByKey(parts, "scope");
		table = getRouteValueByKey(parts, "table");

		// create new route object
		return new Route(prefix, via, dev, scope, table);
	}
	
	private static String getRouteValueByKey(String[] parts,String k)
	{
		for(int i = 0; i < parts.length - 1; i++)
		{
			if (k.equals(parts[i]))
				return parts[i+1];
		}
		// not found
		return null;
	}

	public Route(String prefix, String via, String dev)
	{
		this.prefix = prefix;
		this.via = via;
		this.dev = dev;
		this.scope = null;
		this.table = null;
	}
	
	public Route(String prefix, String via, String dev, String scope, String table)
	{
		this.prefix = prefix;
		this.via = via;
		this.dev = dev;
		this.scope = scope;
		this.table = table;
	}

	public String toString()
	{
		String part2 = "";
		String part3 = "";
		String part4 = "";
		String part5 = "";
		if (via != null)
			part2 = " via " + via;
		if (dev != null)
			part3 = " dev " + dev;
		if (scope != null)
			part4 = " scope " + scope;
		if (table != null)
			part5 = " table " + table;
		return this.prefix + part2 + part3 + part4 + part5;
	}
	
	public boolean equals(Route r)
	{
		if(this.prefix != null && r.getPrefix() != null)
			if (!this.prefix.equals(r.getPrefix()))
				return false;
		if(this.via != null && r.getVia() != null)
			if (!this.via.equals(r.getVia()))
				return false;
		if(this.dev != null && r.getDev() != null)
			if (!this.dev.equals(r.getDev()))
				return false;
		if(this.scope != null && r.getScope() != null)
			if (!this.scope.equals(r.getScope()))
				return false;
		if(this.table != null && r.getTable() != null)
			if (!this.table.equals(r.getTable()))
				return false;
		return true;
	}

}
