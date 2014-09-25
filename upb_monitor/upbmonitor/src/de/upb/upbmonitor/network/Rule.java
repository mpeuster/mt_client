package de.upb.upbmonitor.network;

public class Rule
{
	private static final String LTAG = "Roule";
	private String from;
	private String lookup;

	public String getFrom()
	{
		return from;
	}

	public void setFrom(String from)
	{
		this.from = from;
	}

	public String getLookup()
	{
		return lookup;
	}

	public void setLookup(String lookup)
	{
		this.lookup = lookup;
	}

	public static Rule parse(String input)
	{
		// parse command line output
		String from = null;
		String lookup = null;

		// at least from + table
		String[] parts = input.split("\\s+");
		if (parts.length < 3)
			return null;

		from = getRuleValueByKey(parts, "from");
		lookup = getRuleValueByKey(parts, "lookup");

		// create new route object
		return new Rule(from, lookup);
	}

	private static String getRuleValueByKey(String[] parts, String k)
	{
		for (int i = 0; i < parts.length - 1; i++)
		{
			if (k.equals(parts[i]))
				return parts[i + 1];
		}
		// not found
		return null;
	}

	public Rule(String from, String lookup)
	{
		this.from = from;
		this.lookup = lookup;
	}

	public String toString()
	{
		return "from " + this.from + " lookup " + this.lookup;
	}

	public boolean equals(Rule r)
	{
		if (this.from != null && r.getFrom() != null)
			if (!this.from.equals(r.getFrom()))
				return false;
		if (this.lookup != null && r.getLookup() != null)
			if (!this.lookup.equals(r.getLookup()))
				return false;
		return true;
	}

}
