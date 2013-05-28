
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
    
    // the alarm manager to refresh baseball widget periodically.
    private static AlarmManager mManager;

    // save intent android.appwidget.action.APPWIDGET_UPDATE
    private static Intent mAppWidgetUpdateIntent;
    
    // the url string to show selected item.
    private static String mUrl = null;

    // private static final int MAX_CLIENTS = 2;
    // private List<ServerThread> mServerThreads;
    // private Handler mHandler = new Handler();

    public BullpenWidgetProvider() {
        /*
         * mServerThreads = new
         * ArrayList<BaseballWidgetProvider.ServerThread>(MAX_CLIENTS); for (int
         * i=0 ; i<MAX_CLIENTS ; i++) { ServerThread st = new ServerThread(i);
         * Thread t = new Thread(st); t.start(); mServerThreads.add(st); }
         */
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

        String action = intent.getAction();
        Log.i(TAG, "onReceive - action[" + action + "], appWidgetsNum[" + appWidgetIds.length + "]");

        for (int i=0 ; i<appWidgetIds.length ; i++) {
            
            // In case of APPWIDGET_UPDATE intent
            if (action.equals(Constants.ACTION_APPWIDGET_UPDATE)) {
                mAppWidgetUpdateIntent = intent;
                removePreviousAlarm();
                setNewAlarm(context);

                setRemoteViewToShowList(context, appWidgetManager, appWidgetIds[i] );
    
            // In case of APPWIDGET_DISABLED
            } else if (action.equals(Constants.ACTION_APPWIDGET_DISABLED)) {
                removePreviousAlarm();

            // In case of ACTION_SHOW_ITEM
            } else if (action.equals(Constants.ACTION_SHOW_ITEM)) {
                if (Utils.checkInternetConnectivity(cm)) {
                    removePreviousAlarm();
                    mUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
                    
                    setRemoteViewToShowItem(context, appWidgetManager, appWidgetIds[i]);
        
                    // Send broadcast intent to update mUrl variable on the BaseballContentFactory.
                    // On the first time to show some item, this intent does not operate.
                    Intent broadcastIntent = new Intent(Constants.ACTION_UPDATE_URL);
                    broadcastIntent.putExtra(Constants.EXTRA_ITEM_URL, mUrl);
                    context.sendBroadcast(broadcastIntent);
                } else {
                    Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
                }

            // In case of ACTION_SHOW_LIST
            } else if (action.equals(Constants.ACTION_SHOW_LIST)) {
                if (Utils.checkInternetConnectivity(cm)) {
                    removePreviousAlarm();
                    setNewAlarm(context);
        
                    setRemoteViewToShowList(context, appWidgetManager, appWidgetIds[i]);
                } else {
                    Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setRemoteViewToShowList(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        
        Intent serviceIntent, clickIntent;
        
        serviceIntent = new Intent(context, BullpenListViewService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
        clickIntent = new Intent(context, BullpenWidgetProvider.class);
        clickIntent.setAction(Constants.ACTION_SHOW_ITEM);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list);
        // views.setRemoteAdapter(R.id.list, serviceIntent); // For API14+
        rv.setRemoteAdapter(appWidgetId, R.id.list1, serviceIntent);
    
        PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.list1, linkPendingIntent);
    
        Log.i(TAG, "updateAppWidget[BaseballListViewService]");
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        Log.i(TAG, "notifyAppWidgetViewDataChanged[BaseballListViewService]");
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list1);
    }
    
    private void setRemoteViewToShowItem(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    
        Intent serviceIntent, clickIntent;
        
        serviceIntent = new Intent(context, BullpenContentService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(Constants.EXTRA_ITEM_URL, mUrl); // Store mUrl to the serviceIntent
    
        clickIntent = new Intent(context, BullpenWidgetProvider.class);
        clickIntent.setAction(Constants.ACTION_SHOW_LIST);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.content);
        // views.setRemoteAdapter(R.id.list, serviceIntent); // For API14+
        rv.setRemoteAdapter(appWidgetId, R.id.list2, serviceIntent);
    
        PendingIntent linkPendingIntent = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.list2, linkPendingIntent);
    
        Log.i(TAG, "updateAppWidget[BaseballContentService]");
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        Log.i(TAG, "notifyAppWidgetViewDataChanged[BaseballContentService]");
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list2);
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(TAG, "onUpdate");

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.i(TAG, "onDeleted");

        // mHandler = null;

        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        Log.i(TAG, "onDisabled");
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        Log.i(TAG, "onEnabled");
        super.onEnabled(context);
    }

    private void setNewAlarm(Context context) {
        Log.i(TAG, "setNewAlarm");

        long firstTime = System.currentTimeMillis() + Constants.WIDGET_UPDATE_INTERVAL_AT_MILLIS;
        mSender = PendingIntent.getBroadcast(context, 0, mAppWidgetUpdateIntent, 0);
        mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mManager.set(AlarmManager.RTC, firstTime, mSender);
    }

    private void removePreviousAlarm() {
        Log.i(TAG, "removePreviousAlarm");

        if (mManager != null && mSender != null) {
            mSender.cancel();
            mManager.cancel(mSender);
        }
    }

    /*
     * public class ServerThread implements Runnable { private final int
     * clientNum; private ServerSocket serverSocket; private BufferedReader
     * inputReader; private int SERVER_PORT = 13329; public ServerThread(int
     * client) { clientNum = client; } public void sendMessageToClient(String
     * msg) { }
     * @Override public void run() { try { int usedServerPort = SERVER_PORT +
     * clientNum; serverSocket = new ServerSocket(usedServerPort); Log.i(TAG,
     * "Starting server on port[" + usedServerPort + "]"); while (true) { Socket
     * client = serverSocket.accept(); // Read client request inputReader = new
     * BufferedReader( new InputStreamReader(client.getInputStream())); String
     * line = null; while ((line = inputReader.readLine()) != null) { JSONObject
     * json = new JSONObject(line); receivedMessageFromClient(client, json);
     * sendMessageToClient("OK"); } break; } } catch (Exception e) { } } }
     * private void receivedMessageFromClient(Socket client, JSONObject json) {
     * // TODO Auto-generated method stub }
     */
}
