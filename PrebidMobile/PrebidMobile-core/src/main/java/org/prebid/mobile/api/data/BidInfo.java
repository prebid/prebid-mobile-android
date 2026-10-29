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
    @Nullable
    private Double price;
    @Nullable
    private String currency;
    @Nullable
    private String creativeId;
    @Nullable
    private String adId;
    @Nullable
    private String requestId;

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

    /**
     * Net price (ORTB {@code Bid.price}, the exact CPM) of the bid whose targeting was attached,
     * or {@code null} when no bid was designated. A bid is designated when its targeting has
     * {@code hb_pb}, {@code hb_bidder} and {@code hb_cache_id}. A {@code null} price is not a
     * zero-price win.
     * <p>
     * When {@link #isTopBidFiltered()} is {@code true}, this and the other bid fields describe the
     * promoted cached runner-up, whose targeting was attached, not the bid Prebid Server designated.
     */
    @Nullable
    public Double getPrice() {
        return price;
    }

    /**
     * Currency (ISO-4217) of {@link #getPrice()}: the response's ORTB {@code cur}, forwarded as sent.
     * {@code null} when the response omits it (the SDK does not substitute the ORTB default) or no
     * bid was designated.
     */
    @Nullable
    public String getCurrency() {
        return currency;
    }

    /**
     * Creative id ({@code crid}) of the bid whose targeting was attached, or {@code null} when no
     * bid was designated.
     */
    @Nullable
    public String getCreativeId() {
        return creativeId;
    }

    /**
     * Ad id ({@code adid}) of the bid whose targeting was attached, or {@code null} when no bid
     * was designated.
     */
    @Nullable
    public String getAdId() {
        return adId;
    }

    /**
     * The Prebid request id: the ORTB {@code BidResponse.id}, a UUID the SDK generates for each bid
     * request. It identifies this request, not an auction. Unlike the bid fields, it is set for a
     * {@link ResultCode#SUCCESS} result even when no bid was designated. It is {@code null} for any
     * other result, including {@link ResultCode#NO_BIDS} and {@link ResultCode#NO_CACHED_BIDS}: the
     * SDK reports those as errors without the response, even when the response was parsed (for
     * example when {@link org.prebid.mobile.PrebidMobile#setFilterOutUncachedBids(boolean)} removed
     * every bid).
     */
    @Nullable
    public String getRequestId() {
        return requestId;
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
            bidInfo.price = winningBid.hasPrice() ? Double.valueOf(winningBid.getPrice()) : null;
            bidInfo.creativeId = winningBid.getCrid();
            bidInfo.adId = winningBid.getAdid();
            // BidResponse reads a missing cur as "". Forward it as sent: the SDK does not apply
            // the ORTB default currency.
            bidInfo.currency = nullIfEmpty(bidResponse.getCur());
        }

        bidInfo.requestId = nullIfEmpty(bidResponse.getId());

        boolean isNative = configuration != null && configuration.getNativeConfiguration() != null;
        if (isNative && bidInfo.resultCode == ResultCode.SUCCESS) {
            // NativeAdUnit's bid listener has usually saved the bid already: reuse that entry so
            // each response is cached once.
            @Nullable String cacheId = bidResponse.getNativeCacheId();
            if (cacheId == null) {
                cacheId = CacheManager.save(
                        bidResponse.getWinningBidJson(),
                        bidInfo.exp != null ? bidInfo.exp.longValue() : null
                );
                bidResponse.setNativeCacheId(cacheId);
            }
            bidInfo.nativeCacheId = cacheId;
            if (cacheId != null) {
                bidInfo.targetingKeywords.put(BidResponse.KEY_CACHE_ID, cacheId);
            }
        }

        return bidInfo;
    }

    @Nullable
    private static String nullIfEmpty(@Nullable String value) {
        return value == null || value.isEmpty() ? null : value;
    }

}
