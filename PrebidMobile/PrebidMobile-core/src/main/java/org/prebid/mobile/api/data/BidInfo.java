package org.prebid.mobile.api.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.CacheManager;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.Map;

public class BidInfo {

    @NonNull
    private final ResultCode resultCode;
    @Nullable
    private final Map<String, String> targetingKeywords;
    @Nullable
    private final Map<String, String> events;
    @Nullable
    private String nativeCacheId;
    @Nullable
    private Integer exp;

    /**
     * Key for {@link #getEvents()} map to get win event.
     */
    public static final String EVENT_WIN = "ext.prebid.events.win";
    /**
     * Key for {@link #getEvents()} map to get impression event.
     */
    public static final String EVENT_IMP = "ext.prebid.events.imp";


    private BidInfo(
            @NonNull ResultCode resultCode,
            @Nullable Map<String, String> targetingKeywords,
            @Nullable Map<String, String> events
    ) {
        this.resultCode = resultCode;
        this.targetingKeywords = targetingKeywords;
        this.events = events;
    }

    @NonNull
    public ResultCode getResultCode() {
        return resultCode;
    }

    @Nullable
    public Map<String, String> getTargetingKeywords() {
        return targetingKeywords;
    }

    @Nullable
    public String getNativeCacheId() {
        return nativeCacheId;
    }

    @Nullable
    public Integer getExp() {
        return exp;
    }

    @Nullable
    public Map<String, String> getEvents() {
        return events;
    }


    @NonNull
    public static BidInfo create(@NonNull ResultCode resultCode, @Nullable BidResponse bidResponse) {
        if (bidResponse == null) {
            return new BidInfo(resultCode, null, null);
        }

        Bid winningBid = bidResponse.getWinningBid();
        Map<String, String> events = null;
        if (winningBid != null) {
            events = winningBid.getEvents();
        }

        return new BidInfo(resultCode, bidResponse.getTargeting(), events);
    }

    public static void saveNativeResult(@NonNull BidInfo bidInfo, @Nullable BidResponse bidResponse, @Nullable Object adObject) {
        if (bidInfo.resultCode == ResultCode.SUCCESS && bidResponse != null) {
            String cacheId = CacheManager.save(bidResponse.getWinningBidJson());
            Util.saveCacheId(cacheId, adObject);
            bidInfo.nativeCacheId = cacheId;
            bidInfo.exp = bidResponse.getExpirationTimeSeconds();
        }
    }

}
