
package com.smilo.bullpen.services;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.smilo.bullpen.R;
import com.smilo.bullpen.Utils;
import com.smilo.bullpen.definitions.Constants;
import com.smilo.bullpen.definitions.Constants.PARSING_RESULT;
import com.smilo.bullpen.definitions.ExtraItem;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class ContentsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "ContentsFactory";
    private static final boolean DEBUG = Constants.DEBUG_MODE;

    private static Context mContext;
    private static ExtraItem mItem = null;
    private static JSONObject mParsedJSONObject = null;
    private static BroadcastReceiver mIntentListener;
    private static PARSING_RESULT mParsingResult = PARSING_RESULT.FAILED_UNKNOWN;
    
    private static String mSelectedItemUrl = null;
    
    private static final String JSON_TITLE = "title";
    private static final String JSON_WRITER = "writer";
    private static final String JSON_BODY = "body";
    private static final String JSON_BODY_TEXT = "bodyText";
    private static final String JSON_BODY_IMAGE = "bodyImage";
    private static final String JSON_COMMENT = "comment";
    private static final String JSON_COMMENT_WRITER = "commentWriter";
    private static final String JSON_COMMENT_TEXT = "commentText";
    
    private static int mDisplayWidth;
    private static int mDisplayHeight;
    
    private ImageLoader mImageLoader;

    public ContentsFactory(Context context, Intent intent) {
        mContext = context;
        
        // Get ExtraItems
        mItem = Utils.createExtraItemFromIntent(intent);
        
        mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
        
        Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mDisplayWidth = display.getWidth();
        mDisplayHeight = display.getHeight();

        if (DEBUG) Log.i(TAG, "constructor - mItem[" + mItem.toString() + "], mSelectedItemUrl[" + mSelectedItemUrl +
                "], mDisplayWidth[" + mDisplayWidth + "], mDisplayHeight[" + mDisplayHeight +"]");
        
        setupIntentListener();
        
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i(TAG, "getViewAt - position[" + position + "]");

        // Create remoteViews.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.content_row);

        // Get resources.
        Resources res = mContext.getResources();
        
        switch (mParsingResult) {
            case SUCCESS_MOBILE_BOARD :
                RemoteViews rvDivider = new RemoteViews(mContext.getPackageName(), R.layout.content_row_divider);
                
                // Set writer and title
                String contentTitle = mParsedJSONObject.optString(JSON_TITLE);
                String contentWriter = mParsedJSONObject.optString(JSON_WRITER);
                if (contentTitle != null && contentTitle.length() > 0) {
                    if (contentWriter != null && contentWriter.length() > 0) {
                        rv.setTextViewText(R.id.contentRowTitleText, "[" + contentWriter + "]\n" + contentTitle);
                    } else {
                        rv.setTextViewText(R.id.contentRowTitleText, "[" + res.getString(R.string.text_writer_not_existed) + "]\n" + contentTitle);
                    }
                } else {
                    if (contentWriter != null && contentWriter.length() > 0) {
                        rv.setTextViewText(R.id.contentRowTitleText, "[" + contentWriter + "]\n" + res.getString(R.string.text_title_not_existed));
                    } else {
                        rv.setTextViewText(R.id.contentRowTitleText, res.getString(R.string.text_failed_may_be_deleted));
                        break;
                    }
                }
                
                // Add a divider between title and body.
                rv.addView(R.id.contentRowBodyLayout, rvDivider);
                
                // Set text and image of content body.
                JSONArray bodyArray = mParsedJSONObject.optJSONArray(JSON_BODY);
                if (bodyArray != null && bodyArray.length() > 0) {
                    for (int i = 0 ; i < bodyArray.length() ; i++) {
                        JSONObject obj = bodyArray.optJSONObject(i);
                        if (obj == null || obj.length() == 0) {
                            break;
                        }
                        String bodyText = obj.optString(JSON_BODY_TEXT);
                        if (bodyText != null && bodyText.length() > 0) {
                            //if (DEBUG) Log.i(TAG, "getViewAt - text[" + bodyText + "]");
                            RemoteViews rvBodyText = new RemoteViews(mContext.getPackageName(), R.layout.content_row_text);
                            rvBodyText.setTextViewText(R.id.contentRowText, bodyText);
                            rv.addView(R.id.contentRowBodyLayout, rvBodyText);
                            continue;
                        }
                        String bodyImage = obj.optString(JSON_BODY_IMAGE);
                        if (bodyImage != null && bodyImage.length() > 0) {
                            if (DEBUG) Log.i(TAG, "getViewAt - image[" + bodyImage + "]");
                            final RemoteViews rvBodyImage = new RemoteViews(mContext.getPackageName(), R.layout.content_row_image);

                            // Load given image synchronously.
                            // NOTE :
                            // I don't use AsyncTask method to load given image file,
                            // because the job to update specific remoteViews MUST update widget entirely to refresh.
                            // This is very expensive job! :(
                            DisplayImageOptions options = new DisplayImageOptions.Builder()
                                .imageScaleType(ImageScaleType.EXACTLY)
                                .bitmapConfig(Bitmap.Config.RGB_565)
                                .build();
                            ImageSize imageSize = new ImageSize(mDisplayWidth, mDisplayHeight);
                            Bitmap bitmap = mImageLoader.loadImageSync(bodyImage, imageSize, options);
                            
                            // Check image size. If too large, scale it.
                            int bitmapWidth = bitmap.getWidth();
                            int bitmapHeight = bitmap.getHeight();
                            Bitmap resizeBitmap = null;
                            if ((bitmapWidth > bitmapHeight) && (bitmapWidth > mDisplayWidth)) {
                                resizeBitmap = Bitmap.createScaledBitmap(
                                        bitmap, mDisplayWidth, (bitmapHeight * mDisplayWidth)/bitmapWidth, true);
                                bitmap.recycle();
                                rvBodyImage.setImageViewBitmap(R.id.contentRowImage, resizeBitmap);
                                if (DEBUG) Log.d(TAG, "bitmap width[" + resizeBitmap.getWidth() + "] height[" + resizeBitmap.getHeight() + "]");
                            } else if ((bitmapWidth < bitmapHeight) && (bitmapHeight > mDisplayHeight)) {
                                resizeBitmap = Bitmap.createScaledBitmap(
                                        bitmap, (bitmapWidth * mDisplayWidth)/bitmapHeight, mDisplayWidth, true);
                                bitmap.recycle();
                                rvBodyImage.setImageViewBitmap(R.id.contentRowImage, resizeBitmap);
                                if (DEBUG) Log.d(TAG, "bitmap width[" + resizeBitmap.getWidth() + "] height[" + resizeBitmap.getHeight() + "]");
                            } else {
                                rvBodyImage.setImageViewBitmap(R.id.contentRowImage, bitmap);
                                if (DEBUG) Log.d(TAG, "bitmap width[" + bitmap.getWidth() + "] height[" + bitmap.getHeight() + "]");
                            }
                            
                            rv.addView(R.id.contentRowBodyLayout, rvBodyImage);
                        }
                    }
                }
                
                // Set text of content comment.
                JSONArray commentArray = mParsedJSONObject.optJSONArray(JSON_COMMENT);
                if (commentArray != null && commentArray.length() > 0) {
                    for (int i = 0 ; i < commentArray.length() ; i++) {
                        JSONObject obj = commentArray.optJSONObject(i);
                        if (obj == null || obj.length() == 0) {
                            break;
                        }
                        String commentWriter = obj.optString(JSON_COMMENT_WRITER);
                        String commentText = obj.optString(JSON_COMMENT_TEXT);
                        RemoteViews rvComment = new RemoteViews(mContext.getPackageName(), R.layout.content_row_text);
                        if (commentWriter != null && commentWriter.length() > 0) {
                            if (commentText != null && commentText.length() >0) {
                                rvComment.setTextViewText(R.id.contentRowText, "[" + commentWriter + "]\n" + commentText);
                            } else {
                                rvComment.setTextViewText(R.id.contentRowText, "[" + commentWriter + "]\n" + res.getString(R.string.text_comment_not_existed));
                            }
                        } else {
                            if (commentText != null && commentText.length() >0) {
                                rvComment.setTextViewText(R.id.contentRowText, "[" + res.getString(R.string.text_writer_not_existed) + "]\n" + commentText);
                            } else {
                                rvComment.setTextViewText(R.id.contentRowText, "[" + res.getString(R.string.text_writer_not_existed) + "]\n" + res.getString(R.string.text_comment_not_existed));
                            }
                        }
                        rv.addView(R.id.contentRowCommentLayout, rvDivider);
                        rv.addView(R.id.contentRowCommentLayout, rvComment);
                    }
                }
                break;
            
            case FAILED_IO_EXCEPTION :
                rv.setTextViewText(R.id.contentRowTitleText, res.getString(R.string.text_failed_io_exception));
                break;
                
            case FAILED_JSON_EXCEPTION :
                rv.setTextViewText(R.id.contentRowTitleText, res.getString(R.string.text_failed_json_exception));
                break;
                
            case FAILED_STACK_OVERFLOW :
                rv.setTextViewText(R.id.contentRowTitleText, res.getString(R.string.text_failed_stack_overflow));
                break;
            
            case FAILED_OUT_OF_MEMORY :
                rv.setTextViewText(R.id.contentRowTitleText, res.getString(R.string.text_failed_out_of_memory));
                break;
               
            case FAILED_UNKNOWN :
            default:
                rv.setTextViewText(R.id.contentRowTitleText, res.getString(R.string.text_failed_unknown));
                break;
        }

        Intent fillInIntent = new Intent();
        rv.setOnClickFillInIntent(R.id.contentRowLayout, fillInIntent);
        return rv;
    }
    
    @Override
    public void onDataSetChanged() {
        if (DEBUG) Log.i(TAG, "onDataSetChanged - mSelectedItemUrl[" + mSelectedItemUrl + "]");

        if (mSelectedItemUrl == null) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - mSelectedItemUrl is null!");
            return;
        }
        
        // Parse MLBPark html data and add items to the widget item array list.
        try {
            mParsingResult = parseMLBParkHtmlDataMobileVer(mSelectedItemUrl);
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - parseMLBParkHtmlDataMobileVer - IOException![" + e.toString() + "]");
            e.printStackTrace();
            mParsingResult = PARSING_RESULT.FAILED_IO_EXCEPTION;
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - parseMLBParkHtmlDataMobileVer - JSONException![" + e.toString() + "]");
            e.printStackTrace();
            mParsingResult = PARSING_RESULT.FAILED_JSON_EXCEPTION;
        } catch (StackOverflowError e) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - parseMLBParkHtmlDataMobileVer - StackOverflowError![" + e.toString() + "]");
            e.printStackTrace();
            mParsingResult = PARSING_RESULT.FAILED_STACK_OVERFLOW;
        } catch (OutOfMemoryError e) {
            if (DEBUG) Log.e(TAG, "onDataSetChanged - parseMLBParkHtmlDataMobileVer - OutOfMemoryError![" + e.toString() + "]");
            e.printStackTrace();
            mParsingResult = PARSING_RESULT.FAILED_OUT_OF_MEMORY;
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
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.preview_content);
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

    private PARSING_RESULT parseMLBParkHtmlDataMobileVer(String urlAddress)
            throws IOException, JSONException, StackOverflowError, OutOfMemoryError {

        // Load HTML data from given urlAddress.
        Source source = new Source(new URL(urlAddress));
        source.fullSequentialParse();
        
        // Create an empty JSONObjects.
        JSONObject obj = new JSONObject();
        JSONArray body = new JSONArray();
        JSONArray comment = new JSONArray();
        obj.put(JSON_BODY, body);
        obj.put(JSON_COMMENT, comment);
        
        List<Element> divs = source.getAllElements(HTMLElementName.DIV);
        for (int i = 0; i < divs.size(); i++) {
            Element div = divs.get(i);
            String value = div.getAttributeValue("class");
            
            // Find the same pattern with <div class='article'>. This means the title of this article.
            if (value != null && value.equals("article")) {
                Element h3 = div.getContent().getFirstElement("h3");
                if ((h3 != null) && (h3.isEmpty() == false)) {
                    String itemTitle = h3.getTextExtractor().toString();
                    //Log.i(TAG, "parseMLBParkHtmlDataMobileVer - parsed title[" + itemTitle + "]");
                    
                    // Put itemTitle to the 'obj' JSONObject.
                    obj.put(JSON_TITLE, itemTitle);
                    continue;
                }
            
            // Find the same pattern with <div class='w'>. This means the writer of this article.
            } else if (value != null && value.equals("w")) {
                Segment seg = div.getContent();
                boolean isAddWriter = false, isFinished = false;
                String itemWriter = "";
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("span")) {
                            isAddWriter = false;
                        }
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("strong")) {
                            isAddWriter = true;
                        } else if (tagName.equals("span")) {
                            isFinished = true;
                        }
                    //} else if (nodeSeg instanceof CharacterReference) {
                    //    ;
                    } else {
                        if (isAddWriter) {
                            itemWriter += nodeSeg.getTextExtractor().toString();
                        }
                    }
                    
                    if (isFinished) {
                        // Put itemWriter to the 'obj' JSONObject.
                        //Log.i(TAG, "parseMLBParkHtmlDataMobileVer - parsed writer[" + itemWriter + "]");
                        obj.put(JSON_WRITER, itemWriter);
                        break;
                    }
                }
                continue;
                
            // Find the same pattern with <div class='ar_txt'>. This means the body of this article.
            } else if (value != null && value.equals("ar_txt")) {
                Segment seg = div.getContent();
                boolean isSkipSegment = false, isAddTextToBody = false;
                String itemBodyText = "";
                for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                    Segment nodeSeg = nodeIterator.next();
                    if (nodeSeg instanceof StartTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("style")) {
                            isSkipSegment = true;
                        } else if (!isSkipSegment && (tagName.equals("br") || tagName.equals("div"))) {
                            itemBodyText += "\n";
                            isAddTextToBody = true;
                        } else if (!isSkipSegment && tagName.equals("img")) {
                            // If stored itemBodyText exists before parsing image tag,
                            // put it to the 'body' JSONArray and initialize isAddTextToBody, itemBodyText.
                            if (isAddTextToBody) {
                                JSONObject newBodyText = new JSONObject();
                                newBodyText.put(JSON_BODY_TEXT, itemBodyText);
                                body.put(newBodyText);
                                isAddTextToBody = false;
                                itemBodyText = "";
                            }
                            String itemImgUrl = ((StartTag) nodeSeg).getAttributeValue("src");
                            if (itemImgUrl.startsWith("/")) {
                                StringBuffer strBuf = new StringBuffer();
                                strBuf.append(Constants.Specific.URL_BASE);
                                strBuf.append(itemImgUrl);
                                itemImgUrl = strBuf.toString();
                            } else if (itemImgUrl.startsWith(Constants.Specific.URL_FAKE_IMG_BASE)) {
                            	itemImgUrl = itemImgUrl.replaceFirst(
                            			Constants.Specific.URL_FAKE_IMG_BASE, Constants.Specific.URL_CORRECT_IMG_BASE);
                            }
                            
                            // Put itemImgUrl to the 'body' JSONArray.
                            JSONObject newImgUrl = new JSONObject();
                            newImgUrl.put(JSON_BODY_IMAGE, itemImgUrl);
                            body.put(newImgUrl);
                        }
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("style")) {
                            isSkipSegment = false;
                        } else if (!isSkipSegment && tagName.equals("p")) {
                            itemBodyText += "\n";
                            isAddTextToBody = true;
                        }
                    } else if (nodeSeg instanceof CharacterReference) {
                        ;
                    } else {
                        if (!isSkipSegment && (nodeSeg.isWhiteSpace() == false)) {
                            itemBodyText += nodeSeg.getTextExtractor().toString();
                            isAddTextToBody = true;
                        }
                    }
                }
                // If stored itemBodyText exists after parsing this article,
                // put it to the 'body' JSONArray.
                if (isAddTextToBody) {
                    JSONObject newBodyText = new JSONObject();
                    newBodyText.put(JSON_BODY_TEXT, itemBodyText);
                    body.put(newBodyText);
                }
                continue;
                
            // Find the same pattern with <div class='reply'>. This means the comment of this article.
            } else if (value != null && value.equals("reply")) {
                Element ul = div.getFirstElement("ul");
                boolean isAddWriter = false, isAddComment = false, isFinished = false;
                String tmpWriter = "", tmpComment = "";
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
                                isAddWriter = true;
                            }
                        }
                    } else if (nodeSeg instanceof EndTag) {
                        String tagName = ((Tag)nodeSeg).getName();
                        if (tagName.equals("strong")) {
                            isAddComment = false;
                        } else if (tagName.equals("span")) {
                            isAddWriter = false;
                        } else if (tagName.equals("li")) {
                            isFinished = true;
                        }
                    //} else if (nodeSeg instanceof CharacterReference) {
                    //    ;
                    } else {
                        if (isAddComment) {
                            tmpComment += nodeSeg.getTextExtractor().toString();
                        } else if (isAddWriter) {
                            tmpWriter += nodeSeg.getTextExtractor().toString();
                        }
                    }
                    
                    if (isFinished) {
                        JSONObject newComment = new JSONObject();
                        newComment.put(JSON_COMMENT_WRITER, tmpWriter);
                        newComment.put(JSON_COMMENT_TEXT, tmpComment);
                        comment.put(newComment);
                        tmpComment = "";
                        tmpWriter = "";
                        isFinished = false;
                    }
                }

                // Finish!
                break;
            }
        }
        
        // Save parsed result.
        mParsedJSONObject = obj;
        //Log.i(TAG, "parseMLBParkHtmlDataMobileVer - mParsedJSONString[" + obj.toString(4) + "]");
        if (DEBUG) Log.i(TAG, "parseMLBParkHtmlDataMobileVer - done!");
        
        return PARSING_RESULT.SUCCESS_MOBILE_BOARD;
    }

    private void setupIntentListener() {
        if (mIntentListener == null) {
            mIntentListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Update mItem through Broadcast Intent.
                    ExtraItem item = Utils.createExtraItemFromIntent(intent);
                    mItem.update(item);
                    mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
                    if (DEBUG) Log.i(TAG, "onReceive - update mItem[" + mItem.toString() + "], mSelectedItemUrl[" + mSelectedItemUrl + "]");
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_UPDATE_ITEM_INFO);
            mContext.registerReceiver(mIntentListener, filter);
        }
    }

    private void teardownIntentListener() {
        if (mIntentListener != null) {
            mContext.unregisterReceiver(mIntentListener);
            mIntentListener = null;
        }
    }
    /*
    private Bitmap getImageBitmap(String url) throws IOException, RuntimeException, OutOfMemoryError { 
        Bitmap bitmap = null; 
        URL aURL = new URL(url); 
        URLConnection conn = aURL.openConnection(); 
        conn.connect(); 
        InputStream is = conn.getInputStream(); 
        BufferedInputStream bis = new BufferedInputStream(is); 
        bitmap = BitmapFactory.decodeStream(bis);
        bis.close(); 
        is.close();

        if (bitmap == null) {
            if (DEBUG) Log.e(TAG, "getImageBitmap - bitmap is null!");
            return null;
        } else {
            Bitmap resizeBitmap = null;
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if (bitmapWidth > mDisplayWidth || bitmapHeight > mDisplayWidth) {
                if (bitmapWidth > bitmapHeight) {
                    resizeBitmap = Bitmap.createScaledBitmap(
                            bitmap, mDisplayWidth, (bitmapHeight * mDisplayWidth)/bitmapWidth, true);
                } else {
                    resizeBitmap = Bitmap.createScaledBitmap(
                            bitmap, (bitmapWidth * mDisplayWidth)/bitmapHeight, mDisplayWidth, true);
                }
                bitmap.recycle();
                if (resizeBitmap == null) {
                    if (DEBUG) Log.e(TAG, "getImageBitmap - resizeBitmap is null!");
                    return null;
                } else {
                    if (DEBUG) Log.i(TAG, "getImageBitmap - resizeBitmap[" + resizeBitmap.getWidth() + "," + resizeBitmap.getHeight() + "] is ok!");
                    return resizeBitmap;
                }
            } else {
                if (DEBUG) Log.i(TAG, "getImageBitmap - bitmap[" + bitmapWidth + "," + bitmapHeight + "] is ok!");
                return bitmap;
            }
        }
    } 
    */
    /*
    static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final RemoteViews rv;
        private final Context context;
        private final int appWidgetId;
        private final int resourceId;

        public BitmapWorkerTask(RemoteViews rv, Context context, int appWidgetId, int resourceId) {
            this.rv = rv;
            this.context = context;
            this.appWidgetId = appWidgetId;
            this.resourceId = resourceId;
        }
        
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                bitmap = getImageBitmap(params[0]);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "BitmapWorkerTask - getImageBitmap - OutOfMemoryError![" + e.toString() + "]");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "BitmapWorkerTask - getImageBitmap - IOException![" + e.toString() + "]");
                e.printStackTrace();
            } catch (RuntimeException e) {
                Log.e(TAG, "BitmapWorkerTask - getImageBitmap - RuntimeException![" + e.toString() + "]");
                e.printStackTrace();
            }
            
            return bitmap;
        }
        
        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            
            rv.setImageViewBitmap(resourceId, bitmap);
            awm.partiallyUpdateAppWidget(appWidgetId, rv);
            
            // TODO : How can I update this widget entirely?
            //awm.updateAppWidget(appWidgetId, ????);
        }
    }
    */
}
