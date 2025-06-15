package com.emil_z.helper;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class NetworkUtil {

	/**
	 * Checks if the device has an active internet connection.
	 *
	 * @param context The application or activity context.
	 * @return true if internet is available, false otherwise.
	 */
	public static boolean isInternetAvailable(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getNetworkCapabilities(cm.getActiveNetwork()).hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
		} catch (Exception e) {
			return false;
		}
	}
}