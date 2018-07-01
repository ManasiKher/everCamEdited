package io.evercam.androidapp.custom;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import io.evercam.androidapp.R;
import io.evercam.androidapp.photoview.SnapshotManager;
import io.intercom.android.sdk.Intercom;

public class CustomSnackbar {
    public static void showShort(Activity activity, int messageId) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), messageId, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(activity.getResources().getColor(R.color.dark_gray_background));
        snackbar.show();

    }

    public static void showLong(Activity activity, int messageId) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), messageId, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(activity.getResources().getColor(R.color.dark_gray_background));
        snackbar.show();
    }

    public static void showSnapshotSaved(final Activity activity, final String cameraId) {
        Snackbar.make(activity.findViewById(android.R.id.content), R.string.msg_snapshot_saved, Snackbar.LENGTH_LONG)
                .setAction(R.string.view_capital, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SnapshotManager.showSnapshotsForCamera(activity, cameraId);
                    }
                }).show();
    }

    public static void showFeedbackSent(final Context context) {
        Snackbar.make(((Activity) context).findViewById(android.R.id.content), R.string.msg_feedback_sent, Snackbar.LENGTH_LONG)
                .setAction(R.string.view_capital, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intercom.client().displayConversationsList();
                    }
                }).show();
    }
}
