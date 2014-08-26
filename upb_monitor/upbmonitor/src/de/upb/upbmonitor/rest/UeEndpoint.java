package de.upb.upbmonitor.rest;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
import de.upb.upbmonitor.monitoring.model.UeContext;
import de.upb.upbmonitor.rest.RestAsyncRequest.RequestType;

public class UeEndpoint
{
	private static final String LTAG = "UeEndpoint";
	private String mUrl;

	public UeEndpoint(String host, int port)
	{
		this.mUrl = "http://" + host + ":" + port;
		Log.v(LTAG, "Created endpoint: " + this.mUrl);
	}

	public void register()
	{
		class UePostRequest extends RestAsyncRequest
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
				if (response.getStatusLine().getStatusCode() != 201)
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
					// set URI in model
					UeContext c = UeContext.getInstance();
					c.setURI(temp.get(0).toString());
					Log.i(LTAG, "Registered with URI: " + c.getURI());
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		;
		UePostRequest r = new UePostRequest();
		r.setup(RequestType.POST, this.mUrl + "/api/ue", UeContext
				.getInstance().toJson().toString());
		r.execute();
	}

	public void update()
	{
		class UeUpdateRequest extends RestAsyncRequest
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
				if (response.getStatusLine().getStatusCode() != 204)
				{
					Log.e(LTAG, "Bad Request: "
							+ response.getStatusLine().getStatusCode());
					return;
				}
				Log.i(LTAG, "UE was succesfully updatet in backend.");
			}
		}
		;
		UeUpdateRequest r = new UeUpdateRequest();
		r.setup(RequestType.PUT, this.mUrl
				+ UeContext.getInstance().getURI(), UeContext.getInstance()
				.toJson().toString());
		r.execute();
	}

	public void get()
	{

	}

	public void remove()
	{
		class UeDeleteRequest extends RestAsyncRequest
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
				if (response.getStatusLine().getStatusCode() != 204)
				{
					Log.e(LTAG, "Bad Request: "
							+ response.getStatusLine().getStatusCode());
					return;
				}
				// result code looks fine, reset model
				UeContext c = UeContext.getInstance();
				c.setURI(null);
				Log.i(LTAG, "UE was succesfully removed from backend.");
			}
		}
		;
		UeDeleteRequest r = new UeDeleteRequest();
		r.setup(RequestType.DELETE, this.mUrl
				+ UeContext.getInstance().getURI(), null);
		r.execute();
	}

}
