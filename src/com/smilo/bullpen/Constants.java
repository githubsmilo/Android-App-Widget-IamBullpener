
package com.smilo.bullpen;

public final class Constants {

    public static final String PACKAGE_NAME = "com.smilo.bullpen";

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
    public static final String EXTRA_PERMIT_MOBILE_CONNECTION_TYPE = PACKAGE_NAME + ".PERMIT_MOBILE_CONNECTION";
    public static final String EXTRA_REFRESH_TIME_TYPE = PACKAGE_NAME + ".REFRESH_TIME_TYPE";
    public static final String EXTRA_BULLPEN_BOARD_TYPE = PACKAGE_NAME + ".BULLPEN_BOARD_TYPE";
    public static final String EXTRA_ITEM_URL = PACKAGE_NAME + ".EXTRA_ITEM_URL";
    public static final String EXTRA_LIST_URL = PACKAGE_NAME + ".EXTRA_LIST_URL"; 
    public static final String EXTRA_PAGE_NUM = PACKAGE_NAME + ".EXTRA_PAGE_NUM";

    public static final int DEFAULT_INTERVAL_AT_MILLIS = 20000;
    public static final int LISTVIEW_MAX_ITEM_COUNT = 20;
    public static final int DEFAULT_PAGE_NUM = 1;

    public static final String mMLBParkUrl_base = "http://mlbpark.donga.com";
    public static final String mMLBParkUrl_mlbtown = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=mlbtown&cpage=";
    public static final String mMLBParkUrl_kbotown = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=kbotown&cpage=";
    public static final String mMLBParkUrl_bullpen = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&cpage=";
    public static final String mMLBParkUrl_mlbtown_todaybest = mMLBParkUrl_base + "/MLBTOWN_TODAYBEST";
    public static final String mMLBParkUrl_kbotown_todaybest = mMLBParkUrl_base + "/KBOTOWN_TODAYBEST";
    public static final String mMLBParkUrl_bullpen_todaybest = mMLBParkUrl_base + "/BULLPEN_TODAYBEST";
    public static final String mMLBParkUrl_bullpen1000 = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&mbsW=search&select=hit&opt=1&keyword=1000&cpage=";
    public static final String mMLBParkUrl_bullpen2000 = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen&mbsW=search&select=hit&opt=1&keyword=2000&cpage=";
    
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
