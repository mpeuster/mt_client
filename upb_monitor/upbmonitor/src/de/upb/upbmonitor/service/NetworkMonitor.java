package de.upb.upbmonitor.service;

import java.util.ArrayList;

import de.upb.upbmonitor.commandline.Shell;
import de.upb.upbmonitor.model.NetworkTraffic;
import de.upb.upbmonitor.network.NetworkManager;
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
		// measure values (use command line since SDK version produces wrong values)
		long mobileRx = this.getMobileRxBytes();
		long mobileTx = this.getMobileTxBytes();
		long wifiRx = this.getWifiRxBytes();
		long wifiTx = this.getWifiTxBytes();
		
		// update values in NetworkTraffic model
		NetworkTraffic nt = NetworkTraffic.getInstance();
		nt.setTotalRxBytes(mobileRx + wifiRx);
		nt.setTotalTxBytes(mobileTx + wifiTx);
		nt.setMobileRxBytes(mobileRx);
		nt.setMobileTxBytes(mobileTx);
		nt.setWifiRxBytes(wifiRx);
		nt.setWifiTxBytes(wifiTx);
		
		/*
		// old SDK version:
		nt.setTotalRxBytes(TrafficStats.getTotalRxBytes());
		nt.setTotalTxBytes(TrafficStats.getTotalTxBytes());
		nt.setMobileRxBytes(TrafficStats.getMobileRxBytes());
		nt.setMobileTxBytes(TrafficStats.getMobileTxBytes());
		*/
	}

	private long getMobileRxBytes()
	{
		return getRxBytes(NetworkManager.MOBILE_INTERFACE);
	}

	private long getMobileTxBytes()
	{
		return getTxBytes(NetworkManager.MOBILE_INTERFACE);
	}

	private long getWifiRxBytes()
	{
		return getRxBytes(NetworkManager.WIFI_INTERFACE);
	}

	private long getWifiTxBytes()
	{
		return getTxBytes(NetworkManager.WIFI_INTERFACE);
	}

	private long getRxBytes(String iface)
	{
		ArrayList<String> out = Shell
				.executeBlocking("busybox ifconfig "
						+ iface
						+ " | grep \"RX bytes\" | cut -d \" \" -f 12 | cut -d \":\" -f2");
		// if output is not one line, something went wrong
		if (out.size() < 1)
			return 0;
		if (out.get(out.size() - 1).length() < 1)
			return 0;
		try
		{
			return Long.parseLong(out.get(out.size() - 1)); // always use last line
		}
		catch (Exception e)
		{
			return 0;
		}		
	}

	private long getTxBytes(String iface)
	{
		ArrayList<String> out = Shell
				.executeBlocking("busybox ifconfig "
						+ iface
						+ " | grep \"RX bytes\" | cut -d \" \" -f 17 | cut -d \":\" -f2");
		// if output is not one line, something went wrong
		if (out.size() < 1)
			return 0;
		if (out.get(out.size() - 1).length() < 1)
			return 0;
		try
		{
			return Long.parseLong(out.get(out.size() - 1)); // always use last line
		}
		catch (Exception e)
		{
			return 0;
		}
	}

}
