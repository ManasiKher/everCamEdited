package io.evercam.androidapp.addeditcamera;

import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.tasks.PortCheckTask;

public abstract class AddCameraParentActivity extends ParentAppCompatActivity {

    public void checkPort(PortCheckTask.PortType portType) {
        String ipText = getPublicIpEditText().getText().toString();

        if (!ipText.isEmpty()) {
            if (portType == PortCheckTask.PortType.HTTP) {
                String httpText = getHttpEditText().getText().toString();
                if (!httpText.isEmpty()) {
                    launchPortCheckTask(ipText, httpText, getHttpStatusText(), getHttpProgressBar());
                }
            } else if (portType == PortCheckTask.PortType.RTSP) {
                String rtspText = getRtspEditText().getText().toString();
                if (!rtspText.isEmpty()) {
                    launchPortCheckTask(ipText, rtspText, getRtspStatusText(), getRtspProgressBar());
                }
            }
        }
    }

    protected void launchPortCheckTask(String ip, String port, TextView statusView, ProgressBar progressBar) {
        new PortCheckTask(ip, port, getApplicationContext()).bindStatusView(statusView).bindProgressView(progressBar)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void showLocalIpWarning() {
        CustomToast.showInCenterLong(this, R.string.msg_local_ip_warning);
    }

    public abstract EditText getPublicIpEditText();

    public abstract EditText getHttpEditText();

    public abstract EditText getRtspEditText();

    public abstract TextView getHttpStatusText();

    public abstract TextView getRtspStatusText();

    public abstract ProgressBar getHttpProgressBar();

    public abstract ProgressBar getRtspProgressBar();
}
