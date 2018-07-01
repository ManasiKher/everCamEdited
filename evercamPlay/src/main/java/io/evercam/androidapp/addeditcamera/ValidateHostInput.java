package io.evercam.androidapp.addeditcamera;

import io.evercam.androidapp.custom.PortCheckEditText;
import io.evercam.androidapp.utils.Commons;

public abstract class ValidateHostInput {
    final private PortCheckEditText mIpEditText;
    final private PortCheckEditText mHttpPortEditText;
    final private PortCheckEditText mRtspPortEditText;

    public ValidateHostInput(PortCheckEditText ipEditText,
                             PortCheckEditText httpPortEditText, PortCheckEditText rtspPortEditText) {
        mIpEditText = ipEditText;
        mHttpPortEditText = httpPortEditText;
        mRtspPortEditText = rtspPortEditText;
    }

    public boolean passed() {
        String externalHost = mIpEditText.getText().toString();

        if (externalHost.isEmpty()) {
            onHostEmpty();
            return false;
        } else {
            if (Commons.isLocalIp(externalHost)) {
                onLocalIp();
                return false;
            } else {
                String httpPort = mHttpPortEditText.getText().toString();

                if (httpPort.isEmpty()) {
                    onHttpEmpty();
                    return false;
                } else {
                    if (!mHttpPortEditText.isPortStringValid()) {
                        onInvalidHttpPort();
                        return false;
                    }
                }

                if (!mRtspPortEditText.isPortStringValid()) {
                    onInvalidRtspPort();
                    return false;
                }
            }
        }

        return true;
    }

    public abstract void onLocalIp();

    public abstract void onHostEmpty();

    public abstract void onHttpEmpty();

    public abstract void onInvalidHttpPort();

    public abstract void onInvalidRtspPort();
}
