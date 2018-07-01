package io.evercam.androidapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsManager {
    public final static String KEY_CAMERA_PER_ROW = "lstgridcamerasperrow";
    public final static String KEY_RELEASE_NOTES_SHOWN = "isReleaseNotesShown";
    public static final String KEY_AWAKE_TIME = "prefsAwakeTime";
    public static final String KEY_FORCE_LANDSCAPE = "prefsForceLandscape";
    public static final String KEY_SHOW_OFFLINE_CAMERA = "prefsShowOfflineCameras";
    public final static String KEY_VERSION = "prefsVersion";
    public final static String KEY_SHOWCASE_SHOWN = "isShowcaseShown";
    public final static String KEY_GUIDE = "prefsGuide";

    public final static String KEY_GCM_PREFS_ID = "gcmDetails";
    public final static String KEY_GCM_REGISTRATION_ID = "registrationId";
    public final static String KEY_GCM_APP_VERSION = "gcmAppVersion";

    public static int getCameraPerRow(Context context, int oldNumber) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sharedPrefs.getString(KEY_CAMERA_PER_ROW, "" + oldNumber));
    }

    public static void setCameraPerRow(Context context, int cameraPerRow) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_CAMERA_PER_ROW, "" + cameraPerRow);
        editor.apply();
    }

    public static String getSleepTimeValue(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(KEY_AWAKE_TIME, "" + 0);
    }

    public static boolean isForceLandscape(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(KEY_FORCE_LANDSCAPE, false);
    }

    public static boolean showOfflineCameras(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(KEY_SHOW_OFFLINE_CAMERA, true);
    }

    public static void setShowOfflineCamera(Context context, boolean show) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_SHOW_OFFLINE_CAMERA, show);
        editor.apply();
    }

    public static boolean isReleaseNotesShown(Context context, int versionCode) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(KEY_RELEASE_NOTES_SHOWN + versionCode, false);
    }

    public static boolean isShowcaseShown(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(KEY_SHOWCASE_SHOWN, false);
    }

    public static void setShowcaseShown(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_SHOWCASE_SHOWN, true);
        editor.apply();
    }

    public static void setReleaseNotesShown(Context context, int versionCode) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_RELEASE_NOTES_SHOWN + versionCode, true);
        editor.apply();
    }

    public static void storeGcmRegistrationId(Context context, String regId) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_GCM_PREFS_ID, Activity.MODE_PRIVATE);
        int appVersion = new DataCollector(context).getAppVersionCode();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_GCM_REGISTRATION_ID, regId);
        editor.putInt(KEY_GCM_APP_VERSION, appVersion);
        editor.apply();
    }

    public static String getGcmRegistrationId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_GCM_PREFS_ID, Activity.MODE_PRIVATE);
        String registrationId = prefs.getString(KEY_GCM_REGISTRATION_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        int registeredVersion = prefs.getInt(KEY_GCM_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = new DataCollector(context).getAppVersionCode();
        if (registeredVersion != 0 && registeredVersion != currentVersion) {
            return "";
        }

        return registrationId;
    }
}
