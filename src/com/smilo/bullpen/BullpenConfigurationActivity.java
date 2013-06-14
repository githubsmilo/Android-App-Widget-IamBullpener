
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

public class BullpenConfigurationActivity extends Activity {

    private static final String TAG = "BullpenConfigurationActivity";
    
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    private int mSelectedRefreshTimeType = -1, mSelectedBullpenBoardType = -1;
    
    private boolean mIsExecutedBySettingButton = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        setContentView(R.layout.activity_bullpen_configuration);
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        
        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        if (extras.containsKey(Constants.EXTRA_REFRESH_TIME_TYPE) &&
              extras.containsKey(Constants.EXTRA_BULLPEN_BOARD_TYPE)) {
            mIsExecutedBySettingButton = true;
        }
        
        boolean isPermitMobileConnection = extras.getBoolean(
                Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION);
        int refreshTimeType = extras.getInt(
                Constants.EXTRA_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
        int bullpenBoardType = extras.getInt(
                Constants.EXTRA_BULLPEN_BOARD_TYPE, Constants.DEFAULT_BULLPEN_BOARD_TYPE);
        
        initializeRadioButton(isPermitMobileConnection);
        initializeSpinners(refreshTimeType, bullpenBoardType);
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
    
    private void initializeSpinners(int refreshTypeType, int bullpenBoardType) {
        Spinner spinRefreshTime = (Spinner)findViewById(R.id.spinRefreshTime);
        ArrayAdapter<CharSequence> adapterRefreshTime = ArrayAdapter.createFromResource(this, R.array.refreshTime, android.R.layout.simple_spinner_item);
        adapterRefreshTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRefreshTime.setAdapter(adapterRefreshTime);
        spinRefreshTime.setOnItemSelectedListener(mSpinRefreshTimeSelectedListener);
        spinRefreshTime.setSelection(refreshTypeType);

        Spinner spinBullpenBoard = (Spinner)findViewById(R.id.spinBullpenBoard);
        ArrayAdapter<CharSequence> adapterBullpenBoard = ArrayAdapter.createFromResource(this, R.array.bullpenBoard, android.R.layout.simple_spinner_item);
        adapterBullpenBoard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinBullpenBoard.setAdapter(adapterBullpenBoard);
        spinBullpenBoard.setOnItemSelectedListener(mSpinBullpenBoardSelectedListener);
        spinBullpenBoard.setSelection(bullpenBoardType);
    }

    private void initializeButtons() {
        findViewById(R.id.btnOk).setOnClickListener(mBtnOkOnClickListener);
        findViewById(R.id.btnCancel).setOnClickListener(mBtnCancelOnClickListener);
    }
    
    View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Button OK clicked");

            if (mSelectedRefreshTimeType < 0) {
                mSelectedRefreshTimeType = 0;
            }
            if (mSelectedBullpenBoardType < 0) {
                mSelectedBullpenBoardType = 0;
            }
            
            CheckBox cb = (CheckBox)findViewById(R.id.cbMobileConnection);
            boolean selectedPermitMobileConnectionType = cb.isChecked();
            
            Log.i(TAG, "selectedPermitMobileConnectionType[" + selectedPermitMobileConnectionType +
                    "], mSelectedRefreshTimeType[" + mSelectedRefreshTimeType + 
                    "], mSelectedBullpenBoardType[" + mSelectedBullpenBoardType + "]");
            
            final Context context = BullpenConfigurationActivity.this;
            Intent initIntent = new Intent(context, BullpenWidgetProvider.class);
            initIntent.setAction(Constants.ACTION_INIT_LIST);
            initIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            initIntent.putExtra(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE, selectedPermitMobileConnectionType);
            initIntent.putExtra(Constants.EXTRA_REFRESH_TIME_TYPE, mSelectedRefreshTimeType);
            initIntent.putExtra(Constants.EXTRA_BULLPEN_BOARD_TYPE, mSelectedBullpenBoardType);
            context.sendBroadcast(initIntent);
            
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };
    
    View.OnClickListener mBtnCancelOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Button Cancel clicked");
            final Context context = BullpenConfigurationActivity.this;

            if (mIsExecutedBySettingButton == false) {
                BullpenWidgetProvider.removeWidget(context, mAppWidgetId);
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
    
    Spinner.OnItemSelectedListener mSpinBullpenBoardSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mSelectedBullpenBoardType = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Do nothing
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bullpen_configuration, menu);
        return true;
    }
}
