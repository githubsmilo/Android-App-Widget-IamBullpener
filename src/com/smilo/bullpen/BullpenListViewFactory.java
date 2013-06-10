
package com.smilo.bullpen;

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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BullpenListViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "BullpenListViewFactory";

    private static List<listItem> mlistItems = new ArrayList<listItem>();
    private static Context mContext;
    private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static String mSelectedBullpenBoardUrl = null;
    private static BroadcastReceiver mIntentListener;
    private static boolean mIsSuccessToParse = false;

    private class listItem {
        public String itemTitle;
        public String itemUrl;

        listItem(String title, String url) {
            itemTitle = title;
            itemUrl = url;
        }
    }

    public BullpenListViewFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mSelectedBullpenBoardUrl = intent.getStringExtra(Constants.EXTRA_LIST_URL);
        Log.i(TAG, "constructor - mSelectedBullpenBoardUrl[" + mSelectedBullpenBoardUrl + "], mAppWidgetId[" + mAppWidgetId + "]");
        
        setupIntentListener();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i(TAG, "getViewAt - position[" + position + "]");

        // Create a RemoteView and set widget item array list to the RemoteView.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_row);
        if (mIsSuccessToParse) {
            rv.setTextViewText(R.id.listRowText, mlistItems.get(position).itemTitle);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(Constants.EXTRA_ITEM_URL, mlistItems.get(position).itemUrl);
            rv.setOnClickFillInIntent(R.id.listRowText, fillInIntent);
        } else {
            rv.setTextViewText(R.id.listRowText, mContext.getResources().getString(R.string.text_failedToParse));
        }

        return rv;
    }

    @Override
    public void onDataSetChanged() {
        Log.i(TAG, "onDataSetChanged - mSelectedBullpenBoardUrl[" + mSelectedBullpenBoardUrl + "]");

        if (mSelectedBullpenBoardUrl == null) {
            Log.e(TAG, "onDataSetChanged - mSelectedBullpenBoardUrl is null!");
            return;
        }
        
        // Parse MLBPark html data and add items to the widget item array list.
        try {
            mIsSuccessToParse = (parseMLBParkHtmlDataMobileVer(mSelectedBullpenBoardUrl) == true) ? true : false;
        } catch (IOException e) {
            Log.e(TAG, "onDataSetChanged - IOException![" + e.toString() + "]");
            e.printStackTrace();
            mIsSuccessToParse = false;
        }
    }

    @Override
    public int getCount() {
    	//Log.i(TAG, "getCount");
    	
    	if (mIsSuccessToParse) {
              return Constants.LISTVIEW_MAX_ITEM_COUNT;
    	} else {
              return 1;
    	}
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_row);
        rv.setTextViewText(R.id.listRowText, mContext.getResources().getString(R.string.text_loadingView));

        return rv;
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
    
    private boolean parseMLBParkHtmlDataMobileVer(String urlAddress) throws IOException {
        // Initialize widget item array list here.
        mlistItems.clear();
        
        Source source = new Source(new URL(urlAddress));
        source.fullSequentialParse();
        
        // Find the same pattern with <ul id="mNewsList">. This means the body of this article.
        List<Element> uls = source.getAllElements(HTMLElementName.UL);
        Element targetUl = null;
        for (int i = 0 ; i < uls.size() ; i++ ) {
            Element ul = uls.get(i);
            String idAttr = ul.getAttributeValue("id");
            if (idAttr != null && idAttr.equals("mNewsList")) {
                targetUl = ul;
                break;
            }
        }

        if ((targetUl != null) && (targetUl.isEmpty() == false)) {
            List<Element> lis = targetUl.getAllElements(HTMLElementName.LI);
            int addedItemCount = 0;
            
            for (int i = 0 ; i < lis.size() ; i++ ) {
                Segment seg = lis.get(i).getContent();
                String title = null, url = null;
                boolean isAddTitle = false, isAddCommentNum = false;
                //Log.i(TAG, "seg[" + seg.toString() + "]");

                // Parse title
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    
                    // If <a href> tag, add address to url.
                    // if <strong> tag, prepare to add title.
                    // If <span class="r"> tag, prepare to add comment number.
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("a")) {
                           url = ((StartTag) nodeSeg).getAttributeValue("href");
                           if (url.startsWith("/")) {
                               StringBuffer strBuf = new StringBuffer();
                               strBuf.append(Constants.mMLBParkUrl_base);
                               strBuf.append(url);
                               url = strBuf.toString();
                           }
                        } else if (tagName.equals("strong")) {
                            isAddTitle = true;
                        } else if (tagName.equals("span")) {
                            String spanClass = ((StartTag) nodeSeg).getAttributeValue("class");
                            if (spanClass != null && spanClass.equals("r")) {
                                isAddCommentNum = true;
                            }
                        }
                        continue;
                        
                    } else if (nodeSeg instanceof EndTag) {
                        // Do nothing
                        continue;
                        
                     // Ignore &bnsp;
                    } else if (nodeSeg instanceof CharacterReference) {
                        continue;
                        
                    // If plain text, add it to title.
                    } else {
                        if (isAddTitle) {
                            title = nodeSeg.getTextExtractor().toString();
                            isAddTitle = false;
                        } else if (isAddCommentNum) {
                            title += (" [" + nodeSeg.getTextExtractor().toString() + "]");
                            isAddCommentNum = false;
                        }
                    }
                }
                //Log.i(TAG, "parseMLBParkHtmlDataMobileVer - title[" + title + "],url[" + url + "]");
                
                // Add widget item array list
                listItem item = new listItem(title, url);
                mlistItems.add(item);
                addedItemCount++;

                if (addedItemCount == Constants.LISTVIEW_MAX_ITEM_COUNT) {
                    Log.i(TAG, "parseMLBParkHtmlDataMobileVer - done!");
                    return true;
                }
            }
            
            return true;
        } else {
            Log.e(TAG, "parseMLBParkHtmlDataMobileVer - Cannot find article list.");
            return false;
        }
    }
    
    private void setupIntentListener() {
	    if (mIntentListener == null) {
	        mIntentListener = new BroadcastReceiver() {
	            @Override
	            public void onReceive(Context context, Intent intent) {
	                // Update mSelectedBullpenBoardUrl through Broadcast Intent.
	                mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	                mSelectedBullpenBoardUrl = intent.getStringExtra(Constants.EXTRA_LIST_URL);
	                Log.i(TAG, "onReceive - update mSelectedBullpenBoardUrl[" + mSelectedBullpenBoardUrl + "], mAppWidgetId[" + mAppWidgetId + "]");
	            }
	        };
	        IntentFilter filter = new IntentFilter();
	        filter.addAction(Constants.ACTION_UPDATE_LIST_URL);
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
