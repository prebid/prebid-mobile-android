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
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.utils.helpers.Utils;

/**
 * Manages the JS files in SDK
 * Provides JS scripts extracted from bundled resource
 */
public class JSLibraryManager {

    private static JSLibraryManager sInstance;

    private Context context;
    private String MRAIDscript;
    private String OMSDKscirpt;

    private JSLibraryManager(Context context) {
        this.context = context.getApplicationContext();
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
        return MRAIDscript;
    }

    public String getOMSDKScript() {
        return OMSDKscirpt;
    }

    private void initScriptStrings() {
        Resources resources = context.getResources();
        MRAIDscript = Utils.loadStringFromFile(resources, R.raw.mraid);
        OMSDKscirpt = Utils.loadStringFromFile(resources, R.raw.omsdk_v1);
    }
}
