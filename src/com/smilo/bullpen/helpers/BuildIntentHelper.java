package com.smilo.bullpen.helpers;

import java.io.UnsupportedEncodingException;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smilo.bullpen.Utils;
import com.smilo.bullpen.activities.AddToBlacklistActivity;
import com.smilo.bullpen.activities.ConfigurationActivity;
import com.smilo.bullpen.activities.SearchActivity;
import com.smilo.bullpen.activities.WebViewActivity;
import com.smilo.bullpen.definitions.Constants;
import com.smilo.bullpen.definitions.ExtraItem;
import com.smilo.bullpen.definitions.ListItem;
import com.smilo.bullpen.services.ContentsService;
import com.smilo.bullpen.services.ListViewService;

public final class BuildIntentHelper {

    private static final String TAG = "BuildIntentHelper";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    public static Intent buildInitListIntent(Context context, ExtraItem extraItem, String className) {
        return createIntentFromExtraItem(
                context, className, Constants.ACTION_INIT_LIST, extraItem, false);
    }
    
    public static Intent buildUpdateListInfoIntent(Context context, ExtraItem extraItem) {
        return createIntentFromExtraItem(
                context, null, Constants.ACTION_UPDATE_LIST_INFO, extraItem, false);
    }
    
    public static Intent buildUpdateItemInfoIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = createIntentFromExtraItem(
                context, null, Constants.ACTION_UPDATE_ITEM_INFO, extraItem, false);
        if (listItem != null) {
            String itemUrl = listItem.getUrl();
            if (itemUrl != null && itemUrl.length() >0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    public static Intent buildRefreshListIntent(Context context, ExtraItem extraItem, String className) {
        return createIntentFromExtraItem(
                context, className, Constants.ACTION_REFRESH_LIST, extraItem, false);
    }
    
    public static Intent buildShowListIntent(Context context, ExtraItem extraItem, String className) {
        return createIntentFromExtraItem(
                context, className, Constants.ACTION_SHOW_LIST, extraItem, false);
    }
    
    public static Intent buildShowItemIntent(Context context, ExtraItem extraItem, ListItem listItem, String className) {
        Intent intent = createIntentFromExtraItem(
                context, className, Constants.ACTION_SHOW_ITEM, extraItem, false);
        if (listItem != null) {
            String itemUrl = listItem.getUrl();
            if (itemUrl != null && itemUrl.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    public static Intent buildConfigurationActivityIntent(Context context, ExtraItem extraItem) {
        return createIntentFromExtraItem(
                context, ConfigurationActivity.CONFIGURATION_ACTIVITY_CLASS_NAME, null, extraItem, true);
    }
    
    public static Intent buildSearchActivityIntent(Context context, ExtraItem extraItem) {
        return createIntentFromExtraItem(
                context, SearchActivity.SEARCH_ACTIVITY_CLASS_NAME, null, extraItem, true);
    }
    
    public static Intent buildAddToBlackListIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = createIntentFromExtraItem(
                context, AddToBlacklistActivity.ADDTOBLACKLIST_ACTIVITY_CLASS_NAME, null, extraItem, true);
        if (listItem != null) {
            String itemWriter = listItem.getWriter();
            if (itemWriter != null && itemWriter.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_WRITER, itemWriter);
        }
        return intent;
    }
    
    public static Intent buildScrapItemIntent(Context context, ExtraItem extraItem, ListItem listItem, String className) {
        Intent intent = createIntentFromExtraItem(
                context, className, Constants.ACTION_SCRAP_ITEM, extraItem, false);
        if (listItem != null) {
            String itemTitle = listItem.getTitle();
            String itemWriter = listItem.getWriter();
            String itemUrl = listItem.getUrl();
            if (itemTitle != null && itemTitle.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_TITLE, itemTitle);
            if (itemWriter != null && itemWriter.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_WRITER, itemWriter);
            if (itemUrl != null && itemUrl.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    public static Intent buildDeleteScrappedItemIntent(Context context, ExtraItem extraItem, ListItem listItem, String className) {
        Intent intent = createIntentFromExtraItem(
                context, className, Constants.ACTION_DELETE_SCRAPPED_ITEM, extraItem, false);
        if (listItem != null) {
            String itemUrl = listItem.getUrl();
            if (itemUrl != null && itemUrl.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    public static Intent buildListViewServiceIntent(Context context, ExtraItem extraItem) {
        return createIntentFromExtraItem(
                context, ListViewService.LISTVIEW_SERVICE_CLASS_NAME, null, extraItem, false);
    }
    
    public static Intent buildContentServiceIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        Intent intent = createIntentFromExtraItem(
                context, ContentsService.CONTENTS_SERVICE_CLASS_NAME, null, extraItem, false);
        if (listItem != null) {
            String itemUrl = listItem.getUrl();
            if (itemUrl != null && itemUrl.length() > 0)
                intent.putExtra(Constants.EXTRA_ITEM_URL, itemUrl);
        }
        return intent;
    }
    
    public static Intent buildWidgetUpdateIntent(Context context, ExtraItem extraItem, String className) {
        return createIntentFromExtraItem(
                context, className, AppWidgetManager.ACTION_APPWIDGET_UPDATE, extraItem, false);
    }
    
    public static Intent buildExportIntent(Context context, ExtraItem extraItem, ListItem listItem) {
        String targetUrl = null;
        
        if (listItem == null) {
            try {
                if (Utils.isTodayBestBoardType(extraItem.getBoardType())) {
                    targetUrl = Constants.Specific.URL_BASE;
                } else {
                    targetUrl = Utils.getMobileBoardUrl(context, extraItem.getPageNum(), extraItem.getBoardType(), 
                            extraItem.getSearchCategoryType(), extraItem.getSearchSubjectType(), extraItem.getSearchKeyword());
                }
            } catch (UnsupportedEncodingException e) {
                if (DEBUG) Log.e(TAG, "buildExportIntent - UnsupportedEncodingException![" + e.toString() + "]");
                e.printStackTrace();
            }
        } else {
            targetUrl = listItem.getUrl();
        }
        
        Intent intent = createIntentFromExtraItem(
                context, WebViewActivity.WEBVIEW_ACTIVITY_CLASS_NAME, null, extraItem, true);
        intent.putExtra(Constants.EXTRA_EXPORT_URL, targetUrl);

        return intent;
    }
    
    private static Intent createIntentFromExtraItem(
            Context context, String className, String actionName, ExtraItem item, boolean isAddNewTask) {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, item.getAppWidgetId());
        intent.putExtra(Constants.EXTRA_PAGE_NUM, item.getPageNum());
        intent.putExtra(Constants.EXTRA_BOARD_TYPE, item.getBoardType());
        intent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, item.getRefreshTimeType());
        intent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, item.getPermitMobileConnectionType());
        intent.putExtra(Constants.EXTRA_BLACK_LIST, item.getBlackList());
        intent.putExtra(Constants.EXTRA_BLOCKED_WORDS, item.getBlockedWords());
        intent.putExtra(Constants.EXTRA_SEARCH_CATEGORY_TYPE, item.getSearchCategoryType());
        intent.putExtra(Constants.EXTRA_SEARCH_SUBJECT_TYPE, item.getSearchSubjectType());
        intent.putExtra(Constants.EXTRA_SEARCH_KEYWORD, item.getSearchKeyword());
        intent.putExtra(Constants.EXTRA_BG_IMAGE_TYPE, item.getBgImageType());
        intent.putExtra(Constants.EXTRA_TEXT_SIZE_TYPE, item.getTextSizeType());
        if (className != null)
            intent.setClassName(context, className);
        if (actionName != null)
            intent.setAction(actionName);
        if (isAddNewTask)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        return intent;
    }
}
