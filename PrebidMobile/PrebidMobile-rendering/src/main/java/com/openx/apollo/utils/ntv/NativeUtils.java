package com.openx.apollo.utils.ntv;

import androidx.annotation.NonNull;

import com.openx.apollo.bidding.data.NativeFetchDemandResult;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.data.ntv.NativeAd;
import com.openx.apollo.bidding.data.ntv.NativeAdParser;
import com.openx.apollo.bidding.display.BidResponseCache;
import com.openx.apollo.bidding.listeners.NativeAdCallback;
import com.openx.apollo.utils.logger.OXLog;

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