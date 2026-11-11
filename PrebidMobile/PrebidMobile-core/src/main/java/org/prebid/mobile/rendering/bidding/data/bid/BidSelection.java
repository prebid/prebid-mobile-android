/*
 *    Copyright 2018-2026 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.bidding.data.bid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.plugin.PluginCapability;
import org.prebid.mobile.configuration.AdUnitConfiguration;

import java.util.HashMap;
import java.util.List;

/**
 * Runs the registered {@link PrebidBidSelecting} on a bid response.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class BidSelection {

    private static final String TAG = BidSelection.class.getSimpleName();

    private BidSelection() {}

    /**
     * The registered bid selector, or {@code null} if there is none.
     */
    @Nullable
    public static PrebidBidSelecting getRegisteredSelector() {
        List<PrebidBidSelecting> selectors = PluginCapability.BID_SELECTOR.getRegistry().getActive();
        return selectors.isEmpty() ? null : selectors.get(0);
    }

    /**
     * Makes the bid that {@code selector} chooses among the bids of {@code response} its winner.
     * <p>
     * The selector is called once, and its decision is final: if it chooses no bid, a bid it
     * wasn't given, or a bid without the targeting the ad unit needs, no bid wins and the SDK's
     * default choice is not used. Must be called on the main thread.
     *
     * @return {@code null} when the selected bid won, or the error that ends the auction.
     */
    @Nullable
    public static AdException select(
            @NonNull PrebidBidSelecting selector,
            @NonNull BidResponse response,
            @NonNull AdUnitConfiguration adUnitConfiguration
    ) {
        String prefix = "[" + PluginCapability.BID_SELECTOR + "] ";

        // Uncached bids are already gone when the Original API filters them
        List<Bid> eligibleBids = response.getAllBids();
        if (eligibleBids.isEmpty()) {
            boolean allBidsUncached = response.isFilteringUncachedBids() && response.getBidsWithoutSuccessfulCacheCount() > 0;
            return new AdException(
                    AdException.NO_BIDS,
                    allBidsUncached ? FetchDemandResult.NO_CACHED_BIDS_MESSAGE : FetchDemandResult.NO_BIDS_MESSAGE
            );
        }

        BidSelectionContext context = new BidSelectionContext(
                adUnitConfiguration.getConfigId(),
                adUnitConfiguration.getAdFormats()
        );

        Bid selectedBid = selector.selectBid(eligibleBids, context);
        if (selectedBid == null) {
            LogUtil.info(TAG, prefix + selector.getPluginInfo() + " chose no bid.");
            return new AdException(AdException.NO_BIDS, FetchDemandResult.NO_BIDS_MESSAGE);
        }

        if (!containsInstance(eligibleBids, selectedBid)) {
            LogUtil.warning(TAG, prefix + selector.getPluginInfo() + " chose a bid that isn't one of the bids it was given. No bid wins.");
            return new AdException(AdException.NO_BIDS, FetchDemandResult.NO_BIDS_MESSAGE);
        }

        boolean requiresAdServerTargeting = !adUnitConfiguration.rendersWithoutAdServer();
        Prebid prebid = selectedBid.getPrebid();
        HashMap<String, String> ownTargeting = prebid.getTargeting();

        SelectedBidTargeting targeting = SelectedBidTargeting.make(ownTargeting);
        if (targeting.getTargeting() != null) {
            prebid.setTargeting(targeting.getTargeting());
        } else if (requiresAdServerTargeting) {
            return invalidTargeting(prefix, selector, targeting.getFailure());
        }
        // Otherwise the ad unit renders the bid itself and doesn't use its targeting

        if (requiresAdServerTargeting && !response.hasWinningKeywords(prebid)) {
            prebid.setTargeting(ownTargeting);
            String keys = TargetingKey.PRICE + ", " + TargetingKey.BIDDER
                    + (response.usesCache() ? ", " + TargetingKey.CACHE_ID : "");
            return invalidTargeting(prefix, selector, "it lacks one of " + keys);
        }

        response.commitSelectedBid(selectedBid);
        return null;
    }

    @NonNull
    private static AdException invalidTargeting(
            @NonNull String prefix,
            @NonNull PrebidBidSelecting selector,
            @Nullable String reason
    ) {
        LogUtil.warning(TAG, prefix + "The bid that " + selector.getPluginInfo() + " chose has no valid targeting: " + reason + ".");
        return new AdException(AdException.NO_BIDS, FetchDemandResult.INVALID_TARGETING_MESSAGE + ": " + reason + ".");
    }

    private static boolean containsInstance(@NonNull List<Bid> bids, @NonNull Bid bid) {
        for (Bid candidate : bids) {
            if (candidate == bid) {
                return true;
            }
        }
        return false;
    }
}
