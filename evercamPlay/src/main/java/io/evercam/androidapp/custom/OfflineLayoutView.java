package io.evercam.androidapp.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.evercam.androidapp.R;

public class OfflineLayoutView extends RelativeLayout {

    private final static String TAG = "OfflineLayoutView";
    private ImageView mRefreshImageView;
    private ProgressBar mRefreshProgressBar;
    private TextView mOfflineTextView;

    public OfflineLayoutView(Context context) {
        super(context);
    }

    public OfflineLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OfflineLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initChildenViews();
    }

    public void show() {
        show(true);
    }

    public void hide() {
        show(false);
    }

    public void startProgress() {
        showProgressView(true);
    }

    public void stopProgress() {
        showProgressView(false);
    }

    public void setOnRefreshClickListener(OnClickListener clickListener) {
        mRefreshImageView.setOnClickListener(clickListener);
    }

    private void initChildenViews() {
        mRefreshImageView = (ImageView) findViewById(R.id.offline_refresh_image_view);
        mRefreshProgressBar = (ProgressBar) findViewById(R.id.offline_refresh_progress_bar);
        mOfflineTextView = (TextView) findViewById(R.id.offline_text_view);
    }

    private void show(boolean show) {
        setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showProgressView(boolean show) {
        mRefreshProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mRefreshImageView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        mOfflineTextView.setText(show ? R.string.msg_refreshing : R.string.msg_offline);
        mOfflineTextView.setTextColor(show ? getResources().getColor(R.color.white) :
                getResources().getColor(R.color.offline_text));
    }
}
