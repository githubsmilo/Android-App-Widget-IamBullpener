package com.smilo.bullpen.helpers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.smilo.bullpen.R;
import com.smilo.bullpen.Utils;
import com.smilo.bullpen.WidgetProvider;
import com.smilo.bullpen.definitions.Constants;
import com.smilo.bullpen.definitions.ExtraItem;
import com.smilo.bullpen.definitions.ListItem;

public final class SetRemoteViewHelper {

    private static final String TAG = "SetRemoteViewHelper";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
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

    public static void setRemoteViewToShowLostInternetConnection(
            Context context, AppWidgetManager awm, ExtraItem extraItem) {
        
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowLostInternetConnection - ExtraItems[" + extraItem.toString() + "]");
        
        PendingIntent pi = null;

        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.lost_internet_connection);
        
        // Set title of the remoteViews.
        rv.setTextViewText(R.id.textLostInternetTitle, context.getResources().getText(R.string.text_lost_internet_connection));

        // Set refresh button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(),
                BuildIntentHelper.buildRefreshListIntent(context, extraItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnLostInternetRefresh, pi);
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(),
                BuildIntentHelper.buildConfigurationActivityIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnLostInternetSetting, pi);
        
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowLostInternetConnection - updateAppWidget [LostInternetConnection]");
        awm.updateAppWidget(extraItem.getAppWidgetId(), rv);
    }
    
    public static boolean setRemoteViewToShowList(
            Context context, AppWidgetManager awm, ExtraItem extraItem, boolean isSkipFirstCallListViewService) {
        
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - ExtraItems[" + extraItem.toString() + "]");
        
        PendingIntent pi = null;

        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
        
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = BuildIntentHelper.buildListViewServiceIntent(context, extraItem);
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
                    BuildIntentHelper.buildRefreshListIntent(context, extraItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
              rv.setOnClickPendingIntent(R.id.btnListNavTop, pi);
              
            // Set prev button of the removeViews.
            rv.setViewVisibility(R.id.btnListNavPrev, View.VISIBLE);
            if (currentPageNum > Constants.DEFAULT_PAGE_NUM)
                extraItem.setPageNum(currentPageNum - 1);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_PREV.ordinal(),
                    BuildIntentHelper.buildRefreshListIntent(context, extraItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListNavPrev, pi);

            // Set next button of the remoteViews.
            rv.setViewVisibility(R.id.btnListNavNext, View.VISIBLE);
            extraItem.setPageNum(currentPageNum + 1);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_NEXT.ordinal(),
                    BuildIntentHelper.buildRefreshListIntent(context, extraItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
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
                    BuildIntentHelper.buildSearchActivityIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
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
                    BuildIntentHelper.buildExportIntent(context, extraItem, null), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListExport, pi);
            
            // Set refresh button of the remoteViews.
            rv.setViewVisibility(R.id.btnListRefresh, View.VISIBLE);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(),
                    BuildIntentHelper.buildRefreshListIntent(context, extraItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnListRefresh, pi);
        }
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(),
                BuildIntentHelper.buildConfigurationActivityIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnListSetting, pi);
        
        // Set a pending intent for click event to the remoteViews.
        PendingIntent clickPi = PendingIntent.getBroadcast(context, 0, 
                BuildIntentHelper.buildShowItemIntent(context, extraItem, null, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.listView, clickPi);
        
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - updateAppWidget [BaseballListViewService]");
        awm.updateAppWidget(extraItem.getAppWidgetId(), rv);
        
        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after ListViewFactory is created.
        if (isSkipFirstCallListViewService) {
            return false;
        } else {
            if (DEBUG) Log.d(TAG, "setRemoteViewToShowList - notifyAppWidgetViewDataChanged [BaseballListViewService]");
            awm.notifyAppWidgetViewDataChanged(extraItem.getAppWidgetId(), R.id.listView);
            return false;
        }
    }
    
    public static boolean setRemoteViewToShowItem(
            Context context, AppWidgetManager awm, ExtraItem extraItem, ListItem listItem, boolean isSkipFirstCallContentService) {
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - " +
            "ExtraItem[" + extraItem.toString() + "], ListItem[" + listItem.toString() + "]");
        
        PendingIntent pi = null;
        
        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
        
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = BuildIntentHelper.buildContentServiceIntent(context, extraItem, listItem);
        rv.setRemoteAdapter(R.id.contentView, serviceIntent); // For API14+
        //rv.setRemoteAdapter(item.getAppWidgetId(), R.id.contentView, serviceIntent); // For API13-

        // Set title of the remoteViews.
        rv.setTextViewText(R.id.textContentTitle, Utils.getBoardTitle(context, extraItem.getBoardType()));

        // Set export button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_EXPORT.ordinal(),
                BuildIntentHelper.buildExportIntent(context, extraItem, listItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentExport, pi);
        
        if (Utils.isScrapBoardType(extraItem.getBoardType())) {
            rv.setViewVisibility(R.id.btnContentAddToBlacklist, View.GONE);
            rv.setViewVisibility(R.id.btnContentScrap, View.GONE);

            // Set delete_scrap button of the remoteViews.
            rv.setViewVisibility(R.id.btnContentDeleteScrap, View.VISIBLE);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_DELETESCRAP.ordinal(),
                    BuildIntentHelper.buildDeleteScrappedItemIntent(context, extraItem, listItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnContentDeleteScrap, pi);
            
        } else {
            // Set addtoblacklist button of the remoteViews.
            rv.setViewVisibility(R.id.btnContentAddToBlacklist, View.VISIBLE);
            pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_ADDTOBLACKLIST.ordinal(),
                    BuildIntentHelper.buildAddToBlackListIntent(context, extraItem, listItem), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnContentAddToBlacklist, pi);
        
            // Set scrap button of the remoteViews.
            rv.setViewVisibility(R.id.btnContentScrap, View.VISIBLE);
            pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SCRAP.ordinal(),
                    BuildIntentHelper.buildScrapItemIntent(context, extraItem, listItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.btnContentScrap, pi);
            
            //rv.setViewVisibility(R.id.btnContentStarScrap, View.GONE);
            rv.setViewVisibility(R.id.btnContentDeleteScrap, View.GONE);
        }

        // Set top button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_TOP.ordinal(),
                BuildIntentHelper.buildRefreshListIntent(context, extraItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentNavTop, pi);
        
        // Set refresh button of the remoteViews.
        pi = PendingIntent.getBroadcast(context, PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH.ordinal(),
                BuildIntentHelper.buildShowItemIntent(context, extraItem, listItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentRefresh, pi);
        
        // Set setting button of the remoteViews.
        pi = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE.REQUEST_SETTING.ordinal(),
                BuildIntentHelper.buildConfigurationActivityIntent(context, extraItem), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btnContentSetting, pi);
        
        // Set a pending intent for click event to the remoteViews.
        PendingIntent clickPi = PendingIntent.getBroadcast(context, 0, 
                BuildIntentHelper.buildShowListIntent(context, extraItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.contentView, clickPi);
    
        // Update widget.
        if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - updateAppWidget [BaseballContentService]");
        awm.updateAppWidget(extraItem.getAppWidgetId(), rv);

        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after ContentsFactory is created.
        if (isSkipFirstCallContentService) {
            return false;
        } else {
            if (DEBUG) Log.d(TAG, "setRemoteViewToShowItem - notifyAppWidgetViewDataChanged [BaseballContentService]");
            awm.notifyAppWidgetViewDataChanged(extraItem.getAppWidgetId(), R.id.contentView);
            return false;
        }
    }
}
