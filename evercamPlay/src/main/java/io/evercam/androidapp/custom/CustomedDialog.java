package io.evercam.androidapp.custom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;

import io.evercam.CameraShare;
import io.evercam.CameraShareInterface;
import io.evercam.CameraShareRequest;
import io.evercam.EvercamObject;
import io.evercam.Right;
import io.evercam.androidapp.R;
import io.evercam.androidapp.addeditcamera.AddCameraActivity;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.feedback.IntercomSendMessageTask;
import io.evercam.androidapp.sharing.RightsStatus;
import io.evercam.androidapp.sharing.SharingActivity;
import io.evercam.androidapp.sharing.SharingListFragment;
import io.evercam.androidapp.sharing.SharingStatus;
import io.evercam.androidapp.tasks.CreatePresetTask;
import io.evercam.androidapp.tasks.ResendShareRequestTask;
import io.evercam.androidapp.tasks.TransferOwnershipTask;
import io.evercam.androidapp.video.VideoActivity;
import io.evercam.network.discovery.Device;

public class CustomedDialog {
    public final static String TAG = "CustomedDialog";

    /**
     * Helper method to show unexpected error dialog.
     */
    public static void showUnexpectedErrorDialog(Activity activity) {
        getStandardStyledDialog(activity, R.string.msg_error_occurred, R.string.msg_exception,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }, null, R.string.ok, 0).show();
    }

    /**
     * The dialog that prompt to connect Internet, with listener.
     */
    public static AlertDialog getNoInternetDialog(final Activity activity,
                                                  DialogInterface.OnClickListener negativeistener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity)
                .setTitle(R.string.msg_network_not_connected)
                .setMessage(R.string.msg_try_network_again)
                .setCancelable(false).setPositiveButton(R.string.settings_capital, new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
                        }).setNegativeButton(R.string.notNow, negativeistener);
        return dialogBuilder.create();
    }

    /**
     * The single message dialog that contains title, a message, two buttons(Yes
     * & No) and two listeners.
     * <p/>
     * If int negativeButton == 0, it will be a dialog without negative button
     */
    private static AlertDialog getStandardStyledDialog(final Activity activity, int title,
                                                       int message,
                                                       DialogInterface.OnClickListener
                                                               positiveListener,
                                                       DialogInterface.OnClickListener
                                                               negativeListener,
                                                       int positiveButton, int negativeButton) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false).setPositiveButton(positiveButton, positiveListener);
        if (negativeButton != 0) {
            dialogBuilder.setNegativeButton(negativeButton, negativeListener);
        }
        return dialogBuilder.create();
    }

    public static AlertDialog getCanNotPlayDialog(final Activity activity,
                                                  DialogInterface.OnClickListener positiveListener) {
        return getStandardStyledDialog(activity, R.string.msg_unable_to_play, R.string
                .msg_please_check_camera, positiveListener, null, R.string.ok, 0);
    }

    /**
     * Return the styled dialog with title and message to ask for confirmation
     * to create camera.
     */
    public static AlertDialog getConfirmCreateDialog(Activity activity,
                                                     DialogInterface.OnClickListener
                                                             positiveListener,
                                                     DialogInterface.OnClickListener
                                                             negativeListener) {
        return getStandardStyledDialog(activity,
                R.string.dialog_title_warning, R.string.msg_confirm_create, positiveListener,
                negativeListener, R.string.yes, R.string.no);
    }

    public static AlertDialog getConfirmQuitFeedbackDialog(Activity activity,
                                                           DialogInterface.OnClickListener
                                                                   positiveListener) {
        return getStandardStyledDialog(activity,
                R.string.dialog_title_warning, R.string.msg_confirm_quit_feedback,
                positiveListener, null, R.string.yes, R.string.cancel);
    }

    /**
     * The helper method to show Internet alert dialog and finish the activity.
     */
    public static void showInternetNotConnectDialog(final Activity activity) {
        CustomedDialog.getNoInternetDialog(activity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.finish();
            }
        }).show();
    }

    /**
     * The alert dialog with no title, but with a cancel button Used in account management.
     */
    public static AlertDialog getAlertDialogNoTitle(Context ctx, View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);

        view.setPadding(14, 10, 5, 21);

        dialogBuilder.setView(view);
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static AlertDialog getConfirmCancelScanDialog(Activity activity,
                                                         DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(activity)

                .setMessage(R.string.msg_confirm_cancel_scan).setPositiveButton(R.string.yes,
                        listener).setNegativeButton(R.string.no, null).create();
    }

    public static AlertDialog getConfirmCancelAddCameraDialog(final Activity activity) {
        return new AlertDialog.Builder(activity)

                .setMessage(R.string.msg_confirm_cancel_add_camera).setPositiveButton(R.string
                        .yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                }).setNegativeButton(R.string.no, null).create();
    }

    public static AlertDialog getConfirmRemoveDialog(Activity activity,
                                                     DialogInterface.OnClickListener listener,
                                                     int message) {
        return getConfirmDialog(activity, listener, message, R.string.remove);
    }

    public static AlertDialog getConfirmDialog(Activity activity,
                                               DialogInterface.OnClickListener listener,
                                               int message,
                                               int positiveButtonText) {
        return new AlertDialog.Builder(activity)
                .setMessage(message).setPositiveButton(positiveButtonText,
                        listener).setNegativeButton(R.string.cancel, null).create();
    }

    public static AlertDialog getSingleButtonDialog(Activity activity,
                                                    DialogInterface.OnClickListener listener,
                                                    int message,
                                                    int positiveButtonText) {
        return new AlertDialog.Builder(activity)
                .setMessage(message).setPositiveButton(positiveButtonText,
                        listener).create();
    }

    public static AlertDialog getConfirmDeleteDialog(Activity activity,
                                                     DialogInterface.OnClickListener listener,
                                                     int message) {
        return getConfirmDialog(activity, listener, message, R.string.delete);
    }

    /**
     * Return a pop up dialog that shows camera snapshot.
     *
     * @param bitmap the returned image Bitmap to show in pop up dialog
     */
    public static AlertDialog getSnapshotDialog(final Activity activity, Bitmap bitmap) {
        Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog snapshotDialog = builder.create();

        snapshotDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        LayoutInflater mInflater = LayoutInflater.from(activity);
        final View snapshotView = mInflater.inflate(R.layout.dialog_test_snapshot, null);
        Button nextButton = (Button) snapshotView.findViewById(R.id.connect_camera_next_button);

        if (activity instanceof AddCameraActivity) {
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AddCameraActivity) activity).showCameraNameView();
                    snapshotDialog.dismiss();
                }
            });
        }
        ImageView snapshotImageView = (ImageView) snapshotView.findViewById(R.id
                .test_snapshot_image);
        snapshotImageView.setImageBitmap(bitmap);
        snapshotDialog.setView(snapshotView);

        return snapshotDialog;
    }

    /**
     * Return a pop up dialog that ask the user whether or not to save the snapshot
     */
    public static AlertDialog getConfirmSnapshotDialog(Activity activity, Bitmap bitmap,
                                                       DialogInterface.OnClickListener listener) {
        Builder snapshotDialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater mInflater = LayoutInflater.from(activity);
        final View snapshotView = mInflater.inflate(R.layout.dialog_confirm_snapshot, null);
        ImageView snapshotImageView = (ImageView) snapshotView.findViewById(R.id
                .confirm_snapshot_image);
        snapshotImageView.setImageBitmap(bitmap);
        snapshotDialogBuilder.setView(snapshotView);
        snapshotDialogBuilder.setPositiveButton(activity.getString(R.string.save), listener);
        snapshotDialogBuilder.setNegativeButton(activity.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog snapshotDialog = snapshotDialogBuilder.create();
        snapshotDialog.setCanceledOnTouchOutside(false);

        return snapshotDialog;
    }

    /**
     * Dialog without title, but with a message and an 'OK' button
     *
     * @param message Message to show in the dialog
     */
    public static void showMessageDialog(Activity activity, int message) {
        new MaterialDialog.Builder(activity)
                .content(message)
                .negativeText(R.string.ok)
                .show();
    }

    public static void showMessageDialog(Activity activity, String message) {
        new MaterialDialog.Builder(activity)
                .content(message)
                .negativeText(R.string.ok)
                .show();
    }

    /**
     * The standard alert dialog that ask the user for a yes or no choice
     *
     * @param listener The callback that perform a option when user choose yes
     * @param message  The message resource to show in the dialog
     * @return
     */
    public static AlertDialog getStandardAlertDialog(Activity activity,
                                                     DialogInterface.OnClickListener listener,
                                                     int message) {
        return new AlertDialog.Builder(activity)
                .setMessage(message).setPositiveButton(R.string.yes, listener).setNegativeButton
                        (R.string.no, null).create();
    }

    /**
     * The prompt dialog that ask for a preset name
     */
    public static AlertDialog getCreatePresetDialog(final VideoActivity videoActivity, final String cameraId) {
        Builder dialogBuilder = new AlertDialog.Builder(videoActivity);
        LayoutInflater inflater = LayoutInflater.from(videoActivity);
        final View view = inflater.inflate(R.layout.dialog_create_preset, null);
        final EditText editText = (EditText) view.findViewById(R.id.create_preset_edit_text);
        dialogBuilder.setView(view);
        dialogBuilder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String presetName = editText.getText().toString();
                if (!presetName.isEmpty()) {
                    new CreatePresetTask(videoActivity, cameraId, presetName)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        return dialogBuilder.create();
    }

    /**
     * The dialog that allow user changing the access permission on the specific camera
     */
    public static AlertDialog getShareStatusDialog(final SharingListFragment fragment,
                                                   String selectedItem) {
        final Activity activity = fragment.getActivity();
        final CharSequence[] shareStatusItems = {activity.getString(R.string.sharing_status_public),
                activity.getString(R.string.sharing_status_link),
                activity.getString(R.string.sharing_status_specific_user)};
        int selectedItemPosition = Arrays.asList(shareStatusItems).indexOf(selectedItem);
        return new AlertDialog.Builder(activity)
                .setSingleChoiceItems(shareStatusItems, selectedItemPosition, null)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView listView = ((AlertDialog) dialog).getListView();
                        Object checkedItem = listView.getAdapter().getItem(listView
                                .getCheckedItemPosition());

                        SharingStatus status = new SharingStatus(checkedItem.toString(),
                                activity);

                        fragment.patchSharingStatusAndUpdateUi(status);
                    }
                })
                .setNegativeButton(R.string.cancel, null).create();
    }

    public static AlertDialog getRightsStatusDialog(final SharingListFragment fragment
            , final CameraShareInterface shareInterface) {

        final Activity activity = fragment.getActivity();

        final CharSequence[] statusItems = RightsStatus.getFullItems(activity);

        String selectedItem = fragment.getString(R.string.read_only);

        Right rights = EvercamObject.getRightsFrom(shareInterface);
        if (rights != null && rights.isFullRight()) {
            selectedItem = fragment.getString(R.string.full_rights);
        }
        int selectedItemPosition = Arrays.asList(statusItems).indexOf(selectedItem);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        Button resendButton = null;
        if(shareInterface instanceof CameraShareRequest) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View view = inflater.inflate(R.layout.partial_sharing_dialog_title, null);
            resendButton = (Button) view.findViewById(R.id.sharing_dialog_resend_button);
            dialogBuilder.setCustomTitle(view);
        } else {
            dialogBuilder.setTitle(R.string.sharing_settings_title);
        }

        final AlertDialog dialog = dialogBuilder
                .setSingleChoiceItems(statusItems, selectedItemPosition, null)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ListView listView = ((AlertDialog) dialog).getListView();
                        Object checkedItem = listView.getAdapter().getItem(listView.getCheckedItemPosition());

                        RightsStatus newRightStatus = new RightsStatus(activity, checkedItem.toString());

                        //If user selected 'No access', show warning dialog before delete the share
                        //or revoking the share request
                        if (newRightStatus.getRightString() == null) {
                            CustomedDialog.getConfirmRemoveShareDialog(activity, shareInterface,
                                    newRightStatus).show();
                        } else {
                            AppUser defaultUser = AppData.defaultUser;
                            String LoggedInEmail = defaultUser.getEmail();
                            String currentUserEmail = ((CameraShare) shareInterface).getUserEmail();
                            if (LoggedInEmail.equals(currentUserEmail)){
                                CustomedDialog.showCannotChangeRightsDialog(activity, shareInterface).show();
                            }else{
                                newRightStatus.updateOnShare(shareInterface);
                            }
                        }
                    }
                }).setNegativeButton(R.string.cancel, null).create();

        if(resendButton != null) {
            resendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    ResendShareRequestTask.launch(activity, (CameraShareRequest) shareInterface);
                }
            });
        }
        return dialog;
    }

    public static AlertDialog getConfirmRemoveShareDialog(Activity activity,
                                                          final CameraShareInterface shareInterface,
                                                          final RightsStatus newRightStatus) {
        int positiveButtonTextId = R.string.remove;
        int messageTextId = R.string.msg_confirm_remove_share;
        if (shareInterface instanceof CameraShareRequest) {
            positiveButtonTextId = R.string.revoke;
            messageTextId = R.string.msg_confirm_revoke_share_request;
        } else if (shareInterface instanceof CameraShare) {
            if (((CameraShare) shareInterface).getUserId().equals(AppData.defaultUser.getUsername())) {
                messageTextId = R.string.msg_confirm_remove_self;
            }
        }

        return getConfirmDialog(activity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newRightStatus.updateOnShare(shareInterface);
            }
        }, messageTextId, positiveButtonTextId);
    }

    public static AlertDialog showCannotChangeRightsDialog(Activity activity,
                                                          final CameraShareInterface shareInterface) {
//        int positiveButtonTextId = R.string.ok;
        int messageTextId = R.string.msg_cannot_change_rights;

        if (((CameraShare) shareInterface).getUserId().equals(AppData.defaultUser.getUsername())) {
            messageTextId = R.string.msg_cannot_change_rights;
        }
        return new AlertDialog.Builder(activity).setMessage(messageTextId).setCancelable(true).create();
    }

    public static AlertDialog getSelectNewOwnerDialog(final Activity activity, final ArrayList<String> usernameList) {
        CharSequence[] listCharArray = usernameList.toArray(new CharSequence[usernameList.size()]);

        Builder dialogBuilder = new AlertDialog.Builder(activity)
                .setNegativeButton(R.string.cancel, null);

        if (usernameList.size() > 0) {
            dialogBuilder.setSingleChoiceItems(listCharArray, 0, null)
                    .setPositiveButton(R.string.transfer, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ListView listView = ((AlertDialog) dialog).getListView();
                            Object checkedItem = listView.getAdapter().getItem(listView
                                    .getCheckedItemPosition());
                            String selectedUsername = checkedItem.toString();

                            if (activity instanceof SharingActivity) {
                                TransferOwnershipTask.launch(activity, SharingActivity
                                        .evercamCamera.getCameraId(), selectedUsername);
                            }
                        }
                    }).setTitle(R.string.transfer_select_title);
        } else {
            dialogBuilder.setMessage(R.string.msg_share_before_transfer);
        }

        return dialogBuilder.create();
    }

    public static void showReportCameraModelDialog(final Context context, final Device device) {
        new MaterialDialog.Builder(context)
                .title(R.string.msg_specify_camera_model)
                .content(R.string.msg_report_camera_model)
                .negativeText(R.string.cancel)
                .positiveText(R.string.report)
                .input(R.string.hint_camera_model, R.string.empty, new MaterialDialog
                        .InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, final CharSequence input) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (AppData.defaultUser != null) {
                                    String modelName = String.valueOf(input);
                                    String message = "";
                                    if (device != null) {
                                        message = "This is a camera:  " + device.toString() + " \n\nCamera model name: " + modelName;
                                    } else {
                                        if(!modelName.isEmpty()) {
                                            message = "I'd like to report a new camera model: " + modelName;
                                        }
                                    }
                                    if(!message.isEmpty()) {
                                        IntercomSendMessageTask.launch(context, AppData
                                                .defaultUser.getUsername(), message);
                                    }
                                }
                            }
                        }).start();
                    }
                }).show();
    }

    public static void showMessageDialogWithTitle(Context context, int title, int message) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(message)
                .negativeText(R.string.ok)
                .show();
    }
}
