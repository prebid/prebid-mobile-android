package com.openx.apollo.networking.modelcontrollers;

import android.os.AsyncTask;

import com.openx.apollo.networking.BaseNetworkTask;
import com.openx.apollo.networking.BaseResponseHandler;
import com.openx.apollo.utils.helpers.AppInfoManager;
import com.openx.apollo.utils.helpers.Utils;

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
