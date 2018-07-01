package io.evercam.androidapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mashape.unirest.http.JsonNode;

import org.json.JSONException;

import java.util.ArrayList;

import io.evercam.Auth;
import io.evercam.Defaults;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.PatchCameraBuilder;
import io.evercam.Vendor;
import io.evercam.androidapp.addeditcamera.AddCameraParentActivity;
import io.evercam.androidapp.addeditcamera.ModelSelectorFragment;
import io.evercam.androidapp.addeditcamera.ValidateHostInput;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.custom.PortCheckEditText;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.PatchCameraTask;
import io.evercam.androidapp.tasks.PortCheckTask;
import io.evercam.androidapp.tasks.TestSnapshotTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;
import io.intercom.android.sdk.Intercom;

public class EditCameraActivity extends AddCameraParentActivity {
    private final String TAG = "AddEditCameraActivity";

    private LinearLayout cameraIdLayout;
    private TextView cameraIdTextView;
    private EditText cameraNameEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private PortCheckEditText externalHostEdit;
    private PortCheckEditText externalHttpEdit;
    private PortCheckEditText externalRtspEdit;
    private EditText jpgUrlEdit;
    private EditText rtspUrlEdit;
    private TextView mHttpStatusTextView;
    private TextView mRtspStatusTextView;
    private ProgressBar mHttpProgressBar;
    private ProgressBar mRtspProgressBar;
    private LinearLayout jpgUrlLayout;
    private LinearLayout rtspUrlLayout;
    private Button editButton;
    private ModelSelectorFragment modelSelectorFragment;
    private ValidateHostInput mValidateHostInput;

    private EvercamCamera cameraEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_camera);

        setUpDefaultToolbar();

        Bundle bundle = getIntent().getExtras();
        // Edit Camera
        if (bundle != null && bundle.containsKey(Constants.KEY_IS_EDIT)) {
            EvercamPlayApplication.sendScreenAnalytics(this,
                    getString(R.string.screen_edit_camera));
            cameraEdit = VideoActivity.evercamCamera;
        } 

        // Initial UI elements
        initialScreen();

        fillEditCameraDetails(cameraEdit);
    }

    @Override
    public void onBackPressed() {
        setResult(Constants.RESULT_FALSE);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_edit_camera, menu);

        MenuItem supportMenuItem = menu.findItem(R.id.menu_action_support);
        if (supportMenuItem != null) {
            LinearLayout menuLayout = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.menu_support_lowercase, null);
            supportMenuItem.setActionView(menuLayout);
            supportMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menuLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intercom.client().displayConversationsList();
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Constants.RESULT_FALSE);
                super.onBackPressed();
                return true;
        }
        return true;
    }

    private void initialScreen() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        modelSelectorFragment = (ModelSelectorFragment) fragmentManager.findFragmentById(R.id.model_selector_fragment);

        cameraIdLayout = (LinearLayout) findViewById(R.id.add_camera_id_layout);
        cameraIdTextView = (TextView) findViewById(R.id.add_id_txt_view);
        cameraNameEdit = (EditText) findViewById(R.id.add_name_edit);
        ImageView externalIpExplainationImageButton = (ImageView) findViewById(R.id.ip_explanation_btn);
        ImageView httpExplainationImageButton = (ImageView) findViewById(R.id.http_explanation_btn);
        ImageView jpgExplainationImageButton = (ImageView) findViewById(R.id.jpg_explanation_btn);
        ImageView rtspPortExplainationImageButton = (ImageView) findViewById(R.id.rtsp_port_explanation_btn);
        ImageView rtspUrlExplainationImageButton = (ImageView) findViewById(R.id.rtsp_url_explanation_btn);
        usernameEdit = (EditText) findViewById(R.id.add_username_edit);
        passwordEdit = (EditText) findViewById(R.id.add_password_edit);
        externalHostEdit = (PortCheckEditText) findViewById(R.id.add_external_host_edit);
        externalHttpEdit = (PortCheckEditText) findViewById(R.id.add_external_http_edit);
        externalHttpEdit.setPortType(PortCheckTask.PortType.HTTP);
        externalRtspEdit = (PortCheckEditText) findViewById(R.id.add_external_rtsp_edit);
        externalRtspEdit.setPortType(PortCheckTask.PortType.RTSP);
        jpgUrlEdit = (EditText) findViewById(R.id.add_jpg_edit);
        rtspUrlEdit = (EditText) findViewById(R.id.add_rtsp_edit);
        mHttpStatusTextView = (TextView) findViewById(R.id.port_status_text_http);
        mRtspStatusTextView = (TextView) findViewById(R.id.port_status_text_rtsp);
        mHttpProgressBar = (ProgressBar) findViewById(R.id.progress_bar_http);
        mRtspProgressBar = (ProgressBar) findViewById(R.id.progress_bar_rtsp);
        jpgUrlLayout = (LinearLayout) findViewById(R.id.add_jpg_url_layout);
        rtspUrlLayout = (LinearLayout) findViewById(R.id.add_rtsp_url_layout);
        editButton = (Button) findViewById(R.id.button_add_edit_camera);
        Button testButton = (Button) findViewById(R.id.button_test_snapshot);

        mValidateHostInput = new ValidateHostInput(externalHostEdit,
                externalHttpEdit, externalRtspEdit) {
            @Override
            public void onLocalIp() {
                externalHostEdit.requestFocus();
                showLocalIpWarning();
            }

            @Override
            public void onHostEmpty() {
                CustomToast.showInCenter(EditCameraActivity.this, getString(R.string.host_required));
            }

            @Override
            public void onHttpEmpty() {
                CustomToast.showInCenter(EditCameraActivity.this, getString(R.string.external_http_required));
            }

            @Override
            public void onInvalidHttpPort() {
                CustomToast.showInCenter(EditCameraActivity.this, getString(R.string.msg_port_range_error));
            }

            @Override
            public void onInvalidRtspPort() {
                CustomToast.showInCenter(EditCameraActivity.this, getString(R.string.msg_port_range_error));
            }
        };

        if (cameraEdit != null) {
            editButton.setText(getString(R.string.save_changes));
            cameraIdLayout.setVisibility(View.VISIBLE);
        }

        externalIpExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomedDialog.showMessageDialogWithTitle(EditCameraActivity.this, R.string
                        .msg_ip_explanation_title, R.string
                        .msg_ip_explanation);
            }
        });
        jpgExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomedDialog.showMessageDialogWithTitle(EditCameraActivity.this, R.string
                        .msg_jpg_explanation_title, R.string
                        .msg_jpg_explanation);
            }
        });
        httpExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomedDialog.showMessageDialogWithTitle(EditCameraActivity.this, R.string
                        .msg_http_explanation_title, R.string
                        .msg_http_explanation);
            }
        });
        rtspPortExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomedDialog.showMessageDialogWithTitle(EditCameraActivity.this, R.string
                        .msg_rtsp_port_explanation_title, R.string
                        .msg_rtsp_port_explanation);
            }
        });
        rtspUrlExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomedDialog.showMessageDialogWithTitle(EditCameraActivity.this, R.string
                        .msg_rtsp_url_explanation_title, R.string
                        .msg_rtsp_url_explanation);
            }
        });

        externalHttpEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    externalHttpEdit.hideStatusViewsOnTextChange(mHttpStatusTextView);
                }
            }
        });

        externalRtspEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    externalRtspEdit.hideStatusViewsOnTextChange(mRtspStatusTextView);
                }
            }
        });

        externalHostEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    externalHostEdit.hideStatusViewsOnTextChange(
                            mRtspStatusTextView, mHttpStatusTextView);
                }
            }
        });

        jpgUrlEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!jpgUrlEdit.isFocusable()) {
                    CustomedDialog.showMessageDialog(EditCameraActivity.this, R.string.msg_url_ending_not_editable);
                }
            }
        });

        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String externalHost = externalHostEdit.getText().toString();
                if (Commons.isLocalIp(externalHost)) {
                    showLocalIpWarning();
                } else {
                    performEdit();
                }
            }
        });

        testButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String externalHost = externalHostEdit.getText().toString();
                if (Commons.isLocalIp(externalHost)) {
                    showLocalIpWarning();
                } else {
                    launchTestSnapshot();
                }
            }
        });
    }

    private void performEdit() {
        PatchCameraBuilder patchCameraBuilder = buildPatchCameraWithLocalCheck();
        if (patchCameraBuilder != null) {
            new PatchCameraTask(patchCameraBuilder.build(),
                    EditCameraActivity.this).executeOnExecutor(AsyncTask
                    .THREAD_POOL_EXECUTOR);
        } else {
            Log.e(TAG, "Camera to patch is null");
        }
    }

    private void fillEditCameraDetails(EvercamCamera camera) {
        if (camera != null) {
            showUrlEndings(!camera.hasModel());

            // Log.d(TAG, cameraEdit.toString());
            cameraIdTextView.setText(camera.getCameraId());
            cameraNameEdit.setText(camera.getName());
            usernameEdit.setText(camera.getUsername());
            passwordEdit.setText(camera.getPassword());
            jpgUrlEdit.setText(camera.getJpgPath());
            rtspUrlEdit.setText(camera.getH264Path());
            externalHostEdit.setText(camera.getExternalHost());
            int externalHttp = camera.getExternalHttp();
            int externalRtsp = camera.getExternalRtsp();
            if (externalHttp != 0) {
                externalHttpEdit.setText(String.valueOf(externalHttp));
            }
            if (externalRtsp != 0) {
                externalRtspEdit.setText(String.valueOf(externalRtsp));
            }
        }
    }

    public void showUrlEndings(boolean show) {
        jpgUrlLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        rtspUrlLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Read and validate user input for edit camera.
     */
    private PatchCameraBuilder buildPatchCameraWithLocalCheck() {
        PatchCameraBuilder patchCameraBuilder = new PatchCameraBuilder(cameraEdit.getCameraId());

        if (mValidateHostInput.passed()) {
            //HTTP port can't be empty
            patchCameraBuilder.setExternalHttpPort(externalHttpEdit.getPort());
            patchCameraBuilder.setExternalHost(externalHostEdit.getText().toString());

            //Allow RTSP port to be empty
            if (externalRtspEdit.isEmpty()) {
                patchCameraBuilder.setExternalRtspPort(null);
            } else {
                int externalRtspInt = externalRtspEdit.getPort();
                if (externalRtspInt != 0) {
                    patchCameraBuilder.setExternalRtspPort(externalRtspInt);
                }
            }

            String cameraName = cameraNameEdit.getText().toString();
            if (cameraName.isEmpty()) {
                CustomToast.showInCenter(this, getString(R.string.name_required));
                return null;
            } else if (!cameraName.equals(cameraEdit.getName())) {
                patchCameraBuilder.setName(cameraName);
            }

            String vendorId = modelSelectorFragment.getVendorIdFromSpinner();
            patchCameraBuilder.setVendor(vendorId);

            String modelName = modelSelectorFragment.getModelIdFromSpinner();

            if (vendorId.equals("other") && modelName.equals("")){
                modelName = "other_default";
            }else if (vendorId.equals("") && modelName.equals("")){
                vendorId    = "other";
                modelName   = "other_default";
            }

            patchCameraBuilder.setModel(modelName);

            String username = usernameEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            if (!username.equals(cameraEdit.getUsername())
                    || !password.equals(cameraEdit.getPassword())) {
                patchCameraBuilder.setCameraUsername(username);
                patchCameraBuilder.setCameraPassword(password);
            }

            String jpgUrl = buildUrlEndingWithSlash(jpgUrlEdit.getText().toString());
            if (!jpgUrl.equals(cameraEdit.getJpgPath())) {
                patchCameraBuilder.setJpgUrl(jpgUrl);
            }

            String rtspUrl = buildUrlEndingWithSlash(rtspUrlEdit.getText().toString());
            if (!rtspUrl.equals(cameraEdit.getH264Path())) {
                patchCameraBuilder.setH264Url(rtspUrl);
            }
        } else {
            return null;
        }

        return patchCameraBuilder;
    }

    public void fillDefaults(Model model) {
        try {
            // FIXME: Sometimes vendor with no default model, contains default
            // jpg url.
            // TODO: Consider if no default values associated, clear defaults
            // that has been filled.
            Defaults defaults = model.getDefaults();
            Auth basicAuth = defaults.getAuth(Auth.TYPE_BASIC);
            if (basicAuth != null && cameraEdit == null) {
                usernameEdit.setText(basicAuth.getUsername());
                passwordEdit.setText(basicAuth.getPassword());
            }
            jpgUrlEdit.setText(defaults.getJpgURL());
//            rtspUrlEdit.setText(defaults.getH264URL());

            if (!model.getName().equals(Model.DEFAULT_MODEL_NAME)
                    && !jpgUrlEdit.getText().toString().isEmpty()) {
                //If user specified a specific model, make it not editable
                jpgUrlEdit.setFocusable(false);
                jpgUrlEdit.setClickable(true);
            } else {
                //For default model or
                jpgUrlEdit.setFocusable(true);
                jpgUrlEdit.setClickable(true);
                jpgUrlEdit.setFocusableInTouchMode(true);
            }
        } catch (EvercamException e) {
            Log.e(TAG, "Fill defaults: " + e.toString());
        }
    }

    public void clearDefaults() {
        if (cameraEdit == null) {
            usernameEdit.setText("");
            passwordEdit.setText("");
        }
//        jpgUrlEdit.setText("");
        rtspUrlEdit.setText("");

        //Make it editable when defaults are cleared
        jpgUrlEdit.setFocusable(true);
        jpgUrlEdit.setClickable(true);
        jpgUrlEdit.setFocusableInTouchMode(true);
    }

    public static String buildUrlEndingWithSlash(String originalUrl) {
        String jpgUrl = "";
        if (originalUrl != null && !originalUrl.equals("")) {
            if (!originalUrl.startsWith("/")) {
                jpgUrl = "/" + originalUrl;
            } else {
                jpgUrl = originalUrl;
            }
        }
        return jpgUrl;
    }

    private void launchTestSnapshot() {
        if (mValidateHostInput.passed()) {
            final String username = usernameEdit.getText().toString();
            final String password = passwordEdit.getText().toString();
            final String externalHost = externalHostEdit.getText().toString();
            final String externalHttp = externalHttpEdit.getText().toString();
            final String jpgUrlString = jpgUrlEdit.getText().toString();
            final String jpgUrl = buildUrlEndingWithSlash(jpgUrlString);
            final String camera_exid = cameraEdit.getCameraId();


            String externalUrl = getString(R.string.prefix_http) + externalHost + ":" + externalHttp;

            new TestSnapshotTask(externalUrl, jpgUrl, username, password,
                    EditCameraActivity.this,modelSelectorFragment.getVendorIdFromSpinner(),camera_exid).executeOnExecutor(AsyncTask
                    .THREAD_POOL_EXECUTOR);
        }
    }

    public void buildSpinnerOnModelListResult(@NonNull ArrayList<Model> modelList) {
        if (cameraEdit != null && cameraEdit.hasModel()) {
            modelSelectorFragment.buildModelSpinner(modelList, cameraEdit.getModelId());
        } else {
            modelSelectorFragment.buildModelSpinner(modelList, null);
        }
    }

    public void buildSpinnerOnVendorListResult(@NonNull ArrayList<Vendor> vendorList) {
        // If the camera has vendor, show as selected in spinner
        if (cameraEdit != null && !cameraEdit.getVendor().isEmpty()) {
            modelSelectorFragment.buildVendorSpinner(vendorList, cameraEdit.getVendor());
        } else {
            modelSelectorFragment.buildVendorSpinner(vendorList, null);
        }
    }

    @Override
    public EditText getPublicIpEditText() {
        return externalHostEdit;
    }

    @Override
    public EditText getHttpEditText() {
        return externalHttpEdit;
    }

    @Override
    public EditText getRtspEditText() {
        return externalRtspEdit;
    }

    @Override
    public TextView getHttpStatusText() {
        return mHttpStatusTextView;
    }

    @Override
    public TextView getRtspStatusText() {
        return mRtspStatusTextView;
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
