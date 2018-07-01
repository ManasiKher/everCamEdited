package io.evercam.androidapp.feedback;

import android.content.Context;
import android.os.AsyncTask;

import io.evercam.androidapp.custom.CustomSnackbar;

public class IntercomSendMessageTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = "IntercomSendMessageTask";
    private String username;
    private String message;
    private Context context;

    public IntercomSendMessageTask(Context context, String username, String message) {
        this.username = username;
        this.message = message;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (IntercomApi.hasApiKey()) {
            String intercomId = IntercomApi.getIntercomIdByUsername(username);

            if (!intercomId.isEmpty()) {
                return IntercomApi.sendMessage(intercomId, message);
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            //CustomToast.showInCenterLong(context, R.string.msg_feedback_sent);
            CustomSnackbar.showFeedbackSent(context);
        }
    }

    public static void launch(Context context, String username, String message) {
        new IntercomSendMessageTask(context, username, message)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
