package com.smilo.bullpen.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.smilo.bullpen.R;
import com.smilo.bullpen.Utils;
import com.smilo.bullpen.WidgetProvider;
import com.smilo.bullpen.definitions.Constants;
import com.smilo.bullpen.definitions.Constants.INTERNET_CONNECTED_RESULT;
import com.smilo.bullpen.definitions.ExtraItem;

public final class ManageAlarmHelper {

    private static final String TAG = "ManageAlarmHelper";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    // the pending intent to broadcast alarm.
    private static PendingIntent mSender;
    
    // the alarm manager to refresh app widget periodically.
    private static AlarmManager mManager;
    
    public static void refreshAlarmSetting(Context context, ExtraItem extraItem, INTERNET_CONNECTED_RESULT result) {
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
    
    public static void setNewAlarm(Context context, ExtraItem item, boolean isUrgentMode) {
        if (DEBUG) Log.d(TAG, "setNewAlarm");

        Resources res = context.getResources();
        int selectedRefreshTime = Utils.getRefreshTime(context, item.getRefreshTimeType());
        long alarmTime = System.currentTimeMillis() + (selectedRefreshTime <= 0 ? res.getInteger(R.integer.int_default_interval) : selectedRefreshTime);
        if (isUrgentMode) alarmTime = 0;
        mSender = PendingIntent.getBroadcast(context, 0, BuildIntentHelper.buildWidgetUpdateIntent(context, item, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME), 0);
        mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mManager.set(AlarmManager.RTC, alarmTime, mSender);
    }

    public static void removePreviousAlarm() {
        if (DEBUG) Log.d(TAG, "removePreviousAlarm");

        if (mManager != null && mSender != null) {
            mSender.cancel();
            mManager.cancel(mSender);
        }
    }
}
