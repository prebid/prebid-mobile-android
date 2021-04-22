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

package org.prebid.mobile.eventhandlers.utils;

import android.os.Bundle;

import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;

import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.NativeFetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.NativeAdCallback;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.utils.ntv.NativeUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GamUtils {
    private static final String TAG = GamUtils.class.getSimpleName();
    static final HashSet<String> RESERVED_KEYS;

    private static final String KEY_IS_PREBID_CREATIVE = "isPrebid";

    static {
        RESERVED_KEYS = new HashSet<>();
    }

    public static void prepare(AdManagerAdRequest adRequest, NativeFetchDemandResult result) {
        Map<String, String> targeting = result.getKeyWordsMap();

        handleGamCustomTargetingUpdate(adRequest, targeting);
    }

    public static void handleGamCustomTargetingUpdate(AdManagerAdRequest adRequest, Map<String, String> keywords) {
        removeUsedCustomTargetingForGam(adRequest);

        if (keywords == null || keywords.isEmpty()) {
            OXLog.error(TAG, "prepare: Failed. Result contains invalid keywords");
            return;
        }

        if (keywords != null && !keywords.isEmpty()) {
            Bundle bundle = adRequest.getCustomTargeting();
            for (String key : keywords.keySet()) {
                bundle.putString(key, keywords.get(key));
                addReservedKeys(key);
            }
        }
    }

    public static void findNativeAd(NativeCustomFormatAd customFormatAd, NativeAdCallback callback) {
        if (customFormatAd == null || callback == null) {
            OXLog.error(TAG, "findNativeAd: Failed. Passed nativeTemplateAd or callback is invalid");
            return;
        }

        final CharSequence cacheIdText = customFormatAd.getText(BidResponse.KEY_CACHE_ID);
        final String cacheId = cacheIdText != null ? cacheIdText.toString() : null;
        final NativeFetchDemandResult fetchDemandResult = createSuccessDemandResult(cacheId);

        NativeUtils.findNativeAd(fetchDemandResult, callback);
    }

    public static void findNativeAd(NativeAd unifiedNativeAd, NativeAdCallback callback) {
        if (unifiedNativeAd == null || callback == null) {
            OXLog.error(TAG, "findNativeAd: Failed. Passed nativeTemplateAd or callback is invalid");
            return;
        }

        final String cacheId = unifiedNativeAd.getCallToAction();
        final NativeFetchDemandResult fetchDemandResult = createSuccessDemandResult(cacheId);

        NativeUtils.findNativeAd(fetchDemandResult, callback);
    }

    public static boolean didPrebidWin(NativeAd unifiedNativeAd) {
        if (unifiedNativeAd == null) {
            return false;
        }

        final String body = unifiedNativeAd.getBody();
        return KEY_IS_PREBID_CREATIVE.equals(body);
    }

    public static boolean didPrebidWin(NativeCustomFormatAd ad) {
        if (ad == null) {
            return false;
        }

        CharSequence isPrebidValue = ad.getText(KEY_IS_PREBID_CREATIVE);

        return isPrebidValue != null && "1".contentEquals(isPrebidValue);
    }

    private static void removeUsedCustomTargetingForGam(AdManagerAdRequest adRequestObj) {
        Bundle bundle = adRequestObj.getCustomTargeting();
        if (RESERVED_KEYS != null) {
            for (String key : RESERVED_KEYS) {
                bundle.remove(key);
            }
        }
    }

    private static NativeFetchDemandResult createSuccessDemandResult(String cacheId) {
        final NativeFetchDemandResult nativeFetchDemandResult = new NativeFetchDemandResult(FetchDemandResult.SUCCESS);
        final Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put(BidResponse.KEY_CACHE_ID, cacheId);

        nativeFetchDemandResult.setKeyWordsMap(keywordMap);
        return nativeFetchDemandResult;
    }

    private static void addReservedKeys(String key) {
        synchronized (RESERVED_KEYS) {
            RESERVED_KEYS.add(key);
        }
    }
}
