
package com.smilo.bullpen.activities;

import com.smilo.bullpen.Constants;
import com.smilo.bullpen.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

    private static final String TAG = "WebViewActivity";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    private static String mExportUrl = null;
    
    private ProgressDialog mProgress = null;
    private WebView mWebView = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        
		setContentView(R.layout.activity_web_view);

		Log.i(TAG, "onCreate");
		Intent intent = getIntent();
		String mExportUrl = intent.getStringExtra(Constants.EXTRA_EXPORT_URL);
		if (mExportUrl == null) {
			Log.e(TAG, "onCreate - exportUrl is null!");
			finish();
		}
		
		initializeWebView();
		mWebView.loadUrl(mExportUrl);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i(TAG, "onNewIntent");
		String mExportUrl = intent.getStringExtra(Constants.EXTRA_EXPORT_URL);
		if (mExportUrl == null) {
			Log.e(TAG, "onCreate - exportUrl is null!");
			finish();
		}
		mWebView.loadUrl(mExportUrl);
		
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
		mWebView.getSettings().setJavaScriptEnabled(true);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_view, menu);
		return true;
	}
}
