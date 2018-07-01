package io.evercam.androidapp.custom;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import io.evercam.androidapp.addeditcamera.AddCameraParentActivity;
import io.evercam.androidapp.tasks.PortCheckTask;

public class PortCheckEditText extends EditText {

    private final static String TAG = "PortCheckEditText";
    private final static int TRIGGER_PORT_CHECK = 1;
    private final long TRIGGER_DELAY_IN_MS = 700;
    private PortCheckTask.PortType portType = null;

    private PortCheckHandler handler = new PortCheckHandler(this);

    public PortCheckEditText(Context context) {
        super(context);
    }

    public PortCheckEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortCheckEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Clear the status text view when the text in EditText gets changed for the first time
     *
     * @param textViews The status text view(s) list to clear after text changes
     */
    public void hideStatusViewsOnTextChange(final TextView... textViews) {
        addTextChangedListener(new TextWatcher() {

            boolean isFirstTimeChange = true;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                // Clear the post status text that already exists
                if (isFirstTimeChange) {
                    for (TextView textView : textViews) {
                        hideView(textView);
                        isFirstTimeChange = false;
                    }
                }

                // Trigger port check when user finishes typing
                handler.removeMessages(TRIGGER_PORT_CHECK);
                handler.sendEmptyMessageDelayed(TRIGGER_PORT_CHECK, TRIGGER_DELAY_IN_MS);
            }
        });
    }

    private void hideView(TextView textView) {
        textView.setVisibility(View.GONE);
    }

    /**
     * Return the port number if it's valid. Otherwise return 0.
     *
     * @return a number between 0 and 65535
     */
    public int getPort() {
        return (isPortStringValid() && !isEmpty()) ? Integer.valueOf(getText().toString()) : 0;
    }

    /**
     * It returns true if port string is valid
     * (A number between 0 - 65535 or empty)
     * So the situation of empty string should be handled afterwards
     */
    public boolean isPortStringValid() {
        String portString = getText().toString();
        //Allow port be patched to empty
        if (portString.isEmpty()) return true;
        try {
            int portInt = Integer.valueOf(portString);
            if (portInt > 0 && portInt <= 65535) {
                return true;
            }
        } catch (NumberFormatException e) {
            //The exception is handled outside the catch
        }
        return false;
    }

    public boolean isEmpty() {
        return getText().toString().isEmpty();
    }

    public void triggerPortCheck() {
        AddCameraParentActivity activity = (AddCameraParentActivity) getContext();
        if (activity != null) {
            if (portType != null) {
                activity.checkPort(portType);
            } else {
                activity.checkPort(PortCheckTask.PortType.HTTP);
                activity.checkPort(PortCheckTask.PortType.RTSP);
            }
        }
    }

    /**
     * Must be called after init view.
     * Otherwise the auto trigger won't work because the port type is not specified.
     *
     * @param portType HTTP or RTSP. null if it's an IP address view
     */
    public void setPortType(PortCheckTask.PortType portType) {
        this.portType = portType;
    }

    private static class PortCheckHandler extends Handler {
        private final WeakReference<PortCheckEditText> editTextReference;

        public PortCheckHandler(PortCheckEditText editText) {
            editTextReference = new WeakReference<>(editText);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TRIGGER_PORT_CHECK) {
                editTextReference.get().triggerPortCheck();
            }
        }
    }
}
