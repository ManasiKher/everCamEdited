package io.evercam.androidapp.feedback;

import android.content.Context;

import java.util.HashMap;

import io.evercam.androidapp.utils.Constants;


public class ShortcutFeedbackItem extends FeedbackItem {
    public static final String ACTION_TYPE_CREATE = "created";
    public static final String ACTION_TYPE_USE = "used";

    public static final String RESULT_TYPE_SUCCESS = "success";
    public static final String RESULT_TYPE_FAILED = "failed";

    private String camera_id = "";
    private String action_type = "";
    private String result = "";

    public ShortcutFeedbackItem(Context context, String username, String cameraId, String
            actionType, String resultType) {
        super(context, username);
        this.camera_id = cameraId;
        this.action_type = actionType;
        this.result = resultType;
    }

    @Override
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> map = super.toHashMap();
        map.put("camera_id", camera_id);
        map.put("action_type", action_type);
        map.put("result", result);
        return map;
    }

}
