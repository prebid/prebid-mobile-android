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

import androidx.annotation.RestrictTo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The Prebid targeting keys that Prebid Server sets on a bid's {@code ext.prebid.targeting}.
 * <p>
 * Prebid Server sends the generic form (e.g. {@code hb_pb}) only for the winning bid of an
 * impression, and the bidder-specific form (e.g. {@code hb_pb_appnexus}) for every bid. It may
 * truncate the bidder-specific form to its key length limit (20 characters by default), e.g.
 * {@code hb_cache_host_appnex}.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class TargetingKey {

    /**
     * The price bucket.
     */
    public static final String PRICE = "hb_pb";

    /**
     * The bidder code. Its bidder-specific form also gives the suffix of every other
     * bidder-specific key.
     */
    public static final String BIDDER = "hb_bidder";

    public static final String SIZE = "hb_size";
    public static final String FORMAT = "hb_format";
    public static final String DEAL = "hb_deal";

    /**
     * The Prebid Cache ID of the whole bid.
     */
    public static final String CACHE_ID = "hb_cache_id";

    /**
     * The Prebid Cache ID of the VAST XML.
     */
    public static final String VAST_CACHE_ID = "hb_uuid";

    public static final String CACHE_HOST = "hb_cache_host";
    public static final String CACHE_PATH = "hb_cache_path";
    public static final String ENVIRONMENT = "hb_env";

    /**
     * The generic keys the SDK fills in for a bid chosen by a bid selector, from that bid's own
     * bidder-specific keys. {@link #BIDDER} is resolved first, so it isn't listed here.
     */
    static final List<String> SELECTED_BID_KEYS = Collections.unmodifiableList(Arrays.asList(
            PRICE,
            SIZE,
            FORMAT,
            DEAL,
            CACHE_ID,
            VAST_CACHE_ID,
            CACHE_HOST,
            CACHE_PATH,
            ENVIRONMENT
    ));

    private TargetingKey() {}
}
