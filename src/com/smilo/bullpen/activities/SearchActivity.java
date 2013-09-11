
package com.smilo.bullpen.activities;

import com.smilo.bullpen.Constants;
import com.smilo.bullpen.ExtraItems;
import com.smilo.bullpen.Utils;
import com.smilo.bullpen.WidgetProvider;
import com.smilo.bullpen.R;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchActivity extends Activity {

    private static final String TAG = "SearchActivity";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    public static final String SEARCH_ACTIVITY_CLASS_NAME = Constants.Specific.PACKAGE_NAME + ".activities." + TAG;

    private static ExtraItems mItem = null;

    private LinearLayout mLayoutSearchWord;
    private LinearLayout mLayoutSearchSubject;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        // Set title.
        setTitle(R.string.title_activity_search);
        
        // Get ExtraItems.
        Intent intent = getIntent();
        mItem = Utils.createExtraItemsFromIntent(intent);
        if (mItem.getAppWidgetId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (DEBUG) Log.e(TAG, "Invalid appWidget Id[" + mItem.getAppWidgetId() + "]");
            finish();
        }

        mLayoutSearchWord = (LinearLayout)findViewById(R.id.layoutSearchKeyword);
        mLayoutSearchSubject = (LinearLayout)findViewById(R.id.layoutSearchSubject);
        
        // Initialize layout components.
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
        spinSearchCategory.setSelection(Constants.Specific.SEARCH_CATEGORY_TYPE_TITLE);
        
        Spinner spinSearchSubject = (Spinner)findViewById(R.id.spinSearchSubject);
        ArrayAdapter<CharSequence> adapterSearchSubject = ArrayAdapter.createFromResource(this, R.array.searchSubject, android.R.layout.simple_spinner_item);
        adapterSearchSubject.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSearchSubject.setAdapter(adapterSearchSubject);
        spinSearchSubject.setOnItemSelectedListener(mSpinSearchSubjectSelectedListener);
        spinSearchSubject.setSelection(Constants.Specific.SEARCH_SUBJECT_TYPE_1);
    }

    private void initializeButtons() {
        findViewById(R.id.btnSearchOk).setOnClickListener(mBtnOkOnClickListener);
        findViewById(R.id.btnSearchCancel).setOnClickListener(mBtnCancelOnClickListener);
    }

    View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (DEBUG) Log.i(TAG, "Button OK clicked");

            final Context context = SearchActivity.this;
            
            // Get search keyword.
            EditText etSearchKeyword = (EditText)findViewById(R.id.editTextSearchKeyword);
            String searchKeyword = etSearchKeyword.getText().toString();
            mItem.setSearchKeyword(searchKeyword);
            
            // Check that keyword is empty.
            if ((mItem.getSearchCategoryType() != Constants.Specific.SEARCH_CATEGORY_TYPE_SUBJECT) &&
                  (searchKeyword.equals(""))) {
                Toast.makeText(context, R.string.text_need_to_enter_keyword, Toast.LENGTH_SHORT).show();
                return;
            }

            // Set mItem to default page number.
            mItem.setPageNum(Constants.DEFAULT_PAGE_NUM);
            
            // Create intent and broadcast it!
            Intent intent = Utils.createIntentFromExtraItems(
                    context, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_SEARCH, mItem, false);
            context.sendBroadcast(intent);

            // set return intent.
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mItem.getAppWidgetId());
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
            mItem.setSearchCategoryType(arg2);
            
            if (mItem.getSearchCategoryType() == Constants.Specific.SEARCH_CATEGORY_TYPE_SUBJECT)
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
            mItem.setSearchSubjectType(arg2);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
}
