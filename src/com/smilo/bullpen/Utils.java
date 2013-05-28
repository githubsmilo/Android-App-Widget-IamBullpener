
package com.smilo.bullpen;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public final class Utils {

    private static final String TAG = "BullpenUtils";
    
    public static boolean checkInternetConnectivity(ConnectivityManager cm) {
        if (cm == null) {
            Log.i(TAG, "ConnectivityManager is null!");
            return false;
        }
        
        NetworkInfo niWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((niWifi != null) && (niWifi.isConnected() == true)) {
                return true;
        }
        
        NetworkInfo niEth = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if ((niEth != null) && (niEth.isConnected() == true)) {
                return true;
        }
        
        NetworkInfo mobileWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((mobileWifi != null) && (mobileWifi.isConnected() == true)) {
            return true;
        }
        
        return false;
    }
}
