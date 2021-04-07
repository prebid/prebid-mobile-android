package com.openx.apollo.bidding.display;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.utils.logger.OXLog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Holds OXBBidResponses in memory until they are used
 */
public class BidResponseCache {
    private static final String TAG = BidResponseCache.class.getSimpleName();

    /**
     * Maximum number of responses that are cached. This limit is intended to be very
     * conservative; it is not recommended to cache more than a few OXBBidResponses.
     */
    @VisibleForTesting
    static final int MAX_SIZE = 20;

    private static final long BID_RESPONSE_LIFE_TIME_IN_CACHE = 60 * 1000;//1 minute

    @NonNull
    private static Map<String, BidResponse> sCachedBidResponses =
        Collections.synchronizedMap(new HashMap<>());

    private static BidResponseCache sInstance;

    private BidResponseCache() {
    }

    public static synchronized BidResponseCache getInstance() {
        if (sInstance == null) {
            sInstance = new BidResponseCache();
        }
        return sInstance;
    }

    /**
     * Stores the { OXBBidResponse} in the cache. This OXBBidResponse will live until it is retrieved via
     * { #popBidResponse(String)}
     *
     * @param response Parsed bid response
     */
    void putBidResponse(
        final BidResponse response) {
        putBidResponse(response.getId(), response);
    }

    /**
     * Stores the { OXBBidResponse} in the cache. This OXBBidResponse will live until it is retrieved via
     * { #popBidResponse(String)}
     *
     * @param key      Custom key to store response
     * @param response Parsed bid response
     */
    void putBidResponse(String key, BidResponse response) {
        trimCache();
        // Ignore request when max size is reached.
        if (sCachedBidResponses.size() >= MAX_SIZE) {
            OXLog.error(TAG,
                        "Unable to cache OXBBidResponse. Please destroy some via #destroy() and try again.");
            return;
        }
        if (TextUtils.isEmpty(key)) {
            OXLog.error(TAG,
                        "Unable to cache OXBBidResponse. Key is empty or null.");
            return;
        }

        sCachedBidResponses.put(key, response);
        OXLog.debug(TAG, "Cached ad count after storing: " + getCachedResponsesCount());
    }

    @Nullable
    public BidResponse popBidResponse(
        @Nullable
        final String responseId) {
        OXLog.debug(TAG, "POPPING the response");

        BidResponse bidResponse = null;

        if (sCachedBidResponses.containsKey(responseId)) {
            //check if the available BidResponse is not stale

            bidResponse = sCachedBidResponses.remove(responseId);
        }
        else {
            OXLog.warn(TAG, "No cached ad to retrieve in the final map");
        }
        OXLog.debug(TAG, "Cached ad count after popping: " + getCachedResponsesCount());
        return bidResponse;
    }

    @VisibleForTesting
    static synchronized void trimCache() {
        final Iterator<Map.Entry<String, BidResponse>> iterator = sCachedBidResponses.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, BidResponse> entry = iterator.next();

            // If the response was removed from memory,
            // discard the entire associated Config.
            if (entry.getValue() == null) {
                iterator.remove();
            }
        }
        if (!sCachedBidResponses.isEmpty()) {
            removeStaleBidResponses(sCachedBidResponses);
        }
    }

    private static void removeStaleBidResponses(Map<String, BidResponse> bidResponseMap) {
        final Iterator<Map.Entry<String, BidResponse>> iterator = bidResponseMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, BidResponse> entry = iterator.next();

            // If the response was removed from memory,
            // discard the entire associated Config.
            if (entry.getValue() != null) {
                long saveTimeInMillis = entry.getValue().getCreationTime();

                // Checking if the current time is past the expiration time
                if (System.currentTimeMillis() > (saveTimeInMillis + BID_RESPONSE_LIFE_TIME_IN_CACHE)) {
                    iterator.remove();
                }
            }
        }
    }

    private static int getCachedResponsesCount() {
        return sCachedBidResponses.size();
    }

    @VisibleForTesting
    static void clearAll() {
        sCachedBidResponses.clear();
    }

    @VisibleForTesting
    static Map<String, BidResponse> getCachedBidResponses() {
        return sCachedBidResponses;
    }
}