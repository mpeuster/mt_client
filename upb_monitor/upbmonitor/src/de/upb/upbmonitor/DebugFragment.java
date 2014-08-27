package de.upb.upbmonitor;

import de.upb.upbmonitor.network.NetworkManager;
import de.upb.upbmonitor.network.Route;
import de.upb.upbmonitor.network.RouteManager;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DebugFragment extends Fragment
{
	private static final String LTAG = "DebugFragment";


	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static DebugFragment newInstance()
	{
		DebugFragment fragment = new DebugFragment();
		return fragment;
	}

	public DebugFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		// get view element pointers
		View rootView = inflater.inflate(R.layout.fragment_debug, container,
				false);
		
		Button b1 = (Button) rootView.findViewById(R.id.buttonTest1);
		Button b2 = (Button) rootView.findViewById(R.id.buttonTest2);
		Button b3 = (Button) rootView.findViewById(R.id.buttonTest3);
		Button b4 = (Button) rootView.findViewById(R.id.buttonTest4);
		
		// add events
		b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //NetworkManager.getInstance().switchWiFi();
            	NetworkManager.getInstance().getWiFiInterfaceIp();
            	NetworkManager.getInstance().getWiFiInterfaceMac();
            	NetworkManager.getInstance().getMobileInterfaceIp();
            	NetworkManager.getInstance().getMobileInterfaceMac();
            	NetworkManager.getInstance().getCurrentSsid();
            }
        });
		
		b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Log.e(LTAG, NetworkManager.getInstance().getWifiGateway());
            	Log.e(LTAG, NetworkManager.getInstance().getMobileGateway());
            	NetworkManager.getInstance().setDnsServer("8.8.8.8", "8.8.4.4");
            }
        });
		
		b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	for(Route r : RouteManager.getInstance().getRouteList())
            	{
            		Log.e(LTAG, r.toString());
            		//RouteManager.getInstance().removeRoute(r);
            	}
            	
            	RouteManager.getInstance().addRoute(new Route("default", "192.168.222.254", "wlan0"));
            	
            }
        });
		
		b4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            }
        });
		

		return rootView;
	}	
}
