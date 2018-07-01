package io.evercam.androidapp.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import io.evercam.androidapp.utils.Commons;

public class VolleyRequest {

    /**
     * Load image using Valley, and handle the image response listener
     *
     * @param context
     * @param imageUrl image URL
     * @param view Any view with equivalent size of the image view for decoding image
     * @param listener Implement {@link ImageResponseListener} to handle callback for valid/error image
     */
    public static void loadImage(Context context, String imageUrl, final View view, final ImageResponseListener listener) {
        /**
         * Volley ImageLoader
         */
        // Request an image response from the provided URL.
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        listener.onValidImage(bitmap);
                    }
                }, view.getWidth(), view.getHeight(), ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse != null) {
                            int errorCode = error.networkResponse.statusCode;
                            if (errorCode == 404) {
                                byte[] data = error.networkResponse.data;
                                Bitmap bitmap = Commons.decodeBitmapFromResource(data, view.getWidth());
                                listener.onNotFoundErrorImage(bitmap);
                            }
                        }
                    }
                });
        // Add the request to the singleton RequestQueue.
        VolleySingleton.getInstance(context).addToRequestQueue(imageRequest);
    }

}