package io.evercam.androidapp.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import io.evercam.CameraShareRequest;
import io.evercam.EvercamException;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomSnackbar;
import io.evercam.androidapp.custom.CustomToast;

public class ResendShareRequestTask extends AsyncTask<Void, Void, Boolean> {

    private CameraShareRequest shareRequest;
    private Activity activity;
    private String errorMessage;

    public ResendShareRequestTask(Activity activity, CameraShareRequest shareRequest) {
        this.activity = activity;
        this.shareRequest = shareRequest;
    }

    @Override
    protected void onPreExecute() {
        errorMessage = activity.getString(R.string.unknown_error);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            return CameraShareRequest.resend(shareRequest.getCameraId(), shareRequest.getEmail());
        } catch (EvercamException e) {
            errorMessage = e.getMessage();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {

        if (isSuccess) {
            CustomSnackbar.showLong(activity, R.string.msg_share_resent);
        } else {
            CustomToast.showInCenterLong(activity, errorMessage);
        }
    }

    public static void launch(Activity activity, CameraShareRequest shareRequest) {
        new ResendShareRequestTask(activity, shareRequest)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
