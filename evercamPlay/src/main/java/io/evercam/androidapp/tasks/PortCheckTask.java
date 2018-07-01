package io.evercam.androidapp.tasks;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.evercam.API;
import io.evercam.androidapp.R;

public class PortCheckTask extends AsyncTask<Void, Void, Boolean> {
    public enum PortType {HTTP, RTSP}

    private final static String TAG = "PortCheckTask";

    private String mIp;
    private String mPort;
    private Context mContext;
    private WeakReference<TextView> mStatusViewReference;
    private WeakReference<ProgressBar> mProgressViewReference;

    public PortCheckTask(String ip, String port, Context context) {
        this.mIp = ip;
        this.mPort = port;
        this.mContext = context;
    }

    public PortCheckTask bindStatusView(TextView textView) {
        mStatusViewReference = new WeakReference<>(textView);
        return this;
    }

    public PortCheckTask bindProgressView(ProgressBar progressBar) {
        mProgressViewReference = new WeakReference<>(progressBar);
        return this;
    }

    public void showProgressBar(boolean show) {
        if (getProgressBar() != null) {
            getProgressBar().setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void showStatusTextView(boolean show) {
        if (mStatusViewReference != null) {
            mStatusViewReference.get().setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private ProgressBar getProgressBar() {
        if (mProgressViewReference != null) {
            return mProgressViewReference.get();
        }
        return null;
    }

    private void updatePortStatus(boolean isPortOpen) {
        if (mStatusViewReference != null) {
            TextView statusView = mStatusViewReference.get();
            if (statusView != null) {
                statusView.setVisibility(View.VISIBLE);
                statusView.setText(isPortOpen ? R.string.port_is_open : R.string.port_is_closed);
                statusView.setTextColor(isPortOpen ? mContext.getResources().getColor(R.color.evercam_blue) :
                        Color.RED);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        showProgressBar(true);
        showStatusTextView(false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            return isPortOpen(mIp, mPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isOpen) {
        showProgressBar(false);
        updatePortStatus(isOpen);
    }

    public static boolean isPortOpen(String ip, String port) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getUrl(ip, port)).build();

        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                String responseString = response.body().string();
                return isResponseIndicatePortOpen(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getUrl(String ip, String port) {
        String apiKey = API.getUserKeyPair()[0];
        String apiId = API.getUserKeyPair()[1];
        return API.MEDIA_URL + "cameras/port-check?address=" + ip + "&port=" + port
                + "&api_key=" + apiKey + "&api_id=" + apiId;
    }

    private static boolean isResponseIndicatePortOpen(String responseString) {
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            return jsonObject.getBoolean("open");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
