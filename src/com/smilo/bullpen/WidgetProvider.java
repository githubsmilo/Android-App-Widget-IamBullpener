
package com.smilo.bullpen;

import com.smilo.bullpen.Constants.INTERNET_CONNECTED_RESULT;
import com.smilo.bullpen.activities.AddToBlacklistActivity;
import com.smilo.bullpen.activities.ConfigurationActivity;
import com.smilo.bullpen.activities.SearchActivity;
import com.smilo.bullpen.activities.WebViewActivity;
import com.smilo.bullpen.db.DatabaseHandler;
import com.smilo.bullpen.services.ContentsService;
import com.smilo.bullpen.services.ListViewService;

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
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

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
        REQUEST_SCRAP,
        REQUEST_DELETESCRAP,
        REQUEST_UNKNOWN,
    };
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Get ExtraItems.
        String action = intent.getAction();
        ExtraItem extraItem = Utils.createExtraItemFromIntent(intent);
        ListItem listItem = Utils.createListItemFromIntent(intent);
        
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));
        if (DEBUG) Log.d(TAG, "onReceive - action[" + action + "], appWidgetsNum[" + appWidgetIds.length +
                "], ExtraItems[" + extraItem.toString() + "], ListItem[" + listItem.toString() + "]");

        for (int i = 0 ; i < appWidgetIds.length ; i++) {
            if (DEBUG) Log.d(TAG, "onReceive - current appWidgetId[" + appWidgetIds[i] + "]");
            
            if (extraItem.getAppWidgetId() == appWidgetIds[i]) {

                // After setting configuration activity, this intent will be called.
                if (action.equals(Constants.ACTION_INIT_LIST)) {
                    removePreviousAlarm();
                    
                    // Save configuration info.
                    saveIntentItem(context, extraItem);

                    // Send broadcast intent to update some variables on the ListViewFactory.
                    context.sendBroadcast(buildUpdateListInfoIntent(context, extraItem));
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(buildShowListIntent(context, extraItem));

                // After setting search activity, this intent will be called.
                } else if (action.equals(Constants.ACTION_REFRESH_LIST)) {
                    removePreviousAlarm();
                    
                    // Send broadcast intent to update some variables on the ListViewFactory.
                    context.sendBroadcast(buildUpdateListInfoIntent(context, extraItem));
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(buildShowListIntent(context, extraItem));
 
                } else if (action.equals(Constants.ACTION_SCRAP_ITEM)) {
                    saveScrappedItem(context, listItem);
                    
                } else if (action.equals(Constants.ACTION_DELETE_SCRAPPED_ITEM)) {
                    deleteScrappedItem(context, listItem);
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(buildShowListIntent(context, extraItem));
                    
                // This intent(ACTION_APPWIDGET_UPDATE) will be called periodically.
                // This intent(ACTION_SHOW_LIST) will be called when current item pressed.
                } else if ((action.equals(Constants.ACTION_APPWIDGET_UPDATE)) ||
                                    (action.equals(Constants.ACTION_SHOW_LIST))) {
                    
                    // Check which the internet is connected or not.
                    INTERNET_CONNECTED_RESULT result = Utils.isInternetConnected(context, extraItem.getPermitMobileConnectionType());

                    refreshAlarmSetting(context, extraItem, result);
                    
                    // Set proper remote view according to the result.
                    if (result == INTERNET_CONNECTED_RESULT.FAILED)
                        setRemoteViewToShowLostInternetConnection(context, awm, extraItem);
                    else
                        setRemoteViewToShowList(context, awm, extraItem);
                    
                    // Save configuration info.
                    saveIntentItem(context, extraItem);

                // This intent will be called when some item selected.
                // EXTRA_ITEM_URL was already filled in the ListViewFactory - getViewAt().
                } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                    removePreviousAlarm();

                    // Send broadcast intent to update some variables on the ContentsFactory.
                    context.sendBroadcast(buildUpdateItemInfoIntent(context, extraItem, listItem));
                    
                    // Check which internet is connected or net.
                    INTERNET_CONNECTED_RESULT result = Utils.isInternetConnected(context, extraItem.getPermitMobileConnectionType());
                    
                    // Set proper remote view according to the result.
                    if (result == INTERNET_CONNECTED_RESULT.FAILED)
                        setRemoteViewToShowLostInternetConnection(context, awm, extraItem);
                    else
                        setRemoteViewToShowItem(context, awm, extraItem, listItem);
                }
            }
        }
    }

    private void setRemoteViewToShowLostInternetConnection(Context context, AppWidgetManager awm, ExtraItem extraItem) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowLostInternetConnection - ExtraItems[" + extraItem.toString() + "]");
        
        PendingIntent pi = null;

        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.lost_internet_connection);
        
        // Set title of the remoteViews.
        rv.setTextViewText(R.id.textLostInternetTitle, context.getResources().getText(R.string.text_lost_internet_connection));

        // Set refresh button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(),
                buildRefreshListIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnLostInternetRefresh, pi);
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(),
                buildConfigurationActivityIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnLostInternetSetting, pi);
        
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowLostInternetConnection - updateAppWidget [LostInternetConnection]");
        awm.updateAppWidget(extraItem.getAppWidgetId(), rv);
    }
    
    private void setRemoteViewToShowList(Context context, AppWidgetManager awm, ExtraItem extraItem) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - ExtraItems[" + extraItem.toString() + "]");
        
        PendingIntent pi = null;

        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
        
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = buildListViewServiceIntent(context, extraItem);
        rv.setRemoteAdapter(R.id.listView, serviceIntent); // For API14+
        //rv.setRemoteAdapter(item.getAppWidgetId(), R.id.listView, serviceIntent); // For API13-
        rv.setScrollPosition(R.id.listView, 0); // Scroll to top

        if (Utils.isTodayBestBoardType(extraItem.getBoardType()) ||
            Utils.isScrapBoardType(extraItem.getBoardType())) {
            // Set title of the remoteViews.
            rv.setTextViewText(R.id.textListTitle, Utils.getBoardTitle(context, extraItem.getBoardType()));
            
            rv.setViewVisibility(R.id.btnListNavTop, View.GONE);
            rv.setViewVisibility(R.id.btnListNavPrev, View.GONE);
            rv.setViewVisibility(R.id.btnListNavNext, View.GONE);
        } else {
            // Save pageNum.
            int currentPageNum = extraItem.getPageNum();
            
            // Set title of the remoteViews.
            if (extraItem.getSearchCategoryType() == Constants.DEFAULT_SEARCH_CATEGORY_TYPE)
                rv.setTextViewText(R.id.textListTitle, (Utils.getBoardTitle(context, extraItem.getBoardType()) + " - " + currentPageNum));
            else if (extraItem.getSearchCategoryType() == Constants.Specific.SEARCH_CATEGORY_TYPE_SUBJECT)
                rv.setTextViewText(R.id.textListTitle, (Utils.getBoardTitle(context, extraItem.getBoardType()) + " - " + currentPageNum +
                        " [" + Utils.getSubjectTitle(context, extraItem.getSearchSubjectType()) + "]"));
            else
                rv.setTextViewText(R.id.textListTitle, (Utils.getBoardTitle(context, extraItem.getBoardType()) + " - " + currentPageNum + 
                        " [" + extraItem.getSearchKeyword() + "]"));
            
            // Set top button of the removeViews.
            rv.setViewVisibility(R.id.btnListNavTop, View.VISIBLE);
            extraItem.setPageNum(Constants.DEFAULT_PAGE_NUM);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_TOP.ordinal(),
                    buildRefreshListIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
              rv.setOnClickPendingIntent(R.id.btnListNavTop, pi);
              
            // Set prev button of the removeViews.
            rv.setViewVisibility(R.id.btnListNavPrev, View.VISIBLE);
            if (currentPageNum > Constants.DEFAULT_PAGE_NUM)
                extraItem.setPageNum(currentPageNum - 1);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_PREV.ordinal(),
                    buildRefreshListIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListNavPrev, pi);

            // Set next button of the remoteViews.
            rv.setViewVisibility(R.id.btnListNavNext, View.VISIBLE);
            extraItem.setPageNum(currentPageNum + 1);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_NEXT.ordinal(),
                    buildRefreshListIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListNavNext, pi);
            
            // Restore pageNum to the intent item.
            extraItem.setPageNum(currentPageNum);
        }
        
        if (Utils.isTodayBestBoardType(extraItem.getBoardType()) ||
            Utils.isPredefinedBoardType(extraItem.getBoardType()) ||
            Utils.isScrapBoardType(extraItem.getBoardType())) {
            rv.setViewVisibility(R.id.btnListSearch, View.GONE);
        } else {
         // Set search button of the remoteViews.
            rv.setViewVisibility(R.id.btnListSearch, View.VISIBLE);
            pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SEARCH.ordinal(), 
                    buildSearchActivityIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListSearch, pi);
        }
        
        if (Utils.isScrapBoardType(extraItem.getBoardType())) {
            rv.setViewVisibility(R.id.btnListEmpty, View.VISIBLE);
            rv.setViewVisibility(R.id.btnListExport, View.GONE);
            rv.setViewVisibility(R.id.btnListRefresh, View.GONE);
        } else {
            rv.setViewVisibility(R.id.btnListEmpty, View.GONE);
            
            // Set export button of the remoteViews.
            rv.setViewVisibility(R.id.btnListExport, View.VISIBLE);
            pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_EXPORT.ordinal(),
                    buildExportIntent(context, extraItem, null), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListExport, pi);
            
            // Set refresh button of the remoteViews.
            rv.setViewVisibility(R.id.btnListRefresh, View.VISIBLE);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(),
                    buildRefreshListIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListRefresh, pi);
        }
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(),
                buildConfigurationActivityIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnListSetting, pi);
        
        // Set a pending intent for click event to the remoteViews.
        PendingIntent clickPi = PendingIntent.getBroadcast(context, 0, 
                buildShowItemIntent(context, extraItem, null), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.listView, clickPi);
        
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - updateAppWidget [BaseballListViewService]");
        awm.updateAppWidget(extraItem.getAppWidgetId(), rv);
        
        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after ListViewFactory is created.
        if (mIsSkipFirstCallListViewService) {
            mIsSkipFirstCallListViewService = false;
        } else {
            if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - notifyAppWidgetViewDataChanged [BaseballListViewService]");
            awm.notifyAppWidgetViewDataChanged(extraItem.getAppWidgetId(), R.id.listView);
        }
    }
    
    private void setRemoteViewToShowItem(Context context, AppWidgetManager awm,
            ExtraItem extraItem, ListItem listItem) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - " +
            "ExtraItem[" + extraItem.toString() + "], ListItem[" + listItem.toString() + "]");
        
        PendingIntent pi = null;
        
        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
        
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = buildContentServiceIntent(context, extraItem, listItem);
        rv.setRemoteAdapter(R.id.contentView, serviceIntent); // For API14+
        //rv.setRemoteAdapter(item.getAppWidgetId(), R.id.contentView, serviceIntent); // For API13-

        // Set title of the remoteViews.
        rv.setTextViewText(R.id.textContentTitle, Utils.getBoardTitle(context, extraItem.getBoardType()));

        // Set export button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_EXPORT.ordinal(),
                buildExportIntent(context, extraItem, listItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentExport, pi);
        
        if (Utils.isScrapBoardType(extraItem.getBoardType())) {
            rv.setViewVisibility(R.id.btnContentAddToBlacklist, View.GONE);
            rv.setViewVisibility(R.id.btnContentScrap, View.GONE);

            // Set delete_scrap button of the remoteViews.
            rv.setViewVisibility(R.id.btnContentDeleteScrap, View.VISIBLE);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_DELETESCRAP.ordinal(),
                    buildDeleteScrappedItemIntent(context, extraItem, listItem), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnContentDeleteScrap, pi);
            
        } else {
            // Set addtoblacklist button of the remoteViews.
            rv.setViewVisibility(R.id.btnContentAddToBlacklist, View.VISIBLE);
            pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_ADDTOBLACKLIST.ordinal(),
                    buildAddToBlackListIntent(context, extraItem, listItem), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnContentAddToBlacklist, pi);
        
            // Set scrap button of the remoteViews.
            rv.setViewVisibility(R.id.btnContentScrap, View.VISIBLE);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SCRAP.ordinal(),
                    buildScrapItemIntent(context, extraItem, listItem), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnContentScrap, pi);
            
            //rv.setViewVisibility(R.id.btnContentStarScrap, View.GONE);
            rv.setViewVisibility(R.id.btnContentDeleteScrap, View.GONE);
        }

        // Set top button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_TOP.ordinal(),
                buildRefreshListIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentNavTop, pi);
        
        // Set refresh button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(),
                buildShowItemIntent(context, extraItem, listItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentRefresh, pi);
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(),
                buildConfigurationActivityIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentSetting, pi);
        
        // Set a pending intent for click event to the remoteViews.
        PendingIntent clickPi = PendingIntent.getBroadcast(context, 0, 
                buildShowListIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.contentView, clickPi);
    
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - updateAppWidget [BaseballContentService]");
        awm.updateAppWidget(extraItem.getAppWidgetId(), rv);

        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after ContentsFactory is created.
        if (mIsSkipFirstCallContentService) {
            mIsSkipFirstCallContentService = false;
        } else {
            if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - notifyAppWidgetViewDataChanged [BaseballContentService]");
            awm.notifyAppWidgetViewDataChanged(extraItem.getAppWidgetId(), R.id.contentView);
        }
    }
    
    private void refreshAlarmSetting(Context context, ExtraItem extraItem, INTERNET_CONNECTED_RESULT result) {
        // If user does not want to refresh, just remove alarm setting.
        // TODO : Consider INTERNET_CONNECTED_RESULT case here?
        if (extraItem.getRefreshTimeType() == Constants.Specific.REFRESH_TIME_TYPE_STOP) {
            removePreviousAlarm();
        
        // If scrap board type, remove alarm.
        } else if (extraItem.getBoardType() == Constants.Specific.BOARD_TYPE_SCRAP) {
            removePreviousAlarm();
        
        // If user wants to refresh, set new alarm.
        } else {
            removePreviousAlarm();
            setNewAlarm(context, extraItem, false);
        }
    }
    
    private void setNewAlarm(Context context, ExtraItem item, boolean isUrgentMode) {
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

    private Intent buildUpdateListInfoIntent(Context context, ExtraItem extraItem) {
        return Utils.createIntentFromExtraItem(
                context, null, Constants.ACTION_UPDATE_LIST_INFO, extraItem, false);
    }
    
    private Intent buildUpdateItemInfoIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = Utils.createIntentFromExtraItem(
                context, null, Constants.ACTION_UPDATE_ITEM_INFO, extraItem, false);
        if (listItem != null) {
            String itemUrl = listItem.getUrl();
            if (itemUrl != null && itemUrl.length() >0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    private Intent buildRefreshListIntent(Context context, ExtraItem extraItem) {
        return Utils.createIntentFromExtraItem(
                context, WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_REFRESH_LIST, extraItem, false);
    }
    
    private Intent buildShowListIntent(Context context, ExtraItem extraItem) {
        return Utils.createIntentFromExtraItem(
                context, WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_SHOW_LIST, extraItem, false);
    }
    
    private Intent buildShowItemIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = Utils.createIntentFromExtraItem(
                context, WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_SHOW_ITEM, extraItem, false);
        if (listItem != null) {
            String itemUrl = listItem.getUrl();
            if (itemUrl != null && itemUrl.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    private Intent buildConfigurationActivityIntent(Context context, ExtraItem extraItem) {
        return Utils.createIntentFromExtraItem(
                context, ConfigurationActivity.CONFIGURATION_ACTIVITY_CLASS_NAME, null, extraItem, true);
    }
    
    private Intent buildSearchActivityIntent(Context context, ExtraItem extraItem) {
        return Utils.createIntentFromExtraItem(
                context, SearchActivity.SEARCH_ACTIVITY_CLASS_NAME, null, extraItem, true);
    }
    
    private Intent buildAddToBlackListIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = Utils.createIntentFromExtraItem(
                context, AddToBlacklistActivity.ADDTOBLACKLIST_ACTIVITY_CLASS_NAME, null, extraItem, true);
        if (listItem != null) {
            String itemWriter = listItem.getWriter();
            if (itemWriter != null && itemWriter.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_WRITER, itemWriter);
        }
        return intent;
    }
    
    private Intent buildScrapItemIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = Utils.createIntentFromExtraItem(
                context, WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_SCRAP_ITEM, extraItem, false);
        if (listItem != null) {
            String itemTitle = listItem.getTitle();
            String itemWriter = listItem.getWriter();
            String itemUrl = listItem.getUrl();
            if (itemTitle != null && itemTitle.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_TITLE, itemTitle);
            if (itemWriter != null && itemWriter.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_WRITER, itemWriter);
            if (itemUrl != null && itemUrl.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    private Intent buildDeleteScrappedItemIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = Utils.createIntentFromExtraItem(
                context, WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_DELETE_SCRAPPED_ITEM, extraItem, false);
        if (listItem != null) {
            String itemUrl = listItem.getUrl();
            if (itemUrl != null && itemUrl.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    private Intent buildListViewServiceIntent(Context context, ExtraItem extraItem) {
        return Utils.createIntentFromExtraItem(
                context, ListViewService.LISTVIEW_SERVICE_CLASS_NAME, null, extraItem, false);
    }
    
    private Intent buildContentServiceIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = Utils.createIntentFromExtraItem(
                context, ContentsService.CONTENTS_SERVICE_CLASS_NAME, null, extraItem, false);
        if (listItem != null) {
            String itemUrl = listItem.getUrl();
            if (itemUrl != null && itemUrl.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    private Intent buildWidgetUpdateIntent(Context context, ExtraItem extraItem) {
        return Utils.createIntentFromExtraItem(
                context, WIDGET_PROVIDER_CLASS_NAME, AppWidgetManager.ACTION_APPWIDGET_UPDATE, extraItem, false);
    }
    
    private Intent buildExportIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        String targetUrl = null;
        
        if (listItem == null) {
            try {
                if (Utils.isTodayBestBoardType(extraItem.getBoardType())) {
                    targetUrl = Constants.Specific.URL_BASE;
                } else {
                    targetUrl = Utils.getMobileBoardUrl(context, extraItem.getPageNum(), extraItem.getBoardType(), 
                            extraItem.getSearchCategoryType(), extraItem.getSearchSubjectType(), extraItem.getSearchKeyword());
                }
            } catch (UnsupportedEncodingException e) {
                if (DEBUG) Log.e(TAG, "buildExportIntent - UnsupportedEncodingException![" + e.toString() + "]");
                e.printStackTrace();
            }
        } else {
            targetUrl = listItem.getUrl();
        }
        
        Intent intent = Utils.createIntentFromExtraItem(
                context, WebViewActivity.WEBVIEW_ACTIVITY_CLASS_NAME, null, extraItem, true);
        intent.putExtra(Constants.EXTRA_EXPORT_URL, targetUrl);

        return intent;
    }

    private void saveScrappedItem(Context context, ListItem listItem) {
        if (DEBUG) Log.d(TAG, "saveScrappedItem - ListItem[" + listItem + "]");
        
        DatabaseHandler handler = DatabaseHandler.open(context);
        
        Cursor c = handler.selectUrl(listItem.getUrl());
        if (c == null) {
            if (DEBUG) Log.e(TAG, "saveScrappedItem - cursor is null!");

        // Check duplicated item.
        } else if (c.getCount() > 0) {
            if (DEBUG) Log.d(TAG, "saveScrappedItem - Duplicated item!");
            Toast.makeText(context, context.getResources().getString(R.string.text_duplicated_scrap_item),
                    Toast.LENGTH_SHORT).show();

        // Insert item.
        } else {
            handler.insert(listItem.getTitle(), listItem.getWriter(), listItem.getUrl());
            if (DEBUG) Log.d(TAG, "saveScrappedItem - completed to insert item!");
            Toast.makeText(context, context.getResources().getString(R.string.text_completed_to_scrap_item),
                    Toast.LENGTH_SHORT).show();
        }
        
        handler.close();
    }

    private void deleteScrappedItem(Context context, ListItem listItem) {
        if (DEBUG) Log.d(TAG, "deleteScrappedItem - ListItem[" + listItem + "]");
        
        DatabaseHandler handler = DatabaseHandler.open(context);
        
        Cursor c = handler.selectUrl(listItem.getUrl());
        if (c == null) {
            if (DEBUG) Log.e(TAG, "deleteScrappedItem - cursor is null!");

        // Check duplicated item.
        } else if (c.getCount() == 0) {
            if (DEBUG) Log.d(TAG, "deleteScrappedItem - item is not existed!");
            Toast.makeText(context, context.getResources().getString(R.string.text_not_existed_item),
                    Toast.LENGTH_SHORT).show();

        // Delete item.
        } else {
            handler.delete(listItem.getUrl());
            if (DEBUG) Log.d(TAG, "deleteScrappedItem - completed to delete scrapped item!");
            Toast.makeText(context, context.getResources().getString(R.string.text_completed_to_delete_scrapped_item),
                    Toast.LENGTH_SHORT).show();
        }
        
        handler.close();
    }
    
    private void saveIntentItem(Context context, ExtraItem extraItem) {
        if (DEBUG) Log.d(TAG, "saveIntentItem");
        
        SharedPreferences pref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.KEY_COMPLETE_TO_SETUP, true);
        editor.putInt(Constants.KEY_BOARD_TYPE, extraItem.getBoardType());
        editor.putInt(Constants.KEY_REFRESH_TIME_TYPE, extraItem.getRefreshTimeType());
        editor.putBoolean(Constants.KEY_PERMIT_MOBILE_CONNECTION_TYPE, extraItem.getPermitMobileConnectionType());
        editor.putString(Constants.KEY_BLACK_LIST, extraItem.getBlackList());
        editor.putString(Constants.KEY_BLOCKED_WORDS, extraItem.getBlockedWords());
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

            // Set urgent alarm to update widget as soon as possible.
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));

            for (int i = 0 ; i < appWidgetIds.length ; i++) {
                ExtraItem item = new ExtraItem(appWidgetIds[i], Constants.DEFAULT_PAGE_NUM,
                        boardType, refreshTimeType, permitMobileConnectionType, blackList, blockedWords,
                        Constants.DEFAULT_SEARCH_CATEGORY_TYPE, Constants.DEFAULT_SEARCH_SUBJECT_TYPE, null);
                
                setNewAlarm(context, item, true);
            }
        }
        
        super.onEnabled(context);
    }
}
