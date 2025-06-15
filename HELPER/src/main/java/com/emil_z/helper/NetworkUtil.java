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
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) return false;

		NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
		return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
			nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
			nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
	}
}