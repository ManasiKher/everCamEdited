package io.evercam.androidapp.recordings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.evercam.Snapshot;
import io.evercam.androidapp.R;
import io.evercam.androidapp.WebActivity;
import io.evercam.androidapp.photoview.SnapshotManager;
import io.evercam.androidapp.tasks.CaptureSnapshotRunnable;
import io.evercam.androidapp.tasks.ValidateRightsRunnable;
import io.evercam.androidapp.utils.Constants;


public class RecordingWebActivity extends WebActivity {
    private final String TAG = "RecordingWebActivity";

    private String cameraId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recording_web);

        setUpDefaultToolbar();

        if (bundle != null) {
            loadPage();
        } else {
            setResult(Constants.RESULT_TRUE);
            finish();
        }
    }

    @Override
    protected void loadPage() {
        cameraId = bundle.getString(Constants.BUNDLE_KEY_CAMERA_ID);

        //Validate if the user still has access to the camera
        new Thread(new ValidateRightsRunnable(this, cameraId)).start();

        RecordingWebView webView = (RecordingWebView) findViewById(R.id.recordings_webview);
        webView.webActivity = this;
        webView.loadRecordingWidget(cameraId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(Constants.RESULT_TRUE);
    }

    @Override
    public WebViewClient getWebViewClient() {
        return new CloudRecordingWebViewClient();
    }

    private class CloudRecordingWebViewClient extends BaseWebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading " + url);

            if (url.startsWith("data:image/jpeg;")) {
                String dataString = Snapshot.getBase64DataStringFrom(url);
                byte[] imageData = Snapshot.getDataFrom(dataString);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                new Thread(new CaptureSnapshotRunnable(RecordingWebActivity.this, cameraId, SnapshotManager
                        .FileType.JPG, bitmap)).start();

                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
