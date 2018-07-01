package io.evercam.androidapp.scan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.network.discovery.DeviceInterface;

public class AllDevicesActivity extends ParentAppCompatActivity {
    private static ArrayList<DeviceInterface> mAllDevices = new ArrayList<>();
    private static AllDeviceAdapter allDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_devices);

        setUpDefaultToolbar();
        setHomeIconAsCancel();

        ListView deviceListView = (ListView) findViewById(R.id.all_device_list);
        allDeviceAdapter = new AllDeviceAdapter(this, R.layout.item_scan_list, mAllDevices);
        deviceListView.setAdapter(allDeviceAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static AllDeviceAdapter getAdapter() {
        return allDeviceAdapter;
    }

    public static void showAllDevices(Activity fromActivity, ArrayList<DeviceInterface> allDevices) {
        mAllDevices = allDevices;
        fromActivity.startActivity(new Intent(fromActivity, AllDevicesActivity.class));
    }
}
