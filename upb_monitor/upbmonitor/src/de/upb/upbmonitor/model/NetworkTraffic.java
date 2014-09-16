package de.upb.upbmonitor.model;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class NetworkTraffic
{
	private static final String LTAG = "NetworkTrafficModel";

	public enum TType
	{
		TotalRx, TotalRxBackup, TotalTx, TotalTxBackup, MobileRx, MobileRxBackup, MobileTx, MobileTxBackup, WifiRx, WifiTx, WifiRxBackup, WifiTxBackup
	}

	private static NetworkTraffic INSTANCE;

	private Map<TType, Long> mBytes;
	private Map<TType, Long> mTimestamp;

	private synchronized void setBytes(TType t, long b)
	{
		// get backup TType
		TType tb = this.getBackupTType(t);

		if (tb != null)
		{
			// add data to model
			this.mBytes.put(tb, this.mBytes.get(t));
			this.mTimestamp.put(tb, this.mTimestamp.get(t));
			this.mBytes.put(t, b);
			this.mTimestamp.put(t, System.currentTimeMillis());
		}
	}

	public synchronized void setTotalRxBytes(long b)
	{
		this.setBytes(TType.TotalRx, b);
	}

	public synchronized void setTotalTxBytes(long b)
	{
		this.setBytes(TType.TotalTx, b);
	}

	public synchronized void setMobileRxBytes(long b)
	{
		this.setBytes(TType.MobileRx, b);
	}

	public synchronized void setMobileTxBytes(long b)
	{
		this.setBytes(TType.MobileTx, b);
	}
	
	public synchronized void setWifiRxBytes(long b)
	{
		this.setBytes(TType.WifiRx, b);
	}

	public synchronized void setWifiTxBytes(long b)
	{
		this.setBytes(TType.WifiTx, b);
	}

	private synchronized long getBytes(TType t)
	{
		return this.mBytes.get(t);
	}

	public synchronized long getTotalRxBytes()
	{
		return this.getBytes(TType.TotalRx);
	}

	public synchronized long getTotalTxBytes()
	{
		return this.getBytes(TType.TotalTx);
	}

	public synchronized long getMobileRxBytes()
	{
		return this.getBytes(TType.MobileRx);
	}

	public synchronized long getMobileTxBytes()
	{
		return this.getBytes(TType.MobileTx);
	}

	public synchronized long getWifiRxBytes()
	{
		return this.getBytes(TType.WifiRx);
	}

	public synchronized long getWifiTxBytes()
	{
		return this.getBytes(TType.WifiTx);
	}

	public synchronized float getBytesPerSecond(TType t)
	{
		TType tb = this.getBackupTType(t); // backup type
		// calculate byte_count and time_interval since last update
		float byte_count = this.mBytes.get(t) - this.mBytes.get(tb);
		float time_interval = this.mTimestamp.get(t) - this.mTimestamp.get(tb);
		// calculate byte/s over time_intervall
		if (time_interval < 50) // avoid to small measurements (< 50ms)
			return 0.0F;
		// TODO Next: Make calculation interval dependent on last get()
		float bps = byte_count / (time_interval / 1000); // = byte/s
		
		// check result
		if(bps < 0)
			bps = 0.0F;
		if(bps > 10000000)
			bps = 10000000;		
		return bps;
	}
	
	public synchronized float getTotalRxBytesPerSecond()
	{
		return this.getBytesPerSecond(TType.TotalRx);
	}

	public synchronized float getTotalTxBytesPerSecond()
	{
		return this.getBytesPerSecond(TType.TotalTx);
	}

	public synchronized float getMobileRxBytesPerSecond()
	{
		return this.getBytesPerSecond(TType.MobileRx);
	}

	public synchronized float getMobileTxBytesPerSecond()
	{
		return this.getBytesPerSecond(TType.MobileTx);
	}

	public synchronized float getWifiRxBytesPerSecond()
	{
		return this.getBytesPerSecond(TType.WifiRx);
	}

	public synchronized float getWifiTxBytesPerSecond()
	{
		return this.getBytesPerSecond(TType.WifiTx);
	}
	

	public synchronized static NetworkTraffic getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new NetworkTraffic();
		return INSTANCE;
	}

	public NetworkTraffic()
	{
		// initializations
		this.mBytes = new HashMap<TType, Long>();
		this.mBytes.put(TType.TotalRx, 0L);
		this.mBytes.put(TType.TotalTx, 0L);
		this.mBytes.put(TType.MobileRx, 0L);
		this.mBytes.put(TType.MobileTx, 0L);
		this.mBytes.put(TType.WifiRx, 0L);
		this.mBytes.put(TType.WifiTx, 0L);
		this.mBytes.put(TType.TotalRxBackup, 0L);
		this.mBytes.put(TType.TotalTxBackup, 0L);
		this.mBytes.put(TType.MobileRxBackup, 0L);
		this.mBytes.put(TType.MobileTxBackup, 0L);
		this.mBytes.put(TType.WifiRxBackup, 0L);
		this.mBytes.put(TType.WifiTxBackup, 0L);

		this.mTimestamp = new HashMap<TType, Long>();
		this.mTimestamp.put(TType.TotalRx, System.currentTimeMillis());
		this.mTimestamp.put(TType.TotalTx, System.currentTimeMillis());
		this.mTimestamp.put(TType.MobileRx, System.currentTimeMillis());
		this.mTimestamp.put(TType.MobileTx, System.currentTimeMillis());
		this.mTimestamp.put(TType.WifiRx, System.currentTimeMillis());
		this.mTimestamp.put(TType.WifiTx, System.currentTimeMillis());
		this.mTimestamp.put(TType.TotalRxBackup, System.currentTimeMillis());
		this.mTimestamp.put(TType.TotalTxBackup, System.currentTimeMillis());
		this.mTimestamp.put(TType.MobileRxBackup, System.currentTimeMillis());
		this.mTimestamp.put(TType.MobileTxBackup, System.currentTimeMillis());
		this.mTimestamp.put(TType.WifiRxBackup, System.currentTimeMillis());
		this.mTimestamp.put(TType.WifiTxBackup, System.currentTimeMillis());
	}

	public synchronized boolean hasChanged()
	{
		return true;
	}

	public void resetDataChangedFlag()
	{
		// TODO Next: implement threshold based change flag for network model
	}

	private TType getBackupTType(TType t)
	{
		// select backup TType
		switch (t)
		{
		case TotalRx:
			return TType.TotalRxBackup;
		case TotalTx:
			return TType.TotalTxBackup;
		case MobileRx:
			return TType.MobileRxBackup;
		case MobileTx:
			return TType.MobileTxBackup;
		case WifiRx:
			return TType.WifiRxBackup;
		case WifiTx:
			return TType.WifiTxBackup;
		default:
			Log.e(LTAG, "Bad TType.");
		}
		return null;
	}

}
