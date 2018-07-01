package io.evercam.androidapp.video;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import io.evercam.EvercamException;
import io.evercam.PTZHome;
import io.evercam.PTZPreset;
import io.evercam.PTZPresetControl;
import io.evercam.PTZRelativeBuilder;
import io.evercam.Right;
import io.evercam.Snapshot;
import io.evercam.androidapp.CamerasActivity;
import io.evercam.androidapp.EvercamPlayApplication;
import io.evercam.androidapp.MainActivity;
import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.ViewCameraActivity;
import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.CameraListAdapter;
import io.evercam.androidapp.custom.CustomSnackbar;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.custom.OfflineLayoutView;
import io.evercam.androidapp.custom.ProgressView;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.feedback.StreamFeedbackItem;
import io.evercam.androidapp.permission.Permission;
import io.evercam.androidapp.photoview.SnapshotManager;
import io.evercam.androidapp.photoview.SnapshotManager.FileType;
import io.evercam.androidapp.player.EventLogger;
import io.evercam.androidapp.player.OnSwipeTouchListener;
import io.evercam.androidapp.ptz.PresetsListAdapter;
import io.evercam.androidapp.recordings.RecordingWebActivity;
import io.evercam.androidapp.sharing.SharingActivity;
import io.evercam.androidapp.tasks.CaptureSnapshotRunnable;
import io.evercam.androidapp.tasks.CheckOnvifTask;
import io.evercam.androidapp.tasks.LiveViewRunnable;
import io.evercam.androidapp.tasks.PTZMoveTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.utils.RxUtils;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import com.google.firebase.analytics.FirebaseAnalytics;

public class VideoActivity extends ParentAppCompatActivity
        implements ExoPlayer.EventListener, TextureView.SurfaceTextureListener,EventLogger.Listener {
    public static EvercamCamera evercamCamera;

    private final static String TAG = "VideoActivity";
    private String liveViewCameraId = "";
    public ArrayList<PTZPreset> presetList = new ArrayList<>();

    private Bitmap mBitmap = null; /* The temp snapshot data while asking for permission */

    /**
     * JPG live view using WebSocket
     */
    private LiveViewRunnable mLiveViewRunnable;
    private boolean showJpgView = false;

    /**
     * ExoPlayer
     */
    private AspectRatioFrameLayout videoFrame;
    private TextureView textureView;
    private Surface surface;
    private boolean playerNeedsPrepare;

    //ExoPlayer 2
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private DefaultTrackSelector trackSelector;
    private EventLogger eventLogger;
    private SimpleExoPlayer player;
    private DataSource.Factory mediaDataSourceFactory;
    private Handler mainHandler;
    private int resumeWindow;
    private long resumePosition;

    /**
     * UI elements
     */
    private ProgressView progressView = null;
    private OfflineLayoutView offlineTextLayout;
    private TextView timeCountTextView;
    private RelativeLayout imageViewLayout;
    private ImageView imageView;
    private ImageView playPauseImageView;
    private ImageView snapshotMenuView;
    private Animation fadeInAnimation = null;
    private RelativeLayout ptzZoomLayout;
    private RelativeLayout ptzMoveLayout;
    private Spinner mCameraListSpinner;

    private Boolean optionsActivityStarted = false;

    public static String startingCameraID;
    private int defaultCameraIndex;

    private boolean paused = false;

    public boolean isPtz = false; //Whether or a PTZ camera model

    private boolean end = false; // whether to end this activity or not

    private boolean editStarted = false;
    private boolean feedbackStarted = false;
    private boolean recordingsStarted = false;
    private boolean sharingStarted = false;
    public static boolean snapshotStarted = false;

    public static boolean showCameraCreated = false;

    private Handler timerHandler = new Handler();
    private Thread timerThread;
    private Runnable timerRunnable;

    private TimeCounter timeCounter;

    private Date startTime;

    private OnSwipeTouchListener swipeTouchListener;

    private Subscription mSubscription;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            clearResumePosition();

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            initAnalyticsObjects();

            readShortcutCameraId();

            if (!liveViewCameraId.isEmpty()) {
                startingCameraID = liveViewCameraId;
                liveViewCameraId = "";
            }

            launchSleepTimer();

            setDisplayOrientation();

            setContentView(R.layout.activity_video);

            mToolbar = (Toolbar) findViewById(R.id.spinner_tool_bar);
            setGradientTitleBackground();
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mCameraListSpinner = (Spinner) findViewById(R.id.spinner_camera_list);

            mediaDataSourceFactory = buildDataSourceFactory(true);
            mainHandler = new Handler();

            initialPageElements();

            checkIsShortcutCameraExists();

            startPlay();


            if (showCameraCreated) {
                CustomSnackbar.showLong(this, R.string.create_success);
                showCameraCreated = false;
            }
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_PATCH_CAMERA
                || requestCode == Constants.REQUEST_CODE_VIEW_CAMERA) {
            // Restart video playing no matter the patch is success or not.
            if (resultCode == Constants.RESULT_TRUE) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomSnackbar.showLong(VideoActivity.this, R.string.patch_success);
                    }
                }, 1000);

                startPlay();
            } else if (resultCode == Constants.RESULT_FALSE) {
                startPlay();
            }
            /* Returned from view camera and the camera has been deleted */
            else if (resultCode == Constants.RESULT_DELETED) {
                setResult(Constants.RESULT_TRUE);
                finish();
            }
        } else
        // If back from feedback or recording or sharing
        {

            if (resultCode == Constants.RESULT_TRANSFERRED) {
                setResult(Constants.RESULT_TRANSFERRED);
                finish();
            } else if (resultCode == Constants.RESULT_ACCESS_REMOVED) {
                setResult(Constants.RESULT_ACCESS_REMOVED);
                finish();
            } else if (resultCode == Constants.RESULT_NO_ACCESS) {
                setResult(Constants.RESULT_NO_ACCESS);
                finish();
            } else {
                startPlay();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        switch (requestCode) {
            case Permission.REQUEST_CODE_STORAGE:

                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                /* If storage permission is granted, continue saving the requested snapshot */
                if (storageAccepted) {
                    if (mBitmap != null) {
                        new Thread(new CaptureSnapshotRunnable(VideoActivity
                                .this, evercamCamera.getCameraId(), FileType.JPG, mBitmap)).start();
                    }
                } else {
                    CustomToast.showInCenter(this, R.string.msg_permission_denied);
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.paused = false;
        editStarted = false;
        feedbackStarted = false;
        recordingsStarted = false;
        sharingStarted = false;
        snapshotStarted = false;

        if (optionsActivityStarted) {
            optionsActivityStarted = false;

            showProgressView(true);

            this.paused = false;
            this.end = false;
        }
    }

    // When activity gets focused again
    @Override
    public void onRestart() {
        super.onRestart();
        paused = false;
        end = false;
        editStarted = false;
        feedbackStarted = false;
        recordingsStarted = false;
        sharingStarted = false;
        snapshotStarted = false;

        if (optionsActivityStarted) {
            setCameraForPlaying(evercamCamera);

            createPlayer(evercamCamera);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }

        if (!optionsActivityStarted) {
            this.paused = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
        end = true;
        if (!optionsActivityStarted) {
            if (showJpgView) {
                this.paused = true;
                disconnectJpgView();
            }
            // Do not finish if user get into edit camera screen,
            // feedback screen recording, sharing, or view snapshot
            if (!editStarted && !feedbackStarted && !recordingsStarted && !sharingStarted && !snapshotStarted) {
                this.finish();
            }
        }

        if (timeCounter != null) {
            timeCounter.stop();
            timeCounter = null;
        }
    }


    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
        RxUtils.unsubscribeIfNotNull(mSubscription);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // TODO: Reset the timer to keep screen awake
        launchSleepTimer();
        return super.dispatchTouchEvent(event);
    }

    private void checkIsShortcutCameraExists() {
        //It will refill global camera list in isUserLogged()
        if (MainActivity.isUserLogged(this)) {
            String username;
            username = AppData.defaultUser.getUsername();
            if (AppData.evercamCameraList.size() > 0) {
                boolean cameraIsAccessible = false;
                for (EvercamCamera camera : AppData.evercamCameraList) {
                    if (camera.getCameraId().equals(startingCameraID)) {
                        cameraIsAccessible = true;
                        break;
                    }
                }

                if (!cameraIsAccessible) {
                    EvercamAccount evercamAccount = new EvercamAccount(this);
                    AppUser matchedUser = null;

                    ArrayList<AppUser> userList = evercamAccount.retrieveUserList();
                    for (AppUser appUser : userList) {
                        if (!appUser.getUsername().equals(username)) {
                            ArrayList<EvercamCamera> cameraList = new DbCamera(this).getCamerasByOwner(appUser.getUsername(), 500);
                            for (EvercamCamera camera : cameraList) {
                                if (camera.getCameraId().equals(startingCameraID)) {
                                    matchedUser = appUser;
                                    break;
                                }
                            }
                        }
                    }

                    if (matchedUser != null) {
                        CustomToast.showInCenterLong(this, getString(R.string.msg_switch_account)
                                + " - " + matchedUser.getUsername());
                        evercamAccount.updateDefaultUser(matchedUser.getEmail());
                        checkIsShortcutCameraExists();
                    } else {
                        CustomToast.showInCenterLong(this, getString(R
                                .string.msg_can_not_access_camera) + " - " + username);

                        navigateBackToCameraList();
                    }
                } else {

                }
            }
        } else {
            //If no account signed in
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void launchSleepTimer() {
        try {
            if (timerThread != null) {
                timerThread = null;
                timerHandler.removeCallbacks(timerRunnable);
            }

            final int sleepTime = getSleepTime();
            if (sleepTime != 0) {
                timerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        VideoActivity.this.getWindow().clearFlags(WindowManager.LayoutParams
                                .FLAG_KEEP_SCREEN_ON);
                    }
                };
                timerThread = new Thread() {
                    @Override
                    public void run() {
                        timerHandler.postDelayed(timerRunnable, sleepTime);
                    }
                };
                timerThread.start();
            }
        } catch (Exception e) {
            // Catch this exception and send by Google Analytics
            // This should not influence user using the app
            EvercamPlayApplication.sendCaughtException(this, e);
        }
    }

    private void initAnalyticsObjects() {
    }

    private int getSleepTime() {
        final String VALUE_NEVER = "0";

        String valueString = PrefsManager.getSleepTimeValue(this);
        if (!valueString.equals(VALUE_NEVER)) {
            return Integer.valueOf(valueString) * 1000;
        } else {
            return 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_video, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem shortcutItem = menu.findItem(R.id.video_menu_create_shortcut);
        MenuItem sharingItem = menu.findItem(R.id.video_menu_share);
//        MenuItem removeItem = menu.findItem(R.id.video_menu_remove_camera);

        if (evercamCamera != null) {
            //Only show the shortcut menu if camera is online
            shortcutItem.setVisible(evercamCamera.isOnline());

            //Only show the sharing menu if the user has full rights
            Right right = new Right(evercamCamera.getRights());
            sharingItem.setVisible(right.isFullRight());

            //Only show item 'Remove Camera' when it's a shared camera
//            removeItem.setVisible(!evercamCamera.isOwned());
        } else {
            Log.e(TAG, "EvercamCamera is null");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        try {
            if (itemId == R.id.video_menu_camera_settings) {
                editStarted = true;
                Intent viewIntent = new Intent(VideoActivity.this, ViewCameraActivity.class);
                startActivityForResult(viewIntent, Constants.REQUEST_CODE_VIEW_CAMERA);
            } else if (itemId == android.R.id.home) {
                navigateBackToCameraList();
            } else if (itemId == R.id.video_menu_share) {
                sharingStarted = true;
                Intent shareIntent = new Intent(VideoActivity.this, SharingActivity.class);
                startActivityForResult(shareIntent, Constants.REQUEST_CODE_SHARE);
            } else if (itemId == R.id.video_menu_view_snapshots) {
                SnapshotManager.showSnapshotsForCamera(this, evercamCamera.getCameraId());
            } else if (itemId == R.id.video_menu_create_shortcut) {
                if (evercamCamera != null) {

                    //Calling firebase analytics
                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                    Bundle bundle = new Bundle();
                    bundle.putString("Evercam_ShortcutCreation", "Home shortcut created successfully");
                    mFirebaseAnalytics.logEvent("Home_Shortcut", bundle);

                    Bitmap bitmap = getBitmapFromImageView(imageView);
                    HomeShortcut.create(getApplicationContext(), evercamCamera, bitmap);
                    CustomSnackbar.showShort(this, R.string.msg_shortcut_created);
                    /*
                    EvercamPlayApplication.sendEventAnalytics(this, R.string.category_shortcut, R.string.action_shortcut_create, R.string.label_shortcut_create);
*/

                    getMixpanel().sendEvent(R.string.mixpanel_event_create_shortcut, new
                            JSONObject().put("Camera ID", evercamCamera.getCameraId()));
                }
            } else if (itemId == R.id.video_menu_view_recordings) {
                if (evercamCamera != null) {
                    recordingsStarted = true;
                    Intent recordingIntent = new Intent(this, RecordingWebActivity.class);
                    recordingIntent.putExtra(Constants.BUNDLE_KEY_CAMERA_ID, evercamCamera.getCameraId());

                    startActivityForResult(recordingIntent, Constants.REQUEST_CODE_RECORDING);
                }
            }
//            else if (itemId == R.id.video_menu_remove_camera) {
//                if (evercamCamera != null) {
//                    CustomedDialog.getConfirmDialog(VideoActivity.this, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            new DeleteCameraTask(evercamCamera.getCameraId(), VideoActivity.this,
//                                    EnumConstants.DeleteType.DELETE_SHARE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                        }
//                    }, R.string.msg_confirm_remove_camera, R.string.remove).show();
//                }
//            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        navigateBackToCameraList();
    }

    /************************
     * ExoPlayer2.Listener
     ************************/
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_READY:
                onVideoLoaded();
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        Log.d(TAG, "onStateChanged: " + text);
    }


    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthAspectRatio) {
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    /*************************************
     * TextureView.SurfaceTextureListener
     ************************************/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface = new Surface(surface);
        if (player != null) {
            player.setVideoSurface(new Surface(surface));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (player != null) {
//            player.blockingClearSurface();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //Do nothing.
    }

    /************************
     * Private Methods
     ************************/

    private void navigateBackToCameraList() {
        if (CamerasActivity.activity == null) {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(upIntent)
                        .startActivities();
            }
        }

        finish();
    }

    private void readShortcutCameraId() {
        Intent liveViewIntent = this.getIntent();
        if (liveViewIntent != null && liveViewIntent.getExtras() != null) {

            //Calling firebase analytics
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString("Evercam_Shortcut_Used", "Use shortcut from Home");
            mFirebaseAnalytics.logEvent("Home_Shortcut", bundle);
/*
            EvercamPlayApplication.sendEventAnalytics(this, R.string.category_shortcut,
                    R.string.action_shortcut_use, R.string.label_shortcut_use);
            */

            try {
                if (evercamCamera != null) {
                    getMixpanel().sendEvent(R.string.mixpanel_event_use_shortcut, new JSONObject().put("Camera ID", evercamCamera.getCameraId()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            liveViewCameraId = liveViewIntent.getExtras().getString(HomeShortcut.KEY_CAMERA_ID, "");
        }
    }

    private void startPlay() {
        paused = false;
        end = false;

        checkNetworkStatus();

        loadCamerasToActionBar();
    }

    public static boolean startPlayingVideoForCamera(Activity activity, String cameraId) {
        startingCameraID = cameraId;
        Intent intent = new Intent(activity, VideoActivity.class);

        activity.startActivityForResult(intent, Constants.REQUEST_CODE_DELETE_CAMERA);

        return false;
    }

    private void setCameraForPlaying(EvercamCamera evercamCamera) {
        VideoActivity.evercamCamera = evercamCamera;

        showJpgView = false;

        optionsActivityStarted = false;

        showAllControlMenus(false);

        paused = false;
        end = false;

        showVideoView(false);
        showImageView(true);
        showProgressView(true);

        loadImageThumbnail(VideoActivity.evercamCamera);

        showProgressView(true);
    }

    public void loadImageThumbnail(EvercamCamera camera) {
        imageView.setImageDrawable(null);

        if (camera.hasThumbnailUrl()) {
            Picasso.with(this).load(camera.getThumbnailUrl())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(imageView);
        } else {
            Log.e(TAG, camera.toString());
        }
    }

    private void startMediaPlayerAnimation() {
        if (fadeInAnimation != null) {
            fadeInAnimation.cancel();
            fadeInAnimation.reset();

            clearControlMenuAnimation();
        }

        fadeInAnimation = AnimationUtils.loadAnimation(VideoActivity.this, R.anim.fadein);

        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (!paused) {
                    showAllControlMenus(false);
                } else {
                    playPauseImageView.setVisibility(View.VISIBLE);
                    if (textureView.getVisibility() != View.VISIBLE) {
                        snapshotMenuView.setVisibility(View.VISIBLE);
                    }
                }

                int orientation = VideoActivity.this.getResources().getConfiguration().orientation;
                if (!paused && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    hideToolbar();
                }
            }
        });

        playPauseImageView.startAnimation(fadeInAnimation);
        snapshotMenuView.startAnimation(fadeInAnimation);
    }

    /**
     * **********
     * Player
     * ***********
     */

    private void createPlayer(EvercamCamera camera) {
        startTime = new Date();

        if (camera.hasHlsUrl()) {
            Log.d(TAG, "HLS url: " + camera.getHlsUrl());
            preparePlayer();
        } else {
            //If no HLS URL exists, start JPG view straight away
            releasePlayer();
            showJpgView = true;
            launchJpgRunnable();
        }
    }

    private void preparePlayer() {

        if (player == null){

            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;

            @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                    ((EvercamPlayApplication) getApplication()).useExtensionRenderers()
                            ? (false ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;

            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);

            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                    drmSessionManager, extensionRendererMode);
            player.addListener(this);

            eventLogger = new EventLogger(trackSelector);
            eventLogger.addListener(this);
            player.addListener(eventLogger);
//        player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setMetadataOutput(eventLogger);

            Uri hlsUrl = Uri.parse(evercamCamera.getHlsUrl());

            MediaSource mediaSource = new HlsMediaSource(hlsUrl, mediaDataSourceFactory, mainHandler, eventLogger);


            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.prepare(mediaSource, !haveResumePosition, false);

            player.setPlayWhenReady(true);
            player.setVideoSurface(surface);

        }else{
            releasePlayer();
            preparePlayer();
        }


        /*DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;

        @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                ((EvercamPlayApplication) getApplication()).useExtensionRenderers()
                        ? (false ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                        : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                        : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                drmSessionManager, extensionRendererMode);
        player.addListener(this);
        eventLogger = new EventLogger(trackSelector);
        eventLogger.addListener(this);
        player.addListener(eventLogger);
//        player.setAudioDebugListener(eventLogger);
        player.setVideoDebugListener(eventLogger);
        player.setMetadataOutput(eventLogger);
        Uri hlsUrl = Uri.parse(evercamCamera.getHlsUrl());
        MediaSource mediaSource = new HlsMediaSource(hlsUrl, mediaDataSourceFactory, mainHandler, eventLogger);

        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
        player.setVideoSurface(surface);*/
    }

    private void releasePlayer() {
        if (player != null) {
            Log.e("EXOPlayer","EXO_PLAYER_RELEASED");
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            eventLogger = null;
        }
    }


    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    private void resumePlayer() {
        /**
         * Restart the player for replay instead of calling setPlayWhenReady(true)
         * to make sure the resumed video is up to date.
         */
        preparePlayer();
    }

    // when screen gets rotated
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            showToolbar();
            adoptSwipeListenerToLandscape(false);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                    .LayoutParams.FLAG_FULLSCREEN);

            setGradientTitleBackground();
            adoptSwipeListenerToLandscape(true);
            if (!paused && !end && !isProgressViewVisible()) hideToolbar();
            else showToolbar();
        }

        this.invalidateOptionsMenu();
    }

    private void adoptSwipeListenerToLandscape(boolean adoptLandscape) {
        swipeTouchListener.isLandscape(adoptLandscape);
    }

    private boolean isProgressViewVisible() {
        return progressView.getVisibility() == View.VISIBLE;
    }

    private void setDisplayOrientation() {
        /** Force landscape if it's enabled in settings */
        boolean forceLandscape = PrefsManager.isForceLandscape(this);
        if (forceLandscape) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                    .LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void checkNetworkStatus() {
        if (!Commons.isOnline(this)) {
            CustomedDialog.getNoInternetDialog(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    paused = true;
                    dialog.dismiss();
                    showProgressView(false);
                }
            }).show();
        }
    }

    private void initialPageElements() {
        imageViewLayout = (RelativeLayout) this.findViewById(R.id.camera_view_layout);
        imageView = (ImageView) this.findViewById(R.id.jpg_image_view);
        playPauseImageView = (ImageView) this.findViewById(R.id.play_pause_image_view);
        snapshotMenuView = (ImageView) this.findViewById(R.id.player_savesnapshot);

        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        textureView = (TextureView) findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);

        progressView = ((ProgressView) imageViewLayout.findViewById(R.id.live_progress_view));

        progressView.setMinimumWidth(playPauseImageView.getWidth());
        progressView.setMinimumHeight(playPauseImageView.getHeight());
        progressView.canvasColor = Color.TRANSPARENT;

        progressView.setVisibility(View.VISIBLE);

        offlineTextLayout = (OfflineLayoutView) findViewById(R.id.offline_view_layout);
        timeCountTextView = (TextView) findViewById(R.id.time_text_view);

        ImageView ptzLeftImageView = (ImageView) findViewById(R.id.arrow_left);
        ImageView ptzRightImageView = (ImageView) findViewById(R.id.arrow_right);
        ImageView ptzUpImageView = (ImageView) findViewById(R.id.arrow_up);
        ImageView ptzDownImageView = (ImageView) findViewById(R.id.arrow_down);
        ImageView ptzHomeImageView = (ImageView) findViewById(R.id.ptz_home);
        ImageView ptzZoomInImageView = (ImageView) findViewById(R.id.zoom_in_image_view);
        ImageView ptzZoomOutImageView = (ImageView) findViewById(R.id.zoom_out_image_view);
        ImageView presetsImageView = (ImageView) findViewById(R.id.presets_image_view);

        ptzZoomLayout = (RelativeLayout) findViewById(R.id.ptz_zoom_control_layout);
        ptzMoveLayout = (RelativeLayout) findViewById(R.id.ptz_move_control_layout);

        offlineTextLayout.setOnRefreshClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                offlineTextLayout.startProgress();
                mSubscription = getRefreshOfflineObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getRefreshOfflineObserver());
            }
        });

        /** The click listeners for PTZ control - move, zoom and preset */
        ptzLeftImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PTZMoveTask.launch(new PTZRelativeBuilder(evercamCamera.getCameraId()).left(4)
                        .build());
            }
        });
        ptzRightImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PTZMoveTask.launch(new PTZRelativeBuilder(evercamCamera.getCameraId()).right(4)
                        .build());
            }
        });
        ptzUpImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PTZMoveTask.launch(new PTZRelativeBuilder(evercamCamera.getCameraId()).up(3)
                        .build());
            }
        });
        ptzDownImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PTZMoveTask.launch(new PTZRelativeBuilder(evercamCamera.getCameraId()).down(3)
                        .build());
            }
        });
        ptzHomeImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PTZMoveTask.launch(new PTZHome(evercamCamera.getCameraId()));
            }
        });
        ptzZoomInImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PTZMoveTask.launch(new PTZRelativeBuilder(evercamCamera.getCameraId()).zoom(1)
                        .build());
            }
        });
        ptzZoomOutImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PTZMoveTask.launch(new PTZRelativeBuilder(evercamCamera.getCameraId()).zoom(-1)
                        .build());
            }
        });
        presetsImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog listDialog = new AlertDialog.Builder(VideoActivity.this)
                        .setNegativeButton(R.string.cancel, null).create();
                LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
                final View view = mInflater.inflate(R.layout.dialog_preset_list, null);
                ListView listView = (ListView) view.findViewById(R.id.presets_list_view);
                View header = getLayoutInflater().inflate(R.layout.header_preset_list, null);
                listView.addHeaderView(header);
                listDialog.setView(view);
                listView.setAdapter(new PresetsListAdapter(getApplicationContext(), R.layout
                        .item_preset_list, presetList));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        if (position == 0) //Header clicked - Create preset
                        {
                            CustomedDialog.getCreatePresetDialog(VideoActivity.this,
                                    evercamCamera.getCameraId()).show();
                        } else {
                            PTZPreset preset = presetList.get(position - 1);
                            PTZMoveTask.launch(new PTZPresetControl(evercamCamera.getCameraId(),
                                    preset.getToken()));
                        }

                        listDialog.cancel();
                    }
                });

                listDialog.show();
            }
        });

        /** The click listener for pause/play button */
        playPauseImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (end) {
                    Toast.makeText(VideoActivity.this, R.string.msg_try_again,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isProgressViewVisible()) return;
                if (paused) // video is currently paused. Now we need to
                // resume it.
                {
                    timeCountTextView.setVisibility(View.VISIBLE);
                    showProgressView(true);

                    playPauseImageView.setImageBitmap(null);
                    showAllControlMenus(true);
                    playPauseImageView.setImageResource(R.drawable.ic_pause);

                    startMediaPlayerAnimation();

                    //If playing url is not null, resume HLS stream
                    if (evercamCamera != null && evercamCamera.hasHlsUrl()) {
                        resumePlayer();
                    }
                    //Otherwise restart jpg view
                    else {
                        showJpgView = true;
                        loadJpgView();
                    }
                    paused = false;
                } else
                // video is currently playing. Now we need to pause video
                {
                    timeCountTextView.setVisibility(View.INVISIBLE);
                    clearControlMenuAnimation();
                    if (fadeInAnimation != null && fadeInAnimation.hasStarted() &&
                            !fadeInAnimation.hasEnded()) {
                        fadeInAnimation.cancel();
                        fadeInAnimation.reset();
                    }
                    showAllControlMenus(true);

                    playPauseImageView.setImageBitmap(null);
                    playPauseImageView.setImageResource(R.drawable.ic_play);

                    pausePlayer();

                    paused = true; // mark the images as paused. Do not stop
                    // threads, but do not show the images
                    // showing up

                    disconnectJpgView();
                }
            }
        });

        /**
         * Moved live view click listener to gesture single click listener
         *
         * The click listener of camera live view layout, including both stream and JPG view
         *  Once clicked, if camera view is playing, show the pause menu, otherwise do nothing.
         *
         *  OnSwipeTouchListener will also handle the pinch zoom & swipe for JPG/HLS live view
         */
        swipeTouchListener = new OnSwipeTouchListener(this) {

            @Override
            public void onClick() {
                if (end) {
                    Toast.makeText(VideoActivity.this, R.string.msg_try_again,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isProgressViewVisible()) return;

                if (!paused && !end && offlineTextLayout.getVisibility() != View.VISIBLE) // video is currently playing. Show pause button
                {
                    if (playPauseImageView.getVisibility() == View.VISIBLE) {
                        showAllControlMenus(false);
                        clearControlMenuAnimation();
                        fadeInAnimation.reset();
                    } else {
                        showToolbar();
                        playPauseImageView.setImageResource(R.drawable.ic_pause);

                        showAllControlMenus(true);

                        startMediaPlayerAnimation();
                    }
                }
            }
        };
        videoFrame.setOnTouchListener(swipeTouchListener);
        imageView.setOnTouchListener(swipeTouchListener);

        snapshotMenuView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide pause/snapshot menu if the live view is not paused
                if (!paused) {
                    showAllControlMenus(false);
                    clearControlMenuAnimation();
                    fadeInAnimation.reset();
                }

                if (imageView.getVisibility() == View.VISIBLE) {
                    Bitmap bitmap = getBitmapFromImageView(imageView);

                    processSnapshot(bitmap, FileType.JPG);
                } else if (textureView.getVisibility() == View.VISIBLE) {
                    Bitmap bitmap = textureView.getBitmap();
                    processSnapshot(bitmap, FileType.JPG);
                }
            }
        });
    }

    /**
     * Observable and Observer for refreshing offline camera using RxJava
     */

    private Observable<Bitmap> getRefreshOfflineObservable() {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                try {
                    subscriber.onNext(reloadSnaopshot(evercamCamera.getCameraId()));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private Observer<Bitmap> getRefreshOfflineObserver() {

        return new Observer<Bitmap>() {
            Bitmap bitmap = null;

            @Override
            public void onCompleted() {
                Log.d(TAG, "On complete");
                offlineTextLayout.stopProgress();

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    evercamCamera.setIsOnline(true);
                    new DbCamera(getApplicationContext()).updateCamera(evercamCamera);
                    CamerasActivity.reloadFromDatabase = true;

                    startPlay();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
                offlineTextLayout.stopProgress();
                CustomToast.showInCenter(getApplicationContext(), e.getMessage());
            }

            @Override
            public void onNext(Bitmap bitmap) {
                Log.d(TAG, "On next");
                this.bitmap = bitmap;
            }
        };
    }

    private Bitmap reloadSnaopshot(String cameraId) throws EvercamException {
        Snapshot snapshot = Snapshot.record(cameraId, "Android client");
        return BitmapFactory.decodeByteArray(snapshot.getData(), 0, snapshot.getData().length);
    }

    public void setTempSnapshotBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    private Bitmap getBitmapFromImageView(ImageView imageView) {
        Bitmap bitmap = null;
        if (imageView.getDrawable() instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                drawable.draw(canvas);
            }
        }
        return bitmap;
    }

    void showProgressView(boolean show) {
        if (show) {
            progressView.canvasColor = Color.TRANSPARENT;
            progressView.setVisibility(View.VISIBLE);
        } else {
            imageViewLayout.findViewById(R.id.live_progress_view).setVisibility(View.GONE);
        }
    }

    private void launchJpgRunnable() {
        Log.d("CameraId",evercamCamera.getCameraId());
        mLiveViewRunnable = new LiveViewRunnable(this, evercamCamera.getCameraId());
        loadJpgView();
    }

    private void loadJpgView() {
        if (mLiveViewRunnable != null) {
            new Thread(mLiveViewRunnable).start();
        }
    }

    private void disconnectJpgView() {
        if (mLiveViewRunnable != null) {
            mLiveViewRunnable.disconnect();
        }
    }

    private void startTimeCounter() {
        if (timeCounter == null) {
            String timezone = "Etc/UTC";
            if (evercamCamera != null) {
                timezone = evercamCamera.getTimezone();
            }
            timeCounter = new TimeCounter(this, timezone);
        }
        if (!timeCounter.isStarted()) {
            timeCounter.start();
        }
    }

    private void processSnapshot(Bitmap btm, SnapshotManager.FileType type) {
        final Bitmap bitmap = btm;
        final SnapshotManager.FileType fileType = type;

        if (bitmap != null) {
            CustomedDialog.getConfirmSnapshotDialog(VideoActivity.this, bitmap,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(new CaptureSnapshotRunnable(VideoActivity
                                    .this, evercamCamera.getCameraId(), fileType, bitmap)).start();
                        }
                    }).show();
        }
    }

    // Handle stream loaded
    private void onVideoLoaded() {
        Log.d(TAG, "onVideoLoaded()");
        runOnUiThread(new Runnable() {
            public void run() {
                //View gets played, show time count, and start buffering
                showProgressView(false);
                showVideoView(true);
                showImageView(false);
                startTimeCounter();

                //Calling firebase analytics
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(VideoActivity.this);
                Bundle bundle = new Bundle();
                bundle.putString("RTSP_Stream_Played", "Successfully played RTSP stream");
                mFirebaseAnalytics.logEvent("RTSP_Streaming", bundle);

                /*
                //And send to Google Analytics
                EvercamPlayApplication.sendEventAnalytics(VideoActivity.this,
                        R.string.category_streaming_rtsp,
                        R.string.action_streaming_rtsp_success,
                        R.string.label_streaming_rtsp_success);
                */

                StreamFeedbackItem successItem = new StreamFeedbackItem(VideoActivity
                        .this, AppData.defaultUser.getUsername(), true);
                successItem.setCameraId(evercamCamera.getCameraId());
                successItem.setUrl(evercamCamera.getHlsUrl());
                successItem.setType(StreamFeedbackItem.TYPE_HLS);
                if (startTime != null) {
                    float timeDifferenceFloat = Commons.calculateTimeDifferenceFrom
                            (startTime);
                    Log.d(TAG, "Time difference: " + timeDifferenceFloat + " seconds");
                    successItem.setLoadTime(timeDifferenceFloat);
                    startTime = null;
                }

            }
        });
    }

    // Handle stream loading failed
    private void onVideoLoadFailed() {
        Log.d(TAG, "onVideoLoadFailed()");
        runOnUiThread(new Runnable() {
            public void run() {

                //Calling firebase analytics
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(VideoActivity.this);
                Bundle bundle = new Bundle();
                bundle.putString("RTSP_Stream_Failed", "Failed to play RTSP stream while the camera is online and has a valid RTSP URL.");
                mFirebaseAnalytics.logEvent("RTSP_Streaming", bundle);
/*
                EvercamPlayApplication.sendEventAnalytics(VideoActivity.this, R.string
                        .category_streaming_rtsp, R.string.action_streaming_rtsp_failed, R.string
                        .label_streaming_rtsp_failed);
                */
                StreamFeedbackItem failedItem = new StreamFeedbackItem
                        (VideoActivity.this, AppData.defaultUser.getUsername(),
                                false);
                failedItem.setCameraId(evercamCamera.getCameraId());
                failedItem.setUrl(evercamCamera.getHlsUrl());
                failedItem.setType(StreamFeedbackItem.TYPE_HLS);

                CustomSnackbar.showShort(VideoActivity.this, R.string.msg_switch_to_jpg);
                releasePlayer();
                showVideoView(false);
                showImageView(true);
                showJpgView = true;
                launchJpgRunnable();
            }
        });
    }

    private String[] getCameraNameArray(ArrayList<EvercamCamera> cameraList) {
        ArrayList<String> cameraNames = new ArrayList<>();

        boolean matched = false;
        for (int count = 0; count < cameraList.size(); count++) {
            EvercamCamera camera = cameraList.get(count);

            cameraNames.add(camera.getName());
            if (cameraList.get(count).getCameraId().equals(startingCameraID)) {
                defaultCameraIndex = cameraNames.size() - 1;
                matched = true;
            }
        }
        if (!matched && cameraExistsInListButOffline(startingCameraID)) {
            CustomedDialog.showMessageDialog(this, R.string.msg_camera_is_hidden);
        }

        String[] cameraNameArray = new String[cameraNames.size()];
        cameraNames.toArray(cameraNameArray);

        return cameraNameArray;
    }

    private boolean cameraExistsInListButOffline(String cameraId) {
        for (EvercamCamera camera : AppData.evercamCameraList) {
            if (camera.getCameraId().equals(cameraId) && !camera.isOnline()) {
                return true;
            }
        }
        return false;
    }

    private void loadCamerasToActionBar() {
        String[] cameraNames;

        final ArrayList<EvercamCamera> onlineCameraList = new ArrayList<>();
        final ArrayList<EvercamCamera> cameraList;

        //If is not showing offline cameras, the offline cameras should be excluded from list
        if (PrefsManager.showOfflineCameras(VideoActivity.this)) {
            cameraList = AppData.evercamCameraList;
        } else {
            for (EvercamCamera evercamCamera : AppData.evercamCameraList) {
                if (evercamCamera.isOnline()) {
                    onlineCameraList.add(evercamCamera);
                }
            }

            cameraList = onlineCameraList;
        }

        cameraNames = getCameraNameArray(cameraList);

        CameraListAdapter adapter = new CameraListAdapter(VideoActivity.this,
                R.layout.item_spinner_live_view, R.id.spinner_camera_name_text, cameraNames, cameraList);
        mCameraListSpinner.setAdapter(adapter);
        mCameraListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Stop time counter when another camera selected
                if (timeCounter != null) {
                    timeCounter.stop();
                    timeCounter = null;
                }

                if (showJpgView) {
                    disconnectJpgView();
                    showJpgView = false;
                }

                evercamCamera = cameraList.get(position);


                startingCameraID = evercamCamera.getCameraId();

                //Hide the PTZ control panel when switch to another camera
                showPtzControl(false);

                if (!evercamCamera.isOnline()) {
                    // If camera is offline, show offline msg and stop video
                    // playing.
                    offlineTextLayout.show();
                    progressView.setVisibility(View.GONE);

                    // Hide video elements if switch to an offline camera.
                    showVideoView(false);
                    showImageView(false);
                } else {
                    offlineTextLayout.hide();

                    setCameraForPlaying(cameraList.get(position));
                    createPlayer(evercamCamera);

                    if (evercamCamera.hasModel()) {
                        new CheckOnvifTask(VideoActivity.this, evercamCamera)
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mCameraListSpinner.setSelection(defaultCameraIndex);
    }

    private void showImageView(boolean show) {
        imageView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showVideoView(boolean show) {
        videoFrame.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showAllControlMenus(boolean show) {
        playPauseImageView.setVisibility(show ? View.VISIBLE : View.GONE);
        snapshotMenuView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void clearControlMenuAnimation() {
        snapshotMenuView.clearAnimation();
        playPauseImageView.clearAnimation();
    }

    public void showPtzControl(boolean show) {
        ptzMoveLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        ptzZoomLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void updateImage(Bitmap bitmap, String cameraId) {
        if (cameraId.equals(evercamCamera.getCameraId())) {
            if (!paused && !end && showJpgView) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    //TODO: If failed to load JPG view, how to handle it?
    public void onFirstJpgLoaded() {
        showProgressView(false);

        startTimeCounter();

        //Calling firebase analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(VideoActivity.this);
        Bundle bundle = new Bundle();
        bundle.putString("JPG_Stream_Played", "Successfully played JPG stream");
        mFirebaseAnalytics.logEvent("JPG_Streaming", bundle);
/*
        EvercamPlayApplication.sendEventAnalytics(VideoActivity.this,
                R.string.category_streaming_jpg,
                R.string.action_streaming_jpg_success,
                R.string.label_streaming_jpg_success);
        */
        StreamFeedbackItem successItem = new StreamFeedbackItem
                (VideoActivity.this, AppData.defaultUser.getUsername
                        (), true);
        successItem.setCameraId(evercamCamera.getCameraId());
        successItem.setType(StreamFeedbackItem.TYPE_JPG);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        String errorString = null;
        if (error.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = error.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            Log.e(TAG, errorString);
        }

        if (isBehindLiveWindow(error)) {
            clearResumePosition();
            preparePlayer();
        } else {
            Log.e("VIDEO FAILED","VIDEO FAILED LOADING NEW ONE.");
            updateResumePosition();
            onVideoLoadFailed();
        }



/*        Log.e(TAG, "onError");
        onVideoLoadFailed();
        playerNeedsPrepare = true;*/

    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {

        return ((EvercamPlayApplication) getApplication()).buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }
}