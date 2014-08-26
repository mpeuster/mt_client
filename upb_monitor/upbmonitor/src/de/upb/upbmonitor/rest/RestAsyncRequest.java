package de.upb.upbmonitor.rest;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class RestAsyncRequest extends AsyncTask<Void, Void, HttpResponse>
{
	private static final String LTAG = "RestAsyncRequest";

	public enum RequestType
	{
		POST, GET, PUT, DELETE
	}

	private RequestType mType = null;
	private String mUrl = null;
	private String mData = null;

	public void setup(RequestType t, String url, String data)
	{
		this.mType = t;
		this.mUrl = url;
		this.mData = data;
	}

	@Override
	protected HttpResponse doInBackground(Void... arg0)
	{
		if (this.mUrl == null)
		{
			Log.e(LTAG, "Error in RestAsyncRequest. Call setup first.");
			return null;
		}

		HttpUriRequest r;
		try
		{
			// build request
			switch (this.mType)
			{
			case POST:
				r = new HttpPost(this.mUrl);
				((HttpPost) r).setEntity(new StringEntity(this.mData));
				break;
			case PUT:
				r = new HttpPut(this.mUrl);
				((HttpPut) r).setEntity(new StringEntity(this.mData));
				break;
			case DELETE:
				r = new HttpDelete(this.mUrl);
				break;
			default: // use GET
				r = new HttpGet(this.mUrl);
				break;
			}
			// set header
			r.setHeader("Content-type", "application/json");
			HttpClient c = new DefaultHttpClient();

			Log.i(LTAG, "HttpRequestUrl:" + r.getURI().toString());
			HttpResponse response = c.execute(r);
			return response;
		} catch (ClientProtocolException e)
		{
			Log.e(LTAG, e.getMessage());
		} catch (IOException e)
		{
			Log.e(LTAG, e.getMessage());
		}
		return null;
	}
}
