package de.upb.upbmonitor;

import java.util.ArrayList;

import com.stericson.RootTools.RootTools;

import de.upb.upbmonitor.commandline.Shell;
import de.upb.upbmonitor.model.UeContext;
import de.upb.upbmonitor.network.NetworkManager;
import de.upb.upbmonitor.service.ManagementService;
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
import android.widget.CheckBox;
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
	private TextView textMPTCPStatus;
	private CheckBox checkMPTCPEnabled;
	private ImageView imageMobileStatus;
	private ImageView imageWifiStatus;

	private boolean mMptcpEnabled;

	private Handler mHandler = new Handler();
	// enable only after dual network switch even, initial: bigger than
	// max_retry!
	private int updateTries = MAX_RETRY_BEFORE_SWITCH_RESET + 1;
	private static final int MAX_RETRY_BEFORE_SWITCH_RESET = 10;

	/**
	 * Background task that checks the network status. Needed, because it is not
	 * always clear how long it takes until, dual networking mode is up and IPs
	 * are received.
	 */
	private Runnable periodicGuiUpdateTask = new Runnable()
	{
		public void run()
		{
			// count number of tries
			updateTries++;
			// check for try count
			if (updateTries == MAX_RETRY_BEFORE_SWITCH_RESET)
			{
				// after a certain number of updates, we re-enable the switch.
				// this is a fallback ensuring that the user can switch of the
				// dual network mode again
				Log.w(LTAG,
						"Update task has reached MAX_RETRY_BEFORE_SWITCH_RESET.");
				// enable switch (fallback)
				switchDualNetworking.setEnabled(true);
			}

			if (isVisible())
			{
				// get network status
				NetworkManager nm = NetworkManager.getInstance();
				boolean mobile_state = nm.isMobileInterfaceEnabled();
				boolean wifi_state = nm.isWiFiInterfaceEnabled();
				String mobile_ip = nm.getMobileInterfaceIp();
				String wifi_ip = nm.getWiFiInterfaceIp();

				// update network status output
				updateNetworkStatus(mobile_state, mobile_ip, wifi_state,
						wifi_ip, nm.getCurrentSsid());

				// if status is not equal try inputs, try to check again after
				// some
				// time
				if (mobile_state == switchDualNetworking.isChecked()
						&& wifi_state == switchDualNetworking.isChecked())
				{
					// if network state matches inputs, re-enable switch
					switchDualNetworking.setEnabled(true);
					updateTries = MAX_RETRY_BEFORE_SWITCH_RESET + 1;
				}
			}

			// toggle next update
			mHandler.postDelayed(periodicGuiUpdateTask, 2000);
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
		this.textMPTCPStatus = (TextView) rootView
				.findViewById(R.id.textViewMPTCP);
		this.checkMPTCPEnabled = (CheckBox) rootView
				.findViewById(R.id.checkBoxMPTCP);

		// check MPTCP availability
		this.mMptcpEnabled = this.checkMptcpEnabled();

		// set switch state based on service state (if app is restarted)
		this.switchMonitoringService
				.setChecked(ManagementService.SERVICE_EXISTS);

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
						// update MPTCP CheckBox
						checkMPTCPEnabled.setEnabled(mMptcpEnabled
								&& !isChecked);
						textMPTCPStatus.setEnabled(mMptcpEnabled && !isChecked);
						// perform network change
						if (isChecked)
							startDualNetworking();
						else
							stopDualNetworking();
					}
				});

		// update and set MPTCP checkbox
		this.textMPTCPStatus
				.setText(this.mMptcpEnabled ? "(MPTCP: installed)"
						: "(MPTCP: not installed)");
		// update MPTCP CheckBox
		checkMPTCPEnabled.setEnabled(mMptcpEnabled
				&& !switchDualNetworking.isChecked());
		textMPTCPStatus.setEnabled(mMptcpEnabled
				&& !switchDualNetworking.isChecked());

		// MPTCP CheckBox listener
		this.checkMPTCPEnabled
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked)
					{
						Log.w(LTAG, "MPTCP ChackBox: " + isChecked);
					}
				});

		// check for root/busybox capabilities of device and disable
		// dual network switch if not available
		this.switchDualNetworking.setEnabled(this.checkRootAvailability()
				&& this.checkBusyBoxAvailability());

		// kick off periodic update task
		mHandler.removeCallbacks(periodicGuiUpdateTask); // remove old one if
															// existing
		mHandler.postDelayed(periodicGuiUpdateTask, 0);

		return rootView;
	}

	/**
	 * Starts monitoring service.
	 */
	public void startMonitoringService()
	{
		if (!ManagementService.SERVICE_EXISTS)
		{
			// force reset of model
			UeContext.getInstance().setApplicationContext(getActivity());
			UeContext.getInstance().setURI(null);
			;
			UeContext.getInstance().setAssignedApURI("none");
		}
		// start service
		Intent i = new Intent(this.getActivity(), ManagementService.class);
		this.getActivity().startService(i);
		// set switch state
		this.switchMonitoringService.setChecked(true);
	}

	/**
	 * Stops monitoring service.
	 */
	public void stopMonitoringService()
	{
		Intent i = new Intent(this.getActivity(), ManagementService.class);
		this.getActivity().stopService(i);
		// set switch state
		this.switchMonitoringService.setChecked(false);
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
				Toast.LENGTH_SHORT).show();

		// disable switch
		this.switchDualNetworking.setEnabled(false);

		// ensure switch state
		this.switchDualNetworking.setChecked(true);

		// reset update try counter
		updateTries = 0;

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
				Toast.LENGTH_SHORT).show();

		// disable switch
		this.switchDualNetworking.setEnabled(false);

		// reset update try counter
		updateTries = 0;

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
				"ERROR: Root access not possible on device!",
				Toast.LENGTH_SHORT).show();
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
			Log.d(LTAG, "Busybox is available.");
			return true;
		}

		Toast.makeText(getActivity(),
				"ERROR: Busybox is not installed on device!",
				Toast.LENGTH_SHORT).show();
		Log.e(LTAG, "Busybox is NOT available");
		return false;
	}

	private boolean checkMptcpEnabled()
	{
		ArrayList<String> out = Shell
				.executeBlocking("sysctl -a | grep mptcp_enabled");
		// if output is not one line, something went wrong
		if (out.size() < 1)
			return false;
		if (out.get(out.size() - 1).length() < 1)
			return false;
		String res = out.get(out.size() - 1); // always use last line
		if ("net.mptcp.mptcp_enabled = 1".equals(res))
			return true;
		return false;
	}

	/**
	 * Updates the network status view elements. Texts and status colors of
	 * icons.
	 * 
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
		this.textWifiStatus.setText("Wi-Fi: " + wifi_ip
				+ ("0.0.0.0/0".equals(wifi_ip) ? "" : " (" + ssid + ")"));

		// tint image views
		if (mobile_status)
			if (!"0.0.0.0/0".equals(mobile_ip))
				this.imageMobileStatus.setColorFilter(Color.GREEN,
						Mode.MULTIPLY); // ok
			else
				this.imageMobileStatus.setColorFilter(Color.RED, Mode.MULTIPLY); // problem
		else
			this.imageMobileStatus.setColorFilter(Color.GRAY, Mode.MULTIPLY); // down

		if (wifi_status)
			if (!"0.0.0.0/0".equals(wifi_ip))
				this.imageWifiStatus.setColorFilter(Color.GREEN, Mode.MULTIPLY); // ok
			else
				this.imageWifiStatus.setColorFilter(Color.RED, Mode.MULTIPLY); // problem
		else
			this.imageWifiStatus.setColorFilter(Color.GRAY, Mode.MULTIPLY); // down
	}

}
