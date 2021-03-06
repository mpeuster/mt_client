package de.upb.upbmonitor.network;

import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.upb.upbmonitor.commandline.Shell;
import de.upb.upbmonitor.commandline.CmdCallback;

/**
 * This class is a network manager to enable Mobile and Wi-Fi connections
 * simultaneously.
 * 
 * It needs root access because it controls the network interfaces with shell
 * commands to circumvent Android's connection manager.
 * 
 * @author manuel
 * 
 */
public class NetworkManager
{
	private static final String LTAG = "NetworkManager";
	// this is maybe vendor specific (tested on Samsung Galaxy Nexsus):
	public static final String WIFI_INTERFACE = "wlan0";
	public static final String MOBILE_INTERFACE = "rmnet0";
	private static NetworkManager INSTANCE;
	private Context myContext = null;

	public void setContext(Context c)
	{
		this.myContext = c;
	}

	public static String WPA_TEMPLATE = null;

	/**
	 * Use as singleton class.
	 * 
	 * @return class instance
	 */
	public synchronized static NetworkManager getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new NetworkManager();
		return INSTANCE;
	}

	/**
	 * Enables Wi-Fi and Mobile data at the same time. Default Wi-Fi parameters
	 * can be given to this method. It will try to connect to this Wi-Fi.
	 * Otherwise no Wi-Fi connection will be established and the interface is
	 * only switched on.
	 * 
	 * @param ssid
	 * @param wpa_psk
	 */
	public synchronized void enableDualNetworking(String ssid, String wpa_psk)
	{
		Log.i(LTAG, "Enabling dual networking");
		// disable wifi with the wifi manager
		//Shell.execute("svc wifi disable"); // does not work on galaxy S3
		// enable mobile with mobile manager
		Shell.execute("svc data enable");
		// bring up wifi interface by hand
		Shell.execute("netcfg wlan0 up");
		// trigger callback command
		Shell.executeCustom(this.eventAfterDualNetworkingEnabled);

		// try to connect to default WiFi
		connectWiFi(ssid, wpa_psk);
	}

	/**
	 * Connect to the specified Wi-Fi if we are in dual mode. IF wpa_psk is
	 * null, it is assumed that the network is not encrypted.
	 * 
	 * Only DHCP is supported at the moment.
	 * 
	 * It will initially choose any AP available in this network. To change the
	 * AP use the connectToAccessPoint(BSSID) method.
	 * 
	 * @param ssid
	 * @param wpa_psk
	 */
	public synchronized void connectWiFi(String ssid, String wpa_psk)
	{
		Log.i(LTAG, "Connecting to WiFi with SSID: " + ssid + " and PSK: "
				+ wpa_psk);
		// -- create an individual configuration for wpa_supplicant
		if (ssid != null) // only reconfigure config if ssid is given
			setWifiConfiguration(ssid, wpa_psk);

		// -- connection procedure
		// stop dhcp client
		Shell.execute("pkill dhcpcd");
		// kill wifi management
		Shell.execute("pkill wpa_supplicant");
		// configure target wifi (copy indiv. config to destination)
		Shell.execute("cp /sdcard/wpa_supplicant.conf /data/misc/wifi/wpa_supplicant.conf");
		Shell.execute("chmod 666 /data/misc/wifi/wpa_supplicant.conf");
		// connect to actual wifi
		Shell.execute("wpa_supplicant -B -Dnl80211 -iwlan0 -c/data/misc/wifi/wpa_supplicant.conf");
		// bring up dhcp client and receive ip (takes some time!)
		if (this.isStaticIpEnabled())
		{
			// static IP
			Shell.execute("ifconfig wlan0 " + this.getStaticIp() + " netmask "
					+ this.getStaticNetmask());
		} else
		{
			// DHCP
			Shell.execute("dhcpcd wlan0");
			// Shell.execute("netcfg wlan0 dhcp");
		}
		// run custom callback command to trigger setup after connection is
		// established and IP is received
		Shell.executeCustom(this.eventAfterWifiConnected);
	}

	/**
	 * Disables the dual networking mode by disabling both, Wi-Fi and mobile
	 * data. After this, Wi-Fi and Mobile data can again be switched on in
	 * normal mode in Android's settings dialog.
	 */
	public synchronized void disableDualNetworking()
	{
		Log.i(LTAG, "Disabling dual networking");
		// kill dhcp client
		// Shell.execute("pkill dhcpcd");
		// kill wifi management
		Shell.execute("pkill wpa_supplicant");
		// tear down wifi interface
		Shell.execute("netcfg wlan0 down");
		// disable wifi in manager
		Shell.execute("svc wifi disable");
		// disable data in manager
		Shell.execute("svc data disable");
		// trigger callback command
		Shell.executeCustom(this.eventAfterDualNetworkingDisabled);

		// remove MPTCP routing rules
		if (isMptcpEnabled())
		{
			RouteManager.getInstance().removeRulesWithLookup("1");
			RouteManager.getInstance().removeRulesWithLookup("2");
		}
	}

	/**
	 * Associates this UE to a AP specified by the BSSID. Assumes, that we are
	 * already connected to a Wi-Fi.
	 * 
	 * @param bssid
	 */
	public synchronized void associateToAccessPoint(String bssid)
	{
		Log.i(LTAG, "Connecting to AP with BSSID: " + bssid);
		Shell.execute("wpa_cli -p/data/misc/wifi/sockets/ -iwlan0 disconnect");
		// only if there is a bssid (else connect to any)
		if (bssid != null && bssid.length() > 0)
			Shell.execute("wpa_cli -p/data/misc/wifi/sockets/ -iwlan0 set_network 0 bssid "
					+ bssid);
		Shell.execute("wpa_cli -p/data/misc/wifi/sockets/ -iwlan0 reassociate");
	}

	/**
	 * Disassociates this UE from a AP.
	 */
	public synchronized void disassociateFromAccessPoint()
	{
		Log.i(LTAG, "Disconnecting from AP.");
		Shell.execute("wpa_cli -p/data/misc/wifi/sockets/ -iwlan0 disconnect");
	}

	public synchronized boolean isDualNetworkingEnabled()
	{
		return (this.isMobileInterfaceEnabled() && this
				.isWiFiInterfaceEnabled());
	}

	public synchronized boolean isWiFiInterfaceEnabled()
	{
		return this.isInterfaceUp(WIFI_INTERFACE);
	}

	public synchronized boolean isMobileInterfaceEnabled()
	{
		return this.isInterfaceUp(MOBILE_INTERFACE);
	}

	public synchronized String getWiFiInterfaceIp()
	{
		return this.getInterfaceIp(WIFI_INTERFACE);
	}

	public synchronized String getMobileInterfaceIp()
	{
		return this.getInterfaceIp(MOBILE_INTERFACE);
	}

	public synchronized String getWiFiInterfaceMac()
	{
		return this.getInterfaceMac(WIFI_INTERFACE);
	}

	public synchronized String getMobileInterfaceMac()
	{
		return this.getInterfaceMac(MOBILE_INTERFACE);
	}

	public synchronized String getCurrentSsid()
	{
		// look into wpa_supplicatn.conf to get ssid
		// ArrayList<String> out = Shell
		// .executeBlocking("cat /data/misc/wifi/wpa_supplicant.conf | grep \"ssid=\" | cut -d '\"' -f2");
		ArrayList<String> out = Shell
				.executeBlocking("wpa_cli -p/data/misc/wifi/sockets/ -iwlan0 status | grep 'ssid' | cut -d '=' -f2");
		// if output is not one line, something went wrong
		if (out.size() < 1)
		{
			// Log.e(LTAG, "Error while fetching SSID.");
			return null;
		}
		// get SSID (always use last output)
		String ssid = out.get(out.size() - 1);

		if (ssid.contains("error") || ssid.contains("Failed"))
			return null;

		Log.v(LTAG, "Current SSID: " + ssid);

		return ssid;
	}

	public synchronized String getCurrentOperator()
	{
		return this.getProp("gsm.sim.operator.alpha");
	}

	public synchronized String getWifiGateway()
	{
		if (this.isStaticIpEnabled())
		{
			// return static gateway
			return this.getStaticGateway();
		}
		// return gateway fetched by dhcp
		// not sure why, but this is not always the same, so try both:
		String alt1 = this.getProp("dhcp." + WIFI_INTERFACE + ".gateway");
		String alt2 = this.getProp("net." + WIFI_INTERFACE + ".gw");
		if (alt1 != null)
			return alt1;
		return alt2;
	}

	public synchronized String getMobileGateway()
	{
		// not sure why, but this is not always the same, so try both:
		String alt1 = this.getProp("dhcp." + MOBILE_INTERFACE + ".gateway");
		String alt2 = this.getProp("net." + MOBILE_INTERFACE + ".gw");
		if (alt1 != null)
			return alt1;
		return alt2;
	}

	public synchronized void setDnsServer(String ip, String ip2,
			String default_interface)
	{
		// 1. for android < 4.4
		// try to set global properties
		// however this does not seem to work perfectly, the values
		// are ignored from time to time
		this.setProp("net.dns1", ip);
		this.setProp("dhcp.wlan0.dns1", ip);
		this.setProp("net.rmnet0.dns1", ip);
		this.setProp("net.dns2", ip2);
		this.setProp("dhcp.wlan0.dns2", ip2);
		this.setProp("net.rmnet0.dns2", ip2);

		// 2.
		// alternative solution is to set iptable entry, not so nice
		// http://android.stackexchange.com/questions/62081/how-to-change-mobile-connectionss-dns-on-android-kitkat

		// 3. for android >= 4.4
		Shell.execute("ndc resolver flushif wlan0");
		Shell.execute("ndc resolver flushdefaultif");
		Shell.execute("ndc resolver setifdns wlan0 " + ip + " " + ip2);
		Shell.execute("ndc resolver setifdns rmnet0 " + ip + " " + ip2);
		Shell.execute("ndc resolver setdefaultif " + default_interface);

		Log.i(LTAG, "Changed DNS server to: " + ip + " and " + ip2
				+ " with defaultif: " + default_interface);
	}

	/**
	 * ==================== CALLBACKS =====================
	 */

	private CmdCallback eventAfterDualNetworkingEnabled = new CmdCallback(
			"sleep 1")
	{
		@Override
		public void commandCompleted(int id, int exitCode)
		{
			// check if wlan0 is really up
			if (isWiFiInterfaceEnabled() && isMobileInterfaceEnabled())
			{
				// log information
				Log.i(LTAG, "Wifi & Mobile interface are UP.");
				// ...
			}
		}
	};

	/**
	 * This shell command must be called as last command of a WiFi connection
	 * process. It acts as a callback to setup routing etc. what has to be done
	 * after a new WiFi connection is established.
	 */
	private CmdCallback eventAfterWifiConnected = new CmdCallback("sleep 1")
	{
		@Override
		public void commandCompleted(int id, int exitCode)
		{
			// check if wlan0 is really up and working
			if (isWiFiInterfaceEnabled()
					&& !getWiFiInterfaceIp().equals("0.0.0.0/0"))
			{
				// log information
				Log.i(LTAG, "Wifi is connected and runnig with IP: "
						+ getWiFiInterfaceIp());

				if (!isMptcpEnabled())
				{ // -------- no MPTCP
					// change routes to use WiFi for everything except backend
					// communication

					// set DNS server
					setDnsServer("8.8.8.8", "8.8.4.4", WIFI_INTERFACE);
					// change routing
					RouteManager rm = RouteManager.getInstance();
					// set default route
					setDefaultRouteToWiFi();
					// set route to backend to always use dev rmnet0
					setBackendRoute();
					// check for active WiFi route
					if (!rm.routeExists("default", null, WIFI_INTERFACE, null,
							null))
						Log.e(LTAG, "ATTENTION: WiFi default route not found.");
				} else
				{ // -------- MPTCP
					// Use rmnet0 as global default connection, and additional
					// paths on wlan0
					// setup MPTCP routing scheme:
					// Table 1: rmnet0
					// Table 2: wlan0
					// Attention: IPs of both interfaces must available here.

					// fetch necessary IP and subnet information
					// TODO: this is a bit fuzzy. create better design.
					String mobileNet = getMobileInterfaceIp();
					String wifiNet = getWiFiInterfaceIp();
					String mobileIp = null;
					if (mobileNet.contains("/"))
						mobileIp = mobileNet.split("/")[0];
					String mobileSubnet = null;
					if (mobileNet.contains("/"))
						mobileSubnet = mobileNet.split("/")[1];
					String wifiIp = null;
					if (wifiNet.contains("/"))
						wifiIp = wifiNet.split("/")[0];
					String wifiSubnet = null;
					if (wifiNet.contains("/"))
						wifiSubnet = wifiNet.split("/")[1];
					String mobileGw = getMobileGateway();
					String wifiGw = getWifiGateway();

					/*
					 * Log.w(LTAG, mobileIp); Log.w(LTAG, mobileSubnet);
					 * Log.w(LTAG, mobileGw); Log.w(LTAG, wifiIp); Log.w(LTAG,
					 * wifiSubnet); Log.w(LTAG, wifiGw);
					 */

					// switch DNS
					setDnsServer("8.8.8.8", "8.8.4.4",
							NetworkManager.MOBILE_INTERFACE);

					RouteManager rm = RouteManager.getInstance();
					// remove all default routes
					rm.removeDefaultRoutes();
					// add rules for both source addresses
					Rule r1 = new Rule(mobileIp, "1");
					if (!rm.ruleExists(r1))
						rm.addRule(r1);
					Rule r2 = new Rule(wifiIp, "2");
					if (!rm.ruleExists(r2))
						rm.addRule(r2);
					// setup table 1 (mobile)
					rm.addRoute(new Route("default", mobileGw,
							MOBILE_INTERFACE, null, "1"));
					// setup table 2 (wifi)
					rm.addRoute(new Route("default", wifiGw, WIFI_INTERFACE,
							null, "2"));
					// add global default route
					rm.addRoute(new Route("default", mobileGw,
							MOBILE_INTERFACE, "global", null));
				}
			}
		}
	};

	private CmdCallback eventAfterDualNetworkingDisabled = new CmdCallback(
			"sleep 1")
	{
		@Override
		public void commandCompleted(int id, int exitCode)
		{
			// check if wlan0 is really down
			if (!isWiFiInterfaceEnabled() && !isMobileInterfaceEnabled())
			{
				// log information
				Log.i(LTAG, "Wifi & Mobile interface are DOWN.");
				// ...
			}
		}
	};

	/**
	 * ====================== HELPER ======================
	 */

	public synchronized void setBackendRoute()
	{
		RouteManager rm = RouteManager.getInstance();
		String backend_ip = getBackendIp();
		if (backend_ip != null)
		{
			Route rb = new Route(backend_ip, null, MOBILE_INTERFACE);
			rm.addRoute(rb);
			Log.i(LTAG,
					"Added route for backend IP over rmnet0: " + rb.toString());
		} else
		{
			Log.e(LTAG, "Can not resolve backend IP address. Route not set.");
		}
	}

	public synchronized void setBackendRoute(String backend_ip)
	{
		RouteManager rm = RouteManager.getInstance();
		if (backend_ip != null)
		{
			Route rb = new Route(backend_ip, null, MOBILE_INTERFACE);
			rm.addRoute(rb);
			Log.i(LTAG,
					"Added route for backend IP over rmnet0: " + rb.toString());
		} else
		{
			Log.e(LTAG, "Can not resolve backend IP address. Route not set.");
		}
	}

	public synchronized void setDefaultRouteToWiFi()
	{
		RouteManager.getInstance().setDefaultRouteToWiFi();
	}

	public synchronized void setDefaultRouteToMobile()
	{
		RouteManager.getInstance().setDefaultRouteToMobile();
	}

	private synchronized String getInterfaceIp(String interfaceName)
	{
		ArrayList<String> status = this.getInterfaceStatus(interfaceName);
		// check result for errors
		if (status == null || status.size() < 2)
			return null;

		Log.v(LTAG, "Interface IP of " + interfaceName + ": " + status.get(2));

		// return IP field
		return status.get(2);
	}

	private synchronized String getInterfaceMac(String interfaceName)
	{
		ArrayList<String> status = this.getInterfaceStatus(interfaceName);
		// check result for errors
		if (status == null || status.size() < 2)
			return null;

		Log.v(LTAG, "Interface MAC of " + interfaceName + ": " + status.get(4));

		// return IP field
		return status.get(4);
	}

	private synchronized boolean isInterfaceUp(String interfaceName)
	{
		ArrayList<String> status = this.getInterfaceStatus(interfaceName);
		// check result for errors
		if (status == null || status.size() < 2)
			return false;

		Log.v(LTAG,
				"Interface state of " + interfaceName + ": " + status.get(1));

		// check against state field
		if (status.get(1).equals("DOWN"))
			return false;
		return true;
	}

	/**
	 * Uses "netcfg" command to receive interface states, IPs, etc.
	 * 
	 * @param interfaceName
	 * @return ArrayList<String> containing (interface name, status, IP, ?, mac)
	 */
	private synchronized ArrayList<String> getInterfaceStatus(
			String interfaceName)
	{

		// execute netcfg command to check interface state
		ArrayList<String> out = Shell.executeBlocking("netcfg | grep "
				+ interfaceName);
		// if output is not one line, something went wrong
		if (out.size() < 1)
		{
			// Log.e(LTAG, "Bad netcfg result: " + out);
			return null;
		}
		// parse output and put all interesting values into array
		ArrayList<String> result = new ArrayList<String>();
		String l = out.get(out.size() - 1); // always use last line
		for (String p : l.split(" "))
			if (p.length() > 1)
				result.add(p);
		return result;
	}

	/**
	 * Writs the WiFi configuration to the wpa_supplicant.conf file. Needs the
	 * SSID of the target Wifi. WPA_PSK can be the WiFi's key, or null in order
	 * to use no encryption.
	 * 
	 * Resulting config file is written to SD-card and has to be copied from
	 * there, because we are not allowed to write into system files.
	 * 
	 * @param ssid
	 * @param wpa_psk
	 * @return
	 */
	private boolean setWifiConfiguration(String ssid, String wpa_psk)
	{
		// definitions
		String filename = "/sdcard/wpa_supplicant.conf";
		String config_str = WPA_TEMPLATE;
		String key_mgmt = wpa_psk == null ? "NONE" : "WPA-PSK";

		// set configuration values in teplate
		config_str = config_str.replace("${SSID}", ssid); // set ssid
		if (wpa_psk != null)
			config_str = config_str.replace("${WPA_PSK}", wpa_psk); // psk
		else
			config_str = config_str.replace("\tpsk=\"${WPA_PSK}\"\n", ""); // no
																			// psk
		config_str = config_str.replace("${KEY_MGMT}", key_mgmt); // mode

		// write configuration file to SD card (is copied later, since we are
		// not allowed to write system paths)
		try
		{
			FileOutputStream f = new FileOutputStream(filename);
			f.write(config_str.getBytes());
			f.close();
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private String getProp(String key)
	{
		ArrayList<String> out = Shell.executeBlocking("getprop " + key);
		// if output is not one line, something went wrong
		if (out.size() < 1)
			return null;
		if (out.get(out.size() - 1).length() < 1)
			return null;
		return out.get(out.size() - 1); // always use last line

	}

	private void setProp(String key, String value)
	{
		Shell.execute("setprop " + key + " " + value);
	}

	private String getBackendIp()
	{
		if (this.myContext == null)
			return null;
		// get preference value and do name lookup
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this.myContext);
		String hostname = preferences.getString("pref_backend_api_address",
				null);
		return this.getIpByHostname(hostname);
	}

	public String getIpByHostname(String hostname)
	{
		return hostname;
		/*
		 * ArrayList<String> out = Shell.executeBlocking("nslookup " + hostname
		 * + " | grep \"Address 1\" | cut -d \" \" -f3"); // if output is not
		 * one line, something went wrong if (out.size() < 1) return null; if
		 * (out.get(out.size() - 1).length() < 1) return null; String res =
		 * out.get(out.size() - 1); // always use last line Log.i(LTAG,
		 * "Lookup: " + hostname + " = " + res); return res;
		 */

	}

	public boolean isMptcpEnabled()
	{
		ArrayList<String> out = Shell
				.executeBlocking("sysctl -a 2>&1 | grep net.mptcp.mptcp_enabled | cut -d ' ' -f 3");
		// if output is not one line, something went wrong
		if (out.size() < 1)
			return false;
		if (out.get(out.size() - 1).length() < 1)
			return false;
		String res = out.get(out.size() - 1); // always use last line
		if ("1".equals(res))
			return true;
		return false;
	}

	public boolean isStaticIpEnabled()
	{
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this.myContext);
		return preferences.getBoolean("pref_switch_static_ip", false);
	}

	public String getStaticIp()
	{
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this.myContext);
		return preferences.getString("pref_network_ip", "10.10.10.10");
	}

	public String getStaticNetmask()
	{
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this.myContext);
		return preferences.getString("pref_network_netmask", "255.255.255.0");
	}

	public String getStaticGateway()
	{
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this.myContext);
		return preferences.getString("pref_network_gateway", "10.10.10.254");
	}

}
