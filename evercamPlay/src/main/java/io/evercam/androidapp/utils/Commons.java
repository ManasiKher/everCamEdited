package io.evercam.androidapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Commons {
    static String TAG = "Commons";
    static boolean enableLogs = false;

    public static boolean isOnline(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context
                    .CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo()
                    .isConnectedOrConnecting();

        } catch (Exception ex) {
            if (enableLogs) Log.e(TAG, ex.toString());
        }
        return false;
    }

    public static String readRawTextFile(int id, Context ctx) {
        InputStream inputStream = ctx.getResources().openRawResource(id);
        InputStreamReader in = new InputStreamReader(inputStream);
        BufferedReader buf = new BufferedReader(in);
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = buf.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String[] joinStringArray(String[]... arrays) {
        int size = 0;
        for (String[] array : arrays) {
            size += array.length;
        }
        java.util.List list = new java.util.ArrayList(size);
        for (String[] array : arrays) {
            list.addAll(java.util.Arrays.asList(array));
        }
        return (String[]) list.toArray(new String[size]);
    }

    public static float calculateTimeDifferenceFrom(Date startTime) {
        long timeDifferenceLong = (new Date()).getTime() - startTime.getTime();
        return (float) timeDifferenceLong / 1000;
    }

    public static boolean isLocalIp(String ip) {
        if (ip != null && !ip.isEmpty()) {
            if (ip.matches(Constants.REGULAR_EXPRESSION_LOCAL_IP)) {
                return true;
            }
        }
        return false;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth) {
        // Raw height and width of image
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > reqWidth) {

            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps
            // width larger than the requested height and width.
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeBitmapFromResource(byte[] byteArray,
                                                  int reqWidth) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
    }

    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public static String md5Hex(String message) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex(md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public static String getGravatarUrl(String email) {
        return "http://www.gravatar.com/avatar/" + md5Hex(email) + "?d=404";
    }
}
