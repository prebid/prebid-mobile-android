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

package org.prebid.mobile.rendering.models.internal;

import android.text.TextUtils;

import org.prebid.mobile.rendering.utils.helpers.MraidUtils;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

public class MraidVariableContainer {
    private static final String TAG = MraidVariableContainer.class.getSimpleName();

    private static final int SMS = 1;
    private static final int TEL = 2;
    private static final int CALENDAR = 4;
    private static final int STORE_PICTURE = 8;
    private static final int INLINE_VIDEO = 16;
    private static final int LOCATION = 32;
    private static final int VPAID = 64;

    private static String sDisabledFlags = null;

    private String mUrlForLaunching;
    private String mExpandProperties;
    private String mOrientationProperties;

    private String mCurrentState;
    private String mCurrentExposure;
    private Boolean mCurrentViewable = null;

    public String getUrlForLaunching() {
        return mUrlForLaunching == null ? "" : mUrlForLaunching;
    }

    public void setUrlForLaunching(String urlForLaunching) {
        mUrlForLaunching = urlForLaunching;
    }

    public String getExpandProperties() {
        return mExpandProperties;
    }

    public void setExpandProperties(String expandProperties) {
        mExpandProperties = expandProperties;
    }

    public String getOrientationProperties() {
        return mOrientationProperties;
    }

    public void setOrientationProperties(String orientationProperties) {
        mOrientationProperties = orientationProperties;
    }

    public boolean isLaunchedWithUrl() {
        return !TextUtils.isEmpty(mUrlForLaunching);
    }

    public static void setDisabledFlags(String disabledFlags) {
        sDisabledFlags = disabledFlags;
    }

    public static String getDisabledFlags() {
        return sDisabledFlags;
    }

    public String getCurrentState() {
        return mCurrentState;
    }

    public void setCurrentState(String currentState) {
        mCurrentState = currentState;
    }

    public String getCurrentExposure() {
        return mCurrentExposure;
    }

    public void setCurrentExposure(String currentExposure) {
        mCurrentExposure = currentExposure;
    }

    public Boolean getCurrentViewable() {
        return mCurrentViewable;
    }

    public void setCurrentViewable(Boolean currentViewable) {
        mCurrentViewable = currentViewable;
    }

    /**
     * Internal SDK use only
     */
    public static void setDisabledSupportFlags(int flags) {
        String[] features = {
            "sms",
            "tel",
            "calendar",
            "storePicture",
            "inlineVideo",
            "location",
            "vpaid"};
        int[] vars = {SMS, TEL, CALENDAR, STORE_PICTURE, INLINE_VIDEO, LOCATION, VPAID};

        StringBuilder disabledFlags = new StringBuilder();

        disabledFlags.append("mraid.allSupports = {");

        for (int i = 0; i < features.length; i++) {

            disabledFlags.append(features[i]);
            disabledFlags.append(":");
            disabledFlags.append((flags & vars[i]) == vars[i]
                      ? "false"
                      : MraidUtils.isFeatureSupported(features[i]));
            if (i < features.length - 1) {
                disabledFlags.append(",");
            }
        }

        disabledFlags.append("};");
        LogUtil.debug(TAG, "Supported features: " + disabledFlags.toString());

        setDisabledFlags(disabledFlags.toString());
    }
}
