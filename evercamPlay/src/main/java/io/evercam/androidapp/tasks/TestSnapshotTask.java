package io.evercam.androidapp.tasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;

import io.evercam.Camera;
import io.evercam.Snapshot;
import io.evercam.androidapp.EditCameraActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.addeditcamera.AddCameraActivity;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.feedback.TestSnapshotFeedbackItem;

public class TestSnapshotTask extends AsyncTask<Void, Void, Bitmap> {
    private final String TAG = "TestSnapshotTask";
    private String url;
    private String ending;
    private String username;
    private String password;
    private Activity activity;
    private CustomProgressDialog customProgressDialog;
    private String errorMessage = null;
    private String vendor_id;
    private String camera_exId;
    

    public TestSnapshotTask(String url, String ending, String username, String password, Activity activity, String vendor_id, String camera_exId) {
        this.url = url;
        this.ending         = ending;
        this.username       = username;
        this.password       = password;
        this.activity       = activity;
        this.vendor_id      = vendor_id;
        this.camera_exId    = camera_exId;
    }

    @Override
    protected void onPreExecute() {
        if (activity instanceof EditCameraActivity) {
            customProgressDialog = new CustomProgressDialog(activity);
            customProgressDialog.show(activity.getString(R.string.retrieving_snapshot));
        } else if (activity instanceof AddCameraActivity) {
            ((AddCameraActivity) activity).showTestSnapshotProgress(true);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlObject = new URL(url);
            boolean isReachable = PortCheckTask.isPortOpen(urlObject.getHost(),
                    String.valueOf(urlObject.getPort()));
            if (!isReachable) {
                errorMessage = activity.getString(R.string.snapshot_test_port_closed);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }

        try {
            Snapshot snapshot = Camera.testSnapshot(url, ending, username, password,vendor_id,camera_exId);
            if (snapshot != null) {
                byte[] snapshotData = snapshot.getData();
                return BitmapFactory.decodeByteArray(snapshotData, 0, snapshotData.length);
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            Log.e(TAG, e.getMessage());
            Log.e(TAG, e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (activity instanceof EditCameraActivity) {
            customProgressDialog.dismiss();
        } else if (activity instanceof AddCameraActivity) {
            ((AddCameraActivity) activity).showTestSnapshotProgress(false);
        }


        if (bitmap != null) {
            CustomedDialog.getSnapshotDialog(activity, bitmap).show();

        } else {
            String username = "";
            if (AppData.defaultUser != null) {
                username = AppData.defaultUser.getUsername();
            }

            if (errorMessage == null) {
                int messageResourceId = R.string.msg_snapshot_test_failed;
                if (activity instanceof AddCameraActivity) {
                    messageResourceId = R.string.msg_snapshot_test_failed_new;
                }
                CustomedDialog.showMessageDialog(activity, messageResourceId);

            } else {
                CustomedDialog.showMessageDialog(activity, errorMessage);

            }
        }
    }
}
