package com.smilo.bullpen;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class SearchActivity extends Activity {

	private static final String TAG = "SearchActivity";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    // intent item list
    private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static int mPageNum = Constants.ERROR_PAGE_NUM;
    private static int mBoardType = Constants.ERROR_BOARD_TYPE;
    private static int mRefreshTimetype = Constants.ERROR_REFRESH_TIME_TYPE;
    private static boolean mIsPermitMobileConnectionType = Constants.ERROR_PERMIT_MOBILE_CONNECTION_TYPE;
    
    private int mSelectedSearchCategoryType = Constants.DEFAULT_SEARCH_CATEGORY_TYPE;
    private int mSelectedSearchSubjectType = Constants.DEFAULT_SEARCH_SUBJECT_TYPE;
    
    private LinearLayout mLayoutSearchWord;
    private LinearLayout mLayoutSearchSubject;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
		setContentView(R.layout.activity_search);
		
		Intent intent = getIntent();
		mAppWidgetId = intent.getIntExtra(
				AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		mPageNum = intent.getIntExtra(
				Constants.EXTRA_PAGE_NUM, Constants.ERROR_PAGE_NUM);
		mBoardType = intent.getIntExtra(
				Constants.EXTRA_BOARD_TYPE, Constants.ERROR_BOARD_TYPE);
		mRefreshTimetype = intent.getIntExtra(
				Constants.EXTRA_REFRESH_TIME_TYPE, Constants.ERROR_REFRESH_TIME_TYPE);
		mIsPermitMobileConnectionType = intent.getBooleanExtra(
				Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, Constants.ERROR_PERMIT_MOBILE_CONNECTION_TYPE);
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

		mLayoutSearchWord = (LinearLayout)findViewById(R.id.layoutSearchKeyword);
		mLayoutSearchSubject = (LinearLayout)findViewById(R.id.layoutSearchSubject);
		
		toggleSearchTarget(true);
		initializeSpinners();
		initializeButtons();
	}

	private void toggleSearchTarget(boolean isShowSearchWord) {
		if (isShowSearchWord) {
			mLayoutSearchWord.setVisibility(View.VISIBLE);
			mLayoutSearchSubject.setVisibility(View.GONE);
		} else {
			mLayoutSearchWord.setVisibility(View.GONE);
			mLayoutSearchSubject.setVisibility(View.VISIBLE);
		}
	}
	
	private void initializeSpinners() {
		Spinner spinSearchCategory = (Spinner)findViewById(R.id.spinSearchCategory);
		ArrayAdapter<CharSequence> adapterSearchCategory = ArrayAdapter.createFromResource(this, R.array.searchCategory, android.R.layout.simple_spinner_item);
		adapterSearchCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSearchCategory.setAdapter(adapterSearchCategory);
        spinSearchCategory.setOnItemSelectedListener(mSpinSearchCategorySelectedListener);
        spinSearchCategory.setSelection(Constants.SEARCH_CATEGORY_TYPE_TITLE);
        
        Spinner spinSearchSubject = (Spinner)findViewById(R.id.spinSearchSubject);
		ArrayAdapter<CharSequence> adapterSearchSubject = ArrayAdapter.createFromResource(this, R.array.searchSubject, android.R.layout.simple_spinner_item);
		adapterSearchSubject.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinSearchSubject.setAdapter(adapterSearchSubject);
		spinSearchSubject.setOnItemSelectedListener(mSpinSearchSubjectSelectedListener);
		spinSearchSubject.setSelection(Constants.SEARCH_SUBJECT_TYPE_1);
	}

	private void initializeButtons() {
		findViewById(R.id.btnSearchOk).setOnClickListener(mBtnOkOnClickListener);
		findViewById(R.id.btnSearchCancel).setOnClickListener(mBtnCancelOnClickListener);
	}

    View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (DEBUG) Log.i(TAG, "Button OK clicked");

            EditText etSearchKeyword = (EditText)findViewById(R.id.editTextSearchKeyword);
            String searchKeyword = etSearchKeyword.getText().toString();
            
            if (DEBUG) Log.i(TAG, "mSelectedSearchCategoryType[" + mSelectedSearchCategoryType +
                    "], mSelectedSearchSubjectType[" + mSelectedSearchSubjectType + 
                    "], searchKeyword[" + searchKeyword + "]");
            
            final Context context = SearchActivity.this;
            Intent initIntent = new Intent(context, WidgetProvider.class);
            initIntent.setAction(Constants.ACTION_SEARCH);
            initIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            initIntent.putExtra(Constants.EXTRA_PAGE_NUM, mPageNum);
            initIntent.putExtra(Constants.EXTRA_BOARD_TYPE, mBoardType);
            initIntent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, mRefreshTimetype);
            initIntent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, mIsPermitMobileConnectionType);
            initIntent.putExtra(Constants.EXTRA_SEARCH_CATEGORY_TYPE, mSelectedSearchCategoryType);
            initIntent.putExtra(Constants.EXTRA_SEARCH_SUBJECT_TYPE, mSelectedSearchSubjectType);
            initIntent.putExtra(Constants.EXTRA_SEARCH_KEYWORD, searchKeyword);
            
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
    
    Spinner.OnItemSelectedListener mSpinSearchCategorySelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	mSelectedSearchCategoryType = arg2;
        	
        	if (mSelectedSearchCategoryType == Constants.SEARCH_CATEGORY_TYPE_SUBJECT)
        		toggleSearchTarget(false);
        	else
        		toggleSearchTarget(true);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
    
	Spinner.OnItemSelectedListener mSpinSearchSubjectSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	mSelectedSearchSubjectType = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
}
