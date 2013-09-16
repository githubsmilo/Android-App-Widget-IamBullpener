
package com.smilo.bullpen.services;

import com.smilo.bullpen.Constants;
import com.smilo.bullpen.Constants.PARSING_RESULT;
import com.smilo.bullpen.ExtraItem;
import com.smilo.bullpen.ListItem;
import com.smilo.bullpen.R;
import com.smilo.bullpen.Utils;
import com.smilo.bullpen.db.DatabaseHandler;
import com.smilo.bullpen.db.DatabaseOpenHelper.ScrapArticleColumns;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
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
    private static ExtraItem mItem = null;
    private static List<ListItem> mListItems = new ArrayList<ListItem>();
    private static BroadcastReceiver mIntentListener;
    private static PARSING_RESULT mParsingResult = PARSING_RESULT.FAILED_UNKNOWN;
    private static int mAddedItemCount = 0;

    public ListViewFactory(Context context, Intent intent) {
        mContext = context;
        
        // Get ExtraItems
        mItem = Utils.createExtraItemFromIntent(intent);
        if (DEBUG) Log.i(TAG, "constructor - mItem[" + mItem.toString() + "]");
        
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
            case SUCCESS_SCRAP_LIST :
                if (position >= mAddedItemCount) {
                    if (DEBUG) Log.e(TAG, "Illegal position number![" + position + "]");
                    return null;
                }
                
                ListItem item = mListItems.get(position);
                String itemTitlePrefix = item.getTitlePrefix();
                String itemTitle = item.getTitle();
                String itemWriter = item.getWriter();
                String itemUrl = item.getUrl();
                int itemCommentNum = item.getCommentNum();
                if (itemCommentNum == Constants.DEFAULT_COMMENT_NUM)
                    rv.setTextViewText(R.id.listRowText, itemTitlePrefix + itemTitle);
                else
                    rv.setTextViewText(R.id.listRowText, itemTitlePrefix + itemTitle + " [" + itemCommentNum + "]");
                
                Intent fillInIntent = new Intent();
                if (itemTitle != null && itemTitle.length() > 0)
                    fillInIntent.putExtra(Constants.EXTRA_ITEM_TITLE, itemTitle);
                if (itemWriter != null && itemWriter.length() > 0)
                    fillInIntent.putExtra(Constants.EXTRA_ITEM_WRITER, itemWriter);
                if (itemUrl != null && itemUrl.length() > 0)
                    fillInIntent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
                if (itemCommentNum != Constants.DEFAULT_COMMENT_NUM)
                    fillInIntent.putExtra(Constants.EXTRA_ITEM_COMMENT_NUM, itemCommentNum);
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
        if (DEBUG) Log.i(TAG, "onDataSetChanged - extraItems[" + mItem + "]");

        // Parse MLBPark html data and add items to the widget item array list.
        try {
            if (Utils.isScrapBoardType(mItem.getBoardType())) {
                mParsingResult = loadScrapListFromDb();
            } else if (Utils.isTodayBestBoardType(mItem.getBoardType())) {
                mParsingResult = parseMLBParkTodayBest(Utils.getBoardUrl(mItem.getBoardType()));
            } else {
                mParsingResult = parseMLBParkMobileBoard(Utils.getMobileBoardUrl(
                        mContext, mItem.getPageNum(), mItem.getBoardType(), mItem.getSearchCategoryType(),
                        mItem.getSearchSubjectType(), mItem.getSearchKeyword()));
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
            mParsingResult == PARSING_RESULT.SUCCESS_MOBILE_TODAY_BEST ||
            mParsingResult == PARSING_RESULT.SUCCESS_SCRAP_LIST) {
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
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.preview_list);
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
    
    private PARSING_RESULT loadScrapListFromDb() {
        // Initialize widget item array list here.
        mListItems.clear();
        
        // Initialize item count.
        mAddedItemCount = 0;
        
        DatabaseHandler handler = DatabaseHandler.open(mContext);
        
        Cursor c = handler.selectAll();
        while (c.moveToNext()) {
            String title = c.getString(ScrapArticleColumns.TITLE_INDEX);
            String writer = c.getString(ScrapArticleColumns.WRITER_INDEX);
            String url = c.getString(ScrapArticleColumns.URL_INDEX);
            ListItem item = new ListItem(Constants.DEFAULT_TITLE_PREFIX, title, writer, url, Constants.DEFAULT_COMMENT_NUM);
            mListItems.add(item);
            mAddedItemCount++;
            if (DEBUG) Log.d(TAG, "loadScrapListFromDb - new ListItem[" + item.toString() + "]");
        }
        handler.close();
        
        if (DEBUG) Log.i(TAG, "loadScrapListFromDb - done!");
        return PARSING_RESULT.SUCCESS_SCRAP_LIST;
    }
    
    private PARSING_RESULT parseMLBParkTodayBest(String urlAddress) throws IOException, JSONException, StackOverflowError {
        // Initialize widget item array list here.
        mListItems.clear();

        Source source = new Source(new URL(Constants.Specific.URL_BASE));
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
                if ((urlAddress.equals(Constants.Specific.URL_MLB_TOWN_TODAY_BEST) && foundTodayBest == 1) ||
                    (urlAddress.equals(Constants.Specific.URL_KBO_TOWN_TODAY_BEST) && foundTodayBest == 2) ||
                    (urlAddress.equals(Constants.Specific.URL_BULLPEN_TODAY_BEST) && foundTodayBest == 3)) {
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
                String titlePrefix = null, title = null, url = null;
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
                                strBuf.append(Constants.Specific.URL_BASE);
                                strBuf.append(url);
                                url = strBuf.toString();
                            }
                        }
                        
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("li")) {
                            // Add widget item array list
                            //Log.i(TAG, "parseMLBParkTodayBest - title[" + title + "],url[" + url + "]");
                            
                            ListItem item = new ListItem(titlePrefix, title, Constants.DEFAULT_WRITER, url, Constants.DEFAULT_COMMENT_NUM);
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
                            if (i == 0) titlePrefix = "[추천] " + itemNum++ + ". ";
                            else if (i == 1) titlePrefix = "[조회] " + itemNum++ + ". ";
                            else if (i == 2) titlePrefix = "[리플] " + itemNum++ + ". ";
                            title = nodeSeg.getTextExtractor().toString();
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

        // Parse black list.
        String[] blackList = null;
        if (mItem.getBlackList() != null) {
            blackList = mItem.getBlackList().split(Constants.DELIMITER_BLACK_LIST);
        }
        
        // Parse blocked words.
        String[] blockedWords = null;
        if (mItem.getBlockedWords() != null) {
            blockedWords = mItem.getBlockedWords().split(Constants.DELIMITER_BLOCKED_WORDS);
        }
        
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
                String title = "", writer = null, url = null, commentNum = "0";
                boolean isAddTitle = false, isAddWriter = false, isAddCommentNum = false, isSkipToAdd = false;
                //Log.i(TAG, "seg[" + seg.toString() + "]");

                // Parse title, writer and url
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
                               strBuf.append(Constants.Specific.URL_BASE);
                               strBuf.append(url);
                               url = strBuf.toString();
                           }
                        } else if (tagName.equals("strong")) {
                            isAddTitle = true;
                        } else if (tagName.equals("em")) {
                            isAddWriter = true;
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
                        } else if (tagName.equals("em")) {
                            isAddWriter = false;
                        }
                        continue;
                        
                    // Ignore &bnsp;
                    } else if (nodeSeg instanceof CharacterReference) {
                        continue;
                        
                    // If plain text, add it to title.
                    } else {
                        if (isAddTitle && !isSkipToAdd) {
                            title += (nodeSeg.getTextExtractor().toString() + " ");
                            if (blockedWords != null) {
                                for(String s : blockedWords) {
                                    if (title.contains(s)) {
                                        if (DEBUG) Log.d(TAG, "parseMLBParkMobileBoard - Skip title[" + title + "]");
                                        isSkipToAdd = true;
                                        break;
                                    }
                                }
                            }
                        } else if (isAddWriter && !isSkipToAdd) {
                            writer = nodeSeg.getTextExtractor().toString();
                            isAddWriter = false;
                            if (blackList != null) {
                                for(String s : blackList) {
                                    if (writer.equals(s)) {
                                        if (DEBUG) Log.d(TAG, "parseMLBParkMobileBoard - Skip writer[" + writer + "]");
                                        isSkipToAdd = true;
                                        break;
                                    }
                                }
                            }
                        } else if (isAddCommentNum && !isSkipToAdd) {
                            commentNum = nodeSeg.getTextExtractor().toString();
                            isAddCommentNum = false;
                        }
                    }
                }

                // Add widget item array list
                if (isSkipToAdd == false) {
                    ListItem item = new ListItem(Constants.DEFAULT_TITLE_PREFIX, title, writer, url, Integer.parseInt(commentNum));
                    if (DEBUG) Log.i(TAG, "parseMLBParkMobileBoard - ListItem[" + item.toString() + "]");
                    mListItems.add(item);
                    mAddedItemCount++;
                }

                if (mAddedItemCount == Constants.Specific.LISTVIEW_MAX_ITEM_COUNT)
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
                    strBuf.append(Constants.Specific.URL_BASE);
                    strBuf.append(url);
                    url = strBuf.toString();
                }
                //Log.i(TAG, "parseMLBParkFullBoard - title[" + title + "], url[" + url + "]");
                
                // Add widget item to array list
                ListItem item = new ListItem(Constants.DEFAULT_TITLE_PREFIX, title, Constants.DEFAULT_WRITER, url, Constants.DEFAULT_COMMENT_NUM);
                mListItems.add(item);
                addedItemCount++;
                
                if (addedItemCount == Constants.Specific.LISTVIEW_MAX_ITEM_COUNT) {
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
                    // Update mItem through Broadcast Intent.
                    ExtraItem item = Utils.createExtraItemFromIntent(intent);
                    mItem.update(item);
                    if (DEBUG) Log.i(TAG, "onReceive - update mItem[" + mItem.toString() + "]");
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
