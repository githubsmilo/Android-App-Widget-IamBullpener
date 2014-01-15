
package com.smilo.bullpen;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.smilo.bullpen.definitions.Constants;
import com.smilo.bullpen.definitions.Constants.INTERNET_CONNECTED_RESULT;
import com.smilo.bullpen.definitions.ExtraItem;
import com.smilo.bullpen.definitions.ListItem;
import com.smilo.bullpen.helpers.BuildIntentHelper;
import com.smilo.bullpen.helpers.DatabaseHelper;
import com.smilo.bullpen.helpers.ManageAlarmHelper;
import com.smilo.bullpen.helpers.SetRemoteViewHelper;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetProvider";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    public static final String WIDGET_PROVIDER_CLASS_NAME = Constants.Specific.PACKAGE_NAME + "." + TAG;
    
    // Flag to skip notifyAppWidgetViewDataChanged() call on boot.
    private static boolean mIsSkipFirstCallListViewService = true;
    private static boolean mIsSkipFirstCallContentService = true;
    
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
                    ManageAlarmHelper.removePreviousAlarm();
                    
                    // Save configuration info.
                    saveIntentItem(context, extraItem);

                    // Send broadcast intent to update some variables on the ListViewFactory.
                    context.sendBroadcast(BuildIntentHelper.buildUpdateListInfoIntent(context, extraItem));
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(BuildIntentHelper.buildShowListIntent(context, extraItem, WIDGET_PROVIDER_CLASS_NAME));

                // After setting search activity, this intent will be called.
                } else if (action.equals(Constants.ACTION_REFRESH_LIST)) {
                    ManageAlarmHelper.removePreviousAlarm();
                    
                    // Send broadcast intent to update some variables on the ListViewFactory.
                    context.sendBroadcast(BuildIntentHelper.buildUpdateListInfoIntent(context, extraItem));
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(BuildIntentHelper.buildShowListIntent(context, extraItem, WIDGET_PROVIDER_CLASS_NAME));
 
                } else if (action.equals(Constants.ACTION_SCRAP_ITEM)) {
                    saveScrappedItem(context, listItem);
                    
                } else if (action.equals(Constants.ACTION_DELETE_SCRAPPED_ITEM)) {
                    deleteScrappedItem(context, listItem);
                    
                    // Broadcast ACTION_SHOW_LIST intent.
                    context.sendBroadcast(BuildIntentHelper.buildShowListIntent(context, extraItem, WIDGET_PROVIDER_CLASS_NAME));
                    
                // This intent(ACTION_APPWIDGET_UPDATE) will be called periodically.
                // This intent(ACTION_SHOW_LIST) will be called when current item pressed.
                } else if ((action.equals(Constants.ACTION_APPWIDGET_UPDATE)) ||
                                    (action.equals(Constants.ACTION_SHOW_LIST))) {
                    
                    // Check which the internet is connected or not.
                    INTERNET_CONNECTED_RESULT result = Utils.isInternetConnected(context, extraItem.getPermitMobileConnectionType());

                    ManageAlarmHelper.refreshAlarmSetting(context, extraItem, result);
                    
                    // Set proper remote view according to the result.
                    if (result == INTERNET_CONNECTED_RESULT.FAILED)
                        SetRemoteViewHelper.setRemoteViewToShowLostInternetConnection(context, awm, extraItem);
                    else
                        mIsSkipFirstCallListViewService = SetRemoteViewHelper.setRemoteViewToShowList(context, awm, extraItem, mIsSkipFirstCallListViewService);
                    
                    // Save configuration info.
                    saveIntentItem(context, extraItem);

                // This intent will be called when some item selected.
                // EXTRA_ITEM_URL was already filled in the ListViewFactory - getViewAt().
                } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                    ManageAlarmHelper.removePreviousAlarm();

                    // Send broadcast intent to update some variables on the ContentsFactory.
                    context.sendBroadcast(BuildIntentHelper.buildUpdateItemInfoIntent(context, extraItem, listItem));
                    
                    // Check which internet is connected or net.
                    INTERNET_CONNECTED_RESULT result = Utils.isInternetConnected(context, extraItem.getPermitMobileConnectionType());
                    
                    // Set proper remote view according to the result.
                    if (result == INTERNET_CONNECTED_RESULT.FAILED)
                        SetRemoteViewHelper.setRemoteViewToShowLostInternetConnection(context, awm, extraItem);
                    else
                        mIsSkipFirstCallContentService = SetRemoteViewHelper.setRemoteViewToShowItem(context, awm, extraItem, listItem, mIsSkipFirstCallContentService);
                }
            }
        }
    }
    
    private void saveScrappedItem(Context context, ListItem listItem) {
        if (DEBUG) Log.d(TAG, "saveScrappedItem - ListItem[" + listItem + "]");
        
        DatabaseHelper helper = DatabaseHelper.open(context);
        
        Cursor c = helper.selectUrl(listItem.getUrl());
        if (c == null) {
            if (DEBUG) Log.e(TAG, "saveScrappedItem - cursor is null!");

        // Check duplicated item.
        } else if (c.getCount() > 0) {
            if (DEBUG) Log.d(TAG, "saveScrappedItem - Duplicated item!");
            Toast.makeText(context, context.getResources().getString(R.string.text_duplicated_scrap_item),
                    Toast.LENGTH_SHORT).show();

        // Insert item.
        } else {
            helper.insert(listItem.getTitle(), listItem.getWriter(), listItem.getUrl());
            if (DEBUG) Log.d(TAG, "saveScrappedItem - completed to insert item!");
            Toast.makeText(context, context.getResources().getString(R.string.text_completed_to_scrap_item),
                    Toast.LENGTH_SHORT).show();
        }
        
        helper.close();
    }

    private void deleteScrappedItem(Context context, ListItem listItem) {
        if (DEBUG) Log.d(TAG, "deleteScrappedItem - ListItem[" + listItem + "]");
        
        DatabaseHelper helper = DatabaseHelper.open(context);
        
        Cursor c = helper.selectUrl(listItem.getUrl());
        if (c == null) {
            if (DEBUG) Log.e(TAG, "deleteScrappedItem - cursor is null!");

        // Check duplicated item.
        } else if (c.getCount() == 0) {
            if (DEBUG) Log.d(TAG, "deleteScrappedItem - item is not existed!");
            Toast.makeText(context, context.getResources().getString(R.string.text_not_existed_item),
                    Toast.LENGTH_SHORT).show();

        // Delete item.
        } else {
            helper.delete(listItem.getUrl());
            if (DEBUG) Log.d(TAG, "deleteScrappedItem - completed to delete scrapped item!");
            Toast.makeText(context, context.getResources().getString(R.string.text_completed_to_delete_scrapped_item),
                    Toast.LENGTH_SHORT).show();
        }
        
        helper.close();
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
        editor.putInt(Constants.KEY_BG_IMAGE_TYPE, extraItem.getBgImageType());
        editor.putInt(Constants.KEY_TEXT_SIZE_TYPE, extraItem.getTextSizeType());
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
        ManageAlarmHelper.removePreviousAlarm();
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
        ManageAlarmHelper.removePreviousAlarm();

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
            int bgImageType = pref.getInt(Constants.KEY_BG_IMAGE_TYPE, Constants.DEFAULT_BG_IMAGE_TYPE);
            int textSizeType = pref.getInt(Constants.KEY_TEXT_SIZE_TYPE, Constants.DEFAULT_TEXT_SIZE_TYPE);

            // Set urgent alarm to update widget as soon as possible.
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(context, getClass()));

            for (int i = 0 ; i < appWidgetIds.length ; i++) {
                ExtraItem item = new ExtraItem(appWidgetIds[i], Constants.DEFAULT_PAGE_NUM,
                        boardType, refreshTimeType, permitMobileConnectionType, blackList, blockedWords,
                        Constants.DEFAULT_SEARCH_CATEGORY_TYPE, Constants.DEFAULT_SEARCH_SUBJECT_TYPE, null,
                        bgImageType, textSizeType);
                
                ManageAlarmHelper.setNewAlarm(context, item, true);
            }
        }
        
        super.onEnabled(context);
    }
}
