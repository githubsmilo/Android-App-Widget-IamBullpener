
package com.smilo.bullpen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
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
    
    private static int mSelectedRefreshTime = -1;
    private static String mSelectedBullpenBoardUrl = null;

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
        
        for (int id : appWidgetIds) {
            
            // This intent will be called periodically.
            if (action.equals(Constants.ACTION_APPWIDGET_UPDATE)) {

                removePreviousAlarm();
                setNewAlarm(context, appWidgetId, false);
                setRemoteViewToShowList(context, awm, appWidgetId, true);
    
            } else if (action.equals(Constants.ACTION_INIT_LIST)) {
                int selectedRefreshTimeType = intent.getIntExtra(Constants.EXTRA_REFRESH_TIME_TYPE, -1);
                int selectedBullpenBoardType = intent.getIntExtra(Constants.EXTRA_BULLPEN_BOARD_TYPE, -1);

                switch (selectedRefreshTimeType) {
                    case 0: // 30 sec
                        mSelectedRefreshTime = 60000 / 2;
                        break;
                    case 1: // 1 min
                        mSelectedRefreshTime = 60000;
                        break;
                    case 2: // 5 min
                        mSelectedRefreshTime = 60000 * 5;
                        break;
                    case 3: // 10 min
                        mSelectedRefreshTime = 60000 * 10;
                        break;
                    case 4: // 30 min
                        mSelectedRefreshTime = 60000 * 30;
                        break; 
                    default:
                        mSelectedRefreshTime = Constants.DEFAULT_INTERVAL_AT_MILLIS;
                }
                
                switch (selectedBullpenBoardType) {
                    case 0 : // MLB 타운
                        mSelectedBullpenBoardUrl = Constants.mMLBParkUrl_mlbtown;
                        break;
                    case 1 : // 한국야구 타운
                        mSelectedBullpenBoardUrl = Constants.mMLBParkUrl_kbotown;
                        break;
                    case 2 : // BULLPEN
                        mSelectedBullpenBoardUrl = Constants.mMLBParkUrl_bullpen;
                        break;
                    case 3 : // BULLPEN 조회수 1000 이상
                        mSelectedBullpenBoardUrl = Constants.mMLBParkUrl_bullpen1000;
                        break;
                    case 4 : // BULLPEN 조회수 2000 이상
                        mSelectedBullpenBoardUrl = Constants.mMLBParkUrl_bullpen2000;
                        break;
                    default:
                        mSelectedBullpenBoardUrl = Constants.mMLBParkUrl_mlbtown;
                }

                removePreviousAlarm();
                setNewAlarm(context, appWidgetId, true);
                
                // Send broadcast intent to update mSelectedBullpenBoardUrl variable on the BullpenListViewFactory.
                // On the first time to show some item, this intent does not operate.
                Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_LIST_URL);
                broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                broadcastIntent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
                context.sendBroadcast(broadcastIntent);

                setRemoteViewToShowList(context, awm, appWidgetId, false);
                
            // This intent will be called when some item selected.
            // EXTRA_ITEM_URL was already filled in the BullpenListViewFactory - getViewAt().
            } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                if (Utils.checkInternetConnectivity(cm)) {
                    removePreviousAlarm();
                    mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);

                    // Send broadcast intent to update mSelectedItemUrl variable on the BullpenContentFactory.
                    // On the first time to show some item, this intent does not operate.
                    Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_ITEM_URL);
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
                    removePreviousAlarm();
                    setNewAlarm(context, appWidgetId, false);
                    setRemoteViewToShowList(context, awm, appWidgetId, false);
                } else {
                    Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setRemoteViewToShowList(Context context, AppWidgetManager awm, int appWidgetId, boolean isNotifyDataChanged) {
        
        Intent serviceIntent, clickIntent;
        
        serviceIntent = new Intent(context, BullpenListViewService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(Constants.EXTRA_LIST_URL, mSelectedBullpenBoardUrl);
    
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
    
    private void setRemoteViewToShowItem(Context context, AppWidgetManager awm, int appWidgetId) {
    
        Intent serviceIntent, clickIntent;
        
        serviceIntent = new Intent(context, BullpenContentService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(Constants.EXTRA_ITEM_URL, mSelectedItemUrl);
    
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
