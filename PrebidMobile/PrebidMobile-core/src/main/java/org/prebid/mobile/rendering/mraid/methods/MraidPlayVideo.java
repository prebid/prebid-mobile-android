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

package org.prebid.mobile.rendering.mraid.methods;

import android.content.Context;
import android.text.TextUtils;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.sdk.ManagersResolver;

public class MraidPlayVideo {

    private static final String TAG = MraidPlayVideo.class.getSimpleName();

    public void playVideo(
        String url,
        Context context
    ) {

        if (TextUtils.isEmpty(url)) {
            LogUtil.error(TAG, "playVideo(): Failed. Provided url is empty or null");
            return;
        }
        ManagersResolver.getInstance().getDeviceManager().playVideo(url, context);
    }

}
