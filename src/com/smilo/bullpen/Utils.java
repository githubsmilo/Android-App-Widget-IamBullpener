
package com.smilo.bullpen;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
    
    public static String getDateByPageNum1(int pageNum) {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, -pageNum);
        Date date = cal.getTime();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        return dateFormat.format(date);
    }
    
    public static String getDateByPageNum2(int pageNum) {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, -pageNum);
        Date date = cal.getTime();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.KOREA);
        return dateFormat.format(date);
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
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen_best_recommended)) {
            return (res.getString(R.string.remoteViewTitle_BullpenBestRecommended));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen_best_hits)) {
            return (res.getString(R.string.remoteViewTitle_BullpenBestHits));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen_best_reply)) {
            return (res.getString(R.string.remoteViewTitle_BullpenBestReply));
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
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen_best_recommended)) {
            return (res.getString(R.string.remoteViewTitle_BullpenBestRecommended) + " " + getDateByPageNum2(pageNum));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen_best_hits)) {
            return (res.getString(R.string.remoteViewTitle_BullpenBestHits) + " " + getDateByPageNum2(pageNum));
        } else if (selectedUrl.equals(Constants.mMLBParkUrl_bullpen_best_reply)) {
            return (res.getString(R.string.remoteViewTitle_BullpenBestReply) + " " + getDateByPageNum2(pageNum));
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
            case 0: // 1 min
                result = 60000;
                break;
            case 1: // 5 min
                result = 60000 * 5;
                break;
            case 2: // 10 min
                result = 60000 * 10;
                break;
            case 3: // 20 min
                result = 60000 * 20;
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
            case 60000: // 1 min
                result = 0;
                break;
            case 60000 * 5: // 5 min
                result = 1;
                break;
            case 60000 * 10: // 10 min
                result = 2;
                break;
            case 60000 * 20: // 20 min
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
    
    public static boolean isMobileSiteUrl(String url) {
        if ((url.equals(Constants.mMLBParkUrl_bullpen_best_hits)) ||
              (url.equals(Constants.mMLBParkUrl_bullpen_best_recommended)) ||
              (url.equals(Constants.mMLBParkUrl_bullpen_best_reply)))
            return false;
        else
            return true;
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
            case 3 : // BULLPEN 최다 추천
                result = Constants.mMLBParkUrl_bullpen_best_recommended;
                break;
            case 4 : // BULLPEN 최고 조회
                result = Constants.mMLBParkUrl_bullpen_best_hits;
                break;
            case 5 : // BULLPEN 최다 리플
                result = Constants.mMLBParkUrl_bullpen_best_reply;
                break;
            case 6 : // BULLPEN 조회수 1000 이상
                result = Constants.mMLBParkUrl_bullpen1000;
                break;
            case 7 : // BULLPEN 조회수 2000 이상
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
        else if (url.equals(Constants.mMLBParkUrl_bullpen_best_recommended))
            result = 3;
        else if (url.equals(Constants.mMLBParkUrl_bullpen_best_hits))
            result = 4;
        else if (url.equals(Constants.mMLBParkUrl_bullpen_best_reply))
            result = 5;
        else if (url.equals(Constants.mMLBParkUrl_bullpen1000))
            result = 6;
        else if (url.equals(Constants.mMLBParkUrl_bullpen2000))
            result = 7;
        else
            result = 0;
        
        return result;
    }
}
