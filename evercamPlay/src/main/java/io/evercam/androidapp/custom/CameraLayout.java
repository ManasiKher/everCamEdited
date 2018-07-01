package io.evercam.androidapp.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.dto.ImageLoadingStatus;
import io.evercam.androidapp.image.ImageResponseListener;
import io.evercam.androidapp.image.VolleyRequest;
import io.evercam.androidapp.video.VideoActivity;

public class CameraLayout extends LinearLayout implements ImageResponseListener {
    private static final String TAG = "CameraLayout";

    public RelativeLayout cameraRelativeLayout;

    public Context context;
    public EvercamCamera evercamCamera;

    /**
     * Tells whether application has ended or not.
     * If it is true, all tasks must end and no further
     * processing should be done in any thread.
     */
    private boolean end = false;

    /**
     * The image view to show camera's thumbnail image
     */
    private ImageView snapshotImageView;

    /**
     * The image view that holds the placeholder of the thumbnail image
     */
    private ImageView offlineImage = null;

    /**
     *
     */
    private GradientTitleLayout gradientLayout;
    public boolean showOfflineIconAsFloat = false;

    /**
     * Handler for the handling the next request. It will call the image loading
     * thread so that it can proceed with next step.
     */
    public final Handler handler = new Handler();

    public CameraLayout(final Activity activity, EvercamCamera camera, boolean showThumbnails) {
        super(activity.getApplicationContext());
        this.context = activity.getApplicationContext();

        try {
            evercamCamera = camera;

            this.setOrientation(LinearLayout.VERTICAL);
            this.setGravity(Gravity.START);
            this.setBackgroundColor(getResources().getColor(R.color.custom_light_gray));

            cameraRelativeLayout = new RelativeLayout(context);
            RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(android.view
                    .ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams
                    .MATCH_PARENT);
            cameraRelativeLayout.setLayoutParams(ivParams);

            this.addView(cameraRelativeLayout);

            snapshotImageView = new ImageView(context);
            RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            snapshotImageView.setLayoutParams(imageViewParams);
            snapshotImageView.setBackgroundColor(Color.TRANSPARENT);
            snapshotImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            cameraRelativeLayout.addView(snapshotImageView);

            offlineImage = new ImageView(context);
            RelativeLayout.LayoutParams offlineImageParams = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
            offlineImageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            offlineImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
            offlineImage.setLayoutParams(offlineImageParams);
            offlineImage.setScaleType(ImageView.ScaleType.FIT_XY);
            cameraRelativeLayout.addView(offlineImage);
            offlineImage.setVisibility(View.INVISIBLE);

            gradientLayout = new GradientTitleLayout(activity);
            gradientLayout.setTitle(evercamCamera.getName());
            cameraRelativeLayout.addView(gradientLayout);

            cameraRelativeLayout.setClickable(true);

            // Show thumbnail returned from Evercam
            if (showThumbnails) {
                showThumbnail();
            }
            cameraRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoActivity.startPlayingVideoForCamera(activity, evercamCamera.getCameraId());
                }
            });
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
        }
    }

    public Rect getOfflineIconBounds() {
        Rect bounds = new Rect();
        gradientLayout.getOfflineImageView().getHitRect(bounds);
        return bounds;
    }

    public void updateTitleIfDifferent() {
        for (EvercamCamera camera : AppData.evercamCameraList) {
            if (evercamCamera.getCameraId().equals(camera.getCameraId())) {
                gradientLayout.setTitle(camera.getName());
            }
        }
    }

    // Stop the image loading process. May be need to end current activity
    public boolean stopAllActivity() {
        end = true;

        return true;
    }

    // Image loaded form camera and now set the controls appearance and text
    // accordingly
    private void setLayoutForLiveImageReceived() {
        evercamCamera.setIsOnline(true);
        offlineImage.setVisibility(View.INVISIBLE);

        handler.removeCallbacks(LoadImageRunnable);
    }

    public boolean showThumbnail() {
        if (evercamCamera.hasThumbnailUrl()) {

            final String thumbnailUrl = evercamCamera.getThumbnailUrl();
            VolleyRequest.loadImage(context, thumbnailUrl, this, this);

            if (!evercamCamera.isOnline()) {
                showGreyImage();
                showOfflineIcon();
            }

            return true;
        } else {
            /**
             * Moved to Picasso/Volley error callback. The thumbnail URL is impossible to be empty
             * because it's a REST API URL.
             */
        }
        return false;
    }

    private void showOfflineIcon() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gradientLayout.showOfflineIcon(true, showOfflineIconAsFloat);
            }
        }, 300);
    }

    // Image not received form cache, Evercam nor camera side. Set the controls
    // appearance and text accordingly
    private void setLayoutForNoImageReceived() {
        if (!evercamCamera.isOnline()) {
            showGreyImage();

            showOfflineIcon();
        }

        // animation must have been stopped when image loaded from cache
        handler.removeCallbacks(LoadImageRunnable);
    }

    public Runnable LoadImageRunnable = new Runnable() {
        @Override
        public void run() {
            if (end) return;

            if (evercamCamera.loadingStatus == ImageLoadingStatus.not_started) {
                if (evercamCamera.isOnline()) {
                }
            } else if (evercamCamera.loadingStatus == ImageLoadingStatus.live_received) {
                setLayoutForLiveImageReceived();
            } else if (evercamCamera.loadingStatus == ImageLoadingStatus.live_not_received) {
                setLayoutForNoImageReceived();
            }
        }
    };

    private void showGreyImage() {
        snapshotImageView.setAlpha(0.5f);
    }

    /**
     * Handle the error thumbnail image with {@link ImageResponseListener}
     */
    @Override
    public void onNotFoundErrorImage(Bitmap bitmap) {
        offlineImage.setVisibility(View.VISIBLE);
        offlineImage.setImageBitmap(bitmap);

        CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_not_received;
        handler.postDelayed(LoadImageRunnable, 0);
    }

    @Override
    public void onValidImage(Bitmap bitmap) {
        snapshotImageView.setImageBitmap(bitmap);
    }
}