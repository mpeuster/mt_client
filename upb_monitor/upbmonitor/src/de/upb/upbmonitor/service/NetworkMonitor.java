package de.upb.upbmonitor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.upb.upbmonitor.commandline.CmdCallback;
import de.upb.upbmonitor.commandline.Shell;
import de.upb.upbmonitor.model.NetworkTraffic;
import de.upb.upbmonitor.model.UeContext;
import de.upb.upbmonitor.network.NetworkManager;
import android.util.Log;

public class NetworkMonitor
{
	private static final String LTAG = "NetworkMonitor";

	public NetworkMonitor()
	{
	}

	public void monitor()
	{
		long m_rx = getByteCount(NetworkManager.MOBILE_INTERFACE, "rx");
		long m_tx = getByteCount(NetworkManager.MOBILE_INTERFACE, "tx");
		long w_rx = getByteCount(NetworkManager.WIFI_INTERFACE, "rx");
		long w_tx = getByteCount(NetworkManager.WIFI_INTERFACE, "tx");
		
		NetworkTraffic nt = NetworkTraffic.getInstance();
		nt.setMobileRxBytes(m_rx);
		nt.setMobileTxBytes(m_tx);
		nt.setWifiRxBytes(w_rx);
		nt.setWifiTxBytes(w_tx);
		nt.setTotalRxBytes(m_rx + w_rx);
		nt.setTotalTxBytes(m_tx + w_tx);

		/*
		 * Log.i(LTAG,"Mobile RX: " + nt.getMobileRxBytes());
		 * Log.i(LTAG,"Mobile TX: " + nt.getMobileTxBytes());
		 * Log.i(LTAG,"Wifi RX: " + nt.getWifiRxBytes()); Log.i(LTAG,"Wifi TX: "
		 * + nt.getWifiTxBytes());
		 */
	}

	private long getByteCount(String iface, String direction)
	{
		String path = "/sys/class/net/" + iface + "/statistics/" + direction
				+ "_bytes";
		long result = 0;

		try
		{
			result = Long.parseLong(this.getSysfilecontent(path));
		} catch (Exception e)
		{
			// e.printStackTrace();
			Log.w(LTAG, "Parsing error: " + this.getSysfilecontent(path));
			result = 0;
		}
		return result;
	}

	private String getSysfilecontent(String path)
	{
		String result = "";
		try
		{
			File file = new File(path);
			InputStream in = new FileInputStream(file);
			byte[] re = new byte[32768];
			int read = 0;
			while ((read = in.read(re, 0, 32768)) != -1)
			{
				result += new String(re, 0, read);
			}
			in.close();
		} catch (IOException e)
		{
			// e.printStackTrace();
			Log.e(LTAG, "Error while reading file: " + path);
		}
		return result.trim();
	}

}