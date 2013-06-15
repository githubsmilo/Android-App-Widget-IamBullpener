
package com.smilo.bullpen;

public final class Constants {

    public static final String PACKAGE_NAME = "com.smilo.bullpen";

    public static final boolean DEBUG_MODE = true;
    
    // Actions predefined by Google
    public static final String ACTION_APPWIDGET_ENABLD = "android.appwidget.action.APPWIDGET_ENABLED";
    public static final String ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    
    // Actions defined by BaseballWidget
    public static final String ACTION_INIT_LIST = PACKAGE_NAME + ".INIT_LIST";
    public static final String ACTION_REFRESH_LIST = PACKAGE_NAME + ".REFRESH_LIST";
    public static final String ACTION_SHOW_LIST = PACKAGE_NAME + ".SHOW_LIST";
    public static final String ACTION_SHOW_ITEM = PACKAGE_NAME + ".SHOW_ITEM";
    public static final String ACTION_UPDATE_ITEM_URL = PACKAGE_NAME + ".UPDATE_ITEM_URL";
    public static final String ACTION_UPDATE_LIST_URL = PACKAGE_NAME + ".UPDATE_LIST_URL";
    public static final String EXTRA_PAGE_NUM = PACKAGE_NAME + ".EXTRA_PAGE_NUM";
    public static final String EXTRA_BOARD_TYPE = PACKAGE_NAME + ".BOARD_TYPE";
    public static final String EXTRA_REFRESH_TIME_TYPE = PACKAGE_NAME + ".REFRESH_TIME_TYPE";
    public static final String EXTRA_PERMIT_MOBILE_CONNECTION_TYPE = PACKAGE_NAME + ".PERMIT_MOBILE_CONNECTION";
    public static final String EXTRA_ITEM_URL = PACKAGE_NAME + ".EXTRA_ITEM_URL";
    
    public static final int LISTVIEW_MAX_ITEM_COUNT = 20;
    public static final int BITMAP_MAX_SIZE = 800;
    
    public static final int     DEFAULT_PAGE_NUM = 1;
    public static final boolean DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE = false;
    public static final int     DEFAULT_REFRESH_TIME_TYPE = 0;
    public static final int     DEFAULT_BOARD_TYPE = 0;
    
    public static final int     ERROR_PAGE_NUM = -1;
    public static final boolean ERROR_PERMIT_MOBILE_CONNECTION_TYPE = false;
    public static final int     ERROR_REFRESH_TIME_TYPE = -1;
    public static final int     ERROR_BOARD_TYPE = -1;
    
    public static final String URL_BASE = "http://mlbpark.donga.com";
    public static final String URL_MLB_TOWN = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=mlbtown&cpage=";
    public static final String URL_KBO_TOWN = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=kbotown&cpage=";
    public static final String URL_BULLPEN = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&cpage=";
    public static final String URL_MLB_TOWN_TODAY_BEST = URL_BASE + "/MLBTOWN_TODAYBEST";
    public static final String URL_KBO_TOWN_TODAY_BEST = URL_BASE + "/KBOTOWN_TODAYBEST";
    public static final String URL_BULLPEN_TODAY_BEST = URL_BASE + "/BULLPEN_TODAYBEST";
    public static final String URL_BULLPEN_1000 = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&mbsW=search&select=hit&opt=1&keyword=1000&cpage=";
    public static final String URL_BULLPEN_2000 = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&mbsW=search&select=hit&opt=1&keyword=2000&cpage=";
    
    public static final int BOARD_TYPE_MLB_TOWN = 0;
    public static final int BOARD_TYPE_KBO_TOWN = 1;
    public static final int BOARD_TYPE_BULLPEN  = 2;
    public static final int BOARD_TYPE_MLB_TOWN_TODAY_BEST = 3;
    public static final int BOARD_TYPE_KBO_TOWN_TODAY_BEST = 4;
    public static final int BOARD_TYPE_BULLPEN_TODAY_BEST  = 5;
    public static final int BOARD_TYPE_BULLPEN_1000 = 6;
    public static final int BOARD_TYPE_BULLPEN_2000 = 7;
    
    public static final int DEFAULT_INTERVAL = 20000;
    public static final int TIME_1_MIN  = 60000;
    public static final int TIME_5_MIN  = 60000 * 5;
    public static final int TIME_10_MIN = 60000 * 10;
    public static final int TIME_20_MIN = 60000 * 20;
    public static final int TIME_30_MIN = 60000 * 30;
    public static final int TIME_STOP   = -1;
    
    public static final int REFRESH_TIME_TYPE_1_MIN  = 0;
    public static final int REFRESH_TIME_TYPE_5_MIN  = 1;
    public static final int REFRESH_TIME_TYPE_10_MIN = 2;
    public static final int REFRESH_TIME_TYPE_20_MIN = 3;
    public static final int REFRESH_TIME_TYPE_30_MIN = 4;
    public static final int REFRESH_TIME_TYPE_STOP   = 5;

    public enum PARSING_RESULT {
    	SUCCESS_FULL_BOARD,
        SUCCESS_MOBILE_BOARD,
        SUCCESS_MOBILE_TODAY_BEST,
        FAILED_IO_EXCEPTION,
        FAILED_JSON_EXCEPTION,
        FAILED_STACK_OVERFLOW,
        FAILED_UNKNOWN,
    };
}
