
package com.smilo.bullpen;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BullpenListViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "BullpenListViewFactory";

    private List<widgetItem> mWidgetItems = new ArrayList<widgetItem>();
    private Context mContext;
    private int mAppWidgetId;
    private ConnectivityManager mConnectivityManager;
    
    private class widgetItem {
        String widgetTitle;
        String widgetUrl;

        widgetItem(String title, String url) {
            widgetTitle = title;
            widgetUrl = url;
        }

        public String getTitle() {
            return widgetTitle;
        }

        public String getUrl() {
            return widgetUrl;
        }
    }

    public BullpenListViewFactory(Context context, Intent intent) {
        Log.i(TAG, "constructor");

        mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mContext = context;
        
        mConnectivityManager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.i(TAG, "getViewAt - position[" + position + "]");

        // Create a RemoteView and set widget item array list to the RemoteView.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                R.layout.list_row);
        rv.setTextViewText(android.R.id.text1, mWidgetItems.get(position).getTitle());

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.EXTRA_ITEM_URL, mWidgetItems.get(position).getUrl());
        rv.setOnClickFillInIntent(android.R.id.text1, fillInIntent);

        return rv;
    }

    @Override
    public void onDataSetChanged() {
        Log.i(TAG, "onDataSetChanged");

        if (Utils.checkInternetConnectivity(mConnectivityManager)) {
            // Parse MLBPark html data and add items to the widget item array list.
            try {
                parseMLBParkHtmlData(Constants.mMLBParkUrl_mlbtown);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "onDataSetChanged - Internet net connected!");
        }
    }

    @Override
    public int getCount() {
        return Constants.LISTVIEW_MAX_ITEM_COUNT;
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
        // no-op
    }

    private void parseMLBParkHtmlData(String urlAddress) throws IOException {
        Source source = new Source(new URL(urlAddress));
        source.fullSequentialParse();

        // Initialize widget item array list here.
        mWidgetItems.clear();

        List<Element> tables = source.getAllElements(HTMLElementName.TABLE);
        int addedItemCount = 0;
        for (int i = 0; i < tables.size(); i++) {
            Element table = tables.get(i);

            // Find the same pattern with <table width='100%' border='0' cellspacing='6' cellpadding='0'
            if (table.getAttributeValue("width").equals("100%")
                    && table.getAttributeValue("border").equals("0")
                    && table.getAttributeValue("cellspacing").equals("6")
                    && table.getAttributeValue("cellpadding").equals("0")) {

                Segment content = table.getFirstElementByClass("G12read")
                        .getContent();

                // Get title and url
                String title = content.getTextExtractor().toString();
                String url = content.getURIAttributes().get(0).getValue();
                if (url.startsWith("/")) {
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append(Constants.mMLBParkUrl_base);
                    strBuf.append(url);
                    url = strBuf.toString();
                }
                Log.i(TAG, "parseMLBParkHtmlData - title[" + title + "],url["
                        + url + "]");

                // Add widget item array list
                widgetItem item = new widgetItem(title, url);
                mWidgetItems.add(item);
                addedItemCount++;

                if (addedItemCount == Constants.LISTVIEW_MAX_ITEM_COUNT) {
                    return;
                }
            }
        }
    }
}
