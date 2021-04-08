package org.prebid.mobile.rendering.networking;

public interface ResponseHandler extends BaseResponseHandler {
    void onResponse(BaseNetworkTask.GetUrlResult response);

    void onError(String msg, long responseTime);

    void onErrorWithException(Exception e, long responseTime);
}

