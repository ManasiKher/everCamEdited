package io.evercam.androidapp.photoview;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomSnackbar;
import io.evercam.androidapp.custom.CustomToast;

public class SnapshotManager {
    private final static String TAG = "SnapshotManager";
    public static final String SNAPSHOT_FOLDER_NAME_EVERCAM = "Evercam";
    public static final String SNAPSHOT_FOLDER_NAME_PLAY = "Evercam Play";

    public enum FileType {
        PNG, JPG
    }

    /**
     * Produce a path for the snapshot to be saved in format:
     * Folder:Evercam/Evercam Play/camera id
     * File name: camera id + current time + file type ending
     * For example Pictures/Evercam/Evercam Play/cameraid/cameraid_20141225_091011.jpg
     *
     * @param cameraId the unique camera id from Evercam
     * @param fileType PNG or JPG depend on it's from video or JPG view
     * @return snapshot file path
     */
    public static String createFilePath(String cameraId, FileType fileType) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeString = dateFormat.format(Calendar.getInstance().getTime());
        String fileName = cameraId + "_" + timeString + fileType(fileType);

        File folder = new File(getPlayFolderPathForCamera(cameraId));
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder.getPath() + File.separator + fileName;
    }

    public static String getPlayFolderPathForCamera(String cameraId) {
        return getPlayFolderPath() + File.separator + cameraId;
    }

    public static String getPlayFolderPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + SNAPSHOT_FOLDER_NAME_EVERCAM + File.separator +
                SNAPSHOT_FOLDER_NAME_PLAY;
    }

    public static void showSnapshotsForCamera(Activity activity, String cameraId) {
        String playFolderPath = SnapshotManager.getPlayFolderPathForCamera(cameraId);
        File folder = new File(playFolderPath);
        String[] allFiles = folder.list();
        if (allFiles != null && allFiles.length > 0) {
            //Sort the snapshots by name (Latest first)
            Arrays.sort(allFiles, Collections.reverseOrder());

            //Append full path to all file names
            int arrayLength = allFiles.length;
            for (int index = 0; index < arrayLength; index++) {
                allFiles[index] = playFolderPath + File.separator + allFiles[index];
            }

            ViewPagerActivity.showSavedSnapshots(activity, allFiles);
        } else {
            CustomToast.showInCenter(activity, R.string.msg_no_snapshot_saved_camera);
        }
    }

    private static String fileType(FileType fileType) {
        if (fileType.equals(FileType.PNG)) {
            return ".png";
        } else {
            return ".jpg";
        }
    }

    /**
     * Notify Gallery about the snapshot that got saved, otherwise the image
     * won't show in Gallery
     *
     * @param path full snapshot path
     */
    public static void updateGallery(String path, String cameraId, Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStoragePublicDirectory
                            (Environment.DIRECTORY_PICTURES))));
        } else {
            new SingleMediaScanner(activity).startScan(path, cameraId);
        }
    }

    static class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
        MediaScannerConnection connection;
        Activity activity;
        private String imagePath;
        private String cameraId = "";

        public SingleMediaScanner(Activity activity) {
            this.activity = activity;
        }

        public void startScan(String url, String cameraId) {
            imagePath = url;
            this.cameraId = cameraId;
            if (connection != null) connection.disconnect();
            connection = new MediaScannerConnection(activity, this);
            connection.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            try {
                connection.scanFile(imagePath, null);
            } catch (java.lang.IllegalStateException e) {
            }
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CustomSnackbar.showSnapshotSaved(activity, cameraId);
                }
            });

            connection.disconnect();
        }
    }
}
