package io.evercam.androidapp.sharing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.evercam.CameraShare;
import io.evercam.CameraShareInterface;
import io.evercam.CameraShareOwner;
import io.evercam.CameraShareRequest;
import io.evercam.EvercamObject;
import io.evercam.Right;
import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.AppData;

public class ShareListArrayAdapter extends ArrayAdapter<CameraShareInterface> {
    private List<CameraShareInterface> mCameraShareList;

    public ShareListArrayAdapter(Context context, int resource, List<CameraShareInterface>
            objects) {
        super(context, resource, objects);
        mCameraShareList = objects;
    }

    @Override
    public int getCount() {
        return mCameraShareList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.item_share_list, null);
        }

        TextView fullNameTextView = (TextView) view.findViewById(R.id.sharing_fullname_text_view);
        TextView emailTextView = (TextView) view.findViewById(R.id.sharing_email_text_view);
        TextView statusTextView = (TextView) view.findViewById(R.id.sharing_item_status_text_view);
        TextView youTextView = (TextView) view.findViewById(R.id.sharing_you_text_view);
        //Reset before updating
        youTextView.setVisibility(View.GONE);
        statusTextView.setText("");

        CameraShareInterface cameraShareInterface = mCameraShareList.get(position);

        if (cameraShareInterface != null) {
            Right rights = EvercamObject.getRightsFrom(cameraShareInterface);

            if (cameraShareInterface instanceof CameraShare) {
                fullNameTextView.setText(((CameraShare) cameraShareInterface).getFullName());
                emailTextView.setText(((CameraShare) cameraShareInterface).getUserEmail());
                rights = ((CameraShare) cameraShareInterface).getRights();
            } else if (cameraShareInterface instanceof CameraShareRequest) {
                fullNameTextView.setText(((CameraShareRequest) cameraShareInterface).getEmail());
                emailTextView.setText(R.string.pending);
                rights = ((CameraShareRequest) cameraShareInterface).getRights();
            } else if (cameraShareInterface instanceof CameraShareOwner) {
                CameraShareOwner owner = (CameraShareOwner) cameraShareInterface;
                fullNameTextView.setText(owner.getFullName());
                emailTextView.setText(owner.getEmail());
                statusTextView.setText(R.string.owner);
                boolean isOwner = owner.getUsername().equals(AppData.defaultUser.getUsername());
                youTextView.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            }

            if (rights != null) {
                if (rights.isFullRight()) statusTextView.setText(R.string.full_rights);
                else if (rights.isReadOnly()) statusTextView.setText(R.string.read_only);
            }
        }

        return view;
    }

    public List<CameraShareInterface> getShareList() {
        return mCameraShareList;
    }
}