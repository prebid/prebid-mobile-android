package org.prebid.mobile.rendering.views.webview.mraid;

import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.utils.logger.OXLog;

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
