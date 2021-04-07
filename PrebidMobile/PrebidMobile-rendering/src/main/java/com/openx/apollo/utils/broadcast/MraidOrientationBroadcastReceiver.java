package com.openx.apollo.utils.broadcast;

import android.os.Build;

import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.JSInterface;

import java.lang.ref.WeakReference;

public class MraidOrientationBroadcastReceiver extends OrientationBroadcastReceiver {
    private static final String TAG = MraidOrientationBroadcastReceiver.class.getSimpleName();

    private final WeakReference<BaseJSInterface> mBaseJSInterfaceWeakReference;

    private String mMraidAction;
    private String mState;

    public MraidOrientationBroadcastReceiver(BaseJSInterface baseJSInterface) {
        mBaseJSInterfaceWeakReference = new WeakReference<>(baseJSInterface);
    }

    @Override
    public void handleOrientationChange(int currentRotation) {
        super.handleOrientationChange(currentRotation);
        BaseJSInterface baseJSInterface = mBaseJSInterfaceWeakReference.get();
        if (baseJSInterface == null) {
            OXLog.debug(TAG, "handleOrientationChange failure. BaseJsInterface is null");
            return;
        }

        if (shouldHandleClose()) {
            OXLog.debug(TAG, "Call 'close' action for MRAID Resize after changing rotation for API 19.");
            baseJSInterface.close();
        }
    }

    public void setState(String state) {
        mState = state;
    }

    public void setMraidAction(String action) {
        mMraidAction = action;
    }

    private boolean shouldHandleClose() {
        return mState != null
               && !JSInterface.STATE_DEFAULT.equals(mState)
               && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT
               && JSInterface.ACTION_RESIZE.equals(mMraidAction);
    }
}
