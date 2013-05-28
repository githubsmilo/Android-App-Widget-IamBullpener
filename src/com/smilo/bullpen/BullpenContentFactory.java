
package com.smilo.bullpen;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class BullpenContentFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "BullpenContentFactory";

    private String mContent = "Empty";
    private Context mContext;
    private int mAppWidgetId;
    private String mUrl = null;

    //private ConnectivityManager mConnectivityManager;
    
    private BroadcastReceiver mIntentListener;

    public BullpenContentFactory(Context context, Intent intent) {
        Log.i(TAG, "constructor");

        mContext = context;
        mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
        Log.i(TAG, "constructor - mUrl[" + mUrl + "]");
        
        //mConnectivityManager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        setupIntentListener();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.i(TAG, "getViewAt");

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.content_row);
        rv.setTextViewText(android.R.id.text2, mContent);

        Intent fillInIntent = new Intent();
        rv.setOnClickFillInIntent(android.R.id.text2, fillInIntent);

        return rv;
    }
    
    @Override
    public void onDataSetChanged() {
        Log.i(TAG, "onDataSetChanged");

        // We check internet connection when BaseballWidgetProvider receives intent ACTION_SHOW_ITEM.
        // So just skip to check it here.
        //if (Utils.checkInternetConnectivity(mConnectivityManager)) {
        if (mUrl != null) {
            // Parse MLBPark html data and add items to the widget item array list.
            try {
                parseMLBParkHtmlData(mUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //}
    }
    
    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }
    
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        // no-op
    }
    
    @Override
    public void onDestroy() {
        teardownIntentListener();
    }

    private void parseMLBParkHtmlData(String urlAddress) throws IOException {
        Source source = new Source(new URL(urlAddress));
        source.fullSequentialParse();

        // Initialize widget item array list here.
        mContent = null;

        List<Element> divs = source.getAllElements(HTMLElementName.DIV);

        for (int i = 0; i < divs.size(); i++) {
            Element div = divs.get(i);

            String value = div.getAttributeValue("align");
            // Find the same pattern with <div align="justify"
            if (value != null && value.equals("justify")) {
                mContent = div.getContent().getTextExtractor().toString();
                Log.i(TAG, "mContent[" + mContent + "]");

                return;
            }
        }
    }

    private void setupIntentListener() {
        if (mIntentListener == null) {
            mIntentListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Update mUrl through Broadcast Intent.
                    mUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_UPDATE_URL);
            mContext.registerReceiver(mIntentListener, filter);
        }
    }

    private void teardownIntentListener() {
        if (mIntentListener != null) {
            mContext.unregisterReceiver(mIntentListener);
            mIntentListener = null;
        }
    }
}
