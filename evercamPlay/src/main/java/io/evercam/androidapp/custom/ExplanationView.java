package io.evercam.androidapp.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.evercam.androidapp.R;

/**
 * The view that inflates from partial_explain_text_view.xml
 * This class provides interfaces for updating the title and message text
 */
public class ExplanationView extends LinearLayout {
    private final String TAG = "ExplanationView";

    public ExplanationView(Context context) {
        super(context);
    }

    public ExplanationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExplanationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateTitle(int titleId) {
        TextView titleTextView = (TextView) findViewById(R.id.explain_text_title);
        if (titleTextView != null) {
            if (titleId == 0) {
                titleTextView.setVisibility(View.GONE);
            } else {
                titleTextView.setText(titleId);
                titleTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void updateMessage(int messageId) {
        TextView messageTextView = (TextView) findViewById(R.id.explain_text_detail);
        if (messageTextView != null) {
            messageTextView.setText(messageId);
        }
    }
}
