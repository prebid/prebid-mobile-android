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

package org.prebid.mobile.rendering.sdk;

import android.content.Context;
import android.content.res.Resources;

import org.prebid.mobile.rendering.R;
import org.prebid.mobile.rendering.utils.helpers.Utils;

/**
 * Manages the JS files in SDK
 * Provides JS scripts extracted from bundled resource
 */
public class JSLibraryManager {
    private static JSLibraryManager sInstance;

    private Context mContext;
    private String mMRAIDscript;
    private String mOMSDKscirpt;

    private JSLibraryManager(Context context) {
        mContext = context.getApplicationContext();
        initScriptStrings();
    }

    public static JSLibraryManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (JSLibraryManager.class) {
                if (sInstance == null) {
                    sInstance = new JSLibraryManager(context);
                }
            }
        }
        return sInstance;
    }

    public String getMRAIDScript() {
        return mMRAIDscript;
    }

    public String getOMSDKScript() {
        return mOMSDKscirpt;
    }

    private void initScriptStrings() {
        Resources resources = mContext.getResources();
        mMRAIDscript = Utils.loadStringFromFile(resources, R.raw.mraid);
        mOMSDKscirpt = Utils.loadStringFromFile(resources, R.raw.omsdk_v1);
    }
}
