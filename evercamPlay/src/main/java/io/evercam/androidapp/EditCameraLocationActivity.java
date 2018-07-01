package io.evercam.androidapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

import io.evercam.PatchCameraBuilder;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.PatchCameraTask;

public class EditCameraLocationActivity extends ParentAppCompatActivity implements OnMapReadyCallback, LocationListener {


    private GoogleMap mMap;
    Location location;
    LatLng tappedLatLng;
    private String timeZone;
    public final static int PERMISSIONS_CODE = 56022;

    private EvercamCamera cameraToUpdate;

    private LocationManager testLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_camera_location);

        cameraToUpdate = ViewCameraActivity.evercamCamera;

        setUpDefaultToolbar();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        addMapTapListner();



        if (isLocationEnabled(this)) {
            // Add a marker in Sydney and move the camera
            // && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //,android.Manifest.permission.ACCESS_COARSE_LOCATION
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_CODE);
                return;
            } else {

                testLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                testLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, EditCameraLocationActivity.this);

                mMap.setMyLocationEnabled(true);
                addMarkerOnMap();
                addMyLocationButtonClickListner();
            }

        } else {
            addMarkerOnMap();
            showDialogGPS();
        }

    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;

//        }
//        else{
//            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//            return !TextUtils.isEmpty(locationProviders);
//        }


    }

    private void showDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Enable Location");
        builder.setMessage("Please enable Location.");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                        testLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        testLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                0, 0, EditCameraLocationActivity.this);
                        mMap.setMyLocationEnabled(true);
                        addMarkerOnMap();
                        addMyLocationButtonClickListner();

                    } else {
                        //No Permission granted
                        addMarkerOnMap();
                    }
                }
            }
        }
    }


    public void addMarkerOnMap() {
        LatLng loc = new LatLng(cameraToUpdate.getLatitude(), cameraToUpdate.getLongitude());
        mMap.addMarker(new MarkerOptions().position(loc));
        float zoomLevel = (float) 16.0; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
    }

    public void addMyLocationButtonClickListner() {
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //TODO: Any custom actions

                if (ActivityCompat.checkSelfPermission(EditCameraLocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(EditCameraLocationActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mMap.clear();

                    LocationManager locationManager =
                            (LocationManager) getSystemService(LOCATION_SERVICE);

                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, false);
                    location = locationManager.getLastKnownLocation(provider);

                    if (location != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new
                                LatLng(location.getLatitude(),
                                location.getLongitude()), 15));
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        tappedLatLng = latLng;

                        mMap.addMarker(new MarkerOptions().position(latLng));

                        new CallMashapeAsync().execute();

                    } else {

                        //Location Object is null May call Location Listener here
                    }

                }

                return false;
            }
        });
    }

    public void addMapTapListner() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.v("Lat and Lng", "" + latLng.toString());
                mMap.clear();
                tappedLatLng = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                new CallMashapeAsync().execute();
            }
        });
    }

    private class CallMashapeAsync extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {

        protected HttpResponse<JsonNode> doInBackground(String... msg) {

            HttpResponse<JsonNode> jsonResponse = null;
            try {
                String latitude = String.valueOf(tappedLatLng.latitude);
                String longitude = String.valueOf(tappedLatLng.longitude);
                jsonResponse = Unirest.get("https://maps.googleapis.com/maps/api/timezone/json?location=" + latitude + "," + longitude + "&timestamp=1482151170&key=AIzaSyAXwqGkwI87v4YoSGCq0FStNXr0")
                        .asJson();
                String str = jsonResponse.getBody().toString();
                Log.v("json First", str);
            } catch (UnirestException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return jsonResponse;
        }

        protected void onProgressUpdate(Integer... integers) {
        }

        protected void onPostExecute(HttpResponse<JsonNode> response) {

            String jsonString = response.getBody().toString();

            try {
                JSONObject jsonobj = new JSONObject(jsonString);
                timeZone = jsonobj.getString("timeZoneId");
//                cameraToUpdate.setTimezone(timeZone);
//                cameraToUpdate.setLatitude(tappedLatLng.latitude);
//                cameraToUpdate.setLongitude(tappedLatLng.longitude);
                Log.v("Time Zone", timeZone);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private PatchCameraBuilder buildPatchCameraWithLocalCheck() {

        cameraToUpdate.setTimezone(timeZone);
        cameraToUpdate.setLatitude(tappedLatLng.latitude);
        cameraToUpdate.setLongitude(tappedLatLng.longitude);

        PatchCameraBuilder patchCameraBuilder = new PatchCameraBuilder(cameraToUpdate.getCameraId());

        patchCameraBuilder.setTimeZone(cameraToUpdate.getTimezone());

        patchCameraBuilder.setLocation(String.format("%.7f", tappedLatLng.latitude), String.format("%.7f", tappedLatLng.longitude));

        return patchCameraBuilder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_edit_camera_location, menu);

        MenuItem supportMenuItem = menu.findItem(R.id.menu_action_support);
        if (supportMenuItem != null) {
            LinearLayout menuLayout = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.edit_location_menu_item, null);
            supportMenuItem.setActionView(menuLayout);
            supportMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menuLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PatchCameraBuilder patchCameraBuilder = buildPatchCameraWithLocalCheck();
                    if (patchCameraBuilder != null) {
                        new PatchCameraTask(patchCameraBuilder.build(),
                                EditCameraLocationActivity.this).executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR);
                    } else {
                        Log.e("Log", "Camera to patch is null");
                    }
                }
            });
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editItem = menu.findItem(R.id.menu_action_edit_camera);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        testLocationManager.removeUpdates(this);
    }


    @Override
    public void onLocationChanged(Location location) {

        //remove location callback:
//        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //,android.Manifest.permission.ACCESS_COARSE_LOCATION
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSIONS_CODE);
            return;
        }else{

            mMap.setMyLocationEnabled(true);
            addMyLocationButtonClickListner();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //,android.Manifest.permission.ACCESS_COARSE_LOCATION
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSIONS_CODE);
            return;
        }else{

            mMap.setMyLocationEnabled(false);
        }
    }

}
