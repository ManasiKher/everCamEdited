package io.evercam.androidapp.publiccameras;

import android.content.Context;
import android.util.AttributeSet;

import io.evercam.API;
import io.evercam.androidapp.BaseWebView;
import io.evercam.androidapp.R;
import io.evercam.androidapp.WebActivity;
import io.evercam.androidapp.dto.AppData;

public class PublicCamerasWebView extends BaseWebView {
    private final String TAG = "PublicCamerasWebView";
    public WebActivity webActivity;

    public PublicCamerasWebView(Context context) {
        super(context);
    }

    public PublicCamerasWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadPublicCameras() {
        enableJavascript(true);

        enableChromeDebugging();

        enableGeoLocation();

        setWebViewClient(webActivity.getWebViewClient());

        enableLocalStorage();

        appendUserAgent(getContext()
                .getString(R.string.user_agent_suffix));

        if(AppData.defaultUser != null) {
            loadUrl("http://www.evercam.io/public/cameras?api_id=" + API.getUserKeyPair()[1] + "&api_key=" +
                    API.getUserKeyPair()[0] + "&user_id=" + AppData.defaultUser.getUsername());
        }
    }
}
