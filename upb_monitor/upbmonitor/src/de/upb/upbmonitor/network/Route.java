package de.upb.upbmonitor.network;

public class Route
{
	private String prefix;
	private String via;
	private String dev;
	
	
	public static Route parse(String input)
	{
		// parse command line output
		String prefix = null;
		String via = null;
		String dev = null;
		
		
		// create new route object
		return new Route(prefix, via, dev);
	}

	public Route(String prefix, String via, String dev)
	{
		this.prefix = prefix;
		this.via = via;
		this.dev = dev;
	}
	
}
