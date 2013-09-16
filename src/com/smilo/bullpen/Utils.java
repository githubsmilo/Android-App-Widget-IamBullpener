
package com.smilo.bullpen;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.smilo.bullpen.Constants.INTERNET_CONNECTED_RESULT;
import com.smilo.bullpen.Constants.PARSING_RESULT;

public final class Utils {

    private static final String TAG = "Utils";
    private static final boolean DEBUG = Constants.DEBUG_MODE;

    public static INTERNET_CONNECTED_RESULT isInternetConnected(Context context, boolean isPermitMobileConnection) {
        ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Check wifi.
        NetworkInfo niWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((niWifi != null) && (niWifi.isConnected() == true)) {
                return INTERNET_CONNECTED_RESULT.SUCCESS_WIFI;
        }

        // Check bluetooth
        NetworkInfo niBluetooth = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
        if ((niBluetooth != null) && (niBluetooth.isConnected() == true)) {
                return INTERNET_CONNECTED_RESULT.SUCCESS_BLUETOOTH;
        }

        // Check mobile.
        if (isPermitMobileConnection) {
            NetworkInfo mobileWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((mobileWifi != null) && (mobileWifi.isConnected() == true)) {
                return INTERNET_CONNECTED_RESULT.SUCCESS_MOBILE;
            }
        }
/*
        // Check ethernet.
        NetworkInfo niEth = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if ((niEth != null) && (niEth.isConnected() == true)) {
                return true;
        }
*/    
        return INTERNET_CONNECTED_RESULT.FAILED;
    }
    
    public static ExtraItem createExtraItemFromIntent(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,  AppWidgetManager.INVALID_APPWIDGET_ID);
        int pageNum = intent.getIntExtra(
                Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
        int boardType = intent.getIntExtra(
                Constants.EXTRA_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
        int refreshTimeType = intent.getIntExtra(
                Constants.EXTRA_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
        boolean permitMobileConnectionType = intent.getBooleanExtra(
                Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
        String blackList = intent.getStringExtra(
                Constants.EXTRA_BLACK_LIST);
        String blockedWords = intent.getStringExtra(
                    Constants.EXTRA_BLOCKED_WORDS);
        int searchCategoryType = intent.getIntExtra(
                Constants.EXTRA_SEARCH_CATEGORY_TYPE, Constants.DEFAULT_SEARCH_CATEGORY_TYPE);
        int searchSubjectType = intent.getIntExtra(
                Constants.EXTRA_SEARCH_SUBJECT_TYPE, Constants.DEFAULT_SEARCH_SUBJECT_TYPE);
        String searchKeyword = intent.getStringExtra(
                Constants.EXTRA_SEARCH_KEYWORD);
        
        ExtraItem item = new ExtraItem(
                appWidgetId, pageNum, boardType, refreshTimeType, permitMobileConnectionType,
                blackList, blockedWords, searchCategoryType, searchSubjectType, searchKeyword);
        
        return item;
    }
    
    public static ListItem createListItemFromIntent(Intent intent) {
        String titlePrefix = intent.getStringExtra(Constants.EXTRA_ITEM_TITLE_PREFIX);
        String title = intent.getStringExtra(Constants.EXTRA_ITEM_TITLE);
        String writer = intent.getStringExtra(Constants.EXTRA_ITEM_WRITER);
        String url = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
        int commentNum = intent.getIntExtra(Constants.EXTRA_ITEM_COMMENT_NUM, Constants.DEFAULT_COMMENT_NUM);
        
        ListItem item = new ListItem(titlePrefix, title, writer, url, commentNum);
        
        return item;
    }
    
    public static Intent createIntentFromExtraItem(
            Context context, String className, String actionName, ExtraItem item, boolean isAddNewTask) {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, item.getAppWidgetId());
        intent.putExtra(Constants.EXTRA_PAGE_NUM, item.getPageNum());
        intent.putExtra(Constants.EXTRA_BOARD_TYPE, item.getBoardType());
        intent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, item.getRefreshTimeType());
        intent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, item.getPermitMobileConnectionType());
        intent.putExtra(Constants.EXTRA_BLACK_LIST, item.getBlackList());
        intent.putExtra(Constants.EXTRA_BLOCKED_WORDS, item.getBlockedWords());
        intent.putExtra(Constants.EXTRA_SEARCH_CATEGORY_TYPE, item.getSearchCategoryType());
        intent.putExtra(Constants.EXTRA_SEARCH_SUBJECT_TYPE, item.getSearchSubjectType());
        intent.putExtra(Constants.EXTRA_SEARCH_KEYWORD, item.getSearchKeyword());
        if (className != null)
            intent.setClassName(context, className);
        if (actionName != null)
            intent.setAction(actionName);
        if (isAddNewTask)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        return intent;
    }
    
    public static String getMobileBoardUrl(Context context, int pageNum, int boardType,
            int searchCategoryType, int searchSubjectType, String searchKeyword) throws UnsupportedEncodingException {
        String result = null;
        if ((searchCategoryType == Constants.DEFAULT_SEARCH_CATEGORY_TYPE) ||
              ((searchCategoryType != Constants.Specific.SEARCH_CATEGORY_TYPE_SUBJECT) &&
                (searchKeyword == null || searchKeyword.equals("")))) {
            result = getBoardUrl(boardType) + pageNum;
        } else {
            result = getBoardUrl(boardType) + pageNum + getSearchUrl(context, searchCategoryType, searchSubjectType, searchKeyword);
        }

        return result;
    }

    public static boolean isTodayBestBoardType(int boardType) {
        if ((boardType == Constants.Specific.BOARD_TYPE_MLB_TOWN_TODAY_BEST) ||
            (boardType == Constants.Specific.BOARD_TYPE_KBO_TOWN_TODAY_BEST) ||
            (boardType == Constants.Specific.BOARD_TYPE_BULLPEN_TODAY_BEST))
            return true;
        else
            return false;
    }
    
    public static boolean isScrapBoardType(int boardType) {
        if (boardType == Constants.Specific.BOARD_TYPE_SCRAP)
            return true;
        else
            return false;
    }
    
    public static boolean isPredefinedBoardType(int boardType) {
        if ((boardType == Constants.Specific.BOARD_TYPE_BULLPEN_1000) ||
            (boardType == Constants.Specific.BOARD_TYPE_BULLPEN_2000))
            return true;
        else
            return false;
    }
    
    public static int getRefreshTime(Context context, int refreshTimeType) {
        Resources res = context.getResources();
        switch (refreshTimeType) {
            case Constants.Specific.REFRESH_TIME_TYPE_1_MIN :
                return res.getInteger(R.integer.int_time_1_min);
            case Constants.Specific.REFRESH_TIME_TYPE_5_MIN :
                return res.getInteger(R.integer.int_time_5_min);
            case Constants.Specific.REFRESH_TIME_TYPE_10_MIN :
                return res.getInteger(R.integer.int_time_10_min);
            case Constants.Specific.REFRESH_TIME_TYPE_20_MIN :
                return res.getInteger(R.integer.int_time_20_min);
            case Constants.Specific.REFRESH_TIME_TYPE_30_MIN :
                return res.getInteger(R.integer.int_time_30_min);
            case Constants.Specific.REFRESH_TIME_TYPE_STOP :
                return res.getInteger(R.integer.int_time_stop);
            default:
                return res.getInteger(R.integer.int_default_interval);
        }
    }
    
    public static String getBoardTitle(Context context, int boardType) {
        Resources res = context.getResources();
        switch (boardType) {
            case Constants.Specific.BOARD_TYPE_SCRAP:
                return res.getString(R.string.remoteViewTitle_Scrap);
            case Constants.Specific.BOARD_TYPE_MLB_TOWN :
                return res.getString(R.string.remoteViewTitle_MlbTown);
            case Constants.Specific.BOARD_TYPE_KBO_TOWN :
                return res.getString(R.string.remoteViewTitle_KboTown);
            case Constants.Specific.BOARD_TYPE_BULLPEN :
                return res.getString(R.string.remoteViewTitle_Bullpen);
            case Constants.Specific.BOARD_TYPE_MLB_TOWN_TODAY_BEST :
                return res.getString(R.string.remoteViewTitle_MlbTown_TodayBest);
            case Constants.Specific.BOARD_TYPE_KBO_TOWN_TODAY_BEST :
                return res.getString(R.string.remoteViewTitle_KboTown_TodayBest);
            case Constants.Specific.BOARD_TYPE_BULLPEN_TODAY_BEST :
                return res.getString(R.string.remoteViewTitle_Bullpen_TodayBest);
            case Constants.Specific.BOARD_TYPE_BULLPEN_1000 :
                return res.getString(R.string.remoteViewTitle_Bullpen1000);
            case Constants.Specific.BOARD_TYPE_BULLPEN_2000 :
                return res.getString(R.string.remoteViewTitle_Bullpen2000);
            case Constants.Specific.BOARD_TYPE_NEWS :
                return res.getString(R.string.remoteViewTitle_News);
            default:
                return res.getString(R.string.remoteViewTitle_MlbTown);
        }
    }
    
    public static String getSubjectTitle(Context context, int searchSubjectType) {
        Resources res = context.getResources();
        switch (searchSubjectType) {
            case Constants.Specific.SEARCH_SUBJECT_TYPE_1:
                return res.getString(R.string.text_subject_1);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_2:
                return res.getString(R.string.text_subject_2);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_3:
                return res.getString(R.string.text_subject_3);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_4:
                return res.getString(R.string.text_subject_4);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_5:
                return res.getString(R.string.text_subject_5);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_6:
                return res.getString(R.string.text_subject_6);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_7:
                return res.getString(R.string.text_subject_7);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_8:
                return res.getString(R.string.text_subject_8);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_9:
                return res.getString(R.string.text_subject_9);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_10:
                return res.getString(R.string.text_subject_10);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_11:
                return res.getString(R.string.text_subject_11);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_12:
                return res.getString(R.string.text_subject_12);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_13:
                return res.getString(R.string.text_subject_13);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_14:
                return res.getString(R.string.text_subject_14);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_15:
                return res.getString(R.string.text_subject_15);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_16:
                return res.getString(R.string.text_subject_16);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_17:
                return res.getString(R.string.text_subject_17);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_18:
                return res.getString(R.string.text_subject_18);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_19:
                return res.getString(R.string.text_subject_19);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_20:
                return res.getString(R.string.text_subject_20);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_21:
                return res.getString(R.string.text_subject_21);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_22:
                return res.getString(R.string.text_subject_22);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_23:
                return res.getString(R.string.text_subject_23);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_24:
                return res.getString(R.string.text_subject_24);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_25:
                return res.getString(R.string.text_subject_25);
            default:
                return res.getString(R.string.text_subject_1);
        }
    }
    
    public static String getBoardUrl(int boardType) {
        switch (boardType) {
            case Constants.Specific.BOARD_TYPE_SCRAP:
                return Constants.Specific.URL_SCRAP;
            case Constants.Specific.BOARD_TYPE_MLB_TOWN :
                return Constants.Specific.URL_MLB_TOWN;
            case Constants.Specific.BOARD_TYPE_KBO_TOWN :
                return Constants.Specific.URL_KBO_TOWN;
            case Constants.Specific.BOARD_TYPE_BULLPEN :
                return Constants.Specific.URL_BULLPEN;
            case Constants.Specific.BOARD_TYPE_MLB_TOWN_TODAY_BEST :
                return Constants.Specific.URL_MLB_TOWN_TODAY_BEST;
            case Constants.Specific.BOARD_TYPE_KBO_TOWN_TODAY_BEST :
                return Constants.Specific.URL_KBO_TOWN_TODAY_BEST;
            case Constants.Specific.BOARD_TYPE_BULLPEN_TODAY_BEST :
                return Constants.Specific.URL_BULLPEN_TODAY_BEST;
            case Constants.Specific.BOARD_TYPE_BULLPEN_1000 :
                return Constants.Specific.URL_BULLPEN_1000;
            case Constants.Specific.BOARD_TYPE_BULLPEN_2000 :
                return Constants.Specific.URL_BULLPEN_2000;
            case Constants.Specific.BOARD_TYPE_NEWS :
                return Constants.Specific.URL_NEWS;
            default:
                return Constants.Specific.URL_MLB_TOWN;
        }
    }
    
    private static String getSearchUrl(Context context, 
            int searchCategoryType, int searchSubjectType, String searchKeyword) throws UnsupportedEncodingException {
        Resources res = context.getResources();
        switch (searchCategoryType) {
            case Constants.Specific.SEARCH_CATEGORY_TYPE_TITLE:
                return (Constants.Specific.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_title) +
                        Constants.Specific.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.Specific.SEARCH_CATEGORY_TYPE_TITLE_CONTENTS:
                return (Constants.Specific.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_title_contents) +
                        Constants.Specific.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.Specific.SEARCH_CATEGORY_TYPE_ID:
                return (Constants.Specific.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_id) +
                        Constants.Specific.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.Specific.SEARCH_CATEGORY_TYPE_WRITER:
                return (Constants.Specific.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_writer) +
                        Constants.Specific.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            case Constants.Specific.SEARCH_CATEGORY_TYPE_SUBJECT:
                return (Constants.Specific.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_subject) +
                        Constants.Specific.URL_PARAMETER_SEARCH_KEYWORD + getSubjectUrl(context, searchSubjectType));
            case Constants.Specific.SEARCH_CATEGORY_TYPE_HITS:
                return (Constants.Specific.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_hits) +
                        Constants.Specific.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
            default:
                return (Constants.Specific.URL_PARAMETER_SEARCH_CATEGORY + res.getString(R.string.text_category_parameter_title) +
                        Constants.Specific.URL_PARAMETER_SEARCH_KEYWORD + URLEncoder.encode(searchKeyword,"EUC_KR"));
        }
    }
    
    
    private static String getSubjectUrl(Context context, int searchSubjectType) {
        Resources res = context.getResources();
        switch (searchSubjectType) {
            case Constants.Specific.SEARCH_SUBJECT_TYPE_1:
                return res.getString(R.string.text_subject_parameter_1);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_2:
                return res.getString(R.string.text_subject_parameter_2);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_3:
                return res.getString(R.string.text_subject_parameter_3);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_4:
                return res.getString(R.string.text_subject_parameter_4);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_5:
                return res.getString(R.string.text_subject_parameter_5);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_6:
                return res.getString(R.string.text_subject_parameter_6);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_7:
                return res.getString(R.string.text_subject_parameter_7);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_8:
                return res.getString(R.string.text_subject_parameter_8);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_9:
                return res.getString(R.string.text_subject_parameter_9);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_10:
                return res.getString(R.string.text_subject_parameter_10);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_11:
                return res.getString(R.string.text_subject_parameter_11);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_12:
                return res.getString(R.string.text_subject_parameter_12);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_13:
                return res.getString(R.string.text_subject_parameter_13);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_14:
                return res.getString(R.string.text_subject_parameter_14);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_15:
                return res.getString(R.string.text_subject_parameter_15);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_16:
                return res.getString(R.string.text_subject_parameter_16);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_17:
                return res.getString(R.string.text_subject_parameter_17);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_18:
                return res.getString(R.string.text_subject_parameter_18);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_19:
                return res.getString(R.string.text_subject_parameter_19);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_20:
                return res.getString(R.string.text_subject_parameter_20);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_21:
                return res.getString(R.string.text_subject_parameter_21);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_22:
                return res.getString(R.string.text_subject_parameter_22);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_23:
                return res.getString(R.string.text_subject_parameter_23);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_24:
                return res.getString(R.string.text_subject_parameter_24);
            case Constants.Specific.SEARCH_SUBJECT_TYPE_25:
                return res.getString(R.string.text_subject_parameter_25);
            default:
                return res.getString(R.string.text_subject_parameter_1);
        }
    }
    
    public static String getParsingResultString(PARSING_RESULT result) {
        if (result == PARSING_RESULT.SUCCESS_FULL_BOARD) {
            return "SUCCESS_FULL_BOARD";
        } else if (result == PARSING_RESULT.SUCCESS_MOBILE_BOARD) {
            return "SUCCESS_MOBILE_BOARD";
        } else if (result == PARSING_RESULT.SUCCESS_MOBILE_TODAY_BEST) {
            return "SUCCESS_MOBILE_TODAY_BEST";
        } else if (result == PARSING_RESULT.FAILED_IO_EXCEPTION) {
            return "FAILED_IO_EXCEPTION";
        } else if (result == PARSING_RESULT.FAILED_JSON_EXCEPTION) {
            return "FAILED_JSON_EXCEPTION";
        } else if (result == PARSING_RESULT.FAILED_STACK_OVERFLOW) {
            return "FAILED_STACK_OVERFLOW";
        } else if (result == PARSING_RESULT.FAILED_UNKNOWN) {
            return "FAILED_UNKNOWN";
        } else {
            return null;
        }
    }
    
    public static String getInternetConnectedResult(INTERNET_CONNECTED_RESULT result) {
        if (result == INTERNET_CONNECTED_RESULT.SUCCESS_WIFI) {
            return "SUCCESS_WIFI";
        } else if (result == INTERNET_CONNECTED_RESULT.SUCCESS_BLUETOOTH) {
            return "SUCCESS_BLUETOOTH";
        } else if (result == INTERNET_CONNECTED_RESULT.SUCCESS_MOBILE) {
            return "SUCCESS_MOBILE";
        } else if (result == INTERNET_CONNECTED_RESULT.FAILED) {
            return "FAILED";
        } else {
            return null;
        }
    }
}
