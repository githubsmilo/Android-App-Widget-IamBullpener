
package com.smilo.bullpen.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.smilo.bullpen.R;
import com.smilo.bullpen.Utils;
import com.smilo.bullpen.WidgetProvider;
import com.smilo.bullpen.definitions.Constants;
import com.smilo.bullpen.definitions.ExtraItem;
import com.smilo.bullpen.helpers.BuildIntentHelper;

public class ConfigurationActivity extends Activity {

    private static final String TAG = "ConfigurationActivity";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    public static final String CONFIGURATION_ACTIVITY_CLASS_NAME = Constants.Specific.PACKAGE_NAME + ".activities." + TAG;
    
    private static ExtraItem mItem = null;
    
    private boolean mIsExecutedBySettingButton = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        // Set title.
        this.setTitle(R.string.title_activity_configuration);

        // Get ExtraItems.
        Intent intent = getIntent();
        mItem = Utils.createExtraItemFromIntent(intent);
        if (mItem.getAppWidgetId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (DEBUG) Log.e(TAG, "Invalid appWidget Id[" + mItem.getAppWidgetId() + "]");
            finish();
        }

        Bundle extras = intent.getExtras();
        if (extras != null &&
            extras.containsKey(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE) &&
            extras.containsKey(Constants.EXTRA_REFRESH_TIME_TYPE) &&
            extras.containsKey(Constants.EXTRA_BOARD_TYPE)) {
            mIsExecutedBySettingButton = true;
        }
        
        int boardType, refreshTimeType;
        boolean isPermitMobileConnection;
        String blackList, blockedWords;
        
        if (mIsExecutedBySettingButton) {
            boardType = mItem.getBoardType();
            refreshTimeType = mItem.getRefreshTimeType();
            isPermitMobileConnection = mItem.getPermitMobileConnectionType();
            blackList = mItem.getBlackList();
            blockedWords = mItem.getBlockedWords();
            
        } else {
            // Load configuration info.
            SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
            
            boardType = pref.getInt(Constants.KEY_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
            refreshTimeType = pref.getInt(Constants.KEY_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
            isPermitMobileConnection = pref.getBoolean(Constants.KEY_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
            blackList = pref.getString(Constants.KEY_BLACK_LIST, Constants.DEFAULT_BLACK_LIST);
            blockedWords = pref.getString(Constants.KEY_BLOCKED_WORDS, Constants.DEFAULT_BLOCKED_WORDS);
        }

        // Initialize layout components.
        initializeRadioButton(isPermitMobileConnection);
        initializeSpinners(refreshTimeType, boardType);
        initializeEditText(blackList, blockedWords);
        initializeHorizontalScrollView();
        initializeButtons();
    }

    private void initializeRadioButton(boolean isPermitMobileConnection) {
        CheckBox cb = (CheckBox)findViewById(R.id.cbMobileConnection);
        cb.setChecked(isPermitMobileConnection);
    }
    
    private void initializeSpinners(int refreshTypeType, int boardType) {
        Spinner spinRefreshTime = (Spinner)findViewById(R.id.spinRefreshTime);
        ArrayAdapter<CharSequence> adapterRefreshTime = ArrayAdapter.createFromResource(this, R.array.refreshTime, android.R.layout.simple_spinner_item);
        adapterRefreshTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRefreshTime.setAdapter(adapterRefreshTime);
        spinRefreshTime.setOnItemSelectedListener(mSpinRefreshTimeSelectedListener);
        spinRefreshTime.setSelection(refreshTypeType);

        Spinner spinBoard = (Spinner)findViewById(R.id.spinBoard);
        ArrayAdapter<CharSequence> adapterBoard = ArrayAdapter.createFromResource(this, R.array.board, android.R.layout.simple_spinner_item);
        adapterBoard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinBoard.setAdapter(adapterBoard);
        spinBoard.setOnItemSelectedListener(mSpinBoardSelectedListener);
        spinBoard.setSelection(boardType);
        
        Spinner spinTextSize = (Spinner)findViewById(R.id.spinTextSize);
        ArrayAdapter<CharSequence> adapterTextSize = ArrayAdapter.createFromResource(this, R.array.textSize, android.R.layout.simple_spinner_item);
        adapterTextSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTextSize.setAdapter(adapterTextSize);
        spinTextSize.setOnItemSelectedListener(mSpinTextSizeSelectedListener);
        spinTextSize.setSelection(0);
    }

    private void initializeEditText(String blackList, String blockedWords) {
        EditText etBlackList = (EditText)findViewById(R.id.editBlackList);
        if (blackList == null)
            etBlackList.setText("");
        else
            etBlackList.setText(blackList);
        
        EditText etBlockedWords = (EditText)findViewById(R.id.editBlockedWords);
        if (blockedWords == null)
            etBlockedWords.setText("");
        else
            etBlockedWords.setText(blockedWords);
    }

    private void initializeButtons() {
        findViewById(R.id.btnConfigurationOk).setOnClickListener(mBtnOkOnClickListener);
        findViewById(R.id.btnConfigurationCancel).setOnClickListener(mBtnCancelOnClickListener);
    }

    private void initializeHorizontalScrollView() {
        HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollViewBgImage);
        
        LinearLayout topLinearLayout = new LinearLayout(this);
        topLinearLayout.setOrientation(LinearLayout.HORIZONTAL); 
        
        for (int i = 0; i < 10; i++){
            final ImageView imageView = new ImageView (this);
            imageView.setTag(i);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 80);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageResource(R.drawable.baseball_field_grass_design);
            topLinearLayout.addView(imageView);
            imageView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.e("Tag",""+imageView.getTag());
                }
            });
        }

        scrollView.addView(topLinearLayout);
    }
    
    View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (DEBUG) Log.i(TAG, "Button OK clicked");

            final Context context = ConfigurationActivity.this;
            
            // Get mobileConnection checkbox's value.
            CheckBox cb = (CheckBox)findViewById(R.id.cbMobileConnection);
            mItem.setPermitMobileConnectionType(cb.isChecked());
            
            // Get black list's value. If empty, set null.
            EditText etBlackList = (EditText)findViewById(R.id.editBlackList);
            String blackList = etBlackList.getText().toString();
            if (blackList != null && blackList.length() == 0)
                mItem.setBlackList(null);
            else
                mItem.setBlackList(blackList);
            
            // Get blocked words's value. If empty, set null.
            EditText etBlockedWords = (EditText)findViewById(R.id.editBlockedWords);
            String blockedWords = etBlockedWords.getText().toString();
            if (blockedWords != null && blockedWords.length() == 0)
                mItem.setBlockedWords(null);
            else
                mItem.setBlockedWords(blockedWords);

            // Set mItem's some fields to default.
            mItem.setPageNum(Constants.DEFAULT_PAGE_NUM);
            mItem.setSearchCategoryType(Constants.DEFAULT_SEARCH_CATEGORY_TYPE);
            mItem.setSearchSubjectType(Constants.DEFAULT_SEARCH_SUBJECT_TYPE);
            mItem.setSearchKeyword(null);
            
            // Create intent and broadcast it!
            Intent intent = BuildIntentHelper.buildInitListIntent(context, mItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME);
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
            final Context context = ConfigurationActivity.this;

            if (mIsExecutedBySettingButton == false) {
                WidgetProvider.removeWidget(context, mItem.getAppWidgetId());
            }
            finish();
        }
    };
    
    Spinner.OnItemSelectedListener mSpinRefreshTimeSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mItem.setRefreshTimeType(arg2);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
    
    Spinner.OnItemSelectedListener mSpinBoardSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mItem.setBoardType(arg2);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
    
    Spinner.OnItemSelectedListener mSpinTextSizeSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // TODO
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
}
