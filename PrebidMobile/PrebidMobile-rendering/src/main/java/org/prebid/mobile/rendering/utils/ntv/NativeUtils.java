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

package org.prebid.mobile.rendering.utils.ntv;

import androidx.annotation.NonNull;

import org.prebid.mobile.rendering.bidding.data.NativeFetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAdParser;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.listeners.NativeAdCallback;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.Map;

public class NativeUtils {
    private static final String TAG = NativeUtils.class.getSimpleName();

    public static void findNativeAd(
        @NonNull
            NativeFetchDemandResult fetchDemandResult,
        @NonNull
            NativeAdCallback callback) {

        final Map<String, String> keyWordsMap = fetchDemandResult.getKeyWordsMap();
        if (keyWordsMap == null || keyWordsMap.isEmpty()) {
            OXLog.error(TAG, "findNativeAd: Failed. Callback or keyword map is null.");
            return;
        }

        final String responseId = keyWordsMap.get(BidResponse.KEY_CACHE_ID);
        final BidResponse bidResponse = BidResponseCache.getInstance().popBidResponse(responseId);

        if (bidResponse == null || bidResponse.getWinningBid() == null) {
            OXLog.debug(TAG, "findNativeAd: Returning null. BidResponse is null or winning bid is null.");
            callback.onNativeAdReceived(null);
            return;
        }

        final String adm = bidResponse.getWinningBid().getAdm();
        final NativeAd nativeAd = new NativeAdParser().parse(adm);

        callback.onNativeAdReceived(nativeAd);
    }
}