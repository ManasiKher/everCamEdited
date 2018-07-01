package io.evercam.androidapp.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import io.evercam.Camera;
import io.evercam.CameraShare;
import io.evercam.EvercamException;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EnumConstants.DeleteType;

public class DeleteCameraTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = "DeleteCameraTask";
    private String cameraId;
    private CustomProgressDialog customProgressDialog;
    private Activity activity;
    private DeleteType deleteType;

    public DeleteCameraTask(String cameraId, Activity activity, DeleteType type) {
        this.cameraId = cameraId;
        this.activity = activity;
        this.deleteType = type;
    }

    @Override
    protected void onPreExecute() {
        customProgressDialog = new CustomProgressDialog(activity);
        if (deleteType == DeleteType.DELETE_OWNED) {
            customProgressDialog.show(activity.getString(R.string.deleting_camera));
        } else if (deleteType == DeleteType.DELETE_SHARE) {
            customProgressDialog.show(activity.getString(R.string.removing_camera));
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (deleteType == DeleteType.DELETE_OWNED) {
                if (Camera.delete(cameraId)) {
                    return true;
                }
            } else {
                if (AppData.defaultUser != null) {
                    return CameraShare.delete(cameraId, AppData.defaultUser.getUsername());
                } else {
                    //This should never happen
                }
            }
        } catch (EvercamException e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        customProgressDialog.dismiss();
        if (success) {
            activity.setResult(Constants.RESULT_DELETED);
            activity.finish();
        } else {
            CustomToast.showInCenter(activity, R.string.msg_delete_failed);
        }
    }
}
