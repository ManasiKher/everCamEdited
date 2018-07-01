package io.evercam.androidapp.player;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

public abstract class OnSwipeTouchListener implements View.OnTouchListener {
    private final String TAG = "OnSwipeTouchListener";
    private float lastX = -1;
    private float lastY = -1;
    private ScaleListener scaleListener;
    private ScaleGestureDetector gestureDetector;
    private long time = 0;
    private int screenHeight;
    private int screenWidth;
    private boolean isLandscape = false;

    public OnSwipeTouchListener(Activity activity) {
        scaleListener = new ScaleListener();
        gestureDetector = new ScaleGestureDetector(activity, scaleListener);

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                lastX = event.getX();
                lastY = event.getY();
                break;

            case MotionEvent.ACTION_UP:

                float upX = event.getX();
                float upY = event.getY();

                if (lastX == upX && lastY == upY) {
                    onClick();
                }

                lastX = -1;
                lastY = -1;
                break;

            case MotionEvent.ACTION_MOVE:

                if (scaleListener != null) {
                    if (scaleListener.zoom) {
                        //Stop zooming when live view reach screen top/bottom while zooming in
                        if (scaleListener.getOriginalScaleFactor() < 1.0 || view.getHeight() < getScreenHeight()) {
                            onActionZoom(view);
                        }
                    } else {
                        onActionMove(event, view);
                    }
                }
                lastX = event.getX();
                lastY = event.getY();

                break;
        }
        return true;
    }

    private void onActionZoom(View view) {
        int originalWidth = view.getWidth();
        int originalHeight = view.getHeight();

        long currentTime = System.nanoTime();
        if (time != 0 && (currentTime - time) > 10000000) {
            int leftOffset = (int) (originalWidth - (originalWidth * scaleListener.scaleFactor));
            int topOffset = (int) (originalHeight - (originalHeight * scaleListener.scaleFactor));
            Log.e(TAG, "Offset: " + leftOffset + "," + topOffset);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.setMargins(leftOffset, topOffset, leftOffset, topOffset);
            view.setLayoutParams(layoutParams);

            //Avoid showing the blanks
            if (view.getX() >= 0) {
                view.setX(0);
            }
            if (view.getX() + view.getWidth() <= getScreenWidth()) {
                view.setX(getScreenWidth() - view.getWidth());
            }
        }

        time = System.nanoTime();
    }

    private void onActionMove(MotionEvent event, View view) {
        if (lastX >= 0 && lastY > 0) {
            float newX = event.getX();
            float newY = event.getY();

            int xDiffInt = (int) (newX - lastX);
            int yDiffInt = (int) (newY - lastY);
            Log.d(TAG, "Swiping - Xdiff " + newX + " - " + lastX + " = " + xDiffInt + " Ydiff: " + newY + "-" + lastY + " = " + yDiffInt);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

            //If moving right, stop if image's left reach screen left
            if (xDiffInt > 0) {
                if (layoutParams.leftMargin < 0) {
                    if (view.getX() >= 0) {
                        view.setX(0);
                    } else {
                        view.setX(view.getX() + xDiffInt);
                    }
                }
            }
            //If moving left, stop if image's right reach screen right
            else if (xDiffInt < 0) {
                if (layoutParams.rightMargin < 0) {
                    if (view.getX() + view.getWidth() <= getScreenWidth()) {
                        view.setX(getScreenWidth() - view.getWidth());
                    } else {
                        view.setX(view.getX() + xDiffInt);
                    }
                }
            }

            //Disabled vertical movements
            //view.setY(view.getY() + yDiffInt);
        }
    }

    public void isLandscape(boolean landscape) {
        this.isLandscape = landscape;
    }

    private int getScreenHeight() {
        return isLandscape ? screenWidth : screenHeight;
    }

    private int getScreenWidth() {
        return isLandscape ? screenHeight : screenWidth;
    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        public static final float MIN_ZOOM = 1.0f;
        public static final float MAX_ZOOM = 1.5f;
        public float scaleFactor = 1.0f;
        public float originalScaleFactor = -1;
        public boolean zoom = false;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            originalScaleFactor = detector.getScaleFactor();
            scaleFactor *= originalScaleFactor;
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            Log.e(TAG, "Original Scale Factor = " + originalScaleFactor);
            Log.e(TAG, "Scale Factor = " + scaleFactor);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //Log.d(TAG, "onScaleBegin");
            zoom = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            //Log.d(TAG, "onScaleEnd");
            zoom = false;
        }

        public float getOriginalScaleFactor() {
            return originalScaleFactor;
        }
    }

    public abstract void onClick();
}
