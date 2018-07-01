package io.evercam.androidapp.image;

import android.graphics.Bitmap;

public interface ImageResponseListener {

    /**
     * Call this method if it returns 404 not found
     * @param bitmap decoded bitmap image
     */
    void onNotFoundErrorImage(Bitmap bitmap);

    /**
     * @param bitmap decoded bitmap image
     */
    void onValidImage(Bitmap bitmap);
}