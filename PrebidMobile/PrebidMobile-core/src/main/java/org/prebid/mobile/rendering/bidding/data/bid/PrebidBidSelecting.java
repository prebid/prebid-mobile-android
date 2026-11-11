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

import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.plugin.PluginCapability;
import org.prebid.mobile.api.plugin.PrebidPluginInfo;

import java.util.List;

/**
 * A plugin that chooses the winning bid of each auction, in place of the SDK's default choice.
 * Register it with {@link PrebidMobile#registerPlugin(Object, PluginCapability)} as
 * {@link PluginCapability#BID_SELECTOR}:
 *
 * <pre>{@code
 * public class HighestPriceSelector implements PrebidBidSelecting {
 *
 *     private final PrebidPluginInfo pluginInfo = new PrebidPluginInfo("com.example.highest-price", "1.0");
 *
 *     public PrebidPluginInfo getPluginInfo() {
 *         return pluginInfo;
 *     }
 *
 *     public Bid selectBid(List<Bid> bids, BidSelectionContext context) {
 *         Bid best = null;
 *         for (Bid bid : bids) {
 *             if (best == null || bid.getPrice() > best.getPrice()) {
 *                 best = bid;
 *             }
 *         }
 *         return best;
 *     }
 * }
 *
 * PrebidMobile.registerPlugin(new HighestPriceSelector(), PluginCapability.BID_SELECTOR);
 * }</pre>
 * <p>
 * A registered selector applies to every ad unit of the Original, Rendering and Mediation APIs. It
 * takes precedence over the default choice, which is the bid that Prebid Server marked as the
 * winner with the {@code hb_pb} and {@code hb_bidder} targeting keys.
 * <p>
 * For each auction the SDK:
 * <ol>
 *     <li>Drops the bids the ad unit can't use. With {@link PrebidMobile#setFilterOutUncachedBids(boolean)},
 *     the Original API drops the bids without a Prebid Cache entry.</li>
 *     <li>Calls {@link #selectBid(List, BidSelectionContext)} once, on the main thread, with the
 *     remaining bids.</li>
 *     <li>Builds the targeting keywords from the selected bid alone: its own keys, plus the generic
 *     keys ({@code hb_pb}, {@code hb_bidder}, {@code hb_cache_id}, ...) filled in from its
 *     bidder-specific keys when Prebid Server didn't pick it. The SDK adds these keys to the
 *     selected bid's targeting.</li>
 *     <li>Makes the selected bid the winner. If the ad unit sends targeting keywords to an ad
 *     server and the selected bid's keywords lack the winner keys, the auction ends with
 *     {@link ResultCode#INVALID_TARGETING} instead. Ad units that render the bid without an ad
 *     server don't need the keywords.</li>
 * </ol>
 * <p>
 * The selector's decision is final. If it returns {@code null}, or an object that isn't one of
 * the bids it was given, the auction has no winner: no targeting keywords are attached and no ad
 * is rendered. The SDK never falls back to its default choice.
 * <p>
 * At most one selector is registered at a time.
 */
public interface PrebidBidSelecting {

    /**
     * The selector's name and version. The SDK uses it to detect duplicate registrations and in
     * logs. Must not change for the lifetime of the selector.
     */
    @NonNull
    PrebidPluginInfo getPluginInfo();

    /**
     * Returns the winning bid for the ad unit that {@code context} describes, or {@code null} for
     * no winner.
     * <p>
     * Called once per auction, on the main thread, so it must return immediately: no network
     * calls, disk I/O or other blocking work. It must not change the bids or start another
     * auction.
     *
     * @param bids    the bids the ad unit can use. Never empty.
     * @param context the ad unit that started the auction.
     * @return one of the objects in {@code bids}, or {@code null}.
     */
    @Nullable
    Bid selectBid(@NonNull List<Bid> bids, @NonNull BidSelectionContext context);

    /**
     * Called once after the selector is registered, before the SDK calls it for an auction.
     */
    default void onRegister() {}

    /**
     * Called when the selector is unregistered. Release resources here.
     */
    default void onUnregister() {}
}
