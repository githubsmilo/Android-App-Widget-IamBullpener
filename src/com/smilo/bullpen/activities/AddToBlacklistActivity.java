
package com.smilo.bullpen.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.smilo.bullpen.Constants;
import com.smilo.bullpen.R;
import com.smilo.bullpen.WidgetProvider;

public class AddToBlacklistActivity extends Activity {

	private static final String TAG = "AddToBlacklistActivity";
	private static final boolean DEBUG = Constants.DEBUG_MODE;
	
	// intent item list
	private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static int mPageNum = Constants.DEFAULT_PAGE_NUM;
    private static int mBoardType = Constants.DEFAULT_BOARD_TYPE;
    private static int mRefreshTimetype = Constants.DEFAULT_REFRESH_TIME_TYPE;
    private static boolean mIsPermitMobileConnectionType = Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE;
    private static String mBlackList = Constants.DEFAULT_BLACK_LIST;
    private static int mSelectedSearchCategoryType = Constants.DEFAULT_SEARCH_CATEGORY_TYPE;
    private static int mSelectedSearchSubjectType = Constants.DEFAULT_SEARCH_SUBJECT_TYPE;
    private static String mSelectedSearchKeyword = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Set title
        this.setTitle(R.string.title_activity_add_to_black_list);
        
        setContentView(R.layout.activity_add_to_black_list);
        
        Intent intent = getIntent();
        mAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mPageNum = intent.getIntExtra(
                Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
        mBoardType = intent.getIntExtra(
                Constants.EXTRA_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
        mRefreshTimetype = intent.getIntExtra(
                Constants.EXTRA_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
        mIsPermitMobileConnectionType = intent.getBooleanExtra(
                Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
        mBlackList = intent.getStringExtra(Constants.EXTRA_BLACK_LIST);
        mSelectedSearchCategoryType = intent.getIntExtra(
        		Constants.EXTRA_SEARCH_CATEGORY_TYPE, Constants.DEFAULT_SEARCH_CATEGORY_TYPE);
        mSelectedSearchSubjectType = intent.getIntExtra(
        		Constants.EXTRA_SEARCH_SUBJECT_TYPE, Constants.DEFAULT_SEARCH_SUBJECT_TYPE);
        mSelectedSearchKeyword = intent.getStringExtra(Constants.EXTRA_SEARCH_KEYWORD);
        
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        
        String selectedItemWriter = intent.getStringExtra(Constants.EXTRA_ITEM_WRITER);
        
        initializeTextView(selectedItemWriter);
        initializeButtons();
    }

    private void initializeTextView(String selectedItemWriter) {
        TextView tv = (TextView)findViewById(R.id.textAddToBlacklistWriter);
        tv.setText(selectedItemWriter);
	}

    private void initializeButtons() {
        findViewById(R.id.btnAddToBlacklistOk).setOnClickListener(mBtnOkOnClickListener);
        findViewById(R.id.btnAddToBlacklistCancel).setOnClickListener(mBtnCancelOnClickListener);
    }

    View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (DEBUG) Log.i(TAG, "Button OK clicked");

            final Context context = AddToBlacklistActivity.this;
            TextView tv = (TextView)findViewById(R.id.textAddToBlacklistWriter);
            String selectedItemWriter = tv.getText().toString();
            
            // Update blackList
            if (mBlackList == null)
            	mBlackList = selectedItemWriter;
            else
            	mBlackList = mBlackList.concat(Constants.DELIMITER_BLACK_LIST + selectedItemWriter);

            // Show toast message
            Toast.makeText(context, selectedItemWriter + context.getResources().getString(R.string.text_add_to_black_list_msg),
            		Toast.LENGTH_SHORT).show();
            
            Intent initIntent = new Intent(context, WidgetProvider.class);
            initIntent.setAction(Constants.ACTION_REFRESH_LIST);
            initIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            initIntent.putExtra(Constants.EXTRA_PAGE_NUM, mPageNum);
            initIntent.putExtra(Constants.EXTRA_BOARD_TYPE, mBoardType);
            initIntent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, mRefreshTimetype);
            initIntent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, mIsPermitMobileConnectionType);
            initIntent.putExtra(Constants.EXTRA_BLACK_LIST, mBlackList);
            initIntent.putExtra(Constants.EXTRA_SEARCH_CATEGORY_TYPE, mSelectedSearchCategoryType);
            initIntent.putExtra(Constants.EXTRA_SEARCH_SUBJECT_TYPE, mSelectedSearchSubjectType);
            initIntent.putExtra(Constants.EXTRA_SEARCH_KEYWORD, mSelectedSearchKeyword);
            
            context.sendBroadcast(initIntent);
            
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };
    
    View.OnClickListener mBtnCancelOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (DEBUG) Log.i(TAG, "Button Cancel clicked");
            finish();
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_to_black_list, menu);
        return true;
    }
}
