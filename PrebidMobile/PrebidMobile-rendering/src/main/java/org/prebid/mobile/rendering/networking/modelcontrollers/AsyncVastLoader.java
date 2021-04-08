package org.prebid.mobile.rendering.networking.modelcontrollers;

import android.os.AsyncTask;

import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.BaseResponseHandler;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

public class AsyncVastLoader {

    private AsyncTask mVideoRequestAsyncTask;

    public void loadVast(String vastUrl, BaseResponseHandler responseHandler) {
        cancelTask();

        BaseNetworkTask videoRequestTask = new BaseNetworkTask(responseHandler);
        BaseNetworkTask.GetUrlParams params = Utils.parseUrl(vastUrl);
        params.userAgent = AppInfoManager.getUserAgent();

        if (vastUrl != null) {
            params.requestType = "GET";
            params.name = "videorequest";
        }

        mVideoRequestAsyncTask = videoRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public void cancelTask() {
        if (mVideoRequestAsyncTask != null) {
            mVideoRequestAsyncTask.cancel(true);
        }
    }
}
