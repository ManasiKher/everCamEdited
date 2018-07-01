package io.evercam.androidapp.sharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.badoo.mobile.util.WeakHandler;

import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomSnackbar;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.FetchShareListTask;
import io.evercam.androidapp.tasks.ValidateRightsRunnable;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;

public class SharingActivity extends ParentAppCompatActivity {
    private static final String TAG = "SharingActivity";

    public static EvercamCamera evercamCamera;

    public SharingListFragment sharingListFragment;

    private MenuItem transferMenu;
    private WeakHandler mWeakHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        evercamCamera = VideoActivity.evercamCamera;

        mWeakHandler = new WeakHandler();

        //Validate if the user still has access to the camera
        if (evercamCamera != null) {
            new Thread(new ValidateRightsRunnable(this, evercamCamera.getCameraId())).start();
        }

        setContentView(R.layout.activity_sharing);

        setUpDefaultToolbar();

        sharingListFragment = new SharingListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, sharingListFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_sharing, menu);

        transferMenu = menu.findItem(R.id.menu_transfer);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_create_share:
                Intent createShareIntent = new Intent(this, CreateShareActivity.class);
                startActivityForResult(createShareIntent, Constants.REQUEST_CODE_CREATE_SHARE);
                return true;

            case R.id.menu_transfer:
                showTransferDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CREATE_SHARE) {
            FetchShareListTask.launch(SharingActivity.evercamCamera.getCameraId(), this);

            if (resultCode == Constants.RESULT_SHARE_CREATED) {
                mWeakHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomSnackbar.showShort(SharingActivity.this, R.string.msg_share_created);
                    }
                }, 1000);
            } else if (resultCode == Constants.RESULT_SHARE_REQUEST_CREATED) {
                mWeakHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomSnackbar.showLong(SharingActivity.this, R.string.msg_share_request_created);
                    }
                }, 1000);
            }
        }
    }

    public void showTransferMenu(boolean show) {
        if (transferMenu != null) {
            transferMenu.setVisible(show);
        }
    }

    private void showTransferDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TransferOwnerDialogFragment dialogFragment = new TransferOwnerDialogFragment()
                .setCameraId(evercamCamera.getCameraId())
                .setUserList(sharingListFragment.getUsernameList());
        dialogFragment.show(fragmentManager, "Transfer dialog");
    }
}
