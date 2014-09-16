package de.upb.upbmonitor.service;


import de.upb.upbmonitor.commandline.CmdCallback;
import de.upb.upbmonitor.commandline.Shell;
import de.upb.upbmonitor.model.NetworkTraffic;
import de.upb.upbmonitor.network.NetworkManager;

import android.util.Log;

public class NetworkMonitor
{
	private static final String LTAG = "NetworkMonitor";
	private static int c = 0;

	public NetworkMonitor()
	{
	}

	public void monitor()
	{
		if((c % 5) == 0)
		{
			Log.v(LTAG, "Measuring network data ...");
			Shell.executeCustom(this.fetchMobileRxTraffic);
			Shell.executeCustom(this.fetchMobileTxTraffic);
			Shell.executeCustom(this.fetchWifiRxTraffic);
			Shell.executeCustom(this.fetchWifiTxTraffic);
		}
		c++;
	}

	private CmdCallback fetchMobileRxTraffic = new CmdCallback(
			"cat /sys/class/net/" + NetworkManager.MOBILE_INTERFACE
					+ "/statistics/rx_bytes")
	{
		@Override
		public void commandCompleted(int id, int exitCode)
		{
			if (exitCode == 0)
			{
				long result = 0;
				// parse result
				if (getOutput().size() < 1)
					result = 0;
				if (getOutput().get(getOutput().size() - 1).length() < 1)
					result = 0;
				try
				{
					result = Long.parseLong(getOutput().get(
							getOutput().size() - 1)); // always use last line
				} catch (Exception e)
				{
					Log.w(LTAG, "Parsing error Rx.");
					result = 0;
				}
				// store results in model
				NetworkTraffic.getInstance().setMobileRxBytes(result);
				Log.v(LTAG, "Stored Mobile RX: " + result);
			}
		}
	};
	
	private CmdCallback fetchMobileTxTraffic = new CmdCallback(
			"cat /sys/class/net/" + NetworkManager.MOBILE_INTERFACE
					+ "/statistics/tx_bytes")
	{
		@Override
		public void commandCompleted(int id, int exitCode)
		{
			if (exitCode == 0)
			{
				long result = 0;
				// parse result
				if (getOutput().size() < 1)
					result = 0;
				if (getOutput().get(getOutput().size() - 1).length() < 1)
					result = 0;
				try
				{
					result = Long.parseLong(getOutput().get(
							getOutput().size() - 1)); // always use last line
				} catch (Exception e)
				{
					Log.w(LTAG, "Parsing error TX.");
					result = 0;
				}
				// store results in model
				NetworkTraffic.getInstance().setMobileTxBytes(result);
				Log.v(LTAG, "Stored Mobile TX: " + result);
			}
		}
	};
	
	private CmdCallback fetchWifiRxTraffic = new CmdCallback(
			"cat /sys/class/net/" + NetworkManager.WIFI_INTERFACE
					+ "/statistics/rx_bytes")
	{
		@Override
		public void commandCompleted(int id, int exitCode)
		{
			if (exitCode == 0)
			{
				long result = 0;
				// parse result
				if (getOutput().size() < 1)
					result = 0;
				if (getOutput().get(getOutput().size() - 1).length() < 1)
					result = 0;
				try
				{
					result = Long.parseLong(getOutput().get(
							getOutput().size() - 1)); // always use last line
				} catch (Exception e)
				{
					Log.w(LTAG, "Parsing error Rx.");
					result = 0;
				}
				// store results in model
				NetworkTraffic.getInstance().setWifiRxBytes(result);
				Log.v(LTAG, "Stored Wifi RX: " + result);
			}
		}
	};
	
	private CmdCallback fetchWifiTxTraffic = new CmdCallback(
			"cat /sys/class/net/" + NetworkManager.WIFI_INTERFACE
					+ "/statistics/tx_bytes")
	{
		@Override
		public void commandCompleted(int id, int exitCode)
		{
			if (exitCode == 0)
			{
				long result = 0;
				// parse result
				if (getOutput().size() < 1)
					result = 0;
				if (getOutput().get(getOutput().size() - 1).length() < 1)
					result = 0;
				try
				{
					result = Long.parseLong(getOutput().get(
							getOutput().size() - 1)); // always use last line
				} catch (Exception e)
				{
					Log.w(LTAG, "Parsing error Tx.");
					result = 0;
				}
				// store results in model
				NetworkTraffic.getInstance().setWifiTxBytes(result);
				Log.v(LTAG, "Stored Wifi TX:" + result);
			}
		}
	};
}