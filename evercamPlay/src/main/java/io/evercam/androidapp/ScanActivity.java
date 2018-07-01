package io.evercam.androidapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.badoo.mobile.util.WeakHandler;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.InputStream;
import java.util.ArrayList;

import io.evercam.androidapp.addeditcamera.AddCameraActivity;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.scan.AllDevicesActivity;
import io.evercam.androidapp.scan.ScanResultAdapter;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.tasks.ScanForCameraTask;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.DataCollector;
import io.evercam.network.EvercamDiscover;
import io.evercam.network.discovery.Device;
import io.evercam.network.discovery.DeviceInterface;
import io.evercam.network.discovery.DiscoveredCamera;
import io.evercam.network.query.EvercamQuery;

public class ScanActivity extends ParentAppCompatActivity {
    private final String TAG = "ScanActivity";

    private View scanProgressView;
    private View scanResultListView;
    private View scanResultNoCameraView;
    private ProgressBar progressBar;

    private ListView cameraListView;
    private MenuItem cancelMenuItem;
    private MenuItem showAllDeviceMenu;
    private Button showAllDeviceButton;
    private Button connectWifiButton;

    private ScanResultAdapter deviceAdapter;
    public ArrayList<DiscoveredCamera> discoveredCameras = new ArrayList<>();
    private ArrayList<DeviceInterface> nonCameraDevices = new ArrayList<>();
    private SparseArray<Drawable> drawableArray = new SparseArray<>();
    private ScanForCameraTask scanTask;

    private WeakHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);

        mHandler = new WeakHandler();

        setUpDefaultToolbar();

        scanProgressView = findViewById(R.id.scan_status_layout);
        scanResultListView = findViewById(R.id.scan_result_layout);
        scanResultNoCameraView = findViewById(R.id.scan_result_no_camera_layout);
        progressBar = (ProgressBar) findViewById(R.id.horizontal_progress_bar);

        cameraListView = (ListView) findViewById(R.id.scan_result_list);
        Button addManuallyButton = (Button) findViewById(R.id.button_add_camera_manually);
        showAllDeviceButton = (Button) findViewById(R.id.button_show_all_devices);
        connectWifiButton = (Button) findViewById(R.id.button_connect_wifi);

        connectWifiButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        View footerView = getLayoutInflater().inflate(R.layout.footer_scan_list, cameraListView, false);
        Button showAllDeviceFooterButton = (Button) footerView.findViewById(R.id.button_all_devices_in_list);
        cameraListView.addFooterView(footerView);

        deviceAdapter = new ScanResultAdapter(this, R.layout.item_scan_list, discoveredCameras,
                drawableArray);
        cameraListView.setAdapter(deviceAdapter);

        cameraListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                final DiscoveredCamera cameraInList = (DiscoveredCamera) cameraListView
                        .getItemAtPosition(position);

                if (cameraInList != null) {
                    //If camera has been added to Evercam, show warning dialog
                    if (isCameraAdded(cameraInList)) {
                        CustomedDialog.getStandardAlertDialog(ScanActivity.this, new Dialog
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                launchAddCameraPage(cameraInList);
                            }
                        }, R.string.msg_camera_has_been_added).show();
                    } else {
                        launchAddCameraPage(cameraInList);
                    }
                }
            }
        });

        showAllDeviceFooterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllDevicesActivity.showAllDevices(ScanActivity.this, nonCameraDevices);
            }
        });

        addManuallyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ScanActivity.this, AddCameraActivity.class)
                        , Constants.REQUEST_CODE_ADD_CAMERA);
            }
        });

        showAllDeviceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllDevicesActivity.showAllDevices(ScanActivity.this, nonCameraDevices);
            }
        });

        new ScanCheckInternetTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_scan, menu);

        cancelMenuItem = menu.findItem(R.id.action_cancel_scan);
        showAllDeviceMenu = menu.findItem(R.id.action_show_all_devices);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:

                if (isScanning()) {
                    showConfirmCancelScanDialog();
                } else {
                    finish();
                }
                return true;

            case R.id.action_cancel_scan:

                showConfirmCancelScanDialog();

                return true;

            case R.id.action_show_all_devices:

                AllDevicesActivity.showAllDevices(this, nonCameraDevices);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (isScanning()) {
            showConfirmCancelScanDialog();
        } else {
            finish();
        }
    }

    // Finish this activity and transfer the result from AddEditCameraActivity
    // to
    // CamerasActivity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_ADD_CAMERA) {
            Intent returnIntent = new Intent();

            if (resultCode == Constants.RESULT_TRUE) {
                setResult(Constants.RESULT_TRUE, returnIntent);
                finish();
            } else {
                setResult(Constants.RESULT_FALSE, returnIntent);
                // If no camera found, finish this page, otherwise stay in
                // scanning page to let the user choose another.
                if (discoveredCameras == null) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void launchAddCameraPage(DiscoveredCamera camera) {
        Intent intentAddCamera = new Intent(ScanActivity.this, AddCameraActivity.class);
        String lowerCaseModel = camera.getModel().toLowerCase();
        camera.setModel(lowerCaseModel);
        intentAddCamera.putExtra("camera", camera);
        startActivityForResult(intentAddCamera, Constants.REQUEST_CODE_ADD_CAMERA);
    }

    private void startDiscovery() {
        scanTask = new ScanForCameraTask(ScanActivity.this);
        scanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void stopDiscovery() {
        if (scanTask != null && !scanTask.isCancelled()) {
            scanTask.cancel(true);
        }
    }

    public void showTextProgress(boolean show) {
        scanProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showCameraListView(boolean show) {
        scanResultListView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showNoCameraView(boolean show) {
        scanResultNoCameraView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateScanResultMessage(int messageId) {
        TextView messageTextView = (TextView) findViewById(R.id.scan_result_message);
        messageTextView.setText(messageId);
    }

    public void showHorizontalProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showCancelMenuItem(final boolean show) {
        if (cancelMenuItem == null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (cancelMenuItem != null) cancelMenuItem.setVisible(show);
                }
            }, 1000);
        } else {
            cancelMenuItem.setVisible(show);
        }
    }

    public void showAllDeviceMenu(final boolean show) {
        if (showAllDeviceMenu == null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (showAllDeviceMenu != null) showAllDeviceMenu.setVisible(show);
                }
            }, 1000);
        } else {
            showAllDeviceMenu.setVisible(show);
        }
    }

    public void showAllDeviceButton(final boolean show) {
        showAllDeviceButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void showConnectWifiButton(final boolean show) {
        connectWifiButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void showConfirmCancelScanDialog() {
        CustomedDialog.getConfirmCancelScanDialog(ScanActivity.this, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopDiscovery();
                finish();
            }
        }).show();
    }

    public boolean isScanning() {
        return cancelMenuItem != null && cancelMenuItem.isVisible();
    }

    public void showScanResults(ArrayList<DiscoveredCamera> discoveredCameras) {
        if (discoveredCameras != null && discoveredCameras.size() > 0) {
            showCameraListView(true);
            showNoCameraView(false);

            EvercamDiscover.mergeDuplicateCameraFromList(discoveredCameras);
            deviceAdapter.notifyDataSetChanged();
        } else {
            showCameraListView(false);
            showNoCameraView(true);
        }
    }

    public void addNonCameraDevice(final Device device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nonCameraDevices.add(device);
                if(AllDevicesActivity.getAdapter() != null) {
                    AllDevicesActivity.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    public void addNewCameraToResultList(DiscoveredCamera discoveredCamera) {
        try {
            //Log.d(TAG, "New discovered camera: " + discoveredCamera.getIP());
            showCameraListView(true);
            showNoCameraView(false);
            showTextProgress(false);

            boolean merged = false; //The new device has been included in the device list or not

            if (discoveredCameras.size() > 0) {
                for (int index = 0; index < discoveredCameras.size(); index++) {
                    DiscoveredCamera originalCamera = discoveredCameras.get(index);

                    if (originalCamera.getIP().equals(discoveredCamera.getIP())) {
                        originalCamera.merge(discoveredCamera);

                        //Log.d(TAG, "Camera after merging: " + originalCamera.toString());

                        merged = true;
                        break;
                    }
                }
            }

            if (!merged) {
                discoveredCameras.add(discoveredCamera);

                new RetrieveThumbnailTask(discoveredCamera, discoveredCameras.indexOf(discoveredCamera)).executeOnExecutor
                        (AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        deviceAdapter.notifyDataSetChanged();
    }

    public static boolean isCameraAdded(DiscoveredCamera discoveredCamera) {
        ArrayList<EvercamCamera> cameras = AppData.evercamCameraList;
        if (cameras.size() > 0) {
            String discoveredMacAddress = discoveredCamera.getMAC();
            for (EvercamCamera camera : cameras) {
                String evercamCameraMac = camera.getMac();
                if (!evercamCameraMac.isEmpty() && evercamCameraMac.equals(discoveredMacAddress)) {
                    return true;
                }
            }
        }
        return false;
    }

    private class RetrieveThumbnailTask extends AsyncTask<Void, Void, Drawable> {
        private DiscoveredCamera discoveredCamera;
        private int position;

        public RetrieveThumbnailTask(DiscoveredCamera discoveredCamera, int position) {
            this.discoveredCamera = discoveredCamera;
            this.position = position;
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            discoveredCamera = EvercamQuery.fillDefaults(discoveredCamera);

            String thumbnailUrl = discoveredCamera.getModelThumbnail();

            Drawable drawable = null;

            if (!thumbnailUrl.isEmpty()) {
                try {
                    InputStream stream = Unirest.get(thumbnailUrl).asBinary().getRawBody();
                    drawable = Drawable.createFromStream(stream, "src");
                } catch (UnirestException e) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }

            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (drawable != null) {
                drawableArray.put(position, drawable);
            } else {
                Drawable questionImage = ScanActivity.this.getResources().getDrawable(R.drawable
                        .question_img_trans);
                drawableArray.put(position, questionImage);
            }

            deviceAdapter.notifyDataSetChanged();
        }
    }

    public void onScanningStarted() {
        showHorizontalProgress(true);
        showTextProgress(true);
        showCancelMenuItem(true);
        showAllDeviceMenu(false);
    }

    public void onScanningFinished(ArrayList<DiscoveredCamera> cameraList) {
        //Hide the scanning percentage
        updateScanPercentage(null);
        //Hide the scanning text
        showTextProgress(false);
        //Hide the horizontal progress bar
        showHorizontalProgress(false);
        //Hide the cancel button
        showCancelMenuItem(false);
        //Show all device menu item
        if (nonCameraDevices.size() > 0) {
            showAllDeviceMenu(true);
        }

        updateTitleText("Finished. " + cameraList.size() + " Camera(s) Found.");
    }

    public void updateScanPercentage(final Float percentageFloat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (percentageFloat == null) {
                    updateTitleText("");
                } else {
                    float percentf = percentageFloat;
                    int percentageInt = (int) percentf;
                    if (percentageInt < 100) {
                        updateTitleText("Scanning... " + percentageInt + '%');
                        progressBar.setProgress(percentageInt);
                    }
                }
            }
        });
    }

    class ScanCheckInternetTask extends CheckInternetTask {
        public ScanCheckInternetTask(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            //Check is Wifi connected or not first
//            showConnectWifiButton(false);
//            showAllDeviceButton(true);

            DataCollector dataCollector = new DataCollector(ScanActivity.this);
            if (dataCollector.isConnectedMobile() && !dataCollector.isConnectedWifi()) {
                //if (true) {
                this.cancel(true);
                showTextProgress(false);
                showNoCameraView(true);
                showCancelMenuItem(false);
                showAllDeviceButton(false);
                showConnectWifiButton(true);
                updateScanResultMessage(R.string.msg_must_wifi_for_scan);
            } else {
                showConnectWifiButton(false);
                showAllDeviceButton(true);
            }
        }

        @Override
        protected void onPostExecute(Boolean hasNetwork) {
            if (hasNetwork) {
                startDiscovery();
            } else {
                CustomedDialog.showInternetNotConnectDialog(ScanActivity.this);
            }
        }
    }
}
