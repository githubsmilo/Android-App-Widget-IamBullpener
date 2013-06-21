
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
    
    public static int getRefreshTime(Context context, int refreshTimeType) {
        Resources res = context.getResources();
        switch (refreshTimeType) {
            case Constants.REFRESH_TIME_TYPE_1_MIN :
                return res.getInteger(R.integer.int_time_1_min);
            case Constants.REFRESH_TIME_TYPE_5_MIN :
                return res.getInteger(R.integer.int_time_5_min);
            case Constants.REFRESH_TIME_TYPE_10_MIN :
                return res.getInteger(R.integer.int_time_10_min);
            case Constants.REFRESH_TIME_TYPE_20_MIN :
                return res.getInteger(R.integer.int_time_20_min);
            case Constants.REFRESH_TIME_TYPE_30_MIN :
                return res.getInteger(R.integer.int_time_30_min);
            case Constants.REFRESH_TIME_TYPE_STOP :
                return res.getInteger(R.integer.int_time_stop);
            default:
                return res.getInteger(R.integer.int_default_interval);
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
    
    public static String getSearchUrl(Context context, 
            int searchCategoryType, int searchSubjectType, String searchKeyword) throws UnsupportedEncodingException {
        Resources res = context.getResources();
        switch (searchCategoryType) {
            case Constants.SEARCH_CATEGORY_TYPE_TITLE:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_title) +
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.SEARCH_CATEGORY_TYPE_TITLE_CONTENTS:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_title_contents) +
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.SEARCH_CATEGORY_TYPE_ID:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_id) +
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.SEARCH_CATEGORY_TYPE_WRITER:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_writer) +
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.SEARCH_CATEGORY_TYPE_SUBJECT:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_subject) +
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + getSubjectUrl(context, searchSubjectType));
            case Constants.SEARCH_CATEGORY_TYPE_HITS:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_hits) +
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            default:
                return (Constants.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_title) +
                        Constants.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
        }
    }
    
    public static String getSubjectTitle(Context context, int searchSubjectType) {
        Resources res = context.getResources();
        switch (searchSubjectType) {
            case Constants.SEARCH_SUBJECT_TYPE_1:
                return res.getString(R.string.text_subject_1);
            case Constants.SEARCH_SUBJECT_TYPE_2:
                return res.getString(R.string.text_subject_2);
            case Constants.SEARCH_SUBJECT_TYPE_3:
                return res.getString(R.string.text_subject_3);
            case Constants.SEARCH_SUBJECT_TYPE_4:
                return res.getString(R.string.text_subject_4);
            case Constants.SEARCH_SUBJECT_TYPE_5:
                return res.getString(R.string.text_subject_5);
            case Constants.SEARCH_SUBJECT_TYPE_6:
                return res.getString(R.string.text_subject_6);
            case Constants.SEARCH_SUBJECT_TYPE_7:
                return res.getString(R.string.text_subject_7);
            case Constants.SEARCH_SUBJECT_TYPE_8:
                return res.getString(R.string.text_subject_8);
            case Constants.SEARCH_SUBJECT_TYPE_9:
                return res.getString(R.string.text_subject_9);
            case Constants.SEARCH_SUBJECT_TYPE_10:
                return res.getString(R.string.text_subject_10);
            case Constants.SEARCH_SUBJECT_TYPE_11:
                return res.getString(R.string.text_subject_11);
            case Constants.SEARCH_SUBJECT_TYPE_12:
                return res.getString(R.string.text_subject_12);
            case Constants.SEARCH_SUBJECT_TYPE_13:
                return res.getString(R.string.text_subject_13);
            case Constants.SEARCH_SUBJECT_TYPE_14:
                return res.getString(R.string.text_subject_14);
            case Constants.SEARCH_SUBJECT_TYPE_15:
                return res.getString(R.string.text_subject_15);
            case Constants.SEARCH_SUBJECT_TYPE_16:
                return res.getString(R.string.text_subject_16);
            case Constants.SEARCH_SUBJECT_TYPE_17:
                return res.getString(R.string.text_subject_17);
            case Constants.SEARCH_SUBJECT_TYPE_18:
                return res.getString(R.string.text_subject_18);
            case Constants.SEARCH_SUBJECT_TYPE_19:
                return res.getString(R.string.text_subject_19);
            case Constants.SEARCH_SUBJECT_TYPE_20:
                return res.getString(R.string.text_subject_20);
            case Constants.SEARCH_SUBJECT_TYPE_21:
                return res.getString(R.string.text_subject_21);
            case Constants.SEARCH_SUBJECT_TYPE_22:
                return res.getString(R.string.text_subject_22);
            case Constants.SEARCH_SUBJECT_TYPE_23:
                return res.getString(R.string.text_subject_23);
            case Constants.SEARCH_SUBJECT_TYPE_24:
                return res.getString(R.string.text_subject_24);
            case Constants.SEARCH_SUBJECT_TYPE_25:
                return res.getString(R.string.text_subject_25);
            default:
                return res.getString(R.string.text_subject_1);
        }
    }
    
    private static String getSubjectUrl(Context context, int searchSubjectType) {
        Resources res = context.getResources();
        switch (searchSubjectType) {
            case Constants.SEARCH_SUBJECT_TYPE_1:
                return res.getString(R.string.text_subject_parameter_1);
            case Constants.SEARCH_SUBJECT_TYPE_2:
                return res.getString(R.string.text_subject_parameter_2);
            case Constants.SEARCH_SUBJECT_TYPE_3:
                return res.getString(R.string.text_subject_parameter_3);
            case Constants.SEARCH_SUBJECT_TYPE_4:
                return res.getString(R.string.text_subject_parameter_4);
            case Constants.SEARCH_SUBJECT_TYPE_5:
                return res.getString(R.string.text_subject_parameter_5);
            case Constants.SEARCH_SUBJECT_TYPE_6:
                return res.getString(R.string.text_subject_parameter_6);
            case Constants.SEARCH_SUBJECT_TYPE_7:
                return res.getString(R.string.text_subject_parameter_7);
            case Constants.SEARCH_SUBJECT_TYPE_8:
                return res.getString(R.string.text_subject_parameter_8);
            case Constants.SEARCH_SUBJECT_TYPE_9:
                return res.getString(R.string.text_subject_parameter_9);
            case Constants.SEARCH_SUBJECT_TYPE_10:
                return res.getString(R.string.text_subject_parameter_10);
            case Constants.SEARCH_SUBJECT_TYPE_11:
                return res.getString(R.string.text_subject_parameter_11);
            case Constants.SEARCH_SUBJECT_TYPE_12:
                return res.getString(R.string.text_subject_parameter_12);
            case Constants.SEARCH_SUBJECT_TYPE_13:
                return res.getString(R.string.text_subject_parameter_13);
            case Constants.SEARCH_SUBJECT_TYPE_14:
                return res.getString(R.string.text_subject_parameter_14);
            case Constants.SEARCH_SUBJECT_TYPE_15:
                return res.getString(R.string.text_subject_parameter_15);
            case Constants.SEARCH_SUBJECT_TYPE_16:
                return res.getString(R.string.text_subject_parameter_16);
            case Constants.SEARCH_SUBJECT_TYPE_17:
                return res.getString(R.string.text_subject_parameter_17);
            case Constants.SEARCH_SUBJECT_TYPE_18:
                return res.getString(R.string.text_subject_parameter_18);
            case Constants.SEARCH_SUBJECT_TYPE_19:
                return res.getString(R.string.text_subject_parameter_19);
            case Constants.SEARCH_SUBJECT_TYPE_20:
                return res.getString(R.string.text_subject_parameter_20);
            case Constants.SEARCH_SUBJECT_TYPE_21:
                return res.getString(R.string.text_subject_parameter_21);
            case Constants.SEARCH_SUBJECT_TYPE_22:
                return res.getString(R.string.text_subject_parameter_22);
            case Constants.SEARCH_SUBJECT_TYPE_23:
                return res.getString(R.string.text_subject_parameter_23);
            case Constants.SEARCH_SUBJECT_TYPE_24:
                return res.getString(R.string.text_subject_parameter_24);
            case Constants.SEARCH_SUBJECT_TYPE_25:
                return res.getString(R.string.text_subject_parameter_25);
            default:
                return res.getString(R.string.text_subject_parameter_1);
        }
    }
}
