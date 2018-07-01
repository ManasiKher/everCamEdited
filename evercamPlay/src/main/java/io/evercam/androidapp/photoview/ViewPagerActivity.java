package io.evercam.androidapp.photoview;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;
import uk.co.senab.photoview.PhotoView;

public class ViewPagerActivity extends ParentAppCompatActivity {
    private final String TAG = "ViewPagerActivity";
    private ViewPager mViewPager;
    private SnapshotPagerAdapter mViewPagerAdapter;
    private FrameLayout mPlaceHolderLayout;
    private static List<String> mImagePathList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_image);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        ImageButton shareImageButton = (ImageButton) findViewById(R.id.control_button_share);
        ImageButton deleteImageButton = (ImageButton) findViewById(R.id.control_button_delete);
        mPlaceHolderLayout = (FrameLayout) findViewById(R.id.place_holder_layout);

        setUpGradientToolbarWithHomeButton();

        mViewPagerAdapter = new SnapshotPagerAdapter();
        mViewPager.setAdapter(mViewPagerAdapter);

        updateTitleWithPage(1); //Initial title as 1 of total pages

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int
                    positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateTitleWithPage(position + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchShareIntent(mImagePathList.get(mViewPager.getCurrentItem()));
            }
        });

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomedDialog.getConfirmDeleteDialog(ViewPagerActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int currentPosition = mViewPager.getCurrentItem();
                        String currentPath = mImagePathList.get(currentPosition);
                        File imageFile = new File(currentPath);
                        boolean isDeleted = imageFile.delete();
                        if (isDeleted) {
                            updateViewAfterDelete(currentPosition);
                            showSnapshotDeletedSnackbar();
                        }
                    }
                }, R.string.msg_confirm_delete_snapshot).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Constants.RESULT_FALSE);
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        setResult(Constants.RESULT_FALSE);
        finish();
    }

    /**
     * Call this method to launch ViewPagerActivity by passing the image path array
     *
     * @param activity   The previous activity that launch the ViewPagerActivity
     * @param imagePaths Snapshots image path string array
     */
    public static void showSavedSnapshots(Activity activity, String[] imagePaths) {
        mImagePathList = new ArrayList<>(Arrays.asList(imagePaths));
        VideoActivity.snapshotStarted = true;
        Intent intent = new Intent(activity, ViewPagerActivity.class);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_SNAPSHOT);
    }

    public void launchShareIntent(String imagePath) {
        Uri uri = Uri.fromFile(new File(imagePath));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, "Send to"));
    }

    public void updateViewAfterDelete(int position) {
        mViewPagerAdapter.removeView(position);
        if (mViewPagerAdapter.getCount() > 0) {
            updateTitleWithPage(mViewPager.getCurrentItem() + 1);
        }
        /* The last image has been deleted, close snapshot page*/
        else {
            CustomToast.showInCenter(this, R.string.msg_no_snapshot_saved_camera);
            finish();
        }
    }

    private void updateTitleWithPage(int currentPageNumber) {
        updateTitleText(currentPageNumber + " of " + mImagePathList.size());
    }

    public void showSnapshotDeletedSnackbar() {
        Snackbar.make(mPlaceHolderLayout, R.string.msg_snapshot_deleted, Snackbar.LENGTH_SHORT)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        mPlaceHolderLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                        super.onShown(snackbar);
                        mPlaceHolderLayout.setVisibility(View.INVISIBLE);
                    }
                })
                .show();
    }

    static class SnapshotPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mImagePathList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setImageURI(Uri.parse(mImagePathList.get(position)));

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void removeView(int position) {
            mImagePathList.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (mImagePathList.contains((View) object)) {
                return mImagePathList.indexOf((View) object);
            } else {
                return POSITION_NONE;
            }
        }
    }
}
