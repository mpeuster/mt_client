package de.upb.upbmonitor.monitoring.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class UeContext
{
	/**
	 * Tread UE context as singelton class
	 */

	private static final String LTAG = "UeContext";
	private Context myContext = null;
	private static UeContext INSTANCE;
	private boolean CONTEXT_CHANGED;

	/**
	 * Context is needed to access shared preferences. It is set by the monitoring and/or sender thread.
	 * @param c
	 */
	public synchronized void updateApplicationContext(Context c)
	{
		this.myContext = c;
	}

	private int mUpdateCount;

	public synchronized int getUpdateCount()
	{
		return mUpdateCount;
	}

	public synchronized void incrementUpdateCount()
	{
		this.mUpdateCount++;
	}

	//private String mURI = null;

	public synchronized String getURI()
	{
		if(this.myContext == null)
			return null;
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this.myContext);
		return p.getString("pref_current_URI", null);
		//return mURI;
	}

	public synchronized void setURI(String mURI)
	{
		// get preference manager
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this.myContext);
		SharedPreferences.Editor e = p.edit();
		e.putString("pref_current_URI", mURI);
		e.commit();
		
		// no change indicator here, since it is only set once
		// and the network controller knows this value
		//this.mURI = mURI;
	}

	private String mDeviceID;

	public synchronized String getDeviceID()
	{
		return mDeviceID;
	}

	public synchronized void setDeviceID(String mDeviceID)
	{
		if (!mDeviceID.equals(this.mDeviceID))
			this.setDataChangedFlag();
		this.mDeviceID = mDeviceID;
	}

	private String mLocationServiceID;

	public synchronized String getLocationServiceID()
	{
		return mLocationServiceID;
	}

	public synchronized void setLocationServiceID(String mLocationServiceID)
	{
		if (!mLocationServiceID.equals(this.mLocationServiceID))
			this.setDataChangedFlag();
		this.mLocationServiceID = mLocationServiceID;
	}

	private String mWifiMac;

	public synchronized String getWifiMac()
	{
		return mWifiMac;
	}

	public synchronized void setWifiMac(String mWifiMac)
	{
		if (!mWifiMac.equals(this.mWifiMac))
			this.setDataChangedFlag();
		this.mWifiMac = mWifiMac;
	}

	private float mPositionX;

	public synchronized float getPositionX()
	{
		return mPositionX;
	}

	public synchronized void setPositionX(float mPositionX)
	{
		if (mPositionX != this.mPositionX)
			this.setDataChangedFlag();
		this.mPositionX = mPositionX;
	}

	private float mPositionY;

	public synchronized float getPositionY()
	{
		return mPositionY;
	}

	public synchronized void setPositionY(float mPositionY)
	{
		if (mPositionY != this.mPositionY)
			this.setDataChangedFlag();
		this.mPositionY = mPositionY;
	}

	private boolean mDisplayState;

	public synchronized boolean isDisplayOn()
	{
		return mDisplayState;
	}

	public synchronized void setDisplayState(boolean mDisplayState)
	{
		if (mDisplayState != this.mDisplayState)
			this.setDataChangedFlag();
		this.mDisplayState = mDisplayState;
	}

	private String mActiveApplicationPackage;

	public synchronized String getActiveApplicationPackage()
	{
		return mActiveApplicationPackage;
	}

	public synchronized void setActiveApplicationPackage(
			String mActiveApplicationPackage)
	{
		if (!mActiveApplicationPackage.equals(this.mActiveApplicationPackage))
			this.setDataChangedFlag();
		this.mActiveApplicationPackage = mActiveApplicationPackage;
	}

	private String mActiveApplicationActivity;

	public synchronized String getActiveApplicationActivity()
	{
		return mActiveApplicationActivity;
	}

	public synchronized void setActiveApplicationActivity(
			String mActiveApplicationActivity)
	{
		if (!mActiveApplicationActivity.equals(this.mActiveApplicationActivity))
			this.setDataChangedFlag();
		this.mActiveApplicationActivity = mActiveApplicationActivity;
	}

	public synchronized long getTotalRxBytes()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getTotalRxBytes();
	}

	public synchronized long getTotalTxBytes()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getTotalTxBytes();
	}

	public synchronized long getMobileRxBytes()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getMobileRxBytes();
	}

	public synchronized long getMobileTxBytes()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getMobileTxBytes();
	}

	public synchronized long getWifiRxBytes()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getWifiRxBytes();
	}

	public synchronized long getWifiTxBytes()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getWifiTxBytes();
	}

	public synchronized float getTotalRxBytesPerSecond()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getTotalRxBytesPerSecond();
	}

	public synchronized float getTotalTxBytesPerSecond()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getTotalTxBytesPerSecond();
	}

	public synchronized float getMobileRxBytesPerSecond()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getMobileRxBytesPerSecond();
	}

	public synchronized float getMobileTxBytesPerSecond()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getMobileTxBytesPerSecond();
	}

	public synchronized float getWifiRxBytesPerSecond()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getWifiRxBytesPerSecond();
	}

	public synchronized float getWifiTxBytesPerSecond()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return nt.getWifiTxBytesPerSecond();
	}

	/**
	 * Use as singleton class.
	 * 
	 * @return class instance
	 */
	public synchronized static UeContext getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new UeContext();
		return INSTANCE;
	}

	public UeContext()
	{
		// value initialization
		this.CONTEXT_CHANGED = false;
		this.mUpdateCount = 0;
		this.mDisplayState = false;
		this.mActiveApplicationPackage = null;
		this.mActiveApplicationActivity = null;
	}

	public void resetDataChangedFlag()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		nt.resetDataChangedFlag();
		this.CONTEXT_CHANGED = false;
	}

	public void setDataChangedFlag()
	{
		this.CONTEXT_CHANGED = true;
	}

	public boolean hasChanged()
	{
		NetworkTraffic nt = NetworkTraffic.getInstance();
		return this.CONTEXT_CHANGED || nt.hasChanged();
	}

	public String toString()
	{
		String res = "Context:\n";
		res = res.concat("-----\n");
		res = res.concat("UpdateCount: \t" + getUpdateCount() + "\n");
		res = res.concat("Device ID: \t" + getDeviceID() + "\n");
		res = res.concat("Location Service ID: \t" + getLocationServiceID()
				+ "\n");
		res = res.concat("Position X/Y: \t" + getPositionX() + "/"
				+ getPositionY() + "\n");
		res = res.concat("Display state: \t" + isDisplayOn() + "\n");
		res = res.concat("Active package: \t" + getActiveApplicationPackage()
				+ "\n");
		res = res.concat("Active activity: \t" + getActiveApplicationActivity()
				+ "\n");
		res = res.concat("Wifi MAC: \t" + getWifiMac() + "\n");
		res = res.concat("Mobile Traffic:\tRx:" + getMobileRxBytes() + "\tTx:"
				+ getMobileTxBytes() + "\tRx/s:" + getMobileRxBytesPerSecond()
				+ " \tTx/s:" + getMobileTxBytesPerSecond() + "\n");
		res = res.concat("Wifi   Traffic:\tRx:" + getWifiRxBytes() + "\tTx:"
				+ getWifiTxBytes() + "\tRx/s:" + getWifiRxBytesPerSecond()
				+ " \tTx/s:" + getWifiTxBytesPerSecond() + "\n");
		res = res.concat("Total  Traffic:\tRx:" + getTotalRxBytes() + "\tTx:"
				+ getTotalTxBytes() + "\tRx/s:" + getTotalRxBytesPerSecond()
				+ " \tTx/s:" + getTotalTxBytesPerSecond() + "\n");
		res = res.concat("-----\n");
		return res;
	}

	/**
	 * JSON Tag names
	 */
	private static final String JSON_URI = "backend_uri";
	private static final String JSON_DEVICE_ID = "device_id";
	private static final String JSON_LOCATIONSERVICE_ID = "location_service_id";
	private static final String JSON_POSITION_X = "position_x";
	private static final String JSON_POSITION_Y = "position_y";
	private static final String JSON_DISPLAY_STATE = "display_state";
	private static final String JSON_ACTIVE_APPLICATION_PACKAGE = "active_application_package";
	private static final String JSON_ACTIVE_APPLICATION_ACTIVITY = "active_application_activity";
	private static final String JSON_WIFI_MAC = "wifi_mac";
	private static final String JSON_TOTAL_RX = "rx_total_bytes";
	private static final String JSON_TOTAL_TX = "tx_total_bytes";
	private static final String JSON_MOBILE_RX = "rx_mobile_bytes";
	private static final String JSON_MOBILE_TX = "tx_mobile_bytes";
	private static final String JSON_WIFI_RX = "rx_wifi_bytes";
	private static final String JSON_WIFI_TX = "tx_wifi_bytes";
	private static final String JSON_TOTAL_RX_S = "rx_total_bytes_per_second";
	private static final String JSON_TOTAL_TX_S = "tx_total_bytes_per_second";
	private static final String JSON_MOBILE_RX_S = "rx_mobile_bytes_per_second";
	private static final String JSON_MOBILE_TX_S = "tx_mobile_bytes_per_second";
	private static final String JSON_WIFI_RX_S = "rx_wifi_bytes_per_second";
	private static final String JSON_WIFI_TX_S = "tx_wifi_bytes_per_second";

	/**
	 * Generate JSON from context model object.
	 * 
	 * @return JSONObject or null
	 */
	public JSONObject toJson()
	{
		JSONObject object = new JSONObject();
		try
		{
			// general
			object.put(JSON_DEVICE_ID, this.getDeviceID());
			object.put(JSON_LOCATIONSERVICE_ID, this.getLocationServiceID());
			object.put(JSON_POSITION_X, Float.valueOf(this.getPositionX()));
			object.put(JSON_POSITION_Y, Float.valueOf(this.getPositionY()));
			// system state
			object.put(
					JSON_DISPLAY_STATE,
					this.isDisplayOn() ? Integer.valueOf(1) : Integer
							.valueOf(0));
			object.put(JSON_ACTIVE_APPLICATION_PACKAGE,
					this.getActiveApplicationPackage());
			object.put(JSON_ACTIVE_APPLICATION_ACTIVITY,
					this.getActiveApplicationActivity());
			// network: bytes
			object.put(JSON_TOTAL_RX, this.getTotalRxBytes());
			object.put(JSON_TOTAL_TX, this.getTotalTxBytes());
			object.put(JSON_MOBILE_RX, this.getMobileRxBytes());
			object.put(JSON_MOBILE_TX, this.getMobileTxBytes());
			object.put(JSON_WIFI_RX, this.getWifiRxBytes());
			object.put(JSON_WIFI_TX, this.getWifiTxBytes());
			// network: bytes per second
			object.put(JSON_TOTAL_RX_S, this.getTotalRxBytesPerSecond());
			object.put(JSON_TOTAL_TX_S, this.getTotalTxBytesPerSecond());
			object.put(JSON_MOBILE_RX_S, this.getMobileRxBytesPerSecond());
			object.put(JSON_MOBILE_TX_S, this.getMobileTxBytesPerSecond());
			object.put(JSON_WIFI_RX_S, this.getWifiRxBytesPerSecond());
			object.put(JSON_WIFI_TX_S, this.getWifiTxBytesPerSecond());
			// network: properties
			object.put(JSON_WIFI_MAC, this.getWifiMac());

			return object;
		} catch (JSONException e)
		{
			Log.e(LTAG, e.getMessage());
		}
		return null;

	}

	/**
	 * Return all values as a LinkedHashMap <KeyString, Value:String> Used to
	 * display the data inside the App.
	 * 
	 * @return LinkedHashMap<String, String>
	 */
	public LinkedHashMap<String, String> toListViewData()
	{
		LinkedHashMap<String, String> r = new LinkedHashMap<String, String>();

		// additional (internal, not for public API)
		r.put(JSON_URI, this.getURI());
		// public API values
		r.put(JSON_DEVICE_ID, this.getDeviceID());
		r.put(JSON_LOCATIONSERVICE_ID, this.getLocationServiceID());
		r.put(JSON_POSITION_X, Float.toString(this.getPositionX()));
		r.put(JSON_POSITION_Y, Float.toString(this.getPositionY()));
		// system state
		r.put(JSON_DISPLAY_STATE, Boolean.toString(this.isDisplayOn()));
		r.put(JSON_ACTIVE_APPLICATION_PACKAGE,
				this.getActiveApplicationPackage());
		r.put(JSON_ACTIVE_APPLICATION_ACTIVITY,
				this.getActiveApplicationActivity());
		// network: bytes
		r.put(JSON_TOTAL_RX, Long.toString(this.getTotalRxBytes()));
		r.put(JSON_TOTAL_TX, Long.toString(this.getTotalTxBytes()));
		r.put(JSON_MOBILE_RX, Long.toString(this.getMobileRxBytes()));
		r.put(JSON_MOBILE_TX, Long.toString(this.getMobileTxBytes()));
		r.put(JSON_WIFI_RX, Long.toString(this.getWifiRxBytes()));
		r.put(JSON_WIFI_TX, Long.toString(this.getWifiTxBytes()));
		// network: bytes per second
		r.put(JSON_TOTAL_RX_S, Float.toString(this.getTotalRxBytesPerSecond()));
		r.put(JSON_TOTAL_TX_S, Float.toString(this.getTotalTxBytesPerSecond()));
		r.put(JSON_MOBILE_RX_S,
				Float.toString(this.getMobileRxBytesPerSecond()));
		r.put(JSON_MOBILE_TX_S,
				Float.toString(this.getMobileTxBytesPerSecond()));
		r.put(JSON_WIFI_RX_S, Float.toString(this.getWifiRxBytesPerSecond()));
		r.put(JSON_WIFI_TX_S, Float.toString(this.getWifiTxBytesPerSecond()));
		// network: properties
		r.put(JSON_WIFI_MAC, this.getWifiMac());

		return r;
	}
}
