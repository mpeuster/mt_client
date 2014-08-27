package de.upb.upbmonitor.network;

import android.util.Log;

public class Route
{
	private static final String LTAG = "Route";

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

	private String prefix;
	private String via;
	private String dev;

	public static Route parse(String input)
	{
		// parse command line output
		String prefix = null;
		String via = null;
		String dev = null;

		// at least prefix + vis or dev
		String[] parts = input.split(" ");
		if (parts.length < 3)
			return null;

		// parse for via or dev
		prefix = parts[0];
		if ("via".equals(parts[1]))
			via = parts[2];
		else if ("dev".equals(parts[1]))
			dev = parts[2];
		
		// parse for 3 as third part
		if(parts.length >= 5)
			if ("dev".equals(parts[3]))
				dev = parts[4];
		
		// Attention: all other parts like src, metrik, skope are ignored

		// create new route object
		return new Route(prefix, via, dev);
	}

	public Route(String prefix, String via, String dev)
	{
		this.prefix = prefix;
		this.via = via;
		this.dev = dev;
	}

	public String toString()
	{
		String part2 = "";
		String part3 = "";
		if (via != null)
			part2 = " via " + via;
		if (dev != null)
			part3 = " dev " + dev;
		return this.prefix + part2 + part3;
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
		return true;
	}

}
