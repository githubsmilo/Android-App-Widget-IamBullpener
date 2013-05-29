
package com.smilo.bullpen;

public final class Constants {

    public static final String PACKAGE_NAME = "com.smilo.bullpen";

    // Actions predefined by Google
    public static final String ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String ACTION_APPWIDGET_DISABLED = "android.appwidget.action.APPWIDGET_DISABLED";
    
    // Actions defined by BaseballWidget
    public static final String ACTION_SHOW_LIST = PACKAGE_NAME + ".SHOW_LIST";
    public static final String ACTION_SHOW_ITEM = PACKAGE_NAME + ".SHOW_ITEM";
    public static final String ACTION_UPDATE_URL = PACKAGE_NAME + ".UPDATE_URL";
    public static final String EXTRA_ITEM_URL = PACKAGE_NAME + ".EXTRA_ITEM_URL";

    public static final int WIDGET_UPDATE_INTERVAL_AT_MILLIS = 20000;
    public static final int LISTVIEW_MAX_ITEM_COUNT = 10;

    public static final String mMLBParkUrl_base = "http://mlbpark.donga.com";
    public static final String mMLBParkUrl_mlbtown = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=mlbtown";
    public static final String mMLBParkUrl_bullpen = "http://mlbpark.donga.com/mbs/articleL.php?mbsC=bullpen";
}
