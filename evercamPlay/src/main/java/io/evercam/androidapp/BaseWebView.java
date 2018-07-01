package io.evercam.androidapp;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class BaseWebView extends WebView {
    public BaseWebView(Context context) {
        super(context);
    }

    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void enableJavascript(boolean enable) {
        getSettings().setJavaScriptEnabled(enable);
    }

    protected void enableChromeDebugging() {
        //Enable DevTool debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    protected void enableGeoLocation() {
        getSettings().setGeolocationEnabled(true);
        getSettings().setGeolocationDatabasePath(getContext().getFilesDir().getPath());

        setWebChromeClient(webChromeClient);
    }

    protected void enableLocalStorage() {
        //Enable HTML5 localStorage
        getSettings().setDomStorageEnabled(true);
        getSettings().setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            getSettings().setDatabasePath("/data/data/" + getContext().getPackageName() + "/databases/");
        }
    }

    protected void appendUserAgent(String agentString) {
        getSettings().setUserAgentString(getSettings().getUserAgentString() + " " + agentString);
    }

    private WebChromeClient webChromeClient = new WebChromeClient() {
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions
                .Callback callback) {
            callback.invoke(origin, true, false);
        }
    };
}
