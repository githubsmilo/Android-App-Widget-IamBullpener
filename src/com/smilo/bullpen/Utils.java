
package com.smilo.bullpen;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public final class Utils {

    private static final String TAG = "Utils";
    private static final boolean DEBUG = Constants.DEBUG_MODE;

    public static boolean isInternetConnected(Context context, boolean isPermitMobileConnection) {
        ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Check wifi.
        NetworkInfo niWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((niWifi != null) && (niWifi.isConnected() == true)) {
                return true;
        }

        // Check bluetooth
        NetworkInfo niBluetooth = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
        if ((niBluetooth != null) && (niBluetooth.isConnected() == true)) {
                return true;
        }

        // Check mobile.
        if (isPermitMobileConnection) {
            NetworkInfo mobileWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((mobileWifi != null) && (mobileWifi.isConnected() == true)) {
                return true;
            }
        }
/*
        // Check ethernet.
        NetworkInfo niEth = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if ((niEth != null) && (niEth.isConnected() == true)) {
                return true;
        }
*/    
        Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
        return false;
    }
    
    public static boolean isTodayBestBoardType(int boardType) {
        if ((boardType == Constants.BOARD_TYPE_MLB_TOWN_TODAY_BEST) ||
            (boardType == Constants.BOARD_TYPE_KBO_TOWN_TODAY_BEST) ||
            (boardType == Constants.BOARD_TYPE_BULLPEN_TODAY_BEST))
            return true;
        else
            return false;
    }
    
    public static int getRefreshTime(int refreshTimeType) {
        switch (refreshTimeType) {
            case Constants.REFRESH_TIME_TYPE_1_MIN :
                return Constants.TIME_1_MIN;
            case Constants.REFRESH_TIME_TYPE_5_MIN :
                return Constants.TIME_5_MIN;
            case Constants.REFRESH_TIME_TYPE_10_MIN :
                return Constants.TIME_10_MIN;
            case Constants.REFRESH_TIME_TYPE_20_MIN :
                return Constants.TIME_20_MIN;
            case Constants.REFRESH_TIME_TYPE_30_MIN :
                return Constants.TIME_30_MIN;
            case Constants.REFRESH_TIME_TYPE_STOP :
                return Constants.TIME_STOP;
            default:
                return Constants.DEFAULT_INTERVAL;
        }
    }
    
    public static String getBoardTitle(Context context, int boardType) {
        Resources res = context.getResources();
        switch (boardType) {
            case Constants.BOARD_TYPE_MLB_TOWN :
                return (res.getString(R.string.remoteViewTitle_MlbTown));
            case Constants.BOARD_TYPE_KBO_TOWN :
                return (res.getString(R.string.remoteViewTitle_KboTown));
            case Constants.BOARD_TYPE_BULLPEN :
                return (res.getString(R.string.remoteViewTitle_Bullpen));
            case Constants.BOARD_TYPE_MLB_TOWN_TODAY_BEST :
                return (res.getString(R.string.remoteViewTitle_MlbTown_TodayBest));
            case Constants.BOARD_TYPE_KBO_TOWN_TODAY_BEST :
                return (res.getString(R.string.remoteViewTitle_KboTown_TodayBest));
            case Constants.BOARD_TYPE_BULLPEN_TODAY_BEST :
                return (res.getString(R.string.remoteViewTitle_Bullpen_TodayBest));
            case Constants.BOARD_TYPE_BULLPEN_1000 :
                return (res.getString(R.string.remoteViewTitle_Bullpen1000));
            case Constants.BOARD_TYPE_BULLPEN_2000 :
                return (res.getString(R.string.remoteViewTitle_Bullpen2000));
            default:
                return (res.getString(R.string.remoteViewTitle_MlbTown));
        }
    }
    
    public static String getBoardUrl(int boardType) {
        switch (boardType) {
            case Constants.BOARD_TYPE_MLB_TOWN :
                return Constants.URL_MLB_TOWN;
            case Constants.BOARD_TYPE_KBO_TOWN :
                return Constants.URL_KBO_TOWN;
            case Constants.BOARD_TYPE_BULLPEN :
                return Constants.URL_BULLPEN;
            case Constants.BOARD_TYPE_MLB_TOWN_TODAY_BEST :
                return Constants.URL_MLB_TOWN_TODAY_BEST;
            case Constants.BOARD_TYPE_KBO_TOWN_TODAY_BEST :
                return Constants.URL_KBO_TOWN_TODAY_BEST;
            case Constants.BOARD_TYPE_BULLPEN_TODAY_BEST :
                return Constants.URL_BULLPEN_TODAY_BEST;
            case Constants.BOARD_TYPE_BULLPEN_1000 :
                return Constants.URL_BULLPEN_1000;
            case Constants.BOARD_TYPE_BULLPEN_2000 :
                return Constants.URL_BULLPEN_2000;
            default:
                return Constants.URL_MLB_TOWN;
        }
    }
    
    public static String getSearchUrl(int searchCategoryType, int searchSubjectType, String searchKeyword) throws UnsupportedEncodingException {
        switch (searchCategoryType) {
            case Constants.SEARCH_CATEGORY_TYPE_TITLE:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + Constants.SEARCH_CATEGORY_PARAMETER_TITLE + 
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.SEARCH_CATEGORY_TYPE_TITLE_CONTENTS:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + Constants.SEARCH_CATEGORY_PARAMETER_TITLE_CONTENTS + 
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.SEARCH_CATEGORY_TYPE_ID:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + Constants.SEARCH_CATEGORY_PARAMETER_ID + 
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.SEARCH_CATEGORY_TYPE_WRITER:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + Constants.SEARCH_CATEGORY_PARAMETER_WRITER + 
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.SEARCH_CATEGORY_TYPE_SUBJECT:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + Constants.SEARCH_CATEGORY_PARAMETER_SUBJECT +
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + getSubjectUrl(searchSubjectType));
            case Constants.SEARCH_CATEGORY_TYPE_HITS:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + Constants.SEARCH_CATEGORY_PARAMETER_HITS + 
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            default:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + Constants.SEARCH_CATEGORY_PARAMETER_TITLE + 
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
        }
    }
    
    private static String getSubjectUrl(int searchSubjectType) {
        switch (searchSubjectType) {
            case Constants.SEARCH_SUBJECT_TYPE_1:
                return Constants.SEARCH_SUBJECT_PARAMETER_1;
            case Constants.SEARCH_SUBJECT_TYPE_2:
                return Constants.SEARCH_SUBJECT_PARAMETER_2;
            case Constants.SEARCH_SUBJECT_TYPE_3:
                return Constants.SEARCH_SUBJECT_PARAMETER_3;
            case Constants.SEARCH_SUBJECT_TYPE_4:
                return Constants.SEARCH_SUBJECT_PARAMETER_4;
            case Constants.SEARCH_SUBJECT_TYPE_5:
                return Constants.SEARCH_SUBJECT_PARAMETER_5;
            case Constants.SEARCH_SUBJECT_TYPE_6:
                return Constants.SEARCH_SUBJECT_PARAMETER_6;
            case Constants.SEARCH_SUBJECT_TYPE_7:
                return Constants.SEARCH_SUBJECT_PARAMETER_7;
            case Constants.SEARCH_SUBJECT_TYPE_8:
                return Constants.SEARCH_SUBJECT_PARAMETER_8;
            case Constants.SEARCH_SUBJECT_TYPE_9:
                return Constants.SEARCH_SUBJECT_PARAMETER_9;
            case Constants.SEARCH_SUBJECT_TYPE_10:
                return Constants.SEARCH_SUBJECT_PARAMETER_10;
            case Constants.SEARCH_SUBJECT_TYPE_11:
                return Constants.SEARCH_SUBJECT_PARAMETER_11;
            case Constants.SEARCH_SUBJECT_TYPE_12:
                return Constants.SEARCH_SUBJECT_PARAMETER_12;
            case Constants.SEARCH_SUBJECT_TYPE_13:
                return Constants.SEARCH_SUBJECT_PARAMETER_13;
            case Constants.SEARCH_SUBJECT_TYPE_14:
                return Constants.SEARCH_SUBJECT_PARAMETER_14;
            case Constants.SEARCH_SUBJECT_TYPE_15:
                return Constants.SEARCH_SUBJECT_PARAMETER_15;
            case Constants.SEARCH_SUBJECT_TYPE_16:
                return Constants.SEARCH_SUBJECT_PARAMETER_16;
            case Constants.SEARCH_SUBJECT_TYPE_17:
                return Constants.SEARCH_SUBJECT_PARAMETER_17;
            case Constants.SEARCH_SUBJECT_TYPE_18:
                return Constants.SEARCH_SUBJECT_PARAMETER_18;
            case Constants.SEARCH_SUBJECT_TYPE_19:
                return Constants.SEARCH_SUBJECT_PARAMETER_19;
            case Constants.SEARCH_SUBJECT_TYPE_20:
                return Constants.SEARCH_SUBJECT_PARAMETER_20;
            case Constants.SEARCH_SUBJECT_TYPE_21:
                return Constants.SEARCH_SUBJECT_PARAMETER_21;
            case Constants.SEARCH_SUBJECT_TYPE_22:
                return Constants.SEARCH_SUBJECT_PARAMETER_22;
            case Constants.SEARCH_SUBJECT_TYPE_23:
                return Constants.SEARCH_SUBJECT_PARAMETER_23;
            case Constants.SEARCH_SUBJECT_TYPE_24:
                return Constants.SEARCH_SUBJECT_PARAMETER_24;
            case Constants.SEARCH_SUBJECT_TYPE_25:
                return Constants.SEARCH_SUBJECT_PARAMETER_25;
            default:
                return Constants.SEARCH_SUBJECT_PARAMETER_1;
        }
    }
}
