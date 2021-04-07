package com.openx.apollo.networking;

public interface ResponseHandler extends BaseResponseHandler {
    void onResponse(BaseNetworkTask.GetUrlResult response);

    void onError(String msg, long responseTime);

    void onErrorWithException(Exception e, long responseTime);
}

