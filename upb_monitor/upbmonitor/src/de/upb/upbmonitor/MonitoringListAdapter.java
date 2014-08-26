package de.upb.upbmonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MonitoringListAdapter extends BaseAdapter
{

	private Activity activity;
	private LinkedHashMap<String, String> data;
	private static LayoutInflater inflater = null;

	public MonitoringListAdapter(Activity a, LinkedHashMap<String, String> d)
	{
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void updateData(LinkedHashMap<String, String> d)
	{
		data.clear();
		if(d != null)
		{
			for (String key : d.keySet())
				data.put(key, d.get(key));
		}
		this.notifyDataSetChanged();
	}

	public int getCount()
	{
		return data.size();
	}

	public Object getItem(int position)
	{
		return position;
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_item_monitoring, null);

		TextView t1 = (TextView) vi.findViewById(R.id.textViewListItemName);
		TextView t2 = (TextView) vi.findViewById(R.id.textViewListItemValue);

		// get key (name) for this position
		String name = data.keySet().toArray()[position].toString();
		// display values in row
		t1.setText(name);
		t2.setText(data.get(name));
		return vi;
	}
}