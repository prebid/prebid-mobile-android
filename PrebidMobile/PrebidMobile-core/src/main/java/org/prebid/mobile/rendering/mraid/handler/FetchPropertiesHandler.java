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

package org.prebid.mobile.rendering.mraid.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;

public class FetchPropertiesHandler extends Handler {
    @NonNull private final FetchPropertyCallback callback;

    public FetchPropertiesHandler(
            @NonNull FetchPropertyCallback callback
    ) {
        super(Looper.getMainLooper());

        this.callback = callback;
    }

    @Override
    public void handleMessage(final Message message) {
        super.handleMessage(message);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            try {
                final String expandProperties = message.getData().getString(JSInterface.JSON_VALUE);
                callback.onResult(expandProperties);
            }
            catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface FetchPropertyCallback {
        void onResult(String propertyJson);

        void onError(Throwable throwable);
    }
}
