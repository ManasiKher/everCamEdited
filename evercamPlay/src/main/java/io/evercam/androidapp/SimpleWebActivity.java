package io.evercam.androidapp;

import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

import io.evercam.androidapp.utils.Constants;

/**
 * The web activity that simply open a web page from a URL
 * <p/>
 * eg. About, Forget password
 */
public class SimpleWebActivity extends WebActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);

        setUpDefaultToolbar();

        if (bundle != null) {
            getSupportActionBar().hide();
            loadPage();
        } else {
            finish();
        }
    }

    @Override
    protected void loadPage() {
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(getWebViewClient());

        //Enable DevTool debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        String url = bundle.getString(Constants.BUNDLE_KEY_URL);
        webView.loadUrl(url);
    }
}
