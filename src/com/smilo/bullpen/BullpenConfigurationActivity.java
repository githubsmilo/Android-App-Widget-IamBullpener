
package com.smilo.bullpen;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.RemoteViews;

public class BullpenConfigurationActivity extends Activity {

    private int mAppWidgetId;
    private Context mContext;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bullpen_configuration);
        
        mContext = getApplicationContext();
    }

    @Override
    protected void onStart() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        // TODO : implement here!
        
        super.onResume();
        
        
        AppWidgetManager awm = AppWidgetManager.getInstance(mContext);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.activity_bullpen_configuration);
        awm.updateAppWidget(mAppWidgetId, views);
        
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bullpen_configuration, menu);
        return true;
    }

}
