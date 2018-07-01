package io.evercam.androidapp;

import android.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.androidapp.addeditcamera.ModelSelectorFragment;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.DeleteCameraTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EnumConstants;
import io.evercam.androidapp.video.VideoActivity;

public class ViewCameraActivity extends ParentAppCompatActivity implements OnMapReadyCallback {
    private final String TAG = "ViewCameraActivity";
    private LinearLayout canEditDetailLayout;
//    private TextView cameraIdTextView;
    private TextView cameraNameTextView;
//    private TextView cameraOwnerTextView;
    private TextView cameraTimezoneTextView;
    private TextView cameraVendorTextView;
    private TextView cameraModelTextView;
//    private TextView cameraUsernameTextView;
//    private TextView cameraPasswordTextView;
//    private TextView cameraSnapshotUrlTextView;
//    private TextView cameraRtspUrlTextView;
//    private TextView cameraInternalHostTextView;
//    private TextView cameraInternalHttpTextView;
//    private TextView cameraInternalRtspTextView;
//    private TextView cameraExternalHostTextView;
//    private TextView cameraExternalHttpTextView;
//    private TextView cameraExternalRtspTextView;
//    private Button editLinkButton;
    private Button editLocationButton;

    public static EvercamCamera evercamCamera;

    public static EvercamCamera newCamera;

    protected GoogleApiClient mGoogleApiClient;
    private LocationManager locManager;
    private GoogleMap mMap;
    Location mLastLocation;
    double lat =53.350140, lng=-6.266155;
    public final static int PERMISSIONS_CODE = 56022;

    private TreeMap<String, String> vendorMap;
    ImageView vendorLogoImageView;
    ImageView modelThumbnailImageView;


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng loc = new LatLng(evercamCamera.getLatitude(), evercamCamera.getLongitude());
        mMap.addMarker(new MarkerOptions().position(loc));
        float zoomLevel = (float) 16.0; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
/*
        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            return;
        }else{
//            mMap.setMyLocationEnabled(true);
            LatLng loc = new LatLng(evercamCamera.getLatitude(), evercamCamera.getLongitude());
            mMap.addMarker(new MarkerOptions().position(loc));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            float zoomLevel = (float) 16.0; //This goes up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mMap.getUiSettings().setScrollGesturesEnabled(false);
//            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(false);
//            mMap.getUiSettings().setZoomControlsEnabled(false);





//            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//            try {
//                List<Address> list =  geocoder.getFromLocation(lat,lng,100);
//                Log.v("list",""+list.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    Log.v("Lat and Lng",""+latLng.toString());
                    mMap.clear();

                    mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    new CallMashapeAsync().execute();
                }
            });

        }
*/

        //add this here:



    }
    private class CallMashapeAsync extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {

        protected HttpResponse<JsonNode> doInBackground(String... msg) {

            HttpResponse<JsonNode> jsonResponse = null;
            try {
                jsonResponse = Unirest.get("https://maps.googleapis.com/maps/api/timezone/json?location=53.350140,-6.266155&timestamp=1482151170&key=AIzaSyAXwqGkwI87v4YoSGCq0FStNXr0")
                        .asJson();
                String str = jsonResponse.getBody().toString();
                Log.v("json First",str);
            } catch (UnirestException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return jsonResponse;
        }

        protected void onProgressUpdate(Integer...integers) {
        }

        protected void onPostExecute(HttpResponse<JsonNode> response) {
            String answer = response.getBody().toString();
            Log.v("Json",answer);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        //            mMap.setMyLocationEnabled(true);
                        LatLng loc = new LatLng(evercamCamera.getLatitude(), evercamCamera.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(loc));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                        float zoomLevel = (float) 16.0; //This goes up to 21
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mMap.getUiSettings().setScrollGesturesEnabled(false);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.getUiSettings().setZoomGesturesEnabled(false);
//            mMap.getUiSettings().setZoomControlsEnabled(false);
                    }else{
                        //No Permission granted
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        evercamCamera = VideoActivity.evercamCamera;

        setContentView(R.layout.activity_view_camera);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        setUpDefaultToolbar();

        // Initial UI elements
        initialScreen();
        fillCameraDetails(evercamCamera);
        vendorLogoImageView = (ImageView) findViewById(R.id.vendor_logo_image_view);
        modelThumbnailImageView = (ImageView) findViewById(R.id.model_thumbnail_image_view);

        new RequestVendorListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    class RequestVendorListTask extends AsyncTask<Void, Void, ArrayList<Vendor>> {

        @Override
        protected void onPostExecute(ArrayList<Vendor> vendorList) {
            if (vendorList != null) {
                buildVendorSpinner(vendorList, evercamCamera.getVendor());
            } else {
                Log.e(TAG, "Vendor list is null");
            }
        }
        @Override
        protected ArrayList<Vendor> doInBackground(Void... params) {
            try {
                return Vendor.getAll();
            } catch (EvercamException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }

    public void buildVendorSpinner(ArrayList<Vendor> vendorList, String selectedVendor) {
        if (vendorMap == null) {
            vendorMap = new TreeMap<>();
        }

//        if (vendorMapIdAsKey == null) {
//            vendorMapIdAsKey = new TreeMap<>();
//        }

        if (vendorList != null) {
            for (Vendor vendor : vendorList) {
                try {
                    vendorMap.put(vendor.getName(), vendor.getId());
//                    vendorMapIdAsKey.put(vendor.getId(), vendor.getName());
                } catch (EvercamException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
        String vendorId = vendorMap.get(evercamCamera.getVendor()).toLowerCase(Locale.UK);
        Log.v("VendorID","" + vendorId);

        if (!evercamCamera.getVendor().equals(getString(R.string.vendor_other))) {
            //Update vendor logo when vendor is selected
            Picasso.with(this).load(Vendor.getLogoUrl(vendorId)
            ).placeholder(android.R.color.transparent).into(vendorLogoImageView);

        }else{
            vendorLogoImageView.setImageResource(android.R.color.transparent);
        }

        Picasso.with(this)
                .load(Model.getThumbnailUrl(vendorId, evercamCamera.getModelId()))
                .placeholder(R.drawable.thumbnail_placeholder)
                .into(modelThumbnailImageView);

//        Set<String> set = vendorMap.keySet();
//        String[] vendorArray = Commons.joinStringArray(new String[]{getResources().getString(R
//                .string.select_vendor)}, set.toArray(new String[0]));
//        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),
//                android.R.layout.simple_spinner_item, vendorArray);
//        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner);
//
//        int selectedPosition = 0;
//        if (isAddActivity()) {
//            if (getAddActivity().isFromDiscoverAndHasVendor()) {
//                String vendorId = getAddActivity().getDiscoveredCamera().getVendor();
//                String vendorName = vendorMapIdAsKey.get(vendorId);
//                selectedPosition = spinnerArrayAdapter.getPosition(vendorName);
//            }
//        }
//        if (selectedVendor != null) {
//            selectedPosition = spinnerArrayAdapter.getPosition(selectedVendor);
//        }
//        vendorSpinner.setAdapter(spinnerArrayAdapter);
//
//        if (selectedPosition != 0) {
//            vendorSpinner.setSelection(selectedPosition);
//        }
//        /* If vendor state are saved but haven't been selected */
//        else if (vendorSavedSelectedPosition != 0
//                && vendorSpinner.getCount() > 1
//                && vendorSavedSelectedPosition < vendorSpinner.getCount()) {
//            vendorSpinner.setSelection(vendorSavedSelectedPosition);
//            vendorSavedSelectedPosition = 0; //Then reset it
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_camera_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editItem = menu.findItem(R.id.menu_action_edit_camera);

        if (evercamCamera != null) {
            if (evercamCamera.canEdit()) {
                editItem.setVisible(true);
            } else {
                editItem.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.menu_action_edit_camera) {
            linkToEditCamera();
        } else if (itemId == R.id.menu_action_delete_camera) {
            CustomedDialog.getConfirmDeleteDialog(ViewCameraActivity.this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface warningDialog, int which) {
                    if (evercamCamera != null) {
                        if (evercamCamera.canDelete()) {
                            new DeleteCameraTask(evercamCamera.getCameraId(), ViewCameraActivity.this, EnumConstants.DeleteType.DELETE_OWNED).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        } else {
                            new DeleteCameraTask(evercamCamera.getCameraId(), ViewCameraActivity.this, EnumConstants.DeleteType.DELETE_SHARE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                }
            }, R.string.msg_confirm_delete_camera).show();
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_PATCH_CAMERA || requestCode == Constants.REQUEST_CODE_EDIT_CAMERA_LOCATION) {
            //If camera details have been edited, return to live view
            if (resultCode == Constants.RESULT_TRUE) {
                setResult(Constants.RESULT_TRUE);
                finish();
            }
        }
    }

    private void initialScreen() {
        canEditDetailLayout = (LinearLayout) findViewById(R.id.can_edit_detail_layout);
        editLocationButton  = (Button) findViewById(R.id.locationButton) ;
        cameraNameTextView = (TextView) findViewById(R.id.view_name_value);
        cameraVendorTextView = (TextView) findViewById(R.id.view_vendor_value);
        cameraModelTextView = (TextView) findViewById(R.id.view_model_value);

        editLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkToEditCameraLocation();
            }
        });
    }

    private void fillCameraDetails(EvercamCamera camera) {
        if (camera != null) {
            cameraNameTextView.setText(camera.getName());
            if (camera.getVendor().isEmpty()) {
                setAsNotSpecified(cameraVendorTextView);
            } else {
                cameraVendorTextView.setText(camera.getVendor());
            }
            if (!camera.hasModel()) {
                setAsNotSpecified(cameraModelTextView);
            } else {
                cameraModelTextView.setText(camera.getModelName());
            }

            //Show more details if user has the rights
            fillCanEditDetails(camera);
        }
    }

    private void fillCanEditDetails(EvercamCamera camera) {
        canEditDetailLayout.setVisibility(View.VISIBLE);
        if (camera.canEdit()) {
//            editLinkButton.setVisibility(View.VISIBLE);
            canEditDetailLayout.setVisibility(View.VISIBLE);
            editLocationButton.setVisibility(View.VISIBLE);

            int externalHttp = camera.getExternalHttp();
            int externalRtsp = camera.getExternalRtsp();
            int internalHttp = camera.getInternalHttp();
            int internalRtsp = camera.getInternalRtsp();

        }else{
            editLocationButton.setVisibility(View.GONE);
        }
    }

    private void setAsNotSpecified(TextView textView) {
        textView.setText(R.string.not_specified);
        textView.setTextColor(Color.GRAY);
    }

    private void linkToEditCamera() {
        Intent intent = new Intent(ViewCameraActivity.this, EditCameraActivity.class);
        intent.putExtra(Constants.KEY_IS_EDIT, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_PATCH_CAMERA);
    }

    private void linkToEditCameraLocation() {
        Intent intent = new Intent(ViewCameraActivity.this, EditCameraLocationActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_EDIT_CAMERA_LOCATION);
//        intent.putExtra(Constants.KEY_IS_EDIT, true);
//        startActivityForResult(intent, Constants.REQUEST_CODE_PATCH_CAMERA);
    }
}
