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