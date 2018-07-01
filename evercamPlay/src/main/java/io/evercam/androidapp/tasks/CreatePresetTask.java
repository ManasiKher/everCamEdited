package io.evercam.androidapp.tasks;

import android.os.AsyncTask;

import java.util.ArrayList;

import io.evercam.PTZException;
import io.evercam.PTZPreset;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.video.VideoActivity;

public class CreatePresetTask extends AsyncTask<Void, Void, Boolean> {
    private String cameraId = "";
    private String presetName = "";
    private CustomProgressDialog customProgressDialog;
    private VideoActivity activity;
    private String errorMessage = "";

    public CreatePresetTask(VideoActivity activity, String cameraId, String presetName) {
        this.cameraId = cameraId;
        this.presetName = presetName;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        customProgressDialog = new CustomProgressDialog(activity);
        customProgressDialog.show(activity.getString(R.string.msg_creating_preset));
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            PTZPreset.create(cameraId, presetName);

            ArrayList<PTZPreset> allPresets = PTZPreset.getAllPresets(cameraId);
            ArrayList<PTZPreset> customPresets = removeSystemPresetsFrom(allPresets);

            if (customPresets.size() > 0) {
                activity.presetList = customPresets;
            }

            return true;
        } catch (PTZException e) {
            errorMessage = e.getMessage();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        customProgressDialog.dismiss();
        CustomToast.showInCenter(activity, success ? activity.getString(R.string
                .msg_preset_created) : errorMessage);
    }

    private ArrayList<PTZPreset> removeSystemPresetsFrom(ArrayList<PTZPreset> allPresets) {
        ArrayList<PTZPreset> customPresets = new ArrayList<>();
        if (allPresets.size() > 0) {
            //Exclude presets with token >= 33 and only keep those user defined presets
            for (PTZPreset preset : allPresets) {
                int tokenInt = Integer.valueOf(preset.getToken());
                if (tokenInt < 33) {
                    customPresets.add(preset);
                }
            }
        }
        return customPresets;
    }
}
