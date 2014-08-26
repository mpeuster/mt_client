package de.upb.upbmonitor.monitoring;

import de.upb.upbmonitor.monitoring.model.NetworkTraffic;
import android.content.Context;
import android.net.TrafficStats;


public class NetworkMonitor
{
	private static final String LTAG = "SystemMonitor";
	private Context myContext;

	public NetworkMonitor(Context myContext)
	{
		this.myContext = myContext;
	}

	public void monitor()
	{
		// update values in NetworkTraffic model
		NetworkTraffic nt = NetworkTraffic.getInstance();
		nt.setTotalRxBytes(TrafficStats.getTotalRxBytes());
		nt.setTotalTxBytes(TrafficStats.getTotalTxBytes());
		nt.setMobileRxBytes(TrafficStats.getMobileRxBytes());
		nt.setMobileTxBytes(TrafficStats.getMobileTxBytes());

	}

}
