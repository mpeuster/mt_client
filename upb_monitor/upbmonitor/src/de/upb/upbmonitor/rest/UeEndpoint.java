package de.upb.upbmonitor.rest;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.upb.upbmonitor.model.UeContext;
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
		// perform request
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
				Log.d(LTAG, "PUT: " + UeContext.getInstance().getURI());
			}
		}
		;
		// check if we are already registered in backend? Otherwise skip this request.
		if(UeContext.getInstance().getURI() == null)
		{
			Log.w(LTAG, "Skipping PUT request, because UE is not yet registered in backend");
			return;
		}
		// perform request
		UeUpdateRequest r = new UeUpdateRequest();
		r.setup(RequestType.PUT, this.mUrl
				+ UeContext.getInstance().getURI(), UeContext.getInstance()
				.toJson().toString());
		r.execute();
	}

	public void get()
	{
		class UeGetRequest extends RestAsyncRequest
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
					// store received context to model
					UeContext.getInstance().setBackendContext(temp);

				} catch (Exception e)
				{
					e.printStackTrace();
				}
				Log.d(LTAG, "GET: " + UeContext.getInstance().getURI());
			}
		}
		;
		// check if we are already registered in backend? Otherwise skip this request.
		if(UeContext.getInstance().getURI() == null)
		{
			Log.w(LTAG, "Skipping GET request, because UE is not yet registered in backend");
			return;
		}
		// perform request
		UeGetRequest r = new UeGetRequest();
		r.setup(RequestType.GET, this.mUrl
				+ UeContext.getInstance().getURI(), null);
		r.execute();
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
		// check if we are already registered in backend? Otherwise skip this request.
		if(UeContext.getInstance().getURI() == null)
		{
			Log.w(LTAG, "Skipping DELETE request, because UE is not yet registered in backend");
			return;
		}
		// perform request
		UeDeleteRequest r = new UeDeleteRequest();
		r.setup(RequestType.DELETE, this.mUrl
				+ UeContext.getInstance().getURI(), null);
		r.execute();
	}

}
