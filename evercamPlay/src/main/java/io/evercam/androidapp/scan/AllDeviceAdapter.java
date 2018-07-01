package io.evercam.androidapp.scan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.network.discovery.Device;
import io.evercam.network.discovery.DeviceInterface;

public class AllDeviceAdapter extends ArrayAdapter<DeviceInterface> {
    private final String TAG = "AllDeviceAdapter";
    private ArrayList<DeviceInterface> deviceList;

    public AllDeviceAdapter(Context context, int resource, ArrayList<DeviceInterface> deviceList) {
        super(context, resource, deviceList);
        this.deviceList = deviceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.item_device_list, null);
        }

        DeviceInterface deviceInterface = deviceList.get(position);
        if (deviceInterface != null) {
            final Device device = (Device) deviceInterface;
            TextView ipTextView = (TextView) view.findViewById(R.id.device_ip_text_view);
            TextView macTextView = (TextView) view.findViewById(R.id.device_mac_text_view);
            TextView companyTextView = (TextView) view.findViewById(R.id.device_company_text_view);
            TextView reportTextView = (TextView) view.findViewById(R.id.report_camera_button);

            ipTextView.setText(device.getIP());
            macTextView.setText(device.getMAC());
            companyTextView.setText(device.getPublicVendor());

            reportTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomedDialog.showReportCameraModelDialog(getContext(), device);
                }
            });
        }

        return view;
    }
}
