
package com.smilo.bullpen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        AppWidgetManager awm = AppWidgetManager.getInstance(context);

        String action = intent.getAction();
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,  AppWidgetManager.INVALID_APPWIDGET_ID);
        int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));
        Log.i(TAG, "onReceive - action[" + action + "], appWidgetId[" + appWidgetId + "], appWidgetsNum[" + appWidgetIds.length + "]");

        if (appWidgetId <= AppWidgetManager.INVALID_APPWIDGET_ID) {
            return;
        }
        
        for (int i=0 ; i<appWidgetIds.length ; i++) {
            
            // This intent will be called periodically.
            if (action.equals(Constants.ACTION_APPWIDGET_UPDATE)) {
            	updateAppWidgetToShowList(context, awm, appWidgetId, true);
    
            // This intent will be called when some item selected.
            // EXTRA_ITEM_URL was already filled in the BullpenListViewFactory - getViewAt().
            } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                if (Utils.checkInternetConnectivity(cm)) {
                    removePreviousAlarm();
                    mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);

                    // Send broadcast intent to update mSelectedItemUrl variable on the BullpenContentFactory.
                    // On the first time to show some item, this intent does not operate.
                    Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_URL);
                    broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    broadcastIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
                    context.sendBroadcast(broadcastIntent);
                    
                    setRemoteViewToShowItem(context, awm, appWidgetId);
                } else {
                    Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
                }

            // This intent will be called when current item pressed.
            } else if (action.equals(Constants.ACTION_SHOW_LIST)) {
                if (Utils.checkInternetConnectivity(cm)) {
                	updateAppWidgetToShowList(context, awm, appWidgetId, false);
                } else {
                    Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static void setRemoteViewToShowList(Context context, AppWidgetManager awm, int appWidgetId, boolean isNotifyDataChanged) {
        
        Intent serviceIntent, clickIntent;
        
        serviceIntent = new Intent(context, BullpenListViewService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
        clickIntent = new Intent(context, BullpenWidgetProvider.class);
        clickIntent.setAction(Constants.ACTION_SHOW_ITEM);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
        // views.setRemoteAdapter(R.id.list, serviceIntent); // For API14+
        rv.setRemoteAdapter(appWidgetId, R.id.listView, serviceIntent);
    
        PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.listView, linkPendingIntent);
    
        Log.i(TAG, "updateAppWidget[BaseballListViewService]");
        awm.updateAppWidget(appWidgetId, rv);
        
        // On first call, we need not execute notifyAppWidgetViewDataChanged()
        // because onDataSetChanged() is called automatically after BullpenListViewFactory is created.
        if (mIsSkipFirstCallListViewService) {
            mIsSkipFirstCallListViewService = false;
        } else {
            if (isNotifyDataChanged) {
                Log.i(TAG, "notifyAppWidgetViewDataChanged[BaseballListViewService]");
                awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView);
            }
        }
    }
    
    private static void setRemoteViewToShowItem(Context context, AppWidgetManager awm, int appWidgetId) {
    
        Intent serviceIntent, clickIntent;
        
        serviceIntent = new Intent(context, BullpenContentService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl); // Store mSelectedItemUrl to the serviceIntent
    
        clickIntent = new Intent(context, BullpenWidgetProvider.class);
        clickIntent.setAction(Constants.ACTION_SHOW_LIST);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
        // views.setRemoteAdapter(R.id.list, serviceIntent); // For API14+
        rv.setRemoteAdapter(appWidgetId, R.id.contentView, serviceIntent);
    
        PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.contentView, linkPendingIntent);
    
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
    
    private static void setNewAlarm(Context context, int appWidgetId) {
        Log.i(TAG, "setNewAlarm - appWidgetId[" + appWidgetId + "]");

        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        updateIntent.setClass(context, BullpenWidgetProvider.class);
    	
        long firstTime = System.currentTimeMillis() + Constants.WIDGET_UPDATE_INTERVAL_AT_MILLIS;
        mSender = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
        mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mManager.set(AlarmManager.RTC, firstTime, mSender);
    }

    private static void removePreviousAlarm() {
        Log.i(TAG, "removePreviousAlarm");

        if (mManager != null && mSender != null) {
            mSender.cancel();
            mManager.cancel(mSender);
        }
    }
    
    public static void updateAppWidgetToShowList(Context context, AppWidgetManager awm, int appWidgetId, boolean isNotifyDataChanged) {
    	Log.i(TAG, "updateAppWidgetToShowList - appWidgetId[" + appWidgetId + "], isNotifyDataChanged[" + isNotifyDataChanged + "]");
    	
    	removePreviousAlarm();
        setNewAlarm(context, appWidgetId);
        
        setRemoteViewToShowList(context, awm, appWidgetId, isNotifyDataChanged);
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
