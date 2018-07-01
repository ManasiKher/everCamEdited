package io.evercam.androidapp.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import io.evercam.CameraShare;
import io.evercam.CameraShareInterface;
import io.evercam.CameraShareOwner;
import io.evercam.CameraShareRequest;
import io.evercam.EvercamException;
import io.evercam.androidapp.sharing.SharingActivity;

public class FetchShareListTask extends AsyncTask<Void, Void, ArrayList<CameraShareInterface>> {
    private final String TAG = "FetchShareListTask";
    private final String cameraId;
    private Activity activity;

    public FetchShareListTask(String cameraId, Activity activity) {
        this.cameraId = cameraId;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<CameraShareInterface> doInBackground(Void... params) {
        ArrayList<CameraShareInterface> shareList = new ArrayList<>();

        try {
            shareList.addAll(CameraShare.getByCamera(cameraId));
            shareList.addAll(CameraShareRequest.get(cameraId, CameraShareRequest.STATUS_PENDING));

        } catch (EvercamException e) {
            Log.e(TAG, e.getMessage());
        }

        /**
         * Append the owner details as the first item in sharing list
         */
        if(shareList.size() > 0) {
            if(shareList.get(0) instanceof CameraShare) {
                CameraShareOwner owner = ((CameraShare) shareList.get(0)).getOwner();
                if(owner != null) {
                    shareList.add(0, owner);
                }
            }
            CameraShare cameraShare =  ((CameraShare) shareList.get(1));

            if (cameraShare.toString().equals("{}")) {
                shareList.remove(1);
            }
        }
        return shareList;
    }

    @Override
    protected void onPostExecute(ArrayList<CameraShareInterface> cameraShareList) {
        if (activity instanceof SharingActivity) {
            ((SharingActivity) activity).sharingListFragment
                    .updateShareListOnUi(cameraShareList);
        }
    }

    public static void launch(String cameraId, Activity activity) {
        new FetchShareListTask(cameraId, activity)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
