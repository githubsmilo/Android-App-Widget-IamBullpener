
package com.smilo.bullpen.activities;

import com.smilo.bullpen.Constants;
import com.smilo.bullpen.WidgetProvider;
import com.smilo.bullpen.R;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class ConfigurationActivity extends Activity {

    private static final String TAG = "ConfigurationActivity";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    //private static int mPageNum = Constants.DEFAULT_PAGE_NUM; // We use default page num.
    private int mSelectedBoardType = Constants.DEFAULT_BOARD_TYPE;
    private int mSelectedRefreshTimeType = Constants.DEFAULT_REFRESH_TIME_TYPE;
    
    private boolean mIsExecutedBySettingButton = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        // Set title
        this.setTitle(R.string.title_activity_configuration);
        
        setContentView(R.layout.activity_configuration);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        
        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        if (extras.containsKey(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE) &&
            extras.containsKey(Constants.EXTRA_REFRESH_TIME_TYPE) &&
            extras.containsKey(Constants.EXTRA_BOARD_TYPE)) {
            mIsExecutedBySettingButton = true;
        }
        
        int boardType;
        int refreshTimeType;
        boolean isPermitMobileConnection;
        String blackList, blockedWords;;
        
        if (mIsExecutedBySettingButton) {
            boardType = extras.getInt(
                    Constants.EXTRA_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
            refreshTimeType = extras.getInt(
                    Constants.EXTRA_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
            isPermitMobileConnection = extras.getBoolean(
                    Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
            blackList = extras.getString(Constants.EXTRA_BLACK_LIST);
            blockedWords = extras.getString(Constants.EXTRA_BLOCKED_WORDS);
            
        } else {
            // Load configuration info.
            SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
            
            boardType = pref.getInt(Constants.KEY_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
            refreshTimeType = pref.getInt(Constants.KEY_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
            isPermitMobileConnection = pref.getBoolean(Constants.KEY_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
            blackList = pref.getString(Constants.KEY_BLACK_LIST, Constants.DEFAULT_BLACK_LIST);
            blockedWords = pref.getString(Constants.KEY_BLOCKED_WORDS, Constants.DEFAULT_BLOCKED_WORDS);
        }

        initializeRadioButton(isPermitMobileConnection);
        initializeSpinners(refreshTimeType, boardType);
        initializeEditText(blackList, blockedWords);
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

    View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (DEBUG) Log.i(TAG, "Button OK clicked");

            // Get mobileConnection checkbox's value.
            CheckBox cb = (CheckBox)findViewById(R.id.cbMobileConnection);
            boolean selectedPermitMobileConnectionType = cb.isChecked();
            
            // Get black list's value. If empty, set null.
            EditText etBlackList = (EditText)findViewById(R.id.editBlackList);
            String blackList = etBlackList.getText().toString();
            if (blackList != null && blackList.length() == 0)
                blackList = null;
            
            // Get blocked words's value. If empty, set null.
            EditText etBlockedWords = (EditText)findViewById(R.id.editBlockedWords);
            String blockedWords = etBlockedWords.getText().toString();
            if (blockedWords != null && blockedWords.length() == 0)
                blockedWords = null;
            
            if (DEBUG) Log.i(TAG, "mSelectedBoardType[" + mSelectedBoardType +
                    "], mSelectedRefreshTimeType[" + mSelectedRefreshTimeType + 
                    "], selectedPermitMobileConnectionType[" + selectedPermitMobileConnectionType +
                    "], blackList[" + blackList + "]");
            
            final Context context = ConfigurationActivity.this;
            Intent initIntent = new Intent(context, WidgetProvider.class);
            initIntent.setAction(Constants.ACTION_INIT_LIST);
            initIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            initIntent.putExtra(Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
            initIntent.putExtra(Constants.EXTRA_BOARD_TYPE, mSelectedBoardType);
            initIntent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, mSelectedRefreshTimeType);
            initIntent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, selectedPermitMobileConnectionType);
            initIntent.putExtra(Constants.EXTRA_BLACK_LIST, blackList);
            initIntent.putExtra(Constants.EXTRA_BLOCKED_WORDS, blockedWords);
 
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
            final Context context = ConfigurationActivity.this;

            if (mIsExecutedBySettingButton == false) {
                WidgetProvider.removeWidget(context, mAppWidgetId);
            }
            finish();
        }
    };
    
    Spinner.OnItemSelectedListener mSpinRefreshTimeSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mSelectedRefreshTimeType = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
    
    Spinner.OnItemSelectedListener mSpinBoardSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mSelectedBoardType = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_configuration, menu);
        return true;
    }
}
