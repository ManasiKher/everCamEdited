package io.evercam.androidapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.evercam.androidapp.addeditcamera.AddCameraActivity;
import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.AccountNavAdapter;
import io.evercam.androidapp.custom.CameraLayout;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomSnackbar;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.dto.ImageLoadingStatus;
import io.evercam.androidapp.feedback.LoadTimeFeedbackItem;
import io.evercam.androidapp.publiccameras.PublicCamerasWebActivity;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.tasks.CheckKeyExpirationTask;
import io.evercam.androidapp.tasks.LoadCameraListTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;
import com.squareup.picasso.Picasso;
import io.intercom.android.sdk.Intercom;
//import io.intercom.com.squareup.picasso.Picasso;
import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class CamerasActivity extends ParentAppCompatActivity implements
        ObservableScrollViewCallbacks, OnClickListener {
    public static CamerasActivity activity = null;
    public MenuItem refresh;

    private static final String TAG = "CamerasActivity";

    public static int camerasPerRow = 1;
    public boolean reloadCameraList = false;
    public static boolean reloadFromDatabase = false;

    public CustomProgressDialog reloadProgressDialog;
    private RelativeLayout actionButtonLayout;
//    private FloatingActionButton manuallyAddButton;
    private FloatingActionButton scanButton;
    private int lastScrollY;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private FrameLayout mNavSettingsItemLayout;
    private FrameLayout mNavFeedbackItemLayout;
    private FrameLayout mNavScanLayout;
    private FrameLayout mNavExploreLayout;
    private FrameLayout mNavTitleLayout;
    private ScrollView mNavBodyScrollView;
    private FrameLayout mNavBodyAccountView;
    private TextView mUserNameTextView;
    private TextView mUserEmailTextView;
    private CircleImageView mCircleImageView;
    private ImageView mTriangleImageView;
    private FrameLayout mNavAddAccountLayout;
    private FrameLayout mNavManageAccountLayout;
    private ListView mAccountListView;
    ;
    // The list copy in nav drawer that excludes default user
    private ArrayList<AppUser> mUserListInNavDrawer;
    private boolean mIsDrawerUpdated = false;

    /**
     * For user data collection, calculate how long it takes to load camera list
     */
    private Date startTime;
    private float databaseLoadTime = 0;

    private enum InternetCheckType {
        START, RESTART
    }

    private String usernameOnStop = "";
    private boolean showOfflineOnStop;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.navigation_drawer_layout);

        checkUser();

        setUpGradientToolbarWithHomeButton();
        initNavigationDrawer();

        ObservableScrollView observableScrollView = (ObservableScrollView) findViewById(R.id.cameras_scroll_view);
        observableScrollView.setScrollViewCallbacks(this);

//        setUpActionButtons();

        initDataCollectionObjects();

        activity = this;

        /**
         * Use Handler here because we want the title bar/menu get loaded first.
         * When the app starts, it will load cameras to grid view twice:
         * 1. Load cameras that saved locally without image (disabled load image from cache
         * because it blocks UI.)
         * 2. When camera list returned from Evercam, show them on screen with thumbnails,
         * then request for snapshots in background separately.
         *
         * TODO: Check is it really necessary to keep the post delay handler here
         * See if refresh icon stop animating or not.
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 * Sometimes Evercam returns the list less than 0.1 sec?
                 * so check it's returned or not before
                 * the first load to avoid loading it twice.
                 */
                io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom.FlowLayout) CamerasActivity.this.findViewById(R.id.cameras_flow_layout);
                if (!(camsLineView.getChildCount() > 0)) {
                    addAllCameraViews(false, false);
                    if (camsLineView.getChildCount() > 0 && databaseLoadTime == 0 && startTime != null) {
                        databaseLoadTime = Commons.calculateTimeDifferenceFrom(startTime);
                    }
                }
            }
        }, 1);

        // Start loading camera list after menu created(because need the menu
        // showing as animation)
        new CamerasCheckInternetTask(CamerasActivity.this, InternetCheckType.START).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // draw the options defined in the following file
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_camera_list, menu);

        refresh = menu.findItem(R.id.menurefresh);
        refresh.setActionView(R.layout.partial_actionbar_progress);

        return true;
    }

    // Tells that the item has been selected from the menu. Now check and get
    // the selected item and perform the relevant action
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menurefresh) {

            //Calling firebase analytics
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString("Refresh", "Refresh Camera List");
            mFirebaseAnalytics.logEvent("Menu", bundle);
/*
            EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu, R.string.action_refresh, R.string.label_list_refresh);
*/
            if (refresh != null) refresh.setActionView(R.layout.partial_actionbar_progress);

            startCameraLoadingTask();

        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onRestart() {
        super.onRestart();

        if (MainActivity.isUserLogged(this)) {
            //Reload camera list if default user has been changed, or offline settings has been changed
            if (isUserChanged() || isOfflineSettingChanged()) {
                new CamerasCheckInternetTask(CamerasActivity.this, InternetCheckType.START).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                try {
                    new CamerasCheckInternetTask(CamerasActivity.this, InternetCheckType.RESTART).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } catch (RejectedExecutionException e) {
                    EvercamPlayApplication.sendCaughtExceptionNotImportant(activity, e);
                }
            }
            usernameOnStop = "";
        } else {
            startActivity(new Intent(this, OnBoardingActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reloadFromDatabase) {
            removeAllCameraViews();
            addAllCameraViews(true, true);
            reloadFromDatabase = false;
        }
        Intercom.client().handlePushMessage();
    }

    private boolean isUserChanged() {
        String restartedUsername = AppData.defaultUser.getUsername();
        return !usernameOnStop.isEmpty() && !usernameOnStop.equals(restartedUsername);
    }

    private boolean isOfflineSettingChanged() {
        return showOfflineOnStop != PrefsManager.showOfflineCameras(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_ADD_CAMERA) {
            reloadCameraList = (resultCode == Constants.RESULT_TRUE);
        } else if (requestCode == Constants.REQUEST_CODE_DELETE_CAMERA) {
            // Don't reset reload variable to false because it's possible set to TRUE when
            // return from shortcut live view
            if (resultCode == Constants.RESULT_TRUE || resultCode == Constants.RESULT_DELETED) {
                reloadCameraList = true;
                CustomSnackbar.showLong(activity, R.string.msg_delete_success);
            }
        } else if (requestCode == Constants.REQUEST_CODE_MANAGE_ACCOUNT) {
            reloadCameraList = (resultCode == Constants.RESULT_ACCOUNT_CHANGED);
        } else if (requestCode == Constants.REQUEST_CODE_SHOW_GUIDE && resultCode == Constants.RESULT_TRUE) {
            showShowcaseView(onlyHasDemoCamera());
        }

        if (resultCode == Constants.RESULT_TRANSFERRED) {
            reloadCameraList = true;
            CustomSnackbar.showLong(this, R.string.msg_transfer_success);
        } else if (resultCode == Constants.RESULT_ACCESS_REMOVED) {
            reloadCameraList = true;
            CustomSnackbar.showShort(this, R.string.msg_share_updated);
        } else if (resultCode == Constants.RESULT_NO_ACCESS) {
            reloadCameraList = true;
            CustomSnackbar.showLong(this, R.string.msg_no_access);
        }
    }

    private void startLoadingCameras() {
        reloadProgressDialog = new CustomProgressDialog(this);
        if (reloadCameraList) {
            reloadProgressDialog.show(getString(R.string.loading_cameras));
        }

        startCameraLoadingTask();
    }

    private void checkUser() {
        if (AppData.defaultUser == null) {
            AppData.defaultUser = new EvercamAccount(this).getDefaultUser();
        }
    }

    private void startCameraLoadingTask() {
        if (Commons.isOnline(this)) {
            LoadCameraListTask loadTask = new LoadCameraListTask(AppData.defaultUser,
                    CamerasActivity.this);
            loadTask.reload = true; // be default do not refresh until there
            // is
            // any change in cameras in database
            loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            CustomedDialog.showInternetNotConnectDialog(CamerasActivity.this);
        }
    }

    // Stop All Camera Views
    public void stopAllCameraViews() {
        io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                .FlowLayout) this.findViewById(R.id.cameras_flow_layout);
        for (int count = 0; count < camsLineView.getChildCount(); count++) {
            LinearLayout linearLayout = (LinearLayout) camsLineView.getChildAt(count);
            CameraLayout cameraLayout = (CameraLayout) linearLayout.getChildAt(0);
            cameraLayout.stopAllActivity();
        }
    }

    private void updateNavDrawerUserInfo() {
        AppUser defaultUser = AppData.defaultUser;
        if (defaultUser != null) {
            String gravatarUrl = Commons.getGravatarUrl(defaultUser.getEmail());
            mUserNameTextView.setText(defaultUser.getFirstName() + " " + defaultUser.getLastName());
            mUserEmailTextView.setText(defaultUser.getEmail());
            Picasso.with(this).load(gravatarUrl)
                    .noFade()
                    .placeholder(R.drawable.ic_profile)
                    .into(mCircleImageView);
        }

        updateUserListInNavDrawer();
        bindAccountList(mUserListInNavDrawer);
    }

    private void initNavigationDrawer() {
        mNavSettingsItemLayout = (FrameLayout) findViewById(R.id.navigation_drawer_items_settings_layout);
        mNavFeedbackItemLayout = (FrameLayout) findViewById(R.id.navigation_drawer_items_feedback_layout);
//        mNavScanLayout = (FrameLayout) findViewById(R.id.navigation_drawer_items_scan_layout);
//        mNavExploreLayout = (FrameLayout) findViewById(R.id.navigation_drawer_items_explore_layout);
        mNavTitleLayout = (FrameLayout) findViewById(R.id.navigation_drawer_title_layout);
        mNavBodyScrollView = (ScrollView) findViewById(R.id.drawer_body_scroll_view);
        mNavBodyAccountView = (FrameLayout) findViewById(R.id.drawer_body_account_view);
        mNavAddAccountLayout = (FrameLayout) findViewById(R.id.drawer_account_items_add_layout);
        mNavManageAccountLayout = (FrameLayout) findViewById(R.id.drawer_account_items_manage_layout);

        mUserNameTextView = (TextView) findViewById(R.id.navigation_drawer_title_user_name);
        mUserEmailTextView = (TextView) findViewById(R.id.navigation_drawer_title_user_email);
        mTriangleImageView = (ImageView) findViewById(R.id.image_view_triangle);
        mCircleImageView = (CircleImageView) findViewById(R.id.navigation_drawer_account_profile_image);
        mAccountListView = (ListView) findViewById(R.id.list_view_account_email);
        /*FrameLayout offlineLayout = (FrameLayout) findViewById(R.id.navigation_drawer_items_offline_layout);
        final CheckBox offlineSwitch = (CheckBox) findViewById(R.id.checkbox_offline);
        offlineSwitch.setChecked(PrefsManager.showOfflineCameras(this));

        offlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefsManager.setShowOfflineCamera(getApplicationContext(), isChecked);
                removeAllCameraViews();
                addAllCameraViews(false, true);
            }
        });

        offlineLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                offlineSwitch.setChecked(!offlineSwitch.isChecked());
            }
        });*/

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_opened,
                R.string.navigation_drawer_closed
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Disables the burger/arrow animation by default
                super.onDrawerSlide(drawerView, 0);

                // Update user account info when it's completely open
                if (slideOffset > 0 && !mIsDrawerUpdated) {
                    // Always hide the account menu by default
                    showAccountView(false);

                    //And update account info
                    updateNavDrawerUserInfo();

                    mIsDrawerUpdated = true;
                }

                if (slideOffset == 0) {
                    mIsDrawerUpdated = false;
                }
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // Nav Drawer item click listener
        mNavSettingsItemLayout.setOnClickListener(this);
        mNavFeedbackItemLayout.setOnClickListener(this);
//        mNavScanLayout.setOnClickListener(this);
//        mNavExploreLayout.setOnClickListener(this);
        mNavAddAccountLayout.setOnClickListener(this);
        mNavManageAccountLayout.setOnClickListener(this);

        mNavTitleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTriangleImageView.getRotation() == 0) {
                    showAccountView(true);
                } else if (mTriangleImageView.getRotation() == 180) {
                    showAccountView(false);
                }
            }
        });

        updateUserListInNavDrawer();
        bindAccountList(mUserListInNavDrawer);
    }

    @Override
    public void onClick(View view) {
        //Close the navigation drawer, currently all click listeners are drawer items
        closeDrawer();

        if (view == mNavSettingsItemLayout) {

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString("Settings", "Click on setting menu");
            mFirebaseAnalytics.logEvent("Menu", bundle);
/*
            EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu, R.string.action_settings, R.string.label_settings);
*/
            startActivityForResult(new Intent(CamerasActivity.this, CameraPrefsActivity.class),
                    Constants.REQUEST_CODE_SHOW_GUIDE);
        } else if (view == mNavFeedbackItemLayout) {
            try{
                if (AppData.defaultUser.getIntercom_hmac_android() != null){
                    if (AppData.defaultUser.getIntercom_hmac_android().equals("")){
                        Toast.makeText(this, "Please sign out and login again to avail this feature.", Toast.LENGTH_SHORT).show();
                    }else{
                        Intercom.client().displayConversationsList();
                    }
                }else{
                    Toast.makeText(this, "Please sign out and login again to avail this feature.", Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e){
                Log.e(TAG, e.toString());
                EvercamPlayApplication.sendCaughtException(this, e);
            }

        } else if (view == mNavScanLayout) {
            startActivityForResult(new Intent(CamerasActivity.this, ScanActivity.class),
                    Constants.REQUEST_CODE_ADD_CAMERA);
        } else if (view == mNavExploreLayout) {
            startActivity(new Intent(CamerasActivity.this, PublicCamerasWebActivity.class));
        } else if (view == mNavAddAccountLayout) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (view == mNavManageAccountLayout) {

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString("Manage_Account", "Click on manage account");
            mFirebaseAnalytics.logEvent("Menu", bundle);
/*
            EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu, R.string.action_manage_account, R.string.label_account);
            */
            startActivityForResult(new Intent(CamerasActivity.this, ManageAccountsActivity.class), Constants.REQUEST_CODE_MANAGE_ACCOUNT);
        }
    }

    private void updateUserListInNavDrawer() {
        mUserListInNavDrawer = new ArrayList<>(new EvercamAccount(this).retrieveUserList());
        mUserListInNavDrawer.remove(AppData.defaultUser);
    }

    private void bindAccountList(ArrayList<AppUser> appUsers) {

        AccountNavAdapter accountNavAdapter = new AccountNavAdapter(this, R.layout.item_list_nav_account,
                R.id.drawer_account_user_textView, appUsers);
        mAccountListView.setAdapter(null);
        mAccountListView.setAdapter(accountNavAdapter);

        mAccountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AppUser appUser = mUserListInNavDrawer.get(position);
                Log.d(TAG, appUser.toString());
                new CheckKeyExpirationTaskNavDrawer(appUser).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setUpActionButtons() {
        actionButtonLayout = (RelativeLayout) findViewById(R.id
                .action_button_layout);
        final FloatingActionsMenu actionMenu = (FloatingActionsMenu) findViewById(R.id.add_action_menu);
//        manuallyAddButton = (FloatingActionButton) findViewById(R.id.add_action_button_manually);
        scanButton = (FloatingActionButton) findViewById(R.id.add_action_button_scan);

        actionMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu
                .OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                dimBackgroundAsAnimation(actionButtonLayout);

                actionButtonLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionMenu.collapse();
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                actionButtonLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                actionButtonLayout.setOnClickListener(null);
                actionButtonLayout.setClickable(false);
            }
        });

/*        manuallyAddButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(CamerasActivity.this);
                Bundle bundle = new Bundle();
                bundle.putString("Add_Camera", "Click on add camera in menu, and choose manually.");
                mFirebaseAnalytics.logEvent("Menu", bundle);
*//*
                EvercamPlayApplication.sendEventAnalytics(CamerasActivity.this, R.string
                        .category_menu, R.string.action_add_camera, R.string
                        .label_add_camera_manually);
*//*
                //TODO: Make the decision of using which design for adding camera
                startActivityForResult(new Intent(CamerasActivity.this, AddCameraActivity
                        .class), Constants.REQUEST_CODE_ADD_CAMERA);
//                startActivityForResult(new Intent(CamerasActivity.this, AddEditCameraActivity
//                        .class), Constants.REQUEST_CODE_ADD_CAMERA);

                actionMenu.collapse();
            }
        });*/
        scanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(CamerasActivity.this);
                Bundle bundle = new Bundle();
                bundle.putString("Add_Camera", "Click on add camera in menu, and choose scan.");
                mFirebaseAnalytics.logEvent("Menu", bundle);
/*
                EvercamPlayApplication.sendEventAnalytics(CamerasActivity.this, R.string
                        .category_menu, R.string.action_add_camera, R.string.label_add_camera_scan);
*/
                startActivityForResult(new Intent(CamerasActivity.this, ScanActivity.class),
                        Constants.REQUEST_CODE_ADD_CAMERA);

                actionMenu.collapse();
            }
        });
    }

    private void dimBackgroundAsAnimation(final View view) {
        Integer colorFrom = ContextCompat.getColor(this, android.R.color.transparent);
        Integer colorTo = ContextCompat.getColor(this, R.color.black_semi_transparent);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    private void resizeCameras() {
        int screen_width = readScreenWidth(this);
        camerasPerRow = recalculateCameraPerRow();

        io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                .FlowLayout) this.findViewById(R.id.cameras_flow_layout);
        for (int i = 0; i < camsLineView.getChildCount(); i++) {
            LinearLayout pview = (LinearLayout) camsLineView.getChildAt(i);
            CameraLayout cameraLayout = (CameraLayout) pview.getChildAt(0);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.view
                    .ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams
                    .WRAP_CONTENT);
            params.width = ((i + 1 % camerasPerRow == 0) ? (screen_width - (i %
                    camerasPerRow) * (screen_width / camerasPerRow)) : screen_width /
                    camerasPerRow);
            params.width = params.width - 1; //1 pixels spacing between cameras
            params.height = (int) (params.width / (1.25));
            params.setMargins(1, 1, 0, 0); //1 pixels spacing between cameras
            cameraLayout.setLayoutParams(params);
        }
    }

    private void updateCameraNames() {
        try {
            io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                    .FlowLayout) this.findViewById(R.id.cameras_flow_layout);
            for (int i = 0; i < camsLineView.getChildCount(); i++) {
                LinearLayout pview = (LinearLayout) camsLineView.getChildAt(i);
                CameraLayout cameraLayout = (CameraLayout) pview.getChildAt(0);

                cameraLayout.updateTitleIfDifferent();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            EvercamPlayApplication.sendCaughtException(this, e);
        }
    }

    /**
     * Remove all the cameras so that all activities being performed can be stopped
     */
    public void removeAllCameraViews() {
        stopAllCameraViews();

        io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                .FlowLayout) this.findViewById(R.id.cameras_flow_layout);
        camsLineView.removeAllViews();
    }

    /**
     * Add all camera views to the main grid page
     *
     * @param reloadImages   reload camera images or not
     * @param showThumbnails show thumbnails that returned by Evercam or not, if true
     *                       and if thumbnail not available, it will request latest snapshot
     *                       instead. If false,
     *                       it will request neither thumbnail nor latest snapshot.
     */
    public void addAllCameraViews(final boolean reloadImages, final boolean showThumbnails) {
        // Recalculate camera per row
        camerasPerRow = recalculateCameraPerRow();

        io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                .FlowLayout) this.findViewById(R.id.cameras_flow_layout);

        int screen_width = readScreenWidth(this);

        int index = 0;

        for (final EvercamCamera evercamCamera : AppData.evercamCameraList) {
            //Don't show offline camera
            if (!PrefsManager.showOfflineCameras(this) && !evercamCamera.isOnline()) {
                continue;
            }

            final LinearLayout cameraListLayout = new LinearLayout(this);

            int indexPlus = index + 1;

            if (reloadImages) evercamCamera.loadingStatus = ImageLoadingStatus.not_started;

            final CameraLayout cameraLayout = new CameraLayout(this, evercamCamera,
                    showThumbnails);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.view
                    .ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams
                    .WRAP_CONTENT);
            params.width = ((indexPlus % camerasPerRow == 0) ? (screen_width - (index %
                    camerasPerRow) * (screen_width / camerasPerRow)) : screen_width /
                    camerasPerRow);
            params.width = params.width - 1; //1 pixels spacing between cameras
            params.height = (int) (params.width / (1.25));
            params.setMargins(0, 0, 0, 0); //No spacing between cameras
            cameraLayout.setLayoutParams(params);

            cameraListLayout.addView(cameraLayout);

            camsLineView.addView(cameraListLayout, new io.evercam.androidapp.custom
                    .FlowLayout.LayoutParams(0, 0));

            index++;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Rect cameraBounds = new Rect();
                    cameraListLayout.getHitRect(cameraBounds);

                    Rect offlineIconBounds = cameraLayout.getOfflineIconBounds();
                    int layoutWidth = cameraBounds.right - cameraBounds.left;
                    int offlineStartsAt = offlineIconBounds.left;
                    int offlineIconWidth = offlineIconBounds.right - offlineIconBounds.left;

                    if (layoutWidth > offlineStartsAt + offlineIconWidth * 2) {
                        cameraLayout.showOfflineIconAsFloat = false;
                    } else {
                        cameraLayout.showOfflineIconAsFloat = true;
                    }
                }
            }, 200);
        }

        if (showThumbnails) showShowcaseViewForFirstTimeUser(onlyHasDemoCamera());

        if (refresh != null) refresh.setActionView(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity = null;
        removeAllCameraViews();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        resizeCameras();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (AppData.defaultUser != null) {
            usernameOnStop = AppData.defaultUser.getUsername();
        }

        showOfflineOnStop = PrefsManager.showOfflineCameras(this);
    }

    private boolean onlyHasDemoCamera() {
        if (AppData.evercamCameraList.size() == 1) {
            String demoCameraId = getResources().getString(R.string.demo_camera_id);
            if (AppData.evercamCameraList.get(0).getCameraId().equals(demoCameraId)) return true;
        }
        return false;
    }

    public static void logOutDefaultUser(Activity activity) {
        getMixpanel().identifyUser(UUID.randomUUID().toString());

        if (AppData.defaultUser != null) {
            new EvercamAccount(activity).remove(AppData.defaultUser.getEmail(), null);
        }

        // clear real-time default app data
        AppData.reset();

        activity.finish();
        activity.startActivity(new Intent(activity, OnBoardingActivity.class));
    }

    public static int readScreenWidth(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    /**
     * Recalculate camera per row preference for the following situations:
     * 1. If it won't influence others and the current user only has one or two cameras,
     * reset the value to be 1.
     * 2. If current user only has one or two cameras, but the device has other accounts
     * logged in, keep the value as it was without overriding it.
     * 3. If the current user has more than two cameras, adjust the value of camera per
     * row to be a proper number based on screen size.
     *
     * @return The recalculated value of camera per row
     */
    public int recalculateCameraPerRow() {
        int totalCameras = AppData.evercamCameraList.size();
        boolean isInfluencingOtherUser = false;
        ArrayList<AppUser> userList = new EvercamAccount(this).retrieveUserList();
        if (userList.size() > 1) {
            isInfluencingOtherUser = true;
        }

        if (totalCameras != 0 && totalCameras <= 2) {
            if (!isInfluencingOtherUser) {
                PrefsManager.setCameraPerRow(this, 1);
                return 1;
            } else {
                return PrefsManager.getCameraPerRow(this, 1);
            }
        } else {
            int screenWidth = readScreenWidth(this);
            int maxCamerasPerRow = 3;
            int minCamerasPerRow = 1;
            if (screenWidth != 0) {
                maxCamerasPerRow = screenWidth / 350;
            }

            int oldCamerasPerRow = PrefsManager.getCameraPerRow(this, 1);
            if (maxCamerasPerRow < oldCamerasPerRow && maxCamerasPerRow != 0) {
                PrefsManager.setCameraPerRow(this, maxCamerasPerRow);
                return maxCamerasPerRow;
            } else if (maxCamerasPerRow == 0) {
                return minCamerasPerRow;
            }
            return oldCamerasPerRow;
        }
    }

    private void initDataCollectionObjects() {
        startTime = new Date();
    }

    /**
     * Calculate how long it takes for the user to see the camera list
     */
    public void calculateLoadingTimeAndSend() {
        if (startTime != null) {
            float timeDifferenceFloat = Commons.calculateTimeDifferenceFrom(startTime);
            Log.d(TAG, "It takes " + databaseLoadTime + " and " + timeDifferenceFloat + " seconds" +
                    " to load camera list");
            startTime = null;

            String username = "";
            if (AppData.defaultUser != null) {
                username = AppData.defaultUser.getUsername();
            }
            LoadTimeFeedbackItem feedbackItem = new LoadTimeFeedbackItem(this,
                    username, databaseLoadTime, timeDifferenceFloat);
            databaseLoadTime = 0;
        }
    }

    private void showAccountView(boolean show) {
        mNavBodyScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        mNavBodyAccountView.setVisibility(show ? View.VISIBLE : View.GONE);
        mTriangleImageView.setRotation(show ? 180 : 0);
    }

    class CamerasCheckInternetTask extends CheckInternetTask {
        InternetCheckType type;

        public CamerasCheckInternetTask(Context context, InternetCheckType type) {
            super(context);
            this.type = type;
        }

        @Override
        protected void onPostExecute(Boolean hasNetwork) {
            if (hasNetwork) {
                if (type == InternetCheckType.START) {
                    updateNavDrawerUserInfo();
                    startLoadingCameras();
                } else if (type == InternetCheckType.RESTART) {
                    if (reloadCameraList) {
                        removeAllCameraViews();
                        startLoadingCameras();
                        reloadCameraList = false;
                    } else {
                        // Re-calculate camera per row because screen size
                        // could change because of screen rotation.
                        int camsOldValue = camerasPerRow;
                        camerasPerRow = recalculateCameraPerRow();
                        if (camsOldValue != camerasPerRow) {
                            removeAllCameraViews();
                            addAllCameraViews(true, true);
                        }

                        // Refresh camera names in case it's changed from camera
                        // live view
                        updateCameraNames();
                    }
                }
            } else {
                CustomedDialog.showInternetNotConnectDialog(CamerasActivity.this);
            }
        }
    }

    private void showActionButtons(boolean show) {
        actionButtonLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showShowcaseViewForFirstTimeUser(boolean onlyHasDemoCamera) {
        if (!PrefsManager.isShowcaseShown(this)) {
            showShowcaseView(onlyHasDemoCamera);
            PrefsManager.setShowcaseShown(this);
        }
    }

    private void showShowcaseView(boolean onlyHasDemoCamera) {
        if (onlyHasDemoCamera) showDemoCamShowcaseView();
//        else showAddCameraShowcaseView();
    }

    private MaterialShowcaseView.Builder applyCommonConfigs(MaterialShowcaseView.Builder builder) {
        builder.setDismissTextSize(17)
                .setDismissText(R.string.showcase_dismiss_text)
                .setContentTextSize(17)
                .setDismissOnTargetTouch(true)
                .setContentTextColor(getResources().getColor(R.color.white))
                .setMaskColour(getResources().getColor(R.color.black_semi_transparent));

        return builder;
    }

    /*private void showAddCameraShowcaseView() {
        MaterialShowcaseView.Builder builder = new MaterialShowcaseView.Builder(this, false);
        applyCommonConfigs(builder);
        builder.setTarget(manuallyAddButton)
                .setShapePadding(30)
                .setContentText(R.string.confirmSignUp)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                })
                .show();
    }
*/
    private void showDemoCamShowcaseView() {

        io.evercam.androidapp.custom.FlowLayout cameraLineLayout =
                (io.evercam.androidapp.custom.FlowLayout) findViewById(R.id.cameras_flow_layout);
        if (cameraLineLayout.getChildCount() > 0) {
            View view = cameraLineLayout.getChildAt(0);

            MaterialShowcaseView.Builder builder = new MaterialShowcaseView.Builder(this, true);
            applyCommonConfigs(builder);
            builder.setTarget(view)
                    .withRectangleShape()
                    .setContentText(R.string.showcase_demo_cam)
                    .setListener(new IShowcaseListener() {
                        @Override
                        public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                        }

                        @Override
                        public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//                            showAddCameraShowcaseView();
                        }
                    })
                    .show();
        }
    }

    /**
     * ObservableScrollView callbacks
     */
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        //Log.e(TAG, "onScrollChanged: " + scrollY + " " + firstScroll + " " + dragging);
        lastScrollY = scrollY;
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        //Log.e(TAG, "onUpOrCancelMotionEvent: " + scrollState);
        if (scrollState == ScrollState.UP) {
            //Fix the bug that it's UP if swiping down when the view reaches screen top
            if (lastScrollY != 0) {
                if (toolbarIsShown()) {
                    hideToolbar();
//                    showActionButtons(false);
                }
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarIsHidden()) {
                showToolbar();
//                showActionButtons(true);
            }
        }
    }

    class CheckKeyExpirationTaskNavDrawer extends CheckKeyExpirationTask {

        public CheckKeyExpirationTaskNavDrawer(AppUser appUser) {
            super(appUser);
        }

        @Override
        protected void onPostExecute(Boolean isExpired) {
            if (isExpired) {
                new EvercamAccount(CamerasActivity.this).remove(appUser.getEmail(), null);
                finish();
                startActivity(new Intent(CamerasActivity.this, OnBoardingActivity.class));
            } else {
                EvercamAccount evercamAccount = new EvercamAccount(getApplicationContext());
                evercamAccount.updateDefaultUser(appUser.getEmail());
                AppData.appUsers = evercamAccount.retrieveUserList();

                getMixpanel().identifyUser(AppData.defaultUser.getUsername());
                registerUserWithIntercom(AppData.defaultUser);

                closeDrawer();
                startLoadingCameras();
            }
        }
    }
}
