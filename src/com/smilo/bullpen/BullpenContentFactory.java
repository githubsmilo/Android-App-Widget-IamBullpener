
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;

public class BullpenContentFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "BullpenContentFactory";

    private static contentItem mContentItem = null;
    private static Context mContext;
    private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static String mSelectedItemUrl = null;
    private static BroadcastReceiver mIntentListener;
    private static Bitmap mBitmap = null;
    private static boolean mIsSuccessToParse = false;

    private class contentItem {
        public String itemTitle = null;
        public String itemBody = null;
        public String itemImgUrl = null;
        public String itemComment = null;

        contentItem(String title, String body, String imgUrl, String comment) {
            itemTitle = title;
            itemBody = body;
            itemImgUrl = imgUrl;
            itemComment = comment;
        }

        public boolean isEmptyTitle() {
            return ((itemTitle == null) || (itemTitle.equals("")));
        }
        
        public boolean isEmptyBody() {
            return ((itemBody == null) || (itemBody.equals("")));
        }
        
        public boolean isEmptyImgUrl() {
            return ((itemImgUrl == null) || (itemImgUrl.equals("")));
        }
        
        public boolean isEmptyComment() {
            return ((itemComment == null) || (itemComment.equals("")));
        }
        
        public String toString() {
            return ("itemTitle[" + itemTitle + "], itemBody[" + itemBody + "], itemImgUrl[" + itemImgUrl + "], itemComment[" + itemComment + "]");
        }
    }

    public BullpenContentFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
        Log.i(TAG, "constructor - mSelectedItemUrl[" + mSelectedItemUrl + "], mAppWidgetId[" + mAppWidgetId + "]");
        
        setupIntentListener();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i(TAG, "getViewAt - position[" + position + "]");

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.content_row);
        if (mIsSuccessToParse) {
            if (mContentItem.isEmptyTitle() == false)
                rv.setTextViewText(R.id.contentRowTitleText, mContentItem.itemTitle);
            
            if (mContentItem.isEmptyBody() == false)
                rv.setTextViewText(R.id.contentRowBodyText, mContentItem.itemBody);

            if (mContentItem.isEmptyImgUrl() == false) {
                mBitmap = getImageBitmap(mContentItem.itemImgUrl);
                if (mBitmap != null) {
                    //Log.i(TAG, "setImageViewBitmap - given bitmap");
                    rv.setImageViewBitmap(R.id.contentRowImage, mBitmap);
                } else {
                    //Log.i(TAG, "setImageViewBitmap - null1");
                    rv.setImageViewBitmap(R.id.contentRowImage, null);
                }
            } else {
                //Log.i(TAG, "setImageViewBitmap - null2");
                rv.setImageViewBitmap(R.id.contentRowImage, null);
            }

            if (mContentItem.isEmptyComment() == false)
                rv.setTextViewText(R.id.contentRowCommentText, mContentItem.itemComment);

            Intent fillInIntent = new Intent();
            rv.setOnClickFillInIntent(R.id.contentRowLayout, fillInIntent);
        } else {
            rv.setTextViewText(R.id.contentRowTitleText, mContext.getResources().getString(R.string.text_failedToParse));
        }

        return rv;
    }
    
    @Override
    public void onDataSetChanged() {
        Log.i(TAG, "onDataSetChanged - mSelectedItemUrl[" + mSelectedItemUrl + "]");

        if (mSelectedItemUrl == null) {
            Log.e(TAG, "onDataSetChanged - mSelectedItemUrl is null!");
            return;
        }
        
        // Parse MLBPark html data and add items to the widget item array list.
        try {
            mIsSuccessToParse = (parseMLBParkHtmlDataMobileVer(mSelectedItemUrl) == true) ? true : false;
        } catch (IOException e) {
        	Log.e(TAG, "onDataSetChanged - IOException![" + e.toString() + "]");
            e.printStackTrace();
            mIsSuccessToParse = false;
        }
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
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.content_row);
        rv.setTextViewText(R.id.contentRowTitleText, mContext.getResources().getString(R.string.text_loadingView));

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

    private void parseMLBParkHtmlDataFullVer(String urlAddress) throws IOException {
        Source source = new Source(new URL(urlAddress));
        source.fullSequentialParse();

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        
        // TODO : parse itemTitle!
        String itemTitle = "";
        String itemBody = "";
        String itemImgUrl = "";
        String itemComment = "";

        List<Element> divs = source.getAllElements(HTMLElementName.DIV);
        for (int i = 0; i < divs.size(); i++) {
            Element div = divs.get(i);
            String value;
            
            // Find the same pattern with <div align="justify". This means the body of this article.
            value = div.getAttributeValue("align");
            if (value != null && value.equals("justify")) {
                Segment seg = div.getContent();
                boolean isSkipSegment = false;
                //Log.i(TAG, "content segment[" + seg.toString() + "]");
                
                // Parse article body.
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    
                    // If article body has <head>...</head>, just skip!
                    // If <br> tag, add new line to itemBody.
                    // if <img> tag, add img address to itemImgUrl.
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("head")) {
                            isSkipSegment = true;
                        } else if (!isSkipSegment && tagName.equals("br")) {
                            itemBody += "\n";
                        } else if (!isSkipSegment && tagName.equals("img")) {
                            itemImgUrl = ((StartTag) nodeSeg).getAttributeValue("src");
                        }
                        continue;
                        
                    // If </p> or </div> tag, add new line to itemBody.
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("head")) {
                            isSkipSegment = false;
                        } else if (!isSkipSegment && (tagName.equals("p") || tagName.equals("div"))) {
                            itemBody += "\n";
                        }
                        continue;
                        
                    // Ignore &bnsp;
                    } else if (nodeSeg instanceof CharacterReference) {
                        continue;
                        
                    // If plain text, add it to itemBody.
                    } else {
                        if (!isSkipSegment && (nodeSeg.isWhiteSpace() == false)) {
                            itemBody += nodeSeg.getTextExtractor().toString();
                        }
                    }
                }
            }
            
            // Find the same pattern with <div id="myArea". This means the comment of this article.
            value = div.getAttributeValue("id");
            if (value != null && value.equals("myArea")) {
                Segment seg = div.getContent();
                boolean isSkipSegment = false, isAddNick = false, isAddComment = false;;
                //Log.i(TAG, "comment segment[" + seg.toString() + "]");
                
                // Parse article comment.
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    
                    // If article body has <script>...</script>, just skip!
                    // If <a> tag, prepare to add nick.
                    // if <tv class="G12"> tag, prepare to add comment.
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("script")) {
                            isSkipSegment = true;
                        } else if (tagName.equals("a")) {
                            isAddNick = true;
                        } else if (tagName.equals("td")) {
                            String classAttr = ((StartTag)nodeSeg).getAttributeValue("class");
                            if (classAttr != null && classAttr.equals("G12"))
                                isAddComment = true;
                        }
                        continue;
                    
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("script")) {
                            isSkipSegment = false;
                        }
                        continue;
                        
                    // Ignore &bnsp;
                    } else if (nodeSeg instanceof CharacterReference) {
                        continue;
                        
                    // If plain text, add it to itemComment.
                    } else {
                        if (!isSkipSegment) {
                            if (isAddNick) {
                                itemComment += ("[" + nodeSeg.getTextExtractor().toString() + "] : ");
                                isAddNick = false;
                            } else if (isAddComment) {
                                itemComment += (nodeSeg.getTextExtractor().toString() + "\n\n");
                                isAddComment = false;
                            }
                        }
                    }
                }

                // Finish!
                break;
            }
        }
        
        mContentItem = new contentItem(itemTitle, itemBody, itemImgUrl, itemComment);
        //Log.i(TAG, "mContentItem[" + mContentItem.toString() + "]");

        Log.i(TAG, "parseMLBParkHtmlData - done!");
        return;
    }

    private boolean parseMLBParkHtmlDataMobileVer(String urlAddress) throws IOException {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        
        Source source = new Source(new URL(urlAddress));
        source.fullSequentialParse();
        
        String itemTitle = "";
        String itemWriter = "";
        String itemBody = "";
        String itemImgUrl = "";
        String itemComment = "";

        List<Element> divs = source.getAllElements(HTMLElementName.DIV);
        for (int i = 0; i < divs.size(); i++) {
            Element div = divs.get(i);
            String value = div.getAttributeValue("class");
            
            // Find the same pattern with <div class='article'>. This means the title of this article.
            if (value != null && value.equals("article")) {
                Element h3 = div.getContent().getFirstElement("h3");
                itemTitle = h3.getTextExtractor().toString();
                continue;
            
            // Find the same pattern with <div class='w'>. This means the writer of this article.
            } else if (value != null && value.equals("w")) {
                Segment seg = div.getContent();
                boolean isAddWriter = false;
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    if (nodeSeg instanceof StartTag) {
                        ;
                    } else if (nodeSeg instanceof EndTag) {
                        if (((Tag) nodeSeg).getName().equals("strong")) {
                            isAddWriter = true;
                        }
                    } else if (nodeSeg instanceof CharacterReference) {
                        ;
                    } else {
                        if (isAddWriter) {
                            itemWriter = nodeSeg.getTextExtractor().toString();
                            isAddWriter = false;
                            break;
                        }
                    }
                }
                continue;
                
            // Find the same pattern with <div class='ar_txt'>. This means the body of this article.
            } else if (value != null && value.equals("ar_txt")) {
                Segment seg = div.getContent();
                boolean isSkipSegment = false, isSkipImgUrl = false;
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("style")) {
                            isSkipSegment = true;
                        } else if (!isSkipSegment && (tagName.equals("br") || tagName.equals("div"))) {
                            itemBody += "\n";
                        } else if (!isSkipSegment && !isSkipImgUrl && tagName.equals("img")) {
                            itemImgUrl = ((StartTag) nodeSeg).getAttributeValue("src");
                            isSkipImgUrl = true;
                        }
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("style")) {
                            isSkipSegment = false;
                        } else if (!isSkipSegment && tagName.equals("p")) {
                            itemBody += "\n";
                        }
                    } else if (nodeSeg instanceof CharacterReference) {
                        ;
                    } else {
                        if (!isSkipSegment && (nodeSeg.isWhiteSpace() == false)) {
                            itemBody += nodeSeg.getTextExtractor().toString();
                        }
                    }
                }
                continue;
                
            // Find the same pattern with <div class='reply'>. This means the comment of this article.
            } else if (value != null && value.equals("reply")) {
                Element ul = div.getFirstElement("ul");
                boolean isSkipSegment = false, isAddNick = false, isAddComment = false;
                String tmpComment = "";
                for (Iterator<Segment> nodeIterator = ul.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("strong")) {
                            isAddComment = true;
                        } else if (tagName.equals("br")) {
                            tmpComment += "\n";
                        } else if (tagName.equals("span")) {
                            String classAttr = ((StartTag) nodeSeg).getAttributeValue("class");
                            if (classAttr != null && classAttr.equals("ti")) {
                                isAddNick = true;
                            }
                        }
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("strong")) {
                            isAddComment = false;
                        }
                    } else if (nodeSeg instanceof CharacterReference) {
                        ;
                    } else {
                        if (!isSkipSegment && isAddComment) {
                            tmpComment += nodeSeg.getTextExtractor().toString();
                        } else if (!isSkipSegment && isAddNick) {
                            itemComment += ("[" + nodeSeg.getTextExtractor().toString() + "] : " + tmpComment + "\n\n");
                            tmpComment = "";
                            isAddNick = false;
                        }
                    }
                }

                // Finish!
                break;
            }
        }
        
        mContentItem = new contentItem((itemTitle + " by " + itemWriter), itemBody, itemImgUrl, itemComment);
        //Log.i(TAG, "mContentItem[" + mContentItem.toString() + "]");

        Log.i(TAG, "parseMLBParkHtmlData - done!");
        return true;
    }
    
    private Bitmap getImageBitmap(String url) { 
        Bitmap bm = null; 
        try { 
            URL aURL = new URL(url); 
            URLConnection conn = aURL.openConnection(); 
            conn.connect(); 
            InputStream is = conn.getInputStream(); 
            BufferedInputStream bis = new BufferedInputStream(is); 
            bm = BitmapFactory.decodeStream(bis); 
            bis.close(); 
            is.close(); 
       } catch (IOException e) { 
           Log.e(TAG, "Error getting bitmap", e); 
       } 
       return bm; 
    } 
    
    private void setupIntentListener() {
        if (mIntentListener == null) {
            mIntentListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Update mSelectedItemUrl through Broadcast Intent.
                    mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
                    Log.i(TAG, "onReceive - update mSelectedItemUrl[" + mSelectedItemUrl + "], mAppWidgetId[" + mAppWidgetId + "]");
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_UPDATE_ITEM_URL);
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
