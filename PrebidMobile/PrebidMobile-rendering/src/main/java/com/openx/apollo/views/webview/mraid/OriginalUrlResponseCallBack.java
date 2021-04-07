package com.openx.apollo.views.webview.mraid;

import com.openx.apollo.mraid.methods.network.RedirectUrlListener;
import com.openx.apollo.networking.BaseNetworkTask;
import com.openx.apollo.networking.ResponseHandler;
import com.openx.apollo.utils.logger.OXLog;

class OriginalUrlResponseCallBack implements ResponseHandler {
    private static final String TAG = OriginalUrlResponseCallBack.class.getSimpleName();

    private RedirectUrlListener mRedirectUrlListener;

    OriginalUrlResponseCallBack(RedirectUrlListener redirectUrlListener) {
        mRedirectUrlListener = redirectUrlListener;
    }

    @Override
    public void onResponse(BaseNetworkTask.GetUrlResult result) {
        if (result == null) {
            OXLog.error(TAG, "getOriginalURLCallback onResponse failed. Result is null");
            notifyFailureListener();
            return;
        }

        if (mRedirectUrlListener != null) {
            mRedirectUrlListener.onSuccess(result.originalUrl, result.contentType);
        }
    }

    @Override
    public void onError(String msg, long responseTime) {
        OXLog.error(TAG, "Failed with " + msg);
        notifyFailureListener();
    }

    @Override
    public void onErrorWithException(Exception e, long responseTime) {
        OXLog.error(TAG, "Failed with " + e.getMessage());
        notifyFailureListener();
    }

    private void notifyFailureListener() {
        if (mRedirectUrlListener != null) {
            mRedirectUrlListener.onFailed();
        }
    }
}
