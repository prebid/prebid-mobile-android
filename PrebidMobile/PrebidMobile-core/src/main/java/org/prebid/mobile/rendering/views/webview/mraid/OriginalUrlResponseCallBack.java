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

package org.prebid.mobile.rendering.views.webview.mraid;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;

class OriginalUrlResponseCallBack implements ResponseHandler {
    private static final String TAG = OriginalUrlResponseCallBack.class.getSimpleName();

    private RedirectUrlListener redirectUrlListener;

    OriginalUrlResponseCallBack(RedirectUrlListener redirectUrlListener) {
        this.redirectUrlListener = redirectUrlListener;
    }

    @Override
    public void onResponse(BaseNetworkTask.GetUrlResult result) {
        if (result == null) {
            LogUtil.error(TAG, "getOriginalURLCallback onResponse failed. Result is null");
            notifyFailureListener();
            return;
        }

        if (redirectUrlListener != null) {
            redirectUrlListener.onSuccess(result.originalUrl, result.contentType);
        }
    }

    @Override
    public void onError(String msg, long responseTime) {
        LogUtil.error(TAG, "Failed with " + msg);
        notifyFailureListener();
    }

    @Override
    public void onErrorWithException(Exception e, long responseTime) {
        LogUtil.error(TAG, "Failed with " + e.getMessage());
        notifyFailureListener();
    }

    private void notifyFailureListener() {
        if (redirectUrlListener != null) {
            redirectUrlListener.onFailed();
        }
    }
}
