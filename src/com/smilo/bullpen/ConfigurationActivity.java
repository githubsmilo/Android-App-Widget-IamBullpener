
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class ConfigurationActivity extends Activity {

    private static final String TAG = "ConfigurationActivity";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int mSelectedBoardType = Constants.ERROR_BOARD_TYPE;
    private int mSelectedRefreshTimeType = Constants.ERROR_REFRESH_TIME_TYPE;
    
    private boolean mIsExecutedBySettingButton = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        setContentView(R.layout.configuration_activity);
        
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
        
        boolean isPermitMobileConnection = extras.getBoolean(
                Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
        int refreshTimeType = extras.getInt(
                Constants.EXTRA_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
        int boardType = extras.getInt(
                Constants.EXTRA_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
        
        initializeRadioButton(isPermitMobileConnection);
        initializeSpinners(refreshTimeType, boardType);
        //initializeEditText();
        initializeButtons();
    }

    /*
    private void initializeEditText() {
        EditText et = (EditText)findViewById(R.id.editBlackList);
        
        // TODO
        et.setFocusable(false);
    }
    */

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

    private void initializeButtons() {
        findViewById(R.id.btnOk).setOnClickListener(mBtnOkOnClickListener);
        findViewById(R.id.btnCancel).setOnClickListener(mBtnCancelOnClickListener);
    }
    
    View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (DEBUG) Log.i(TAG, "Button OK clicked");

            if (mSelectedRefreshTimeType < 0) {
                mSelectedRefreshTimeType = 0;
            }
            if (mSelectedBoardType < 0) {
                mSelectedBoardType = 0;
            }
            
            CheckBox cb = (CheckBox)findViewById(R.id.cbMobileConnection);
            boolean selectedPermitMobileConnectionType = cb.isChecked();
            
            if (DEBUG) Log.i(TAG, "selectedPermitMobileConnectionType[" + selectedPermitMobileConnectionType +
                    "], mSelectedRefreshTimeType[" + mSelectedRefreshTimeType + 
                    "], mSelectedBoardType[" + mSelectedBoardType + "]");
            
            final Context context = ConfigurationActivity.this;
            Intent initIntent = new Intent(context, WidgetProvider.class);
            initIntent.setAction(Constants.ACTION_INIT_LIST);
            initIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            initIntent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, selectedPermitMobileConnectionType);
            initIntent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, mSelectedRefreshTimeType);
            initIntent.putExtra(Constants.EXTRA_BOARD_TYPE, mSelectedBoardType);
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
