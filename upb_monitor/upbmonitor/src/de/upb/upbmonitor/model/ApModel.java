package de.upb.upbmonitor.model;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ApModel
{
	private static final String LTAG = "ApModel";
	private static ApModel INSTANCE;
	private HashMap<String, JSONObject> mApList;
	
	/**
	 * Use as singleton class.
	 * 
	 * @return class instance
	 */
	public synchronized static ApModel getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new ApModel();
		return INSTANCE;
	}

	public ApModel()
	{
		this.mApList = new HashMap<String, JSONObject>();
	}
	
	/**
	 * Adds AP to model. If AP already exists,
	 * it will be overwritten.
	 */
	public synchronized void addAp(JSONObject ap)
	{
		String apUrl;
		try
		{
			apUrl = ap.getString("uri");
			this.mApList.put(apUrl, ap);
			Log.i(LTAG, "Added AP: " + apUrl);
		} catch (JSONException e)
		{
			Log.e(LTAG, "Error while adding AP to model.");
		}
	}
		
	/**
	 * Removes all APs from model.
	 */
	public synchronized void clear()
	{
		this.mApList.clear();
	}
	
	/**
	 * Get SSID for AP identified by its API URL.
	 * @param url
	 * @return
	 */
	public synchronized String getSsid(String url)
	{
		try
		{
			return this.mApList.get(url).getString("ssid");
		} catch (Exception e)
		{
			Log.e(LTAG, "Could not get AP value from model.");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get BSSID for AP identified by its API URL.
	 * @param url
	 * @return
	 */
	public synchronized String getBssid(String url)
	{
		try
		{
			return this.mApList.get(url).getString("bssid");
		} catch (Exception e)
		{
			Log.e(LTAG, "Could not get AP value from model.");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get PSK for AP identified by its API URL.
	 * @param url
	 * @return
	 */
	public synchronized String getPsk(String url)
	{
		try
		{
			return this.mApList.get(url).getString("psk");
		} catch (Exception e)
		{
			Log.e(LTAG, "Could not get AP value from model.");
			e.printStackTrace();
		}
		return null;
	}
	
	
}
