package de.upb.upbmonitor;

import java.util.LinkedHashMap;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.upb.upbmonitor.monitoring.MonitoringService;
import de.upb.upbmonitor.monitoring.model.UeContext;
import android.widget.ListView;
import android.widget.TextView;

public class MonitoringFragment extends Fragment
{
	private static final String LTAG = "MonitoringFragment";

	private ListView listView;
	private TextView textMonitoringServiceStatus;
	MonitoringListAdapter adapter = null;

	private Handler mHandler = new Handler();

	private Runnable periodicTask = new Runnable()
	{
		public void run()
		{
			// update headline
			textMonitoringServiceStatus
					.setText(MonitoringService.SERVICE_EXISTS ? "Current Device State:"
							: "Turn monitoring service on in order to see monitoring outputs.");
						
			// update list view
			if (adapter != null)
			{
				if(MonitoringService.SERVICE_EXISTS)
					adapter.updateData(UeContext.getInstance().toListViewData());
				else // if service is not active, show empty list
					adapter.updateData(null);
			}
			mHandler.postDelayed(periodicTask, 1000);
		}
	};

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static MonitoringFragment newInstance()
	{
		MonitoringFragment fragment = new MonitoringFragment();
		return fragment;
	}

	public MonitoringFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_monitoring,
				container, false);

		listView = (ListView) rootView.findViewById(R.id.listViewMonitoring);
		textMonitoringServiceStatus = (TextView) rootView
				.findViewById(R.id.textViewMonitoringStatus);

		// create custom adapter
		adapter = new MonitoringListAdapter(getActivity(), UeContext
				.getInstance().toListViewData());
		// assign adapter to ListView
		listView.setAdapter(adapter);

		return rootView;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		// start updater handler (periodically refresh list view)
		mHandler.postDelayed(periodicTask, 1000);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		// stop update handler
		mHandler.removeCallbacks(periodicTask);
	}

}
