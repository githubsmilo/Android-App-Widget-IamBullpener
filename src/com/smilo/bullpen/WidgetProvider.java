
package com.smilo.bullpen;

import java.io.UnsupportedEncodingException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.smilo.bullpen.Constants.INTERNET_CONNECTED_RESULT;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetProvider";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    // the pending intent to broadcast alarm.
    private static PendingIntent mSender;
    
    // the alarm manager to refresh app widget periodically.
    private static AlarmManager mManager;

    // Flag to skip notifyAppWidgetViewDataChanged() call on boot.
    private static boolean mIsSkipFirstCallListViewService = true;
    private static boolean mIsSkipFirstCallContentService = true;
    
    // Keys for SharedPreferences.
    private static final String mSharedPreferenceName = Constants.Specific.PACKAGE_NAME;
    private static final String mKeyCompleteToSetup = "key_complete_to_setup";
    private static final String mKeyPermitMobileConnectionType = "key_permit_mobile_connection_type";
    private static final String mKeyRefreshTimeType = "key_refresh_time_type";
    private static final String mKeyBoardType = "key_board_type";
    private static final String mKeyBlackList = "key_black_list";
    
    private static enum PENDING_INTENT_REQUEST_CODE {
        REQUEST_TOP,
        REQUEST_PREV,
        REQUEST_NEXT,
        REQUEST_SEARCH,
        REQUEST_REFRESH,
        REQUEST_SETTING,
        REQUEST_EXPORT,
        REQUEST_UNKNOWN,
    };
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        
        // get intent items.
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
        int searchCategoryType = intent.getIntExtra(
                Constants.EXTRA_SEARCH_CATEGORY_TYPE, Constants.DEFAULT_SEARCH_CATEGORY_TYPE);
        int searchSubjectType = intent.getIntExtra(
                Constants.EXTRA_SEARCH_SUBJECT_TYPE, Constants.DEFAULT_SEARCH_SUBJECT_TYPE);
        String searchKeyword = intent.getStringExtra(Constants.EXTRA_SEARCH_KEYWORD);
        
        // Create intentItem instance.
        intentItem item = new intentItem(
                appWidgetId, pageNum, boardType, refreshTimeType, permitMobileConnectionType, blackList,
                searchCategoryType, searchSubjectType, searchKeyword);
        
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));
        
        if (DEBUG) Log.d(TAG, "onReceive - action[" + action + "], appWidgetsNum[" + appWidgetIds.length +
                "], intentItem[" + item.toString() + "]");
        
        for (int i = 0 ; i < appWidgetIds.length ; i++) {
            if (DEBUG) Log.d(TAG, "onReceive - current appWidgetId[" + appWidgetIds[i] + "]");
            
            if (appWidgetId == appWidgetIds[i]) {

                // After setting configuration activity, this intent will be called.
                if (action.equals(Constants.ACTION_INIT_LIST)) {
                    removePreviousAlarm();
                    
                    // Save configuration info.
                    SharedPreferences pref = context.getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(mKeyCompleteToSetup, true);
                    editor.putInt(mKeyBoardType, boardType);
                    editor.putInt(mKeyRefreshTimeType, refreshTimeType);
                    editor.putBoolean(mKeyPermitMobileConnectionType, permitMobileConnectionType);
                    editor.putString(mKeyBlackList, blackList);
                    editor.commit();

                    // Send broadcast intent to update some variables on the ListViewFactory.
                    context.sendBroadcast(buildUpdateListInfoIntent(item));
                    
                	// Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(buildShowListIntent(context, item));
                    
                // After setting search activity, this intent will be called.
                } else if ((action.equals(Constants.ACTION_REFRESH_LIST)) ||
                        (action.equals(Constants.ACTION_SEARCH))) {    
                    removePreviousAlarm();
                    
                    // Send broadcast intent to update some variables on the ListViewFactory.
                    context.sendBroadcast(buildUpdateListInfoIntent(item));
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(buildShowListIntent(context, item));
 
                // This intent(ACTION_APPWIDGET_UPDATE) will be called periodically.
                // This intent(ACTION_SHOW_LIST) will be called when current item pressed.
                } else if ((action.equals(Constants.ACTION_APPWIDGET_UPDATE)) ||
                                    (action.equals(Constants.ACTION_SHOW_LIST))) {
                	// Check which the internet is connected or not.
                    INTERNET_CONNECTED_RESULT result = Utils.isInternetConnected(context, permitMobileConnectionType);

                    // Set proper remote view according to the result.
                    refreshAlarmSetting(context, item, result);
                    if (result == INTERNET_CONNECTED_RESULT.FAILED)
                    	setRemoteViewToShowLostInternetConnection(context, awm, item);
                    else
                        setRemoteViewToShowList(context, awm, item);

                // This intent will be called when some item selected.
                // EXTRA_ITEM_URL was already filled in the ListViewFactory - getViewAt().
                } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                    removePreviousAlarm();
                    
                    String selectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);

                    // Send broadcast intent to update some variables on the ContentsFactory.
                    context.sendBroadcast(buildUpdateItemInfoIntent(item, selectedItemUrl));
                    
                    // Check which internet is connected or net.
                    INTERNET_CONNECTED_RESULT result = Utils.isInternetConnected(context, permitMobileConnectionType);
                    
                    // Set proper remote view according to the result.
                    if (result == INTERNET_CONNECTED_RESULT.FAILED)
                    	setRemoteViewToShowLostInternetConnection(context, awm, item);
                    else
                        setRemoteViewToShowItem(context, awm, item, selectedItemUrl);
                }
            }
        }
    }

    private void setRemoteViewToShowLostInternetConnection(Context context, AppWidgetManager awm, intentItem item) {
    	if (DEBUG) Log.d(TAG, "setRemoteViewToShowLostInternetConnection - intentItem[" + item.toString() + "]");
    	
    	PendingIntent pi = null;

        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.lost_internet_connection);
    	
    	rv.setTextViewText(R.id.textLostInternetTitle, context.getResources().getText(R.string.text_lost_internet_connection));

        // Set refresh button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(),
                buildRefreshListIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnLostInternetRefresh, pi);
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(),
                buildConfigurationActivityIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnLostInternetSetting, pi);
        
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowLostInternetConnection - updateAppWidget [LostInternetConnection]");
        awm.updateAppWidget(item.getAppWidgetId(), rv);
    }
    
    private void setRemoteViewToShowList(Context context, AppWidgetManager awm, intentItem item) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - intentItem[" + item.toString() + "]");
        
        PendingIntent pi = null;

    	// Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
    	
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = buildListViewServiceIntent(context, item);
        rv.setRemoteAdapter(R.id.listView, serviceIntent); // For API14+
        //rv.setRemoteAdapter(item.getAppWidgetId(), R.id.listView, serviceIntent); // For API13-
        rv.setScrollPosition(R.id.listView, 0); // Scroll to top

        if (Utils.isTodayBestBoardType(item.getBoardType())) {
            // Set title of the remoteViews.
            rv.setTextViewText(R.id.textListTitle, Utils.getBoardTitle(context, item.getBoardType()));
            
            rv.setViewVisibility(R.id.btnListNavTop, View.GONE);
            rv.setViewVisibility(R.id.btnListNavPrev, View.GONE);
            rv.setViewVisibility(R.id.btnListNavNext, View.GONE);
            rv.setViewVisibility(R.id.btnListSearch, View.GONE);
        } else {
            // Save pageNum.
            int currentPageNum = item.getPageNum();
            
            // Set title of the remoteViews.
            if (item.getSearchCategoryType() == Constants.DEFAULT_SEARCH_CATEGORY_TYPE)
                rv.setTextViewText(R.id.textListTitle, (Utils.getBoardTitle(context, item.getBoardType()) + " - " + currentPageNum));
            else if (item.getSearchCategoryType() == Constants.Specific.SEARCH_CATEGORY_TYPE_SUBJECT)
                rv.setTextViewText(R.id.textListTitle, (Utils.getBoardTitle(context, item.getBoardType()) + " - " + currentPageNum +
                        " [" + Utils.getSubjectTitle(context, item.getSearchSubjectType()) + "]"));
            else
                rv.setTextViewText(R.id.textListTitle, (Utils.getBoardTitle(context, item.getBoardType()) + " - " + currentPageNum + 
                        " [" + item.getSearchKeyword() + "]"));
            
            // Set top button of the removeViews.
            rv.setViewVisibility(R.id.btnListNavTop, View.VISIBLE);
            item.setPageNum(Constants.DEFAULT_PAGE_NUM);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_TOP.ordinal(),
                    buildRefreshListIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
              rv.setOnClickPendingIntent(R.id.btnListNavTop, pi);
              
            // Set prev button of the removeViews.
            rv.setViewVisibility(R.id.btnListNavPrev, View.VISIBLE);
            if (currentPageNum > Constants.DEFAULT_PAGE_NUM)
                item.setPageNum(currentPageNum - 1);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_PREV.ordinal(),
                    buildRefreshListIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListNavPrev, pi);

            // Set next button of the remoteViews.
            rv.setViewVisibility(R.id.btnListNavNext, View.VISIBLE);
            item.setPageNum(currentPageNum + 1);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_NEXT.ordinal(),
                    buildRefreshListIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListNavNext, pi);
            
            // Restore pageNum to the intent item.
            item.setPageNum(currentPageNum);

            // Set search button of the remoteViews.
            if (Utils.isPredefinedBoardType(item.getBoardType())) {
            	rv.setViewVisibility(R.id.btnListSearch, View.GONE);
            } else {
	            rv.setViewVisibility(R.id.btnListSearch, View.VISIBLE);
	            pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SEARCH.ordinal(), 
	                    buildSearchActivityIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
	            rv.setOnClickPendingIntent(R.id.btnListSearch, pi);
            }
        }
        
        // Set export button of the remoteViews.
        rv.setViewVisibility(R.id.btnListExport, View.VISIBLE);
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_EXPORT.ordinal(),
                buildExportIntent(context, item, null), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnListExport, pi);
        
        // Set refresh button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(),
                buildRefreshListIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnListRefresh, pi);
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(),
                buildConfigurationActivityIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnListSetting, pi);
        
        // Set a pending intent for click event to the remoteViews.
        PendingIntent clickPi = PendingIntent.getBroadcast(context, 0, 
        		buildShowItemIntent(context, item, null), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.listView, clickPi);
        
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - updateAppWidget [BaseballListViewService]");
        awm.updateAppWidget(item.getAppWidgetId(), rv);
        
        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after ListViewFactory is created.
        if (mIsSkipFirstCallListViewService) {
            mIsSkipFirstCallListViewService = false;
        } else {
            if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - notifyAppWidgetViewDataChanged [BaseballListViewService]");
            awm.notifyAppWidgetViewDataChanged(item.getAppWidgetId(), R.id.listView);
        }
    }
    
    private void setRemoteViewToShowItem(Context context, AppWidgetManager awm, intentItem item, String selectedItemUrl) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - intentItem[" + item.toString() + "], selectedItemUrl[" + selectedItemUrl + "]");
        
        PendingIntent pi = null;
        
        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
        
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = buildContentServiceIntent(context, item, selectedItemUrl);
        rv.setRemoteAdapter(R.id.contentView, serviceIntent); // For API14+
        //rv.setRemoteAdapter(item.getAppWidgetId(), R.id.contentView, serviceIntent); // For API13-

        // Set title of the remoteViews.
        rv.setTextViewText(R.id.textContentTitle, Utils.getBoardTitle(context, item.getBoardType()));

        // Set export button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_EXPORT.ordinal(),
                buildExportIntent(context, item, selectedItemUrl), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentExport, pi);
        
        // Set top button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_TOP.ordinal(),
                                        buildRefreshListIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentNavTop, pi);
        
        // Set refresh button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(), 
                                        buildShowItemIntent(context, item, selectedItemUrl), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentRefresh, pi);
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(), 
                                       buildConfigurationActivityIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentSetting, pi);
        
        // Set a pending intent for click event to the remoteViews.
        PendingIntent clickPi = PendingIntent.getBroadcast(context, 0, 
                                       buildShowListIntent(context, item), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.contentView, clickPi);
    
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - updateAppWidget [BaseballContentService]");
        awm.updateAppWidget(item.getAppWidgetId(), rv);

        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after ContentsFactory is created.
        if (mIsSkipFirstCallContentService) {
            mIsSkipFirstCallContentService = false;
        } else {
            if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - notifyAppWidgetViewDataChanged [BaseballContentService]");
            awm.notifyAppWidgetViewDataChanged(item.getAppWidgetId(), R.id.contentView);
        }
    }
    
    private void refreshAlarmSetting(Context context, intentItem item, INTERNET_CONNECTED_RESULT result) {
        // If user does not want to refresh, just remove alarm setting.
    	// TODO : Consider INTERNET_CONNECTED_RESULT case here?
        if (item.getRefreshTimeType() == Constants.Specific.REFRESH_TIME_TYPE_STOP) {
            removePreviousAlarm();
            
        // If user wants to refresh, set new alarm.
        } else {
            removePreviousAlarm();
            setNewAlarm(context, item, false);
        }
    }
    
    private void setNewAlarm(Context context, intentItem item, boolean isUrgentMode) {
        if (DEBUG) Log.d(TAG, "setNewAlarm - intentItem[" + item.toString() + "], isUrgentMode[" + isUrgentMode + "]");

        Resources res = context.getResources();
        int selectedRefreshTime = Utils.getRefreshTime(context, item.getRefreshTimeType());
        long alarmTime = System.currentTimeMillis() + (selectedRefreshTime <= 0 ? res.getInteger(R.integer.int_default_interval) : selectedRefreshTime);
        if (isUrgentMode) alarmTime = 0;
        mSender = PendingIntent.getBroadcast(context, 0, buildWidgetUpdateIntent(context, item), 0);
        mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mManager.set(AlarmManager.RTC, alarmTime, mSender);
    }

    private void removePreviousAlarm() {
        if (DEBUG) Log.d(TAG, "removePreviousAlarm");

        if (mManager != null && mSender != null) {
            mSender.cancel();
            mManager.cancel(mSender);
        }
    }

    private Intent buildBaseIntent(intentItem item) {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, item.getAppWidgetId());
        intent.putExtra(Constants.EXTRA_PAGE_NUM, item.getPageNum());
        intent.putExtra(Constants.EXTRA_BOARD_TYPE, item.getBoardType());
        intent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, item.getRefreshTimeType());
        intent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, item.getPermitMobileConnectionType());
        intent.putExtra(Constants.EXTRA_BLACK_LIST, item.getBlackList());
        intent.putExtra(Constants.EXTRA_SEARCH_CATEGORY_TYPE, item.getSearchCategoryType());
        intent.putExtra(Constants.EXTRA_SEARCH_SUBJECT_TYPE, item.getSearchSubjectType());
        String searchKeyword = item.getSearchKeyword();
        if (searchKeyword != null && searchKeyword.length() > 0)
            intent.putExtra(Constants.EXTRA_SEARCH_KEYWORD, searchKeyword);
        
        return intent;
    }
    
    private Intent buildUpdateListInfoIntent(intentItem item) {
        Intent intent = buildBaseIntent(item);
        intent.setAction(Constants.ACTION_UPDATE_LIST_INFO);
        
        return intent;
    }
    
    private Intent buildUpdateItemInfoIntent(intentItem item, String selectedItemUrl) {
        Intent intent = buildBaseIntent(item);
        intent.setAction(Constants.ACTION_UPDATE_ITEM_INFO);
        if (selectedItemUrl != null && selectedItemUrl.length() > 0)
            intent.putExtra(Constants.EXTRA_ITEM_URL, selectedItemUrl);
        
        return intent;
    }
    
    private Intent buildRefreshListIntent(Context context, intentItem item) {
        Intent intent = buildBaseIntent(item);
        intent.setClass(context, WidgetProvider.class);
        intent.setAction(Constants.ACTION_REFRESH_LIST);
        
        return intent;
    }
    
    private Intent buildShowListIntent(Context context, intentItem intentItem) {
        Intent intent = buildBaseIntent(intentItem);
        intent.setClass(context, WidgetProvider.class);
        intent.setAction(Constants.ACTION_SHOW_LIST);

        return intent;
    }
    
    private Intent buildShowItemIntent(Context context, intentItem item, String selectedItemUrl) {
        Intent intent = buildBaseIntent(item);
        intent.setClass(context, WidgetProvider.class);  
        intent.setAction(Constants.ACTION_SHOW_ITEM);
        if (selectedItemUrl != null)
            intent.putExtra(Constants.EXTRA_ITEM_URL, selectedItemUrl);
        
        return intent;
    }
    
    private Intent buildConfigurationActivityIntent(Context context, intentItem item) {
        Intent intent = buildBaseIntent(item);
        intent.setClass(context, ConfigurationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }
    
    private Intent buildSearchActivityIntent(Context context, intentItem item) {
        Intent intent = buildBaseIntent(item);
        intent.setClass(context, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }
    
    private Intent buildListViewServiceIntent(Context context, intentItem intentItem) {
        Intent intent = buildBaseIntent(intentItem);
        intent.setClass(context, ListViewService.class);
        
        return intent;
    }
    
    private Intent buildContentServiceIntent(Context context, intentItem item, String selectedItemUrl) {
        Intent intent = buildBaseIntent(item);
        intent.setClass(context, ContentsService.class);
        intent.putExtra(Constants.EXTRA_ITEM_URL, selectedItemUrl);
        
        return intent;
    }
    
    private Intent buildWidgetUpdateIntent(Context context, intentItem item) {
        Intent intent = buildBaseIntent(item);
        intent.setClass(context, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        
        return intent;
    }
    
    private Intent buildExportIntent(Context context, intentItem item, String selectedItemUrl) {
        String url = null;
        
        if (selectedItemUrl == null) {
	        try {
	            if (Utils.isTodayBestBoardType(item.getBoardType())) {
	                url = Constants.Specific.URL_BASE;
	            } else {
	                url = Utils.getMobileBoardUrl(context, item.getPageNum(), item.getBoardType(), 
	                        item.getSearchCategoryType(), item.getSearchSubjectType(), item.getSearchKeyword());
	            }
	        } catch (UnsupportedEncodingException e) {
	            if (DEBUG) Log.e(TAG, "buildExportIntent - UnsupportedEncodingException![" + e.toString() + "]");
	            e.printStackTrace();
	        }
        } else {
        	url = selectedItemUrl;
        }

        Intent intent = new Intent();
        intent.setClass(context, WebViewActivity.class);
        intent.putExtra(Constants.EXTRA_EXPORT_URL, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return intent;
    }

    public static void removeWidget(Context context, int appWidgetId) {
        AppWidgetHost host = new AppWidgetHost(context, 1);
        host.deleteAppWidgetId(appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager awm, int[] appWidgetIds) {
        if (DEBUG) Log.d(TAG, "onUpdate");
        super.onUpdate(context, awm, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (DEBUG) Log.d(TAG, "onDeleted");
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        if (DEBUG) Log.d(TAG, "onDisabled");
        removePreviousAlarm();
        
        // Delete all saved data.
        SharedPreferences pref = context.getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        if (DEBUG) Log.d(TAG, "onEnabled");
        removePreviousAlarm();

        // Initialize global variables.
        mIsSkipFirstCallListViewService = true;
        mIsSkipFirstCallContentService = true;
        
        // Load configuration info.
        SharedPreferences pref = context.getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
        boolean isCompleteToSetup = pref.getBoolean(mKeyCompleteToSetup, false);
        
        // If completed to setup already, update current widget.
        if (isCompleteToSetup) {
            int boardType = pref.getInt(mKeyBoardType, Constants.DEFAULT_BOARD_TYPE);
            int refreshTimeType = pref.getInt(mKeyRefreshTimeType, Constants.DEFAULT_REFRESH_TIME_TYPE);
            boolean permitMobileConnectionType = pref.getBoolean(mKeyPermitMobileConnectionType, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
            String blackList = pref.getString(mKeyBlackList, Constants.DEFAULT_BLACK_LIST);

            // Set urgent alarm to update widget as soon as possible.
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));

            for (int i = 0 ; i < appWidgetIds.length ; i++) {
                intentItem item = new intentItem(appWidgetIds[i], Constants.DEFAULT_PAGE_NUM,
                        boardType, refreshTimeType, permitMobileConnectionType, blackList,
                        Constants.DEFAULT_SEARCH_CATEGORY_TYPE, Constants.DEFAULT_SEARCH_SUBJECT_TYPE, null);
                
                setNewAlarm(context, item, true);
            }
        }
        
        super.onEnabled(context);
    }

    private static class intentItem {
        int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        int pageNumber = Constants.DEFAULT_PAGE_NUM;
        int boardType = Constants.DEFAULT_BOARD_TYPE;
        int refreshType = Constants.DEFAULT_REFRESH_TIME_TYPE;
        boolean isPermitMobileConnection = Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE;
        String blackList = Constants.DEFAULT_BLACK_LIST;
        int searchCategoryType = Constants.DEFAULT_SEARCH_CATEGORY_TYPE;
        int searchSubjectType = Constants.DEFAULT_SEARCH_SUBJECT_TYPE;
        String searchKeyword = null;
        
        intentItem(int widgetId, int pageNumber, int boardType, int refreshType, boolean isPermitMobileConnection,
                String blackList, int searchCategoryType, int searchSubjectType, String searchKeyword) {
            this.widgetId = widgetId;
            this.pageNumber = pageNumber;
            this.boardType = boardType;
            this.refreshType = refreshType;
            this.isPermitMobileConnection = isPermitMobileConnection;
            this.blackList = blackList;
            this.searchCategoryType = searchCategoryType;
            this.searchSubjectType = searchSubjectType;
            this.searchKeyword = searchKeyword;
        }
        
        int getAppWidgetId() {
            return widgetId;
        }
        
        int getPageNum() {
            return pageNumber;
        }
        
        int getBoardType() {
            return boardType;
        }
        
        int getRefreshTimeType() {
            return refreshType;
        }
        
        boolean getPermitMobileConnectionType() {
            return isPermitMobileConnection;
        }
        
        String getBlackList() {
            return blackList;
        }
        
        int getSearchCategoryType() {
            return searchCategoryType;
        }
        
        int getSearchSubjectType() {
            return searchSubjectType;
        }
        
        String getSearchKeyword() {
            return searchKeyword;
        }
        
        void setPageNum(int pageNum) {
            pageNumber = pageNum;
        }
        
        public String toString() {
            return ("appWidgetId[" + widgetId + "], pageNum[" + pageNumber + "], boardType[" + boardType +
                    "], refreshTimeType[" + refreshType + "], isPermitMobileConnectionType[" + isPermitMobileConnection +
                    "], blackList[" + blackList + "], searchCategoryType[" + searchCategoryType +
                    "], searchSubjectType[" + searchSubjectType + "], searchKeyword[" + searchKeyword + "]");
        }
    }
}
