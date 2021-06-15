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

package org.prebid.mobile.rendering.session.manager;

import org.prebid.mobile.rendering.bidding.data.ntv.NativeAdEventTracker;

public class NativeOmVerification {
    private final static String KEY_VENDOR_KEY = "vendorKey";
    private final static String KEY_VERIFICATION_PARAMETERS = "verification_parameters";

    private final String mOmidJsUrl;
    private String mVendorKey;
    private String mVerificationParameters;

    public NativeOmVerification(NativeAdEventTracker eventTracker) {
        mOmidJsUrl = eventTracker.getUrl();
        if (eventTracker.getExt() == null) {
            return;
        }
        mVendorKey = (String) eventTracker.getExt().getMap().get(KEY_VENDOR_KEY);
        mVerificationParameters = (String) eventTracker.getExt().getMap().get(KEY_VERIFICATION_PARAMETERS);
    }

    public String getOmidJsUrl() {
        return mOmidJsUrl;
    }

    public String getVendorKey() {
        return mVendorKey;
    }

    public String getVerificationParameters() {
        return mVerificationParameters;
    }
}
