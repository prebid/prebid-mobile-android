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

import androidx.annotation.Nullable;

import java.util.List;

/**
 * A publisher-supplied strategy for choosing the winning bid out of the bids returned by Prebid Server.
 * <p>
 * When set, {@link #selectBid(List)} takes precedence over the SDK's default winner selection
 * (based on the {@code hb_pb}/{@code hb_bidder} targeting markers).
 */
public interface PrebidBidSelecting {

    /**
     * Selects the winning bid from the given bids, or {@code null} if none should win.
     * <p>
     * Returning {@code null} is final: no bid's targeting keywords are attached and no ad is
     * rendered. The returned bid must be one of the bids passed in; any other value is treated
     * as {@code null}.
     *
     * @param bids all bids returned for the request.
     */
    @Nullable
    Bid selectBid(List<Bid> bids);
}
