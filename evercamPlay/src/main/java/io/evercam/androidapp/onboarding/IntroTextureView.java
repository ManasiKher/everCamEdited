package io.evercam.androidapp.onboarding;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;

import io.evercam.androidapp.R;

public class IntroTextureView extends TextureView
        implements TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer player;

    private float mVideoHeight;
    private float mVideoWidth;

    public IntroTextureView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
        init();
    }

    public IntroTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
        init();
    }

    public IntroTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * MediaPlayer.OnPreparedListener
     * Callback for player.prepareAsync();
     */

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setLooping(true);
        mp.start();
    }

    /**
     * TextureView.SurfaceTextureListener
     */

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        playIntro(surfaceTexture);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        updateTextureViewSize();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        player.stop();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * Private methods
     */

    private void init() {
        setSurfaceTextureListener(this);
    }

    // Center crop TextureView
    private void updateTextureViewSize() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float scaleX = 1.0f;
        float scaleY = 1.0f;

        if (mVideoWidth > viewWidth && mVideoHeight > viewHeight) {
            scaleX = mVideoWidth / viewWidth;
            scaleY = mVideoHeight / viewHeight;
        } else if (mVideoWidth < viewWidth && mVideoHeight < viewHeight) {
            scaleY = viewWidth / mVideoWidth;
            scaleX = viewHeight / mVideoHeight;
        } else if (viewWidth > mVideoWidth) {
            scaleY = (viewWidth / mVideoWidth) / (viewHeight / mVideoHeight);
        } else if (viewHeight > mVideoHeight) {
            scaleX = (viewHeight / mVideoHeight) / (viewWidth / mVideoWidth);
        }

        // Calculate pivot points, in our case crop from center
        int pivotPointX = (int) (viewWidth / 2);
        int pivotPointY = (int) (viewHeight / 2);

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);

        setTransform(matrix);
    }

    private void playIntro(SurfaceTexture surfaceTexture) {
        if (player != null) {
            player.release();
            player = null;
        }

        try {
            player = new MediaPlayer();
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.gpoview);
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            player.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setSurface(new Surface(surfaceTexture));

        player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                mVideoWidth = width;
                mVideoHeight = height;
                updateTextureViewSize();
            }
        });

        player.prepareAsync();
    }
}
