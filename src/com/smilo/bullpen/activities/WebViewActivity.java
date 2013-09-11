
package com.smilo.bullpen.activities;

import com.smilo.bullpen.Constants;
import com.smilo.bullpen.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

    private static final String TAG = "WebViewActivity";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    public static final String WEBVIEW_ACTIVITY_CLASS_NAME = Constants.Specific.PACKAGE_NAME + ".activities." + TAG;

    private ProgressDialog mProgress = null;
    private WebView mWebView = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
        // Set title.
        setTitle(R.string.title_activity_web_view);
        
        // Get exported url.
        Intent intent = getIntent();
        String exportUrl = intent.getStringExtra(Constants.EXTRA_EXPORT_URL);
        if (exportUrl == null) {
            if (DEBUG) Log.e(TAG, "onCreate - exportUrl is null!");
            finish();
        }
        
        // Initialize layout components.
        initializeWebView();
        mWebView.loadUrl(exportUrl);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.i(TAG, "onNewIntent");
        String exportUrl = intent.getStringExtra(Constants.EXTRA_EXPORT_URL);
        if (exportUrl == null) {
            if (DEBUG) Log.e(TAG, "onCreate - exportUrl is null!");
            finish();
        }
        mWebView.loadUrl(exportUrl);
        
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.isFocused() && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    private void initializeWebView() {
        mWebView = (WebView)findViewById(R.id.webViewExport);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setBackgroundColor(0);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Activity activity = WebViewActivity.this;
                
                if (mProgress == null) {
                    mProgress = new ProgressDialog(activity);
                    mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgress.setTitle(activity.getResources().getString(R.string.text_progressbar_title));
                    mProgress.setMessage(activity.getResources().getString(R.string.text_progressbar_message));
                    mProgress.show();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                    mProgress = null;
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                    mProgress = null;
                }
                super.onPageFinished(view, url);
            }
        });
    }
}
