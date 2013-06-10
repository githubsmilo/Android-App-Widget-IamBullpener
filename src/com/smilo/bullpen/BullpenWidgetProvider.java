
package com.smilo.bullpen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class BullpenWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "BullpenWidgetProvider";

    // the pending intent to broadcast alarm.
    private static PendingIntent mSender;
    
    // the alarm manager to refresh bullpen widget periodically.
    private static AlarmManager mManager;
    
    // the url string to show selected item.
    private static String mSelectedItemUrl = null;
    
    // Flag to skip notifyAppWidgetViewDataChanged() call on boot.
    private static boolean mIsSkipFirstCallListViewService = true;
    private static boolean mIsSkipFirstCallContentService = true;
    
    private static int mSelectedRefreshTime = -1;
    private static String mSelectedBullpenBoardUrl = null;
    
    private static enum PENDING_INTENT_REQUEST_CODE {
        REQUEST_TOP,
        REQUEST_PREV,
        REQUEST_NEXT,
        REQUEST_REFRESH,
        REQUEST_SETTING,
        REQUEST_UNKNOWN,
    };
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        int pageNum = intent.getIntExtra(Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,  AppWidgetManager.INVALID_APPWIDGET_ID);
        
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));

        if (appWidgetId <= AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "onReceive - action[" + action + "], Invalid appWidgetId[" + appWidgetId + "], pageNum[" + pageNum + "], appWidgetsNum[" + appWidgetIds.length + "]");
            return;
        } else {
            Log.i(TAG, "onReceive - action[" + action + "], appWidgetId[" + appWidgetId + "], pageNum[" + pageNum + "], appWidgetsNum[" + appWidgetIds.length + "]");
        }

        if (Utils.isInternetConnected(context) == false) {
            Log.e(TAG, "onReceive - Internet is not connected!");
            
            //removePreviousAlarm();
            //setNewAlarm(context, appWidgetId, false);
            
            // TODO : internet not connected remoteview
            return;
        }
        
        //for (int id : appWidgetIds) {
            
            // After setting configuration activity, this intent will be called.
            if (action.equals(Constants.ACTION_INIT_LIST)) {
                int selectedRefreshTimeType = intent.getIntExtra(Constants.EXTRA_REFRESH_TIME_TYPE, -1);
                int selectedBullpenBoardType = intent.getIntExtra(Constants.EXTRA_BULLPEN_BOARD_TYPE, -1);
                mSelectedRefreshTime = Utils.getRefreshTime(selectedRefreshTimeType);
                mSelectedBullpenBoardUrl = Utils.getBullpenBoardUrl(selectedBullpenBoardType);
    
                // Send broadcast intent to update mSelectedBullpenBoardUrl variable on the BullpenListViewFactory.
                // On the first time to show some item, this intent does not operate.
                Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_LIST_URL);
                broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                broadcastIntent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
                broadcastIntent.putExtra(Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
                context.sendBroadcast(broadcastIntent);
                
                // Broadcast ACTION_SHOW_LIST intent.
                Intent showListIntent = new Intent(context, BullpenWidgetProvider.class);
                showListIntent.setAction(Constants.ACTION_SHOW_LIST);
                showListIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                showListIntent.putExtra(Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
                context.sendBroadcast(showListIntent);

            } else if (action.equals(Constants.ACTION_REFRESH_LIST)){
                // Send broadcast intent to update mSelectedBullpenBoardUrl variable on the BullpenListViewFactory.
                // On the first time to show some item, this intent does not operate.
                Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_LIST_URL);
                broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                broadcastIntent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
                broadcastIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
                context.sendBroadcast(broadcastIntent);
                
                // Broadcast ACTION_SHOW_LIST intent.
                Intent showListIntent = new Intent(context, BullpenWidgetProvider.class);
                showListIntent.setAction(Constants.ACTION_SHOW_LIST);
                showListIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                showListIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
                context.sendBroadcast(showListIntent);
                
            // This intent(ACTION_APPWIDGET_UPDATE) will be called periodically.
            // This intent(ACTION_SHOW_LIST) will be called when current item pressed.
            } else if ((action.equals(Constants.ACTION_APPWIDGET_UPDATE)) ||
                                (action.equals(Constants.ACTION_SHOW_LIST))) {
                refreshAlarmSetting(context, appWidgetId, pageNum);
                setRemoteViewToShowList(context, awm, appWidgetId, pageNum);
    
            // This intent will be called when some item selected.
            // EXTRA_ITEM_URL was already filled in the BullpenListViewFactory - getViewAt().
            } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                removePreviousAlarm();
                mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);

                // Send broadcast intent to update mSelectedItemUrl variable on the BullpenContentFactory.
                // On the first time to show some item, this intent does not operate.
                Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_ITEM_URL);
                broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                broadcastIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
                broadcastIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
                context.sendBroadcast(broadcastIntent);
                
                setRemoteViewToShowItem(context, awm, appWidgetId, pageNum);
            }
       // }
    }

    private PendingIntent buildListRefreshIntent(PENDING_INTENT_REQUEST_CODE requestCode, Context context, int appWidgetId, int pageNum) {
        Intent intent = new Intent(context, BullpenWidgetProvider.class);
        intent.setAction(Constants.ACTION_REFRESH_LIST);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        return pendingIntent;
    }
    
    private PendingIntent buildContentRefreshIntent(Context context, int appWidgetId, int pageNum) {
        Intent intent = new Intent(context, BullpenWidgetProvider.class);
        intent.setAction(Constants.ACTION_SHOW_ITEM);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
        intent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        return pendingIntent;
    }
    
    private PendingIntent buildConfigurationActivityIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, BullpenConfigurationActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, Utils.getRefreshTimeType(mSelectedRefreshTime));
        intent.putExtra(Constants.EXTRA_BULLPEN_BOARD_TYPE, Utils.getBullpenBoardType(mSelectedBullpenBoardUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    private void setRemoteViewToShowList(Context context, AppWidgetManager awm, int appWidgetId, int pageNum) {

        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
        
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = new Intent(context, BullpenListViewService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
        serviceIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
        // views.setRemoteAdapter(R.id.listView, serviceIntent); // For API14+
        rv.setRemoteAdapter(appWidgetId, R.id.listView, serviceIntent);

        // Set title of the remoteViews.
        rv.setTextViewText(R.id.textListTitle, Utils.getRemoteViewTitleWithPageNum(context, mSelectedBullpenBoardUrl, pageNum));

        // Set top button of the remoteViews.
        rv.setOnClickPendingIntent(R.id.btnListNavTop, buildListRefreshIntent(
                PENDING_INTENT_REQUEST_CODE.REQUEST_TOP, context, appWidgetId, Constants.DEFAULT_PAGE_NUM));
        
        // Set prev button of the removeViews.
        rv.setOnClickPendingIntent(R.id.btnListNavPrev, buildListRefreshIntent(
                PENDING_INTENT_REQUEST_CODE.REQUEST_PREV, context, appWidgetId, (pageNum > Constants.DEFAULT_PAGE_NUM ? pageNum - 1 : pageNum)));
        
        // Set next button of the remoteViews.
        rv.setOnClickPendingIntent(R.id.btnListNavNext, buildListRefreshIntent(
                PENDING_INTENT_REQUEST_CODE.REQUEST_NEXT, context, appWidgetId, pageNum + 1));
        
        // Set refresh button of the remoteViews.
        rv.setOnClickPendingIntent(R.id.btnListRefresh, buildListRefreshIntent(
                PENDING_INTENT_REQUEST_CODE.REQUEST_REFRESH, context, appWidgetId, pageNum));
        
        // Set setting button of the remoteViews.
        rv.setOnClickPendingIntent(R.id.btnListSetting, buildConfigurationActivityIntent(context, appWidgetId));
        
        // Set a pending intent for click event to the remoteViews.
        Intent clickIntent = new Intent(context, BullpenWidgetProvider.class);
        clickIntent.setAction(Constants.ACTION_SHOW_ITEM);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        clickIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
        PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.listView, linkPendingIntent);
    
        // Update widget.
        Log.i(TAG, "updateAppWidget[BaseballListViewService]");
        awm.updateAppWidget(appWidgetId, rv);
        
        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after BullpenListViewFactory is created.
        if (mIsSkipFirstCallListViewService) {
            mIsSkipFirstCallListViewService = false;
        } else {
            Log.i(TAG, "notifyAppWidgetViewDataChanged[BaseballListViewService]");
            awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView);
        }
    }
    
    private void setRemoteViewToShowItem(Context context, AppWidgetManager awm, int appWidgetId, int pageNum) {
    
        // Create new remoteViews.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
        
        // Set a remoteAdapter to the remoteViews.
        Intent serviceIntent = new Intent(context, BullpenContentService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
        serviceIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
        // views.setRemoteAdapter(R.id.contentView, serviceIntent); // For API14+
        rv.setRemoteAdapter(appWidgetId, R.id.contentView, serviceIntent);

        // Set title of the remoteViews.
        rv.setTextViewText(R.id.textContentTitle, Utils.getRemoteViewTitle(context, mSelectedBullpenBoardUrl));

        // Set refresh button of the remoteViews.
        rv.setOnClickPendingIntent(R.id.btnContentRefresh, buildContentRefreshIntent(context, appWidgetId, pageNum));
        
        // Set setting button of the remoteViews.
        rv.setOnClickPendingIntent(R.id.btnContentSetting, buildConfigurationActivityIntent(context, appWidgetId));
        
        // Set a pending intent for click event to the remoteViews.
        Intent clickIntent = new Intent(context, BullpenWidgetProvider.class);
        clickIntent.setAction(Constants.ACTION_SHOW_LIST);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        clickIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
        PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.contentView, linkPendingIntent);
    
        // Update widget.
        Log.i(TAG, "updateAppWidget[BaseballContentService]");
        awm.updateAppWidget(appWidgetId, rv);
        
        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after BullpenContentFactory is created.
        if (mIsSkipFirstCallContentService) {
            mIsSkipFirstCallContentService = false;
        } else {
            Log.i(TAG, "notifyAppWidgetViewDataChanged[BaseballContentService]");
            awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.contentView);
        }
    }
    
    private void refreshAlarmSetting(Context context, int appWidgetId, int pageNum) {
        // If user does not want to refresh, just remove alarm setting.
        if (mSelectedRefreshTime == -1) {
            removePreviousAlarm();
            
        // If user wants to refresh, set new alarm.
        } else {
            removePreviousAlarm();
            setNewAlarm(context, appWidgetId, pageNum);
        }
    }
    
    private void setNewAlarm(Context context, int appWidgetId, int pageNum) {
        Log.i(TAG, "setNewAlarm - appWidgetId[" + appWidgetId + "], pageNum[" + pageNum + "]");

        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        updateIntent.putExtra(Constants.EXTRA_PAGE_NUM, pageNum);
        updateIntent.setClass(context, BullpenWidgetProvider.class);
        
        long alarmTime = System.currentTimeMillis() + (mSelectedRefreshTime <= 0 ? Constants.DEFAULT_INTERVAL_AT_MILLIS : mSelectedRefreshTime);
        mSender = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
        mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mManager.set(AlarmManager.RTC, alarmTime, mSender);
    }

    private void removePreviousAlarm() {
        Log.i(TAG, "removePreviousAlarm");

        if (mManager != null && mSender != null) {
            mSender.cancel();
            mManager.cancel(mSender);
        }
    }
    
    public static void removeWidget(Context context, int appWidgetId) {
        AppWidgetHost host = new AppWidgetHost(context, 1);
        host.deleteAppWidgetId(appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager awm, int[] appWidgetIds) {
        Log.i(TAG, "onUpdate");
        super.onUpdate(context, awm, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.i(TAG, "onDeleted");
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        Log.i(TAG, "onDisabled");
        removePreviousAlarm();
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        Log.i(TAG, "onEnabled");
        removePreviousAlarm();
        super.onEnabled(context);
    }
}
