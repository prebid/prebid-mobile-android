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

package org.prebid.mobile.rendering.networking.tracking;

import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class TrackingManager {

    private static final String TAG = TrackingManager.class.getSimpleName();

    private static TrackingManager sInstance = null;

    private TrackingManager() {

    }

    public static TrackingManager getInstance() {
        if (sInstance == null) {
            sInstance = new TrackingManager();
        }
        return sInstance;
    }

    public void fireEventTrackingURL(String url) {
        ServerConnection.fireAndForget(url);
    }

    public void fireEventTrackingURLs(@Nullable List<String> urls) {
        if (urls == null) {
            LogUtil.debug(TAG, "fireEventTrackingURLs(): Unable to execute event tracking requests. Provided list is null");
            return;
        }
        for (String url : urls) {
            fireEventTrackingURL(url);
        }
    }

    public void fireEventTrackingImpressionURLs(ArrayList<String> impressionUrls) {
        for (String url : impressionUrls) {
            ServerConnection.fireAndForgetImpressionUrl(url);
        }
    }
}
