
package com.smilo.bullpen;

import com.smilo.bullpen.Constants.PARSING_RESULT;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.json.JSONException;

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

public class ListViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "ListViewFactory";
    private static final boolean DEBUG = Constants.DEBUG_MODE;

    private static Context mContext;
    private static List<listItem> mListItems = new ArrayList<listItem>();
    private static BroadcastReceiver mIntentListener;
    private static PARSING_RESULT mParsingResult = PARSING_RESULT.FAILED_UNKNOWN;
    private static int mAddedItemCount = 0;
    
    // intent item list
    private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static int mPageNum = Constants.ERROR_PAGE_NUM;
    private static int mBoardType = Constants.ERROR_BOARD_TYPE;
    //private static int mRefreshTimetype = Constants.ERROR_REFRESH_TIME_TYPE;
    //private static boolean mIsPermitMobileConnectionType = Constants.ERROR_PERMIT_MOBILE_CONNECTION_TYPE;
    private static int mSelectedSearchCategoryType = Constants.ERROR_SEARCH_CAGETORY_TYPE;
    private static int mSelectedSearchSubjectType = Constants.ERROR_SEARCH_SUBJECT_TYPE;
    private static String mSelectedSearchKeyword = null;

    private class listItem {
        public String itemTitle;
        public String itemUrl;

        listItem(String title, String url) {
            itemTitle = title;
            itemUrl = url;
        }
    }

    public ListViewFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mPageNum = intent.getIntExtra(
                Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
        mBoardType = intent.getIntExtra(
                Constants.EXTRA_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
        mSelectedSearchCategoryType = intent.getIntExtra(
                Constants.EXTRA_SEARCH_CATEGORY_TYPE, Constants.ERROR_SEARCH_CAGETORY_TYPE);
        mSelectedSearchSubjectType = intent.getIntExtra(
                Constants.EXTRA_SEARCH_SUBJECT_TYPE, Constants.ERROR_SEARCH_SUBJECT_TYPE);
        mSelectedSearchKeyword = intent.getStringExtra(
                Constants.EXTRA_SEARCH_KEYWORD);

        if (DEBUG) Log.i(TAG, "constructor - mAppWidgetId[" + mAppWidgetId + 
                "], mPageNum[" + mPageNum + "], mBoardType[" + mBoardType +
                "], mSelectedSearchCategoryType[" + mSelectedSearchCategoryType + 
                "], mSelectedSearchSubjectType[" + mSelectedSearchSubjectType + 
                "], mSelectedSearchKeyword[" + mSelectedSearchKeyword + "]");
        
        setupIntentListener();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i(TAG, "getViewAt - position[" + position + "]");

        // Create a RemoteView and set widget item array list to the RemoteView.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_row);
        
        switch (mParsingResult) {
            case SUCCESS_FULL_BOARD :
            case SUCCESS_MOBILE_BOARD :
            case SUCCESS_MOBILE_TODAY_BEST :
                rv.setTextViewText(R.id.listRowText, mListItems.get(position).itemTitle);
                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(Constants.EXTRA_ITEM_URL, mListItems.get(position).itemUrl);
                rv.setOnClickFillInIntent(R.id.listRowText, fillInIntent);
                break;
                
            case FAILED_IO_EXCEPTION :
                rv.setTextViewText(R.id.listRowText, mContext.getResources().getString(R.string.text_failed_io_exception));
                break;
                
            case FAILED_JSON_EXCEPTION :
                rv.setTextViewText(R.id.listRowText, mContext.getResources().getString(R.string.text_failed_json_exception));
                break;
                
            case FAILED_STACK_OVERFLOW :
                rv.setTextViewText(R.id.listRowText, mContext.getResources().getString(R.string.text_failed_stack_overflow));
                break;
                
            case FAILED_UNKNOWN :
            default:
                rv.setTextViewText(R.id.listRowText, mContext.getResources().getString(R.string.text_failed_unknown));
                break;
        }

        return rv;
    }

    @Override
    public void onDataSetChanged() {
        if (DEBUG) Log.i(TAG, "onDataSetChanged - mBoardType[" + mBoardType + "], mPageNum[" + mPageNum + "]");

        if (mBoardType == Constants.ERROR_BOARD_TYPE) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - mBoardType is invalid![" + mBoardType + "]");
            return;
        } else if (mPageNum == Constants.ERROR_PAGE_NUM) {
            if (DEBUG) Log.e(TAG, "onDatasetChanged - mPageNum is invalid![" + mPageNum + "]");
            return;
        }

        // Parse MLBPark html data and add items to the widget item array list.
        try {
            if (Utils.isTodayBestBoardType(mBoardType)) {
                mParsingResult = parseMLBParkTodayBest(Utils.getBoardUrl(mBoardType));
            } else {
                if ((mSelectedSearchCategoryType == Constants.ERROR_SEARCH_CAGETORY_TYPE) ||
                      ((mSelectedSearchCategoryType != Constants.SEARCH_CATEGORY_TYPE_SUBJECT) && 
                       (mSelectedSearchKeyword == null || mSelectedSearchKeyword.equals("")))) {
                    mParsingResult = parseMLBParkMobileBoard(Utils.getBoardUrl(mBoardType) + mPageNum);
                } else {
                    mParsingResult = parseMLBParkMobileBoard(Utils.getBoardUrl(mBoardType) + mPageNum +
                            Utils.getSearchUrl(mContext, mSelectedSearchCategoryType, mSelectedSearchSubjectType, mSelectedSearchKeyword));
                }
            }
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - IOException![" + e.toString() + "]");
            e.printStackTrace();
            mParsingResult = PARSING_RESULT.FAILED_IO_EXCEPTION;
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - JSONException![" + e.toString() + "]");
            e.printStackTrace();
            mParsingResult = PARSING_RESULT.FAILED_JSON_EXCEPTION;
        } catch (StackOverflowError e) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - StackOverflowError![" + e.toString() + "]");
            e.printStackTrace();
            mParsingResult = PARSING_RESULT.FAILED_STACK_OVERFLOW;
        }
    }

    @Override
    public int getCount() {
        //Log.i(TAG, "getCount");
        
        if (mParsingResult == PARSING_RESULT.SUCCESS_FULL_BOARD ||
            mParsingResult == PARSING_RESULT.SUCCESS_MOBILE_BOARD ||
            mParsingResult == PARSING_RESULT.SUCCESS_MOBILE_TODAY_BEST) {
            return mAddedItemCount;
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
    
    private PARSING_RESULT parseMLBParkTodayBest(String urlAddress) throws IOException, JSONException, StackOverflowError {
        // Initialize widget item array list here.
        mListItems.clear();

        Source source = new Source(new URL(Constants.URL_BASE));
        source.fullSequentialParse();
        
        // Find the same pattern with <div class='today_best'>. This means the 'today best' and we can find three items.
        // First 'today best' item is belong to the MLB town.
        // Second 'today best' item is belong to the KBO town.
        // Third 'today best' item is belong to the Bullpen.
        List<Element> divs = source.getAllElements(HTMLElementName.DIV);
        Element targetDiv = null;
        int foundTodayBest = 0;
        for (int i = 0 ; i < divs.size() ; i++) {
            Element div = divs.get(i);
            String classAttr = div.getAttributeValue("class");
            if (classAttr != null && classAttr.equals("today_best")) {
                foundTodayBest++;
                if ((urlAddress.equals(Constants.URL_MLB_TOWN_TODAY_BEST) && foundTodayBest == 1) ||
                    (urlAddress.equals(Constants.URL_KBO_TOWN_TODAY_BEST) && foundTodayBest == 2) ||
                    (urlAddress.equals(Constants.URL_BULLPEN_TODAY_BEST) && foundTodayBest == 3)) {
                    targetDiv = div;
                    break;
                }
            }
        }
        
        if ((targetDiv != null) && (targetDiv.isEmpty() == false)) {
            List<Element> ols = targetDiv.getAllElements(HTMLElementName.OL);
            mAddedItemCount = 0;
            
            for (int i = 0 ; i < ols.size() ; i++) {
                Segment seg = ols.get(i).getContent();
                String title = null, url = null;
                boolean isStartLiTag = false, isAddTitle = false;
                int itemNum = 1;
                //Log.i(TAG, "seg[" + seg.toString() + "]");
            
                // Parse title and url
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("li")) {
                            isStartLiTag = true;
                        } else if (tagName.equals("a") && isStartLiTag == true) {
                            url = ((StartTag)nodeSeg).getAttributeValue("href");
                            if (url.startsWith("/")) {
                                StringBuffer strBuf = new StringBuffer();
                                strBuf.append(Constants.URL_BASE);
                                strBuf.append(url);
                                url = strBuf.toString();
                            }
                        }
                        
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("li")) {
                            // Add widget item array list
                            //Log.i(TAG, "parseMLBParkTodayBest - title[" + title + "],url[" + url + "]");
                            
                            listItem item = new listItem(title, url);
                            mListItems.add(item);
                            title = null;
                            url = null;
                            mAddedItemCount++;
                            isStartLiTag = false;
                        } else if (tagName.equals("strong") && isStartLiTag == true) {
                            isAddTitle = true;
                        }
                      
                    // Ignore &bnsp;
                    } else if (nodeSeg instanceof CharacterReference) {
                        continue;
                        
                    // If plain text, add it to title.
                    } else {
                        if (isAddTitle) {
                            if (i == 0) title = "[추천] " + itemNum++ + ". ";
                            else if (i == 1) title = "[조회] " + itemNum++ + ". ";
                            else if (i == 2) title = "[리플] " + itemNum++ + ". ";
                            title += nodeSeg.getTextExtractor().toString();
                            isAddTitle = false;
                        }
                        
                    }
                }
            }

            if (DEBUG) Log.i(TAG, "parseMLBParkTodayBest - done!");
            return PARSING_RESULT.SUCCESS_MOBILE_TODAY_BEST;
            
        } else {
            if (DEBUG) Log.e(TAG, "parseMLBParkTodayBest - Cannot find today best element.");
            return PARSING_RESULT.FAILED_UNKNOWN;
        }
    }

    private PARSING_RESULT parseMLBParkMobileBoard(String urlAddress) throws IOException, JSONException, StackOverflowError {
        // Initialize widget item array list here.
        mListItems.clear();
        
        Source source = new Source(new URL(urlAddress));
        source.fullSequentialParse();
        
        // Find the same pattern with <ul id="mNewsList">. This means the body of this article.
        List<Element> uls = source.getAllElements(HTMLElementName.UL);
        Element targetUl = null;
        for (int i = 0 ; i < uls.size() ; i++) {
            Element ul = uls.get(i);
            String idAttr = ul.getAttributeValue("id");
            if (idAttr != null && idAttr.equals("mNewsList")) {
                targetUl = ul;
                break;
            }
        }

        if ((targetUl != null) && (targetUl.isEmpty() == false)) {
            List<Element> lis = targetUl.getAllElements(HTMLElementName.LI);
            mAddedItemCount = 0;
            
            for (int i = 0 ; i < lis.size() ; i++) {
                Segment seg = lis.get(i).getContent();
                String title = "", url = null;
                boolean isAddTitle = false, isAddCommentNum = false;
                //Log.i(TAG, "seg[" + seg.toString() + "]");

                // Parse title and url
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    
                    // If <a href> tag, add address to url.
                    // if <strong> tag, prepare to add title.
                    // If <span class="r"> tag, prepare to add comment number.
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("a")) {
                           url = ((StartTag)nodeSeg).getAttributeValue("href");
                           if (url.startsWith("/")) {
                               StringBuffer strBuf = new StringBuffer();
                               strBuf.append(Constants.URL_BASE);
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
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("strong")) {
                            isAddTitle = false;
                        }
                        continue;
                        
                    // Ignore &bnsp;
                    } else if (nodeSeg instanceof CharacterReference) {
                        continue;
                        
                    // If plain text, add it to title.
                    } else {
                        if (isAddTitle) {
                            title += (nodeSeg.getTextExtractor().toString() + " ");
                        } else if (isAddCommentNum) {
                            title += (" [" + nodeSeg.getTextExtractor().toString() + "]");
                            isAddCommentNum = false;
                        }
                    }
                }
                //Log.i(TAG, "parseMLBParkMobileBoard - title[" + title + "],url[" + url + "]");
                
                // Add widget item array list
                listItem item = new listItem(title, url);
                mListItems.add(item);
                mAddedItemCount++;

                if (mAddedItemCount == Constants.LISTVIEW_MAX_ITEM_COUNT)
                    break;
            }
            
            if (DEBUG) Log.i(TAG, "parseMLBParkMobileBoard - done!");
            return PARSING_RESULT.SUCCESS_MOBILE_BOARD;
        } else {
            if (DEBUG) Log.e(TAG, "parseMLBParkMobileBoard - Cannot find article list.");
            return PARSING_RESULT.FAILED_UNKNOWN;
        }
    }
    
    private PARSING_RESULT parseMLBParkFullBoard(String urlAddress) throws IOException, JSONException, StackOverflowError {
        // Initialize widget item array list here
        mListItems.clear();
        
        Source source = new Source(new URL(urlAddress));
        source.fullSequentialParse();
        
        List<Element> tables = source.getAllElements(HTMLElementName.TABLE);
        int addedItemCount = 0;
        for (int i = 0 ; i < tables.size() ; i++) {
            Element table = tables.get(i);

            // Find the same pattern with <table width='100%' border='0' cellspacing='6' cellpadding='0'>
            String widthAttr = table.getAttributeValue("width");
            String borderAttr = table.getAttributeValue("border");
            String cellspacingAttr = table.getAttributeValue("cellspacing");
            String cellpaddingAttr = table.getAttributeValue("cellpadding");
            if ((widthAttr != null && widthAttr.equals("100%")) &&
                  (borderAttr != null && borderAttr.equals("0")) &&
                  (cellspacingAttr != null && cellspacingAttr.equals("6")) &&
                  (cellpaddingAttr != null && cellpaddingAttr.equals("0"))) {
                
                Segment content;
                
                // Skip Notice
                Element notice = table.getFirstElementByClass("Allgray");
                if (notice != null) {
                    content = notice.getContent();
                    if (content.getTextExtractor().toString().equals("공지"))
                        continue;
                }
                
                content = table.getFirstElementByClass("G12read").getContent();
                
                // Get title and url
                String title = content.getTextExtractor().toString();
                String url = content.getURIAttributes().get(0).getValue();
                if (url.startsWith("/")) {
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append(Constants.URL_BASE);
                    strBuf.append(url);
                    url = strBuf.toString();
                }
                //Log.i(TAG, "parseMLBParkFullBoard - title[" + title + "], url[" + url + "]");
                
                // Add widget item to array list
                listItem item = new listItem(title, url);
                mListItems.add(item);
                addedItemCount++;
                
                if (addedItemCount == Constants.LISTVIEW_MAX_ITEM_COUNT) {
                    if (DEBUG) Log.i(TAG, "parseMLBParkFullBoard - done!");
                    return PARSING_RESULT.SUCCESS_FULL_BOARD;
                }
            }
        }

        return PARSING_RESULT.FAILED_UNKNOWN;
    }

    private void setupIntentListener() {
        if (mIntentListener == null) {
            mIntentListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Update itent items through Broadcast Intent.
                    mAppWidgetId = intent.getIntExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    mBoardType = intent.getIntExtra(
                            Constants.EXTRA_BOARD_TYPE, Constants.ERROR_BOARD_TYPE);
                    mPageNum = intent.getIntExtra(
                            Constants.EXTRA_PAGE_NUM, Constants.ERROR_PAGE_NUM);
                    mSelectedSearchCategoryType = intent.getIntExtra(
                            Constants.EXTRA_SEARCH_CATEGORY_TYPE, Constants.ERROR_SEARCH_CAGETORY_TYPE);
                    mSelectedSearchSubjectType = intent.getIntExtra(
                            Constants.EXTRA_SEARCH_SUBJECT_TYPE, Constants.ERROR_SEARCH_SUBJECT_TYPE);
                    mSelectedSearchKeyword = intent.getStringExtra(Constants.EXTRA_SEARCH_KEYWORD);
                    
                    if (DEBUG) Log.i(TAG, "onReceive - update mAppWidgetId[" + mAppWidgetId + 
                            "], mPageNum[" + mPageNum + "], mBoardType[" + mBoardType +
                            "], mSelectedSearchCategoryType[" + mSelectedSearchCategoryType +
                            "], mSelectedSearchSubjectType[" + mSelectedSearchSubjectType +
                            "], mSelectedSearchKeyword[" + mSelectedSearchKeyword + "]");
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_UPDATE_LIST_INFO);
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
