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

import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAdParser;

import java.util.Map;

public class PrebidNativeAdapter extends CustomEventNative {

    private static final String KEY_BID_RESPONSE = "PREBID_BID_RESPONSE_ID";

    @Override
    protected void loadNativeAd(
        @NonNull
            Context context,
        @NonNull
            CustomEventNativeListener customEventNativeListener,
        @NonNull
            Map<String, Object> localExtras,
        @NonNull
            Map<String, String> serverExtras) {
        Object bidResponseObj = localExtras.get(KEY_BID_RESPONSE);
        if (!(bidResponseObj instanceof BidResponse)) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }
        NativeAdParser nativeAdParser = new NativeAdParser();
        NativeAd nativeAd = nativeAdParser.parse(((BidResponse) bidResponseObj).getWinningBid().getAdm());
        if (nativeAd == null) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.SERVER_ERROR_RESPONSE_CODE);
            return;
        }
        PrebidNativeAdWrapper prebidNativeAdWrapper = new PrebidNativeAdWrapper(nativeAd);
        customEventNativeListener.onNativeAdLoaded(prebidNativeAdWrapper);
    }
}
