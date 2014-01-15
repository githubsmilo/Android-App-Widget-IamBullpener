
package com.smilo.bullpen.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smilo.bullpen.R;
import com.smilo.bullpen.Utils;
import com.smilo.bullpen.WidgetProvider;
import com.smilo.bullpen.definitions.Constants;
import com.smilo.bullpen.definitions.ExtraItem;
import com.smilo.bullpen.helpers.BuildIntentHelper;

public class AddToBlacklistActivity extends Activity {

    private static final String TAG = "AddToBlacklistActivity";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    public static final String ADDTOBLACKLIST_ACTIVITY_CLASS_NAME = Constants.Specific.PACKAGE_NAME + ".activities." + TAG;
    
    private static ExtraItem mItem = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_black_list);
    
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        // Set title.
        this.setTitle(R.string.title_activity_add_to_black_list);

        // Get ExtraItems.
        Intent intent = getIntent();
        mItem = Utils.createExtraItemFromIntent(intent);
        if (mItem.getAppWidgetId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (DEBUG) Log.e(TAG, "Invalid appWidget Id[" + mItem.getAppWidgetId() + "]");
            finish();
        }
        
        // Get other extra item.
        String selectedItemWriter = intent.getStringExtra(Constants.EXTRA_ITEM_WRITER);
        
        // Initialize layout components.
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
            
            // Get itemWriter value.
            TextView tv = (TextView)findViewById(R.id.textAddToBlacklistWriter);
            String selectedItemWriter = tv.getText().toString();
            
            // Update blackList to contain itemWriter.
            if (mItem.getBlackList() == null)
                mItem.setBlackList(selectedItemWriter);
            else
                mItem.setBlackList(mItem.getBlackList().concat(Constants.DELIMITER_BLACK_LIST + selectedItemWriter));

            // Show toast message.
            Toast.makeText(context, selectedItemWriter + context.getResources().getString(R.string.text_add_to_black_list_msg),
                    Toast.LENGTH_SHORT).show();
            
            // Create intent and broadcast it!
            Intent intent = BuildIntentHelper.buildRefreshListIntent(
                    context, mItem, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME);
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
}
