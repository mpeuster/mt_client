package de.upb.upbmonitor;

import java.util.ArrayList;

import com.stericson.RootTools.RootTools;

import de.upb.upbmonitor.monitoring.MonitoringService;
import de.upb.upbmonitor.monitoring.model.UeContext;
import de.upb.upbmonitor.network.NetworkManager;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class ControlFragment extends Fragment
{
	private static final String LTAG = "ControlFragment";

	// singelton instance
	private static ControlFragment INSTANCE = null;

	private View rootView;
	private Switch switchMonitoringService;
	private Switch switchDualNetworking;
	private TextView textMobileStatus;
	private TextView textWifiStatus;
	private ImageView imageMobileStatus;
	private ImageView imageWifiStatus;

	private Handler mHandler = new Handler();
	private int updateTries = 0;
	private static final int MAX_RETRY = 10;

	/**
	 * Background task that checks the network status. If the current status
	 * fits to user inputs, its stops checking. Needed, because it is not always
	 * clear how long it takes until, dual networking mode is up and IPs are
	 * received.
	 */
	private Runnable updateTask = new Runnable()
	{
		public void run()
		{
			Log.i(LTAG, "Update task run: " + updateTries);
			// count number of tries
			updateTries++;
			// check for try count
			if (updateTries > MAX_RETRY)
			{
				Log.e(LTAG, "Update task has reached MAX_RETRY.");
				// enable switch (fallback)
				switchDualNetworking.setEnabled(true);
				// reset and stop runnable
				updateTries = 0;
				return;
			}

			// get network status
			NetworkManager nm = NetworkManager.getInstance();
			boolean mobile_state = nm.isMobileInterfaceEnabled();
			boolean wifi_state = nm.isWiFiInterfaceEnabled();
			String mobile_ip = nm.getMobileInterfaceIp();
			String wifi_ip = nm.getWiFiInterfaceIp();

			// if status is not equal try inputs, try to check again after some
			// time
			if (mobile_state != switchDualNetworking.isChecked()
					|| wifi_state != switchDualNetworking.isChecked())
			{
				mHandler.postDelayed(updateTask, 2000);
				return;
			} else
			{
				// is network state matches inputs, re-enable switch
				switchDualNetworking.setEnabled(true);
			}

			// if IP state does not match network state, try to check it again
			if (mobile_state == "0.0.0.0/0".equals(mobile_ip))
			{
				mHandler.postDelayed(updateTask, 1000);
				return;
			}

			// if IP state does not match network state, try to check it again
			if (wifi_state == "0.0.0.0/0".equals(wifi_ip))
			{
				mHandler.postDelayed(updateTask, 1000);
				return;
			}

			// update network status output
			updateNetworkStatus(mobile_state, mobile_ip, wifi_state, wifi_ip,
					nm.getCurrentSsid());

			// reset try counter
			updateTries = 0;
		}
	};

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static ControlFragment getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new ControlFragment();
		return INSTANCE;
	}

	public ControlFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		// get UI resources
		this.rootView = inflater.inflate(R.layout.fragment_control, container,
				false);
		this.switchMonitoringService = (Switch) rootView
				.findViewById(R.id.switch_monitoringservice);
		this.switchDualNetworking = (Switch) rootView
				.findViewById(R.id.switch_dualnetworking);
		this.imageMobileStatus = (ImageView) rootView
				.findViewById(R.id.imageViewMobileStatus);
		this.imageWifiStatus = (ImageView) rootView
				.findViewById(R.id.imageViewWifiStatus);
		this.textMobileStatus = (TextView) rootView
				.findViewById(R.id.textViewMobileStatus);
		this.textWifiStatus = (TextView) rootView
				.findViewById(R.id.textViewWifiStatus);

		// set switch state based on service state (if app is restarted)
		this.switchMonitoringService
				.setChecked(MonitoringService.SERVICE_EXISTS);

		// set switch state based on network state (if app is restarted)
		this.switchDualNetworking.setChecked(NetworkManager.getInstance()
				.isDualNetworkingEnabled());

		// monitoring switch listener
		this.switchMonitoringService
				.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked)
					{
						if (isChecked)
							startMonitoringService();
						else
							stopMonitoringService();
					}
				});

		// dual networking listener
		this.switchDualNetworking
				.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked)
					{
						if (isChecked)
							startDualNetworking();
						else
							stopDualNetworking();
					}
				});

		// check for root/busybox capabilities of device and disable
		// dual network switch if not available
		this.switchDualNetworking.setEnabled(this.checkRootAvailability()
				&& this.checkBusyBoxAvailability());

		// update network status
		mHandler.postDelayed(updateTask, 0);

		return rootView;
	}

	/**
	 * Starts monitoring service.
	 */
	public void startMonitoringService()
	{
		Intent i = new Intent(this.getActivity(), MonitoringService.class);
		this.getActivity().startService(i);
		// set switch state
		this.switchMonitoringService.setChecked(true);
		Log.i(LTAG, "Monitoring service turned on");
	}

	/**
	 * Stops monitoring service.
	 */
	public void stopMonitoringService()
	{
		Intent i = new Intent(this.getActivity(), MonitoringService.class);
		this.getActivity().stopService(i);
		// set switch state
		this.switchMonitoringService.setChecked(false);
		Log.i(LTAG, "Monitoring service turned off");
	}

	/**
	 * Starts dual networking if not already active.
	 */
	public void startDualNetworking()
	{
		// get NetworkManager instance
		NetworkManager nm = NetworkManager.getInstance();

		// if dual networking is already enabled: skip
		if (nm.isDualNetworkingEnabled())
			return;

		Toast.makeText(getActivity(), "Eanbleing dual network connectivity.",
				Toast.LENGTH_LONG).show();

		// disable switch
		this.switchDualNetworking.setEnabled(false);

		// trigger status test (after 5s)
		mHandler.postDelayed(updateTask, 5000);

		// get preferences for default WiFi
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		String default_ssid = preferences.getString("pref_wifi_default_ssid",
				null);
		String default_psk = preferences.getString("pref_wifi_default_psk",
				null);
		// special case: use no encryption
		if (default_psk.length() < 1 || default_psk.equals("none"))
			default_psk = null;

		// try to enable dual networking
		nm.enableDualNetworking(default_ssid, default_psk);
	}

	/**
	 * Stops dual networking if not already deactivated.
	 */
	public void stopDualNetworking()
	{
		// get NetworkManager instance
		NetworkManager nm = NetworkManager.getInstance();

		// if dual networking is already disabled:skip
		if (!nm.isDualNetworkingEnabled())
			return;

		Toast.makeText(getActivity(), "Disableing dual network connectivity.",
				Toast.LENGTH_LONG).show();

		// disable switch
		this.switchDualNetworking.setEnabled(false);

		// trigger status test (after 3s)
		mHandler.postDelayed(updateTask, 3000);

		// try to disable dual networking
		nm.disableDualNetworking();
	}

	/**
	 * checks if root access is possible and tries to get root access for this
	 * app.
	 * 
	 * @return true/false
	 */
	public boolean checkRootAvailability()
	{
		if (RootTools.isAccessGiven())
		{
			Log.i(LTAG, "Root access granted");
			return true;
		}
		Toast.makeText(getActivity(),
				"ERROR: Root access not possible on device!", Toast.LENGTH_LONG)
				.show();
		Log.e(LTAG, "Root access not possible");
		return false;
	}

	/**
	 * checks for busybox (command line tool) availability
	 * 
	 * @return true/false
	 */
	public boolean checkBusyBoxAvailability()
	{
		if (RootTools.isBusyboxAvailable())
		{
			Log.i(LTAG, "Busybox is available.");
			return true;
		}

		Toast.makeText(getActivity(),
				"ERROR: Busybox is not installed on device!", Toast.LENGTH_LONG)
				.show();
		Log.e(LTAG, "Busybox is NOT available");
		return false;
	}

	/**
	 * Updates the network status view elements.
	 * Texts and status colors of icons.
	 * @param mobile_status
	 * @param mobile_ip
	 * @param wifi_status
	 * @param wifi_ip
	 * @param ssid
	 */
	private void updateNetworkStatus(boolean mobile_status, String mobile_ip,
			boolean wifi_status, String wifi_ip, String ssid)
	{
		// set text views
		this.textMobileStatus.setText("Mobile: " + mobile_ip);
		this.textWifiStatus.setText("Wi-Fi: " + wifi_ip + " (" + ssid + ")");

		// tint image views
		if (mobile_status)
			this.imageMobileStatus.setColorFilter(Color.GREEN, Mode.MULTIPLY);
		else
			this.imageMobileStatus.setColorFilter(Color.GRAY, Mode.MULTIPLY);

		if (wifi_status)
			this.imageWifiStatus.setColorFilter(Color.GREEN, Mode.MULTIPLY);
		else
			this.imageWifiStatus.setColorFilter(Color.GRAY, Mode.MULTIPLY);
	}

}
