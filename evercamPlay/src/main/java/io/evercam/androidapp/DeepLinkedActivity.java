package io.evercam.androidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class DeepLinkedActivity extends ParentAppCompatActivity {

    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_linked);

        setUpDefaultToolbar();

        webview = (WebView) findViewById(R.id.cuartoWebView);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);


        onNewIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            webview.loadUrl(data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
        }
        return true;
    }
}
