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

package com.mopub.nativeads;

import android.content.Context;
import androidx.annotation.NonNull;
import com.mopub.mediation.MoPubBaseMediationUtils;
import org.prebid.mobile.PrebidNativeAd;

import java.util.Map;

public class PrebidNativeAdapter extends CustomEventNative {

    @Override
    protected void loadNativeAd(
            @NonNull Context context,
            @NonNull CustomEventNativeListener moPubListener,
            @NonNull Map<String, Object> localExtras,
            @NonNull Map<String, String> serverExtras
    ) {
        Object bidResponseObj = localExtras.get(MoPubBaseMediationUtils.KEY_BID_RESPONSE);
        if (!(bidResponseObj instanceof String)) {
            moPubListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        String cacheId = (String) bidResponseObj;
        PrebidNativeAd nativeAd = PrebidNativeAd.create(cacheId);
        if (nativeAd == null) {
            moPubListener.onNativeAdFailed(NativeErrorCode.EMPTY_AD_RESPONSE);
            return;
        }

        PrebidNativeAdWrapper mapper = new PrebidNativeAdWrapper(nativeAd);
        moPubListener.onNativeAdLoaded(mapper);
    }
}
