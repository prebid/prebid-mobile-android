/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.networking.modelcontrollers;

import android.os.AsyncTask;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.BaseResponseHandler;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

public class AsyncVastLoader {

    private AsyncTask videoRequestAsyncTask;

    public void loadVast(String vastUrl, BaseResponseHandler responseHandler) {
        cancelTask();

        BaseNetworkTask videoRequestTask = new BaseNetworkTask(responseHandler);
        BaseNetworkTask.GetUrlParams params = Utils.parseUrl(vastUrl);
        params.userAgent = AppInfoManager.getUserAgent();

        if (vastUrl != null) {
            params.requestType = "GET";
            params.name = "videorequest";
        }

        videoRequestAsyncTask = videoRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public void cancelTask() {
        if (videoRequestAsyncTask != null) {
            videoRequestAsyncTask.cancel(true);
        }
    }
}
