package de.upb.upbmonitor;

import de.upb.upbmonitor.network.NetworkManager;
import de.upb.upbmonitor.network.Route;
import de.upb.upbmonitor.network.RouteManager;
import de.upb.upbmonitor.network.Rule;
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
            	//NetworkManager.getInstance().getWiFiInterfaceIp();
            	//NetworkManager.getInstance().getWiFiInterfaceMac();
            	//NetworkManager.getInstance().getMobileInterfaceIp();
            	//NetworkManager.getInstance().getMobileInterfaceMac();
            	//NetworkManager.getInstance().getCurrentSsid();
            	//NetworkManager.getInstance().connectWiFi("webauth", null);
            	//Log.i(LTAG, NetworkManager.getInstance().getBackendIp());
            	//NetworkManager.getInstance().associateToAccessPoint("64:70:02:1a:cf:e4");
            }
        });
		
		b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	//NetworkManager.getInstance().associateToAccessPoint("64:70:02:18:7c:60");
            }
        });
		
		b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click   
            	//NetworkManager.getInstance().associateToAccessPoint("64:70:02:0f:f4:a0");	
            	//for(Rule r : RouteManager.getInstance().getRuleList())
            	//{
            	//	Log.w(LTAG, r.toString());
            	//}
            }
        });
		
		b4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	//NetworkManager.getInstance().connectToAccessPoint("64:70:02:18:a0:ed");
            	//NetworkManager.getInstance().disassociateFromAccessPoint();
            	//RouteManager.getInstance().setDefaultRouteToMobile();
            }
        });
		

		return rootView;
	}	
}
