package org.prebid.mobile.api.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.CacheManager;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.Map;

/**
 * Bid info object for {@link org.prebid.mobile.api.original.PrebidAdUnit}.
 */
public class BidInfo {

    @NonNull
    private final ResultCode resultCode;
    @Nullable
    private Map<String, String> targetingKeywords;
    @Nullable
    private Map<String, String> events;
    @Nullable
    private String nativeCacheId;
    @Nullable
    private Integer exp;
    private boolean topBidFiltered;

    /**
     * Key for {@link #getEvents()} map to get win event.
     */
    public static final String EVENT_WIN = "ext.prebid.events.win";
    /**
     * Key for {@link #getEvents()} map to get impression event.
     */
    public static final String EVENT_IMP = "ext.prebid.events.imp";


    private BidInfo(@NonNull ResultCode resultCode) {
        this.resultCode = resultCode;
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

    /**
     * {@code true} when {@link org.prebid.mobile.PrebidMobile#setFilterOutUncachedBids(boolean)} removed
     * the bid Prebid Server designated as the winner because it had no successful Prebid Cache entry,
     * and a lower-priced cached bid was promoted in its place.
     * <p>
     * The demand is still valid and its targeting is applied, so {@link #getResultCode()} stays
     * {@link ResultCode#SUCCESS}. Use this flag only to track the yield impact of the filtering.
     */
    public boolean isTopBidFiltered() {
        return topBidFiltered;
    }


    @NonNull
    public static BidInfo create(
            @NonNull ResultCode resultCode,
            @Nullable BidResponse bidResponse,
            @Nullable AdUnitConfiguration configuration
    ) {
        BidInfo bidInfo = new BidInfo(resultCode);
        if (bidResponse == null) {
            return bidInfo;
        }

        bidInfo.targetingKeywords = bidResponse.getTargeting();

        bidInfo.exp = bidResponse.getExpirationTimeSeconds();

        bidInfo.topBidFiltered = bidResponse.isTopBidFiltered();

        Bid winningBid = bidResponse.getWinningBid();
        if (winningBid != null) {
            bidInfo.events = winningBid.getEvents();
        }

        boolean isNative = configuration != null && configuration.getNativeConfiguration() != null;
        if (isNative && bidInfo.resultCode == ResultCode.SUCCESS) {
            final @Nullable String cacheId = CacheManager.save(bidResponse.getWinningBidJson());
            bidInfo.nativeCacheId = cacheId;
            if (cacheId != null) {
                bidInfo.targetingKeywords.put(BidResponse.KEY_CACHE_ID, cacheId);
            }
        }

        return bidInfo;
    }

}
