mt_client
=========

Android code for master thesis project.

This application can switch on the cellular and Wi-Fi interface of a device simultaneously. It also controls the routing and switches the default route between cellular and Wi-Fi interface. The route to the backend is always set to the cellular interface so that all signaling traffic is forced to use the cellular connection.

It contains a monitoring service that monitors the system state of the device and periodically sends all these information to the backend. It also polls the backend to get information about the assigned access point. If the assigned access point changes, it automatically triggers wpa_suplicant to connect to the new access point (this is based of the BSSID returned by the backend). 

<h3>Requirements</h3>

* CyanogenMod > 10
* Application needs super user access to the phone
* Cellular interface name has to be: rmnet0 (should work on Nexus devices)
* Wi-Fi interface name has to be: wlan0 (should work on Nexus devices)

<h3>Setup</h3>

Some preferences have to be set before anything in the application is turned on (Otherwise something may fail). The preference dialog can be accessed by pressing the menu item (top-right) or hardware menu button and selecting "Settings"

* Backend API address: IP of the backed API server (must be accessible over the Internet)
* Backend API port: Port of the backend API (e.g. 6680)
* Device ID: Name of this device (any String)
* Location Service ID: Key that maps location information provided by the location service to this device (any String)
* Default SSID: SSID of the Wi-Fi that should be used. Has to be configured here, because we bypass Android's connection manager
* Default PSK: WPA_PSK for Wi-Fi access. Use 'none' for an open Wi-Fi

<h3>Usage</h3>

General: Do not enable MPTCP mode (check box) unless your device has an MPTCP-enabled kernel installed.

1. Switch on Dual Networking Mode in order to activate 3G and WiFi simultaneously.
	* Important: The WiFi that should be used has to be in range, and the MAC address of this device has to be whitelisted. If the connection setup fails, the phone should be restarted in order to set it back into normal mode.
	* Important: SSID and PSK have to be configured in the settings dialog, before dual NW mode is activated.
	* Attention: When the switch is activated, it can take 5-20s until the connection is setup. Do not perform any other actions during this time!


2. Switch on Monitoring Service to connect with backend and periodically send UE status updates to it.
	* Important: The IP address and port of the backend API have to be configured in the settings dialog before this switch is enabled.
	* This service also reacts on decisions made by the backend and switches between access points, based on their SSIDs.




