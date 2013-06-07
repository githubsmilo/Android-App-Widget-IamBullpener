
package com.smilo.bullpen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
    
    private static int mSelectedRefreshTime = -1;
    private static String mSelectedBullpenBoardUrl = null;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,  AppWidgetManager.INVALID_APPWIDGET_ID);
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));

        if (appWidgetId <= AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "onReceive - action[" + action + "], Invalid appWidgetId[" + appWidgetId + "], appWidgetsNum[" + appWidgetIds.length + "]");
            return;
        } else {
            Log.i(TAG, "onReceive - action[" + action + "], appWidgetId[" + appWidgetId + "], appWidgetsNum[" + appWidgetIds.length + "]");
        }

        if (isInternetConnected(context) == false) {
            Log.e(TAG, "onReceive - Internet is not connected!");
            
            //removePreviousAlarm();
            //setNewAlarm(context, appWidgetId, false);
            
            // TODO : internet not connected remoteview
            return;
        }
        
        //for (int id : appWidgetIds) {
            
            // This intent will be called periodically.
            if (action.equals(Constants.ACTION_APPWIDGET_UPDATE)) {
                removePreviousAlarm();
                setNewAlarm(context, appWidgetId, false);
                
                setRemoteViewToShowList(context, awm, appWidgetId);
    
            } else if (action.equals(Constants.ACTION_INIT_LIST)) {
                int selectedRefreshTimeType = intent.getIntExtra(Constants.EXTRA_REFRESH_TIME_TYPE, -1);
                int selectedBullpenBoardType = intent.getIntExtra(Constants.EXTRA_BULLPEN_BOARD_TYPE, -1);
                mSelectedRefreshTime = Utils.getRefreshTime(selectedRefreshTimeType);
                mSelectedBullpenBoardUrl = Utils.getBullpenBoardUrl(selectedBullpenBoardType);

                removePreviousAlarm();
                setNewAlarm(context, appWidgetId, false);
                
                // Send broadcast intent to update mSelectedBullpenBoardUrl variable on the BullpenListViewFactory.
                // On the first time to show some item, this intent does not operate.
                Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_LIST_URL);
                broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                broadcastIntent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
                context.sendBroadcast(broadcastIntent);
                
                setRemoteViewToShowList(context, awm, appWidgetId);
                
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
                context.sendBroadcast(broadcastIntent);
                
                setRemoteViewToShowItem(context, awm, appWidgetId);

            // This intent will be called when current item pressed.
            } else if (action.equals(Constants.ACTION_SHOW_LIST)) {
                removePreviousAlarm();
                setNewAlarm(context, appWidgetId, false);
                
                setRemoteViewToShowList(context, awm, appWidgetId);
            }
       // }
    }

    private String getRemoteViewTitle(Context context) {
    	Resources res = context.getResources();
    	if (mSelectedBullpenBoardUrl == null) {
    		return res.getString(R.string.remoteViewTitle_Default);
    	} else if (mSelectedBullpenBoardUrl.equals(Constants.mMLBParkUrl_mlbtown)) {
    		return res.getString(R.string.remoteViewTitle_MlbTown);
    	} else if (mSelectedBullpenBoardUrl.equals(Constants.mMLBParkUrl_kbotown)) {
    		return res.getString(R.string.remoteViewTitle_KboTown);
    	} else if (mSelectedBullpenBoardUrl.equals(Constants.mMLBParkUrl_bullpen)) {
    		return res.getString(R.string.remoteViewTitle_Bullpen);
    	} else if (mSelectedBullpenBoardUrl.equals(Constants.mMLBParkUrl_bullpen1000)) {
    		return res.getString(R.string.remoteViewTitle_Bullpen1000);
    	} else if (mSelectedBullpenBoardUrl.equals(Constants.mMLBParkUrl_bullpen2000)) {
    		return res.getString(R.string.remoteViewTitle_Bullpen2000);
    	} else {
    		return null;
    	}
    }
    
    private PendingIntent buildConfigurationActivityIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, BullpenConfigurationActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, Utils.getRefreshTimeType(mSelectedRefreshTime));
        intent.putExtra(Constants.EXTRA_BULLPEN_BOARD_TYPE, Utils.getBullpenBoardType(mSelectedBullpenBoardUrl));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    private void setRemoteViewToShowList(Context context, AppWidgetManager awm, int appWidgetId) {
        
        Intent serviceIntent, clickIntent;
        
        serviceIntent = new Intent(context, BullpenListViewService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
    
        clickIntent = new Intent(context, BullpenWidgetProvider.class);
        clickIntent.setAction(Constants.ACTION_SHOW_ITEM);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
        rv.setTextViewText(R.id.textListTitle, getRemoteViewTitle(context));
        rv.setOnClickPendingIntent(R.id.btnListSetting, buildConfigurationActivityIntent(context, appWidgetId));
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
            Log.i(TAG, "notifyAppWidgetViewDataChanged[BaseballListViewService]");
            awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView);
        }
    }
    
    private void setRemoteViewToShowItem(Context context, AppWidgetManager awm, int appWidgetId) {
    
        Intent serviceIntent, clickIntent;
        
        serviceIntent = new Intent(context, BullpenContentService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
    
        clickIntent = new Intent(context, BullpenWidgetProvider.class);
        clickIntent.setAction(Constants.ACTION_SHOW_LIST);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
        rv.setTextViewText(R.id.textContentTitle, getRemoteViewTitle(context));
        rv.setOnClickPendingIntent(R.id.btnContentSetting, buildConfigurationActivityIntent(context, appWidgetId));
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
    
    private void setNewAlarm(Context context, int appWidgetId, boolean isUrgent) {
        Log.i(TAG, "setNewAlarm - appWidgetId[" + appWidgetId + "], isUrgent[" + isUrgent + "]");

        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        updateIntent.setClass(context, BullpenWidgetProvider.class);
        
        long alarmTime = System.currentTimeMillis() + (mSelectedRefreshTime <= 0 ? Constants.DEFAULT_INTERVAL_AT_MILLIS : mSelectedRefreshTime);
        if (isUrgent) alarmTime = 0;
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
    
    private boolean isInternetConnected(Context context) {
        ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if(Utils.checkInternetConnectivity(cm)) {
            return true;
        } else {
                Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
                return false;
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
