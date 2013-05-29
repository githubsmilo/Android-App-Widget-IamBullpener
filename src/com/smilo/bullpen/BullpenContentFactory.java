
package com.smilo.bullpen;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class BullpenContentFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "BullpenContentFactory";

    private String mContent = "Empty";
    private Context mContext;
    private int mAppWidgetId;
    private String mSelectedItemUrl = null;

    //private ConnectivityManager mConnectivityManager;
    
    private BroadcastReceiver mIntentListener;

    public BullpenContentFactory(Context context, Intent intent) {
        Log.i(TAG, "constructor");

        mContext = context;
        mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
        Log.i(TAG, "constructor - mSelectedItemUrl[" + mSelectedItemUrl + "]");
        
        //mConnectivityManager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        setupIntentListener();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i(TAG, "getViewAt - position[" + position + "]");

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.content_row);
        rv.setTextViewText(R.id.contentRowBodyText, mContent);
        rv.setTextViewText(R.id.contentRowCommentText, "Comment here");

        Intent fillInIntent = new Intent();
        rv.setOnClickFillInIntent(R.id.contentRowLayout, fillInIntent);

        return rv;
    }
    
    @Override
    public void onDataSetChanged() {
        Log.i(TAG, "onDataSetChanged");

        // We check internet connection when BaseballWidgetProvider receives intent ACTION_SHOW_ITEM.
        // So just skip to check it here.
        //if (Utils.checkInternetConnectivity(mConnectivityManager)) {
        if (mSelectedItemUrl != null) {
            // Parse MLBPark html data and add items to the widget item array list.
            try {
                parseMLBParkHtmlData(mSelectedItemUrl);
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
        mContent = "";

        List<Element> divs = source.getAllElements(HTMLElementName.DIV);

        for (int i = 0; i < divs.size(); i++) {
            Element div = divs.get(i);

            String value = div.getAttributeValue("align");
            // Find the same pattern with <div align="justify"
            if (value != null && value.equals("justify")) {
                Segment seg = div.getContent();
                boolean isSkip = false;
                //Log.i(TAG, "content segment[" + seg.toString() + "]");
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSegment = nodeIterator.next();
                    if (nodeSegment instanceof StartTag) {
                        String tagName = ((Tag)nodeSegment).getName();
                        if (tagName.equals("head")) {
                            isSkip = true;
                        } else if (!isSkip && tagName.equals("br")) {
                            mContent += "\n";
                        }
                        continue;
                    } else if (nodeSegment instanceof EndTag) {
                        String tagName = ((Tag)nodeSegment).getName();
                        if (tagName.equals("head")) {
                            isSkip = false;
                        } else if (!isSkip && (tagName.equals("p") || tagName.equals("div"))) {
                            mContent += "\n";
                        }
                        continue;
                    } else if (nodeSegment instanceof CharacterReference) {
                        // Ignore &nbsp;
                        continue;
                    } else { // plain text
                        if (!isSkip && (nodeSegment.isWhiteSpace() == false)) {
                            mContent += nodeSegment.toString();
                        }
                        continue;
                    }
                }
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
                    // Update mSelectedItemUrl through Broadcast Intent.
                    Log.i(TAG, "onReceive - update mSelectedItemUrl[" + mSelectedItemUrl + "]");
                    mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
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
