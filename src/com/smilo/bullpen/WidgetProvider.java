
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
import com.smilo.bullpen.activities.AddToBlacklistActivity;
import com.smilo.bullpen.activities.ConfigurationActivity;
import com.smilo.bullpen.activities.SearchActivity;
import com.smilo.bullpen.activities.WebViewActivity;
import com.smilo.bullpen.services.ContentsService;
import com.smilo.bullpen.services.ListViewService;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetProvider";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    public static final String WIDGET_PROVIDER_CLASS_NAME = Constants.Specific.PACKAGE_NAME + "." + TAG;
    
    // the pending intent to broadcast alarm.
    private static PendingIntent mSender;
    
    // the alarm manager to refresh app widget periodically.
    private static AlarmManager mManager;

    // Flag to skip notifyAppWidgetViewDataChanged() call on boot.
    private static boolean mIsSkipFirstCallListViewService = true;
    private static boolean mIsSkipFirstCallContentService = true;
    
    private static enum PENDING_INTENT_REQUEST_CODE {
        REQUEST_TOP,
        REQUEST_PREV,
        REQUEST_NEXT,
        REQUEST_SEARCH,
        REQUEST_REFRESH,
        REQUEST_SETTING,
        REQUEST_EXPORT,
        REQUEST_ADDTOBLACKLIST,
        REQUEST_UNKNOWN,
    };
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Get ExtraItems.
        String action = intent.getAction();
        ExtraItems item = Utils.createExtraItemsFromIntent(intent);
        
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));
        if (DEBUG) Log.d(TAG, "onReceive - action[" + action + "], appWidgetsNum[" + appWidgetIds.length +
                "], ExtraItems[" + item.toString() + "]");

        for (int i = 0 ; i < appWidgetIds.length ; i++) {
            if (DEBUG) Log.d(TAG, "onReceive - current appWidgetId[" + appWidgetIds[i] + "]");
            
            if (item.getAppWidgetId() == appWidgetIds[i]) {

                // After setting configuration activity, this intent will be called.
                if (action.equals(Constants.ACTION_INIT_LIST)) {
                    removePreviousAlarm();
                    
                    // Save configuration info.
                    saveIntentItem(context, item);

                    // Send broadcast intent to update some variables on the ListViewFactory.
                    context.sendBroadcast(buildUpdateListInfoIntent(context, item));
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(buildShowListIntent(context, item));

                // After setting search activity, this intent will be called.
                } else if ((action.equals(Constants.ACTION_REFRESH_LIST)) ||
                        (action.equals(Constants.ACTION_SEARCH))) {    
                    removePreviousAlarm();
                    
                    // Send broadcast intent to update some variables on the ListViewFactory.
                    context.sendBroadcast(buildUpdateListInfoIntent(context, item));
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(buildShowListIntent(context, item));
 
                // This intent(ACTION_APPWIDGET_UPDATE) will be called periodically.
                // This intent(ACTION_SHOW_LIST) will be called when current item pressed.
                } else if ((action.equals(Constants.ACTION_APPWIDGET_UPDATE)) ||
                                    (action.equals(Constants.ACTION_SHOW_LIST))) {
                    
                    // Check which the internet is connected or not.
                    INTERNET_CONNECTED_RESULT result = Utils.isInternetConnected(context, item.getPermitMobileConnectionType());

                    refreshAlarmSetting(context, item, result);
                    
                    // Set proper remote view according to the result.
                    if (result == INTERNET_CONNECTED_RESULT.FAILED)
                        setRemoteViewToShowLostInternetConnection(context, awm, item);
                    else
                        setRemoteViewToShowList(context, awm, item);
                    
                    // Save configuration info.
                      saveIntentItem(context, item);

                // This intent will be called when some item selected.
                // EXTRA_ITEM_URL was already filled in the ListViewFactory - getViewAt().
                } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                    removePreviousAlarm();
                    
                    String selectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
                    String selectedItemWriter = intent.getStringExtra(Constants.EXTRA_ITEM_WRITER);

                    // Send broadcast intent to update some variables on the ContentsFactory.
                    context.sendBroadcast(buildUpdateItemInfoIntent(context, item, selectedItemUrl, selectedItemWriter));
                    
                    // Check which internet is connected or net.
                    INTERNET_CONNECTED_RESULT result = Utils.isInternetConnected(context, item.getPermitMobileConnectionType());
                    
                    // Set proper remote view according to the result.
                    if (result == INTERNET_CONNECTED_RESULT.FAILED)
                        setRemoteViewToShowLostInternetConnection(context, awm, item);
                    else
                        setRemoteViewToShowItem(context, awm, item, selectedItemUrl, selectedItemWriter);
                }
            }
        }
    }

    private void setRemoteViewToShowLostInternetConnection(Context context, AppWidgetManager awm, ExtraItems item) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowLostInternetConnection - ExtraItems[" + item.toString() + "]");
        
        PendingIntent pi = null;

        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.lost_internet_connection);
        
        // Set title of the remoteViews.
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
    
    private void setRemoteViewToShowList(Context context, AppWidgetManager awm, ExtraItems item) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - ExtraItems[" + item.toString() + "]");
        
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
    
    private void setRemoteViewToShowItem(Context context, AppWidgetManager awm, ExtraItems item,
            String selectedItemUrl, String selectedItemWriter) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - ExtraItems[" + item.toString() +
                "], selectedItemUrl[" + selectedItemUrl + "], selectedItemWriter[" + selectedItemWriter + "]");
        
        PendingIntent pi = null;
        
        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
        
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = buildContentServiceIntent(context, item, selectedItemUrl, selectedItemWriter);
        rv.setRemoteAdapter(R.id.contentView, serviceIntent); // For API14+
        //rv.setRemoteAdapter(item.getAppWidgetId(), R.id.contentView, serviceIntent); // For API13-

        // Set title of the remoteViews.
        rv.setTextViewText(R.id.textContentTitle, Utils.getBoardTitle(context, item.getBoardType()));

        // Set export button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_EXPORT.ordinal(),
                buildExportIntent(context, item, selectedItemUrl), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentExport, pi);
        
        // Set addtoblacklist button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_ADDTOBLACKLIST.ordinal(),
                buildAddToBlackListIntent(context, item, selectedItemWriter), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentAddToBlacklist, pi);

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
    
    private void refreshAlarmSetting(Context context, ExtraItems item, INTERNET_CONNECTED_RESULT result) {
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
    
    private void setNewAlarm(Context context, ExtraItems item, boolean isUrgentMode) {
        if (DEBUG) Log.d(TAG, "setNewAlarm");

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

    private Intent buildUpdateListInfoIntent(Context context, ExtraItems item) {
        return Utils.createIntentFromExtraItems(
                context, null, Constants.ACTION_UPDATE_LIST_INFO, item, false);
    }
    
    private Intent buildUpdateItemInfoIntent(Context context, ExtraItems item,
            String selectedItemUrl, String selectedItemWriter) {
        Intent intent = Utils.createIntentFromExtraItems(
                context, null, Constants.ACTION_UPDATE_ITEM_INFO, item, false);
        if (selectedItemUrl != null && selectedItemUrl.length() > 0)
            intent.putExtra(Constants.EXTRA_ITEM_URL, selectedItemUrl);
        if (selectedItemWriter != null && selectedItemWriter.length() >0)
            intent.putExtra(Constants.EXTRA_ITEM_WRITER, selectedItemWriter);
        return intent;
    }
    
    private Intent buildRefreshListIntent(Context context, ExtraItems item) {
        return Utils.createIntentFromExtraItems(
                context, WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_REFRESH_LIST, item, false);
    }
    
    private Intent buildShowListIntent(Context context, ExtraItems item) {
        return Utils.createIntentFromExtraItems(
                context, WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_SHOW_LIST, item, false);
    }
    
    private Intent buildShowItemIntent(Context context, ExtraItems item,
            String selectedItemUrl) {
        Intent intent = Utils.createIntentFromExtraItems(
                context, WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_SHOW_ITEM, item, false);
        if (selectedItemUrl != null && selectedItemUrl.length() > 0)
            intent.putExtra(Constants.EXTRA_ITEM_URL, selectedItemUrl);
        return intent;
    }
    
    private Intent buildConfigurationActivityIntent(Context context, ExtraItems item) {
        return Utils.createIntentFromExtraItems(
                context, ConfigurationActivity.CONFIGURATION_ACTIVITY_CLASS_NAME, null, item, true);
    }
    
    private Intent buildSearchActivityIntent(Context context, ExtraItems item) {
        return Utils.createIntentFromExtraItems(
                context, SearchActivity.SEARCH_ACTIVITY_CLASS_NAME, null, item, true);
    }
    
    private Intent buildAddToBlackListIntent(Context context, ExtraItems item,
            String selectedItemWriter) {
        Intent intent = Utils.createIntentFromExtraItems(
                context, AddToBlacklistActivity.ADDTOBLACKLIST_ACTIVITY_CLASS_NAME, null, item, true);
        if (selectedItemWriter != null && selectedItemWriter.length() > 0)
            intent.putExtra(Constants.EXTRA_ITEM_WRITER, selectedItemWriter);
        return intent;
    }
    
    private Intent buildListViewServiceIntent(Context context, ExtraItems item) {
        return Utils.createIntentFromExtraItems(
                context, ListViewService.LISTVIEW_SERVICE_CLASS_NAME, null, item, false);
    }
    
    private Intent buildContentServiceIntent(Context context, ExtraItems item,
            String selectedItemUrl, String selectedItemWriter) {
        Intent intent = Utils.createIntentFromExtraItems(
                context, ContentsService.CONTENTS_SERVICE_CLASS_NAME, null, item, false);
        if (selectedItemUrl != null && selectedItemUrl.length() > 0)
            intent.putExtra(Constants.EXTRA_ITEM_URL, selectedItemUrl);
        if (selectedItemWriter != null && selectedItemWriter.length() > 0)
            intent.putExtra(Constants.EXTRA_ITEM_WRITER, selectedItemWriter);
        return intent;
    }
    
    private Intent buildWidgetUpdateIntent(Context context, ExtraItems item) {
        return Utils.createIntentFromExtraItems(
                context, WIDGET_PROVIDER_CLASS_NAME, AppWidgetManager.ACTION_APPWIDGET_UPDATE, item, false);
    }
    
    private Intent buildExportIntent(Context context, ExtraItems item, String selectedItemUrl) {
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
        
        Intent intent = Utils.createIntentFromExtraItems(
                context, WebViewActivity.WEBVIEW_ACTIVITY_CLASS_NAME, null, item, true);
        intent.putExtra(Constants.EXTRA_EXPORT_URL, url);

        return intent;
    }

    private void saveIntentItem(Context context, ExtraItems item) {
        if (DEBUG) Log.d(TAG, "saveIntentItem");
        
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.KEY_COMPLETE_TO_SETUP, true);
        editor.putInt(Constants.KEY_BOARD_TYPE, item.getBoardType());
        editor.putInt(Constants.KEY_REFRESH_TIME_TYPE, item.getRefreshTimeType());
        editor.putBoolean(Constants.KEY_PERMIT_MOBILE_CONNECTION_TYPE, item.getPermitMobileConnectionType());
        editor.putString(Constants.KEY_BLACK_LIST, item.getBlackList());
        editor.putString(Constants.KEY_BLOCKED_WORDS, item.getBlockedWords());
        editor.putString(Constants.KEY_SCRAP_LIST, item.getScrapList());
        editor.commit();
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
        removePreviousAlarm();
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        if (DEBUG) Log.d(TAG, "onDisabled");
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
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean isCompleteToSetup = pref.getBoolean(Constants.KEY_COMPLETE_TO_SETUP, false);
        
        // If completed to setup already, update current widget.
        if (isCompleteToSetup) {
            int boardType = pref.getInt(Constants.KEY_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
            int refreshTimeType = pref.getInt(Constants.KEY_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
            boolean permitMobileConnectionType = pref.getBoolean(Constants.KEY_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
            String blackList = pref.getString(Constants.KEY_BLACK_LIST, Constants.DEFAULT_BLACK_LIST);
            String blockedWords = pref.getString(Constants.KEY_BLOCKED_WORDS, Constants.DEFAULT_BLOCKED_WORDS);
            String scrapList = pref.getString(Constants.KEY_SCRAP_LIST, Constants.DEFAULT_SCRAP_LIST);

            // Set urgent alarm to update widget as soon as possible.
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));

            for (int i = 0 ; i < appWidgetIds.length ; i++) {
                ExtraItems item = new ExtraItems(appWidgetIds[i], Constants.DEFAULT_PAGE_NUM,
                        boardType, refreshTimeType, permitMobileConnectionType, blackList, blockedWords, scrapList,
                        Constants.DEFAULT_SEARCH_CATEGORY_TYPE, Constants.DEFAULT_SEARCH_SUBJECT_TYPE, null);
                
                setNewAlarm(context, item, true);
            }
        }
        
        super.onEnabled(context);
    }
}
