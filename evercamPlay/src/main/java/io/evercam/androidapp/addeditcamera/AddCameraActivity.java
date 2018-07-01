package io.evercam.androidapp.addeditcamera;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.view.KeyEvent;

import java.util.ArrayList;

import io.evercam.CameraBuilder;
import io.evercam.Defaults;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.androidapp.EditCameraActivity;
import io.evercam.androidapp.EvercamPlayApplication;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.custom.ExplanationView;
import io.evercam.androidapp.custom.PortCheckEditText;
import io.evercam.androidapp.tasks.AddCameraTask;
import io.evercam.androidapp.tasks.PortCheckTask;
import io.evercam.androidapp.tasks.TestSnapshotTask;
import io.evercam.androidapp.utils.DataCollector;
import io.evercam.network.discovery.DiscoveredCamera;
import io.intercom.android.sdk.Intercom;

public class AddCameraActivity extends AddCameraParentActivity {
    private final String TAG = "AddCameraActivity";
    private final String KEY_FLIPPER_POSITION = "flipperPosition";
    private final String KEY_SELECTED_MODEL = "selectedModel";

    /**
     * The object for camera from discovery passed by intent
     * If it's not null, the camera details should be auto populated in the form
     */
    private DiscoveredCamera mDiscoveredCamera;

    private ViewFlipper mViewFlipper;
    private ProgressBar mProgressBar;
    private Handler mHandler;

    /**
     * Model selector
     */
    private ModelSelectorFragment mModelSelectorFragment;
    private SelectedModel mSelectedModel;
    private Defaults mSelectedModelDefaults;

    /**
     * Connect camera
     */
    private PortCheckEditText mPublicIpEditText;
    private PortCheckEditText mHttpEditText;
    private PortCheckEditText mRtspEditText;
    private TextView mHttpStatusText;
    private TextView mRtspStatusText;
    private ProgressBar mHttpProgressBar;
    private ProgressBar mRtspProgressBar;
    private ExplanationView mConnectExplainView;
    private LinearLayout mAuthLayout;
    private CheckBox mAuthCheckBox;
    private EditText mCamUsernameEditText;
    private EditText mCamPasswordEditText;
    private Button mCheckSnapshotButton;
    private ValidateHostInput mValidateHostInput;
    private TextView mSelectedModelTextView;
    private RelativeLayout mButtonIndicatorLayout;
    private LinearLayout mConnectFormLayout;
    private LinearLayout mSnapshotPathLayout;
    private LinearLayout mRtspPathLayout;
    private EditText mSnapshotPathEditText;
    private EditText mRtspPathEditText;
    private TextView mLiveSupportLink;

    /**
     * Camera name view
     */
    private EditText mCameraNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);

        setUpDefaultToolbar();
        setHomeIconAsCancel();

        initDiscoveredCamera();

        EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_add_camera));

        mViewFlipper = (ViewFlipper) findViewById(R.id.add_camera_view_flipper);
        mProgressBar = (ProgressBar) findViewById(R.id.add_camera_progress_bar);
        mProgressBar.setProgress(33);

        if (savedInstanceState != null) {
            mSelectedModel = (SelectedModel) savedInstanceState.get(KEY_SELECTED_MODEL);
        }

        mHandler = new Handler();

        /** Init UI for model selector screen */
        initModelSelectorUI();

        /** Init UI for connect camera screen */
        initConnectCameraUI();

        /** Init UI for camera name view */
        initCameraNameView();

        fillDiscoveredCameraDetails(mDiscoveredCamera);

        if (savedInstanceState != null) {
            int flipperPosition = savedInstanceState.getInt(KEY_FLIPPER_POSITION);
            showPageAtPosition(flipperPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        int position = mViewFlipper.getDisplayedChild();
        savedInstanceState.putInt(KEY_FLIPPER_POSITION, position);
        savedInstanceState.putSerializable(KEY_SELECTED_MODEL, mSelectedModel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                quitAddCamera();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        int selectedPage = mViewFlipper.getDisplayedChild();
        if (selectedPage == 0) {
            quitAddCamera();
        }else {
            showPageAtPosition(selectedPage - 1);
        }
    }


    private void showPageAtPosition(int flipperPosition) {
        if (flipperPosition == 0) {
            showModelSelectorView();
        } else if (flipperPosition == 1) {
            showConnectCameraView();
        } else if (flipperPosition == 2) {
            showCameraNameView();
        }
    }

    private void initDiscoveredCamera() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mDiscoveredCamera = (DiscoveredCamera) getIntent().getSerializableExtra("camera");
        }
    }

    private void fillDiscoveredCameraDetails(DiscoveredCamera camera) {
        if (camera != null) {
            Log.d(TAG, camera.toString());
            if (camera.hasExternalIp()) {
                mPublicIpEditText.setText(camera.getExternalIp());
            }
            if (camera.hasExternalHttp()) {
                mHttpEditText.setText(String.valueOf(camera.getExthttp()));
            }
            if (camera.hasExternalRtsp()) {
                mRtspEditText.setText(String.valueOf(camera.getExtrtsp()));
            }
            if (camera.hasName()) {
                //The maximum camera name length is 24
                String cameraName = camera.getName();
                if (cameraName.length() > 24) {
                    cameraName = cameraName.substring(0, 23);
                }
                mCameraNameEditText.setText(cameraName);
            } else {
                mCameraNameEditText.setText((camera.getVendor() + " " + camera.getModel()).toUpperCase());
            }
        }
    }

    private void initModelSelectorUI() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mModelSelectorFragment = (ModelSelectorFragment)
                fragmentManager.findFragmentById(R.id.add_camera_model_selector_fragment);
        mModelSelectorFragment.hideModelQuestionMark();
        Button modelSelectorViewNextButton = (Button) findViewById(R.id.model_selector_view_next_button);
        TextView reportModelLink = (TextView) findViewById(R.id.report_model_text_link);

        reportModelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomedDialog.showReportCameraModelDialog(AddCameraActivity.this, null);
            }
        });

        modelSelectorViewNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String modelId = mModelSelectorFragment.getModelIdFromSpinner();
                String modelName = mModelSelectorFragment.getModelNameFromSpinner();
                String vendorId = mModelSelectorFragment.getVendorIdFromSpinner();
                String vendorName = mModelSelectorFragment.getVendorNameFromSpinner();
                mSelectedModel = new SelectedModel(modelId, modelName, vendorId, vendorName, mSelectedModelDefaults);

                showConnectCameraView();
            }
        });
    }

    private void initConnectCameraUI() {
        mPublicIpEditText = (PortCheckEditText) findViewById(R.id.external_ip_float_edit_text);
        mHttpEditText = (PortCheckEditText) findViewById(R.id.http_float_edit_text);
        mHttpEditText.setPortType(PortCheckTask.PortType.HTTP);
        mRtspEditText = (PortCheckEditText) findViewById(R.id.rtsp_float_edit_text);
        mRtspEditText.setPortType(PortCheckTask.PortType.RTSP);
        mHttpStatusText = (TextView) findViewById(R.id.port_status_text_http);
        mRtspStatusText = (TextView) findViewById(R.id.port_status_text_rtsp);
        mHttpProgressBar = (ProgressBar) findViewById(R.id.progress_bar_http);
        mRtspProgressBar = (ProgressBar) findViewById(R.id.progress_bar_rtsp);
        mConnectExplainView = (ExplanationView) findViewById(R.id.explanation_view_layout);
        mAuthCheckBox = (CheckBox) findViewById(R.id.auth_check_box);
        mAuthLayout = (LinearLayout) findViewById(R.id.auth_layout);
        TextView requiredAuthText = (TextView) findViewById(R.id.required_auth_text);
        mCamUsernameEditText = (EditText) findViewById(R.id.cam_username_float_edit_text);
        mCamPasswordEditText = (EditText) findViewById(R.id.cam_password_float_edit_text);
        mCheckSnapshotButton = (Button) findViewById(R.id.check_snapshot_button);
        mLiveSupportLink = (TextView) findViewById(R.id.live_support_text_link);
        ImageView editModelImageButton = (ImageView) findViewById(R.id.edit_model_image_view);
        mSelectedModelTextView = (TextView) findViewById(R.id.selected_model_text);
        ImageView clearHostImageButton = (ImageView) findViewById(R.id.clear_host_image_button);
        mButtonIndicatorLayout = (RelativeLayout) findViewById(R.id.snapshot_button_indicator_layout);
        mConnectFormLayout = (LinearLayout) findViewById(R.id.connect_form_layout);
        mSnapshotPathLayout = (LinearLayout) findViewById(R.id.snapshot_path_layout);
        mRtspPathLayout = (LinearLayout) findViewById(R.id.rtsp_path_layout);
        mSnapshotPathEditText = (EditText) findViewById(R.id.snapshot_path_float_edit_text);
        mRtspPathEditText = (EditText) findViewById(R.id.rtsp_path_float_edit_text);

        clearHostImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublicIpEditText.requestFocus();
                mPublicIpEditText.setText("");
            }
        });

        editModelImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModelSelectorView();
            }
        });

        mLiveSupportLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intercom.client().displayConversationsList();
            }
        });

        mCheckSnapshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mValidateHostInput.passed()) {
                    final String username = mCamUsernameEditText.getText().toString();
                    final String password = mCamPasswordEditText.getText().toString();
                    final String externalHost = mPublicIpEditText.getText().toString();
                    final String externalHttp = mHttpEditText.getText().toString();

                    String jpgUrl = "";

                    if (mSelectedModel != null) {
                        if (!mSelectedModel.isUnknown()) {
                            jpgUrl = EditCameraActivity.buildUrlEndingWithSlash(mSelectedModel.getDefaultJpgUrl());
                        } else {
                            final String jpgPath = mSnapshotPathEditText.getText().toString();
                            jpgUrl = EditCameraActivity.buildUrlEndingWithSlash(jpgPath);
                        }
                    }

                    String externalUrl = getString(R.string.prefix_http) + externalHost + ":" + externalHttp;

                    new TestSnapshotTask(externalUrl, jpgUrl, username, password,
                            AddCameraActivity.this,mModelSelectorFragment.getVendorIdFromSpinner(),"").executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR);
                }
            }
        });

        View.OnFocusChangeListener showAuthTextListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                showAuthExplanation();
            }
        };
        mCamUsernameEditText.setOnFocusChangeListener(showAuthTextListener);
        mCamPasswordEditText.setOnFocusChangeListener(showAuthTextListener);

        mAuthCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onAuthCheckedChange(isChecked);
            }
        });

        requiredAuthText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthCheckBox.setChecked(!mAuthCheckBox.isChecked());
                onAuthCheckedChange(mAuthCheckBox.isChecked());
            }
        });

        mHttpEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mHttpEditText.hideStatusViewsOnTextChange(mHttpStatusText);
                    updateMessage(mConnectExplainView, 0, R.string.connect_camera_http_message);
                }
            }
        });

        mRtspEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mRtspEditText.hideStatusViewsOnTextChange(mRtspStatusText);
                    updateMessage(mConnectExplainView, 0, R.string.connect_camera_rtsp_message);
                }
            }
        });

        mPublicIpEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPublicIpEditText.hideStatusViewsOnTextChange(
                            mHttpStatusText, mRtspStatusText);
                    updateMessage(mConnectExplainView, 0, R.string.connect_camera_ip_message);
                }
            }
        });

        mSnapshotPathEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateMessage(mConnectExplainView, 0, R.string.connect_camera_snapshot_path_message);
                }
            }
        });

        mRtspPathEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateMessage(mConnectExplainView, 0, R.string.connect_camera_rtsp_path_message);
                }
            }
        });

        mValidateHostInput = new ValidateHostInput(mPublicIpEditText,
                mHttpEditText, mRtspEditText) {
            @Override
            public void onLocalIp() {
                mPublicIpEditText.requestFocus();
                showLocalIpWarning();
            }

            @Override
            public void onHostEmpty() {
                mPublicIpEditText.requestFocus();
                CustomToast.showInCenter(AddCameraActivity.this, getString(R.string.host_required));
            }

            @Override
            public void onHttpEmpty() {
                mHttpEditText.requestFocus();
                CustomToast.showInCenter(AddCameraActivity.this, getString(R.string.external_http_required));
            }

            @Override
            public void onInvalidHttpPort() {
                mHttpEditText.requestFocus();
                CustomToast.showInCenter(AddCameraActivity.this, getString(R.string.msg_port_range_error));
            }

            @Override
            public void onInvalidRtspPort() {
                mRtspEditText.requestFocus();
                CustomToast.showInCenter(AddCameraActivity.this, getString(R.string.msg_port_range_error));
            }
        };
    }

    public void initCameraNameView() {
        mCameraNameEditText = (EditText) findViewById(R.id.cam_name_float_edit_text);
        final Button createCameraButton = (Button) findViewById(R.id.create_camera_button);

        createCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraBuilder cameraBuilder = buildCamera(mSelectedModel);
                if (cameraBuilder != null) {
                    boolean isFromScan = mDiscoveredCamera != null;
                    //Set camera status to be online as a temporary fix for #133
                    cameraBuilder.setOnline(true);
                    createCameraButton.setEnabled(false);
                    new AddCameraTask(cameraBuilder.build(), AddCameraActivity.this,
                            isFromScan,createCameraButton).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
    }

    public void showTestSnapshotProgress(final boolean show) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    mButtonIndicatorLayout.setVisibility(View.VISIBLE);
                    mCheckSnapshotButton.setVisibility(View.GONE);
                } else {
                    mButtonIndicatorLayout.setVisibility(View.GONE);
                    mCheckSnapshotButton.setVisibility(View.VISIBLE);
                }
            }
        }, 100);

    }

    public void showUnknownModelForm(boolean show) {
        /** Adjust form margin */
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mConnectFormLayout.getLayoutParams();
        if (show) {
            layoutParams.setMargins(dpInPixels(12), dpInPixels(5), dpInPixels(12), 0);
        } else {
            layoutParams.setMargins(dpInPixels(50), dpInPixels(5), dpInPixels(50), 0);
        }
        mConnectFormLayout.setLayoutParams(layoutParams);

        /** Show/hide snapshot & RTSP path */
        mSnapshotPathLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        mRtspPathLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void onDefaultsLoaded(Model model) {
        if (model != null) {
            try {
                mSelectedModelDefaults = model.getDefaults();
            } catch (EvercamException e) {
                e.printStackTrace();
            }
        } else {
            mSelectedModelDefaults = null;
        }
    }

    public void buildSpinnerOnVendorListResult(@NonNull ArrayList<Vendor> vendorList) {
        mModelSelectorFragment.buildVendorSpinner(vendorList, null);
    }

    public void buildSpinnerOnModelListResult(ArrayList<Model> modelList) {
        String autoPopulatedModel = null;
        if (mDiscoveredCamera != null && mDiscoveredCamera.hasModel()) {
            autoPopulatedModel = mDiscoveredCamera.getModel();
        }
        mModelSelectorFragment.buildModelSpinner(modelList, autoPopulatedModel);
    }

    public boolean isFromDiscoverAndHasVendor() {
        return mDiscoveredCamera != null && mDiscoveredCamera.hasVendor();
    }

    public DiscoveredCamera getDiscoveredCamera() {
        return mDiscoveredCamera;
    }

    private void onAuthCheckedChange(boolean isChecked) {
        mAuthLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);

        if (isChecked) {
            mCamUsernameEditText.requestFocus();
            showAuthExplanation();
            populateDefaultAuth();
            expandSupportLink(false);
        } else {
            mCamUsernameEditText.setText("");
            mCamUsernameEditText.setText("");
            expandSupportLink(true);
        }
    }

    private void populateDefaultAuth() {
        if (mSelectedModel != null) {
            String defaultUsername = mSelectedModel.getDefaultUsername();
            String defaultPassword = mSelectedModel.getDefaultPassword();

            TextInputLayout usernameInputLayout = (TextInputLayout) findViewById(R.id.input_layout_cam_username);
            TextInputLayout passwordInputLayout = (TextInputLayout) findViewById(R.id.input_layout_cam_password);
            usernameInputLayout.setErrorEnabled(true);
            passwordInputLayout.setErrorEnabled(true);

            if (!defaultUsername.isEmpty()) {
                usernameInputLayout.setError(getString(R.string.default_colon) + defaultUsername);
            } else {
                usernameInputLayout.setErrorEnabled(false);
            }
            if (!defaultPassword.isEmpty()) {
                passwordInputLayout.setError(getString(R.string.default_colon) + defaultPassword);
            } else {
                passwordInputLayout.setErrorEnabled(false);
            }
        }
    }

    private void showModelSelectorView() {
        mViewFlipper.setDisplayedChild(0);
        mProgressBar.setProgress(33);
        setTitle(R.string.title_choose_model);
    }

    private void showConnectCameraView() {
        mViewFlipper.setDisplayedChild(1);
        mProgressBar.setProgress(67);
        setTitle(R.string.title_connect_camera);
        updateMessage(mConnectExplainView, 0, R.string.connect_camera_explain_message);
        populateSelectedModel(mSelectedModelTextView, mSelectedModel);

        /**
         * Apply a minor delay for populating external IP
         *  In case there are data restored in public IP EditText
         */
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoPopulateExternalIP(mPublicIpEditText);
            }
        }, 100);
    }

    public void showCameraNameView() {
        mViewFlipper.setDisplayedChild(2);
        mProgressBar.setProgress(100);
        setTitle(R.string.title_name_camera);
    }

    private void updateMessage(ExplanationView explanationView, int titleId, int messageId) {
        explanationView.updateTitle(titleId);
        explanationView.updateMessage(messageId);
    }

    private void showAuthExplanation() {
        updateMessage(mConnectExplainView, 0, R.string.connect_camera_auth_message);
    }

    private CameraBuilder buildCamera(SelectedModel selectedModel) {
        String cameraName = mCameraNameEditText.getText().toString();

        if (!cameraName.isEmpty()) {
            CameraBuilder cameraBuilder = new CameraBuilder(cameraName, false);
            cameraBuilder.setExternalHttpPort(mHttpEditText.getPort());
            cameraBuilder.setExternalHost(mPublicIpEditText.getText().toString());
            int externalRtspInt = mRtspEditText.getPort();

            cameraBuilder.setExternalRtspPort(externalRtspInt);

            if (selectedModel != null) {
                String vendorId = selectedModel.getVendorId();
                if (!vendorId.isEmpty()) {
                    cameraBuilder.setVendor(vendorId);
                }

                String modelId = selectedModel.getModelId();

                if (!modelId.isEmpty()) {

                    cameraBuilder.setModel(modelId);

                }else if (vendorId.equals("other") && modelId.equals("")){

                    cameraBuilder.setModel("other_default");

                }else if (vendorId.equals("") && modelId.equals("")){

                    cameraBuilder.setVendor("other");
                    cameraBuilder.setModel("other_default");
                }

                String jpgUrl;
                String rtspUrl;

                if (selectedModel.isUnknown()) {
                    jpgUrl = mSnapshotPathEditText.getText().toString();
                    rtspUrl = mRtspPathEditText.getText().toString();
                } else {
                    jpgUrl = EditCameraActivity.buildUrlEndingWithSlash(selectedModel.getDefaultJpgUrl());
                    rtspUrl = EditCameraActivity.buildUrlEndingWithSlash(selectedModel.getDefaultRtspUrl());
                }

                if (!jpgUrl.isEmpty()) {
                    cameraBuilder.setJpgUrl(jpgUrl);
                }

                if (!rtspUrl.isEmpty()) {
                    cameraBuilder.setH264Url(rtspUrl);
                }
            }

            String username = mCamUsernameEditText.getText().toString();
            if (!username.isEmpty()) {
                cameraBuilder.setCameraUsername(username);
            }

            String password = mCamPasswordEditText.getText().toString();
            if (!password.isEmpty()) {
                cameraBuilder.setCameraPassword(password);
            }

            //Attach additional info for discovered camera
            if (mDiscoveredCamera != null) {
                cameraBuilder.setInternalHost(mDiscoveredCamera.getIP());

                if (mDiscoveredCamera.hasMac()) {
                    cameraBuilder.setMacAddress(mDiscoveredCamera.getMAC());
                }

                if (mDiscoveredCamera.hasHTTP()) {
                    cameraBuilder.setInternalHttpPort(mDiscoveredCamera.getHttp());
                }

                if (mDiscoveredCamera.hasRTSP()) {
                    cameraBuilder.setInternalRtspPort(mDiscoveredCamera.getRtsp());
                }
            }

            return cameraBuilder;
        }

        return null;
    }

    private void quitAddCamera() {

        CustomedDialog.getConfirmCancelAddCameraDialog(this).show();
    }

    private void populateSelectedModel(TextView textView, SelectedModel selectedModel) {
        String modelName = selectedModel.getModelName();
        String vendorName = selectedModel.getVendorName();

        if (modelName.isEmpty() && vendorName.isEmpty()) {
            textView.setText(R.string.unknown);
        } else {
            if (modelName.isEmpty()) modelName = getString(R.string.unknown);
            if (vendorName.isEmpty()) vendorName = getString(R.string.unknown);
            textView.setText(vendorName + " - " + modelName);
        }

        showUnknownModelForm(mSelectedModel.isUnknown());

        populateDefaultAuth();
    }

    private void autoPopulateExternalIP(final EditText editText) {
        /**
         * Auto populate IP as external IP address if on WiFi
         */
        if (new DataCollector(this).isConnectedWifi()) {
            if (editText.getText().toString().isEmpty()) {

                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        return io.evercam.network.discovery.NetworkInfo.getExternalIP();
                    }

                    @Override
                    protected void onPostExecute(String externalIp) {
                        editText.setText(externalIp);
                        autoPopulateDefaultPorts(mHttpEditText, mRtspEditText);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                autoPopulateDefaultPorts(mHttpEditText, mRtspEditText);
            }
        }
    }

    /**
     * Auto populate default port 80 and 554 and launch port check
     * Only when the port text field is empty
     */
    private void autoPopulateDefaultPorts(EditText httpEditText, EditText rtspEditText) {
        if (httpEditText.getText().toString().isEmpty()) {
            httpEditText.setText("80");
            checkPort(PortCheckTask.PortType.HTTP);
        } else {
            checkPort(PortCheckTask.PortType.HTTP);
        }
        if (rtspEditText.getText().toString().isEmpty()) {
            rtspEditText.setText("554");
            checkPort(PortCheckTask.PortType.RTSP);
        } else {
            checkPort(PortCheckTask.PortType.RTSP);
        }
    }

    private void expandSupportLink(boolean expand) {
        if (mLiveSupportLink != null) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mLiveSupportLink
                    .getLayoutParams();
            int marginTop = dpInPixels(50);
            if (!expand) marginTop = dpInPixels(20);
            layoutParams.setMargins(0, marginTop, 0, 0);
        }
    }

    @Override
    public EditText getPublicIpEditText() {
        return mPublicIpEditText;
    }

    @Override
    public EditText getHttpEditText() {
        return mHttpEditText;
    }

    @Override
    public EditText getRtspEditText() {
        return mRtspEditText;
    }

    @Override
    public TextView getHttpStatusText() {
        return mHttpStatusText;
    }

    @Override
    public TextView getRtspStatusText() {
        return mRtspStatusText;
    }

    @Override
    public ProgressBar getHttpProgressBar() {
        return mHttpProgressBar;
    }

    @Override
    public ProgressBar getRtspProgressBar() {
        return mRtspProgressBar;
    }
}
