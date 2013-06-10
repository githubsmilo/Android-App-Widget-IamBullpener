
package com.smilo.bullpen;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public final class Utils {

    private static final String TAG = "BullpenUtils";
    
    public static boolean isInternetConnected(Context context, boolean selectedPermitMobileConnection) {
    	Log.i(TAG, "isInternetConnected - selectedPermitMobileConnection[" + selectedPermitMobileConnection + "]");
    	
        ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo niWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((niWifi != null) && (niWifi.isConnected() == true)) {
                return true;
        }

        if (selectedPermitMobileConnection) {
            NetworkInfo mobileWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((mobileWifi != null) && (mobileWifi.isConnected() == true)) {
                return true;
            }
        }
/*
        NetworkInfo niEth = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if ((niEth != null) && (niEth.isConnected() == true)) {
                return true;
        }
*/    
        Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
        return false;
    }
    
    public static String getRemoteViewTitle(Context context, String selectedUrl) {
        Resources res = context.getResources();
        if (selectedUrl == null) {
                  return (res.getString(R.string.remoteViewTitle_Default));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_mlbtown)) {
                  return (res.getString(R.string.remoteViewTitle_MlbTown));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_kbotown)) {
                  return (res.getString(R.string.remoteViewTitle_KboTown));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen)) {
                  return (res.getString(R.string.remoteViewTitle_Bullpen));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen1000)) {
                  return (res.getString(R.string.remoteViewTitle_Bullpen1000));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen2000)) {
                  return (res.getString(R.string.remoteViewTitle_Bullpen2000));
        } else {
                  return null;
        }
  }
    
    public static String getRemoteViewTitleWithPageNum(Context context, String selectedUrl, int pageNum) {
        Resources res = context.getResources();
        if (selectedUrl == null) {
                  return (res.getString(R.string.remoteViewTitle_Default) + " - " + pageNum);
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_mlbtown)) {
                  return (res.getString(R.string.remoteViewTitle_MlbTown) + " - " + pageNum);
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_kbotown)) {
                  return (res.getString(R.string.remoteViewTitle_KboTown) + " - " + pageNum);
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen)) {
                  return (res.getString(R.string.remoteViewTitle_Bullpen) + " - " + pageNum);
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen1000)) {
                  return (res.getString(R.string.remoteViewTitle_Bullpen1000) + " - " + pageNum);
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen2000)) {
                  return (res.getString(R.string.remoteViewTitle_Bullpen2000) + " - " + pageNum);
        } else {
                  return null;
        }
  }
    
    public static int getRefreshTime(int type) {
        int result = -1;
        
        switch (type) {
            case 0: // 30 sec
                result = 60000 / 2;
                break;
            case 1: // 1 min
                result = 60000;
                break;
            case 2: // 5 min
                result = 60000 * 5;
                break;
            case 3: // 10 min
                result = 60000 * 10;
                break;
            case 4: // 30 min
                result = 60000 * 30;
                break; 
            case 5: // do not refresh
                result = -1;
                break;
            default:
                result = Constants.DEFAULT_INTERVAL_AT_MILLIS;
        }
        
        return result;
    }
    
    public static int getRefreshTimeType(int time) {
        int result = -1;
        
        switch (time) {
            case 60000 / 2: // 30 sec
                result = 0;
                break;
            case 60000: // 1 min
                result = 1;
                break;
            case 60000 * 5: // 5 min
                result = 2;
                break;
            case 60000 * 10: // 10 min
                result = 3;
                break;
            case 60000 * 30: // 30 min
                result = 4;
                break; 
            case -1: // do not refresh
                result = 5;
                break;
            default:
                result = 0;
        }
        
        return result;
    }
    
    public static String getBullpenBoardUrl(int type) {
        String result = null;
        
        switch (type) {
            case 0 : // MLB 타운
                result = Constants.mMLBParkUrl_mlbtown;
                break;
            case 1 : // 한국야구 타운
                result = Constants.mMLBParkUrl_kbotown;
                break;
            case 2 : // BULLPEN
                result = Constants.mMLBParkUrl_bullpen;
                break;
            case 3 : // BULLPEN 조회수 1000 이상
                result = Constants.mMLBParkUrl_bullpen1000;
                break;
            case 4 : // BULLPEN 조회수 2000 이상
                result = Constants.mMLBParkUrl_bullpen2000;
                break;
            default:
                result = Constants.mMLBParkUrl_mlbtown;
        }
        
        return result;
    }
    
    public static int getBullpenBoardType(String url) {
        int result = -1;
        if (url.equals(Constants.mMLBParkUrl_mlbtown))
            result = 0;
        else if (url.equals(Constants.mMLBParkUrl_kbotown))
            result = 1;
        else if (url.equals(Constants.mMLBParkUrl_bullpen))
            result = 2;
        else if (url.equals(Constants.mMLBParkUrl_bullpen1000))
            result = 3;
        else if (url.equals(Constants.mMLBParkUrl_bullpen2000))
            result = 4;
        else
            result = 0;
        
        return result;
    }
}
