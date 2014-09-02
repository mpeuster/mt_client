package de.upb.upbmonitor.rest;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import de.upb.upbmonitor.model.ApModel;
import de.upb.upbmonitor.model.UeContext;
import de.upb.upbmonitor.rest.RestAsyncRequest.RequestType;
import android.util.Log;

public class ApEndpoint
{
	private static final String LTAG = "ApEndpoint";
	private String mUrl;

	public ApEndpoint(String host, int port)
	{
		this.mUrl = "http://" + host + ":" + port;
		Log.i(LTAG, "Created endpoint: " + this.mUrl);
	}
	
	
	/**
	 * Fetches data of all access points registered in backend.
	 * All AP definitions are then stored in the model.
	 * 1. get list of AP URLs 
	 * 2. get data of each AP
	 */
	public void fetchApData()
	{
		class ApListRequest extends RestAsyncRequest
		{
			@Override
			protected void onPostExecute(HttpResponse response)
			{
				// error handling
				if (response == null)
				{
					Log.e(LTAG, "Request error.");
					return;
				}
				if (response.getStatusLine().getStatusCode() != 200)
				{
					Log.e(LTAG, "Bad Request: "
							+ response.getStatusLine().getStatusCode());
					return;
				}
				// result code looks fine, process it:
				try
				{
					// parse json data
					String json_string;
					json_string = EntityUtils.toString(response.getEntity());
					JSONArray temp = new JSONArray(json_string);
					// do get request for each AP in list:
					for(int i = 0; i < temp.length(); i++)
					{
						ApEndpoint.this.get(temp.getString(i));
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				Log.d(LTAG, "GET: " + "/api/accesspoint");
			}
		}
		;
		// perform request
		ApListRequest r = new ApListRequest();
		r.setup(RequestType.GET, this.mUrl
				+ "/api/accesspoint", null);
		r.execute();
	}
	
	private void get(final String apUrl)
	{
		class ApGetRequest extends RestAsyncRequest
		{
			@Override
			protected void onPostExecute(HttpResponse response)
			{
				// error handling
				if (response == null)
				{
					Log.e(LTAG, "Request error.");
					return;
				}
				if (response.getStatusLine().getStatusCode() != 200)
				{
					Log.e(LTAG, "Bad Request: "
							+ response.getStatusLine().getStatusCode());
					return;
				}
				// result code looks fine, process it:
				try
				{
					// parse json data
					String json_string;
					json_string = EntityUtils.toString(response.getEntity());
					JSONObject temp = new JSONObject(json_string);
					// store received AP in model
					ApModel.getInstance().addAp(temp);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				Log.d(LTAG, "GET: " + apUrl);
			}
		}
		;
		// perform request
		ApGetRequest r = new ApGetRequest();
		r.setup(RequestType.GET, this.mUrl
				+ apUrl, null);
		r.execute();
	}
}
