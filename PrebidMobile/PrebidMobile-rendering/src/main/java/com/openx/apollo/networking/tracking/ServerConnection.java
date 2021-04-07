package com.openx.apollo.networking.tracking;

import android.os.AsyncTask;

import com.openx.apollo.networking.BaseNetworkTask;
import com.openx.apollo.networking.ResponseHandler;
import com.openx.apollo.utils.helpers.AppInfoManager;

public class ServerConnection {

    public static void fireWithResult(String url, ResponseHandler responseHandler) {
        BaseNetworkTask networkTask = new BaseNetworkTask(responseHandler);
        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.url = url;
        params.requestType = "GET";
        params.userAgent = AppInfoManager.getUserAgent();
        params.name = "recordevents";

        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public static void fireAndForget(String resourceURL) {
        fireWithResult(resourceURL, null);
    }

    public static void fireAndForgetImpressionUrl(String impressionUrl) {

        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.url = impressionUrl;
        params.requestType = "GET";
        params.userAgent = AppInfoManager.getUserAgent();
        params.name = BaseNetworkTask.REDIRECT_TASK;

        BaseNetworkTask networkTask = new ImpressionUrlTask(null);
        networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}
