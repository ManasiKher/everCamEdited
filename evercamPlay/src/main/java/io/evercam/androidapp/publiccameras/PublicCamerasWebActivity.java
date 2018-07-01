package io.evercam.androidapp.publiccameras;

import android.os.Bundle;
import android.support.annotation.NonNull;

import io.evercam.androidapp.R;
import io.evercam.androidapp.WebActivity;
import io.evercam.androidapp.permission.Permission;
import io.evercam.androidapp.utils.Constants;

public class PublicCamerasWebActivity extends WebActivity {
    private final String TAG = "PublicCamerasWebActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_public_cameras);

        if (!Permission.isGranted(this, Permission.LOCATION)) {
            Permission.request(this, new String[]{Permission.LOCATION}, Permission.REQUEST_CODE_LOCATION);
        } else {
            loadPage();
        }
    }

    @Override
    protected void loadPage() {
        PublicCamerasWebView webView = (PublicCamerasWebView) findViewById(R.id.public_cameras_webview);
        webView.webActivity = this;
        webView.loadPublicCameras();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(Constants.RESULT_TRUE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Permission.REQUEST_CODE_LOCATION:
                loadPage();
                break;
        }
    }
}
